package de.birk.calory.usecase.food.importer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.FoodSource;

/**
 * Pure mapping logic from a single Bundeslebensmittelschlüssel (BLS) 4.0 spreadsheet row to a
 * persistable food item.
 *
 * <p>A row is only mapped if the mandatory quality filter is satisfied: a non-blank name and a
 * plausible calory value (0-900 kcal per 100g). All other fields are optional and simply left
 * {@code null} when missing, unparsable, a known non-numeric placeholder ({@code -}, {@code TR},
 * {@code <LOD}, {@code <LOQ}, {@code <LOD or <LOQ}) or - for macro-/micronutrients - implausible.
 * Every BLS value is already normalized to 100g, so {@code grams} is simply fixed at 100.
 *
 * <p>{@code NA Natrium} is given in mg/100g, but the existing {@code sodium} field's established
 * unit (matching the OpenFoodFacts-derived {@code sodium_100g}) is grams - the raw mg value is
 * therefore divided by 1000 before being stored, so sodium stays comparable regardless of which
 * source a food item was imported from.
 *
 * @author Marius Birk
 */
public class BlsFoodRowMapper {

  private static final int COLUMN_BLS_CODE = 0;
  private static final int COLUMN_NAME = 1;
  private static final int COLUMN_CALORY = 6;
  private static final int COLUMN_PROTEIN = 12;
  private static final int COLUMN_FAT = 15;
  private static final int COLUMN_CARBOHYDRATES = 18;
  private static final int COLUMN_FIBER = 21;
  private static final int COLUMN_VITAMIN_A = 33;
  private static final int COLUMN_VITAMIN_D = 48;
  private static final int COLUMN_FOLATE = 105;
  private static final int COLUMN_VITAMIN_E = 57;
  private static final int COLUMN_VITAMIN_K = 75;
  private static final int COLUMN_VITAMIN_B1 = 84;
  private static final int COLUMN_VITAMIN_B2 = 87;
  private static final int COLUMN_VITAMIN_B6 = 99;
  private static final int COLUMN_VITAMIN_B12 = 114;
  private static final int COLUMN_VITAMIN_C = 117;
  private static final int COLUMN_SALT = 120;
  private static final int COLUMN_SODIUM_MILLIGRAMS = 123;
  private static final int COLUMN_POTASSIUM = 129;
  private static final int COLUMN_CALCIUM = 132;
  private static final int COLUMN_MAGNESIUM = 135;
  private static final int COLUMN_IRON = 144;
  private static final int COLUMN_ZINC = 147;
  private static final int COLUMN_IODINE = 150;
  private static final int COLUMN_SUGAR = 219;
  private static final int COLUMN_SATURATED_FAT = 246;

  private static final Set<String> NON_NUMERIC_PLACEHOLDERS =
      Set.of("-", "TR", "<LOD", "<LOQ", "<LOD or <LOQ");

  private static final BigDecimal FIXED_GRAMS = new BigDecimal("100");
  private static final BigDecimal MILLIGRAMS_PER_GRAM = new BigDecimal("1000");
  private static final int MACRO_SCALE = 2;
  private static final int MICRO_SCALE = 3;

  /**
   * Upper bound for a plausible {@code ENERCC} (calory) value. Pure fat/oil, the most
   * energy-dense food there is, tops out around 900 kcal per 100g - values above this are
   * malformed source data, not real foods.
   */
  private static final BigDecimal MAX_PLAUSIBLE_CALORIES = new BigDecimal("900");

  /**
   * Upper bound for a plausible per-100g macronutrient value: a nutrient can never weigh more
   * than the 100g of food it's measured in.
   */
  private static final BigDecimal MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("100");

  // Micronutrient upper bounds, mirroring Food#validatePlausibility() - kept as a second copy
  // here rather than shared, matching the established duplication between Food and
  // FoodCsvRowMapper's own calory/macro bounds: the import path talks directly to
  // FoodPersistence, never constructing a Food domain object, so there is nothing to delegate to.
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

  // Word-boundary based (not substring) so e.g. "Ei" doesn't false-positive-match inside "Eis" -
  // Pattern.UNICODE_CHARACTER_CLASS makes \b treat German umlauts as word characters too, so a
  // keyword like "Hähnchen" gets a correct boundary around the whole word.
  private static final Pattern MEAT_OR_FISH_KEYWORDS = wordBoundaryPattern(List.of(
      "Fleisch", "Wurst", "Schinken", "Speck", "Fisch", "Hering", "Lachs", "Huhn", "Hähnchen",
      "Rind", "Schwein", "Kalb", "Lamm", "Ente", "Pute", "Wild", "Garnele", "Krabbe", "Muschel"));
  private static final Pattern DAIRY_EGG_OR_HONEY_KEYWORDS = wordBoundaryPattern(List.of(
      "Milch", "Käse", "Sahne", "Rahm", "Joghurt", "Quark", "Butter", "Ei", "Eier", "Honig"));

