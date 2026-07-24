package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
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
            dto.getGrams(),
            dto.getBrand(),
            dto.getCategory(),
            dto.getFat(),
            dto.getSaturatedFat(),
            dto.getCarbohydrates(),
            dto.getSugar(),
            dto.getFiber(),
            dto.getProtein(),
            dto.getSalt(),
            dto.getSodium(),
            dto.getImageUrl(),
            dto.getSource() == null ? FoodSource.MANUAL : FoodSource.valueOf(dto.getSource()),
            dto.getExternalId()
        ),
        entity -> new FoodPersistence(
            entity.getId(),
            entity.getName(),
            entity.getCalory(),
            entity.getGrams(),
            entity.getBrand(),
            entity.getCategory(),
            entity.getFat(),
            entity.getSaturatedFat(),
            entity.getCarbohydrates(),
            entity.getSugar(),
            entity.getFiber(),
            entity.getProtein(),
            entity.getSalt(),
            entity.getSodium(),
            entity.getImageUrl(),
            entity.getSource() == null ? null : entity.getSource().name(),
            entity.getExternalId()
        )
    );
  }
}
