package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.converter.Converter;

/**
 * An inheritor of the converter class. Converts a DTO to an entity.
 *
 * @author Marius Birk
 */
public class FoodDetailsDtoConverter extends Converter<FoodDetailsDto, Food> {

  /**
   * Basic Constructor.
   */
  public FoodDetailsDtoConverter() {
    super(
        dto -> new Food(
            dto.getUuid(),
            dto.getName(),
            dto.getCalory(),
            dto.getGrams()
        ),
        entity -> new FoodDetailsDto(
            entity.getId(),
            entity.getName(),
            entity.getCalory(),
            entity.getGrams()
        )
    );
  }
}
