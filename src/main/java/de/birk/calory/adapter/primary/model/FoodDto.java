package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

public class FoodDto {
  private final String name;
  private final BigDecimal calory;

  public FoodDto(String name, BigDecimal calory) {
    this.name = name;
    this.calory = calory;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCalory() {
    return calory;
  }
}
