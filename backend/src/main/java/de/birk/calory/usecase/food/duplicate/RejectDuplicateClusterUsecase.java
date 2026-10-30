package de.birk.calory.usecase.food.duplicate;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.exception.ValidationException;

/**
 * Usecase that rejects a proposed duplicate cluster as not actually being a duplicate (FR-6).
 * Rejected clusters are excluded from future analysis runs for the same source, by
 * {@link AnalyzeFoodDuplicatesUsecase}.
 *
 * @author Marius Birk
 */
@Component
public class RejectDuplicateClusterUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(RejectDuplicateClusterUsecase.class);

  private final DuplicateClusterRepository clusterRepository;
  private final DuplicateClusterPersistenceConverter persistenceConverter;
  private final DuplicateClusterDtoConverter dtoConverter;

  /**
   * Constructor for usecase.
   *
   * @param clusterRepository reads the cluster and resolves its status
   */
  public RejectDuplicateClusterUsecase(DuplicateClusterRepository clusterRepository) {
    this.clusterRepository = clusterRepository;
    this.persistenceConverter = new DuplicateClusterPersistenceConverter();
    this.dtoConverter = new DuplicateClusterDtoConverter();
  }

  /**
   * Marks a pending cluster as {@code REJECTED}. No food or recipe data is touched.
   *
   * @param clusterId the cluster to reject
   * @return the now-{@code REJECTED} cluster
   * @throws NoSuchElementException if no cluster with this id exists
   * @throws ValidationException if the cluster is not {@code PENDING}
   */
  @Transactional
  public DuplicateClusterDto reject(UUID clusterId) {
    LOG.debug("Ablehnung fuer Cluster {} angefragt", clusterId);
    DuplicateClusterPersistence cluster = this.clusterRepository.findById(clusterId)
        .orElseThrow(() -> new NoSuchElementException("Duplicate cluster not found"));

    if (!ClusterStatus.PENDING.name().equals(cluster.getStatus())) {
      LOG.warn("Ablehnung fuer bereits aufgeloesten Cluster {} abgelehnt", clusterId);
      throw new ValidationException();
    }

    this.clusterRepository.updateStatus(clusterId, ClusterStatus.REJECTED.name(), Instant.now());
    LOG.info("Cluster {} als kein Duplikat abgelehnt", clusterId);

    DuplicateClusterPersistence updated = this.clusterRepository.findById(clusterId).orElseThrow();
    return this.dtoConverter.toDto(this.persistenceConverter.convertFromDto(updated));
  }
}
