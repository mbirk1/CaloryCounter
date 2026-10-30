package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.DuplicateAnalysisJobStatusDto;
import de.birk.calory.domain.food.FoodSource;

public class DuplicateAnalysisJobStatusDtoConverterUnitTest {

  private final DuplicateAnalysisJobStatusDtoConverter converter =
      new DuplicateAnalysisJobStatusDtoConverter();

  @Test
  public void mapsARunningStatusTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.OPENFOODFACTS);

    // Act
    DuplicateAnalysisJobStatusDto dto = converter.toDto(status);

    // Assert
    assertThat(dto.getJobId()).isEqualTo(status.getJobId());
    assertThat(dto.getSource()).isEqualTo("OPENFOODFACTS");
    assertThat(dto.getState()).isEqualTo("RUNNING");
    assertThat(dto.getClustersFound()).isZero();
    assertThat(dto.getErrorMessage()).isNull();
    assertThat(dto.getFinishedAt()).isNull();
  }

  @Test
  public void mapsAFailedStatusTest() {
    // Arrange
    DuplicateAnalysisJobStatus status =
        new DuplicateAnalysisJobStatus(UUID.randomUUID(), FoodSource.BLS);
    status.fail("boom");

    // Act
    DuplicateAnalysisJobStatusDto dto = converter.toDto(status);

    // Assert
    assertThat(dto.getState()).isEqualTo("FAILED");
    assertThat(dto.getErrorMessage()).isEqualTo("boom");
    assertThat(dto.getFinishedAt()).isNotNull();
  }
}
