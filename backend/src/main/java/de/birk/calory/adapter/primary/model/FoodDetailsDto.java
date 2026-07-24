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
  private String brand;
  private String category;
  private BigDecimal fat;
  private BigDecimal saturatedFat;
  private BigDecimal carbohydrates;
  private BigDecimal sugar;
  private BigDecimal fiber;
  private BigDecimal protein;
  private BigDecimal salt;
  private BigDecimal sodium;
  private String imageUrl;
  private String source;
  private String externalId;

  public FoodDetailsDto() {
  }

  /**
   * Constructor to create a detailed entity of a food item with only the core fields, all
   * extended fields default to {@code null}. Used e.g. for the food items embedded in a recipe.
   *
   * @param uuid the identifier
   * @param name the name
   * @param calory amount of calory for the amount of grams
   * @param grams amount of grams
   */
  public FoodDetailsDto(UUID uuid, String name, BigDecimal calory, BigDecimal grams) {
    this(
        uuid, name, calory, grams,
        null, null, null, null, null, null, null, null, null, null, null, null, null
    );
  }

  /**
   * Constructor, that creates a detailed entity of a food item.
   *
   * @param uuid the identifier
   * @param name the name
   * @param calory amount of calory for the amount of grams
   * @param grams amount of grams
   * @param brand the manufacturer/brand
   * @param category the product category
   * @param fat amount of fat per {@code grams}
   * @param saturatedFat amount of saturated fat per {@code grams}
   * @param carbohydrates amount of carbohydrates per {@code grams}
   * @param sugar amount of sugar per {@code grams}
   * @param fiber amount of fiber per {@code grams}
   * @param protein amount of protein per {@code grams}
   * @param salt amount of salt per {@code grams}
   * @param sodium amount of sodium per {@code grams}
   * @param imageUrl a URL pointing to a product image
   * @param source the name of the {@code FoodSource} enum constant this item came from
   * @param externalId the identifier used by the external data source, if any
   */
  public FoodDetailsDto(
      UUID uuid,
      String name,
      BigDecimal calory,
      BigDecimal grams,
      String brand,
      String category,
      BigDecimal fat,
      BigDecimal saturatedFat,
      BigDecimal carbohydrates,
      BigDecimal sugar,
      BigDecimal fiber,
      BigDecimal protein,
      BigDecimal salt,
      BigDecimal sodium,
      String imageUrl,
      String source,
      String externalId) {
    this.uuid = uuid;
    this.name = name;
    this.calory = calory;
    this.grams = grams;
    this.brand = brand;
    this.category = category;
    this.fat = fat;
    this.saturatedFat = saturatedFat;
    this.carbohydrates = carbohydrates;
    this.sugar = sugar;
    this.fiber = fiber;
    this.protein = protein;
    this.salt = salt;
    this.sodium = sodium;
    this.imageUrl = imageUrl;
    this.source = source;
    this.externalId = externalId;
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

  public String getBrand() {
    return brand;
  }

  public String getCategory() {
    return category;
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

  public String getImageUrl() {
    return imageUrl;
  }

  public String getSource() {
    return source;
  }

  public String getExternalId() {
    return externalId;
  }
}
