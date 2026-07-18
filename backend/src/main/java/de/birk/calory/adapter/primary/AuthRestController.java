package de.birk.calory.adapter.primary;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.PostRequest;
import de.birk.calory.adapter.primary.model.AuthResponseDto;
import de.birk.calory.adapter.primary.model.LoginRequestDto;
import de.birk.calory.adapter.primary.model.RegisterUserDto;
import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.usecase.auth.AuthResult;
import de.birk.calory.usecase.auth.LoginUsecase;
import de.birk.calory.usecase.auth.LogoutUsecase;
import de.birk.calory.usecase.auth.RefreshTokenUsecase;
import de.birk.calory.usecase.auth.RegisterUserUsecase;

/**
 * RestController for registration, login and token lifecycle requests.
 *
 * <p>The access token is always returned in the response body, the refresh
 * token is always transported as an HttpOnly, Secure, SameSite=Lax cookie
 * scoped to {@code /api/auth}, so that it is never exposed to client side
 * JavaScript and never sent on unrelated API calls.
 *
 * @author Marius Birk
 */
@RestController
@RequestMapping("/api/auth")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestController {

  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
  private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(14);

  private final RegisterUserUsecase registerUserUsecase;
  private final LoginUsecase loginUsecase;
  private final RefreshTokenUsecase refreshTokenUsecase;
  private final LogoutUsecase logoutUsecase;

  /**
   * Every necessary usecase needed to complete the incoming requests.
   *
   * @param registerUserUsecase Register User Usecase
   * @param loginUsecase Login Usecase
   * @param refreshTokenUsecase Refresh Token Usecase
   * @param logoutUsecase Logout Usecase
   */
  public AuthRestController(
      RegisterUserUsecase registerUserUsecase,
      LoginUsecase loginUsecase,
      RefreshTokenUsecase refreshTokenUsecase,
      LogoutUsecase logoutUsecase) {
    this.registerUserUsecase = registerUserUsecase;
    this.loginUsecase = loginUsecase;
    this.refreshTokenUsecase = refreshTokenUsecase;
    this.logoutUsecase = logoutUsecase;
  }

  @PostRequest("/register")
  public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterUserDto registerUserDto) {
    return withRefreshCookie(this.registerUserUsecase.register(registerUserDto));
  }

  //TODO Marius Add rate limiting / brute-force protection on this endpoint
  @PostRequest("/login")
  public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
    return withRefreshCookie(this.loginUsecase.login(loginRequestDto));
  }

  /**
   * Rotates the given refresh token and issues a new access/refresh token pair.
   *
   * @param refreshToken the raw refresh token read from the HttpOnly cookie
   * @return the new access token, with the rotated refresh token set as cookie
   */
  @PostRequest("/refresh")
  public ResponseEntity<AuthResponseDto> refresh(
      @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
    if (refreshToken == null) {
      throw new InvalidTokenException();
    }
    return withRefreshCookie(this.refreshTokenUsecase.refresh(refreshToken));
  }

  /**
   * Revokes the given refresh token and clears the refresh cookie.
   *
   * @param refreshToken the raw refresh token read from the HttpOnly cookie, may be null
   * @return an empty response with the refresh cookie cleared
   */
  @PostRequest("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
    this.logoutUsecase.logout(refreshToken);
    ResponseCookie expiredCookie = buildRefreshCookie("", Duration.ZERO);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
        .build();
  }

  private ResponseEntity<AuthResponseDto> withRefreshCookie(AuthResult result) {
    ResponseCookie cookie = buildRefreshCookie(result.getRefreshToken(), REFRESH_TOKEN_MAX_AGE);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(result.getResponse());
  }

  private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/api/auth")
        .maxAge(maxAge)
        .build();
  }
}
