package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.FoodSource;

public class FoodCsvRowMapperUnitTest {

  private static final String HEADER = "code\tproduct_name\tbrands\tcategories_en\t"
      + "energy-kcal_100g\tenergy_100g\tfat_100g\tsaturated-fat_100g\tcarbohydrates_100g\t"
      + "sugars_100g\tfiber_100g\tproteins_100g\tsalt_100g\tsodium_100g\timage_url";

  private static final String HEADER_WITH_DIET = HEADER + "\tingredients_analysis_tags";

  private final FoodCsvRowMapper mapper = new FoodCsvRowMapper();

  @Test
  public void mapsAValidRowTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "1234567890123\tCola\tAcme, Other Brand\tBeverages, Soft Drinks\t42\t175.7\t0\t0\t"
            + "10.6\t10.6\t0\t0\t0.01\t0\thttps://example.com/cola.png"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    FoodPersistence food = result.get();
    assertThat(food.getName()).isEqualTo("Cola");
    assertThat(food.getBrand()).isEqualTo("Acme");
    assertThat(food.getCategory()).isEqualTo("Beverages");
    assertThat(food.getCalory()).isEqualByComparingTo("42.00");
    assertThat(food.getGrams()).isEqualByComparingTo("100");
    assertThat(food.getFat()).isEqualByComparingTo("0.00");
    assertThat(food.getSaturatedFat()).isEqualByComparingTo("0.00");
    assertThat(food.getCarbohydrates()).isEqualByComparingTo("10.60");
    assertThat(food.getSugar()).isEqualByComparingTo("10.60");
    assertThat(food.getFiber()).isEqualByComparingTo("0.00");
    assertThat(food.getProtein()).isEqualByComparingTo("0.00");
    assertThat(food.getSalt()).isEqualByComparingTo("0.01");
    assertThat(food.getSodium()).isEqualByComparingTo("0.00");
    assertThat(food.getImageUrl()).isEqualTo("https://example.com/cola.png");
    assertThat(food.getSource()).isEqualTo(FoodSource.OPENFOODFACTS.name());
    assertThat(food.getExternalId()).isEqualTo("1234567890123");
    assertThat(food.getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  @Test
  public void rejectsRowWithBlankProductNameTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "123\t \tAcme\tBeverages\t42\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithMissingCaloriesTest() throws IOException {
    // Arrange - neither energy-kcal_100g nor energy_100g are set
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithMissingBrandTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "123\tCola\t\tBeverages\t42\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void fallsBackToKilojoulesWhenKcalIsMissingTest() throws IOException {
    // Arrange - 418.4 kJ / 4.184 = 100.00 kcal
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t\t418.4\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getCalory()).isEqualByComparingTo("100.00");
  }

  @Test
  public void roundsCaloriesToTwoDecimalPlacesTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t52.6789\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getCalory()).isEqualByComparingTo("52.68");
  }

  @Test
  public void treatsUnparsableCaloriesAsMissingTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\tnot-a-number\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithImplausiblyHighCaloriesTest() throws IOException {
    // Arrange - 10447.76 kcal/100g is physically impossible, malformed source data
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t10447.76\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void rejectsRowWithNegativeCaloriesTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t-1\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  public void acceptsCaloriesAtThePlausibleBoundariesTest() throws IOException {
    // Arrange - 0 and 900 are both inclusive boundaries of the plausible range
    CSVRecord zeroCalories = parseRow(
        "123\tWater\tAcme\tBeverages\t0\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );
    CSVRecord maxCalories = parseRow(
        "124\tOil\tAcme\tOils\t900\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> zeroResult = mapper.map(zeroCalories);
    Optional<FoodPersistence> maxResult = mapper.map(maxCalories);

    // Assert
    assertThat(zeroResult).isPresent();
    assertThat(zeroResult.get().getCalory()).isEqualByComparingTo("0.00");
    assertThat(maxResult).isPresent();
    assertThat(maxResult.get().getCalory()).isEqualByComparingTo("900.00");
  }

  @Test
  public void nullsOutASingleImplausibleMacroWithoutRejectingTheRowTest() throws IOException {
    // Arrange - fat_100g of 140 is implausible (can't exceed the 100g the product is measured
    // in), but the rest of the row (name, brand, calories) is still valid.
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t42\t\t140\t0\t10.6\t10.6\t0\t0\t0.01\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    FoodPersistence food = result.get();
    assertThat(food.getCalory()).isEqualByComparingTo("42.00");
    assertThat(food.getFat()).isNull();
    assertThat(food.getCarbohydrates()).isEqualByComparingTo("10.60");
  }

  @Test
  public void leavesOptionalFieldsNullWhenBlankTest() throws IOException {
    // Arrange
    CSVRecord record = parseRow(
        "\tCola\tAcme\t\t42\t\t\t\t\t\t\t\t\t\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    FoodPersistence food = result.get();
    assertThat(food.getExternalId()).isNull();
    assertThat(food.getCategory()).isNull();
    assertThat(food.getFat()).isNull();
    assertThat(food.getImageUrl()).isNull();
  }

  @Test
  public void parsesAProductNameWithAnEmbeddedQuoteCharacterAsLiteralTextTest()
      throws IOException {
    // Arrange - a `"` in free text (e.g. an inch mark) is not CSV field encapsulation in this
    // raw, unescaped TSV export; the row must parse normally instead of crashing the tokenizer.
    CSVRecord record = parseRow(
        "123\t12\" Pizza Margherita\tAcme\tFrozen Food\t250\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("12\" Pizza Margherita");
  }

  @Test
  public void derivesVeganDietFromIngredientsAnalysisTagsTest() throws IOException {
    // Arrange - the tag list may carry both a vegan and a vegetarian tag together
    CSVRecord record = parseRowWithDiet(
        "123\tTofu\tAcme\tPlant-based\t120\t\t5\t1\t2\t1\t2\t12\t0.5\t0.2\t\ten:vegan,en:vegetarian"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.VEGAN.name());
  }

  @Test
  public void derivesVegetarianDietWhenNotVeganTest() throws IOException {
    // Arrange - "en:non-vegan" textually contains "vegan" but must NOT match TAG_VEGAN
    CSVRecord record = parseRowWithDiet(
        "123\tCheese\tAcme\tDairy\t350\t\t28\t18\t2\t1\t0\t22\t1.5\t0.6\t\t"
            + "en:non-vegan,en:vegetarian"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.VEGETARIAN.name());
  }

  @Test
  public void derivesNonVegetarianDietTest() throws IOException {
    // Arrange
    CSVRecord record = parseRowWithDiet(
        "123\tChicken Breast\tAcme\tMeat\t165\t\t3.6\t1\t0\t0\t0\t31\t0.1\t0.07\t\t"
            + "en:non-vegetarian,en:non-vegan"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.NON_VEGETARIAN.name());
  }

  @Test
  public void defaultsToUnknownDietWhenTagsAreAmbiguousTest() throws IOException {
    // Arrange - "maybe-vegan"/"vegan-status-unknown" are genuine uncertainty, not a real answer
    CSVRecord record = parseRowWithDiet(
        "123\tMystery Snack\tAcme\tSnacks\t250\t\t5\t1\t30\t10\t2\t5\t0.5\t0.2\t\t"
            + "en:maybe-vegan,en:vegan-status-unknown"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  @Test
  public void defaultsToUnknownDietWhenTagsColumnIsMissingTest() throws IOException {
    // Arrange - the plain HEADER (used everywhere else in this test class) has no
    // ingredients_analysis_tags column at all
    CSVRecord record = parseRow(
        "123\tCola\tAcme\tBeverages\t42\t\t0\t0\t0\t0\t0\t0\t0\t0\t"
    );

    // Act
    Optional<FoodPersistence> result = mapper.map(record);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getDiet()).isEqualTo(Diet.UNKNOWN.name());
  }

  private CSVRecord parseRowWithDiet(String row) throws IOException {
    String csv = HEADER_WITH_DIET + "\n" + row;
    try (CSVParser parser = CSVFormat.DEFAULT
        .withDelimiter('\t')
        .withQuote(null)
        .withFirstRecordAsHeader()
        .withIgnoreHeaderCase()
        .withTrim()
        .parse(new StringReader(csv))) {
      return parser.iterator().next();
    }
  }

  private CSVRecord parseRow(String row) throws IOException {
    String csv = HEADER + "\n" + row;
    // Mirrors FoodCsvImportUsecase's parser config exactly, including the disabled quote
    // character - the OpenFoodFacts export is a raw TSV without CSV-style quote-escaping, so a
    // `"` in free text must parse as a literal character, not a field encapsulation marker.
    try (CSVParser parser = CSVFormat.DEFAULT
        .withDelimiter('\t')
        .withQuote(null)
        .withFirstRecordAsHeader()
        .withIgnoreHeaderCase()
        .withTrim()
        .parse(new StringReader(csv))) {
      return parser.iterator().next();
    }
  }
}
