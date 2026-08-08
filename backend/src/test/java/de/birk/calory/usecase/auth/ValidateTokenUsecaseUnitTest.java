package de.birk.calory.usecase.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
public class ValidateTokenUsecaseUnitTest {

  @Mock
  private TokenService tokenService;

  @InjectMocks
  private ValidateTokenUsecase validateTokenUsecase;

  @Test
  public void extractsPrincipalFromValidTokenTest() {
    // Arrange
    UUID userId = UUID.randomUUID();
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "HS256")
        .subject(userId.toString())
        .claim("email", "user@example.com")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
    when(tokenService.parseAndValidate("token")).thenReturn(jwt);

    // Act
    TokenPrincipal principal = validateTokenUsecase.validate("token");

    // Assert
    assertThat(principal.getId()).isEqualTo(userId);
    assertThat(principal.getEmail()).isEqualTo("user@example.com");
  }
}
