package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

/**
 * A single proposed-duplicate food item within a {@link DuplicateClusterDto}.
 *
 * @author Marius Birk
 */
public class DuplicateClusterMemberDto {

  private FoodDetailsDto food;
  private BigDecimal similarityScore;
  private boolean suggestedCanonical;

  public DuplicateClusterMemberDto() {
  }

  /**
   * Constructor that takes all properties needed to describe a cluster member.
   *
   * @param food the full food item, so the review view can show name, macros etc.
   * @param similarityScore the highest name-similarity score this food reached against another
   *     member of the same cluster
   * @param suggestedCanonical whether this member is the system-suggested canonical item
   */
  public DuplicateClusterMemberDto(
      FoodDetailsDto food, BigDecimal similarityScore, boolean suggestedCanonical) {
    this.food = food;
    this.similarityScore = similarityScore;
    this.suggestedCanonical = suggestedCanonical;
  }

  public FoodDetailsDto getFood() {
    return food;
  }

  public BigDecimal getSimilarityScore() {
    return similarityScore;
  }

  public boolean isSuggestedCanonical() {
    return suggestedCanonical;
  }
}
