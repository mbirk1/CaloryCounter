package de.birk.calory.usecase.food.importer;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;

/**
 * Usecase to find the status of a CSV import job.
 *
 * @author Marius Birk
 */
@Service
public class FindImportJobStatusUsecase {

  private final ImportJobRegistry jobRegistry;
  private final ImportJobStatusDtoConverter statusDtoConverter;

  public FindImportJobStatusUsecase(ImportJobRegistry jobRegistry) {
    this.jobRegistry = jobRegistry;
    this.statusDtoConverter = new ImportJobStatusDtoConverter();
  }

  /**
   * Finds the status of a specific import job.
   *
   * @param jobId the identifier of the job
   * @return the current job status
   */
  public ImportJobStatusDto findById(UUID jobId) {
    FoodImportJobStatus status = this.jobRegistry.findJob(jobId).orElseThrow();
    return this.statusDtoConverter.toDto(status);
  }
}
