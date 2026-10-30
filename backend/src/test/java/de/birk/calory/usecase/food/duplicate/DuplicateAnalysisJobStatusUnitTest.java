package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.food.FoodSource;

public class DuplicateAnalysisJobStatusUnitTest {

  @Test
  public void completeSetsStateAndFinishedAtTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.BLS);

    // Act
    status.setClustersFound(3);
    status.complete();

    // Assert
    assertThat(status.getState()).isEqualTo(DuplicateAnalysisJobState.COMPLETED);
    assertThat(status.getClustersFound()).isEqualTo(3);
    assertThat(status.getFinishedAt()).isNotNull();
    assertThat(status.getErrorMessage()).isNull();
  }

  @Test
  public void failSetsStateAndErrorMessageTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.MANUAL);

    // Act
    status.fail("boom");

    // Assert
    assertThat(status.getState()).isEqualTo(DuplicateAnalysisJobState.FAILED);
    assertThat(status.getErrorMessage()).isEqualTo("boom");
    assertThat(status.getFinishedAt()).isNotNull();
  }
}
