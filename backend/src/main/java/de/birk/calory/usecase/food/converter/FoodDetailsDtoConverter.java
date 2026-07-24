package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
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
        entity -> new FoodDetailsDto(
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
