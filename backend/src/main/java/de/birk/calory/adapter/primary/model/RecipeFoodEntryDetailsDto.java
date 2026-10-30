package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

/**
 * Response-side DTO for a single food entry of a recipe: the full food details plus the amount
 * of it used in the recipe.
 *
 * @author Marius Birk
 */
public class RecipeFoodEntryDetailsDto {

  private final FoodDetailsDto food;
  private final BigDecimal amountGrams;

  public RecipeFoodEntryDetailsDto(FoodDetailsDto food, BigDecimal amountGrams) {
    this.food = food;
    this.amountGrams = amountGrams;
  }

  public FoodDetailsDto getFood() {
    return food;
  }

  public BigDecimal getAmountGrams() {
    return amountGrams;
  }
}
