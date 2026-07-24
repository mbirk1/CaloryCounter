package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;

/**
 * The DTO that is used to submit the onboarding profile data (height,
 * weight, activity level) of a user.
 *
 * @author Marius Birk
 */
public class UserProfileDto {

  private BigDecimal height;
  private BigDecimal weight;
  private String activityLevel;

  public UserProfileDto() {
  }

  /**
   * Constructor that takes all onboarding properties of a user.
   *
   * @param height the height in centimeters
   * @param weight the weight in kilograms
   * @param activityLevel the name of the activity level enum constant
   */
  public UserProfileDto(BigDecimal height, BigDecimal weight, String activityLevel) {
    this.height = height;
    this.weight = weight;
    this.activityLevel = activityLevel;
  }

  public BigDecimal getHeight() {
    return height;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public String getActivityLevel() {
    return activityLevel;
  }
}
