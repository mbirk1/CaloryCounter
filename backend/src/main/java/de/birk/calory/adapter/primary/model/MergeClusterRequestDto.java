package de.birk.calory.adapter.primary.model;

import java.util.UUID;

/**
 * Request body for confirming and merging a duplicate cluster (FR-5).
 *
 * @author Marius Birk
 */
public class MergeClusterRequestDto {

  private UUID canonicalFoodId;

  public MergeClusterRequestDto() {
  }

  public MergeClusterRequestDto(UUID canonicalFoodId) {
    this.canonicalFoodId = canonicalFoodId;
  }

  public UUID getCanonicalFoodId() {
    return canonicalFoodId;
  }
}
