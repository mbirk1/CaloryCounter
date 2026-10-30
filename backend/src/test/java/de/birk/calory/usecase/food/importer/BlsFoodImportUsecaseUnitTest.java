package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.secondary.FoodRepository;

@ExtendWith(MockitoExtension.class)
public class BlsFoodImportUsecaseUnitTest {

  private static final int COL_NAME = 1;
  private static final int COL_CALORY = 6;

  @Mock
  private FoodRepository foodRepository;

  @Mock
  private ImportJobRegistry jobRegistry;

  @Mock
  private FoodImportDispatcher dispatcher;

  @Mock
  private BlsFoodRowMapper rowMapper;

  // A real (not mocked) persister wrapping the mocked repository, same reasoning as
  // FoodCsvImportUsecaseUnitTest: persistBatch/rollbackAndFail are already covered by
  // FoodImportPersisterUnitTest, so reusing the real implementation lets assertions here target
  // foodRepository directly.
  private FoodImportPersister persister;

  @BeforeEach
  public void setUp() {
    this.persister = new FoodImportPersister(this.foodRepository);
  }

  @Test
  public void splitsRowsIntoBatchesOfTheConfiguredSizeTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase = new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 2);
    Sheet sheet = sheetWithRows(
        "C100001", "Apple", 50,
        "C100002", "Banana", 90,
        "C100003", "Cherry", 60,
        "C100004", "Date", 280,
        "C100005", "Elderberry", 70
    );
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRows(sheet, status);

    // Assert - 5 rows at batch size 2 means 3 saveAll calls (2, 2, 1)
    verify(foodRepository, times(3)).saveAll(anyList());
    assertThat(status.getProcessedRows()).isEqualTo(5);
    assertThat(status.getImportedCount()).isEqualTo(5);
    assertThat(status.getSkippedCount()).isEqualTo(0);
  }

  @Test
  public void skipsTheHeaderRowTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Sheet sheet = sheetWithRows("C100001", "Apple", 50);
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRows(sheet, status);

    // Assert - only the single data row is processed, not the header
    assertThat(status.getProcessedRows()).isEqualTo(1);
  }

  @Test
  public void countsSkippedRowsThatFailTheQualityFilterTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Sheet sheet = sheetWithRows(
        "C100001", "Apple", 50,
        "C100002", "", 90
    );
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRows(sheet, status);

    // Assert
    assertThat(status.getProcessedRows()).isEqualTo(2);
    assertThat(status.getImportedCount()).isEqualTo(1);
    assertThat(status.getSkippedCount()).isEqualTo(1);
  }

  @Test
  public void countsRowsThatThrowDuringMappingAsErrorsTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, rowMapper, 10);
    Sheet sheet = sheetWithRows("C100001", "Apple", 50);
    when(rowMapper.map(any())).thenThrow(new IllegalStateException("boom"));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRows(sheet, status);

    // Assert
    assertThat(status.getProcessedRows()).isEqualTo(1);
    assertThat(status.getErrorCount()).isEqualTo(1);
    assertThat(status.getImportedCount()).isEqualTo(0);
  }

  @Test
  public void rollsBackAndFailsTheJobWhenAnUnexpectedRuntimeExceptionEscapesProcessingTest()
      throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Path file = workbookFile("C100001", "Apple", 50);
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
  public void marksTheJobAsFailedWhenTheFileCannotBeReadTest() {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Path missingFile = Path.of(
        System.getProperty("java.io.tmpdir"), "does-not-exist-" + UUID.randomUUID() + ".xlsx");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.importFromFile(missingFile, status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    assertThat(status.getFinishedAt()).isNotNull();
  }

  @Test
  public void importsFromAPlainWorkbookFileAndDeletesItAfterwardsTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Path file = workbookFile("C100001", "Apple", 50);
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
  public void startImportStagesFileCreatesJobAndDispatchesTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    byte[] content = workbookBytes("C100001", "Apple", 50);
    MultipartFile file = new MockMultipartFile(
        "file", "bls.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    UUID jobId = UUID.randomUUID();
    FoodImportJobStatus status = new FoodImportJobStatus(jobId, 0);
    when(jobRegistry.createJob(anyLong())).thenReturn(status);

    // Act
    ImportJobStatusDto result = usecase.startImport(file);

    // Assert
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getState()).isEqualTo("RUNNING");
    verify(dispatcher).dispatch(any(Runnable.class));
  }

  @Test
  public void fallsBackToRecordByRecordForADataAccessExceptionTest() throws IOException {
    // Arrange
    BlsFoodImportUsecase usecase =
        new BlsFoodImportUsecase(jobRegistry, dispatcher, persister, 10);
    Sheet sheet = sheetWithRows("C100001", "Apple", 50);
    doThrow(new InvalidDataAccessResourceUsageException("numeric field overflow"))
        .when(foodRepository).saveAll(anyList());
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    usecase.processRows(sheet, status);

    // Assert
    verify(foodRepository).saveAll(anyList());
    verify(foodRepository).save(any());
    assertThat(status.getImportedCount()).isEqualTo(1);
  }

  /**
   * Builds an in-memory sheet with a header row followed by one data row per given
   * (blsCode, name, calory) triple - only the columns {@link BlsFoodRowMapper} actually reads
   * are populated, the same fixture shape used throughout {@link BlsFoodRowMapperUnitTest}.
   */
  private Sheet sheetWithRows(Object... triples) {
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet();
    sheet.createRow(0);
    int rowIndex = 1;
    for (int i = 0; i < triples.length; i += 3) {
      Row row = sheet.createRow(rowIndex++);
      row.createCell(0).setCellValue((String) triples[i]);
      row.createCell(COL_NAME).setCellValue((String) triples[i + 1]);
      row.createCell(COL_CALORY).setCellValue(((Number) triples[i + 2]).doubleValue());
    }
    return sheet;
  }

  private byte[] workbookBytes(Object... triples) throws IOException {
    Sheet sheet = sheetWithRows(triples);
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      sheet.getWorkbook().write(out);
      return out.toByteArray();
    } finally {
      sheet.getWorkbook().close();
    }
  }

  private Path workbookFile(Object... triples) throws IOException {
    Path file = Files.createTempFile("bls-import-test-", ".xlsx");
    Files.write(file, workbookBytes(triples));
    return file;
  }
}
