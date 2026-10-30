package de.birk.calory.usecase.food;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * This Usecase creates new Food Items.
 *
 * <p>It is desired to only create Food Entities and return them.
 *
 * @author Marius Birk
 */

@Component
public class CreateFoodUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(CreateFoodUsecase.class);

  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter persistenceConverter;
  private final FoodDtoConverter dtoConverter;
  private final FoodDetailsDtoConverter detailsDtoConverter;

  /**
   * Basic Constructor takes a Repository and automatically creates new converters.
   *
   * @author Marius Birk
   * @param foodRepository is used to store food entities
   */
  public CreateFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.persistenceConverter = new FoodPersistenceConverter();
    this.dtoConverter = new FoodDtoConverter();
    this.detailsDtoConverter = new FoodDetailsDtoConverter();
  }

  /**
   * Receives a FoodDto and persists it to the database.
   * If the saving was successfully it returns a DetailsDto with all corresponding properties.
   *
   * @param foodDto new Food Item with all necessary properties
   *
   * @return the new food item with a generated uuid
   *
   * @throws de.birk.calory.exception.ValidationException if a core field is missing, or calory
   *     or a macronutrient falls outside its plausible range
   */
  public FoodDetailsDto createFood(FoodDto foodDto) {
    LOG.debug(
        "Anlegeanfrage fuer Lebensmittel erhalten: name='{}', calory={}, grams={}",
        foodDto.getName(), foodDto.getCalory(), foodDto.getGrams());
    Food food = this.dtoConverter.convertFromDto(foodDto);
    try {
      food.validatePlausibility();
    } catch (ValidationException e) {
      LOG.warn(
          "Anlegen von Lebensmittel '{}' abgelehnt: unplausibler Wert (calory={})",
          foodDto.getName(), foodDto.getCalory());
      throw e;
    }

    FoodPersistence saved = this.foodRepository.save(
        this.persistenceConverter.convertFromEntity(food));
    Food savedFood = this.persistenceConverter.convertFromDto(saved);
    LOG.info("Lebensmittel {} ('{}') erfolgreich angelegt", savedFood.getId(), savedFood.getName());

    return this.detailsDtoConverter.convertFromEntity(savedFood);
  }
}
