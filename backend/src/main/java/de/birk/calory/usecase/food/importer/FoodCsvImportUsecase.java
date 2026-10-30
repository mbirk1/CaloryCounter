package de.birk.calory.usecase.food.importer;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Streams a (optionally gzip-compressed) OpenFoodFacts CSV export and imports every row that
 * satisfies {@link FoodCsvRowMapper}'s quality filter in batches, without ever loading the full
 * file into memory.
 *
 * <p>The uploaded file is first copied to a temporary file on disk: the {@code InputStream}
 * backing an incoming {@code MultipartFile} is only guaranteed to be valid for the duration of
 * the HTTP request, but the actual import happens asynchronously, long after the request has
 * completed and Spring/Tomcat have cleaned up the multipart request state.
 *
 * <p>Job-tracking, batch-persistence and rollback are format-agnostic and live in
 * {@link FoodImportPersister} and {@link ImportJobRegistry}, shared with {@code
 * BlsFoodImportUsecase} - only the file parsing and row mapping here (Commons CSV +
 * {@link FoodCsvRowMapper}) are specific to this import path.
 *
 * @author Marius Birk
 */
@Component
public class FoodCsvImportUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(FoodCsvImportUsecase.class);

  private static final int DEFAULT_BATCH_SIZE = 1000;
  private static final int GZIP_BYTE_1 = 0x1f;
  private static final int GZIP_BYTE_2 = 0x8b;

  private final ImportJobRegistry jobRegistry;
  private final FoodImportDispatcher dispatcher;
  private final FoodImportPersister persister;
  private final FoodCsvRowMapper rowMapper;
  private final ImportJobStatusDtoConverter statusDtoConverter;
  private final int batchSize;

  /**
   * Basic Constructor takes all collaborators needed to run a CSV import.
   *
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param persister persists imported batches and rolls back a failed job
   */
  @Autowired
  public FoodCsvImportUsecase(
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
  FoodCsvImportUsecase(
      ImportJobRegistry jobRegistry,
      FoodImportDispatcher dispatcher,
      FoodImportPersister persister,
      int batchSize) {
    this(jobRegistry, dispatcher, persister, new FoodCsvRowMapper(), batchSize);
  }

  /**
   * Package-private constructor that additionally allows tests to inject a (possibly mocked)
   * row mapper, so row-level mapping errors can be simulated without a malformed real CSV row.
   *
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param persister persists imported batches and rolls back a failed job
   * @param rowMapper maps CSV rows to persistable food items
   * @param batchSize the number of rows collected before each batch is persisted
   */
  FoodCsvImportUsecase(
      ImportJobRegistry jobRegistry,
      FoodImportDispatcher dispatcher,
      FoodImportPersister persister,
      FoodCsvRowMapper rowMapper,
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
   * @param file the uploaded CSV (or gzip-compressed CSV) file
   * @return the freshly created, still running job status
   * @throws IOException if the uploaded file cannot be staged to a temporary file
   */
  public ImportJobStatusDto startImport(MultipartFile file) throws IOException {
    Path tempFile = Files.createTempFile("food-import-", ".tmp");
    file.transferTo(tempFile);

    FoodImportJobStatus status = this.jobRegistry.createJob(Files.size(tempFile));
    LOG.info(
        "CSV-Import-Job {} gestartet: Datei '{}', {} Bytes",
        status.getJobId(), file.getOriginalFilename(), status.getTotalBytes());
    this.dispatcher.dispatch(() -> importFromFile(tempFile, status));
    return this.statusDtoConverter.toDto(status);
  }

  /**
   * Performs the actual import from a staged file. Package-private: only called by
   * {@link FoodImportDispatcher} from its {@code @Async} method, or directly from tests.
   *
   * <p>A distinction is deliberately made between a single bad row - skipped and counted, the
   * job carries on and still ends up {@code COMPLETED} - and a job-level failure (the file can't
   * be read, or an unexpected error escapes row/batch processing entirely). The latter aborts
   * the whole job as {@code FAILED} and rolls back everything this job has already imported, so
   * a half-finished import never lingers in the database as if it had succeeded.
   *
   * @param filePath the staged, temporary copy of the uploaded file
   * @param status the job status to update while importing
   */
  void importFromFile(Path filePath, FoodImportJobStatus status) {
    try (InputStream rawStream = Files.newInputStream(filePath);
        InputStream countingStream = new CountingInputStream(rawStream, status);
        InputStream decompressed = decompressIfNeeded(countingStream);
        Reader reader = new InputStreamReader(decompressed, StandardCharsets.UTF_8);
        // The OpenFoodFacts export is a raw TSV file without CSV-style quote-escaping: a `"`
        // appearing in free text (e.g. an inch mark in "12" Pizza", a stray quotation mark) is
        // just a literal character, not a field encapsulation marker. Leaving RFC4180 quote
        // handling on (CSVFormat.DEFAULT's default) makes the Lexer misinterpret such a `"` as
        // starting/ending an encapsulated field; the next character that doesn't fit that
        // interpretation then aborts the whole parse with an unrecoverable Lexer exception -
        // this can't be caught and skipped per-row, since it happens while tokenizing, before a
        // CSVRecord even exists to skip. Disabling quoting entirely avoids that failure mode. The
        // trade-off: a field with a genuine embedded line break (rare in an exported TSV, where
        // that's typically stripped already) would now end its row early instead of being kept
        // together - a few extra skipped/malformed rows, not a job-ending crash.
        CSVParser parser = CSVFormat.DEFAULT
            .withDelimiter('\t')
            .withQuote(null)
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
            .parse(reader)) {
      processRecords(parser, status);
      status.complete();
      LOG.info(
          "CSV-Import-Job {} abgeschlossen: {} Zeilen verarbeitet, {} importiert, "
              + "{} uebersprungen (Duplikate), {} Fehler",
          status.getJobId(), status.getProcessedRows(), status.getImportedCount(),
          status.getSkippedCount(), status.getErrorCount());
    } catch (IOException e) {
      LOG.error("CSV-Import-Job {} beim Lesen der Datei fehlgeschlagen", status.getJobId(), e);
      this.persister.rollbackAndFail(status);
    } catch (RuntimeException e) {
      LOG.error("CSV-Import-Job {} unerwartet fehlgeschlagen", status.getJobId(), e);
      this.persister.rollbackAndFail(status);
    } finally {
      deleteQuietly(filePath);
    }
  }

  /**
   * Iterates the parsed CSV records, mapping and batch-persisting them as it goes. Package
   * visible so it can be unit tested directly with a small in-memory CSV, without needing to
   * stage a temporary file first.
   *
   * @param records the parsed CSV records, read lazily
   * @param status the job status to update while importing
   */
  void processRecords(Iterable<CSVRecord> records, FoodImportJobStatus status) {
    List<FoodPersistence> batch = new ArrayList<>(this.batchSize);
    for (CSVRecord record : records) {
      status.incrementProcessed(1);
      mapRow(record, status).ifPresent(batch::add);
      if (batch.size() >= this.batchSize) {
        this.persister.persistBatch(batch, status);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      this.persister.persistBatch(batch, status);
    }
  }

  private Optional<FoodPersistence> mapRow(CSVRecord record, FoodImportJobStatus status) {
    try {
      Optional<FoodPersistence> mapped = this.rowMapper.map(record);
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

  private InputStream decompressIfNeeded(InputStream inputStream) throws IOException {
    PushbackInputStream pushbackStream = new PushbackInputStream(inputStream, 2);
    byte[] signature = new byte[2];
    int bytesRead = pushbackStream.read(signature);
    if (bytesRead > 0) {
      pushbackStream.unread(signature, 0, bytesRead);
    }
    boolean isGzip = bytesRead == 2
        && (signature[0] & 0xFF) == GZIP_BYTE_1
        && (signature[1] & 0xFF) == GZIP_BYTE_2;
    return isGzip ? new GZIPInputStream(pushbackStream) : pushbackStream;
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      // best effort cleanup, a leftover temp file is not worth failing the job for
    }
  }

  /**
   * Tracks bytes read from the staged upload as {@link FoodImportJobStatus#getBytesRead()}, so
   * clients can compute a processing-progress percentage against
   * {@link FoodImportJobStatus#getTotalBytes()}. Wraps the raw file stream (before gzip
   * decompression, if any), so the percentage reflects progress through the actual uploaded
   * file regardless of compression.
   */
  private static final class CountingInputStream extends FilterInputStream {

    private final FoodImportJobStatus status;

    private CountingInputStream(InputStream in, FoodImportJobStatus status) {
      super(in);
      this.status = status;
    }

    @Override
    public int read() throws IOException {
      int singleByte = super.read();
      if (singleByte != -1) {
        this.status.incrementBytesRead(1);
      }
      return singleByte;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
      int bytesRead = super.read(buffer, offset, length);
      if (bytesRead > 0) {
        this.status.incrementBytesRead(bytesRead);
      }
      return bytesRead;
    }
  }
}
