package de.birk.calory.service.builder;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.domain.food.Food;

public class FoodBuilder {
  public static Food convertFromFoodDto(String name, BigDecimal bigDecimal) {
    return new Food(UUID.randomUUID(), name, bigDecimal);
  }
}
