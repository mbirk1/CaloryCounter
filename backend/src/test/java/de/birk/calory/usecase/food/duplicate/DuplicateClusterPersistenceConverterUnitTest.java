package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;

public class DuplicateClusterPersistenceConverterUnitTest {

  private final DuplicateClusterPersistenceConverter converter =
      new DuplicateClusterPersistenceConverter();

  @Test
  public void roundTripsAClusterWithFullFoodDataTest() {
    // Arrange - brand is a field the old, lossy Recipe converters used to drop; asserting it
    // survives here proves this converter delegates to the full FoodPersistenceConverter.
    Food canonical = Food.builder().id(UUID.randomUUID()).name("Apfel")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).brand("Demeter").build();
    Food other = Food.builder().id(UUID.randomUUID()).name("Apfel rot")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
    DuplicateCluster cluster = new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, ClusterStatus.PENDING,
        List.of(
            new DuplicateClusterMember(canonical, new BigDecimal("0.80"), true),
            new DuplicateClusterMember(other, new BigDecimal("0.80"), false)
        ),
        Instant.now(), null);

    // Act
    DuplicateClusterPersistence persistence = converter.convertFromEntity(cluster);
    DuplicateCluster roundTripped = converter.convertFromDto(persistence);

    // Assert
    assertThat(persistence.getSource()).isEqualTo("BLS");
    assertThat(persistence.getStatus()).isEqualTo("PENDING");
    assertThat(persistence.getMembers()).hasSize(2);

    assertThat(roundTripped.getSource()).isEqualTo(FoodSource.BLS);
    assertThat(roundTripped.getStatus()).isEqualTo(ClusterStatus.PENDING);
    Food roundTrippedCanonical = roundTripped.getMembers().stream()
        .filter(DuplicateClusterMember::isSuggestedCanonical)
        .findFirst().orElseThrow()
        .getFood();
    assertThat(roundTrippedCanonical.getBrand()).isEqualTo("Demeter");
  }
}
