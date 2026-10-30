package de.birk.calory.domain.food.duplicate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.exception.ValidationException;

/**
 * A group of food items from the same {@link FoodSource} that the duplicate-detection analysis
 * (FR-2) considers similar enough to be the same underlying food, pending human review (FR-4).
 *
 * @author Marius Birk
 */
public class DuplicateCluster extends AbstractEntity<UUID> {

  private final UUID id;
  private final FoodSource source;
  private final ClusterStatus status;
  private final List<DuplicateClusterMember> members;
  private final Instant createdAt;
  private final Instant resolvedAt;

  /**
   * Creates a duplicate cluster.
   *
   * @param id the identifier
   * @param source the food source this cluster's members all belong to
   * @param status the review state
   * @param members the food items proposed as duplicates of one another
   * @param createdAt when this cluster was proposed
   * @param resolvedAt when this cluster was confirmed or rejected, or {@code null} while pending
   * @throws ValidationException if a required field is missing or there are no members
   */
  public DuplicateCluster(
      UUID id,
      FoodSource source,
      ClusterStatus status,
      List<DuplicateClusterMember> members,
      Instant createdAt,
      Instant resolvedAt) {
    this.id = id;
    this.source = source;
    this.status = status;
    this.members = members;
    this.createdAt = createdAt;
    this.resolvedAt = resolvedAt;
    validate();
  }

  // Only ">= 1 member" is a universal invariant - a cluster is always PROPOSED with >= 2 (that
  // rule lives in DuplicateClusterBuilder, the only place clusters are created), but a CONFIRMED
  // cluster legitimately shrinks to exactly its canonical member once merge (FR-5) deletes every
  // other one, and reconstructing that already-resolved cluster from persistence must not throw.
  @Override
  protected void validate() throws ValidationException {
    if (this.source == null || this.status == null) {
      throw new ValidationException();
    }
    if (this.members == null || this.members.isEmpty()) {
      throw new ValidationException();
    }
  }

  @Override
  public UUID getId() {
    return id;
  }

  public FoodSource getSource() {
    return source;
  }

  public ClusterStatus getStatus() {
    return status;
  }

  public List<DuplicateClusterMember> getMembers() {
    return members;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }
}
