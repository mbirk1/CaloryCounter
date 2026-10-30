package de.birk.calory.adapter.secondary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;

/**
 * Basic Spring Boot Repository.
 *
 * @author Marius Birk
 */
@Repository
public interface DuplicateClusterRepository
    extends JpaRepository<DuplicateClusterPersistence, UUID> {

  Page<DuplicateClusterPersistence> findByStatus(String status, Pageable pageable);

  /**
   * Finds every cluster previously proposed for a source in a given status - used by FR-2's
   * re-analysis exclusion, which compares a freshly proposed cluster's food-id combination
   * against every existing {@code REJECTED} cluster's, so a rejected suggestion never reappears.
   *
   * <p>Explicitly {@code join fetch}es {@code members} (and each member's {@code food}) rather
   * than relying on the default lazy {@code @OneToMany}: the only caller runs on the
   * {@code @Async} analysis thread, which has no HTTP request (and therefore none of the
   * Open-Session-In-View session that keeps lazy proxies resolvable for every other,
   * request-driven caller in this feature) - accessing {@code member.getFood()} after this
   * query returns would otherwise throw {@code LazyInitializationException}.
   *
   * @param source the name of the {@code FoodSource} enum constant to look up
   * @param status the name of the {@code ClusterStatus} enum constant to filter by
   * @return every matching cluster, each with its members and their food eagerly loaded
   */
  @Query("select distinct c from DuplicateClusterPersistence c "
      + "left join fetch c.members m "
      + "left join fetch m.food "
      + "where c.source = :source and c.status = :status")
  List<DuplicateClusterPersistence> findBySourceAndStatus(
      @Param("source") String source, @Param("status") String status);

  /**
   * Resolves a cluster by bulk-updating just its {@code status}/{@code resolvedAt} columns,
   * rather than mutating and re-saving an already-loaded entity. Used by the merge (FR-5) and
   * reject (FR-6) usecases specifically because the merge path clears the persistence context
   * partway through (its {@code @Modifying} member/recipe-repointing queries do, to keep
   * Hibernate's first-level cache from going stale) - re-saving a since-detached cluster entity
   * whose in-memory {@code members} collection no longer matches the database would risk Hibernate
   * trying to reconcile (and possibly re-insert) already-deleted member rows on merge.
   *
   * @param id the cluster to resolve
   * @param status the name of the {@code ClusterStatus} enum constant to set
   * @param resolvedAt when the cluster was resolved
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("update DuplicateClusterPersistence c set c.status = :status, c.resolvedAt = :resolvedAt "
      + "where c.id = :id")
  void updateStatus(
      @Param("id") UUID id,
      @Param("status") String status,
      @Param("resolvedAt") Instant resolvedAt);
}
