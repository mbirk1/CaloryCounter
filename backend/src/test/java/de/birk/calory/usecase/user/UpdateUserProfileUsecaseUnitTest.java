package de.birk.calory.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.adapter.primary.model.UserProfileDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class UpdateUserProfileUsecaseUnitTest {

  @Mock
  private UserRepository userRepository;

  @Test
  public void updatesProfileWithValidDataTest() {
    // Arrange
    UpdateUserProfileUsecase usecase = new UpdateUserProfileUsecase(userRepository);
    UUID id = UUID.randomUUID();
    UserPersistence userPersistence = new UserPersistence(
        id, "user@example.com", "hash", null, null, null, Instant.now());
    when(userRepository.findById(id)).thenReturn(Optional.of(userPersistence));
    when(userRepository.save(any(UserPersistence.class))).thenReturn(userPersistence);
    UserProfileDto profileDto =
        new UserProfileDto(new BigDecimal("180"), new BigDecimal("75"), "VERY_ACTIVE");

    // Act
    UserDetailsDto result = usecase.updateProfile(id, profileDto);

    // Assert
    assertThat(result.getHeight()).isEqualTo(new BigDecimal("180"));
    assertThat(result.getWeight()).isEqualTo(new BigDecimal("75"));
    assertThat(result.getActivityLevel()).isEqualTo("VERY_ACTIVE");
    assertThat(result.isProfileCompleted()).isTrue();
  }

  @Test
  public void throwsWhenActivityLevelNullTest() {
    // Arrange
    UpdateUserProfileUsecase usecase = new UpdateUserProfileUsecase(userRepository);
    UUID id = UUID.randomUUID();
    UserPersistence userPersistence = new UserPersistence(
        id, "user@example.com", "hash", null, null, null, Instant.now());
    when(userRepository.findById(id)).thenReturn(Optional.of(userPersistence));
    UserProfileDto profileDto =
        new UserProfileDto(new BigDecimal("180"), new BigDecimal("75"), null);

    // Act & Assert
    assertThatThrownBy(() -> usecase.updateProfile(id, profileDto))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void throwsWhenActivityLevelInvalidTest() {
    // Arrange
    UpdateUserProfileUsecase usecase = new UpdateUserProfileUsecase(userRepository);
    UUID id = UUID.randomUUID();
    UserPersistence userPersistence = new UserPersistence(
        id, "user@example.com", "hash", null, null, null, Instant.now());
    when(userRepository.findById(id)).thenReturn(Optional.of(userPersistence));
    UserProfileDto profileDto =
        new UserProfileDto(new BigDecimal("180"), new BigDecimal("75"), "NOT_A_LEVEL");

    // Act & Assert
    assertThatThrownBy(() -> usecase.updateProfile(id, profileDto))
        .isInstanceOf(ValidationException.class);
  }
}
