package de.birk.calory.adapter.secondary.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class RecipePersistence {

    @Id
    @Column
    private UUID id;

    @Column
    private String name;

    @ManyToMany
    @JoinTable(
        name = "tab_calory_food",
        joinColumns = @JoinColumn(name = "id"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<FoodPersistence> foods;

    public RecipePersistence(UUID id, String name, List<FoodPersistence> foods) {
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

    public List<FoodPersistence> getFoods() {
        return foods;
    }
}
