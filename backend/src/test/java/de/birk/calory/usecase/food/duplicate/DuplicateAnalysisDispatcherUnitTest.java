package de.birk.calory.usecase.food.duplicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

public class DuplicateAnalysisDispatcherUnitTest {

  @Test
  public void dispatchRunsTheGivenTaskTest() {
    // Arrange
    DuplicateAnalysisDispatcher dispatcher = new DuplicateAnalysisDispatcher();
    Runnable task = mock(Runnable.class);

    // Act
    dispatcher.dispatch(task);

    // Assert
    verify(task).run();
  }
}
