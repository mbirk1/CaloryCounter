package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.ImportJobStatusDto;

public class ImportJobStatusDtoConverterUnitTest {

  @Test
  public void toDtoCopiesAllFieldsTest() {
    // Arrange
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 2000);
    status.incrementBytesRead(1500);
    status.incrementProcessed(10);
    status.incrementImported(6);
    status.incrementSkipped(3);
    status.incrementError(1);
    status.complete();
    ImportJobStatusDtoConverter converter = new ImportJobStatusDtoConverter();

    // Act
    ImportJobStatusDto dto = converter.toDto(status);

    // Assert
    assertThat(dto.getJobId()).isEqualTo(status.getJobId());
    assertThat(dto.getState()).isEqualTo("COMPLETED");
    assertThat(dto.getTotalBytes()).isEqualTo(2000);
    assertThat(dto.getBytesRead()).isEqualTo(1500);
    assertThat(dto.getProcessedRows()).isEqualTo(10);
    assertThat(dto.getImportedCount()).isEqualTo(6);
    assertThat(dto.getSkippedCount()).isEqualTo(3);
    assertThat(dto.getErrorCount()).isEqualTo(1);
    assertThat(dto.getStartedAt()).isEqualTo(status.getStartedAt());
    assertThat(dto.getFinishedAt()).isEqualTo(status.getFinishedAt());
  }
}
