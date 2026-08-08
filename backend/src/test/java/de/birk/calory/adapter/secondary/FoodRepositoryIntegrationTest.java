package de.birk.calory.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.birk.calory.IntegrationTest;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Integration test for {@link FoodRepository#deleteByImportJobId(UUID)}, the bulk-delete used to
 * roll back everything a failed CSV import job has already persisted.
 *
 * <p>Deliberately NOT {@code @Transactional}: the real caller (the CSV import's {@code @Async}
 * background thread) has no ambient transaction either, and wrapping this test in one would mask
 * exactly that - a missing {@code @Transactional} on the repository method previously surfaced
 * only in production as a {@code TransactionRequiredException}, never in a transactional test.
 *
 * @author Marius Birk
 */
@IntegrationTest
public class FoodRepositoryIntegrationTest {

  @Autowired
  private FoodRepository foodRepository;

  private UUID fromDeletedJobId;
  private UUID fromOtherJobId;

  @AfterEach
  public void cleanUp() {
    if (this.fromDeletedJobId != null) {
      this.foodRepository.deleteById(this.fromDeletedJobId);
    }
    if (this.fromOtherJobId != null) {
      this.foodRepository.deleteById(this.fromOtherJobId);
    }
  }

  @Test
  @DisplayName("deleteByImportJobId only removes rows belonging to the given job")
  public void deleteByImportJobIdOnlyRemovesRowsOfTheGivenJobTest() {
    // Arrange
    UUID jobIdToDelete = UUID.randomUUID();
    UUID otherJobId = UUID.randomUUID();

    FoodPersistence fromDeletedJob = foodItem("From Deleted Job");
    fromDeletedJob.setImportJobId(jobIdToDelete);
    this.fromDeletedJobId = fromDeletedJob.getId();

    FoodPersistence fromOtherJob = foodItem("From Other Job");
    fromOtherJob.setImportJobId(otherJobId);
    this.fromOtherJobId = fromOtherJob.getId();

    this.foodRepository.saveAll(List.of(fromDeletedJob, fromOtherJob));

    // Act
    this.foodRepository.deleteByImportJobId(jobIdToDelete);

    // Assert
    assertThat(this.foodRepository.findById(fromDeletedJob.getId())).isEmpty();
    assertThat(this.foodRepository.findById(fromOtherJob.getId())).isPresent();
  }

  private FoodPersistence foodItem(String name) {
    return new FoodPersistence(
        UUID.randomUUID(), name, new BigDecimal("42.00"), new BigDecimal("100"));
  }
}
