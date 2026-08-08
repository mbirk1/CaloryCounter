package de.birk.calory.usecase.user.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.ActivityLevel;
import de.birk.calory.domain.user.User;

public class UserPersistenceConverterUnitTest {

  @Test
  public void convertFromDtoWithCompleteProfileTest() {
    // Arrange
    UserPersistence userPersistence = new UserPersistence(
        UUID.randomUUID(),
        "user@example.com",
        "hash",
        new BigDecimal("180"),
        new BigDecimal("75"),
        "VERY_ACTIVE",
        Instant.now()
    );
    UserPersistenceConverter converter = new UserPersistenceConverter();

    // Act
    User result = converter.convertFromDto(userPersistence);

    // Assert
    assertThat(result.getEmail()).isEqualTo(userPersistence.getEmail());
    assertThat(result.getActivityLevel()).isEqualTo(ActivityLevel.VERY_ACTIVE);
  }

  @Test
  public void convertFromDtoWithoutActivityLevelTest() {
    // Arrange
    UserPersistence userPersistence = new UserPersistence(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    UserPersistenceConverter converter = new UserPersistenceConverter();

    // Act
    User result = converter.convertFromDto(userPersistence);

    // Assert
    assertThat(result.getActivityLevel()).isNull();
  }

  @Test
  public void convertFromEntityWithCompleteProfileTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(),
        "user@example.com",
        "hash",
        new BigDecimal("180"),
        new BigDecimal("75"),
        ActivityLevel.LIGHTLY_ACTIVE,
        Instant.now()
    );
    UserPersistenceConverter converter = new UserPersistenceConverter();

    // Act
    UserPersistence result = converter.convertFromEntity(user);

    // Assert
    assertThat(result.getEmail()).isEqualTo(user.getEmail());
    assertThat(result.getActivityLevel()).isEqualTo("LIGHTLY_ACTIVE");
  }

  @Test
  public void convertFromEntityWithoutActivityLevelTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    UserPersistenceConverter converter = new UserPersistenceConverter();

    // Act
    UserPersistence result = converter.convertFromEntity(user);

    // Assert
    assertThat(result.getActivityLevel()).isNull();
  }
}