  /**
   * Maps a single BLS spreadsheet row to a food item, if it satisfies the mandatory quality
   * filter.
   *
   * @param row the row to map, expected to follow the BLS 4.0 column layout
   * @return the mapped food item, or empty if a required field is missing or unparsable
   */
  public Optional<FoodPersistence> map(Row row) {
    String name = trimToNull(stringValue(row, COLUMN_NAME));
    if (name == null) {
      return Optional.empty();
    }

    BigDecimal calory = decimal(row, COLUMN_CALORY, MACRO_SCALE);
    if (calory == null || !isInRange(calory, MAX_PLAUSIBLE_CALORIES)) {
      return Optional.empty();
    }

    String blsCode = trimToNull(stringValue(row, COLUMN_BLS_CODE));

    return Optional.of(new FoodPersistence.Builder()
        .id(UUID.randomUUID())
        .name(name)
        .calory(calory)
        .grams(FIXED_GRAMS)
        .fat(macroGrams(row, COLUMN_FAT))
        .saturatedFat(macroGrams(row, COLUMN_SATURATED_FAT))
        .carbohydrates(macroGrams(row, COLUMN_CARBOHYDRATES))
        .sugar(macroGrams(row, COLUMN_SUGAR))
        .fiber(macroGrams(row, COLUMN_FIBER))
        .protein(macroGrams(row, COLUMN_PROTEIN))
        .salt(macroGrams(row, COLUMN_SALT))
        .sodium(sodiumGrams(row))
        .source(FoodSource.BLS.name())
        .externalId(blsCode)
        .diet(deriveDiet(blsCode, name).name())
        .vitaminA(micro(row, COLUMN_VITAMIN_A, MAX_VITAMIN_A_MICROGRAMS))
        .vitaminD(micro(row, COLUMN_VITAMIN_D, MAX_VITAMIN_D_MICROGRAMS))
        .vitaminE(micro(row, COLUMN_VITAMIN_E, MAX_VITAMIN_E_MILLIGRAMS))
        .vitaminK(micro(row, COLUMN_VITAMIN_K, MAX_VITAMIN_K_MICROGRAMS))
        .vitaminB1(micro(row, COLUMN_VITAMIN_B1, MAX_VITAMIN_B1_MILLIGRAMS))
        .vitaminB2(micro(row, COLUMN_VITAMIN_B2, MAX_VITAMIN_B2_MILLIGRAMS))
        .vitaminB6(micro(row, COLUMN_VITAMIN_B6, MAX_VITAMIN_B6_MICROGRAMS))
        .vitaminB12(micro(row, COLUMN_VITAMIN_B12, MAX_VITAMIN_B12_MICROGRAMS))
        .vitaminC(micro(row, COLUMN_VITAMIN_C, MAX_VITAMIN_C_MILLIGRAMS))
        .folate(micro(row, COLUMN_FOLATE, MAX_FOLATE_MICROGRAMS))
        .potassium(micro(row, COLUMN_POTASSIUM, MAX_POTASSIUM_MILLIGRAMS))
        .calcium(micro(row, COLUMN_CALCIUM, MAX_CALCIUM_MILLIGRAMS))
        .magnesium(micro(row, COLUMN_MAGNESIUM, MAX_MAGNESIUM_MILLIGRAMS))
        .iron(micro(row, COLUMN_IRON, MAX_IRON_MILLIGRAMS))
        .zinc(micro(row, COLUMN_ZINC, MAX_ZINC_MILLIGRAMS))
        .iodine(micro(row, COLUMN_IODINE, MAX_IODINE_MICROGRAMS))
        .build());
  }

  /**
   * Derives the vegan/vegetarian diet compatibility in two stages: first a name-keyword match
   * (meat/fish takes priority over dairy/egg/honey), then - only if no keyword matched - a
   * fallback based on the BLS code's leading letter, which encodes the food group.
   *
   * @param blsCode the row's BLS code, used only for its leading letter
   * @param name the row's food name
   * @return the derived diet, or {@link Diet#UNKNOWN} if neither signal is conclusive
   */
  private Diet deriveDiet(String blsCode, String name) {
    if (MEAT_OR_FISH_KEYWORDS.matcher(name).find()) {
      return Diet.NON_VEGETARIAN;
    }
    if (DAIRY_EGG_OR_HONEY_KEYWORDS.matcher(name).find()) {
      return Diet.VEGETARIAN;
    }
    return dietFromFoodGroup(blsCode);
  }

