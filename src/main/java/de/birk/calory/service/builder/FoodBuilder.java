package de.birk.calory.service.builder;

import de.birk.calory.domain.food.Food;
import java.math.BigDecimal;
import java.util.UUID;

public class FoodBuilder {
  public static Food convertFromFood(UUID uuid, String name, BigDecimal bigDecimal) {
    return new Food(uuid, name, bigDecimal);
  }
}
