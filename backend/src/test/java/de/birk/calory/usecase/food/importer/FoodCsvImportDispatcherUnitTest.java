package de.birk.calory.usecase.food.importer;

import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FoodCsvImportDispatcherUnitTest {

  @Mock
  private FoodCsvImportUsecase usecase;

  @Test
  public void dispatchDelegatesToTheUsecaseTest() {
    // Arrange
    FoodCsvImportDispatcher dispatcher = new FoodCsvImportDispatcher();
    Path filePath = Path.of("does-not-matter.csv");
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID());

    // Act
    dispatcher.dispatch(usecase, filePath, status);

    // Assert
    verify(usecase).importFromFile(filePath, status);
  }
}
