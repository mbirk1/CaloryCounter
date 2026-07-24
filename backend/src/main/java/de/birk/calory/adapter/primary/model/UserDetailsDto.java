package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The DTO that is returned to clients for the current user, including the
 * profileCompleted flag that drives the onboarding redirect on the client.
 *
 * @author Marius Birk
 */
public class UserDetailsDto {

  private UUID id;
  private String email;
  private BigDecimal height;
  private BigDecimal weight;
  private String activityLevel;
  private boolean profileCompleted;

  public UserDetailsDto() {
  }

  /**
   * Constructor that creates a detailed representation of a user.
   *
   * @param id the identifier
   * @param email the email address
   * @param height the height in centimeters, may be null before onboarding
   * @param weight the weight in kilograms, may be null before onboarding
   * @param activityLevel the name of the activity level, may be null before onboarding
   * @param profileCompleted whether height, weight and activity level are all set
   */
  public UserDetailsDto(
      UUID id,
      String email,
      BigDecimal height,
      BigDecimal weight,
      String activityLevel,
      boolean profileCompleted) {
    this.id = id;
    this.email = email;
    this.height = height;
    this.weight = weight;
    this.activityLevel = activityLevel;
    this.profileCompleted = profileCompleted;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
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

  public boolean isProfileCompleted() {
    return profileCompleted;
  }
}
