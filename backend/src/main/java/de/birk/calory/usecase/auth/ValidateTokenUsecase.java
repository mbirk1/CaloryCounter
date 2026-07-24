package de.birk.calory.usecase.auth;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;

/**
 * This Usecase validates a raw JWT access token and extracts the identity
 * of the requesting user from it. Used by the
 * {@code adapter.primary.security.JwtAuthenticationFilter} to authenticate
 * incoming requests without the primary adapter layer having to depend on
 * the JWT library directly.
 *
 * @author Marius Birk
 */
@Component
public class ValidateTokenUsecase {

  private final TokenService tokenService;

  public ValidateTokenUsecase(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  /**
   * Validates the given access token and extracts the authenticated principal.
   *
   * @param token the raw JWT access token
   * @return the identity of the authenticated user
   * @throws TokenExpiredException if the token is well-formed but expired
   * @throws InvalidTokenException if the token is malformed or has an invalid signature
   */
  public TokenPrincipal validate(String token) {
    Jwt jwt = this.tokenService.parseAndValidate(token);
    UUID userId = UUID.fromString(jwt.getSubject());
    String email = jwt.getClaimAsString("email");
    return new TokenPrincipal(userId, email);
  }
}
