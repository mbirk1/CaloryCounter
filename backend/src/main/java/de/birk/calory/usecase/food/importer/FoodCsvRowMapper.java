package de.birk.calory.usecase.food.importer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.csv.CSVRecord;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.FoodSource;

/**
 * Pure mapping logic from a single OpenFoodFacts CSV row to a persistable food item.
 *
 * <p>A row is only mapped if the mandatory quality filter is satisfied: a non-blank product
 * name, a non-blank brand and either {@code energy-kcal_100g} or a convertible
 * {@code energy_100g} (kJ) value that is also plausible (0-900 kcal per 100g). All other fields
 * are optional and simply left {@code null} when missing, unparsable, or - for per-100g
 * macronutrients - implausible (outside 0-100g). Numeric values are rounded to 2 decimal places
 * to match the database column precision.
 *
 * @author Marius Birk
 */
public class FoodCsvRowMapper {

  private static final String COLUMN_PRODUCT_NAME = "product_name";
  private static final String COLUMN_BRANDS = "brands";
  private static final String COLUMN_CATEGORIES = "categories_en";
  private static final String COLUMN_ENERGY_KCAL = "energy-kcal_100g";
  private static final String COLUMN_ENERGY_KJ = "energy_100g";
  private static final String COLUMN_FAT = "fat_100g";
  private static final String COLUMN_SATURATED_FAT = "saturated-fat_100g";
  private static final String COLUMN_CARBOHYDRATES = "carbohydrates_100g";
  private static final String COLUMN_SUGAR = "sugars_100g";
  private static final String COLUMN_FIBER = "fiber_100g";
  private static final String COLUMN_PROTEIN = "proteins_100g";
  private static final String COLUMN_SALT = "salt_100g";
  private static final String COLUMN_SODIUM = "sodium_100g";
  private static final String COLUMN_IMAGE_URL = "image_url";
  private static final String COLUMN_CODE = "code";
  private static final String COLUMN_INGREDIENTS_ANALYSIS_TAGS = "ingredients_analysis_tags";

  private static final String TAG_VEGAN = "en:vegan";
  private static final String TAG_VEGETARIAN = "en:vegetarian";
  private static final String TAG_NON_VEGETARIAN = "en:non-vegetarian";

  private static final BigDecimal KCAL_PER_KJ = new BigDecimal("4.184");
  private static final BigDecimal FIXED_GRAMS = new BigDecimal("100");
  private static final int SCALE = 2;

  /**
   * Upper bound for a plausible {@code energy-kcal_100g} value. Pure fat/oil, the most
   * energy-dense food there is, tops out around 900 kcal per 100g - values above this are
   * malformed source data (e.g. a units mix-up), not real products.
   */
  private static final BigDecimal MAX_PLAUSIBLE_CALORIES = new BigDecimal("900");

  /**
   * Upper bound for a plausible per-100g macronutrient value: a nutrient can never weigh more
   * than the 100g of product it's measured in.
   */
  private static final BigDecimal MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("100");

  /**
   * Maps a single CSV record to a food item, if it satisfies the mandatory quality filter.
   *
   * @param record the parsed CSV row
   * @return the mapped food item, or empty if a required field is missing or unparsable
   */
  public Optional<FoodPersistence> map(CSVRecord record) {
    String name = trimToNull(get(record, COLUMN_PRODUCT_NAME));
    if (name == null) {
      return Optional.empty();
    }

    String brand = firstOf(get(record, COLUMN_BRANDS));
    if (brand == null) {
      return Optional.empty();
    }

    BigDecimal calory = calories(record);
    if (calory == null) {
      return Optional.empty();
    }

    return Optional.of(new FoodPersistence(
        UUID.randomUUID(),
        name,
        calory,
        FIXED_GRAMS,
        brand,
        firstOf(get(record, COLUMN_CATEGORIES)),
        macroGrams(record, COLUMN_FAT),
        macroGrams(record, COLUMN_SATURATED_FAT),
        macroGrams(record, COLUMN_CARBOHYDRATES),
        macroGrams(record, COLUMN_SUGAR),
        macroGrams(record, COLUMN_FIBER),
        macroGrams(record, COLUMN_PROTEIN),
        macroGrams(record, COLUMN_SALT),
        macroGrams(record, COLUMN_SODIUM),
        trimToNull(get(record, COLUMN_IMAGE_URL)),
        FoodSource.OPENFOODFACTS.name(),
        trimToNull(get(record, COLUMN_CODE)),
        diet(record).name()
    ));
  }

