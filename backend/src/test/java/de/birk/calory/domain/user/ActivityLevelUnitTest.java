package de.birk.calory.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ActivityLevelUnitTest {

  @Test
  public void valueOfReturnsMatchingConstantTest() {
    // Arrange & Act
    ActivityLevel activityLevel = ActivityLevel.valueOf("SEDENTARY");

    // Assert
    assertThat(activityLevel).isEqualTo(ActivityLevel.SEDENTARY);
  }

  @Test
  public void valuesContainsAllFiveLevelsTest() {
    // Arrange & Act
    ActivityLevel[] values = ActivityLevel.values();

    // Assert
    assertThat(values).containsExactly(
        ActivityLevel.SEDENTARY,
        ActivityLevel.LIGHTLY_ACTIVE,
        ActivityLevel.MODERATELY_ACTIVE,
        ActivityLevel.VERY_ACTIVE,
        ActivityLevel.EXTRA_ACTIVE
    );
  }
}
