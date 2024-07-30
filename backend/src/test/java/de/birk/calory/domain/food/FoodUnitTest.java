package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

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
  }
}
