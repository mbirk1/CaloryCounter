package de.birk.calory.usecase.recipe;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.secondary.RecipeRepository;

/**
 * Usecase for deleting a recipe.
 *
 * <p>Unlike {@code DeleteFoodUsecase}, no {@code DataIntegrityViolationException} handling is
 * needed here: {@code tab_calory_recipe_food.recipe_id} has an {@code ON DELETE CASCADE} clause,
 * so a recipe's food entries are removed automatically, and nothing else references a recipe as
 * a foreign key.
 *
 * @author Marius Birk
 */
@Component
public class DeleteRecipeUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteRecipeUsecase.class);

  private final RecipeRepository recipeRepository;

  public DeleteRecipeUsecase(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  /**
   * Deletes the recipe with the given identifier.
   *
   * @param id UUID and primary key of the recipe.
   */
  public void deleteRecipe(UUID id) {
    LOG.debug("Loeschanfrage fuer Rezept {} erhalten", id);
    if (!this.recipeRepository.existsById(id)) {
      LOG.warn("Loeschanfrage fuer nicht vorhandenes Rezept {}", id);
      throw new NoSuchElementException("Recipe not found");
    }
    this.recipeRepository.deleteById(id);
    LOG.info("Rezept {} erfolgreich geloescht", id);
  }
}
