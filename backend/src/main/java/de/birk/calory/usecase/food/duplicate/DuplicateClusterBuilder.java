package de.birk.calory.usecase.food.duplicate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;

/**
 * Pure, DB-free FR-2 clustering logic: turns a flat list of name-similar candidate pairs into
 * transitively grouped clusters (union-find), after additionally requiring every pair's present
 * macros to be within a configurable tolerance of one another - name similarity alone would
 * conflate genuinely distinct BLS preparation variants of the same base food (e.g. "Apfel roh"
 * vs. "Apfel gedünstet"), which the macro check tells apart.
 *
 * <p>Deliberately has no Spring/DB dependencies, so the union-find and canonical-selection logic
 * can be unit tested directly against plain {@link Food} objects.
 *
 * @author Marius Birk
 */
public class DuplicateClusterBuilder {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  /**
   * Groups name-similar, macro-near candidate pairs into clusters.
   *
   * @param candidates every name-similar pair found for a single {@code FoodSource}, not yet
   *     filtered by macro-nearness
   * @param foodsById every food item referenced by {@code candidates}, keyed by id
   * @param macroTolerancePercent the maximum allowed relative deviation between two present
   *     macro/calory values, e.g. {@code 10} for 10%
   * @param excludedFoodIdSets food-id combinations to skip entirely (already-rejected clusters
   *     for this source, per FR-2's re-analysis exclusion)
   * @return every resulting cluster with two or more members, in no particular order
   */
  public List<ProposedCluster> build(
      List<NameSimilarityCandidate> candidates,
      Map<UUID, Food> foodsById,
      BigDecimal macroTolerancePercent,
      Set<Set<UUID>> excludedFoodIdSets) {
    Map<UUID, UUID> parent = new HashMap<>();
    List<NameSimilarityCandidate> acceptedEdges = new ArrayList<>();

    for (NameSimilarityCandidate candidate : candidates) {
      Food first = foodsById.get(candidate.firstFoodId());
      Food second = foodsById.get(candidate.secondFoodId());
      if (first == null || second == null
          || !macrosWithinTolerance(first, second, macroTolerancePercent)) {
        continue;
      }
      union(parent, candidate.firstFoodId(), candidate.secondFoodId());
      acceptedEdges.add(candidate);
    }

    Map<UUID, List<UUID>> groups = new LinkedHashMap<>();
    for (UUID id : parent.keySet()) {
      groups.computeIfAbsent(find(parent, id), key -> new ArrayList<>()).add(id);
    }

    List<ProposedCluster> result = new ArrayList<>();
    for (List<UUID> memberIds : groups.values()) {
      if (memberIds.size() < 2) {
        continue;
      }
      Set<UUID> idSet = new HashSet<>(memberIds);
      if (excludedFoodIdSets.contains(idSet)) {
        continue;
      }
      result.add(toProposedCluster(idSet, acceptedEdges, foodsById));
    }
    return result;
  }

  private ProposedCluster toProposedCluster(
      Set<UUID> idSet, List<NameSimilarityCandidate> acceptedEdges, Map<UUID, Food> foodsById) {
    Map<UUID, BigDecimal> maxScore = new HashMap<>();
    for (NameSimilarityCandidate edge : acceptedEdges) {
      if (idSet.contains(edge.firstFoodId()) && idSet.contains(edge.secondFoodId())) {
        maxScore.merge(edge.firstFoodId(), edge.nameSimilarity(), BigDecimal::max);
        maxScore.merge(edge.secondFoodId(), edge.nameSimilarity(), BigDecimal::max);
      }
    }

    List<UUID> sortedIds = idSet.stream().sorted().toList();
    UUID canonicalId = chooseCanonical(sortedIds, foodsById);

    List<DuplicateClusterMember> members = sortedIds.stream()
        .map(id -> new DuplicateClusterMember(
            foodsById.get(id), maxScore.get(id), id.equals(canonicalId)))
        .toList();
    return new ProposedCluster(members);
  }

  // The member with the most filled-in optional fields is the most complete, and therefore the
  // best canonical candidate; ties are broken deterministically by ascending id (sortedIds is
  // already sorted), so the same input always proposes the same canonical member.
  private UUID chooseCanonical(List<UUID> sortedIds, Map<UUID, Food> foodsById) {
    UUID best = null;
    int bestCount = -1;
    for (UUID id : sortedIds) {
      int count = filledOptionalFieldCount(foodsById.get(id));
      if (count > bestCount) {
        bestCount = count;
        best = id;
      }
    }
    return best;
  }

  private int filledOptionalFieldCount(Food food) {
    Object[] optionalFields = {
        food.getBrand(), food.getCategory(), food.getImageUrl(), food.getFat(),
        food.getSaturatedFat(), food.getCarbohydrates(), food.getSugar(), food.getFiber(),
        food.getProtein(), food.getSalt(), food.getSodium(),
    };
    int count = 0;
    for (Object field : optionalFields) {
      if (field != null) {
        count++;
      }
    }
    return count;
  }

  private boolean macrosWithinTolerance(Food first, Food second, BigDecimal tolerancePercent) {
    return withinTolerance(first.getCalory(), second.getCalory(), tolerancePercent)
        && withinTolerance(first.getFat(), second.getFat(), tolerancePercent)
        && withinTolerance(first.getSaturatedFat(), second.getSaturatedFat(), tolerancePercent)
        && withinTolerance(first.getCarbohydrates(), second.getCarbohydrates(), tolerancePercent)
        && withinTolerance(first.getSugar(), second.getSugar(), tolerancePercent)
        && withinTolerance(first.getFiber(), second.getFiber(), tolerancePercent)
        && withinTolerance(first.getProtein(), second.getProtein(), tolerancePercent)
        && withinTolerance(first.getSalt(), second.getSalt(), tolerancePercent)
        && withinTolerance(first.getSodium(), second.getSodium(), tolerancePercent);
  }

  // A macro missing on either side is skipped, per FR-2's "alle vorhandenen Makros" - only
  // macros present on both sides must be near one another.
  private boolean withinTolerance(BigDecimal x, BigDecimal y, BigDecimal tolerancePercent) {
    if (x == null || y == null) {
      return true;
    }
    BigDecimal maxAbs = x.abs().max(y.abs());
    if (maxAbs.signum() == 0) {
      return true;
    }
    BigDecimal diff = x.subtract(y).abs();
    BigDecimal allowed = maxAbs.multiply(tolerancePercent).divide(HUNDRED, MathContext.DECIMAL64);
    return diff.compareTo(allowed) <= 0;
  }

  private UUID find(Map<UUID, UUID> parent, UUID id) {
    UUID root = id;
    while (!parent.get(root).equals(root)) {
      root = parent.get(root);
    }
    UUID current = id;
    while (!current.equals(root)) {
      UUID next = parent.get(current);
      parent.put(current, root);
      current = next;
    }
    return root;
  }

  private void union(Map<UUID, UUID> parent, UUID first, UUID second) {
    parent.putIfAbsent(first, first);
    parent.putIfAbsent(second, second);
    UUID firstRoot = find(parent, first);
    UUID secondRoot = find(parent, second);
    if (!firstRoot.equals(secondRoot)) {
      parent.put(firstRoot, secondRoot);
    }
  }
}
