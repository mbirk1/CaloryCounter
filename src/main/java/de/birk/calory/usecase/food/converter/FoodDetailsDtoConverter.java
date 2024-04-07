package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;

import java.util.UUID;

public class FoodDetailsDtoConverter extends Converter<FoodDetailsDto, Food> {
    public FoodDetailsDtoConverter() {
        super(
            dto -> new Food(dto.getName(), dto.getCalory(), dto.getGrams()),
            entity -> new FoodDetailsDto(entity.getId(), entity.getName(), entity.getCalory(), entity.getGrams())
        );
    }
}
