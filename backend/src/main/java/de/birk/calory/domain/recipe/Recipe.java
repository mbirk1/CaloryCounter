package de.birk.calory.domain.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

/**
 * Entity that depicts a recipe.
 *
 * @author Marius Birk
 */
public class Recipe extends AbstractEntity<UUID> {

  private UUID id;

  private final String name;
  private final List<Food> foods;

  /**
   * Default constructor with zero/empty values.
   */
  public Recipe() {
    this.id = UUID.randomUUID();
    this.name = "";
    this.foods = new ArrayList<>();
  }

  /**
   * Constructor to create a basic recipe.
   *
   * @param name the name
   * @param foods a list of food items
   */
  public Recipe(String name, List<Food> foods) {
    this.name = name;
    this.foods = foods;
    validate();
  }

  /**
   * Constructor for a saved recipe.
   *
   * @param id the identifier
   * @param name the name
   * @param foods a list of food items
   */
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
    if (this.name == null) {
      throw new ValidationException();
    }
    if (this.name.isEmpty()) {
      throw new ValidationException();
    }
    if (this.name.isBlank()) {
      throw new ValidationException();
    }
    if (this.foods == null) {
      throw new ValidationException();
    }
  }
}
