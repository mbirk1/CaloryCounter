package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

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
    assertThat(food.getDiet()).isEqualTo(Diet.UNKNOWN);
  }

  @Test
  public void fullConstructorSetsAllExtendedFieldsTest() {
    // Arrange
    UUID id = UUID.randomUUID();

    // Act
    Food food = new Food.Builder()
        .id(id)
        .name("Cola")
        .calory(new BigDecimal("42.00"))
        .grams(new BigDecimal("100"))
        .brand("Acme")
        .category("Beverages")
        .fat(new BigDecimal("0.00"))
        .saturatedFat(new BigDecimal("0.00"))
        .carbohydrates(new BigDecimal("10.60"))
        .sugar(new BigDecimal("10.60"))
        .fiber(new BigDecimal("0.00"))
        .protein(new BigDecimal("0.00"))
        .salt(new BigDecimal("0.01"))
        .sodium(new BigDecimal("0.00"))
        .imageUrl("https://example.com/cola.png")
        .source(FoodSource.OPENFOODFACTS)
        .externalId("1234567890123")
        .diet(Diet.VEGAN)
        .build();

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
    assertThat(food.getDiet()).isEqualTo(Diet.VEGAN);
  }

  @Test
  public void validatePlausibilityAcceptsAFullyPlausibleFoodTest() {
    // Arrange
    Food food = new Food.Builder()
        .name("Hafer ganzes Korn, roh")
        .calory(new BigDecimal("343"))
        .grams(new BigDecimal("100"))
        .fat(new BigDecimal("7.09"))
        .vitaminA(new BigDecimal("0"))
        .vitaminC(new BigDecimal("0"))
        .iron(new BigDecimal("4.7"))
        .build();

    // Act & Assert
    assertThatCode(food::validatePlausibility).doesNotThrowAnyException();
  }

  @Test
  public void validatePlausibilityRejectsANegativeMicronutrientValueTest() {
    // Arrange - the negative-value check is identical logic shared by every field
    // (Food#requireInRange), so a single representative micronutrient is enough to prove it.
    Food food = new Food.Builder()
        .name("Testprodukt")
        .calory(new BigDecimal("100"))
        .grams(new BigDecimal("100"))
        .vitaminC(new BigDecimal("-1"))
        .build();

    // Act & Assert
    assertThatThrownBy(food::validatePlausibility).isInstanceOf(ValidationException.class);
  }

  @Test
  public void validatePlausibilityAcceptsEveryMicronutrientAtItsUpperBoundTest() {
    // Arrange & Act & Assert - one @Test method looping over every micronutrient bound, rather
    // than a @ParameterizedTest per bound: this codebase's ArchUnit rule requires every
    // "*Test"-named method to carry @Test, which @ParameterizedTest does not satisfy.
    for (MicronutrientBound bound : micronutrientBounds()) {
      Food food = bound.setter().apply(baseBuilder(), bound.maxBound()).build();
      assertThatCode(food::validatePlausibility)
          .as("micronutrient: " + bound.label())
          .doesNotThrowAnyException();
    }
  }

  @Test
  public void validatePlausibilityRejectsEveryMicronutrientAboveItsUpperBoundTest() {
    // Arrange & Act & Assert - a unit-mix-up-sized value, comfortably above any real bound
    for (MicronutrientBound bound : micronutrientBounds()) {
      Food food = bound.setter().apply(baseBuilder(), bound.maxBound().add(BigDecimal.ONE))
          .build();
      assertThatThrownBy(food::validatePlausibility)
          .as("micronutrient: " + bound.label())
          .isInstanceOf(ValidationException.class);
    }
  }

  private Food.Builder baseBuilder() {
    return new Food.Builder()
        .name("Testprodukt")
        .calory(new BigDecimal("100"))
        .grams(new BigDecimal("100"));
  }

  private record MicronutrientBound(
      String label,
      BiFunction<Food.Builder, BigDecimal, Food.Builder> setter,
      BigDecimal maxBound) {
  }

  private static List<MicronutrientBound> micronutrientBounds() {
    return List.of(
        new MicronutrientBound("vitaminA", Food.Builder::vitaminA, new BigDecimal("30000")),
        new MicronutrientBound("vitaminD", Food.Builder::vitaminD, new BigDecimal("500")),
        new MicronutrientBound("vitaminE", Food.Builder::vitaminE, new BigDecimal("300")),
        new MicronutrientBound("vitaminK", Food.Builder::vitaminK, new BigDecimal("3000")),
        new MicronutrientBound("vitaminB1", Food.Builder::vitaminB1, new BigDecimal("30")),
        new MicronutrientBound("vitaminB2", Food.Builder::vitaminB2, new BigDecimal("20")),
        new MicronutrientBound("vitaminB6", Food.Builder::vitaminB6, new BigDecimal("10000")),
        new MicronutrientBound("vitaminB12", Food.Builder::vitaminB12, new BigDecimal("200")),
        new MicronutrientBound("vitaminC", Food.Builder::vitaminC, new BigDecimal("3000")),
        new MicronutrientBound("folate", Food.Builder::folate, new BigDecimal("20000")),
        new MicronutrientBound("potassium", Food.Builder::potassium, new BigDecimal("10000")),
        new MicronutrientBound("calcium", Food.Builder::calcium, new BigDecimal("3000")),
        new MicronutrientBound("magnesium", Food.Builder::magnesium, new BigDecimal("2000")),
        new MicronutrientBound("iron", Food.Builder::iron, new BigDecimal("150")),
        new MicronutrientBound("zinc", Food.Builder::zinc, new BigDecimal("150")),
        new MicronutrientBound("iodine", Food.Builder::iodine, new BigDecimal("5000"))
    );
  }
}
