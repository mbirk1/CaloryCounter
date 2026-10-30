package de.birk.calory.usecase.food.importer;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.FoodSource;

public class BlsFoodRowMapperUnitTest {

  private static final int COL_BLS_CODE = 0;
  private static final int COL_NAME = 1;
  private static final int COL_CALORY = 6;
  private static final int COL_PROTEIN = 12;
  private static final int COL_FAT = 15;
  private static final int COL_CARBOHYDRATES = 18;
  private static final int COL_FIBER = 21;
  private static final int COL_VITAMIN_A = 33;
  private static final int COL_VITAMIN_D = 48;
  private static final int COL_VITAMIN_E = 57;
  private static final int COL_VITAMIN_K = 75;
  private static final int COL_VITAMIN_B1 = 84;
  private static final int COL_VITAMIN_B2 = 87;
  private static final int COL_VITAMIN_B6 = 99;
  private static final int COL_FOLATE = 105;
  private static final int COL_VITAMIN_B12 = 114;
  private static final int COL_VITAMIN_C = 117;
  private static final int COL_SALT = 120;
  private static final int COL_SODIUM_MG = 123;
  private static final int COL_POTASSIUM = 129;
  private static final int COL_CALCIUM = 132;
  private static final int COL_MAGNESIUM = 135;
  private static final int COL_IRON = 144;
  private static final int COL_ZINC = 147;
  private static final int COL_IODINE = 150;
  private static final int COL_SUGAR = 219;
  private static final int COL_SATURATED_FAT = 246;

  private final BlsFoodRowMapper mapper = new BlsFoodRowMapper();

