package de.birk.calory.adapter.secondary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.RefreshTokenPersistence;

/**
 * Basic Spring Boot Repository for refresh tokens.
 *
 * @author Marius Birk
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenPersistence, UUID> {

  Optional<RefreshTokenPersistence> findByTokenHash(String tokenHash);

  void deleteAllByUserId(UUID userId);
}
