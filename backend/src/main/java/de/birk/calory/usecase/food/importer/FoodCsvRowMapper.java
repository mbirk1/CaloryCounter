package de.birk.calory.usecase.food.importer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.csv.CSVRecord;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.FoodSource;

/**
 * Pure mapping logic from a single OpenFoodFacts CSV row to a persistable food item.
 *
 * <p>A row is only mapped if the mandatory quality filter is satisfied: a non-blank product
 * name, a non-blank brand and either {@code energy-kcal_100g} or a convertible
 * {@code energy_100g} (kJ) value. All other fields are optional and simply left {@code null}
 * when missing. Numeric values are rounded to 2 decimal places to match the database column
 * precision.
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

  private static final BigDecimal KCAL_PER_KJ = new BigDecimal("4.184");
  private static final BigDecimal FIXED_GRAMS = new BigDecimal("100");
  private static final int SCALE = 2;

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
        decimal(record, COLUMN_FAT),
        decimal(record, COLUMN_SATURATED_FAT),
        decimal(record, COLUMN_CARBOHYDRATES),
        decimal(record, COLUMN_SUGAR),
        decimal(record, COLUMN_FIBER),
        decimal(record, COLUMN_PROTEIN),
        decimal(record, COLUMN_SALT),
        decimal(record, COLUMN_SODIUM),
        trimToNull(get(record, COLUMN_IMAGE_URL)),
        FoodSource.OPENFOODFACTS.name(),
        trimToNull(get(record, COLUMN_CODE))
    ));
  }

  private BigDecimal calories(CSVRecord record) {
    BigDecimal kcal = decimal(record, COLUMN_ENERGY_KCAL);
    if (kcal != null) {
      return kcal;
    }
    BigDecimal kilojoule = decimal(record, COLUMN_ENERGY_KJ);
    if (kilojoule == null) {
      return null;
    }
    return kilojoule.divide(KCAL_PER_KJ, SCALE, RoundingMode.HALF_UP);
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
