package de.birk.calory.usecase.user;

import java.util.UUID;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.adapter.primary.model.UserProfileDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.ActivityLevel;
import de.birk.calory.domain.user.User;
import de.birk.calory.exception.ValidationException;
import de.birk.calory.usecase.user.converter.UserDetailsDtoConverter;
import de.birk.calory.usecase.user.converter.UserPersistenceConverter;

/**
 * Usecase that completes the onboarding of a user by setting height, weight
 * and activity level.
 *
 * @author Marius Birk
 */
@Component
public class UpdateUserProfileUsecase {

  private final UserRepository userRepository;
  private final UserPersistenceConverter persistenceConverter;
  private final UserDetailsDtoConverter detailsDtoConverter;

  /**
   * Constructor for usecase.
   *
   * @param userRepository user data imaged through repository
   */
  public UpdateUserProfileUsecase(UserRepository userRepository) {
    this.userRepository = userRepository;
    this.persistenceConverter = new UserPersistenceConverter();
    this.detailsDtoConverter = new UserDetailsDtoConverter();
  }

  /**
   * Applies the onboarding profile data to the user with the given identifier.
   *
   * @param userId the identifier of the user
   * @param userProfileDto the submitted onboarding data
   * @return the updated user details
   */
  public UserDetailsDto updateProfile(UUID userId, UserProfileDto userProfileDto) {
    UserPersistence userPersistence = this.userRepository.findById(userId).orElseThrow();
    User user = this.persistenceConverter.convertFromDto(userPersistence);

    ActivityLevel activityLevel = parseActivityLevel(userProfileDto.getActivityLevel());
    User updatedUser =
        user.withProfile(userProfileDto.getHeight(), userProfileDto.getWeight(), activityLevel);

    this.userRepository.save(this.persistenceConverter.convertFromEntity(updatedUser));
    return this.detailsDtoConverter.toDto(updatedUser);
  }

  private ActivityLevel parseActivityLevel(String value) {
    if (value == null) {
      throw new ValidationException();
    }
    try {
      return ActivityLevel.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new ValidationException();
    }
  }
}
