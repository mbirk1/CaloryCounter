package de.birk.calory.adapter.primary.model;

import java.util.List;

/**
 * Despict the dto for recipes.
 *
 * @author Marius Birk
 */
public class RecipeDto {
  private String name;
  private int servings;
  private List<RecipeFoodEntryDto> foods;

  public RecipeDto() {
  }

  /**
   * Basic constructor.
   *
   * @param name the name
   * @param servings the number of servings this recipe yields
   * @param foods the food entries (by id, with amount) this recipe is made of
   */
  public RecipeDto(String name, int servings, List<RecipeFoodEntryDto> foods) {
    this.name = name;
    this.servings = servings;
    this.foods = foods;
  }

  public String getName() {
    return name;
  }

  public int getServings() {
    return servings;
  }

  public List<RecipeFoodEntryDto> getFoods() {
    return foods;
  }
}
