package de.birk.calory.adapter.primary.model;

import java.util.List;
import java.util.UUID;

import de.birk.calory.domain.food.Food;

public class RecipeDetailsDto {
    private UUID id;

    private String name;

    private List<Food> foods;

    public RecipeDetailsDto(UUID id, String name, List<Food> foods) {
        this.id = id;
        this.name = name;
        this.foods = foods;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Food> getFoods() {
        return foods;
    }
}
