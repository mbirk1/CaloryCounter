package de.birk.calory.adapter.secondary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.RecipePersistence;

@Repository
public interface RecipeRepository extends JpaRepository<RecipePersistence, UUID> {
}
