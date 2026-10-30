package de.birk.calory.usecase.food.duplicate;

import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;
import de.birk.calory.usecase.converter.Converter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Converts between {@link DuplicateCluster} and its JPA persistence entity, delegating the
 * Food/FoodPersistence side of each member to the existing {@link FoodPersistenceConverter} -
 * same reasoning as {@code RecipePersistenceConverter}.
 *
 * @author Marius Birk
 */
public class DuplicateClusterPersistenceConverter
    extends Converter<DuplicateClusterPersistence, DuplicateCluster> {

  /**
   * Inherits the super constructor and creates the conversion from persistence entity to domain
   * entity and the other way around.
   */
  public DuplicateClusterPersistenceConverter() {
    super(
        dto -> {
          FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();
          return new DuplicateCluster(
              dto.getId(),
              FoodSource.valueOf(dto.getSource()),
              ClusterStatus.valueOf(dto.getStatus()),
              dto.getMembers().stream()
                  .map(member -> new DuplicateClusterMember(
                      foodPersistenceConverter.convertFromDto(member.getFood()),
                      member.getSimilarityScore(),
                      member.isSuggestedCanonical()))
                  .toList(),
              dto.getCreatedAt(),
              dto.getResolvedAt()
          );
        },
        entity -> {
          FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();
          DuplicateClusterPersistence persistence = new DuplicateClusterPersistence(
              entity.getId(),
              entity.getSource().name(),
              entity.getStatus().name(),
              entity.getCreatedAt(),
              entity.getResolvedAt());
          persistence.setMembers(entity.getMembers().stream()
              .map(member -> new DuplicateClusterMemberPersistence(
                  persistence,
                  foodPersistenceConverter.convertFromEntity(member.getFood()),
                  member.getSimilarityScore(),
                  member.isSuggestedCanonical()))
              .toList());
          return persistence;
        }
    );
  }
}
