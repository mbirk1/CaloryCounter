package de.birk.calory.usecase.auth;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;

/**
 * This Usecase logs out a user on the current device by revoking the
 * presented refresh token. It is idempotent: presenting a missing or
 * already invalid token is not treated as an error.
 *
 * @author Marius Birk
 */
@Component
public class LogoutUsecase {

  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenService tokenService;

  public LogoutUsecase(RefreshTokenRepository refreshTokenRepository, TokenService tokenService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenService = tokenService;
  }

  /**
   * Revokes the given raw refresh token, if it exists.
   *
   * @param rawRefreshToken the raw refresh token presented by the client, may be null
   */
  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
      return;
    }

    String tokenHash = this.tokenService.hash(rawRefreshToken);
    this.refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(stored -> {
      RefreshTokenPersistence revoked = new RefreshTokenPersistence(
          stored.getId(),
          stored.getUserId(),
          stored.getTokenHash(),
          stored.getExpiresAt(),
          true,
          stored.getCreatedAt()
      );
      this.refreshTokenRepository.save(revoked);
    });
  }
}
