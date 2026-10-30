package de.birk.calory.adapter.secondary.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Persistence entity for a proposed duplicate cluster.
 *
 * <p>{@code status} and {@code resolvedAt} are mutable (unlike most other fields in this
 * codebase's persistence entities): a cluster starts {@code PENDING} and is later confirmed or
 * rejected in place, rather than being recreated - mirroring {@link FoodPersistence}'s mutable
 * {@code importJobId}.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_food_duplicate_cluster")
public class DuplicateClusterPersistence {

  @Id
  @Column
  private UUID id;

  @Column
  private String source;

  @Column
  private String status;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  // Members don't exist independently of their cluster (orphanRemoval), and are always saved/
  // deleted together with it (cascade = ALL) - the join-table-turned-entity is entirely owned by
  // the cluster side, same reasoning as RecipePersistence#foodEntries.
  @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DuplicateClusterMemberPersistence> members;

  public DuplicateClusterPersistence() {
  }

  /**
   * Creates a new, pending cluster.
   *
   * @param id the identifier
   * @param source the name of the {@code FoodSource} enum constant this cluster's members all
   *     belong to
   * @param status the name of the {@code ClusterStatus} enum constant
   * @param createdAt when this cluster was proposed
   * @param resolvedAt when this cluster was confirmed or rejected, or {@code null} while pending
   */
  public DuplicateClusterPersistence(
      UUID id, String source, String status, Instant createdAt, Instant resolvedAt) {
    this.id = id;
    this.source = source;
    this.status = status;
    this.createdAt = createdAt;
    this.resolvedAt = resolvedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(Instant resolvedAt) {
    this.resolvedAt = resolvedAt;
  }

  public List<DuplicateClusterMemberPersistence> getMembers() {
    return members;
  }

  public void setMembers(List<DuplicateClusterMemberPersistence> members) {
    this.members = members;
  }
}
