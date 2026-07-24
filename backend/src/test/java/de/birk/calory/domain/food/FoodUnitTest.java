package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.exception.ValidationException;

public class FoodUnitTest {

  @Test
  public void validNameAndCaloryTest() {
    // Arrange
    String name = "Apple";
    BigDecimal calory = new BigDecimal("100");
    BigDecimal grams = new BigDecimal("100");

    // Act
    Food food = new Food(name, calory, grams);

    // Assert
    assertThat(name).isEqualTo(food.getName());
    assertThat(calory).isEqualTo(food.getCalory());
    assertThat(grams).isEqualTo(food.getGrams());
  }

  @Test
  public void nullNameTest() {
    // Arrange
    String name = null;
    BigDecimal calory = new BigDecimal("100");
    BigDecimal grams = new BigDecimal("100");

    // Act & Assert
    assertThatThrownBy(() -> new Food(name, calory, grams)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void emptyNameTest() {
    // Arrange
    String name = "";
    BigDecimal calory = new BigDecimal("100");
    BigDecimal grams = new BigDecimal("100");

    // Act & Assert
    assertThatThrownBy(() -> new Food(name, calory, grams)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void blankNameTest() {
    // Arrange
    String name = "    ";
    BigDecimal calory = new BigDecimal("100");
    BigDecimal grams = new BigDecimal("100");

    // Act & Assert
    assertThatThrownBy(() -> new Food(name, calory, grams)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void nullCaloryTest() {
    // Arrange
    String name = "123";
    BigDecimal calory = null;
    BigDecimal grams = new BigDecimal("100");

    // Act & Assert
    assertThatThrownBy(() -> new Food(name, calory, grams)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void nullGramsTest() {
    // Arrange
    String name = "123";
    BigDecimal calory = new BigDecimal("100");
    BigDecimal grams = null;

    // Act & Assert
    assertThatThrownBy(() -> new Food(name, calory, grams)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void defaultConstructorTest() {
    Food food = new Food();
    assertThat(food.getCalory()).isEqualTo(BigDecimal.ZERO);
    assertThat(food.getName()).isEqualTo("");
    assertThat(food.getBrand()).isNull();
    assertThat(food.getSource()).isEqualTo(FoodSource.MANUAL);
  }

  @Test
  public void constructorWithIdOnlyDefaultsExtendedFieldsToNullTest() {
    // Arrange & Act
    Food food = new Food(
        UUID.randomUUID(), "Apple", new BigDecimal("100"), new BigDecimal("100"));

    // Assert
    assertThat(food.getBrand()).isNull();
    assertThat(food.getSource()).isEqualTo(FoodSource.MANUAL);
    assertThat(food.getExternalId()).isNull();
  }

  @Test
  public void fullConstructorSetsAllExtendedFieldsTest() {
    // Arrange
    UUID id = UUID.randomUUID();

    // Act
    Food food = new Food(
        id,
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

    // Assert
    assertThat(food.getId()).isEqualTo(id);
    assertThat(food.getBrand()).isEqualTo("Acme");
    assertThat(food.getCategory()).isEqualTo("Beverages");
    assertThat(food.getFat()).isEqualByComparingTo("0.00");
    assertThat(food.getSaturatedFat()).isEqualByComparingTo("0.00");
    assertThat(food.getCarbohydrates()).isEqualByComparingTo("10.60");
    assertThat(food.getSugar()).isEqualByComparingTo("10.60");
    assertThat(food.getFiber()).isEqualByComparingTo("0.00");
    assertThat(food.getProtein()).isEqualByComparingTo("0.00");
    assertThat(food.getSalt()).isEqualByComparingTo("0.01");
    assertThat(food.getSodium()).isEqualByComparingTo("0.00");
    assertThat(food.getImageUrl()).isEqualTo("https://example.com/cola.png");
    assertThat(food.getSource()).isEqualTo(FoodSource.OPENFOODFACTS);
    assertThat(food.getExternalId()).isEqualTo("1234567890123");
  }
}
