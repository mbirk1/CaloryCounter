package de.birk.calory.usecase.food;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.FoodInUseException;

/**
 * Usecase for deleting a food item.
 *
 * @author Marius Birk
 */
@Component
public class DeleteFoodUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteFoodUsecase.class);

  private final FoodRepository foodRepository;

  /**
   * Constructor for usecase.
   *
   * @param foodRepository Fooddata imaged through repository
   */
  public DeleteFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  /**
   * Deletes the food item with the given identifier.
   *
   * <p>{@code tab_calory_recipe_food.food_id} references this row without an {@code ON DELETE}
   * clause, so deleting a food item that is still part of a recipe violates that foreign key
   * constraint at the database level. The client already disables the delete action for such
   * items, but that check can be stale (e.g. the food was added to a recipe in another browser
   * tab a moment ago) - the resulting {@link DataIntegrityViolationException} is therefore
   * caught here and translated into a {@link FoodInUseException}, so the client gets a clear,
   * mappable 409 instead of an unhandled 500.
   *
   * <p>Deliberately does not return the remaining food items: this used to load and convert
   * every single row in the table via {@code foodRepository.findAll()} after every delete - on
   * a table with real, bulk-imported data (hundreds of thousands to millions of rows) that
   * turns an O(1) primary-key delete into an O(n) operation, several seconds long, for a return
   * value no caller ever read (the client reloads the food table's current, paginated page
   * itself instead).
   *
   * @param id UUID and primary key of food item.
   */
  public void deleteFood(UUID id) {
    LOG.debug("Loeschanfrage fuer Lebensmittel {} erhalten", id);
    Optional<FoodPersistence> food = this.foodRepository.findById(id);
    if (food.isEmpty()) {
      LOG.warn("Loeschanfrage fuer nicht vorhandenes Lebensmittel {} - bereits geloescht?", id);
      throw new NoSuchElementException("Food not found");
    }
    try {
      this.foodRepository.delete(food.get());
      LOG.info("Lebensmittel {} ('{}') erfolgreich geloescht", id, food.get().getName());
    } catch (DataIntegrityViolationException e) {
      LOG.warn(
          "Lebensmittel {} ('{}') kann nicht geloescht werden, da es Teil eines Rezepts ist",
          id, food.get().getName());
      throw new FoodInUseException();
    }
  }
}
