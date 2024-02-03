package de.birk.calory.usecase.food;

import de.birk.calory.adapter.primary.model.FoodDto;
import java.math.BigDecimal;
import java.util.UUID;

public class FoodDtoBuilder {

  public static FoodDto convertDtoFromFood(UUID uuid, String name, BigDecimal bigDecimal) {
    return new FoodDto(uuid, name, bigDecimal);
  }
}
