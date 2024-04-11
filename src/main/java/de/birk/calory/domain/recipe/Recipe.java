package de.birk.calory.domain.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

public class Recipe extends AbstractEntity<UUID> {

  private UUID id;

  private final String name;
  private final List<Food> foods;

  public Recipe() {
    this.id = UUID.randomUUID();
    this.name = "";
    this.foods = new ArrayList<>();
    validate();
  }

  public Recipe(String name, List<Food> foods) {
    this.name = name;
    this.foods = foods;
    validate();
  }

  public Recipe(UUID id, String name, List<Food> foods) {
    this.id = id;
    this.name = name;
    this.foods = foods;
    validate();
  }

  public UUID getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

  public List<Food> getFoods() {
    return foods;
  }

  @Override
  protected void validate() throws ValidationException {
    if (this.name == null || this.name.isEmpty() || this.name.isBlank()) {
      throw new ValidationException();
    }
    if (this.foods == null) {
      throw new ValidationException();
    }
  }
}
