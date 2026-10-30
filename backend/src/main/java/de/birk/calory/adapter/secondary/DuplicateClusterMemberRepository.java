package de.birk.calory.adapter.secondary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberId;
import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;

/**
 * Repository for {@code tab_calory_food_duplicate_cluster_member} rows, used directly (rather
 * than only through {@code DuplicateClusterPersistence#members}) by the merge usecase (FR-5) to
 * remove a food item's cluster memberships - in the cluster being merged and any other cluster it
 * happens to also appear in - before the food item itself is deleted, since
 * {@code tab_calory_food_duplicate_cluster_member.food_id} has no {@code ON DELETE} clause.
 *
 * @author Marius Birk
 */
@Repository
public interface DuplicateClusterMemberRepository
    extends JpaRepository<DuplicateClusterMemberPersistence, DuplicateClusterMemberId> {

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("delete from DuplicateClusterMemberPersistence m where m.food.id = :foodId")
  void deleteByFoodId(@Param("foodId") UUID foodId);
}
