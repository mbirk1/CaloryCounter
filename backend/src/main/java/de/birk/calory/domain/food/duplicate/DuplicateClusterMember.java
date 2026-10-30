package de.birk.calory.domain.food.duplicate;

import java.math.BigDecimal;

import de.birk.calory.domain.food.Food;

/**
 * A single food item proposed as part of a {@link DuplicateCluster}, together with how similar
 * it is to the rest of the cluster and whether it is the suggested canonical item.
 *
 * @author Marius Birk
 */
public class DuplicateClusterMember {

  private final Food food;
  private final BigDecimal similarityScore;
  private final boolean suggestedCanonical;

  /**
   * Creates a cluster member.
   *
   * @param food the food item
   * @param similarityScore the highest name-similarity score this food reached against another
   *     member of the same cluster
   * @param suggestedCanonical whether this member is the system-suggested canonical item
   */
  public DuplicateClusterMember(Food food, BigDecimal similarityScore, boolean suggestedCanonical) {
    this.food = food;
    this.similarityScore = similarityScore;
    this.suggestedCanonical = suggestedCanonical;
  }

  public Food getFood() {
    return food;
  }

  public BigDecimal getSimilarityScore() {
    return similarityScore;
  }

  public boolean isSuggestedCanonical() {
    return suggestedCanonical;
  }
}
