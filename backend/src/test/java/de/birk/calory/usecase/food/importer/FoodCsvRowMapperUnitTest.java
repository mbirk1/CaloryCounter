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
import de.birk.calory.domain.food.FoodSource;

public class FoodCsvRowMapperUnitTest {

  private static final String HEADER = "code\tproduct_name\tbrands\tcategories_en\t"
      + "energy-kcal_100g\tenergy_100g\tfat_100g\tsaturated-fat_100g\tcarbohydrates_100g\t"
      + "sugars_100g\tfiber_100g\tproteins_100g\tsalt_100g\tsodium_100g\timage_url";

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

  private CSVRecord parseRow(String row) throws IOException {
    String csv = HEADER + "\n" + row;
    try (CSVParser parser = CSVFormat.DEFAULT
        .withDelimiter('\t')
        .withFirstRecordAsHeader()
        .withIgnoreHeaderCase()
        .withTrim()
        .parse(new StringReader(csv))) {
      return parser.iterator().next();
    }
  }
}
