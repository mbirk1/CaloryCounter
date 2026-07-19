package de.birk.calory.usecase.food.importer;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mutable, thread-safe progress tracker for a single CSV import job. A single background thread
 * performs the import and updates the counters, while the job status endpoint reads them
 * concurrently from request-handling threads - every mutable field is therefore either atomic
 * or volatile instead of relying on external synchronization.
 *
 * @author Marius Birk
 */
public class FoodImportJobStatus {

  private final UUID jobId;
  private final Instant startedAt;
  private final AtomicLong processedRows = new AtomicLong();
  private final AtomicLong importedCount = new AtomicLong();
  private final AtomicLong skippedCount = new AtomicLong();
  private final AtomicLong errorCount = new AtomicLong();
  private volatile ImportJobState state = ImportJobState.RUNNING;
  private volatile Instant finishedAt;

  public FoodImportJobStatus(UUID jobId) {
    this.jobId = jobId;
    this.startedAt = Instant.now();
  }

  public UUID getJobId() {
    return jobId;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public ImportJobState getState() {
    return state;
  }

  public long getProcessedRows() {
    return processedRows.get();
  }

  public long getImportedCount() {
    return importedCount.get();
  }

  public long getSkippedCount() {
    return skippedCount.get();
  }

  public long getErrorCount() {
    return errorCount.get();
  }

  public void incrementProcessed(long delta) {
    processedRows.addAndGet(delta);
  }

  public void incrementImported(long delta) {
    importedCount.addAndGet(delta);
  }

  public void incrementSkipped(long delta) {
    skippedCount.addAndGet(delta);
  }

  public void incrementError(long delta) {
    errorCount.addAndGet(delta);
  }

  public void complete() {
    this.state = ImportJobState.COMPLETED;
    this.finishedAt = Instant.now();
  }

  public void fail() {
    this.state = ImportJobState.FAILED;
    this.finishedAt = Instant.now();
  }
}
