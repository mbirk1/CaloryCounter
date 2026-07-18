package de.birk.calory.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.exception.ValidationException;

public class UserUnitTest {

  @Test
  public void validEmailAndPasswordHashTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    String email = "user@example.com";
    String passwordHash = "hash";
    Instant createdAt = Instant.now();

    // Act
    User user = new User(id, email, passwordHash, null, null, null, createdAt);

    // Assert
    assertThat(user.getId()).isEqualTo(id);
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    assertThat(user.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  public void nullEmailTest() {
    // Arrange & Act & Assert
    assertThatThrownBy(
        () -> new User(UUID.randomUUID(), null, "hash", null, null, null, Instant.now())
    ).isInstanceOf(ValidationException.class);
  }

  @Test
  public void blankEmailTest() {
    // Arrange & Act & Assert
    assertThatThrownBy(
        () -> new User(UUID.randomUUID(), "   ", "hash", null, null, null, Instant.now())
    ).isInstanceOf(ValidationException.class);
  }

  @Test
  public void malformedEmailTest() {
    // Arrange & Act & Assert
    assertThatThrownBy(
        () -> new User(UUID.randomUUID(), "not-an-email", "hash", null, null, null, Instant.now())
    ).isInstanceOf(ValidationException.class);
  }

  @Test
  public void nullPasswordHashTest() {
    // Arrange & Act & Assert
    assertThatThrownBy(
        () -> new User(
            UUID.randomUUID(), "user@example.com", null, null, null, null, Instant.now())
    ).isInstanceOf(ValidationException.class);
  }

  @Test
  public void blankPasswordHashTest() {
    // Arrange & Act & Assert
    assertThatThrownBy(
        () -> new User(
            UUID.randomUUID(), "user@example.com", "   ", null, null, null, Instant.now())
    ).isInstanceOf(ValidationException.class);
  }

  @Test
  public void incompleteProfileIsNotCompleteTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());

    // Act & Assert
    assertThat(user.isProfileComplete()).isFalse();
  }

  @Test
  public void completeProfileIsCompleteTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(),
        "user@example.com",
        "hash",
        new BigDecimal("180"),
        new BigDecimal("75"),
        ActivityLevel.MODERATELY_ACTIVE,
        Instant.now()
    );

    // Act & Assert
    assertThat(user.isProfileComplete()).isTrue();
  }

  @Test
  public void withProfileReturnsUpdatedCopyTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    BigDecimal height = new BigDecimal("180");
    BigDecimal weight = new BigDecimal("75");

    // Act
    User updated = user.withProfile(height, weight, ActivityLevel.VERY_ACTIVE);

    // Assert
    assertThat(updated.getId()).isEqualTo(user.getId());
    assertThat(updated.getEmail()).isEqualTo(user.getEmail());
    assertThat(updated.getHeight()).isEqualTo(height);
    assertThat(updated.getWeight()).isEqualTo(weight);
    assertThat(updated.getActivityLevel()).isEqualTo(ActivityLevel.VERY_ACTIVE);
    assertThat(updated.isProfileComplete()).isTrue();
  }

  @Test
  public void defaultConstructorTest() {
    // Arrange & Act
    User user = new User();

    // Assert
    assertThat(user.getEmail()).isEqualTo("");
    assertThat(user.getPasswordHash()).isEqualTo("");
    assertThat(user.getHeight()).isNull();
    assertThat(user.getWeight()).isNull();
    assertThat(user.getActivityLevel()).isNull();
    assertThat(user.getCreatedAt()).isNull();
  }
}
