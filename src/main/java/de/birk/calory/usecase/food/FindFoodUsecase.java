package de.birk.calory.usecase.food;

import static de.birk.calory.usecase.food.FoodDtoBuilder.convertDtoFromFood;

import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.service.FoodService;
import de.birk.calory.service.exceptions.FoodNotExistException;
import java.math.BigDecimal;
import java.util.UUID;

public class FindFoodUsecase {

  private final FoodService foodService;

  public FindFoodUsecase(FoodService foodService) {
    this.foodService = foodService;
  }

  public FoodDto findFoodById(UUID uuid) {
    Food food = null;
    try {
      food = foodService.findFood(uuid);
      return convertDtoFromFood(food.getId(), food.getName(), food.getCalory());
    } catch (FoodNotExistException e) {
      return new FoodDto(UUID.randomUUID(), "", BigDecimal.ZERO);
    }
  }
}
