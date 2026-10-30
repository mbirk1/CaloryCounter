package de.birk.calory.domain.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.exception.ValidationException;


/**
 * Entity that depicts a food item. It needs to display all important informations
 * that count for calories and recipes.
 *
 * <p>Besides the original core fields (name, calory, grams) a food item optionally carries
 * extended nutrition and provenance data, populated either manually or through a bulk import
 * from an external data source such as OpenFoodFacts or the Bundeslebensmittelschlüssel (BLS).
 * {@link #validate()} intentionally only checks the original core fields - all extended fields
 * are optional by design.
 *
 * <p>With 34 fields, a telescoping constructor would be a serious error risk (two adjacent
 * {@code BigDecimal} parameters silently swapped, undetected by the compiler) - every field
 * beyond the two small convenience constructors below is therefore set through {@link Builder}.
 *
 * @author Marius Birk
 */
public class Food extends AbstractEntity<UUID> {

  /**
   * Upper bound for a plausible calory value, matching the bound {@code FoodCsvRowMapper} and
   * {@code BlsFoodRowMapper} already enforce on import. Pure fat/oil, the most energy-dense food
   * there is, tops out around 900 kcal per 100g.
   */
  private static final BigDecimal MAX_PLAUSIBLE_CALORIES = new BigDecimal("900");

  /**
   * Upper bound for a plausible macronutrient value: a nutrient can never weigh more than the
   * 100g of food it's a part of.
   */
  private static final BigDecimal MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("100");

  // Micronutrient upper bounds - calibrated against the actual extreme values present in the
  // BLS 4.0 reference dataset (e.g. dried herbs for iron/zinc, liver for vitamin A/B12, roasted
  // nori for iodine) plus headroom, not picked blindly. Two intentionally sit exactly at the
  // ticket's own examples (vitamin A, vitamin D, vitamin C, potassium) even though a handful of
  // BLS rows exceed them - those rows are near-pure chemical additives (e.g. ascorbic acid E300
  // for vitamin C, potassium carbonate E501 for potassium), not real foods, and are meant to have
  // that one field rejected rather than raising the bound to accommodate them.
  private static final BigDecimal MAX_VITAMIN_A_MICROGRAMS = new BigDecimal("30000");
  private static final BigDecimal MAX_VITAMIN_D_MICROGRAMS = new BigDecimal("500");
  private static final BigDecimal MAX_VITAMIN_E_MILLIGRAMS = new BigDecimal("300");
  private static final BigDecimal MAX_VITAMIN_K_MICROGRAMS = new BigDecimal("3000");
  private static final BigDecimal MAX_VITAMIN_B1_MILLIGRAMS = new BigDecimal("30");
  private static final BigDecimal MAX_VITAMIN_B2_MILLIGRAMS = new BigDecimal("20");
  private static final BigDecimal MAX_VITAMIN_B6_MICROGRAMS = new BigDecimal("10000");
  private static final BigDecimal MAX_VITAMIN_B12_MICROGRAMS = new BigDecimal("200");
  private static final BigDecimal MAX_VITAMIN_C_MILLIGRAMS = new BigDecimal("3000");
  private static final BigDecimal MAX_FOLATE_MICROGRAMS = new BigDecimal("20000");
  private static final BigDecimal MAX_POTASSIUM_MILLIGRAMS = new BigDecimal("10000");
  private static final BigDecimal MAX_CALCIUM_MILLIGRAMS = new BigDecimal("3000");
  private static final BigDecimal MAX_MAGNESIUM_MILLIGRAMS = new BigDecimal("2000");
  private static final BigDecimal MAX_IRON_MILLIGRAMS = new BigDecimal("150");
  private static final BigDecimal MAX_ZINC_MILLIGRAMS = new BigDecimal("150");
  private static final BigDecimal MAX_IODINE_MICROGRAMS = new BigDecimal("5000");

