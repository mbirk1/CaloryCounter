package de.birk.calory.usecase.food.importer;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;
import de.birk.calory.adapter.secondary.FoodRepository;
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
 * @author Marius Birk
 */
@Component
public class FoodCsvImportUsecase {

  private static final int DEFAULT_BATCH_SIZE = 1000;
  private static final int GZIP_BYTE_1 = 0x1f;
  private static final int GZIP_BYTE_2 = 0x8b;

  private final FoodRepository foodRepository;
  private final ImportJobRegistry jobRegistry;
  private final FoodCsvImportDispatcher dispatcher;
  private final FoodCsvRowMapper rowMapper;
  private final ImportJobStatusDtoConverter statusDtoConverter;
  private final int batchSize;

  /**
   * Basic Constructor takes all collaborators needed to run a CSV import.
   *
   * @param foodRepository is used to persist the imported food items
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   */
  @Autowired
  public FoodCsvImportUsecase(
      FoodRepository foodRepository,
      ImportJobRegistry jobRegistry,
      FoodCsvImportDispatcher dispatcher) {
    this(foodRepository, jobRegistry, dispatcher, DEFAULT_BATCH_SIZE);
  }

  /**
   * Package-private constructor that allows tests to use a small batch size, so batch-splitting
   * behavior can be verified without needing thousands of rows.
   *
   * @param foodRepository is used to persist the imported food items
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param batchSize the number of rows collected before each {@code saveAll()} call
   */
  FoodCsvImportUsecase(
      FoodRepository foodRepository,
      ImportJobRegistry jobRegistry,
      FoodCsvImportDispatcher dispatcher,
      int batchSize) {
    this(foodRepository, jobRegistry, dispatcher, new FoodCsvRowMapper(), batchSize);
  }

  /**
   * Package-private constructor that additionally allows tests to inject a (possibly mocked)
   * row mapper, so row-level mapping errors can be simulated without a malformed real CSV row.
   *
   * @param foodRepository is used to persist the imported food items
   * @param jobRegistry tracks the progress of running and finished import jobs
   * @param dispatcher triggers the actual import asynchronously
   * @param rowMapper maps CSV rows to persistable food items
   * @param batchSize the number of rows collected before each {@code saveAll()} call
   */
  FoodCsvImportUsecase(
      FoodRepository foodRepository,
      ImportJobRegistry jobRegistry,
      FoodCsvImportDispatcher dispatcher,
      FoodCsvRowMapper rowMapper,
      int batchSize) {
    this.foodRepository = foodRepository;
    this.jobRegistry = jobRegistry;
    this.dispatcher = dispatcher;
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

    FoodImportJobStatus status = this.jobRegistry.createJob();
    this.dispatcher.dispatch(this, tempFile, status);
    return this.statusDtoConverter.toDto(status);
  }

  /**
   * Performs the actual import from a staged file. Package-private: only called by
   * {@link FoodCsvImportDispatcher} from its {@code @Async} method, or directly from tests.
   *
   * @param filePath the staged, temporary copy of the uploaded file
   * @param status the job status to update while importing
   */
  void importFromFile(Path filePath, FoodImportJobStatus status) {
    try (InputStream rawStream = Files.newInputStream(filePath);
        InputStream decompressed = decompressIfNeeded(rawStream);
        Reader reader = new InputStreamReader(decompressed, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT
            .withDelimiter('\t')
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
            .parse(reader)) {
      processRecords(parser, status);
      status.complete();
    } catch (IOException e) {
      status.fail();
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
        persistBatch(batch, status);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      persistBatch(batch, status);
    }
  }

  private Optional<FoodPersistence> mapRow(CSVRecord record, FoodImportJobStatus status) {
    try {
      Optional<FoodPersistence> mapped = this.rowMapper.map(record);
      if (mapped.isEmpty()) {
        status.incrementSkipped(1);
      }
      return mapped;
    } catch (RuntimeException e) {
      status.incrementError(1);
      return Optional.empty();
    }
  }

  /**
   * Persists a batch in a single call, relying on Spring Data's default per-repository-method
   * transaction boundary - each {@code saveAll()} call therefore already runs in its own short
   * transaction without needing an explicit {@code @Transactional} here, which would be silently
   * ignored anyway since this method is called via {@code this} from within the same bean.
   *
   * <p>Re-import safety: rather than pre-checking every row against the unique constraint on
   * {@code external_id} (far too slow at this volume), batches are inserted optimistically. If a
   * batch fails because the file (or part of it) was already imported before, it is retried
   * record by record so only the genuine duplicates are skipped.
   *
   * @param batch the batch to persist
   * @param status the job status to update
   */
  void persistBatch(List<FoodPersistence> batch, FoodImportJobStatus status) {
    try {
      this.foodRepository.saveAll(batch);
      status.incrementImported(batch.size());
    } catch (DataIntegrityViolationException e) {
      persistOneByOne(batch, status);
    }
  }

  private void persistOneByOne(List<FoodPersistence> batch, FoodImportJobStatus status) {
    for (FoodPersistence food : batch) {
      try {
        this.foodRepository.save(food);
        status.incrementImported(1);
      } catch (DataIntegrityViolationException e) {
        status.incrementSkipped(1);
      }
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
}
