package de.birk.calory.usecase.food.converter;

import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.converter.Converter;

public class FoodDtoConverter extends Converter<FoodDto, Food> {

  public FoodDtoConverter() {
    super(
        dto -> new Food(UUID.randomUUID(), dto.getName(), dto.getCalory(), dto.getGrams()),
        entity -> new FoodDto(entity.getName(), entity.getCalory(), entity.getGrams())
    );
  }
}
