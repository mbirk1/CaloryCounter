package de.birk.calory.adapter.secondary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.RecipePersistence;

/**
 * Basic Spring Boot Repository.
 *
 * @author Marius Birk
 */
@Repository
public interface RecipeRepository extends JpaRepository<RecipePersistence, UUID> {
}
