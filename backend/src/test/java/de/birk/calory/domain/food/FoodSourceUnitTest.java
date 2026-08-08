package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class FoodSourceUnitTest {

  @Test
  public void valueOfReturnsMatchingConstantTest() {
    // Arrange & Act
    FoodSource source = FoodSource.valueOf("OPENFOODFACTS");

    // Assert
    assertThat(source).isEqualTo(FoodSource.OPENFOODFACTS);
  }

  @Test
  public void valuesContainsBothSourcesTest() {
    // Arrange & Act
    FoodSource[] values = FoodSource.values();

    // Assert
    assertThat(values).containsExactly(FoodSource.MANUAL, FoodSource.OPENFOODFACTS);
  }
}