  /**
   * Derives the vegan/vegetarian diet compatibility from OpenFoodFacts's
   * {@code ingredients_analysis_tags} column, a comma-separated tag list that may contain up to
   * one vegan-related and one vegetarian-related tag (e.g. {@code en:vegan,en:vegetarian} or
   * {@code en:non-vegetarian,en:vegan-status-unknown}). Tags are matched exactly against the
   * split, trimmed list rather than via substring search: {@code en:non-vegan} and {@code
   * en:maybe-vegan} both textually contain "vegan" but must NOT be mistaken for {@code en:vegan}.
   * Any uncertainty (missing tag, {@code en:maybe-vegan}, {@code en:vegan-status-unknown}, ...)
   * deliberately falls through to {@link Diet#UNKNOWN} rather than guessing.
   *
   * @param record the parsed CSV row
   * @return the derived diet, or {@link Diet#UNKNOWN} if it can't be determined
   */
  private Diet diet(CSVRecord record) {
    String raw = trimToNull(get(record, COLUMN_INGREDIENTS_ANALYSIS_TAGS));
    if (raw == null) {
      return Diet.UNKNOWN;
    }
    List<String> tags = Arrays.stream(raw.split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .toList();
    if (tags.contains(TAG_VEGAN)) {
      return Diet.VEGAN;
    }
    if (tags.contains(TAG_VEGETARIAN)) {
      return Diet.VEGETARIAN;
    }
    if (tags.contains(TAG_NON_VEGETARIAN)) {
      return Diet.NON_VEGETARIAN;
    }
    return Diet.UNKNOWN;
  }

  private BigDecimal calories(CSVRecord record) {
    BigDecimal kcal = decimal(record, COLUMN_ENERGY_KCAL);
    if (kcal != null) {
      return isInRange(kcal, MAX_PLAUSIBLE_CALORIES) ? kcal : null;
    }
    BigDecimal kilojoule = decimal(record, COLUMN_ENERGY_KJ);
    if (kilojoule == null) {
      return null;
    }
    BigDecimal convertedKcal = kilojoule.divide(KCAL_PER_KJ, SCALE, RoundingMode.HALF_UP);
    return isInRange(convertedKcal, MAX_PLAUSIBLE_CALORIES) ? convertedKcal : null;
  }

  /**
   * Reads a per-100g macronutrient value, additionally discarding it (leaving the field
   * {@code null}) if it falls outside {@link #MAX_PLAUSIBLE_MACRO_GRAMS} - unlike an implausible
   * calorie value, a single bad macronutrient reading doesn't invalidate the rest of the row.
   *
   * @param record the parsed CSV row
   * @param column the name of the macronutrient column to read
   * @return the plausible value, or {@code null} if missing, unparsable or implausible
   */
  private BigDecimal macroGrams(CSVRecord record, String column) {
    BigDecimal value = decimal(record, column);
    if (value == null) {
      return null;
    }
    return isInRange(value, MAX_PLAUSIBLE_MACRO_GRAMS) ? value : null;
  }

  private boolean isInRange(BigDecimal value, BigDecimal max) {
    return value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(max) <= 0;
  }

  private BigDecimal decimal(CSVRecord record, String column) {
    String raw = trimToNull(get(record, column));
    if (raw == null) {
      return null;
    }
    try {
      return new BigDecimal(raw).setScale(SCALE, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String firstOf(String commaSeparatedValues) {
    String trimmed = trimToNull(commaSeparatedValues);
    if (trimmed == null) {
      return null;
    }
    return trimToNull(trimmed.split(",", 2)[0]);
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String get(CSVRecord record, String column) {
    return record.isMapped(column) ? record.get(column) : null;
  }
}
