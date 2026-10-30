package de.birk.calory.domain.food;

import java.util.Comparator;
import java.util.Map;

/**
 * A food item's diet compatibility, restricted to the vegan/vegetarian axis by design - other
 * dietary properties (gluten-free, halal, lactose-free, ...) are out of scope for now, but a
 * separate field per property keeps adding one later straightforward.
 *
 * @author Marius Birk
 */
public enum Diet {
  VEGAN,
  VEGETARIAN,
  NON_VEGETARIAN,
  UNKNOWN;

  private static final Map<Diet, Integer> STRICTNESS_RANK =
      Map.of(VEGAN, 0, VEGETARIAN, 1, NON_VEGETARIAN, 2, UNKNOWN, 3);

  /**
   * Orders diets from strictest (VEGAN) to weakest (UNKNOWN), independent of the enum's
   * declaration order - used to derive a recipe's diet as the "weakest link" of its foods.
   */
  public static final Comparator<Diet> STRICTNESS_ORDER =
      Comparator.comparingInt(diet -> STRICTNESS_RANK.get(diet));
}
