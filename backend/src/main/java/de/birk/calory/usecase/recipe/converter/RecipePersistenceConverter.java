package de.birk.calory.usecase.recipe.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.converter.Converter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;


/**
 * Inheritor of the converter.
 *
 * @author Marius Birk
 */
public class RecipePersistenceConverter extends Converter<RecipePersistence, Recipe> {

  private final FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();

  /**
   * Inherits the super constructor and creates the conversion from dto to
   * entity and the other way around.
   */
  public RecipePersistenceConverter() {
    super(
            dto -> new Recipe(dto.getId(), dto.getName(), dto.getFoods().stream()
                    .map(foodPersistence ->
                            new Food(
                                    foodPersistence.getId(),
                                    foodPersistence.getName(),
                                    foodPersistence.getCalory(),
                                    foodPersistence.getGrams())
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
