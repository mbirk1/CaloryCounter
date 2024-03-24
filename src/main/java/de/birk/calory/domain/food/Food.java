package de.birk.calory.domain.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDto;

public class Food {

    private UUID id;
    private String name;
    private BigDecimal calory;

    private Food() {
        //for JPA
    }

    public Food(UUID id, String name, BigDecimal calory) {
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
