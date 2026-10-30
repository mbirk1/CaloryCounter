package de.birk.calory.domain.recipe;

import java.math.BigDecimal;

import de.birk.calory.domain.food.Food;

/**
 * A single food item used in a recipe, together with the amount (in grams) actually used - not
 * necessarily the food's own base amount ({@code food.getGrams()}), which is why nutrition
 * values must be scaled proportionally before being aggregated by {@link Recipe}.
 *
 * <p>Deliberately does not validate itself - {@link Recipe#validate()} is the single place that
 * checks {@code amountGrams} is positive, consistent with the rest of the recipe validation.
 *
 * @author Marius Birk
 */
public class RecipeFoodEntry {

  private final Food food;
  private final BigDecimal amountGrams;

  public RecipeFoodEntry(Food food, BigDecimal amountGrams) {
    this.food = food;
    this.amountGrams = amountGrams;
  }

  public Food getFood() {
    return food;
  }

  public BigDecimal getAmountGrams() {
    return amountGrams;
  }
}
