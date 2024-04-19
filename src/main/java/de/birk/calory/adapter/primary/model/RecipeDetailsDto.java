package de.birk.calory.adapter.primary.model;

import java.util.List;
import java.util.UUID;

public class RecipeDetailsDto {
    private UUID id;

    private String name;

    private List<FoodDetailsDto> foods;

    public RecipeDetailsDto(UUID id, String name, List<FoodDetailsDto> foods) {
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

    public List<FoodDetailsDto> getFoods() {
        return foods;
    }
}
