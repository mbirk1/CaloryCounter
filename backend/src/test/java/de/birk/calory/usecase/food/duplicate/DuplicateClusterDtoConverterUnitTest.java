package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;

public class DuplicateClusterDtoConverterUnitTest {

  private final DuplicateClusterDtoConverter converter = new DuplicateClusterDtoConverter();

  @Test
  public void mapsAClusterAndItsMembersTest() {
    // Arrange
    Food canonical = new Food("Apfel", BigDecimal.TEN, BigDecimal.valueOf(100));
    Food other = new Food("Apfel rot", BigDecimal.TEN, BigDecimal.valueOf(100));
    DuplicateCluster cluster = new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, ClusterStatus.PENDING,
        List.of(
            new DuplicateClusterMember(canonical, new BigDecimal("0.80"), true),
            new DuplicateClusterMember(other, new BigDecimal("0.80"), false)
        ),
        Instant.now(), null);

    // Act
    DuplicateClusterDto dto = converter.toDto(cluster);

    // Assert
    assertThat(dto.getId()).isEqualTo(cluster.getId());
    assertThat(dto.getSource()).isEqualTo("BLS");
    assertThat(dto.getStatus()).isEqualTo("PENDING");
    assertThat(dto.getMembers()).hasSize(2);
    assertThat(dto.getMembers().get(0).getFood().getName()).isEqualTo("Apfel");
    assertThat(dto.getMembers().get(0).isSuggestedCanonical()).isTrue();
    assertThat(dto.getMembers().get(1).isSuggestedCanonical()).isFalse();
  }
}
