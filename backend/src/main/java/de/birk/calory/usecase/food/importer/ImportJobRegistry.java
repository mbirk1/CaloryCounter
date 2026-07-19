package de.birk.calory.usecase.food.importer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * In-memory registry of CSV import job statuses, keyed by job id. Holding this in memory is
 * sufficient for a single-instance deployment - persisting job history across restarts is a
 * possible future extension, not part of this ticket.
 *
 * @author Marius Birk
 */
@Component
public class ImportJobRegistry {

  private final Map<UUID, FoodImportJobStatus> jobs = new ConcurrentHashMap<>();

  /**
   * Creates and registers a new job, in {@code RUNNING} state.
   *
   * @return the newly created job status
   */
  public FoodImportJobStatus createJob() {
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());
    this.jobs.put(status.getJobId(), status);
    return status;
  }

  /**
   * Looks up a job by its identifier.
   *
   * @param jobId the job identifier
   * @return the job status, if a job with this id was ever created
   */
  public Optional<FoodImportJobStatus> findJob(UUID jobId) {
    return Optional.ofNullable(this.jobs.get(jobId));
  }
}
