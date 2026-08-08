package de.birk.calory.usecase.user.converter;

import de.birk.calory.adapter.secondary.model.UserPersistence;
import de.birk.calory.domain.user.ActivityLevel;
import de.birk.calory.domain.user.User;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * @author Marius Birk
 */
public class UserPersistenceConverter extends Converter<UserPersistence, User> {

  /**
   * Converts from a UserPersistence to a User entity and back.
   */
  public UserPersistenceConverter() {
    super(
        dto -> new User(
            dto.getId(),
            dto.getEmail(),
            dto.getPasswordHash(),
            dto.getHeight(),
            dto.getWeight(),
            dto.getActivityLevel() == null ? null : ActivityLevel.valueOf(dto.getActivityLevel()),
            dto.getCreatedAt()
        ),
        entity -> new UserPersistence(
            entity.getId(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getHeight(),
            entity.getWeight(),
            entity.getActivityLevel() == null ? null : entity.getActivityLevel().name(),
            entity.getCreatedAt()
        )
    );
  }
}
