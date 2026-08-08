package de.birk.calory.usecase.auth;

import java.time.Instant;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.secondary.RefreshTokenRepository;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.User;
import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;
import de.birk.calory.usecase.user.converter.UserPersistenceConverter;

/**
 * This Usecase rotates a refresh token: the presented refresh token is
 * revoked and a new access/refresh token pair is issued.
 *
 * <p>If an already revoked (i.e. already rotated) refresh token is
 * presented again, this is treated as a sign of token theft - every
 * refresh token of the owning user is revoked, forcing a fresh login on
 * all devices.
 *
 * @author Marius Birk
 */
@Component
public class RefreshTokenUsecase {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final RefreshTokenIssuer refreshTokenIssuer;
  private final UserPersistenceConverter persistenceConverter;

  /**
   * Basic Constructor takes all collaborators needed to rotate a refresh token.
   *
   * @param refreshTokenRepository is used to look up and revoke refresh tokens
   * @param userRepository is used to look up the owning user
   * @param tokenService is used to hash the presented refresh token
   * @param refreshTokenIssuer issues the new access/refresh token pair
   */
  public RefreshTokenUsecase(
      RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository,
      TokenService tokenService,
      RefreshTokenIssuer refreshTokenIssuer) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.tokenService = tokenService;
    this.refreshTokenIssuer = refreshTokenIssuer;
    this.persistenceConverter = new UserPersistenceConverter();
  }

  /**
   * Rotates the given raw refresh token and issues a new token pair.
   *
   * @param rawRefreshToken the raw refresh token presented by the client
   * @return the response body and the new raw refresh token for the cookie
   * @throws InvalidTokenException if the token is unknown or was reused
   * @throws TokenExpiredException if the token is known but expired
   */
  public AuthResult refresh(String rawRefreshToken) {
    String tokenHash = this.tokenService.hash(rawRefreshToken);
    RefreshTokenPersistence stored = this.refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(InvalidTokenException::new);

    if (stored.isRevoked()) {
      this.refreshTokenRepository.deleteAllByUserId(stored.getUserId());
      throw new InvalidTokenException();
    }

    if (stored.getExpiresAt().isBefore(Instant.now())) {
      throw new TokenExpiredException();
    }

    revoke(stored);

    UserPersistence userPersistence = this.userRepository.findById(stored.getUserId())
        .orElseThrow();
    User user = this.persistenceConverter.convertFromDto(userPersistence);

    return this.refreshTokenIssuer.issueTokenPair(user);
  }

  private void revoke(RefreshTokenPersistence stored) {
    RefreshTokenPersistence revoked = new RefreshTokenPersistence(
        stored.getId(),
        stored.getUserId(),
        stored.getTokenHash(),
        stored.getExpiresAt(),
        true,
        stored.getCreatedAt()
    );
    this.refreshTokenRepository.save(revoked);
  }
}