  private final UUID id;
  private final String name;
  private final BigDecimal calory;
  private final BigDecimal grams;
  private final String brand;
  private final String category;
  private final BigDecimal fat;
  private final BigDecimal saturatedFat;
  private final BigDecimal carbohydrates;
  private final BigDecimal sugar;
  private final BigDecimal fiber;
  private final BigDecimal protein;
  private final BigDecimal salt;
  private final BigDecimal sodium;
  private final String imageUrl;
  private final FoodSource source;
  private final String externalId;
  private final Diet diet;
  private final BigDecimal vitaminA;
  private final BigDecimal vitaminD;
  private final BigDecimal vitaminE;
  private final BigDecimal vitaminK;
  private final BigDecimal vitaminB1;
  private final BigDecimal vitaminB2;
  private final BigDecimal vitaminB6;
  private final BigDecimal vitaminB12;
  private final BigDecimal vitaminC;
  private final BigDecimal folate;
  private final BigDecimal potassium;
  private final BigDecimal calcium;
  private final BigDecimal magnesium;
  private final BigDecimal iron;
  private final BigDecimal zinc;
  private final BigDecimal iodine;

  /**
   * Default constructor with zero/empty values.
   */
  protected Food() {
    this.id = null;
    this.calory = BigDecimal.ZERO;
    this.name = "";
    this.grams = BigDecimal.ZERO;
    this.brand = null;
    this.category = null;
    this.fat = null;
    this.saturatedFat = null;
    this.carbohydrates = null;
    this.sugar = null;
    this.fiber = null;
    this.protein = null;
    this.salt = null;
    this.sodium = null;
    this.imageUrl = null;
    this.source = FoodSource.MANUAL;
    this.externalId = null;
    this.diet = Diet.UNKNOWN;
    this.vitaminA = null;
    this.vitaminD = null;
    this.vitaminE = null;
    this.vitaminK = null;
    this.vitaminB1 = null;
    this.vitaminB2 = null;
    this.vitaminB6 = null;
    this.vitaminB12 = null;
    this.vitaminC = null;
    this.folate = null;
    this.potassium = null;
    this.calcium = null;
    this.magnesium = null;
    this.iron = null;
    this.zinc = null;
    this.iodine = null;
  }

  /**
   * Constructor to create a basic, manually entered food item.
   *
   * @param name the name of the food
   * @param calory amount of calories
   * @param grams amount of grams for the amount of calories
   */
  public Food(String name, BigDecimal calory, BigDecimal grams) {
    this(UUID.randomUUID(), name, calory, grams);
  }

  /**
   * Constructor to create a basic, manually entered food item with an id.
   *
   * @param id the identifier as uuid
   * @param name the name of the food
   * @param calory amount of calories
   * @param grams amount of grams for the amount of calories
   * @throws ValidationException validates the entity
   */
  public Food(UUID id, String name, BigDecimal calory, BigDecimal grams)
      throws ValidationException {
    this(new Builder().id(id).name(name).calory(calory).grams(grams));
  }

  private Food(Builder builder) throws ValidationException {
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
    validate();
  }

