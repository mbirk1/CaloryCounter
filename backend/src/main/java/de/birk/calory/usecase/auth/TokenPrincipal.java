package de.birk.calory.usecase.auth;

import java.util.UUID;

/**
 * Identity of the currently authenticated user, extracted from a validated
 * access token. Used as the principal of the
 * {@code org.springframework.security.core.Authentication} that the
 * {@code JwtAuthenticationFilter} puts into the security context.
 *
 * @author Marius Birk
 */
public class TokenPrincipal {

  private final UUID id;
  private final String email;

  public TokenPrincipal(UUID id, String email) {
    this.id = id;
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }
}
