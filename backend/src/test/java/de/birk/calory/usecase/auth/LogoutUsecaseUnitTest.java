package de.birk.calory.usecase.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;

@ExtendWith(MockitoExtension.class)
public class LogoutUsecaseUnitTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private TokenService tokenService;

  @InjectMocks
  private LogoutUsecase logoutUsecase;

  @Test
  public void revokesKnownRefreshTokenTest() {
    // Arrange
    RefreshTokenPersistence stored = new RefreshTokenPersistence(
        UUID.randomUUID(), UUID.randomUUID(), "hashed", Instant.now(), false, Instant.now());
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));

    // Act
    logoutUsecase.logout("raw-token");

    // Assert
    verify(refreshTokenRepository).save(any(RefreshTokenPersistence.class));
  }

  @Test
  public void doesNothingWhenTokenUnknownTest() {
    // Arrange
    when(tokenService.hash("raw-token")).thenReturn("hashed");
    when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.empty());

    // Act
    logoutUsecase.logout("raw-token");

    // Assert
    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  public void doesNothingWhenTokenNullTest() {
    // Act
    logoutUsecase.logout(null);

    // Assert
    verifyNoInteractions(refreshTokenRepository, tokenService);
  }

  @Test
  public void doesNothingWhenTokenBlankTest() {
    // Act
    logoutUsecase.logout("   ");

    // Assert
    verifyNoInteractions(refreshTokenRepository, tokenService);
  }
}
