package de.birk.calory.adapter.primary.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The DTO that is returned to clients for the status of a duplicate-detection analysis job.
 *
 * @author Marius Birk
 */
public class DuplicateAnalysisJobStatusDto {

  private UUID jobId;
  private String source;
  private String state;
  private long clustersFound;
  private String errorMessage;
  private Instant startedAt;
  private Instant finishedAt;

  public DuplicateAnalysisJobStatusDto() {
  }

  /**
   * Constructor that takes all properties needed to describe the progress of an analysis job.
   *
   * @param jobId the job identifier
   * @param source the name of the {@code FoodSource} enum constant being analyzed
   * @param state the name of the {@code DuplicateAnalysisJobState} enum constant
   * @param clustersFound the number of clusters proposed so far
   * @param errorMessage a description of what went wrong, if the job failed
   * @param startedAt the point in time the job started
   * @param finishedAt the point in time the job finished, or null while still running
   */
  public DuplicateAnalysisJobStatusDto(
      UUID jobId,
      String source,
      String state,
      long clustersFound,
      String errorMessage,
      Instant startedAt,
      Instant finishedAt) {
    this.jobId = jobId;
    this.source = source;
    this.state = state;
    this.clustersFound = clustersFound;
    this.errorMessage = errorMessage;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
  }

  public UUID getJobId() {
    return jobId;
  }

  public String getSource() {
    return source;
  }

  public String getState() {
    return state;
  }

  public long getClustersFound() {
    return clustersFound;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }
}
