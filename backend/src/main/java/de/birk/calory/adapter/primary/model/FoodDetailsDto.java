package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.UUID;

public class FoodDetailsDto {

  private UUID uuid;
  private String name;
  private BigDecimal calory;
  private BigDecimal grams;

  public FoodDetailsDto() {
  }

  public FoodDetailsDto(UUID uuid, String name, BigDecimal calory, BigDecimal grams) {
    this.uuid = uuid;
    this.name = name;
    this.calory = calory;
    this.grams = grams;
  }

  public UUID getUuid() {
    return uuid;
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
