package de.birk.calory.usecase.food.duplicate;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;

/**
 * Usecase to find the status of a duplicate-detection analysis job.
 *
 * @author Marius Birk
 */
@Service
public class FindDuplicateAnalysisJobStatusUsecase {

  private final DuplicateAnalysisJobRegistry jobRegistry;
  private final DuplicateAnalysisJobStatusDtoConverter statusDtoConverter;

  public FindDuplicateAnalysisJobStatusUsecase(DuplicateAnalysisJobRegistry jobRegistry) {
    this.jobRegistry = jobRegistry;
    this.statusDtoConverter = new DuplicateAnalysisJobStatusDtoConverter();
  }

  /**
   * Finds the status of a specific analysis job.
   *
   * @param jobId the identifier of the job
   * @return the current job status
   */
  public DuplicateAnalysisJobStatusDto findById(UUID jobId) {
    DuplicateAnalysisJobStatus status = this.jobRegistry.findJob(jobId).orElseThrow();
    return this.statusDtoConverter.toDto(status);
  }
}
