package de.birk.calory.usecase.recipe.converter;

import de.birk.calory.adapter.secondary.model.RecipeFoodEntryPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.domain.recipe.RecipeFoodEntry;
import de.birk.calory.usecase.converter.Converter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Inheritor of the converter. Converts between {@link Recipe} and its JPA persistence entity,
 * delegating the Food/FoodPersistence side of each entry to the existing
 * {@link FoodPersistenceConverter} so a food item's full data (brand, macros, diet, image, ...)
 * survives the roundtrip instead of only its core {@code id/name/calory/grams}.
 *
 * @author Marius Birk
 */
public class RecipePersistenceConverter extends Converter<RecipePersistence, Recipe> {

  /**
   * Inherits the super constructor and creates the conversion from dto to
   * entity and the other way around.
   */
  public RecipePersistenceConverter() {
    super(
        dto -> {
          FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();
          return new Recipe(
              dto.getId(),
              dto.getName(),
              dto.getFoodEntries().stream()
                  .map(entry -> new RecipeFoodEntry(
                      foodPersistenceConverter.convertFromDto(entry.getFood()),
                      entry.getAmountGrams()
                  ))
                  .toList(),
              dto.getServings()
          );
        },
        entity -> {
          FoodPersistenceConverter foodPersistenceConverter = new FoodPersistenceConverter();
          RecipePersistence persistence =
              new RecipePersistence(entity.getId(), entity.getName(), entity.getServings());
          persistence.setFoodEntries(entity.getEntries().stream()
              .map(entry -> new RecipeFoodEntryPersistence(
                  persistence,
                  foodPersistenceConverter.convertFromEntity(entry.getFood()),
                  entry.getAmountGrams()
              ))
              .toList());
          return persistence;
        }
    );
  }
}
