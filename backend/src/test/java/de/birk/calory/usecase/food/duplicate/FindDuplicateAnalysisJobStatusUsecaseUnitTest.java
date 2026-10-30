package de.birk.calory.usecase.food.duplicate;

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

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;
import de.birk.calory.domain.food.FoodSource;

@ExtendWith(MockitoExtension.class)
public class FindDuplicateAnalysisJobStatusUsecaseUnitTest {

  @Mock
  private DuplicateAnalysisJobRegistry jobRegistry;

  @Test
  public void findsAnExistingJobTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.BLS);
    when(jobRegistry.findJob(status.getJobId())).thenReturn(Optional.of(status));
    FindDuplicateAnalysisJobStatusUsecase usecase =
        new FindDuplicateAnalysisJobStatusUsecase(jobRegistry);

    // Act
    DuplicateAnalysisJobStatusDto result = usecase.findById(status.getJobId());

    // Assert
    assertThat(result.getJobId()).isEqualTo(status.getJobId());
  }

  @Test
  public void throwsWhenTheJobIsUnknownTest() {
    // Arrange
    UUID unknownId = UUID.randomUUID();
    when(jobRegistry.findJob(unknownId)).thenReturn(Optional.empty());
    FindDuplicateAnalysisJobStatusUsecase usecase =
        new FindDuplicateAnalysisJobStatusUsecase(jobRegistry);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findById(unknownId))
        .isInstanceOf(NoSuchElementException.class);
  }
}
