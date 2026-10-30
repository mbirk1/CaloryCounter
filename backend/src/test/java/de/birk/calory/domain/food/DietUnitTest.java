package de.birk.calory.domain.food;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class DietUnitTest {

  @Test
  public void veganIsStricterThanVegetarianTest() {
    assertThat(Diet.STRICTNESS_ORDER.compare(Diet.VEGAN, Diet.VEGETARIAN)).isLessThan(0);
  }

  @Test
  public void vegetarianIsStricterThanNonVegetarianTest() {
    assertThat(Diet.STRICTNESS_ORDER.compare(Diet.VEGETARIAN, Diet.NON_VEGETARIAN))
        .isLessThan(0);
  }

  @Test
  public void nonVegetarianIsStricterThanUnknownTest() {
    assertThat(Diet.STRICTNESS_ORDER.compare(Diet.NON_VEGETARIAN, Diet.UNKNOWN)).isLessThan(0);
  }

  @Test
  public void veganIsStricterThanUnknownTest() {
    // Explicitly not relying on declaration order (ordinal()) - checked directly, independent
    // of adjacency in the enum.
    assertThat(Diet.STRICTNESS_ORDER.compare(Diet.VEGAN, Diet.UNKNOWN)).isLessThan(0);
  }

  @Test
  public void sameDietComparesEqualTest() {
    assertThat(Diet.STRICTNESS_ORDER.compare(Diet.VEGETARIAN, Diet.VEGETARIAN)).isZero();
  }
}
