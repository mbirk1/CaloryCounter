package de.birk.calory.usecase.food.duplicate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import de.birk.calory.domain.food.FoodSource;

/**
 * In-memory registry of duplicate-analysis job statuses, keyed by job id - the FR-3 analogue of
 * {@code ImportJobRegistry}. Holding this in memory is sufficient for a single-instance
 * deployment, same reasoning as the import job registry.
 *
 * @author Marius Birk
 */
@Component
public class DuplicateAnalysisJobRegistry {

  private final Map<UUID, DuplicateAnalysisJobStatus> jobs = new ConcurrentHashMap<>();

  /**
   * Creates and registers a new job, in {@code RUNNING} state.
   *
   * @param source the food source being analyzed
   * @return the newly created job status
   */
  public DuplicateAnalysisJobStatus createJob(FoodSource source) {
    DuplicateAnalysisJobStatus status = new DuplicateAnalysisJobStatus(UUID.randomUUID(), source);
    this.jobs.put(status.getJobId(), status);
    return status;
  }

  /**
   * Looks up a job by its identifier.
   *
   * @param jobId the job identifier
   * @return the job status, if a job with this id was ever created
   */
  public Optional<DuplicateAnalysisJobStatus> findJob(UUID jobId) {
    return Optional.ofNullable(this.jobs.get(jobId));
  }
}
