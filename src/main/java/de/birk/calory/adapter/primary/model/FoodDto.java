package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

public class FoodDto {
  private final String name;
  private final BigDecimal calory;
  private final BigDecimal grams;

  public FoodDto(String name, BigDecimal calory, BigDecimal grams) {
    this.name = name;
    this.calory = calory;
    this.grams = grams;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCalory() {
    return calory;
  }
  public BigDecimal getGrams() {
    return grams;
  }
}
