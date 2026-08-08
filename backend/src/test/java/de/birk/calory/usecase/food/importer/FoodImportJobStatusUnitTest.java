package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FoodImportJobStatusUnitTest {

  @Test
  public void newJobStartsRunningWithTheGivenTotalBytesAndNoBytesReadYetTest() {
    // Arrange & Act
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 5_000L);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.RUNNING);
    assertThat(status.getTotalBytes()).isEqualTo(5_000L);
    assertThat(status.getBytesRead()).isZero();
    assertThat(status.getFinishedAt()).isNull();
  }

  @Test
  public void incrementBytesReadAccumulatesAcrossMultipleCallsTest() {
    // Arrange
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 5_000L);

    // Act
    status.incrementBytesRead(1_200L);
    status.incrementBytesRead(800L);

    // Assert
    assertThat(status.getBytesRead()).isEqualTo(2_000L);
  }

  @Test
  public void completeSetsStateAndFinishedAtTest() {
    // Arrange
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 100L);

    // Act
    status.complete();

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.COMPLETED);
    assertThat(status.getFinishedAt()).isNotNull();
  }

  @Test
  public void failSetsStateAndFinishedAtTest() {
    // Arrange
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 100L);

    // Act
    status.fail();

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    assertThat(status.getFinishedAt()).isNotNull();
  }
}
