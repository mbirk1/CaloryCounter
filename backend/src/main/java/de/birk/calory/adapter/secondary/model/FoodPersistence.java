package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persistence entity for the food items.
 *
 * <p>The source and extended nutrition fields are stored as plain columns (source as a string,
 * not the domain {@code FoodSource} enum) - persistence entities must not depend on domain
 * types, the mapping between the two is done by the usecase layer converter.
 *
 * <p>With 34 fields, a telescoping constructor would be a serious error risk (two adjacent
 * {@code BigDecimal} parameters silently swapped, undetected by the compiler) - every field
 * beyond the one small convenience constructor below is therefore set through {@link Builder}.
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

  @Column(name = "import_job_id")
  private UUID importJobId;

  @Column
  private String diet;

  @Column(name = "vitamin_a")
  private BigDecimal vitaminA;

  @Column(name = "vitamin_d")
  private BigDecimal vitaminD;

  @Column(name = "vitamin_e")
  private BigDecimal vitaminE;

  @Column(name = "vitamin_k")
  private BigDecimal vitaminK;

  @Column(name = "vitamin_b1")
  private BigDecimal vitaminB1;

  @Column(name = "vitamin_b2")
  private BigDecimal vitaminB2;

  @Column(name = "vitamin_b6")
  private BigDecimal vitaminB6;

  @Column(name = "vitamin_b12")
  private BigDecimal vitaminB12;

  @Column(name = "vitamin_c")
  private BigDecimal vitaminC;

  @Column
  private BigDecimal folate;

  @Column
  private BigDecimal potassium;

  @Column
  private BigDecimal calcium;

  @Column
  private BigDecimal magnesium;

  @Column
  private BigDecimal iron;

  @Column
  private BigDecimal zinc;

  @Column
  private BigDecimal iodine;

  public FoodPersistence() {
    //for JPA
  }

  /**
   * Constructor to create an instance of the object with only the core fields, all extended
   * fields default to {@code null} (source defaults to {@code MANUAL} and diet to
   * {@code UNKNOWN} to satisfy the not-null database columns). Used e.g. for the food items
   * embedded in a recipe.
   *
   * @param id the identifier
   * @param name the name
   * @param calory amount of calories for the grams
   * @param grams amount of grams
   */
  public FoodPersistence(UUID id, String name, BigDecimal calory, BigDecimal grams) {
    this(new Builder().id(id).name(name).calory(calory).grams(grams)
        .source("MANUAL").diet("UNKNOWN"));
  }

  private FoodPersistence(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.calory = builder.calory;
    this.grams = builder.grams;
    this.brand = builder.brand;
    this.category = builder.category;
    this.fat = builder.fat;
    this.saturatedFat = builder.saturatedFat;
    this.carbohydrates = builder.carbohydrates;
    this.sugar = builder.sugar;
    this.fiber = builder.fiber;
    this.protein = builder.protein;
    this.salt = builder.salt;
    this.sodium = builder.sodium;
    this.imageUrl = builder.imageUrl;
    this.source = builder.source;
    this.externalId = builder.externalId;
    this.diet = builder.diet;
    this.vitaminA = builder.vitaminA;
    this.vitaminD = builder.vitaminD;
    this.vitaminE = builder.vitaminE;
    this.vitaminK = builder.vitaminK;
    this.vitaminB1 = builder.vitaminB1;
    this.vitaminB2 = builder.vitaminB2;
    this.vitaminB6 = builder.vitaminB6;
    this.vitaminB12 = builder.vitaminB12;
    this.vitaminC = builder.vitaminC;
    this.folate = builder.folate;
    this.potassium = builder.potassium;
    this.calcium = builder.calcium;
    this.magnesium = builder.magnesium;
    this.iron = builder.iron;
    this.zinc = builder.zinc;
    this.iodine = builder.iodine;
  }

  /**
   * Starts building a food persistence entity field by field, instead of through a long,
   * error-prone positional constructor call - the only way to set the extended nutrition/
   * provenance fields.
   *
   * @return a new, empty builder
   */
  public static Builder builder() {
    return new Builder();
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

  public UUID getImportJobId() {
    return importJobId;
  }

  public void setImportJobId(UUID importJobId) {
    this.importJobId = importJobId;
  }

  public String getDiet() {
    return diet;
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

  /**
   * Builds a {@link FoodPersistence} field by field. Every setter returns {@code this} for
   * chaining.
   */
  public static final class Builder {
    private UUID id;
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
    private String source = "MANUAL";
    private String externalId;
    private String diet = "UNKNOWN";
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

    /**
     * Sets the identifier.
     *
     * @param id the identifier
     * @return this builder
     */
    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the amount of calories.
     *
     * @param calory amount of calories for the grams
     * @return this builder
     */
    public Builder calory(BigDecimal calory) {
      this.calory = calory;
      return this;
    }

    /**
     * Sets the amount of grams.
     *
     * @param grams amount of grams
     * @return this builder
     */
    public Builder grams(BigDecimal grams) {
      this.grams = grams;
      return this;
    }

    /**
     * Sets the manufacturer/brand.
     *
     * @param brand the manufacturer/brand
     * @return this builder
     */
    public Builder brand(String brand) {
      this.brand = brand;
      return this;
    }

    /**
     * Sets the product category.
     *
     * @param category the product category
     * @return this builder
     */
    public Builder category(String category) {
      this.category = category;
      return this;
    }

    /**
     * Sets the amount of fat.
     *
     * @param fat amount of fat per {@code grams}
     * @return this builder
     */
    public Builder fat(BigDecimal fat) {
      this.fat = fat;
      return this;
    }

    /**
     * Sets the amount of saturated fat.
     *
     * @param saturatedFat amount of saturated fat per {@code grams}
     * @return this builder
     */
    public Builder saturatedFat(BigDecimal saturatedFat) {
      this.saturatedFat = saturatedFat;
      return this;
    }

    /**
     * Sets the amount of carbohydrates.
     *
     * @param carbohydrates amount of carbohydrates per {@code grams}
     * @return this builder
     */
    public Builder carbohydrates(BigDecimal carbohydrates) {
      this.carbohydrates = carbohydrates;
      return this;
    }

    /**
     * Sets the amount of sugar.
     *
     * @param sugar amount of sugar per {@code grams}
     * @return this builder
     */
    public Builder sugar(BigDecimal sugar) {
      this.sugar = sugar;
      return this;
    }

    /**
     * Sets the amount of fiber.
     *
     * @param fiber amount of fiber per {@code grams}
     * @return this builder
     */
    public Builder fiber(BigDecimal fiber) {
      this.fiber = fiber;
      return this;
    }

    /**
     * Sets the amount of protein.
     *
     * @param protein amount of protein per {@code grams}
     * @return this builder
     */
    public Builder protein(BigDecimal protein) {
      this.protein = protein;
      return this;
    }

    /**
     * Sets the amount of salt.
     *
     * @param salt amount of salt per {@code grams}
     * @return this builder
     */
    public Builder salt(BigDecimal salt) {
      this.salt = salt;
      return this;
    }

    /**
     * Sets the amount of sodium.
     *
     * @param sodium amount of sodium per {@code grams}
     * @return this builder
     */
    public Builder sodium(BigDecimal sodium) {
      this.sodium = sodium;
      return this;
    }

    /**
     * Sets the product image URL.
     *
     * @param imageUrl a URL pointing to a product image
     * @return this builder
     */
    public Builder imageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    /**
     * Sets the name of the {@code FoodSource} enum constant this item came from, defaults to
     * {@code "MANUAL"} if never called.
     *
     * @param source the name of the {@code FoodSource} enum constant this item came from
     * @return this builder
     */
    public Builder source(String source) {
      this.source = source;
      return this;
    }

    /**
     * Sets the identifier used by the external data source.
     *
     * @param externalId the identifier used by the external data source, if any
     * @return this builder
     */
    public Builder externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }

    /**
     * Sets the name of the {@code Diet} enum constant this item is compatible with, defaults to
     * {@code "UNKNOWN"} if never called.
     *
     * @param diet the name of the {@code Diet} enum constant this item is compatible with
     * @return this builder
     */
    public Builder diet(String diet) {
      this.diet = diet;
      return this;
    }

    /**
     * Sets the amount of vitamin A (retinol equivalent, µg).
     *
     * @param vitaminA amount of vitamin A per {@code grams}, in µg
     * @return this builder
     */
    public Builder vitaminA(BigDecimal vitaminA) {
      this.vitaminA = vitaminA;
      return this;
    }

    /**
     * Sets the amount of vitamin D (µg).
     *
     * @param vitaminD amount of vitamin D per {@code grams}, in µg
     * @return this builder
     */
    public Builder vitaminD(BigDecimal vitaminD) {
      this.vitaminD = vitaminD;
      return this;
    }

    /**
     * Sets the amount of vitamin E (alpha-tocopherol, mg).
     *
     * @param vitaminE amount of vitamin E per {@code grams}, in mg
     * @return this builder
     */
    public Builder vitaminE(BigDecimal vitaminE) {
      this.vitaminE = vitaminE;
      return this;
    }

    /**
     * Sets the amount of vitamin K (µg).
     *
     * @param vitaminK amount of vitamin K per {@code grams}, in µg
     * @return this builder
     */
    public Builder vitaminK(BigDecimal vitaminK) {
      this.vitaminK = vitaminK;
      return this;
    }

    /**
     * Sets the amount of vitamin B1 (thiamin, mg).
     *
     * @param vitaminB1 amount of vitamin B1 per {@code grams}, in mg
     * @return this builder
     */
    public Builder vitaminB1(BigDecimal vitaminB1) {
      this.vitaminB1 = vitaminB1;
      return this;
    }

    /**
     * Sets the amount of vitamin B2 (riboflavin, mg).
     *
     * @param vitaminB2 amount of vitamin B2 per {@code grams}, in mg
     * @return this builder
     */
    public Builder vitaminB2(BigDecimal vitaminB2) {
      this.vitaminB2 = vitaminB2;
      return this;
    }

    /**
     * Sets the amount of vitamin B6 (µg).
     *
     * @param vitaminB6 amount of vitamin B6 per {@code grams}, in µg
     * @return this builder
     */
    public Builder vitaminB6(BigDecimal vitaminB6) {
      this.vitaminB6 = vitaminB6;
      return this;
    }

    /**
     * Sets the amount of vitamin B12 (cobalamine, µg).
     *
     * @param vitaminB12 amount of vitamin B12 per {@code grams}, in µg
     * @return this builder
     */
    public Builder vitaminB12(BigDecimal vitaminB12) {
      this.vitaminB12 = vitaminB12;
      return this;
    }

    /**
     * Sets the amount of vitamin C (mg).
     *
     * @param vitaminC amount of vitamin C per {@code grams}, in mg
     * @return this builder
     */
    public Builder vitaminC(BigDecimal vitaminC) {
      this.vitaminC = vitaminC;
      return this;
    }

    /**
     * Sets the amount of folate (folate equivalent, µg).
     *
     * @param folate amount of folate per {@code grams}, in µg
     * @return this builder
     */
    public Builder folate(BigDecimal folate) {
      this.folate = folate;
      return this;
    }

    /**
     * Sets the amount of potassium (mg).
     *
     * @param potassium amount of potassium per {@code grams}, in mg
     * @return this builder
     */
    public Builder potassium(BigDecimal potassium) {
      this.potassium = potassium;
      return this;
    }

    /**
     * Sets the amount of calcium (mg).
     *
     * @param calcium amount of calcium per {@code grams}, in mg
     * @return this builder
     */
    public Builder calcium(BigDecimal calcium) {
      this.calcium = calcium;
      return this;
    }

    /**
     * Sets the amount of magnesium (mg).
     *
     * @param magnesium amount of magnesium per {@code grams}, in mg
     * @return this builder
     */
    public Builder magnesium(BigDecimal magnesium) {
      this.magnesium = magnesium;
      return this;
    }

    /**
     * Sets the amount of iron (mg).
     *
     * @param iron amount of iron per {@code grams}, in mg
     * @return this builder
     */
    public Builder iron(BigDecimal iron) {
      this.iron = iron;
      return this;
    }

    /**
     * Sets the amount of zinc (mg).
     *
     * @param zinc amount of zinc per {@code grams}, in mg
     * @return this builder
     */
    public Builder zinc(BigDecimal zinc) {
      this.zinc = zinc;
      return this;
    }

    /**
     * Sets the amount of iodine (µg).
     *
     * @param iodine amount of iodine per {@code grams}, in µg
     * @return this builder
     */
    public Builder iodine(BigDecimal iodine) {
      this.iodine = iodine;
      return this;
    }

    /**
     * Builds the food persistence entity.
     *
     * @return the built food persistence entity
     */
    public FoodPersistence build() {
      return new FoodPersistence(this);
    }
  }
}
