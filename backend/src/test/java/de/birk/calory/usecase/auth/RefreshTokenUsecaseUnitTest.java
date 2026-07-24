package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import de.birk.calory.adapter.primary.model.AuthResponseDto;
import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenUsecaseUnitTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenService tokenService;

  @Mock
  private RefreshTokenIssuer refreshTokenIssuer;

  @InjectMocks
  private RefreshTokenUsecase refreshTokenUsecase;

  @Test
  public void rotatesValidRefreshTokenTest() {
    // Arrange
    UUID userId = UUID.randomUUID();
    RefreshTokenPersistence stored = new RefreshTokenPersistence(
        UUID.randomUUID(), userId, "hashed", Instant.now().plusSeconds(60), false, Instant.now());
    UserPersistence userPersistence = new UserPersistence(
        userId, "user@example.com", "hash", null, null, null, Instant.now());
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));
    when(userRepository.findById(userId)).thenReturn(Optional.of(userPersistence));
    AuthResult expected = new AuthResult(
        new AuthResponseDto("access-token", 900L, "Bearer", false), "new-raw-refresh-token");
    when(refreshTokenIssuer.issueTokenPair(any())).thenReturn(expected);

    // Act
    AuthResult result = refreshTokenUsecase.refresh("raw-token");

    // Assert
    assertThat(result).isEqualTo(expected);
    verify(refreshTokenRepository).save(any(RefreshTokenPersistence.class));
    verify(refreshTokenRepository, never()).deleteAllByUserId(any());
  }

  @Test
  public void throwsWhenTokenUnknownTest() {
    // Arrange
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUsecase.refresh("raw-token"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  public void revokesAllTokensOnReuseTest() {
    // Arrange
    UUID userId = UUID.randomUUID();
    RefreshTokenPersistence stored = new RefreshTokenPersistence(
        UUID.randomUUID(), userId, "hashed", Instant.now().plusSeconds(60), true, Instant.now());
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUsecase.refresh("raw-token"))
        .isInstanceOf(InvalidTokenException.class);
    verify(refreshTokenRepository).deleteAllByUserId(userId);
  }

  @Test
  public void throwsWhenTokenExpiredTest() {
    // Arrange
    UUID userId = UUID.randomUUID();
    RefreshTokenPersistence stored = new RefreshTokenPersistence(
        UUID.randomUUID(), userId, "hashed", Instant.now().minusSeconds(60), false, Instant.now());
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));

    // Act & Assert
    assertThatThrownBy(() -> refreshTokenUsecase.refresh("raw-token"))
        .isInstanceOf(TokenExpiredException.class);
  }
}
