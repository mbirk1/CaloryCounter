package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.dao.InvalidDataAccessResourceUsageException;
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
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

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
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

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
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

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
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRecords(records, status);

    // Assert
    verify(foodRepository).saveAll(anyList());
    verify(foodRepository, times(2)).save(any(FoodPersistence.class));
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getSkippedCount()).isEqualTo(1);
    // Backfill: the duplicate row's diet is written to the already-existing row, since a
    // re-import is the deliberately chosen way to backfill diet onto pre-existing rows.
    verify(foodRepository).updateDietByExternalId("2", "UNKNOWN");
  }

  @Test
  public void backfillsDietOntoAnAlreadyImportedVeganProductOnReImportTest() throws IOException {
    // Arrange - a re-import of a row that already exists (duplicate external_id) must backfill
    // its now-derivable diet onto the existing row, since a plain re-import otherwise only skips
    // duplicates without updating them. This is the deliberately chosen backfill mechanism (see
    // FoodCsvImportUsecase#persistOneByOne): no separate script or endpoint is needed, a normal
    // re-import of the original file is enough.
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    String headerWithDiet = HEADER + "\tingredients_analysis_tags";
    List<CSVRecord> records = parseRowsWithHeader(
        headerWithDiet, "42\tTofu\tAcme\t120\ten:vegan,en:vegetarian"
    );
    doThrow(new DataIntegrityViolationException("duplicate external_id"))
        .when(foodRepository).saveAll(anyList());
    doThrow(new DataIntegrityViolationException("duplicate external_id"))
        .when(foodRepository).save(any(FoodPersistence.class));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRecords(records, status);

    // Assert
    assertThat(status.getSkippedCount()).isEqualTo(1);
    verify(foodRepository).updateDietByExternalId("42", "VEGAN");
  }

  @Test
  public void fallsBackToRecordByRecordForAnyDataAccessExceptionNotJustDuplicatesTest()
      throws IOException {
    // Arrange - "numeric field overflow" (SQLState 22003) translates to a DataAccessException
    // subtype that is NOT DataIntegrityViolationException; persistBatch's fallback must still
    // kick in so one bad batch doesn't take the whole job down.
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    List<CSVRecord> records = parseRows("1\tApple\tAcme\t50");
    doThrow(new InvalidDataAccessResourceUsageException("numeric field overflow"))
        .when(foodRepository).saveAll(anyList());
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRecords(records, status);

    // Assert
    verify(foodRepository).saveAll(anyList());
    verify(foodRepository).save(any(FoodPersistence.class));
    assertThat(status.getImportedCount()).isEqualTo(1);
  }

  @Test
  public void rollsBackAndFailsTheJobWhenAnUnexpectedRuntimeExceptionEscapesProcessingTest()
      throws IOException {
    // Arrange - a plain RuntimeException is neither an IOException nor a DataAccessException, so
    // it escapes both persistBatch's fallback and mapRow's per-row catch entirely, reaching
    // importFromFile as a genuine job-level failure.
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp");
    Files.writeString(file, HEADER + "\n1\tApple\tAcme\t50\n");
    doThrow(new IllegalStateException("boom")).when(foodRepository).saveAll(anyList());
    UUID jobId = UUID.randomUUID();
    FoodImportJobStatus status = new FoodImportJobStatus(jobId, Files.size(file));

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    verify(foodRepository).deleteByImportJobId(jobId);
    assertThat(Files.exists(file)).isFalse();
  }

  @Test
  public void stillMarksTheJobAsFailedWhenTheRollbackDeleteItselfThrowsTest() throws IOException {
    // Arrange - the rollback delete failing too (e.g. a lost DB connection) must never leave the
    // job stuck in RUNNING; a stray orphaned row is far less harmful than a client polling
    // forever for a status update that never comes.
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp");
    Files.writeString(file, HEADER + "\n1\tApple\tAcme\t50\n");
    doThrow(new IllegalStateException("boom")).when(foodRepository).saveAll(anyList());
    doThrow(new InvalidDataAccessResourceUsageException("db connection lost"))
        .when(foodRepository).deleteByImportJobId(any());
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), Files.size(file));

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    assertThat(status.getFinishedAt()).isNotNull();
  }

  @Test
  public void importsFromAPlainFileAndDeletesItAfterwardsTest() throws IOException {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp");
    String content = HEADER + "\n1\tApple\tAcme\t50\n";
    Files.writeString(file, content);
    long fileSize = Files.size(file);
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), fileSize);

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getBytesRead()).isEqualTo(fileSize);
    assertThat(Files.exists(file)).isFalse();
  }

  @Test
  public void marksTheJobAsFailedWhenTheFileCannotBeReadTest() {
    // Arrange
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path missingFile = Path.of(System.getProperty("java.io.tmpdir"), "does-not-exist-" + UUID
        .randomUUID() + ".csv");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

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
    long fileSize = Files.size(file);
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), fileSize);

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getBytesRead()).isEqualTo(fileSize);
  }

  @Test
  public void importsARowWithAnEmbeddedQuoteCharacterInsteadOfCrashingTheParserTest()
      throws IOException {
    // Arrange - reproduces the real-world Lexer crash: a raw, unescaped TSV export can contain a
    // literal `"` (e.g. an inch mark) in free text. importFromFile must parse it as plain text
    // instead of the parser aborting entirely, since the whole job would otherwise be unable to
    // get past that single row.
    FoodCsvImportUsecase usecase =
        new FoodCsvImportUsecase(foodRepository, jobRegistry, dispatcher, 10);
    Path file = Files.createTempFile("csv-import-test-", ".tmp");
    Files.writeString(file, HEADER + "\n1\t12\" Pizza Margherita\tAcme\t250\n");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), Files.size(file));

    // Act
    usecase.importFromFile(file, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getErrorCount()).isEqualTo(0);
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
    FoodImportJobStatus status = new FoodImportJobStatus(jobId, 0);
    when(jobRegistry.createJob(anyLong())).thenReturn(status);

    // Act
    ImportJobStatusDto result = usecase.startImport(file);

    // Assert
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getState()).isEqualTo("RUNNING");
    verify(dispatcher).dispatch(eq(usecase), any(Path.class), eq(status));
  }

  private List<CSVRecord> parseRows(String... rows) throws IOException {
    return parseRowsWithHeader(HEADER, rows);
  }

  private List<CSVRecord> parseRowsWithHeader(String header, String... rows) throws IOException {
    StringBuilder csv = new StringBuilder(header);
    for (String row : rows) {
      csv.append('\n').append(row);
    }
    try (CSVParser parser = CSVFormat.DEFAULT
        .withDelimiter('\t')
        .withQuote(null)
        .withFirstRecordAsHeader()
        .withIgnoreHeaderCase()
        .withTrim()
        .parse(new StringReader(csv.toString()))) {
      return parser.getRecords();
    }
  }
}
