package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

@ExtendWith(MockitoExtension.class)
public class FoodImportPersisterUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void persistBatchSavesTheWholeBatchInOneCallOnSuccessTest() {
    // Arrange
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    List<FoodPersistence> batch = List.of(food("Apple", null));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    persister.persistBatch(batch, status);

    // Assert
    verify(foodRepository).saveAll(batch);
    verify(foodRepository, never()).save(any());
    assertThat(status.getImportedCount()).isEqualTo(1);
  }

  @Test
  public void persistBatchFallsBackToRecordByRecordOnDataAccessExceptionTest() {
    // Arrange
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    List<FoodPersistence> batch = List.of(food("Apple", null), food("Banana", null));
    doThrow(new InvalidDataAccessResourceUsageException("numeric overflow"))
        .when(foodRepository).saveAll(anyList());
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    persister.persistBatch(batch, status);

    // Assert
    verify(foodRepository, times(2)).save(any(FoodPersistence.class));
    assertThat(status.getImportedCount()).isEqualTo(2);
  }

  @Test
  public void persistOneByOneBackfillsDietOnADuplicateExternalIdTest() {
    // Arrange
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    FoodPersistence duplicate = food("Tofu", "42");
    List<FoodPersistence> batch = List.of(duplicate);
    doThrow(new DataIntegrityViolationException("duplicate external_id"))
        .when(foodRepository).saveAll(anyList());
    doThrow(new DataIntegrityViolationException("duplicate external_id"))
        .when(foodRepository).save(any(FoodPersistence.class));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    persister.persistBatch(batch, status);

    // Assert
    verify(foodRepository).updateDietByExternalId("42", "UNKNOWN");
    assertThat(status.getSkippedCount()).isEqualTo(1);
    assertThat(status.getImportedCount()).isEqualTo(0);
  }

  @Test
  public void persistOneByOneSkipsWithoutBackfillWhenExternalIdIsNullTest() {
    // Arrange - a manually-import-adjacent row with no externalId can't be backfilled by
    // external_id, so it is simply counted as skipped without touching the repository again.
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    FoodPersistence noExternalId = food("Apple", null);
    List<FoodPersistence> batch = List.of(noExternalId);
    doThrow(new DataIntegrityViolationException("some other constraint"))
        .when(foodRepository).saveAll(anyList());
    doThrow(new DataIntegrityViolationException("some other constraint"))
        .when(foodRepository).save(any(FoodPersistence.class));
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    persister.persistBatch(batch, status);

    // Assert
    verify(foodRepository, never()).updateDietByExternalId(any(), any());
    assertThat(status.getSkippedCount()).isEqualTo(1);
  }

  @Test
  public void rollbackAndFailDeletesTheJobsRowsAndMarksItFailedTest() {
    // Arrange
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    UUID jobId = UUID.randomUUID();
    FoodImportJobStatus status = new FoodImportJobStatus(jobId, 0);

    // Act
    persister.rollbackAndFail(status);

    // Assert
    verify(foodRepository).deleteByImportJobId(jobId);
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
  }

  @Test
  public void rollbackAndFailStillMarksTheJobFailedWhenTheDeleteItselfThrowsTest() {
    // Arrange
    FoodImportPersister persister = new FoodImportPersister(foodRepository);
    doThrow(new InvalidDataAccessResourceUsageException("db connection lost"))
        .when(foodRepository).deleteByImportJobId(any());
    FoodImportJobStatus status = new FoodImportJobStatus(UUID.randomUUID(), 0);

    // Act
    persister.rollbackAndFail(status);

    // Assert
    assertThat(status.getState()).isEqualTo(ImportJobState.FAILED);
    assertThat(status.getFinishedAt()).isNotNull();
  }

  private FoodPersistence food(String name, String externalId) {
    return new FoodPersistence.Builder()
        .id(UUID.randomUUID())
        .name(name)
        .calory(BigDecimal.TEN)
        .grams(BigDecimal.valueOf(100))
        .externalId(externalId)
        .diet("UNKNOWN")
        .build();
  }
}
