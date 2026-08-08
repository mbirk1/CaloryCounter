package de.birk.calory.usecase.user.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.domain.user.ActivityLevel;
import de.birk.calory.domain.user.User;

public class UserDetailsDtoConverterUnitTest {

  @Test
  public void toDtoWithCompleteProfileTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(),
        "user@example.com",
        "hash",
        new BigDecimal("180"),
        new BigDecimal("75"),
        ActivityLevel.EXTRA_ACTIVE,
        Instant.now()
    );
    UserDetailsDtoConverter converter = new UserDetailsDtoConverter();

    // Act
    UserDetailsDto result = converter.toDto(user);

    // Assert
    assertThat(result.getId()).isEqualTo(user.getId());
    assertThat(result.getEmail()).isEqualTo(user.getEmail());
    assertThat(result.getHeight()).isEqualTo(user.getHeight());
    assertThat(result.getWeight()).isEqualTo(user.getWeight());
    assertThat(result.getActivityLevel()).isEqualTo("EXTRA_ACTIVE");
    assertThat(result.isProfileCompleted()).isTrue();
  }

  @Test
  public void toDtoWithIncompleteProfileTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    UserDetailsDtoConverter converter = new UserDetailsDtoConverter();

    // Act
    UserDetailsDto result = converter.toDto(user);

    // Assert
    assertThat(result.getActivityLevel()).isNull();
    assertThat(result.isProfileCompleted()).isFalse();
  }
}
