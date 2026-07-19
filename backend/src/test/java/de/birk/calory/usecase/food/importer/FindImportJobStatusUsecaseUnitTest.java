package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;

@ExtendWith(MockitoExtension.class)
public class FindImportJobStatusUsecaseUnitTest {

  @Mock
  private ImportJobRegistry jobRegistry;

  @Test
  public void findsAnExistingJobTest() {
    // Arrange
    UUID jobId = UUID.randomUUID();
    FoodImportJobStatus status = new FoodImportJobStatus(jobId);
    when(jobRegistry.findJob(jobId)).thenReturn(Optional.of(status));
    FindImportJobStatusUsecase usecase = new FindImportJobStatusUsecase(jobRegistry);

    // Act
    ImportJobStatusDto result = usecase.findById(jobId);

    // Assert
    assertThat(result.getJobId()).isEqualTo(jobId);
    assertThat(result.getState()).isEqualTo("RUNNING");
  }

  @Test
  public void throwsWhenJobIsUnknownTest() {
    // Arrange
    UUID jobId = UUID.randomUUID();
    when(jobRegistry.findJob(jobId)).thenReturn(Optional.empty());
    FindImportJobStatusUsecase usecase = new FindImportJobStatusUsecase(jobRegistry);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findById(jobId)).isInstanceOf(NoSuchElementException.class);
  }
}
