package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

/**
 * The DTO that is used for the communication with clients.
 *
 * @author Marius Birk
 */
public class FoodDto {
  private String name;
  private BigDecimal calory;
  private BigDecimal grams;

  public FoodDto() {
  }

  /**
   * A constructor that takes all needed informations to despict a food item.
   *
   * @param name the name
   * @param calory amount of calories for the grams
   * @param grams amount of grams
   */
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
