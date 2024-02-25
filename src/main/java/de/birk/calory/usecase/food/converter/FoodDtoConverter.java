package de.birk.calory.usecase.food.converter;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;

public class FoodDtoConverter {

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
}
