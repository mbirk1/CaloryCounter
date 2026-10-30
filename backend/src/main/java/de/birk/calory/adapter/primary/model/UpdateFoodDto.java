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
  private BigDecimal vitaminA;
  private BigDecimal vitaminD;
  private BigDecimal vitaminE;
  private BigDecimal vitaminK;
  private BigDecimal vitaminB1;
  private BigDecimal vitaminB2;
  private BigDecimal vitaminB6;
  private BigDecimal vitaminB12;
  private BigDecimal vitaminC;
  private BigDecimal folate;
  private BigDecimal potassium;
  private BigDecimal calcium;
  private BigDecimal magnesium;
  private BigDecimal iron;
  private BigDecimal zinc;
  private BigDecimal iodine;

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
   * @param vitaminA amount of vitamin A (µg) per {@code grams}, optional
   * @param vitaminD amount of vitamin D (µg) per {@code grams}, optional
   * @param vitaminE amount of vitamin E (mg) per {@code grams}, optional
   * @param vitaminK amount of vitamin K (µg) per {@code grams}, optional
   * @param vitaminB1 amount of vitamin B1 (mg) per {@code grams}, optional
   * @param vitaminB2 amount of vitamin B2 (mg) per {@code grams}, optional
   * @param vitaminB6 amount of vitamin B6 (µg) per {@code grams}, optional
   * @param vitaminB12 amount of vitamin B12 (µg) per {@code grams}, optional
   * @param vitaminC amount of vitamin C (mg) per {@code grams}, optional
   * @param folate amount of folate (µg) per {@code grams}, optional
   * @param potassium amount of potassium (mg) per {@code grams}, optional
   * @param calcium amount of calcium (mg) per {@code grams}, optional
   * @param magnesium amount of magnesium (mg) per {@code grams}, optional
   * @param iron amount of iron (mg) per {@code grams}, optional
   * @param zinc amount of zinc (mg) per {@code grams}, optional
   * @param iodine amount of iodine (µg) per {@code grams}, optional
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
      BigDecimal sodium,
      BigDecimal vitaminA,
      BigDecimal vitaminD,
      BigDecimal vitaminE,
      BigDecimal vitaminK,
      BigDecimal vitaminB1,
      BigDecimal vitaminB2,
      BigDecimal vitaminB6,
      BigDecimal vitaminB12,
      BigDecimal vitaminC,
      BigDecimal folate,
      BigDecimal potassium,
      BigDecimal calcium,
      BigDecimal magnesium,
      BigDecimal iron,
      BigDecimal zinc,
      BigDecimal iodine) {
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
    this.vitaminA = vitaminA;
    this.vitaminD = vitaminD;
    this.vitaminE = vitaminE;
    this.vitaminK = vitaminK;
    this.vitaminB1 = vitaminB1;
    this.vitaminB2 = vitaminB2;
    this.vitaminB6 = vitaminB6;
    this.vitaminB12 = vitaminB12;
    this.vitaminC = vitaminC;
    this.folate = folate;
    this.potassium = potassium;
    this.calcium = calcium;
    this.magnesium = magnesium;
    this.iron = iron;
    this.zinc = zinc;
    this.iodine = iodine;
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

  public BigDecimal getVitaminA() {
    return vitaminA;
  }

  public BigDecimal getVitaminD() {
    return vitaminD;
  }

  public BigDecimal getVitaminE() {
    return vitaminE;
  }

  public BigDecimal getVitaminK() {
    return vitaminK;
  }

  public BigDecimal getVitaminB1() {
    return vitaminB1;
  }

  public BigDecimal getVitaminB2() {
    return vitaminB2;
  }

  public BigDecimal getVitaminB6() {
    return vitaminB6;
  }

  public BigDecimal getVitaminB12() {
    return vitaminB12;
  }

  public BigDecimal getVitaminC() {
    return vitaminC;
  }

  public BigDecimal getFolate() {
    return folate;
  }

  public BigDecimal getPotassium() {
    return potassium;
  }

  public BigDecimal getCalcium() {
    return calcium;
  }

  public BigDecimal getMagnesium() {
    return magnesium;
  }

  public BigDecimal getIron() {
    return iron;
  }

  public BigDecimal getZinc() {
    return zinc;
  }

  public BigDecimal getIodine() {
    return iodine;
  }
}
