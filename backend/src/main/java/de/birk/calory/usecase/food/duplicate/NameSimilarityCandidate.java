package de.birk.calory.usecase.food.duplicate;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A pair of food items whose (lower-cased) names are trigram-similar, before the FR-2
 * macro-nearness check is applied.
 *
 * @param firstFoodId one food item's identifier
 * @param secondFoodId the other food item's identifier
 * @param nameSimilarity the trigram name-similarity score, between 0 and 1
 * @author Marius Birk
 */
public record NameSimilarityCandidate(
    UUID firstFoodId, UUID secondFoodId, BigDecimal nameSimilarity) {
}
