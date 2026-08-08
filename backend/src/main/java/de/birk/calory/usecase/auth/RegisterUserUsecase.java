package de.birk.calory.usecase.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.RegisterUserDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.User;
import de.birk.calory.exception.EmailAlreadyInUseException;
import de.birk.calory.usecase.user.converter.UserPersistenceConverter;

/**
 * This Usecase registers new users and logs them in immediately, so no
 * separate login call is needed after registration.
 *
 * @author Marius Birk
 */
@Component
public class RegisterUserUsecase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenIssuer refreshTokenIssuer;
  private final UserPersistenceConverter persistenceConverter;

  /**
   * Basic Constructor takes all collaborators needed to register a user.
   *
   * @param userRepository is used to store user entities
   * @param passwordEncoder is used to hash the raw password
   * @param refreshTokenIssuer issues the initial access/refresh token pair
   */
  public RegisterUserUsecase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RefreshTokenIssuer refreshTokenIssuer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.refreshTokenIssuer = refreshTokenIssuer;
    this.persistenceConverter = new UserPersistenceConverter();
  }

  /**
   * Registers a new user and immediately issues an access/refresh token pair.
   *
   * @param registerUserDto the email and raw password of the new user
   * @return the response body and the raw refresh token for the cookie
   * @throws EmailAlreadyInUseException if the email address is already registered
   */
  public AuthResult register(RegisterUserDto registerUserDto) {
    this.userRepository.findByEmail(registerUserDto.getEmail()).ifPresent(existing -> {
      throw new EmailAlreadyInUseException();
    });

    String passwordHash = this.passwordEncoder.encode(registerUserDto.getPassword());
    User user = new User(
        UUID.randomUUID(),
        registerUserDto.getEmail(),
        passwordHash,
        null,
        null,
        null,
        Instant.now()
    );

    UserPersistence userPersistence = this.persistenceConverter.convertFromEntity(user);
    this.userRepository.save(userPersistence);

    return this.refreshTokenIssuer.issueTokenPair(user);
  }
}
