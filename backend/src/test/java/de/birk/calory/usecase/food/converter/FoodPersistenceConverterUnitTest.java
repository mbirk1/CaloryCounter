package de.birk.calory.usecase.food.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;

public class FoodPersistenceConverterUnitTest {

  @Test
  public void convertFromDtoWithExtendedFieldsTest() {
    // Arrange
    FoodPersistence persistence = new FoodPersistence(
        UUID.randomUUID(),
        "Cola",
        new BigDecimal("42.00"),
        new BigDecimal("100"),
        "Acme",
        "Beverages",
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("10.60"),
        new BigDecimal("10.60"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.01"),
        new BigDecimal("0.00"),
        "https://example.com/cola.png",
        "OPENFOODFACTS",
        "1234567890123"
    );
    FoodPersistenceConverter converter = new FoodPersistenceConverter();

    // Act
    Food result = converter.convertFromDto(persistence);

    // Assert
    assertThat(result.getBrand()).isEqualTo("Acme");
    assertThat(result.getCategory()).isEqualTo("Beverages");
    assertThat(result.getFat()).isEqualByComparingTo("0.00");
    assertThat(result.getSaturatedFat()).isEqualByComparingTo("0.00");
    assertThat(result.getCarbohydrates()).isEqualByComparingTo("10.60");
    assertThat(result.getSugar()).isEqualByComparingTo("10.60");
    assertThat(result.getFiber()).isEqualByComparingTo("0.00");
    assertThat(result.getProtein()).isEqualByComparingTo("0.00");
    assertThat(result.getSalt()).isEqualByComparingTo("0.01");
    assertThat(result.getSodium()).isEqualByComparingTo("0.00");
    assertThat(result.getImageUrl()).isEqualTo("https://example.com/cola.png");
    assertThat(result.getSource()).isEqualTo(FoodSource.OPENFOODFACTS);
    assertThat(result.getExternalId()).isEqualTo("1234567890123");
  }

  @Test
  public void convertFromDtoWithNullSourceDefaultsToManualTest() {
    // Arrange
    FoodPersistence persistence = new FoodPersistence(
        UUID.randomUUID(), "Apple", new BigDecimal("52"), new BigDecimal("100"));
    FoodPersistenceConverter converter = new FoodPersistenceConverter();

    // Act
    Food result = converter.convertFromDto(persistence);

    // Assert
    assertThat(result.getSource()).isEqualTo(FoodSource.MANUAL);
    assertThat(result.getBrand()).isNull();
    assertThat(result.getExternalId()).isNull();
  }

  @Test
  public void convertFromEntityWithExtendedFieldsTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(),
        "Cola",
        new BigDecimal("42.00"),
        new BigDecimal("100"),
        "Acme",
        "Beverages",
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("10.60"),
        new BigDecimal("10.60"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.01"),
        new BigDecimal("0.00"),
        "https://example.com/cola.png",
        FoodSource.OPENFOODFACTS,
        "1234567890123"
    );
    FoodPersistenceConverter converter = new FoodPersistenceConverter();

    // Act
    FoodPersistence result = converter.convertFromEntity(food);

    // Assert
    assertThat(result.getBrand()).isEqualTo("Acme");
    assertThat(result.getCategory()).isEqualTo("Beverages");
    assertThat(result.getImageUrl()).isEqualTo("https://example.com/cola.png");
    assertThat(result.getSource()).isEqualTo("OPENFOODFACTS");
    assertThat(result.getExternalId()).isEqualTo("1234567890123");
  }

  @Test
  public void convertFromEntityUsingCoreConstructorDefaultsSourceToManualTest() {
    // Arrange
    Food food = new Food(UUID.randomUUID(), "Apple", new BigDecimal("52"), new BigDecimal("100"));
    FoodPersistenceConverter converter = new FoodPersistenceConverter();

    // Act
    FoodPersistence result = converter.convertFromEntity(food);

    // Assert
    assertThat(result.getSource()).isEqualTo("MANUAL");
  }

  @Test
  public void convertFromEntityWithNullSourceLeavesSourceNullTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(), "Apple", new BigDecimal("52"), new BigDecimal("100"),
        null, null, null, null, null, null, null, null, null, null, null, null, null
    );
    FoodPersistenceConverter converter = new FoodPersistenceConverter();

    // Act
    FoodPersistence result = converter.convertFromEntity(food);

    // Assert
    assertThat(result.getSource()).isNull();
  }
}
