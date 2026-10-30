package de.birk.calory.adapter.primary.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class FoodDtoUnitTest {

  @Test
  public void storesAndReturnsAllGivenFieldsTest() {
    // Arrange & Act
    FoodDto foodDto = new FoodDto(
        "Hafermilch",
        new BigDecimal("45"),
        new BigDecimal("100"),
        "VEGAN",
        "Oatly",
        "Plant-based beverages",
        "https://example.com/oatly.png",
        new BigDecimal("1.5"),
        new BigDecimal("0.2"),
        new BigDecimal("6.7"),
        new BigDecimal("4.1"),
        new BigDecimal("0.8"),
        new BigDecimal("1"),
        new BigDecimal("0.11"),
        new BigDecimal("0.04"),
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );

    // Assert
    assertThat(foodDto.getName()).isEqualTo("Hafermilch");
    assertThat(foodDto.getCalory()).isEqualByComparingTo("45");
    assertThat(foodDto.getGrams()).isEqualByComparingTo("100");
    assertThat(foodDto.getDiet()).isEqualTo("VEGAN");
    assertThat(foodDto.getBrand()).isEqualTo("Oatly");
    assertThat(foodDto.getCategory()).isEqualTo("Plant-based beverages");
    assertThat(foodDto.getImageUrl()).isEqualTo("https://example.com/oatly.png");
    assertThat(foodDto.getFat()).isEqualByComparingTo("1.5");
    assertThat(foodDto.getSaturatedFat()).isEqualByComparingTo("0.2");
    assertThat(foodDto.getCarbohydrates()).isEqualByComparingTo("6.7");
    assertThat(foodDto.getSugar()).isEqualByComparingTo("4.1");
    assertThat(foodDto.getFiber()).isEqualByComparingTo("0.8");
    assertThat(foodDto.getProtein()).isEqualByComparingTo("1");
    assertThat(foodDto.getSalt()).isEqualByComparingTo("0.11");
    assertThat(foodDto.getSodium()).isEqualByComparingTo("0.04");
  }

  @Test
  public void defaultConstructorLeavesAllFieldsNullTest() {
    // Arrange & Act
    FoodDto foodDto = new FoodDto();

    // Assert
    assertThat(foodDto.getName()).isNull();
    assertThat(foodDto.getCalory()).isNull();
    assertThat(foodDto.getGrams()).isNull();
    assertThat(foodDto.getDiet()).isNull();
    assertThat(foodDto.getBrand()).isNull();
    assertThat(foodDto.getCategory()).isNull();
    assertThat(foodDto.getImageUrl()).isNull();
    assertThat(foodDto.getFat()).isNull();
    assertThat(foodDto.getSaturatedFat()).isNull();
    assertThat(foodDto.getCarbohydrates()).isNull();
    assertThat(foodDto.getSugar()).isNull();
    assertThat(foodDto.getFiber()).isNull();
    assertThat(foodDto.getProtein()).isNull();
    assertThat(foodDto.getSalt()).isNull();
    assertThat(foodDto.getSodium()).isNull();
  }
}
