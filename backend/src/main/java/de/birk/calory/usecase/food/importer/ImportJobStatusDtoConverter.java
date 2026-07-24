package de.birk.calory.usecase.food.importer;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;

/**
 * Maps a {@link FoodImportJobStatus} to the {@link ImportJobStatusDto} returned by the API.
 *
 * <p>Unlike {@link de.birk.calory.usecase.converter.Converter}, this mapping is intentionally
 * one-directional only: a {@link FoodImportJobStatus} is a live, mutable, in-memory progress
 * tracker that a client-supplied DTO could never be used to reconstruct.
 *
 * @author Marius Birk
 */
public class ImportJobStatusDtoConverter {

  /**
   * Converts a job status into its API representation.
   *
   * @param status the job status
   * @return the details dto
   */
  public ImportJobStatusDto toDto(FoodImportJobStatus status) {
    return new ImportJobStatusDto(
        status.getJobId(),
        status.getState().name(),
        status.getProcessedRows(),
        status.getImportedCount(),
        status.getSkippedCount(),
        status.getErrorCount(),
        status.getStartedAt(),
        status.getFinishedAt()
    );
  }
}
