package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;
import de.birk.calory.domain.user.User;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenIssuerUnitTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private TokenService tokenService;

  @InjectMocks
  private RefreshTokenIssuer refreshTokenIssuer;

  @Test
  public void issueTokenPairPersistsHashedRefreshTokenTest() {
    // Arrange
    User user = new User(
        UUID.randomUUID(), "user@example.com", "hash", null, null, null, Instant.now());
    when(tokenService.issueAccessToken(user.getId(), user.getEmail())).thenReturn("access-token");
    when(tokenService.generateOpaqueRefreshToken()).thenReturn("raw-refresh-token");
    when(tokenService.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
    when(tokenService.getAccessTokenTtlSeconds()).thenReturn(900L);

    // Act
    AuthResult result = refreshTokenIssuer.issueTokenPair(user);

    // Assert
    assertThat(result.getRefreshToken()).isEqualTo("raw-refresh-token");
    assertThat(result.getResponse().getAccessToken()).isEqualTo("access-token");
    assertThat(result.getResponse().getExpiresIn()).isEqualTo(900L);
    assertThat(result.getResponse().getTokenType()).isEqualTo("Bearer");
    assertThat(result.getResponse().isProfileCompleted()).isFalse();
    verify(refreshTokenRepository).save(any(RefreshTokenPersistence.class));
  }
}
