package de.birk.calory.usecase.food.converter;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;

public class FoodDtoConverter extends Converter<FoodDto, Food>{

  public FoodDtoConverter() {
    super(
        dto -> new Food(UUID.randomUUID(), dto.getName(), dto.getCalory(), dto.getGrams()),
        entity -> new FoodDto(entity.getName(), entity.getCalory(), entity.getGrams())
    );
  }
}
