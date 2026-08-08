package de.birk.calory.usecase.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.User;
import de.birk.calory.usecase.user.converter.UserDetailsDtoConverter;
import de.birk.calory.usecase.user.converter.UserPersistenceConverter;

/**
 * Usecase to find users in the repository.
 *
 * @author Marius Birk
 */
@Service
public class FindUserUsecase {

  private final UserRepository userRepository;
  private final UserPersistenceConverter persistenceConverter;
  private final UserDetailsDtoConverter detailsDtoConverter;

  /**
   * Simple constructor that takes a repository and creates corresponding converters.
   *
   * @param userRepository the user repository that connects to the database
   */
  public FindUserUsecase(UserRepository userRepository) {
    this.userRepository = userRepository;
    this.persistenceConverter = new UserPersistenceConverter();
    this.detailsDtoConverter = new UserDetailsDtoConverter();
  }

  /**
   * Finds a specific user by its uuid.
   *
   * @param id the identifier of the user
   * @return the user details
   */
  public UserDetailsDto findById(UUID id) {
    UserPersistence userPersistence = this.userRepository.findById(id).orElseThrow();
    User user = this.persistenceConverter.convertFromDto(userPersistence);
    return this.detailsDtoConverter.toDto(user);
  }
}
