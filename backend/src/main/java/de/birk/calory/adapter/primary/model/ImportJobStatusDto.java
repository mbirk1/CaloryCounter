package de.birk.calory.adapter.primary.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The DTO that is returned to clients for the status of a CSV import job.
 *
 * @author Marius Birk
 */
public class ImportJobStatusDto {

  private UUID jobId;
  private String state;
  private long processedRows;
  private long importedCount;
  private long skippedCount;
  private long errorCount;
  private Instant startedAt;
  private Instant finishedAt;

  public ImportJobStatusDto() {
  }

  /**
   * Constructor that takes all properties needed to describe the progress of an import job.
   *
   * @param jobId the job identifier
   * @param state the name of the {@code ImportJobState} enum constant
   * @param processedRows the number of CSV rows read so far
   * @param importedCount the number of rows successfully imported
   * @param skippedCount the number of rows skipped due to the quality filter or a duplicate
   * @param errorCount the number of rows that could not be processed at all
   * @param startedAt the point in time the job started
   * @param finishedAt the point in time the job finished, or null while still running
   */
  public ImportJobStatusDto(
      UUID jobId,
      String state,
      long processedRows,
      long importedCount,
      long skippedCount,
      long errorCount,
      Instant startedAt,
      Instant finishedAt) {
    this.jobId = jobId;
    this.state = state;
    this.processedRows = processedRows;
    this.importedCount = importedCount;
    this.skippedCount = skippedCount;
    this.errorCount = errorCount;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
  }

  public UUID getJobId() {
    return jobId;
  }

  public String getState() {
    return state;
  }

  public long getProcessedRows() {
    return processedRows;
  }

  public long getImportedCount() {
    return importedCount;
  }

  public long getSkippedCount() {
    return skippedCount;
  }

  public long getErrorCount() {
    return errorCount;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }
}
