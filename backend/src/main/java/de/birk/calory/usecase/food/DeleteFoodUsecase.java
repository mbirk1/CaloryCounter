package de.birk.calory.usecase.food;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
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
   * @param id UUID and primary key of food item.
   * @return a list of remainig food items.
   */
  public List<FoodDetailsDto> deleteFood(UUID id) {
    Optional<FoodPersistence> food = this.foodRepository.findById(id);
    if (food.isPresent()) {
      this.foodRepository.delete(food.get());
    } else {
      throw new NoSuchElementException("Food not found");
    }
    List<FoodPersistence> allFoods = this.foodRepository.findAll();
    List<Food> foods = this.persistenceConverter.convertFromDtos(allFoods);

    return this.detailsDtoConverter.convertFromEntities(foods);
  }
}
