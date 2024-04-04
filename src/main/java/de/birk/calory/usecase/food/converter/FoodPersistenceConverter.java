package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;

public class FoodPersistenceConverter extends Converter<FoodPersistence, Food> {

    public FoodPersistenceConverter() {
        super(
            dto -> new Food(dto.getId(), dto.getName(), dto.getCalory()),
            entity -> new FoodPersistence(entity.getId(), entity.getName(), entity.getCalory())
        );
    }
}
