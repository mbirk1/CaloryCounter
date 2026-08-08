package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import de.birk.calory.adapter.primary.model.LoginRequestDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.exception.InvalidCredentialsException;

@ExtendWith(MockitoExtension.class)
public class LoginUsecaseUnitTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RefreshTokenIssuer refreshTokenIssuer;

  @InjectMocks
  private LoginUsecase loginUsecase;

  @Test
  public void logsInWithValidCredentialsTest() {
    // Arrange
    LoginRequestDto loginRequestDto = new LoginRequestDto("user@example.com", "raw-password");
    UserPersistence userPersistence = new UserPersistence(
        UUID.randomUUID(), "user@example.com", "hashed-password", null, null, null, Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userPersistence));
    when(passwordEncoder.matches("raw-password", "hashed-password")).thenReturn(true);
    AuthResult expected = new AuthResult(
        new AuthResponseDto("access-token", 900L, "Bearer", false), "raw-refresh-token");
    when(refreshTokenIssuer.issueTokenPair(any())).thenReturn(expected);

    // Act
    AuthResult result = loginUsecase.login(loginRequestDto);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void throwsWhenEmailUnknownTest() {
    // Arrange
    LoginRequestDto loginRequestDto = new LoginRequestDto("unknown@example.com", "raw-password");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> loginUsecase.login(loginRequestDto))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  public void throwsWhenPasswordWrongTest() {
    // Arrange
    LoginRequestDto loginRequestDto = new LoginRequestDto("user@example.com", "wrong-password");
    UserPersistence userPersistence = new UserPersistence(
        UUID.randomUUID(), "user@example.com", "hashed-password", null, null, null, Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userPersistence));
    when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> loginUsecase.login(loginRequestDto))
        .isInstanceOf(InvalidCredentialsException.class);
  }
}
