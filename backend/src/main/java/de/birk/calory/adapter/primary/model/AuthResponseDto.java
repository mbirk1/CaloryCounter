package de.birk.calory.adapter.primary.model;

/**
 * The DTO that is returned to clients after a successful registration,
 * login or token refresh. The refresh token itself is never part of this
 * body, it is transported as an HttpOnly cookie instead.
 *
 * @author Marius Birk
 */
public class AuthResponseDto {

  private String accessToken;
  private long expiresIn;
  private String tokenType;
  private boolean profileCompleted;

  public AuthResponseDto() {
  }

  /**
   * Constructor that takes all properties needed to describe an authentication result.
   *
   * @param accessToken the signed JWT access token
   * @param expiresIn the validity of the access token in seconds
   * @param tokenType the token type, always {@code Bearer}
   * @param profileCompleted whether the onboarding of the user has already been completed
   */
  public AuthResponseDto(
      String accessToken, long expiresIn, String tokenType, boolean profileCompleted) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
    this.tokenType = tokenType;
    this.profileCompleted = profileCompleted;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }

  public boolean isProfileCompleted() {
    return profileCompleted;
  }
}
