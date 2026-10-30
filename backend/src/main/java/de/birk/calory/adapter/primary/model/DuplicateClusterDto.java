package de.birk.calory.adapter.primary.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The DTO that is returned to clients for a proposed duplicate cluster.
 *
 * @author Marius Birk
 */
public class DuplicateClusterDto {

  private UUID id;
  private String source;
  private String status;
  private List<DuplicateClusterMemberDto> members;
  private Instant createdAt;
  private Instant resolvedAt;

  public DuplicateClusterDto() {
  }

  /**
   * Constructor that takes all properties needed to describe a duplicate cluster.
   *
   * @param id the identifier
   * @param source the name of the {@code FoodSource} enum constant this cluster's members all
   *     belong to
   * @param status the name of the {@code ClusterStatus} enum constant
   * @param members the proposed-duplicate food items
   * @param createdAt when this cluster was proposed
   * @param resolvedAt when this cluster was confirmed or rejected, or {@code null} while pending
   */
  public DuplicateClusterDto(
      UUID id,
      String source,
      String status,
      List<DuplicateClusterMemberDto> members,
      Instant createdAt,
      Instant resolvedAt) {
    this.id = id;
    this.source = source;
    this.status = status;
    this.members = members;
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

  public List<DuplicateClusterMemberDto> getMembers() {
    return members;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }
}
