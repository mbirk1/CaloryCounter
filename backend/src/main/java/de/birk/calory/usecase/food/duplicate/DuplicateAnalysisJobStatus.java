package de.birk.calory.usecase.food.duplicate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import de.birk.calory.domain.food.FoodSource;

/**
 * Mutable, thread-safe progress tracker for a single duplicate-detection analysis job. A single
 * background thread performs the analysis and updates this status, while the job status endpoint
 * reads it concurrently from request-handling threads - every mutable field is therefore either
 * atomic or volatile instead of relying on external synchronization. Structurally mirrors
 * {@code FoodImportJobStatus}, but without the byte-progress fields a file-based import tracks -
 * an analysis run has no file to read progress through.
 *
 * @author Marius Birk
 */
public class DuplicateAnalysisJobStatus {

  private final UUID jobId;
  private final FoodSource source;
  private final Instant startedAt;
  private final AtomicLong clustersFound = new AtomicLong();
  private volatile DuplicateAnalysisJobState state = DuplicateAnalysisJobState.RUNNING;
  private volatile Instant finishedAt;
  private volatile String errorMessage;

  /**
   * Creates a new, running job status.
   *
   * @param jobId the job identifier
   * @param source the food source being analyzed
   */
  public DuplicateAnalysisJobStatus(UUID jobId, FoodSource source) {
    this.jobId = jobId;
    this.source = source;
    this.startedAt = Instant.now();
  }

  public UUID getJobId() {
    return jobId;
  }

  public FoodSource getSource() {
    return source;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public DuplicateAnalysisJobState getState() {
    return state;
  }

  public long getClustersFound() {
    return clustersFound.get();
  }

  public void setClustersFound(long count) {
    this.clustersFound.set(count);
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void complete() {
    this.state = DuplicateAnalysisJobState.COMPLETED;
    this.finishedAt = Instant.now();
  }

  /**
   * Marks the job as failed.
   *
   * @param errorMessage a description of what went wrong
   */
  public void fail(String errorMessage) {
    this.state = DuplicateAnalysisJobState.FAILED;
    this.errorMessage = errorMessage;
    this.finishedAt = Instant.now();
  }
}
