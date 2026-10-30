package de.birk.calory.usecase.food.duplicate;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.primary.model.DuplicateClusterMemberDto;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;

/**
 * Maps a {@link DuplicateCluster} to the {@link DuplicateClusterDto} returned by the API
 * (FR-4). One-directional only: clusters are never (re)constructed from a client-supplied DTO,
 * only their id and the chosen canonical food id are read back on merge/reject.
 *
 * @author Marius Birk
 */
public class DuplicateClusterDtoConverter {

  private final FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

  /**
   * Converts a cluster into its API representation.
   *
   * @param cluster the cluster
   * @return the cluster dto
   */
  public DuplicateClusterDto toDto(DuplicateCluster cluster) {
    return new DuplicateClusterDto(
        cluster.getId(),
        cluster.getSource().name(),
        cluster.getStatus().name(),
        cluster.getMembers().stream()
            .map(member -> new DuplicateClusterMemberDto(
                this.foodDetailsDtoConverter.convertFromEntity(member.getFood()),
                member.getSimilarityScore(),
                member.isSuggestedCanonical()))
            .toList(),
        cluster.getCreatedAt(),
        cluster.getResolvedAt()
    );
  }
}
