package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.birk.calory.adapter.primary.model.AuthResponseDto;
import de.birk.calory.adapter.primary.model.RegisterUserDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.exception.EmailAlreadyInUseException;

@ExtendWith(MockitoExtension.class)
public class RegisterUserUsecaseUnitTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RefreshTokenIssuer refreshTokenIssuer;

  @InjectMocks
  private RegisterUserUsecase registerUserUsecase;

  @Test
  public void registersNewUserAndIssuesTokensTest() {
    // Arrange
    RegisterUserDto registerUserDto = new RegisterUserDto("user@example.com", "raw-password");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");
    AuthResult expected = new AuthResult(
        new AuthResponseDto("access-token", 900L, "Bearer", false), "raw-refresh-token");
    when(refreshTokenIssuer.issueTokenPair(any())).thenReturn(expected);

    // Act
    AuthResult result = registerUserUsecase.register(registerUserDto);

    // Assert
    assertThat(result).isEqualTo(expected);
    verify(userRepository).save(any(UserPersistence.class));
  }

  @Test
  public void throwsWhenEmailAlreadyInUseTest() {
    // Arrange
    RegisterUserDto registerUserDto = new RegisterUserDto("user@example.com", "raw-password");
    UserPersistence existing = new UserPersistence(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

    // Act & Assert
    assertThatThrownBy(() -> registerUserUsecase.register(registerUserDto))
        .isInstanceOf(EmailAlreadyInUseException.class);
  }
}
