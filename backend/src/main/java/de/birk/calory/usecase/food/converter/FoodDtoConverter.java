package de.birk.calory.usecase.food.converter;

import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * @author Marius Birk
 */
public class FoodDtoConverter extends Converter<FoodDto, Food> {

  /**
   * Converts a FoodDto to a Food Entity.
   */
  public FoodDtoConverter() {
    super(
        dto -> new Food(UUID.randomUUID(), dto.getName(), dto.getCalory(), dto.getGrams()),
        entity -> new FoodDto(entity.getName(), entity.getCalory(), entity.getGrams())
    );
  }
}
