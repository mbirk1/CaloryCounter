package de.birk.calory.domain.recipe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

/**
 * Entity that depicts a recipe: a name, a number of servings, and the food items (each with an
 * amount used) it is made of. Nutrition totals, per-serving values, and the recipe's diet are
 * deliberately not stored - they are always computed fresh from the current food entries, so
 * they can never go stale if a referenced food's own nutrition data changes later.
 *
 * @author Marius Birk
 */
public class Recipe extends AbstractEntity<UUID> {

  /**
   * Scale used for the intermediate amount/food-grams ratio, before it is multiplied into a
   * nutrition value - needs to be wide enough that dividing two arbitrary {@code numeric(6,2)}
   * amounts never throws {@link ArithmeticException} for a non-terminating decimal.
   */
  private static final int INTERMEDIATE_SCALE = 4;

  /**
   * Scale every aggregated nutrition value (totals and per-serving) is rounded to.
   */
  private static final int RESULT_SCALE = 2;

  private UUID id;

  private final String name;
  private final List<RecipeFoodEntry> entries;
  private final int servings;

  /**
   * Default constructor with zero/empty values.
   */
  public Recipe() {
    this.id = UUID.randomUUID();
    this.name = "";
    this.entries = new ArrayList<>();
    this.servings = 1;
  }

  /**
   * Constructor to create a basic recipe.
   *
   * @param name the name
   * @param entries the food items used in this recipe, each with its amount
   * @param servings the number of servings this recipe yields
   */
  public Recipe(String name, List<RecipeFoodEntry> entries, int servings) {
    this.name = name;
    this.entries = entries;
    this.servings = servings;
    validate();
  }

  /**
   * Constructor for a saved recipe.
   *
   * @param id the identifier
   * @param name the name
   * @param entries the food items used in this recipe, each with its amount
   * @param servings the number of servings this recipe yields
   */
  public Recipe(UUID id, String name, List<RecipeFoodEntry> entries, int servings) {
    this.id = id;
    this.name = name;
    this.entries = entries;
    this.servings = servings;
    validate();
  }

  public UUID getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

  public List<RecipeFoodEntry> getEntries() {
    return entries;
  }

  public int getServings() {
    return servings;
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
    if (this.entries == null) {
      throw new ValidationException();
    }
    if (this.servings < 1) {
      throw new ValidationException();
    }
    for (RecipeFoodEntry entry : this.entries) {
      BigDecimal amount = entry.getAmountGrams();
      if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException();
      }
    }
  }

  public BigDecimal getTotalCalories() {
    return sumMacro(Food::getCalory);
  }

  public BigDecimal getTotalFat() {
    return sumMacro(Food::getFat);
  }

  public BigDecimal getTotalSaturatedFat() {
    return sumMacro(Food::getSaturatedFat);
  }

  public BigDecimal getTotalCarbohydrates() {
    return sumMacro(Food::getCarbohydrates);
  }

  public BigDecimal getTotalSugar() {
    return sumMacro(Food::getSugar);
  }

  public BigDecimal getTotalFiber() {
    return sumMacro(Food::getFiber);
  }

  public BigDecimal getTotalProtein() {
    return sumMacro(Food::getProtein);
  }

  public BigDecimal getTotalSalt() {
    return sumMacro(Food::getSalt);
  }

  public BigDecimal getTotalSodium() {
    return sumMacro(Food::getSodium);
  }

  public BigDecimal getCaloriesPerServing() {
    return perServing(getTotalCalories());
  }

  public BigDecimal getFatPerServing() {
    return perServing(getTotalFat());
  }

  public BigDecimal getSaturatedFatPerServing() {
    return perServing(getTotalSaturatedFat());
  }

  public BigDecimal getCarbohydratesPerServing() {
    return perServing(getTotalCarbohydrates());
  }

  public BigDecimal getSugarPerServing() {
    return perServing(getTotalSugar());
  }

  public BigDecimal getFiberPerServing() {
    return perServing(getTotalFiber());
  }

  public BigDecimal getProteinPerServing() {
    return perServing(getTotalProtein());
  }

  public BigDecimal getSaltPerServing() {
    return perServing(getTotalSalt());
  }

  public BigDecimal getSodiumPerServing() {
    return perServing(getTotalSodium());
  }

  /**
   * The recipe's diet is the least strict (weakest) diet among all its food entries - a single
   * non-vegan or unknown-diet ingredient downgrades the whole recipe. A recipe without any food
   * entries is vacuously VEGAN, since there is nothing in it that could violate that.
   *
   * @return the recipe's diet
   */
  public Diet getDiet() {
    return entries.stream()
        .map(entry -> entry.getFood().getDiet())
        .max(Diet.STRICTNESS_ORDER)
        .orElse(Diet.VEGAN);
  }

  private BigDecimal sumMacro(Function<Food, BigDecimal> macro) {
    BigDecimal total = BigDecimal.ZERO;
    for (RecipeFoodEntry entry : entries) {
      BigDecimal value = macro.apply(entry.getFood());
      if (value == null) {
        continue;
      }
      BigDecimal ratio = entry.getAmountGrams()
          .divide(entry.getFood().getGrams(), INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
      total = total.add(value.multiply(ratio));
    }
    return total.setScale(RESULT_SCALE, RoundingMode.HALF_UP);
  }

  private BigDecimal perServing(BigDecimal total) {
    return total.divide(BigDecimal.valueOf(servings), RESULT_SCALE, RoundingMode.HALF_UP);
  }
}
