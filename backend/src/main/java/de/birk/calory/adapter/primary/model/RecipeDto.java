package de.birk.calory.adapter.primary.model;

import java.util.List;

public class RecipeDto {
    private String name;
    private List<FoodDetailsDto> foods;

    public RecipeDto(String name, List<FoodDetailsDto> foods) {
        this.name = name;
        this.foods = foods;
    }

    public String getName() {
        return name;
    }

    public List<FoodDetailsDto> getFoods() {
        return foods;
    }
}
