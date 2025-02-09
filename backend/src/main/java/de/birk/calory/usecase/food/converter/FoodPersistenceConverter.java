package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * @author Marius Birk
 */
public class FoodPersistenceConverter extends Converter<FoodPersistence, Food> {

  /**
   * Converts from a FoodPersitence to a Food entity.
   */
  public FoodPersistenceConverter() {
    super(
        dto -> new Food(
                dto.getId(),
                dto.getName(),
                dto.getCalory(),
                dto.getGrams()
        ),
        entity -> new FoodPersistence(
                entity.getId(),
                entity.getName(),
                entity.getCalory(),
                entity.getGrams()
        )
    );
  }
}
