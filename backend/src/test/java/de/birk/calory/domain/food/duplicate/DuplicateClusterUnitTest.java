package de.birk.calory.domain.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.exception.ValidationException;

public class DuplicateClusterUnitTest {

  private Food food(String name) {
    return new Food(name, BigDecimal.TEN, BigDecimal.valueOf(100));
  }

  private List<DuplicateClusterMember> twoMembers() {
    return List.of(
        new DuplicateClusterMember(food("Apfel"), new BigDecimal("0.7"), true),
        new DuplicateClusterMember(food("Apfel rot"), new BigDecimal("0.7"), false)
    );
  }

  @Test
  public void acceptsAValidClusterTest() {
    // Act
    DuplicateCluster cluster = new DuplicateCluster(
        UUID.randomUUID(), FoodSource.OPENFOODFACTS, ClusterStatus.PENDING, twoMembers(),
        Instant.now(), null);

    // Assert
    assertThat(cluster.getMembers()).hasSize(2);
    assertThat(cluster.getSource()).isEqualTo(FoodSource.OPENFOODFACTS);
    assertThat(cluster.getStatus()).isEqualTo(ClusterStatus.PENDING);
  }

  @Test
  public void rejectsAMissingSourceTest() {
    assertThatThrownBy(() -> new DuplicateCluster(
        UUID.randomUUID(), null, ClusterStatus.PENDING, twoMembers(), Instant.now(), null))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void rejectsAMissingStatusTest() {
    assertThatThrownBy(() -> new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, null, twoMembers(), Instant.now(), null))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void acceptsASingleMemberTest() {
    // A CONFIRMED cluster legitimately shrinks to just its canonical member once FR-5's merge
    // deletes every other one - reconstructing it from persistence must not throw.
    DuplicateCluster cluster = new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, ClusterStatus.CONFIRMED,
        List.of(new DuplicateClusterMember(food("Apfel"), BigDecimal.ONE, true)),
        Instant.now(), Instant.now());

    assertThat(cluster.getMembers()).hasSize(1);
  }

  @Test
  public void rejectsEmptyMembersTest() {
    assertThatThrownBy(() -> new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, ClusterStatus.PENDING, List.of(), Instant.now(), null))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void rejectsNullMembersTest() {
    assertThatThrownBy(() -> new DuplicateCluster(
        UUID.randomUUID(), FoodSource.BLS, ClusterStatus.PENDING, null, Instant.now(), null))
        .isInstanceOf(ValidationException.class);
  }
}
