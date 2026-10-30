package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection for a single name-similar pair of food items found by
 * {@code FoodRepository#findNameSimilarPairs}, before the FR-2 macro-nearness check is applied.
 *
 * @author Marius Birk
 */
public interface NameSimilarityPair {

  UUID getFirstFoodId();

  UUID getSecondFoodId();

  BigDecimal getNameSimilarity();
}