  @Test
  public void mapsAValidRowWithAllMacrosAndMicronutrientsTest() {
    // Arrange
    Row row = row(Map.ofEntries(
        entry(COL_BLS_CODE, "C131000"),
        entry(COL_NAME, "Hafer ganzes Korn, roh"),
        entry(COL_CALORY, 343.0),
        entry(COL_PROTEIN, 11.38),
        entry(COL_FAT, 7.09),
        entry(COL_SATURATED_FAT, 1.22),
        entry(COL_CARBOHYDRATES, 53.7),
        entry(COL_SUGAR, 0.8),
        entry(COL_FIBER, 9.3),
        entry(COL_SALT, 0.01),
        entry(COL_VITAMIN_C, 0.0),
        entry(COL_CALCIUM, 58.0),
        entry(COL_IRON, 4.7)
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    FoodPersistence food = result.get();
    assertThat(food.getName()).isEqualTo("Hafer ganzes Korn, roh");
    assertThat(food.getExternalId()).isEqualTo("C131000");
    assertThat(food.getCalory()).isEqualByComparingTo("343.00");
    assertThat(food.getGrams()).isEqualByComparingTo("100");
    assertThat(food.getProtein()).isEqualByComparingTo("11.38");
    assertThat(food.getFat()).isEqualByComparingTo("7.09");
    assertThat(food.getSaturatedFat()).isEqualByComparingTo("1.22");
    assertThat(food.getCarbohydrates()).isEqualByComparingTo("53.70");
    assertThat(food.getSugar()).isEqualByComparingTo("0.80");
    assertThat(food.getFiber()).isEqualByComparingTo("9.30");
    assertThat(food.getSalt()).isEqualByComparingTo("0.01");
    assertThat(food.getCalcium()).isEqualByComparingTo("58.000");
    assertThat(food.getIron()).isEqualByComparingTo("4.700");
    assertThat(food.getBrand()).isNull();
    assertThat(food.getCategory()).isNull();
    assertThat(food.getSource()).isEqualTo(FoodSource.BLS.name());
  }

  @Test
  public void convertsSodiumFromMilligramsToGramsTest() {
    // Arrange - NA Natrium is given in mg/100g, but the established `sodium` field unit is
    // grams (matching OpenFoodFacts' sodium_100g)
    Row row = row(Map.of(
        COL_NAME, "Nitritpökelsalz",
        COL_CALORY, 0.0,
        COL_SODIUM_MG, 3960.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getSodium()).isEqualByComparingTo("3.96");
  }

  @Test
  public void rejectsRowWithBlankNameTest() {
    // Arrange
    Row row = row(Map.of(COL_NAME, "   ", COL_CALORY, 50.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithMissingCaloryTest() {
    // Arrange - no COL_CALORY entry at all
    Row row = row(Map.of(COL_NAME, "Apfel"));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithImplausiblyHighCaloryTest() {
    // Arrange
    Row row = row(Map.of(COL_NAME, "Reines Fett", COL_CALORY, 950.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void acceptsCaloriesAtThePlausibleBoundariesTest() {
    // Arrange - 0 and 900 are both inclusive boundaries of the plausible range
    Row zeroRow = row(Map.of(COL_NAME, "Wasser", COL_CALORY, 0.0));
    Row maxRow = row(Map.of(COL_NAME, "Öl", COL_CALORY, 900.0));

    // Act
    Optional<FoodPersistence> zeroResult = mapper.map(zeroRow);
    Optional<FoodPersistence> maxResult = mapper.map(maxRow);

    // Assert
    assertThat(zeroResult).isPresent();
    assertThat(zeroResult.get().getCalory()).isEqualByComparingTo("0.00");
    assertThat(maxResult).isPresent();
    assertThat(maxResult.get().getCalory()).isEqualByComparingTo("900.00");
  }

  @Test
  public void treatsKnownPlaceholdersAsMissingValueRatherThanRejectingTheRowTest() {
    // Arrange & Act & Assert - one @Test method looping over every known placeholder, rather
    // than a @ParameterizedTest per placeholder: this codebase's ArchUnit rule requires every
    // "*Test"-named method to carry @Test, which @ParameterizedTest does not satisfy.
    for (String placeholder : List.of("-", "TR", "<LOD", "<LOQ", "<LOD or <LOQ")) {
      Row row = row(Map.of(
          COL_NAME, "Testprodukt",
          COL_CALORY, 50.0,
          COL_VITAMIN_C, placeholder
      ));

      Optional<FoodPersistence> result = mapper.map(row);

      assertThat(result).as("placeholder: " + placeholder).isPresent();
      assertThat(result.get().getVitaminC()).as("placeholder: " + placeholder).isNull();
    }
  }

  @Test
  public void nullsOutASingleImplausibleMicronutrientWithoutRejectingTheRowTest() {
    // Arrange - Ascorbinsäure (E 300), a near-pure vitamin C additive, really does carry
    // ~100000mg/100g in BLS - implausible for the plausibility filter by design, but the rest
    // of the row (name, calory) is still valid and must still be imported.
    Row row = row(Map.of(
        COL_NAME, "Ascorbinsäure (E 300)",
        COL_CALORY, 0.0,
        COL_VITAMIN_C, 100000.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getVitaminC()).isNull();
    assertThat(result.get().getName()).isEqualTo("Ascorbinsäure (E 300)");
  }

  @Test
  public void treatsUnparsableTextCellsAsMissingValueTest() {
    // Arrange
    Row row = row(Map.of(
        COL_NAME, "Testprodukt",
        COL_CALORY, 50.0,
        COL_FAT, "nicht bestimmbar"
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getFat()).isNull();
  }

  @Test
  public void derivesNonVegetarianDietFromAMeatKeywordTest() {
    // Arrange - group letter deliberately left as an unrelated group, proving the keyword (not
    // the group fallback) drove the classification
    Row row = row(Map.of(
        COL_BLS_CODE, "X999999",
        COL_NAME, "Schwein Kotelett, roh",
        COL_CALORY, 200.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.NON_VEGETARIAN.name());
  }

  @Test
  public void derivesVegetarianDietFromADairyKeywordTest() {
    // Arrange
    Row row = row(Map.of(
        COL_BLS_CODE, "X999999",
        COL_NAME, "Käse Gouda, mittelalt",
        COL_CALORY, 350.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.VEGETARIAN.name());
  }

  @Test
  public void nameKeywordDoesNotFalsePositiveMatchAsASubstringTest() {
    // Arrange - "Ei" must not match inside "Eis" (the ticket's own example); the group fallback
    // (a made-up, deliberately unmapped group letter) then correctly falls through to UNKNOWN.
    Row row = row(Map.of(
        COL_BLS_CODE, "Y123456",
        COL_NAME, "Schoko Eis",
        COL_CALORY, 220.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  @Test
  public void nameKeywordTakesPriorityOverFoodGroupFallbackTest() {
    // Arrange - BLS code group M would default to VEGETARIAN via the group fallback, but a meat
    // keyword in the name must win
    Row row = row(Map.of(
        COL_BLS_CODE, "M123456",
        COL_NAME, "Schinken Wurst Aufschnitt",
        COL_CALORY, 250.0
    ));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.NON_VEGETARIAN.name());
  }

  @Test
  public void fallsBackToNonVegetarianForMeatFishFoodGroupsTest() {
    // Arrange
    Row row = row(Map.of(COL_BLS_CODE, "T500000", COL_NAME, "Testprodukt", COL_CALORY, 150.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.NON_VEGETARIAN.name());
  }

  @Test
  public void fallsBackToVegetarianForTheDairyFoodGroupTest() {
    // Arrange
    Row row = row(Map.of(COL_BLS_CODE, "M100000", COL_NAME, "Testprodukt", COL_CALORY, 60.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.VEGETARIAN.name());
  }

  @Test
  public void fallsBackToVeganForPlantFoodGroupsTest() {
    // Arrange
    Row row = row(Map.of(COL_BLS_CODE, "F100000", COL_NAME, "Testprodukt", COL_CALORY, 52.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.VEGAN.name());
  }

  @Test
  public void fallsBackToUnknownForMixedFoodGroupsTest() {
    // Arrange - e.g. "B" (Backwaren) mixes plant and animal ingredients too freely to classify
    // from the group letter alone
    Row row = row(Map.of(COL_BLS_CODE, "B100000", COL_NAME, "Testprodukt", COL_CALORY, 300.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  @Test
  public void defaultsToUnknownDietWhenBlsCodeIsMissingAndNoKeywordMatchesTest() {
    // Arrange
    Row row = row(Map.of(COL_NAME, "Testprodukt", COL_CALORY, 100.0));

    // Act
    Optional<FoodPersistence> result = mapper.map(row);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  /**
   * Builds a single-row in-memory workbook sheet, setting cells only at the given column
   * indices - every other of the BLS format's 418 columns is left absent, matching how a real
   * sheet often omits trailing empty cells.
   */
  private Row row(Map<Integer, Object> cellValues) {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet();
    sheet.createRow(0); // header row, skipped by the mapper's caller, not by map() itself
    Row row = sheet.createRow(1);
    cellValues.forEach((column, value) -> {
      Cell cell = row.createCell(column);
      if (value instanceof Number number) {
        cell.setCellValue(number.doubleValue());
      } else {
        cell.setCellValue(String.valueOf(value));
      }
    });
    return row;
  }
}
