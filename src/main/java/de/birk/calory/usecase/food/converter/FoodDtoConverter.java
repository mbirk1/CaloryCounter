package de.birk.calory.usecase.food.converter;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;

public class FoodDtoConverter {

  public static Food convertToEntity(FoodDto food) {
    validateDto(food);
    UUID id = UUID.randomUUID();
    return new Food(id, food.getName(), food.getCalory());
  }

  public static FoodDetailsDto convertToDetails(UUID id, String name, BigDecimal calory) {
    return new FoodDetailsDto(
        id,
        name,
        calory
    );
  }

  public static FoodDto convertToDto(String name, BigDecimal calory) {
    return new FoodDto(
        name,
        calory
    );
  }
  private static void validateDto(FoodDto foodDto){
    if(foodDto.getName().trim().isBlank() || foodDto.getName().trim().isEmpty() || foodDto.getName() == null){
      throw new IllegalArgumentException();
    }

    if(foodDto.getCalory() == null){
      throw new IllegalArgumentException();
    }
  }
}
