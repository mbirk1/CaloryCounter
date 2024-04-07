package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.converter.Converter;

public class FoodPersistenceConverter extends Converter<FoodPersistence, Food> {

    public FoodPersistenceConverter() {
        super(
            dto -> new Food(dto.getId(), dto.getName(), dto.getCalory(), dto.getGrams()),
            entity -> new FoodPersistence(entity.getId(), entity.getName(), entity.getCalory(), entity.getGrams())
        );
    }
}
