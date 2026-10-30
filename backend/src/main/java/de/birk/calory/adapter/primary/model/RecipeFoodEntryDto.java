package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request-side DTO for a single food entry of a recipe: only a reference to the food by id plus
 * the amount used - the client never sends nutrition data, it is always resolved server-side
 * from the referenced food's own, current data.
 *
 * @author Marius Birk
 */
public class RecipeFoodEntryDto {

  private UUID foodId;
  private BigDecimal amountGrams;

  public RecipeFoodEntryDto() {
  }

  public RecipeFoodEntryDto(UUID foodId, BigDecimal amountGrams) {
    this.foodId = foodId;
    this.amountGrams = amountGrams;
  }

  public UUID getFoodId() {
    return foodId;
  }

  public BigDecimal getAmountGrams() {
    return amountGrams;
  }
}
