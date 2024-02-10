package de.birk.calory.usecase.food;

import static de.birk.calory.usecase.food.FoodDetailsDtoBuilder.convertDtoFromFood;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.service.FoodService;
import de.birk.calory.service.exceptions.FoodNotExistException;

@Component
public class FindFoodUsecase {

  private final FoodService foodService;

  public FindFoodUsecase(FoodService foodService) {
    this.foodService = foodService;
  }

  public FoodDetailsDto findFoodById(UUID uuid) {
    Food food = foodService.findFood(uuid);
    return convertDtoFromFood(food.getId(), food.getName(), food.getCalory());
  }
}
