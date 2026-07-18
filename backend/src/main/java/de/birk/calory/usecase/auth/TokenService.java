package de.birk.calory.usecase.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;

/**
 * Issues and validates self-signed JWT access tokens (HS256, via the Spring
 * Security OAuth2 Resource Server / Nimbus JOSE stack) and issues opaque
 * refresh tokens together with their SHA-256 hash for persistence.
 *
 * <p>This class deliberately only produces and reads tokens, it never
 * touches the database - persisting refresh tokens is the responsibility of
 * the calling usecases, so that the storage strategy can differ per flow
 * (register, login, refresh rotation).
 *
 * @author Marius Birk
 */
@Component
public class TokenService {

  private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60L;
  private static final String ISSUER = "calory-counter";
  private static final String EMAIL_CLAIM = "email";

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  /**
   * Builds the JWT encoder/decoder pair from the configured HMAC secret.
   *
   * @param secret the signing secret, injected from {@code jwt.secret}
   */
  public TokenService(@Value("${jwt.secret}") String secret) {
    SecretKeySpec secretKey =
        new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
    this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  /**
   * Issues a new, short-lived access token for the given user.
   *
   * @param userId the identifier of the user the token is issued for
   * @param email the email address of the user
   * @return the signed JWT access token
   */
  public String issueAccessToken(UUID userId, String email) {
    Instant now = Instant.now();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(ISSUER)
        .issuedAt(now)
        .expiresAt(now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS))
        .subject(userId.toString())
        .claim(EMAIL_CLAIM, email)
        .id(UUID.randomUUID().toString())
        .build();
    return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }

  /**
   * Parses and validates an access token.
   *
   * @param token the raw JWT access token
   * @return the decoded token with its claims
   * @throws TokenExpiredException if the token is well-formed but expired
   * @throws InvalidTokenException if the token is malformed or has an invalid signature
   */
  public Jwt parseAndValidate(String token) {
    try {
      return this.jwtDecoder.decode(token);
    } catch (JwtValidationException e) {
      // Nimbus/Spring Security do not expose a dedicated "expired" exception type,
      // the timestamp validator reports it as a JwtValidationException with a
      // message that mentions the expiry - this is the only reliable signal available.
      if (e.getMessage() != null && e.getMessage().toLowerCase().contains("expired")) {
        throw new TokenExpiredException();
      }
      throw new InvalidTokenException();
    } catch (JwtException e) {
      throw new InvalidTokenException();
    }
  }

  public long getAccessTokenTtlSeconds() {
    return ACCESS_TOKEN_TTL_SECONDS;
  }

  /**
   * Generates a new cryptographically random opaque refresh token value.
   *
   * @return a URL-safe, base64 encoded random token
   */
  public String generateOpaqueRefreshToken() {
    byte[] randomBytes = new byte[64];
    new SecureRandom().nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  /**
   * Hashes a raw refresh token so that only its hash needs to be persisted.
   *
   * @param rawToken the raw refresh token value
   * @return the hex encoded SHA-256 hash of the token
   */
  public String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
