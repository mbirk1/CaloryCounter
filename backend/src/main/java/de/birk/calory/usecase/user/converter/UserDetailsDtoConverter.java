package de.birk.calory.usecase.user.converter;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.domain.user.User;

/**
 * Maps a {@link User} to the {@link UserDetailsDto} returned by the API.
 *
 * <p>Unlike {@link de.birk.calory.usecase.converter.Converter}, this mapping
 * is intentionally one-directional only: a {@link UserDetailsDto} never
 * carries the password hash or the creation timestamp, so it cannot be used
 * to safely reconstruct a {@link User}.
 *
 * @author Marius Birk
 */
public class UserDetailsDtoConverter {

  /**
   * Converts a user entity into its details representation for API responses.
   *
   * @param user the user entity
   * @return the details dto, including the profileCompleted flag
   */
  public UserDetailsDto toDto(User user) {
    return new UserDetailsDto(
        user.getId(),
        user.getEmail(),
        user.getHeight(),
        user.getWeight(),
        user.getActivityLevel() == null ? null : user.getActivityLevel().name(),
        user.isProfileComplete()
    );
  }
}
