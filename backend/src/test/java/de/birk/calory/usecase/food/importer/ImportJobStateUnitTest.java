package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ImportJobStateUnitTest {

  @Test
  public void valueOfReturnsMatchingConstantTest() {
    // Arrange & Act
    ImportJobState state = ImportJobState.valueOf("COMPLETED");

    // Assert
    assertThat(state).isEqualTo(ImportJobState.COMPLETED);
  }

  @Test
  public void valuesContainsAllThreeStatesTest() {
    // Arrange & Act
    ImportJobState[] values = ImportJobState.values();

    // Assert
    assertThat(values).containsExactly(
        ImportJobState.RUNNING, ImportJobState.COMPLETED, ImportJobState.FAILED);
  }
}
