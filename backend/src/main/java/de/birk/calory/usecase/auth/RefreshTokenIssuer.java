package de.birk.calory.usecase.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.AuthResponseDto;
import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;
import de.birk.calory.domain.user.User;

/**
 * Package-private helper, shared by {@link RegisterUserUsecase},
 * {@link LoginUsecase} and {@link RefreshTokenUsecase}, that issues a fresh
 * access/refresh token pair for a user and persists the hash of the new
 * refresh token.
 *
 * @author Marius Birk
 */
@Component
class RefreshTokenIssuer {

  private static final long REFRESH_TOKEN_TTL_DAYS = 14;
  private static final String TOKEN_TYPE = "Bearer";

  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenService tokenService;

  RefreshTokenIssuer(RefreshTokenRepository refreshTokenRepository, TokenService tokenService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenService = tokenService;
  }

  /**
   * Issues a new access/refresh token pair for the given user.
   *
   * @param user the user the tokens are issued for
   * @return the response body and the raw refresh token for the cookie
   */
  AuthResult issueTokenPair(User user) {
    String accessToken = this.tokenService.issueAccessToken(user.getId(), user.getEmail());
    String rawRefreshToken = this.tokenService.generateOpaqueRefreshToken();

    RefreshTokenPersistence refreshTokenPersistence = new RefreshTokenPersistence(
        UUID.randomUUID(),
        user.getId(),
        this.tokenService.hash(rawRefreshToken),
        Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS),
        false,
        Instant.now()
    );
    this.refreshTokenRepository.save(refreshTokenPersistence);

    AuthResponseDto response = new AuthResponseDto(
        accessToken,
        this.tokenService.getAccessTokenTtlSeconds(),
        TOKEN_TYPE,
        user.isProfileComplete()
    );

    return new AuthResult(response, rawRefreshToken);
  }
}
