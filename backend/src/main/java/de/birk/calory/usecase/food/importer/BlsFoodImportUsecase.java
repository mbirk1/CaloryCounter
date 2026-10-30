package de.birk.calory.usecase.food.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Imports food items from a Bundeslebensmittelschlüssel (BLS) 4.0 {@code .xlsx} export.
 *
 * <p>The uploaded file is first copied to a temporary file on disk, for the same reason as
 * {@link FoodCsvImportUsecase}: the actual import happens asynchronously, after the HTTP request
 * (and the {@code MultipartFile}'s backing stream) has already completed.
 *
 * <p>Unlike the OpenFoodFacts CSV export, BLS is small enough (a few thousand rows) that reading
 * the whole workbook into memory up front is unproblematic - no streaming parser is needed here.
 * Job-tracking, batch-persistence and rollback are format-agnostic and shared with
 * {@link FoodCsvImportUsecase} via {@link FoodImportPersister} and {@link ImportJobRegistry};
 * only the Apache POI-based file reading and {@link BlsFoodRowMapper}-based row mapping here are
 * specific to this import path.
 *
 * @author Marius Birk
 */
@Component
public class BlsFoodImportUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(BlsFoodImportUsecase.class);

  private static final int DEFAULT_BATCH_SIZE = 1000;
  private static final int HEADER_ROW_COUNT = 1;

  private final ImportJobRegistry jobRegistry;
  private final FoodImportDispatcher dispatcher;
  private final FoodImportPersister persister;
  private final BlsFoodRowMapper rowMapper;
  private final ImportJobStatusDtoConverter statusDtoConverter;
  private final int batchSize;

  /**
   * Basic Constructor takes all collaborators needed to run a BLS import.
   *
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param persister persists imported batches and rolls back a failed job
   */
  @Autowired
  public BlsFoodImportUsecase(
      ImportJobRegistry jobRegistry,
      FoodImportDispatcher dispatcher,
      FoodImportPersister persister) {
    this(jobRegistry, dispatcher, persister, DEFAULT_BATCH_SIZE);
  }

  /**
   * Package-private constructor that allows tests to use a small batch size, so batch-splitting
   * behavior can be verified without needing thousands of rows.
   *
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param persister persists imported batches and rolls back a failed job
   * @param batchSize the number of rows collected before each batch is persisted
   */
  BlsFoodImportUsecase(
      ImportJobRegistry jobRegistry,
      FoodImportDispatcher dispatcher,
      FoodImportPersister persister,
      int batchSize) {
    this(jobRegistry, dispatcher, persister, new BlsFoodRowMapper(), batchSize);
  }

  /**
   * Package-private constructor that additionally allows tests to inject a (possibly mocked)
   * row mapper, so row-level mapping errors can be simulated without a malformed real workbook.
   *
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param persister persists imported batches and rolls back a failed job
   * @param rowMapper maps spreadsheet rows to persistable food items
   * @param batchSize the number of rows collected before each batch is persisted
   */
  BlsFoodImportUsecase(
      ImportJobRegistry jobRegistry,
      FoodImportDispatcher dispatcher,
      FoodImportPersister persister,
      BlsFoodRowMapper rowMapper,
      int batchSize) {
    this.jobRegistry = jobRegistry;
    this.dispatcher = dispatcher;
    this.persister = persister;
    this.rowMapper = rowMapper;
    this.statusDtoConverter = new ImportJobStatusDtoConverter();
    this.batchSize = batchSize;
  }

  /**
   * Stages the uploaded file and starts an asynchronous import, immediately returning a job
   * status that can be polled for progress.
   *
   * @param file the uploaded BLS {@code .xlsx} file
   * @return the freshly created, still running job status
   * @throws IOException if the uploaded file cannot be staged to a temporary file
   */
  public ImportJobStatusDto startImport(MultipartFile file) throws IOException {
    Path tempFile = Files.createTempFile("food-import-bls-", ".xlsx");
    file.transferTo(tempFile);

    FoodImportJobStatus status = this.jobRegistry.createJob(Files.size(tempFile));
    LOG.info(
        "BLS-Import-Job {} gestartet: Datei '{}', {} Bytes",
        status.getJobId(), file.getOriginalFilename(), status.getTotalBytes());
    this.dispatcher.dispatch(() -> importFromFile(tempFile, status));
    return this.statusDtoConverter.toDto(status);
  }

  /**
   * Performs the actual import from a staged file. Package-private: only called by
   * {@link FoodImportDispatcher} from its {@code @Async} method, or directly from tests.
   *
   * @param filePath the staged, temporary copy of the uploaded file
   * @param status the job status to update while importing
   */
  void importFromFile(Path filePath, FoodImportJobStatus status) {
    try (InputStream rawStream = Files.newInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(rawStream)) {
      // The whole file is already fully read into memory by the XSSFWorkbook constructor above -
      // unlike the streaming CSV import, there is no meaningful partial-progress point to report
      // bytesRead at, so it jumps straight to 100% once the workbook has been parsed.
      status.incrementBytesRead(Files.size(filePath));
      processRows(workbook.getSheetAt(0), status);
      status.complete();
      LOG.info(
          "BLS-Import-Job {} abgeschlossen: {} Zeilen verarbeitet, {} importiert, "
              + "{} uebersprungen (Duplikate), {} Fehler",
          status.getJobId(), status.getProcessedRows(), status.getImportedCount(),
          status.getSkippedCount(), status.getErrorCount());
    } catch (IOException e) {
      LOG.error("BLS-Import-Job {} beim Lesen der Datei fehlgeschlagen", status.getJobId(), e);
      this.persister.rollbackAndFail(status);
    } catch (RuntimeException e) {
      LOG.error("BLS-Import-Job {} unerwartet fehlgeschlagen", status.getJobId(), e);
      this.persister.rollbackAndFail(status);
    } finally {
      deleteQuietly(filePath);
    }
  }

  /**
   * Iterates the sheet's rows (skipping the header), mapping and batch-persisting them as it
   * goes. Package visible so it can be unit tested directly against an in-memory workbook,
   * without needing to stage a temporary file first.
   *
   * @param sheet the BLS sheet, header row included
   * @param status the job status to update while importing
   */
  void processRows(Sheet sheet, FoodImportJobStatus status) {
    List<FoodPersistence> batch = new ArrayList<>(this.batchSize);
    int rowIndex = 0;
    for (Row row : sheet) {
      if (rowIndex < HEADER_ROW_COUNT) {
        rowIndex++;
        continue;
      }
      rowIndex++;
      status.incrementProcessed(1);
      mapRow(row, status).ifPresent(batch::add);
      if (batch.size() >= this.batchSize) {
        this.persister.persistBatch(batch, status);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      this.persister.persistBatch(batch, status);
    }
  }

  private Optional<FoodPersistence> mapRow(Row row, FoodImportJobStatus status) {
    try {
      Optional<FoodPersistence> mapped = this.rowMapper.map(row);
      if (mapped.isEmpty()) {
        status.incrementSkipped(1);
      }
      mapped.ifPresent(f -> f.setImportJobId(status.getJobId()));
      return mapped;
    } catch (RuntimeException e) {
      status.incrementError(1);
      return Optional.empty();
    }
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      // best effort cleanup, a leftover temp file is not worth failing the job for
    }
  }
}
