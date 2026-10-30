package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The detailed DTO for recipes, including the server-computed nutrition totals, per-serving
 * values, and diet - none of these are persisted, they are recomputed from the recipe's food
 * entries on every read.
 *
 * @author Marius Birk
 */
public class RecipeDetailsDto {
  private final UUID uuid;
  private final String name;
  private final int servings;
  private final List<RecipeFoodEntryDetailsDto> foods;
  private final BigDecimal totalCalories;
  private final BigDecimal totalFat;
  private final BigDecimal totalSaturatedFat;
  private final BigDecimal totalCarbohydrates;
  private final BigDecimal totalSugar;
  private final BigDecimal totalFiber;
  private final BigDecimal totalProtein;
  private final BigDecimal totalSalt;
  private final BigDecimal totalSodium;
  private final BigDecimal caloriesPerServing;
  private final BigDecimal fatPerServing;
  private final BigDecimal saturatedFatPerServing;
  private final BigDecimal carbohydratesPerServing;
  private final BigDecimal sugarPerServing;
  private final BigDecimal fiberPerServing;
  private final BigDecimal proteinPerServing;
  private final BigDecimal saltPerServing;
  private final BigDecimal sodiumPerServing;
  private final String diet;

  /**
   * The default constructor, that takes all needed properties.
   *
   * @param uuid the identifier
   * @param name the name
   * @param servings the number of servings this recipe yields
   * @param foods the food entries this recipe is made of, each with its amount
   * @param totalCalories the recipe's total calories
   * @param totalFat the recipe's total fat
   * @param totalSaturatedFat the recipe's total saturated fat
   * @param totalCarbohydrates the recipe's total carbohydrates
   * @param totalSugar the recipe's total sugar
   * @param totalFiber the recipe's total fiber
   * @param totalProtein the recipe's total protein
   * @param totalSalt the recipe's total salt
   * @param totalSodium the recipe's total sodium
   * @param caloriesPerServing calories per serving
   * @param fatPerServing fat per serving
   * @param saturatedFatPerServing saturated fat per serving
   * @param carbohydratesPerServing carbohydrates per serving
   * @param sugarPerServing sugar per serving
   * @param fiberPerServing fiber per serving
   * @param proteinPerServing protein per serving
   * @param saltPerServing salt per serving
   * @param sodiumPerServing sodium per serving
   * @param diet the name of the {@code Diet} enum constant this recipe is compatible with
   */
  public RecipeDetailsDto(
      UUID uuid,
      String name,
      int servings,
      List<RecipeFoodEntryDetailsDto> foods,
      BigDecimal totalCalories,
      BigDecimal totalFat,
      BigDecimal totalSaturatedFat,
      BigDecimal totalCarbohydrates,
      BigDecimal totalSugar,
      BigDecimal totalFiber,
      BigDecimal totalProtein,
      BigDecimal totalSalt,
      BigDecimal totalSodium,
      BigDecimal caloriesPerServing,
      BigDecimal fatPerServing,
      BigDecimal saturatedFatPerServing,
      BigDecimal carbohydratesPerServing,
      BigDecimal sugarPerServing,
      BigDecimal fiberPerServing,
      BigDecimal proteinPerServing,
      BigDecimal saltPerServing,
      BigDecimal sodiumPerServing,
      String diet) {
    this.uuid = uuid;
    this.name = name;
    this.servings = servings;
    this.foods = foods;
    this.totalCalories = totalCalories;
    this.totalFat = totalFat;
    this.totalSaturatedFat = totalSaturatedFat;
    this.totalCarbohydrates = totalCarbohydrates;
    this.totalSugar = totalSugar;
    this.totalFiber = totalFiber;
    this.totalProtein = totalProtein;
    this.totalSalt = totalSalt;
    this.totalSodium = totalSodium;
    this.caloriesPerServing = caloriesPerServing;
    this.fatPerServing = fatPerServing;
    this.saturatedFatPerServing = saturatedFatPerServing;
    this.carbohydratesPerServing = carbohydratesPerServing;
    this.sugarPerServing = sugarPerServing;
    this.fiberPerServing = fiberPerServing;
    this.proteinPerServing = proteinPerServing;
    this.saltPerServing = saltPerServing;
    this.sodiumPerServing = sodiumPerServing;
    this.diet = diet;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public int getServings() {
    return servings;
  }

  public List<RecipeFoodEntryDetailsDto> getFoods() {
    return foods;
  }

  public BigDecimal getTotalCalories() {
    return totalCalories;
  }

  public BigDecimal getTotalFat() {
    return totalFat;
  }

  public BigDecimal getTotalSaturatedFat() {
    return totalSaturatedFat;
  }

  public BigDecimal getTotalCarbohydrates() {
    return totalCarbohydrates;
  }

  public BigDecimal getTotalSugar() {
    return totalSugar;
  }

  public BigDecimal getTotalFiber() {
    return totalFiber;
  }

  public BigDecimal getTotalProtein() {
    return totalProtein;
  }

  public BigDecimal getTotalSalt() {
    return totalSalt;
  }

  public BigDecimal getTotalSodium() {
    return totalSodium;
  }

  public BigDecimal getCaloriesPerServing() {
    return caloriesPerServing;
  }

  public BigDecimal getFatPerServing() {
    return fatPerServing;
  }

  public BigDecimal getSaturatedFatPerServing() {
    return saturatedFatPerServing;
  }

  public BigDecimal getCarbohydratesPerServing() {
    return carbohydratesPerServing;
  }

  public BigDecimal getSugarPerServing() {
    return sugarPerServing;
  }

  public BigDecimal getFiberPerServing() {
    return fiberPerServing;
  }

  public BigDecimal getProteinPerServing() {
    return proteinPerServing;
  }

  public BigDecimal getSaltPerServing() {
    return saltPerServing;
  }

  public BigDecimal getSodiumPerServing() {
    return sodiumPerServing;
  }

  public String getDiet() {
    return diet;
  }
}
