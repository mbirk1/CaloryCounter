package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Persistence entity for a single food item proposed as part of a duplicate cluster, i.e. one
 * row of {@code tab_calory_food_duplicate_cluster_member}.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_food_duplicate_cluster_member")
@IdClass(DuplicateClusterMemberId.class)
public class DuplicateClusterMemberPersistence {

  @Id
  @ManyToOne
  @JoinColumn(name = "cluster_id")
  private DuplicateClusterPersistence cluster;

  @Id
  @ManyToOne
  @JoinColumn(name = "food_id")
  private FoodPersistence food;

  @Column(name = "similarity_score")
  private BigDecimal similarityScore;

  @Column(name = "suggested_canonical")
  private boolean suggestedCanonical;

  public DuplicateClusterMemberPersistence() {
  }

  /**
   * Creates a cluster member row.
   *
   * @param cluster the owning cluster
   * @param food the proposed-duplicate food item
   * @param similarityScore the highest name-similarity score this food reached against another
   *     member of the same cluster
   * @param suggestedCanonical whether this member is the system-suggested canonical item
   */
  public DuplicateClusterMemberPersistence(
      DuplicateClusterPersistence cluster,
      FoodPersistence food,
      BigDecimal similarityScore,
      boolean suggestedCanonical) {
    this.cluster = cluster;
    this.food = food;
    this.similarityScore = similarityScore;
    this.suggestedCanonical = suggestedCanonical;
  }

  public DuplicateClusterPersistence getCluster() {
    return cluster;
  }

  public FoodPersistence getFood() {
    return food;
  }

  public BigDecimal getSimilarityScore() {
    return similarityScore;
  }

  public boolean isSuggestedCanonical() {
    return suggestedCanonical;
  }
}
