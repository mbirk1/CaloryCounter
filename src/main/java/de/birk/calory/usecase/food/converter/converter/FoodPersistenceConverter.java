package de.birk.calory.usecase.food.converter.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;

public class FoodPersistenceConverter {

  public static Food convertToFood(FoodPersistence foodPersistence) {
    return new Food(
        foodPersistence.getId(),
        foodPersistence.getName(),
        foodPersistence.getCalory()
    );
  }
}
