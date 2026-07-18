package de.birk.calory.adapter.secondary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Basic Spring Boot Repository.
 *
 * @author Marius Birk
 */
@Repository
public interface FoodRepository extends JpaRepository<FoodPersistence, UUID> {

  Optional<FoodPersistence> findByExternalId(String externalId);
}
