package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

/**
 * The DTO used to submit edits to an existing food item.
 *
 * <p>Deliberately excludes {@code brand}, {@code category}, {@code imageUrl}, {@code source}
 * and {@code externalId} - those provenance fields describe where a food item's data came from
 * and are not meant to be edited through this endpoint.
 *
 * @author Marius Birk
 */
public class UpdateFoodDto {
  private String name;
  private BigDecimal calory;
  private BigDecimal grams;
  private String diet;
  private BigDecimal fat;
  private BigDecimal saturatedFat;
  private BigDecimal carbohydrates;
  private BigDecimal sugar;
  private BigDecimal fiber;
  private BigDecimal protein;
  private BigDecimal salt;
  private BigDecimal sodium;

  public UpdateFoodDto() {
  }

  /**
   * A constructor that takes all editable fields of a food item.
   *
   * @param name the name
   * @param calory amount of calories for the amount of grams
   * @param grams amount of grams
   * @param diet the name of the {@code Diet} enum constant, optional - {@code null} defaults
   *     to {@code UNKNOWN}
   * @param fat amount of fat per {@code grams}
   * @param saturatedFat amount of saturated fat per {@code grams}
   * @param carbohydrates amount of carbohydrates per {@code grams}
   * @param sugar amount of sugar per {@code grams}
   * @param fiber amount of fiber per {@code grams}
   * @param protein amount of protein per {@code grams}
   * @param salt amount of salt per {@code grams}
   * @param sodium amount of sodium per {@code grams}
   */
  public UpdateFoodDto(
      String name,
      BigDecimal calory,
      BigDecimal grams,
      String diet,
      BigDecimal fat,
      BigDecimal saturatedFat,
      BigDecimal carbohydrates,
      BigDecimal sugar,
      BigDecimal fiber,
      BigDecimal protein,
      BigDecimal salt,
      BigDecimal sodium) {
    this.name = name;
    this.calory = calory;
    this.grams = grams;
    this.diet = diet;
    this.fat = fat;
    this.saturatedFat = saturatedFat;
    this.carbohydrates = carbohydrates;
    this.sugar = sugar;
    this.fiber = fiber;
    this.protein = protein;
    this.salt = salt;
    this.sodium = sodium;
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

  public String getDiet() {
    return diet;
  }

  public BigDecimal getFat() {
    return fat;
  }

  public BigDecimal getSaturatedFat() {
    return saturatedFat;
  }

  public BigDecimal getCarbohydrates() {
    return carbohydrates;
  }

  public BigDecimal getSugar() {
    return sugar;
  }

  public BigDecimal getFiber() {
    return fiber;
  }

  public BigDecimal getProtein() {
    return protein;
  }

  public BigDecimal getSalt() {
    return salt;
  }

  public BigDecimal getSodium() {
    return sodium;
  }
}
