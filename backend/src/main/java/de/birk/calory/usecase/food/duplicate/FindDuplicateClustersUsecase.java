package de.birk.calory.usecase.food.duplicate;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.domain.food.duplicate.DuplicateCluster;
import de.birk.calory.exception.InvalidSortParameterException;

/**
 * Usecase to find proposed duplicate clusters for review (FR-4).
 *
 * @author Marius Birk
 */
@Service
public class FindDuplicateClustersUsecase {

  private final DuplicateClusterRepository clusterRepository;
  private final DuplicateClusterPersistenceConverter persistenceConverter;
  private final DuplicateClusterDtoConverter dtoConverter;

  /**
   * Constructor for usecase.
   *
   * @param clusterRepository reads proposed clusters
   */
  public FindDuplicateClustersUsecase(DuplicateClusterRepository clusterRepository) {
    this.clusterRepository = clusterRepository;
    this.persistenceConverter = new DuplicateClusterPersistenceConverter();
    this.dtoConverter = new DuplicateClusterDtoConverter();
  }

  /**
   * Finds a page of clusters with the given status, newest first.
   *
   * @param status the name of the {@code ClusterStatus} enum constant to filter by
   * @param page the zero-based page index
   * @param size the page size
   * @return the requested page of clusters
   * @throws InvalidSortParameterException if {@code status} is not a valid {@code ClusterStatus}
   */
  public PageResponseDto<DuplicateClusterDto> findByStatus(String status, int page, int size) {
    String normalizedStatus = normalizeStatus(status);
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<DuplicateClusterPersistence> result =
        this.clusterRepository.findByStatus(normalizedStatus, pageable);

    List<DuplicateCluster> clusters =
        this.persistenceConverter.convertFromDtos(result.getContent());
    List<DuplicateClusterDto> content = clusters.stream().map(this.dtoConverter::toDto).toList();

    return new PageResponseDto<>(
        content, result.getNumber(), result.getSize(), result.getTotalElements(),
        result.getTotalPages(), result.isLast());
  }

  private String normalizeStatus(String status) {
    try {
      return ClusterStatus.valueOf(status).name();
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidSortParameterException(
          "Invalid status '" + status + "', allowed values are "
              + List.of(ClusterStatus.values()));
    }
  }
}
