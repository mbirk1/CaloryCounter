package de.birk.calory.controller.model;

import de.birk.calory.domain.food.Food;

import java.math.BigDecimal;
import java.util.UUID;

public class FoodDto {
    private UUID id;
    private String name;
    private BigDecimal calory;

    public FoodDto(UUID id, String name, BigDecimal calory) {
        this.id = id;
        this.name = name;
        this.calory = calory;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCalory() {
        return calory;
    }
}
