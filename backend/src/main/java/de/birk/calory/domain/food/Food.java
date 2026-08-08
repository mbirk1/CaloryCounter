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
 * from an external data source such as OpenFoodFacts. {@link #validate()} intentionally only
 * checks the original core fields - all extended fields are optional by design.
 *
 * @author Marius Birk
 */
public class Food extends AbstractEntity<UUID> {

  /**
   * Upper bound for a plausible calory value, matching the bound {@code FoodCsvRowMapper}
   * already enforces on import. Pure fat/oil, the most energy-dense food there is, tops out
   * around 900 kcal per 100g.
   */
  private static final BigDecimal MAX_PLAUSIBLE_CALORIES = new BigDecimal("900");

  /**
   * Upper bound for a plausible macronutrient value: a nutrient can never weigh more than the
   * 100g of food it's a part of.
   */
  private static final BigDecimal MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("100");

  private UUID id;
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

  /**
   * Default constructor with zero/empty values.
   */
  protected Food() {
    calory = BigDecimal.ZERO;
    name = "";
    grams = BigDecimal.ZERO;
    brand = null;
    category = null;
    fat = null;
    saturatedFat = null;
    carbohydrates = null;
    sugar = null;
    fiber = null;
    protein = null;
    salt = null;
    sodium = null;
    imageUrl = null;
    source = FoodSource.MANUAL;
    externalId = null;
    diet = Diet.UNKNOWN;
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
    this(
        id, name, calory, grams,
        null, null, null, null, null, null, null, null, null, null, null,
        FoodSource.MANUAL, null, Diet.UNKNOWN
    );
  }

  /**
   * Constructor to create a food item with all extended nutrition and provenance fields, used
   * for items that were imported from an external data source such as OpenFoodFacts.
   *
   * @param id the identifier as uuid
   * @param name the name of the food
   * @param calory amount of calories
   * @param grams amount of grams for the amount of calories
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
   * @param source where this food item's data came from
   * @param externalId the identifier used by the external data source, if any
   * @param diet the food item's vegan/vegetarian diet compatibility
   * @throws ValidationException validates the entity
   */
  public Food(
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
      FoodSource source,
      String externalId,
      Diet diet) throws ValidationException {
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
    this.diet = diet;
    validate();
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
   * Additionally checks that {@code calory} and every present macronutrient value falls within
   * a plausible range (0-900 kcal, 0-100g respectively) - the same bounds {@code
   * FoodCsvRowMapper} already enforces on CSV import.
   *
   * <p>Deliberately NOT folded into {@link #validate()}, which every constructor call runs
   * unconditionally, including when a {@code Food} is merely being reconstructed from
   * already-persisted data (e.g. every time a page of foods is read). Some existing rows -
   * created before this check existed, or imported before the CSV importer's own plausibility
   * filter was added - already fall outside these bounds; folding this into {@code validate()}
   * would make simply reading such a row throw. This method is instead called explicitly, only
   * by usecases that persist newly entered or edited data.
   *
   * @throws ValidationException if calory or any macronutrient is negative or implausibly high
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
  }

  private void requireInRange(BigDecimal value, BigDecimal max) throws ValidationException {
    if (value == null) {
      return;
    }
    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(max) > 0) {
      throw new ValidationException();
    }
  }
}
