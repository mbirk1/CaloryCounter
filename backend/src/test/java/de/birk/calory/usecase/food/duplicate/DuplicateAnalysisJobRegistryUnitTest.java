package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.food.FoodSource;

public class DuplicateAnalysisJobRegistryUnitTest {

  private final DuplicateAnalysisJobRegistry registry = new DuplicateAnalysisJobRegistry();

  @Test
  public void createJobRegistersAFindableRunningJobTest() {
    // Act
    DuplicateAnalysisJobStatus created = registry.createJob(FoodSource.OPENFOODFACTS);

    // Assert
    assertThat(created.getState()).isEqualTo(DuplicateAnalysisJobState.RUNNING);
    assertThat(registry.findJob(created.getJobId())).contains(created);
  }

  @Test
  public void findJobReturnsEmptyForAnUnknownIdTest() {
    // Act
    Optional<DuplicateAnalysisJobStatus> result = registry.findJob(UUID.randomUUID());

    // Assert
    assertThat(result).isEmpty();
  }
}