  /**
   * Falls back to the food group encoded by the first letter of the BLS code. Groups that mix
   * plant and animal ingredients too freely to classify from the group alone (backwaren,
   * fertiggerichte, suppen, getraenke, gewuerze, ...) deliberately fall through to
   * {@link Diet#UNKNOWN} rather than guessing.
   *
   * @param blsCode the row's BLS code, or {@code null}
   * @return the group-derived diet, or {@link Diet#UNKNOWN}
   */
  private Diet dietFromFoodGroup(String blsCode) {
    if (blsCode == null || blsCode.isEmpty()) {
      return Diet.UNKNOWN;
    }
    return switch (Character.toUpperCase(blsCode.charAt(0))) {
      case 'T', 'U', 'V', 'W' -> Diet.NON_VEGETARIAN;
      case 'M' -> Diet.VEGETARIAN;
      case 'F', 'G', 'C', 'K' -> Diet.VEGAN;
      default -> Diet.UNKNOWN;
    };
  }

  /**
   * Reads a per-100g macronutrient value, additionally discarding it (leaving the field
   * {@code null}) if it falls outside {@link #MAX_PLAUSIBLE_MACRO_GRAMS} - unlike an implausible
   * calorie value, a single bad macronutrient reading doesn't invalidate the rest of the row.
   *
   * @param row the row to read from
   * @param column the column index to read
   * @return the plausible value, or {@code null} if missing, unparsable or implausible
   */
  private BigDecimal macroGrams(Row row, int column) {
    BigDecimal value = decimal(row, column, MACRO_SCALE);
    if (value == null) {
      return null;
    }
    return isInRange(value, MAX_PLAUSIBLE_MACRO_GRAMS) ? value : null;
  }

  /**
   * Reads a micronutrient value against its own plausible upper bound.
   *
   * @param row the row to read from
   * @param column the column index to read
   * @param max the plausible upper bound for this specific micronutrient
   * @return the plausible value, or {@code null} if missing, unparsable or implausible
   */
  private BigDecimal micro(Row row, int column, BigDecimal max) {
    BigDecimal value = decimal(row, column, MICRO_SCALE);
    if (value == null) {
      return null;
    }
    return isInRange(value, max) ? value : null;
  }

  /**
   * Reads {@code NA Natrium}, given in mg/100g, and converts it to grams to match the existing
   * {@code sodium} field's established unit.
   *
   * @param row the row to read from
   * @return the plausible value in grams, or {@code null} if missing, unparsable or implausible
   */
  private BigDecimal sodiumGrams(Row row) {
    BigDecimal milligrams = rawDecimal(row, COLUMN_SODIUM_MILLIGRAMS);
    if (milligrams == null) {
      return null;
    }
    BigDecimal grams = milligrams.divide(MILLIGRAMS_PER_GRAM, MACRO_SCALE, RoundingMode.HALF_UP);
    return isInRange(grams, MAX_PLAUSIBLE_MACRO_GRAMS) ? grams : null;
  }

  private boolean isInRange(BigDecimal value, BigDecimal max) {
    return value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(max) <= 0;
  }

  private BigDecimal decimal(Row row, int column, int scale) {
    BigDecimal raw = rawDecimal(row, column);
    return raw == null ? null : raw.setScale(scale, RoundingMode.HALF_UP);
  }

  /**
   * Reads a cell as a number, treating a known non-numeric placeholder ({@code -}, {@code TR},
   * {@code <LOD}, {@code <LOQ}, {@code <LOD or <LOQ}) or any other unparsable text the same way
   * as a missing value - {@code null}, never a thrown exception that would invalidate the row.
   *
   * @param row the row to read from
   * @param column the column index to read
   * @return the raw, unrounded numeric value, or {@code null} if missing/unparsable
   */
  private BigDecimal rawDecimal(Row row, int column) {
    Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      return BigDecimal.valueOf(cell.getNumericCellValue());
    }
    if (cell.getCellType() == CellType.STRING) {
      String raw = trimToNull(cell.getStringCellValue());
      if (raw == null || NON_NUMERIC_PLACEHOLDERS.contains(raw)) {
        return null;
      }
      try {
        return new BigDecimal(raw.replace(',', '.'));
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  private String stringValue(Row row, int column) {
    Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
    }
    return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : null;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static Pattern wordBoundaryPattern(List<String> keywords) {
    String alternation = keywords.stream().map(Pattern::quote).collect(Collectors.joining("|"));
    return Pattern.compile(
        "\\b(?:" + alternation + ")\\b",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
  }
}
