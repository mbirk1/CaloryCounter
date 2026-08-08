package de.birk.calory.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.adapter.secondary.UserRepository;
import de.birk.calory.adapter.secondary.model.UserPersistence;

@ExtendWith(MockitoExtension.class)
public class FindUserUsecaseUnitTest {

  @Mock
  private UserRepository userRepository;

  @Test
  public void findsUserByIdTest() {
    // Arrange
    FindUserUsecase findUserUsecase = new FindUserUsecase(userRepository);
    UUID id = UUID.randomUUID();
    UserPersistence userPersistence = new UserPersistence(
        id, "user@example.com", "hash", null, null, null, Instant.now());
    when(userRepository.findById(id)).thenReturn(Optional.of(userPersistence));

    // Act
    UserDetailsDto result = findUserUsecase.findById(id);

    // Assert
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getEmail()).isEqualTo("user@example.com");
  }
}
