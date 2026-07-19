package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

@ExtendWith(MockitoExtension.class)
public class FoodCsvImportUsecaseUnitTest {

  private static final String HEADER = "code\tproduct_name\tbrands\tenergy-kcal_100g";

  @Mock
  private FoodRepository foodRepository;

  @Mock
  private ImportJobRegistry jobRegistry;

  @Mock
  private FoodCsvImportDispatcher dispatcher;

  @Mock
  private FoodCsvRowMapper rowMapper;

  @Test
  public void splitsRowsIntoBatchesOfTheConfiguredSizeTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 2);
    List<CSVRecord> records = parseRows(
        "1\tApple\tAcme\t50",
        "2\tBanana\tAcme\t90",
        "3\tCherry\tAcme\t60",
        "4\tDate\tAcme\t280",
        "5\tElderberry\tAcme\t70"
    );
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.processRecords(records, status);

    // Assert - 5 rows at batch size 2 means 3 saveAll calls (2, 2, 1)
    verify(foodRepository, times(3)).saveAll(anyList());
    assertThat(status.getProcessedRows()).isEqualTo(5);
    assertThat(status.getImportedCount()).isEqualTo(5);
    assertThat(status.getSkippedCount()).isEqualTo(0);
  }

  @Test
  public void countsSkippedRowsThatFailTheQualityFilterTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    List<CSVRecord> records = parseRows(
        "1\tApple\tAcme\t50",
        "2\t \tAcme\t90"
    );
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.processRecords(records, status);

    // Assert
    assertThat(status.getProcessedRows()).isEqualTo(2);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getSkippedCount()).isEqualTo(1);
  }

  @Test
  public void countsRowsThatThrowDuringMappingAsErrorsTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, rowMapper, 10);
    List<CSVRecord> records = parseRows("1\tApple\tAcme\t50");
    when(rowMapper.map(any())).thenThrow(new IllegalStateException("boom"));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.processRecords(records, status);

    // Assert
    assertThat(status.getProcessedRows()).isEqualTo(1);
    assertThat(status.getErrorCount()).isEqualTo(1);
    assertThat(status.getImportedCount()).isEqualTo(0);
  }

  @Test
  public void retriesRecordByRecordWhenBatchViolatesUniqueConstraintTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    List<CSVRecord> records = parseRows(
        "1\tApple\tAcme\t50",
        "2\tBanana\tAcme\t90"
    );
    doThrow(new DataIntegrityViolationException("duplicate external_id"))
        .when(foodRepository).saveAll(anyList());
    when(foodRepository.save(any(FoodPersistence.class)))
        .thenReturn(null)
        .thenThrow(new DataIntegrityViolationException("duplicate external_id"));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.processRecords(records, status);

    // Assert
    verify(foodRepository).saveAll(anyList());
    verify(foodRepository, times(2)).save(any(FoodPersistence.class));
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getSkippedCount()).isEqualTo(1);
  }

  @Test
  public void importsFromAPlainFileAndDeletesItAfterwardsTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp");
    Files.writeString(file, HEADER + "\n1\tApple\tAcme\t50\n");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(Files.exists(file)).isFalse();
  }

  @Test
  public void marksTheJobAsFailedWhenTheFileCannotBeReadTest() {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path missingFile = Path.of(System.getProperty("java.io.tmpdir"), "does-not-exist-" + UUID
        .randomUUID() + ".csv");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.importFromFile(missingFile, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    assertThat(status.getFinishedAt()).isNotNull();
  }

  @Test
  public void importsFromAGzipCompressedFileTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp.gz");
    try (var out = Files.newOutputStream(file);
        var gzip = new GZIPOutputStream(out)) {
      gzip.write((HEADER + "\n1\tApple\tAcme\t50\n").getBytes(StandardCharsets.UTF_8));
    }
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getImportedCount()).isEqualTo(1);
  }

  @Test
  public void startImportStagesFileCreatesJobAndDispatchesTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    MultipartFile file = new MockMultipartFile(
        "file", "sample.csv", "text/csv",
        (HEADER + "\n1\tApple\tAcme\t50\n").getBytes(StandardCharsets.UTF_8));
    UUID jobId = UUID.randomUUID();
    FoodImportJobStatus status = new FoodImportJobStatus(jobId);
    when(jobRegistry.createJob()).thenReturn(status);

    // Act
    ImportJobStatusDto result = usecase.startImport(file);

    // Assert
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getState()).isEqualTo("RUNNING");
    verify(dispatcher).dispatch(eq(usecase), any(Path.class), eq(status));
  }

  private List<CSVRecord> parseRows(String... rows) throws IOException {
    StringBuilder csv = new StringBuilder(HEADER);
    for (String row : rows) {
      csv.append('\n').append(row);
    }
    try (CSVParser parser = CSVFormat.DEFAULT
        .withDelimiter('\t')
        .withFirstRecordAsHeader()
        .withIgnoreHeaderCase()
        .withTrim()
        .parse(new StringReader(csv.toString()))) {
      return parser.getRecords();
    }
  }
}
