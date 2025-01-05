package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

public class FoodDto {
  private String name;
  private BigDecimal calory;
  private BigDecimal grams;

  public FoodDto() {
  }

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
