package de.birk.calory.usecase.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.LoginRequestDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.User;
import de.birk.calory.exception.InvalidCredentialsException;
import de.birk.calory.usecase.user.converter.UserPersistenceConverter;

/**
 * This Usecase logs in an existing user by verifying their credentials.
 *
 * @author Marius Birk
 */
@Component
public class LoginUsecase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenIssuer refreshTokenIssuer;
  private final UserPersistenceConverter persistenceConverter;

  /**
   * Basic Constructor takes all collaborators needed to log in a user.
   *
   * @param userRepository is used to look up the user by email
   * @param passwordEncoder is used to verify the raw password against the stored hash
   * @param refreshTokenIssuer issues a fresh access/refresh token pair
   */
  public LoginUsecase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RefreshTokenIssuer refreshTokenIssuer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.refreshTokenIssuer = refreshTokenIssuer;
    this.persistenceConverter = new UserPersistenceConverter();
  }

  /**
   * Verifies the given credentials and, if valid, issues an access/refresh token pair.
   *
   * @param loginRequestDto the email and raw password to verify
   * @return the response body and the raw refresh token for the cookie
   * @throws InvalidCredentialsException if the email is unknown or the password is wrong
   */
  public AuthResult login(LoginRequestDto loginRequestDto) {
    UserPersistence userPersistence = this.userRepository.findByEmail(loginRequestDto.getEmail())
        .orElseThrow(InvalidCredentialsException::new);

    boolean passwordMatches = this.passwordEncoder.matches(
        loginRequestDto.getPassword(), userPersistence.getPasswordHash());
    if (!passwordMatches) {
      throw new InvalidCredentialsException();
    }

    User user = this.persistenceConverter.convertFromDto(userPersistence);
    return this.refreshTokenIssuer.issueTokenPair(user);
  }
}
