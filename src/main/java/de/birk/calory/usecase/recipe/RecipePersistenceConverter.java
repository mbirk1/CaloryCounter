package de.birk.calory.usecase.recipe;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.converter.Converter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;


public class RecipePersistenceConverter extends Converter<RecipePersistence, Recipe> {

  private final FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();

  public RecipePersistenceConverter() {
    super(
        dto -> new Recipe(dto.getId(), dto.getName(), dto.getFoods().stream()
            .map(foodPersistence ->

                new Food(
                foodPersistence.getId(),
                foodPersistence.getName(),
                foodPersistence.getCalory(),
                foodPersistence.getCalory())
            ).toList()
        ),
        entity -> new RecipePersistence(
            entity.getId(), entity.getName(), entity.getFoods().stream()
            .map(food -> new FoodPersistence(
                food.getId(),
                food.getName(),
                food.getCalory(),
                food.getGrams()
            )).toList()
        )
    );
  }
}
