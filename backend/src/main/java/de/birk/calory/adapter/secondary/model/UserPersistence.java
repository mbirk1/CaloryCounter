package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persistence entity for application users.
 *
 * <p>The activity level is intentionally stored as a plain string instead of
 * the {@code domain.user.ActivityLevel} enum - persistence entities must not
 * depend on domain types, the mapping is done by the usecase layer converter.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_user")
public class UserPersistence {

  @Id
  @Column
  private UUID id;

  @Column
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column
  private BigDecimal height;

  @Column
  private BigDecimal weight;

  @Column(name = "activity_level")
  private String activityLevel;

  @Column(name = "created_at")
  private Instant createdAt;

  public UserPersistence() {
    //for JPA
  }

  /**
   * Constructor to create an instance of the object, that can be saved in the database.
   *
   * @param id the identifier
   * @param email the email address
   * @param passwordHash the bcrypt hash of the password
   * @param height the height in centimeters
   * @param weight the weight in kilograms
   * @param activityLevel the name of the activity level enum constant
   * @param createdAt the point in time the user was created
   */
  public UserPersistence(
      UUID id,
      String email,
      String passwordHash,
      BigDecimal height,
      BigDecimal weight,
      String activityLevel,
      Instant createdAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.height = height;
    this.weight = weight;
    this.activityLevel = activityLevel;
    this.createdAt = createdAt;
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

  public String getActivityLevel() {
    return activityLevel;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
