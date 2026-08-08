package de.birk.calory.adapter.secondary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.UserPersistence;

/**
 * Basic Spring Boot Repository for users.
 *
 * @author Marius Birk
 */
@Repository
public interface UserRepository extends JpaRepository<UserPersistence, UUID> {

  Optional<UserPersistence> findByEmail(String email);
}
