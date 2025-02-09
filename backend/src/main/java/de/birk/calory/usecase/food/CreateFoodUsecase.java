package de.birk.calory.usecase.food;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
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
   */
  public FoodDetailsDto createFood(FoodDto foodDto) {
    Food food = this.dtoConverter.convertFromDto(foodDto);
    FoodPersistence foodPersistence = this.persistenceConverter.convertFromEntity(food);
    this.foodRepository.save(foodPersistence);
    return this.detailsDtoConverter.convertFromEntity(food);
  }
}
