package de.birk.calory.adapter.secondary;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.birk.calory.adapter.secondary.model.RecipeFoodEntryId;
import de.birk.calory.adapter.secondary.model.RecipeFoodEntryPersistence;

/**
 * Repository for {@code tab_calory_recipe_food} rows, used directly (rather than only through
 * {@code RecipePersistence#foodEntries}) by the duplicate-merge usecase (FR-5), which repoints
 * recipe references from a merged-away food item to its canonical replacement in bulk, without
 * loading every affected recipe into memory.
 *
 * @author Marius Birk
 */
@Repository
public interface RecipeFoodEntryRepository
    extends JpaRepository<RecipeFoodEntryPersistence, RecipeFoodEntryId> {

  /**
   * Repoints every recipe-food row that references {@code fromFoodId} onto {@code toFoodId},
   * except where the owning recipe already references {@code toFoodId} directly - repointing
   * those too would violate the {@code (recipe_id, food_id)} composite primary key. Rows left
   * unrepointed by this call are exactly the conflicting ones described in FR-5.2.
   *
   * @param fromFoodId the merged-away, non-canonical food item
   * @param toFoodId the canonical food item to repoint onto
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query(value = "update tab_calory_recipe_food set food_id = :toFoodId "
      + "where food_id = :fromFoodId and recipe_id not in "
      + "(select recipe_id from tab_calory_recipe_food where food_id = :toFoodId)",
      nativeQuery = true)
  void repointNonConflicting(
      @Param("fromFoodId") UUID fromFoodId, @Param("toFoodId") UUID toFoodId);

  /**
   * Finds the recipes that still reference {@code foodId} - used to log which recipes hit the
   * FR-5.2 merge conflict (both the canonical and a non-canonical member were already in the same
   * recipe) before the conflicting row is discarded.
   *
   * @param foodId the food item to look up remaining references for
   * @return the ids of recipes still referencing it
   */
  @Query("select e.recipe.id from RecipeFoodEntryPersistence e where e.food.id = :foodId")
  List<UUID> findRecipeIdsByFoodId(@Param("foodId") UUID foodId);

  /**
   * Deletes every remaining recipe-food row referencing {@code foodId} - used after
   * {@link #repointNonConflicting} to discard the FR-5.2 conflict rows before the now-orphaned
   * food item itself is deleted.
   *
   * @param foodId the food item whose remaining recipe references should be discarded
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("delete from RecipeFoodEntryPersistence e where e.food.id = :foodId")
  void deleteByFoodId(@Param("foodId") UUID foodId);
}
