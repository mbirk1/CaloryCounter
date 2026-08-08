package de.birk.calory.usecase.food;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.FoodInUseException;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Usecase for deleting and finding foods.
 *
 * @author Marius Birk
 */
@Component
public class DeleteFoodUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteFoodUsecase.class);

  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter persistenceConverter;
  private final FoodDtoConverter dtoConverter;
  private final FoodDetailsDtoConverter detailsDtoConverter;

  /**
   * Constructor for usecase.
   *
   * @param foodRepository Fooddata imaged through repository
   */
  public DeleteFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.persistenceConverter = new FoodPersistenceConverter();
    this.dtoConverter = new FoodDtoConverter();
    this.detailsDtoConverter = new FoodDetailsDtoConverter();
  }

  /**
   * Deleting Foods with given identifier.
   *
   * <p>{@code tab_calory_recipe_food.food_id} references this row without an {@code ON DELETE}
   * clause, so deleting a food item that is still part of a recipe violates that foreign key
   * constraint at the database level. The client already disables the delete action for such
   * items, but that check can be stale (e.g. the food was added to a recipe in another browser
   * tab a moment ago) - the resulting {@link DataIntegrityViolationException} is therefore
   * caught here and translated into a {@link FoodInUseException}, so the client gets a clear,
   * mappable 409 instead of an unhandled 500.
   *
   * @param id UUID and primary key of food item.
   * @return a list of remainig food items.
   */
  public List<FoodDetailsDto> deleteFood(UUID id) {
    LOG.debug("Loeschanfrage fuer Lebensmittel {} erhalten", id);
    Optional<FoodPersistence> food = this.foodRepository.findById(id);
    if (food.isPresent()) {
      try {
        this.foodRepository.delete(food.get());
        LOG.info("Lebensmittel {} ('{}') erfolgreich geloescht", id, food.get().getName());
      } catch (DataIntegrityViolationException e) {
        LOG.warn(
            "Lebensmittel {} ('{}') kann nicht geloescht werden, da es Teil eines Rezepts ist",
            id, food.get().getName());
        throw new FoodInUseException();
      }
    } else {
      LOG.warn("Loeschanfrage fuer nicht vorhandenes Lebensmittel {} - bereits geloescht?", id);
      throw new NoSuchElementException("Food not found");
    }
    List<FoodPersistence> allFoods = this.foodRepository.findAll();
    List<Food> foods = this.persistenceConverter.convertFromDtos(allFoods);

    return this.detailsDtoConverter.convertFromEntities(foods);
  }
}
