package de.birk.calory.usecase.food.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

public class FoodImportDispatcherUnitTest {

  @Test
  public void dispatchRunsTheGivenTaskTest() {
    // Arrange
    FoodImportDispatcher dispatcher = new FoodImportDispatcher();
    Runnable task = mock(Runnable.class);

    // Act
    dispatcher.dispatch(task);

    // Assert
    verify(task).run();
  }
}
