package de.birk.calory.adapter.primary.model;

import java.util.List;

/**
 * Despict the dto for recipes.
 *
 * @author Marius Birk
 */
public class RecipeDto {
  private String name;
  private List<FoodDetailsDto> foods;

  public RecipeDto() {
  }

  /**
   * Basic constructor, with only a name and a list of foods.
   *
   * @param name the name
   * @param foods a list of foods
   */
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
