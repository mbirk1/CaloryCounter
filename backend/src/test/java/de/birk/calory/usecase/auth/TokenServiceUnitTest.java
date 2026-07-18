package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;

public class TokenServiceUnitTest {

  private static final String SECRET = "unit-test-jwt-signing-secret-at-least-32-bytes-long";

  @Test
  public void issueAndValidateAccessTokenTest() {
    // Arrange
    TokenService tokenService = new TokenService(SECRET);
    UUID userId = UUID.randomUUID();
    String email = "user@example.com";

    // Act
    String token = tokenService.issueAccessToken(userId, email);
    Jwt jwt = tokenService.parseAndValidate(token);

    // Assert
    assertThat(jwt.getSubject()).isEqualTo(userId.toString());
    assertThat(jwt.getClaimAsString("email")).isEqualTo(email);
  }

  @Test
  public void parseAndValidateWithGarbageTokenTest() {
    // Arrange
    TokenService tokenService = new TokenService(SECRET);

    // Act & Assert
    assertThatThrownBy(() -> tokenService.parseAndValidate("not-a-jwt"))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  public void parseAndValidateWithWrongSignatureTest() {
    // Arrange
    TokenService issuer = new TokenService(SECRET);
    TokenService validator = new TokenService("a-completely-different-signing-secret-32-bytes!");
    String token = issuer.issueAccessToken(UUID.randomUUID(), "user@example.com");

    // Act & Assert
    assertThatThrownBy(() -> validator.parseAndValidate(token))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  public void parseAndValidateWithExpiredTokenTest() throws Exception {
    // Arrange
    TokenService tokenService = new TokenService(SECRET);
    String expiredToken = buildExpiredToken();

    // Act & Assert
    assertThatThrownBy(() -> tokenService.parseAndValidate(expiredToken))
        .isInstanceOf(TokenExpiredException.class);
  }

  @Test
  public void hashIsDeterministicTest() {
    // Arrange
    TokenService tokenService = new TokenService(SECRET);

    // Act
    String first = tokenService.hash("raw-token-value");
    String second = tokenService.hash("raw-token-value");

    // Assert
    assertThat(first).isEqualTo(second);
    assertThat(first).hasSize(64);
  }

  @Test
  public void generateOpaqueRefreshTokenIsRandomTest() {
    // Arrange
    TokenService tokenService = new TokenService(SECRET);

    // Act
    String first = tokenService.generateOpaqueRefreshToken();
    String second = tokenService.generateOpaqueRefreshToken();

    // Assert
    assertThat(first).isNotEqualTo(second);
  }

  private String buildExpiredToken() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(UUID.randomUUID().toString())
        .issueTime(Date.from(Instant.now().minusSeconds(3600)))
        .expirationTime(Date.from(Instant.now().minusSeconds(1800)))
        .build();
    SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    signedJwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
    return signedJwt.serialize();
  }
}
