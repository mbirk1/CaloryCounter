package de.birk.calory.usecase.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;

public class FoodDetailsDtoBuilder {

  public static FoodDetailsDto convertDtoFromFood(UUID uuid, String name, BigDecimal bigDecimal) {
    return new FoodDetailsDto(uuid, name, bigDecimal);
  }
}
