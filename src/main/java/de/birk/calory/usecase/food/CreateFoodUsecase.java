package de.birk.calory.usecase.food;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.service.FoodService;
import de.birk.calory.service.builder.FoodBuilder;

@Component
public class CreateFoodUsecase {

  private final FoodService foodService;

  public CreateFoodUsecase(FoodService foodService) {
    this.foodService = foodService;
  }

  public FoodDetailsDto createFood(FoodDto foodDto) {
    Food food = this.foodService.createFood(
        FoodBuilder.convertFromFoodDto(foodDto.getName(), foodDto.getCalory()));
    return FoodDetailsDtoBuilder.convertDtoFromFood(food.getId(), food.getName(), food.getCalory());
  }
}
