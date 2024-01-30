package de.birk.calory.domain.food;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tab_calory_recipe")
public class Recipe {

    @Id
    private UUID uuid;

    private List<Food> foodList;

    public Recipe(UUID uuid, List<Food> foodList) {
        this.uuid = uuid;
        this.foodList = foodList;
    }

    public Recipe(List<Food> foodList) {
        this.foodList = foodList;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Food> getFoodList() {
        return foodList;
    }
}
