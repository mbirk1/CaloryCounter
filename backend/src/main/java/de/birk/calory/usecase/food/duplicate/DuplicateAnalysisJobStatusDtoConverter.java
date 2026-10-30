package de.birk.calory.usecase.food.duplicate;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;

/**
 * Maps a {@link DuplicateAnalysisJobStatus} to the {@link DuplicateAnalysisJobStatusDto} returned
 * by the API.
 *
 * <p>Unlike {@link de.birk.calory.usecase.converter.Converter}, this mapping is intentionally
 * one-directional only: a {@link DuplicateAnalysisJobStatus} is a live, mutable, in-memory
 * progress tracker that a client-supplied DTO could never be used to reconstruct.
 *
 * @author Marius Birk
 */
public class DuplicateAnalysisJobStatusDtoConverter {

  /**
   * Converts a job status into its API representation.
   *
   * @param status the job status
   * @return the status dto
   */
  public DuplicateAnalysisJobStatusDto toDto(DuplicateAnalysisJobStatus status) {
    return new DuplicateAnalysisJobStatusDto(
        status.getJobId(),
        status.getSource().name(),
        status.getState().name(),
        status.getClustersFound(),
        status.getErrorMessage(),
        status.getStartedAt(),
        status.getFinishedAt()
    );
  }
}
