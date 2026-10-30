package de.birk.calory.usecase.food.duplicate;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.primary.model.DuplicateClusterDto;
import de.birk.calory.adapter.secondary.DuplicateClusterMemberRepository;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.RecipeFoodEntryRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;
import de.birk.calory.domain.food.duplicate.ClusterStatus;
import de.birk.calory.exception.ValidationException;

/**
 * Usecase that confirms a proposed duplicate cluster and merges its non-canonical members into
 * the chosen canonical food item (FR-5).
 *
 * @author Marius Birk
 */
@Component
public class MergeDuplicateClusterUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(MergeDuplicateClusterUsecase.class);

  private final DuplicateClusterRepository clusterRepository;
  private final DuplicateClusterMemberRepository memberRepository;
  private final RecipeFoodEntryRepository recipeFoodEntryRepository;
  private final FoodRepository foodRepository;
  private final DuplicateClusterPersistenceConverter persistenceConverter;
  private final DuplicateClusterDtoConverter dtoConverter;

  /**
   * Constructor for usecase.
   *
   * @param clusterRepository reads the cluster and resolves its status
   * @param memberRepository removes a merged-away food item's cluster memberships
   * @param recipeFoodEntryRepository repoints and, where conflicting, discards recipe references
   *     to a merged-away food item
   * @param foodRepository deletes the merged-away, non-canonical food items
   */
  public MergeDuplicateClusterUsecase(
      DuplicateClusterRepository clusterRepository,
      DuplicateClusterMemberRepository memberRepository,
      RecipeFoodEntryRepository recipeFoodEntryRepository,
      FoodRepository foodRepository) {
    this.clusterRepository = clusterRepository;
    this.memberRepository = memberRepository;
    this.recipeFoodEntryRepository = recipeFoodEntryRepository;
    this.foodRepository = foodRepository;
    this.persistenceConverter = new DuplicateClusterPersistenceConverter();
    this.dtoConverter = new DuplicateClusterDtoConverter();
  }

  /**
   * Merges every non-canonical member of a pending cluster into {@code canonicalFoodId}: recipe
   * references are repointed (or, on conflict, discarded), the non-canonical food items are
   * deleted, and the cluster is marked {@code CONFIRMED}.
   *
   * <p>Runs in a single transaction, per FR-5: if any step fails, the whole merge rolls back
   * rather than leaving recipes repointed but the old food items still present, or vice versa.
   *
   * @param clusterId the cluster to merge
   * @param canonicalFoodId which of the cluster's members to keep
   * @return the now-{@code CONFIRMED} cluster
   * @throws NoSuchElementException if no cluster with this id exists
   * @throws ValidationException if the cluster is not {@code PENDING}, or {@code canonicalFoodId}
   *     is not one of its members
   */
  @Transactional
  public DuplicateClusterDto merge(UUID clusterId, UUID canonicalFoodId) {
    LOG.debug(
        "Merge-Anfrage fuer Cluster {} mit kanonischem Lebensmittel {} erhalten",
        clusterId, canonicalFoodId);
    DuplicateClusterPersistence cluster = this.clusterRepository.findById(clusterId)
        .orElseThrow(() -> new NoSuchElementException("Duplicate cluster not found"));

    if (!ClusterStatus.PENDING.name().equals(cluster.getStatus())) {
      LOG.warn("Merge-Anfrage fuer bereits aufgeloesten Cluster {} abgelehnt", clusterId);
      throw new ValidationException();
    }

    List<UUID> memberFoodIds =
        cluster.getMembers().stream().map(member -> member.getFood().getId()).toList();
    if (!memberFoodIds.contains(canonicalFoodId)) {
      LOG.warn(
          "Merge-Anfrage fuer Cluster {} mit lebensmittelfremdem kanonischem Lebensmittel {} "
              + "abgelehnt",
          clusterId, canonicalFoodId);
      throw new ValidationException();
    }

    for (UUID foodId : memberFoodIds) {
      if (!foodId.equals(canonicalFoodId)) {
        mergeAway(foodId, canonicalFoodId);
      }
    }

    this.clusterRepository.updateStatus(clusterId, ClusterStatus.CONFIRMED.name(), Instant.now());
    LOG.info(
        "Cluster {} erfolgreich zusammengefuehrt, kanonisches Lebensmittel {}",
        clusterId, canonicalFoodId);

    DuplicateClusterPersistence updated = this.clusterRepository.findById(clusterId).orElseThrow();
    return this.dtoConverter.toDto(this.persistenceConverter.convertFromDto(updated));
  }

  // Every @Modifying(clearAutomatically = true) call below detaches the whole persistence
  // context immediately after running - including any not-yet-flushed em.remove() from
  // foodRepository.deleteById() lower in this same method. With more than one non-canonical
  // member, a cluster's second (and later) mergeAway() call would otherwise clear away the
  // previous iteration's still-pending food deletion before it ever reached the database,
  // silently leaving that food row behind. Flushing immediately after each deleteById() forces
  // it to hit the database there and then, before any later clearAutomatically query in this
  // same transaction gets a chance to discard it.
  private void mergeAway(UUID nonCanonicalFoodId, UUID canonicalFoodId) {
    this.recipeFoodEntryRepository.repointNonConflicting(nonCanonicalFoodId, canonicalFoodId);

    List<UUID> conflictingRecipeIds =
        this.recipeFoodEntryRepository.findRecipeIdsByFoodId(nonCanonicalFoodId);
    if (!conflictingRecipeIds.isEmpty()) {
      LOG.warn(
          "Merge-Konflikt: Rezepte {} referenzierten bereits sowohl {} als auch das kanonische "
              + "Lebensmittel {} - die veraltete Zeile fuer {} wird entfernt statt gemergt",
          conflictingRecipeIds, nonCanonicalFoodId, canonicalFoodId, nonCanonicalFoodId);
      this.recipeFoodEntryRepository.deleteByFoodId(nonCanonicalFoodId);
    }

    this.memberRepository.deleteByFoodId(nonCanonicalFoodId);
    this.foodRepository.deleteById(nonCanonicalFoodId);
    this.foodRepository.flush();
  }
}