  /**
   * Starts building a food item field by field, instead of through a long, error-prone
   * positional constructor call - the only way to set the extended nutrition/provenance fields.
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
    return this.grams;
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

  public FoodSource getSource() {
    return source;
  }

  public String getExternalId() {
    return externalId;
  }

  public Diet getDiet() {
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

  protected void validate() throws ValidationException {
    if (this.grams == null) {
      throw new ValidationException();
    }
    if (this.name == null || this.name.isEmpty() || this.name.isBlank()) {
      throw new ValidationException();
    }
    if (this.calory == null) {
      throw new ValidationException();
    }
  }

  /**
   * Additionally checks that {@code calory} and every present macro- and micronutrient value
   * falls within a plausible range - the same bounds {@code FoodCsvRowMapper} and
   * {@code BlsFoodRowMapper} already enforce on import.
   *
   * <p>Deliberately NOT folded into {@link #validate()}, which every constructor call runs
   * unconditionally, including when a {@code Food} is merely being reconstructed from
   * already-persisted data (e.g. every time a page of foods is read). Some existing rows -
   * created before this check existed, or imported before the importer's own plausibility filter
   * was added - already fall outside these bounds; folding this into {@code validate()} would
   * make simply reading such a row throw. This method is instead called explicitly, only by
   * usecases that persist newly entered or edited data.
   *
   * @throws ValidationException if calory or any macro-/micronutrient is negative or implausibly
   *     high
   */
  public void validatePlausibility() throws ValidationException {
    requireInRange(this.calory, MAX_PLAUSIBLE_CALORIES);
    requireInRange(this.fat, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.saturatedFat, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.carbohydrates, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.sugar, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.fiber, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.protein, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.salt, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.sodium, MAX_PLAUSIBLE_MACRO_GRAMS);
    requireInRange(this.vitaminA, MAX_VITAMIN_A_MICROGRAMS);
    requireInRange(this.vitaminD, MAX_VITAMIN_D_MICROGRAMS);
    requireInRange(this.vitaminE, MAX_VITAMIN_E_MILLIGRAMS);
    requireInRange(this.vitaminK, MAX_VITAMIN_K_MICROGRAMS);
    requireInRange(this.vitaminB1, MAX_VITAMIN_B1_MILLIGRAMS);
    requireInRange(this.vitaminB2, MAX_VITAMIN_B2_MILLIGRAMS);
    requireInRange(this.vitaminB6, MAX_VITAMIN_B6_MICROGRAMS);
    requireInRange(this.vitaminB12, MAX_VITAMIN_B12_MICROGRAMS);
    requireInRange(this.vitaminC, MAX_VITAMIN_C_MILLIGRAMS);
    requireInRange(this.folate, MAX_FOLATE_MICROGRAMS);
    requireInRange(this.potassium, MAX_POTASSIUM_MILLIGRAMS);
    requireInRange(this.calcium, MAX_CALCIUM_MILLIGRAMS);
    requireInRange(this.magnesium, MAX_MAGNESIUM_MILLIGRAMS);
    requireInRange(this.iron, MAX_IRON_MILLIGRAMS);
    requireInRange(this.zinc, MAX_ZINC_MILLIGRAMS);
    requireInRange(this.iodine, MAX_IODINE_MICROGRAMS);
  }

  private void requireInRange(BigDecimal value, BigDecimal max) throws ValidationException {
    if (value == null) {
      return;
    }
    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(max) > 0) {
      throw new ValidationException();
    }
  }

  /**
   * Builds a {@link Food} field by field. Every setter returns {@code this} for chaining;
   * {@link #build()} runs the same {@link Food#validate()} check as every other constructor.
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
    private FoodSource source = FoodSource.MANUAL;
    private String externalId;
    private Diet diet = Diet.UNKNOWN;
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
     * @param id the identifier as uuid
     * @return this builder
     */
    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the name.
     *
     * @param name the name of the food
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the amount of calories.
     *
     * @param calory amount of calories per {@code grams}
     * @return this builder
     */
    public Builder calory(BigDecimal calory) {
      this.calory = calory;
      return this;
    }

    /**
     * Sets the amount of grams the other values are measured for.
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
     * Sets where this food item's data came from, defaults to {@link FoodSource#MANUAL} if
     * never called.
     *
     * @param source where this food item's data came from
     * @return this builder
     */
    public Builder source(FoodSource source) {
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
     * Sets the vegan/vegetarian diet compatibility, defaults to {@link Diet#UNKNOWN} if never
     * called.
     *
     * @param diet the food item's vegan/vegetarian diet compatibility
     * @return this builder
     */
    public Builder diet(Diet diet) {
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
     * Builds the food item, running the same validation every other constructor runs.
     *
     * @return the built food item
     * @throws ValidationException if a core field is missing
     */
    public Food build() throws ValidationException {
      return new Food(this);
    }
  }
}
