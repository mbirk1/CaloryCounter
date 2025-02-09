package de.birk.calory.adapter.primary.model;

import java.util.List;
import java.util.UUID;

/**
 * The detailed DTO for recipes.
 *
 * @author Marius Birk
 */
public class RecipeDetailsDto {
  private final UUID id;
  private final String name;
  private final List<FoodDetailsDto> foods;

  /**
   * The default constructor, that takes all needed properties.
   *
   * @param id the identifier
   * @param name the name
   * @param foods a list of food items
   */
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
