package de.birk.calory.usecase.food;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.food.converter.FoodDtoConverter;
import de.birk.calory.usecase.food.converter.converter.FoodPersistenceConverter;

@Component
public class FindFoodUsecase {
  private final FoodRepository foodRepository;

  public FindFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  public FoodDetailsDto findFoodById(UUID uuid) {
    FoodPersistence foodPersistence = this.foodRepository.findById(uuid)
        .orElseThrow();
    Food food = FoodPersistenceConverter.convertToFood(foodPersistence);
    return FoodDtoConverter.convertToDetails(
        food.getId(),
        food.getName(),
        food.getCalory()
    );
  }
}
