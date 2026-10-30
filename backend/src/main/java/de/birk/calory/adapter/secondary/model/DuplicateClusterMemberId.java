package de.birk.calory.adapter.secondary.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for {@link DuplicateClusterMemberPersistence}, mirroring the
 * {@code (cluster_id, food_id)} composite primary key on
 * {@code tab_calory_food_duplicate_cluster_member}. Field names must match
 * {@link DuplicateClusterMemberPersistence}'s {@code @Id} field names exactly, per the
 * {@code @IdClass} contract.
 *
 * @author Marius Birk
 */
public class DuplicateClusterMemberId implements Serializable {

  private UUID cluster;
  private UUID food;

  public DuplicateClusterMemberId() {
  }

  public DuplicateClusterMemberId(UUID cluster, UUID food) {
    this.cluster = cluster;
    this.food = food;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof DuplicateClusterMemberId)) {
      return false;
    }
    DuplicateClusterMemberId that = (DuplicateClusterMemberId) object;
    return Objects.equals(cluster, that.cluster) && Objects.equals(food, that.food);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cluster, food);
  }
}
