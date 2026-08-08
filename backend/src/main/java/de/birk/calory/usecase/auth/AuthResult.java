package de.birk.calory.usecase.auth;

import de.birk.calory.adapter.primary.model.AuthResponseDto;

/**
 * Holds the outcome of an authentication operation: the response body that
 * is sent to the client and the raw (unhashed) refresh token that the
 * controller places into the HttpOnly cookie. The raw refresh token is
 * never persisted or logged, only its hash is.
 *
 * @author Marius Birk
 */
public class AuthResult {

  private final AuthResponseDto response;
  private final String refreshToken;

  public AuthResult(AuthResponseDto response, String refreshToken) {
    this.response = response;
    this.refreshToken = refreshToken;
  }

  public AuthResponseDto getResponse() {
    return response;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
