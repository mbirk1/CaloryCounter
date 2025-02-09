package de.birk.calory.usecase.food;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Usecase to find food items in the repository.
 *
 * @author Marius Birk
 */
@Service
public class FindFoodUsecase {
  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter persistenceConverter;
  private final FoodDetailsDtoConverter detailsDtoConverter;

  /**
   * Simple constructor that takes a repository and creates corresponding converters.
   *
   * @param foodRepository the food repository that connects to the database
   *
   */
  public FindFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.persistenceConverter = new FoodPersistenceConverter();
    this.detailsDtoConverter = new FoodDetailsDtoConverter();
  }

  /**
   * Finds all food items in the database.
   *
   * @return all food items as a list
   */
  public List<FoodDetailsDto> findAllFoods() {
    List<FoodPersistence> foodPersistence = this.foodRepository.findAll();

    List<Food> foods = this.persistenceConverter.convertFromDtos(foodPersistence);
    return this.detailsDtoConverter.convertFromEntities(foods);
  }

  /**
   * Finds a specific foot item with its uuid.
   *
   * @param uuid the identifier for the food item
   *
   * @return the food item
   */
  public FoodDetailsDto findFoodById(UUID uuid) {
    FoodPersistence foodPersistence = this.foodRepository.findById(uuid)
        .orElseThrow();

    Food food = this.persistenceConverter.convertFromDto(foodPersistence);
    return this.detailsDtoConverter.convertFromEntity(food);
  }
}
