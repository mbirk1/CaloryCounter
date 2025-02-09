package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity that stores all details of a food item.
 *
 * @author Marius Birk
 */
public class FoodDetailsDto {

  private UUID uuid;
  private String name;
  private BigDecimal calory;
  private BigDecimal grams;

  public FoodDetailsDto() {
  }

  /**
   * Constructor, that creates a detailed entity of a food item.
   *
   * @param uuid the identifier
   * @param name the name
   * @param calory amount of calory for the amount of grams
   * @param grams amount of grams
   */
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
