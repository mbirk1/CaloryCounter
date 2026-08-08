package de.birk.calory.domain.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.exception.ValidationException;

/**
 * Entity that depicts an application user.
 *
 * <p>Height, weight and activity level are unknown at registration time and
 * are filled in a second onboarding step, which is why they are nullable.
 *
 * @author Marius Birk
 */
public class User extends AbstractEntity<UUID> {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private UUID id;
  private final String email;
  private final String passwordHash;
  private final BigDecimal height;
  private final BigDecimal weight;
  private final ActivityLevel activityLevel;
  private final Instant createdAt;

  /**
   * Default constructor with empty/unset values.
   */
  protected User() {
    this.email = "";
    this.passwordHash = "";
    this.height = null;
    this.weight = null;
    this.activityLevel = null;
    this.createdAt = null;
  }

  /**
   * Constructor to create a user with an already known identifier, used
   * whenever a persisted user is rebuilt from its persistence
   * representation.
   *
   * @param id the identifier as uuid
   * @param email the email address, used as the login name
   * @param passwordHash the bcrypt hash of the user's password, never the raw password
   * @param height the height in centimeters, unknown until onboarding is completed
   * @param weight the weight in kilograms, unknown until onboarding is completed
   * @param activityLevel the activity level, unknown until onboarding is completed
   * @param createdAt the point in time the user was created
   * @throws ValidationException validates the entity
   */
  public User(
      UUID id,
      String email,
      String passwordHash,
      BigDecimal height,
      BigDecimal weight,
      ActivityLevel activityLevel,
      Instant createdAt) throws ValidationException {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.height = height;
    this.weight = weight;
    this.activityLevel = activityLevel;
    this.createdAt = createdAt;
    validate();
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public BigDecimal getHeight() {
    return height;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public ActivityLevel getActivityLevel() {
    return activityLevel;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   * Determines whether the onboarding step has been completed, i.e. whether
   * height, weight and activity level have all been provided.
   *
   * @return true if the profile is complete
   */
  public boolean isProfileComplete() {
    return this.height != null && this.weight != null && this.activityLevel != null;
  }

  /**
   * Creates a copy of this user with the given onboarding data applied.
   *
   * @param newHeight the height in centimeters
   * @param newWeight the weight in kilograms
   * @param newActivityLevel the activity level
   * @return a new user instance carrying the updated profile data
   */
  public User withProfile(
      BigDecimal newHeight, BigDecimal newWeight, ActivityLevel newActivityLevel) {
    return new User(
        this.id,
        this.email,
        this.passwordHash,
        newHeight,
        newWeight,
        newActivityLevel,
        this.createdAt
    );
  }

  protected void validate() throws ValidationException {
    boolean emailValid = this.email != null
        && !this.email.isBlank()
        && EMAIL_PATTERN.matcher(this.email).matches();
    if (!emailValid) {
      throw new ValidationException();
    }
    if (this.passwordHash == null || this.passwordHash.isBlank()) {
      throw new ValidationException();
    }
  }
}
