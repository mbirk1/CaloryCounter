package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Persistence entity for the food items.
 *
 * <p>The source and extended nutrition fields are stored as plain columns (source as a string,
 * not the domain {@code FoodSource} enum) - persistence entities must not depend on domain
 * types, the mapping between the two is done by the usecase layer converter.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_food")
public class FoodPersistence {

  @Id
  @Column
  private UUID id;
  @Column
  private String name;
  @Column(name = "calory_count")
  private BigDecimal calory;

  @Column(name = "grams")
  private BigDecimal grams;

  @Column
  private String brand;

  @Column
  private String category;

  @Column
  private BigDecimal fat;

  @Column(name = "saturated_fat")
  private BigDecimal saturatedFat;

  @Column
  private BigDecimal carbohydrates;

  @Column
  private BigDecimal sugar;

  @Column
  private BigDecimal fiber;

  @Column
  private BigDecimal protein;

  @Column
  private BigDecimal salt;

  @Column
  private BigDecimal sodium;

  @Column(name = "image_url")
  private String imageUrl;

  @Column
  private String source;

  @Column(name = "external_id")
  private String externalId;

  @ManyToMany(mappedBy = "foods")
  private Set<RecipePersistence> recipe;

  public FoodPersistence() {
    //for JPA
  }

  /**
   * Constructor to create an instance of the object with only the core fields, all extended
   * fields default to {@code null} (source defaults to {@code MANUAL} to satisfy the
   * not-null database column). Used e.g. for the food items embedded in a recipe.
   *
   * @param id the identifier
   * @param name the name
   * @param calory amount of calories for the grams
   * @param grams amount of grams
   */
  public FoodPersistence(UUID id, String name, BigDecimal calory, BigDecimal grams) {
    this(
        id, name, calory, grams,
        null, null, null, null, null, null, null, null, null, null, null, "MANUAL", null
    );
  }

  /**
   * Constructor to create an instance of the object, that can be saved in the database.
   *
   * @param id the identifier
   * @param name the name
   * @param calory amount of calories for the grams
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
  public FoodPersistence(
      UUID id,
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
    this.id = id;
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

  public UUID getId() {
    return id;
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
