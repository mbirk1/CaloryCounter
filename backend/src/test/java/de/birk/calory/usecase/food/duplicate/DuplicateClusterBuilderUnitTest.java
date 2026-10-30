package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;

public class DuplicateClusterBuilderUnitTest {

  private static final BigDecimal TEN_PERCENT = BigDecimal.TEN;

  private final DuplicateClusterBuilder builder = new DuplicateClusterBuilder();

  private Food food(UUID id, String name, int calory, BigDecimal fat) {
    return Food.builder().id(id).name(name).calory(BigDecimal.valueOf(calory))
        .grams(BigDecimal.valueOf(100)).fat(fat).build();
  }

  @Test
  public void transitivelyGroupsABAndBCIntoOneClusterTest() {
    // Arrange - A~B and B~C are both name+macro similar, but A and C were never directly
    // compared - union-find must still put all three in one cluster.
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    UUID c = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    foods.put(b, food(b, "Apfel rot", 52, null));
    foods.put(c, food(c, "Apfel rot Bio", 52, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.60")),
        new NameSimilarityCandidate(b, c, new BigDecimal("0.55"))
    );

    // Act
    List<ProposedCluster> result =
        builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).members()).extracting(m -> m.getFood().getId())
        .containsExactlyInAnyOrder(a, b, c);
  }

  @Test
  public void rejectsAPairWhenMacrosDeviateBeyondToleranceTest() {
    // Arrange - names are similar enough, but calories differ by more than 10%.
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel roh", 52, null));
    foods.put(b, food(b, "Apfel gedünstet", 80, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.70"))
    );

    // Act
    List<ProposedCluster> result =
        builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert - the BLS-variant scenario from the ticket: no cluster proposed.
    assertThat(result).isEmpty();
  }

  @Test
  public void acceptsAPairWhenMacrosAreWithinToleranceTest() {
    // Arrange - 52 vs 55 kcal is within 10% of the larger value (55 * 0.10 = 5.5).
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    foods.put(b, food(b, "Apfel Bio", 55, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.75"))
    );

    // Act
    List<ProposedCluster> result =
        builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).hasSize(1);
  }

  @Test
  public void treatsAMacroMissingOnEitherSideAsNotBlockingTest() {
    // Arrange - fat is present on neither/one side; only present-on-both macros must be near.
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    foods.put(b, food(b, "Apfel Bio", 52, new BigDecimal("0.2")));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.75"))
    );

    // Act
    List<ProposedCluster> result =
        builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).hasSize(1);
  }

  @Test
  public void treatsBothSidesZeroAsWithinToleranceTest() {
    // Arrange - 0 vs 0 must not divide by zero and must count as equal.
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Wasser", 0, BigDecimal.ZERO));
    foods.put(b, food(b, "Wasser still", 0, BigDecimal.ZERO));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.80"))
    );

    // Act
    List<ProposedCluster> result =
        builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).hasSize(1);
  }

  @Test
  public void assignsEachMemberTheHighestScoreItReachedWithinTheClusterTest() {
    // Arrange - A~B at 0.60, B~C at 0.90; B's score should be the max of both (0.90).
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    UUID c = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    foods.put(b, food(b, "Apfel rot", 52, null));
    foods.put(c, food(c, "Apfel rot fein", 52, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.60")),
        new NameSimilarityCandidate(b, c, new BigDecimal("0.90"))
    );

    // Act
    List<ProposedCluster> result = builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    Map<UUID, BigDecimal> scoresById = new HashMap<>();
    for (DuplicateClusterMember member : result.get(0).members()) {
      scoresById.put(member.getFood().getId(), member.getSimilarityScore());
    }
    assertThat(scoresById.get(a)).isEqualByComparingTo("0.60");
    assertThat(scoresById.get(b)).isEqualByComparingTo("0.90");
    assertThat(scoresById.get(c)).isEqualByComparingTo("0.90");
  }

  @Test
  public void suggestsTheMemberWithTheMostFilledOptionalFieldsAsCanonicalTest() {
    // Arrange - B has brand+category+fat filled in, A only has the required core fields.
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Food sparse = food(a, "Apfel", 52, null);
    Food rich = Food.builder().id(b).name("Apfel Bio").calory(BigDecimal.valueOf(52))
        .grams(BigDecimal.valueOf(100)).brand("Demeter").category("Obst")
        .fat(new BigDecimal("0.2")).build();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, sparse);
    foods.put(b, rich);
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.75"))
    );

    // Act
    List<ProposedCluster> result = builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    DuplicateClusterMember canonical = result.get(0).members().stream()
        .filter(DuplicateClusterMember::isSuggestedCanonical)
        .findFirst().orElseThrow();
    assertThat(canonical.getFood().getId()).isEqualTo(b);
  }

  @Test
  public void breaksACanonicalTieByTheLowerIdTest() {
    // Arrange - both members equally complete (only core fields) - the lower UUID wins.
    UUID low = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID high = UUID.fromString("00000000-0000-0000-0000-000000000002");
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(high, food(high, "Apfel", 52, null));
    foods.put(low, food(low, "Apfel rot", 52, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(low, high, new BigDecimal("0.75"))
    );

    // Act
    List<ProposedCluster> result = builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    DuplicateClusterMember canonical = result.get(0).members().stream()
        .filter(DuplicateClusterMember::isSuggestedCanonical)
        .findFirst().orElseThrow();
    assertThat(canonical.getFood().getId()).isEqualTo(low);
  }

  @Test
  public void excludesAClusterMatchingAnAlreadyRejectedFoodIdCombinationTest() {
    // Arrange
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    foods.put(b, food(b, "Apfel rot", 52, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, b, new BigDecimal("0.75"))
    );
    Set<Set<UUID>> excluded = Set.of(Set.of(a, b));

    // Act
    List<ProposedCluster> result = builder.build(candidates, foods, TEN_PERCENT, excluded);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void ignoresACandidatePairReferencingAnUnknownFoodIdTest() {
    // Arrange - defensive guard: a food id present in a candidate pair but missing from the
    // lookup map (should never happen in practice, but must not blow up if it does).
    UUID a = UUID.randomUUID();
    UUID unknown = UUID.randomUUID();
    Map<UUID, Food> foods = new HashMap<>();
    foods.put(a, food(a, "Apfel", 52, null));
    List<NameSimilarityCandidate> candidates = List.of(
        new NameSimilarityCandidate(a, unknown, new BigDecimal("0.75"))
    );

    // Act
    List<ProposedCluster> result = builder.build(candidates, foods, TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void returnsNoClustersForCandidatesThatNeverPairUpTest() {
    // Arrange - no candidates at all.
    // Act
    List<ProposedCluster> result = builder.build(List.of(), Map.of(), TEN_PERCENT, Set.of());

    // Assert
    assertThat(result).isEmpty();
  }
}
