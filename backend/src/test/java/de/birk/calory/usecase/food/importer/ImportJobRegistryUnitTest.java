package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ImportJobRegistryUnitTest {

  @Test
  public void createJobRegistersAFreshRunningJobTest() {
    // Arrange
    ImportJobRegistry registry = new ImportJobRegistry();

    // Act
    FoodImportJobStatus status = registry.createJob();

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.RUNNING);
    assertThat(registry.findJob(status.getJobId())).contains(status);
  }

  @Test
  public void findJobReturnsEmptyForUnknownIdTest() {
    // Arrange
    ImportJobRegistry registry = new ImportJobRegistry();

    // Act
    Optional<FoodImportJobStatus> result = registry.findJob(UUID.randomUUID());

    // Assert
    assertThat(result).isEmpty();
  }
}
