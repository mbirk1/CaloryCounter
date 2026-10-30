package de.birk.calory.usecase.recipe;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.recipe.converter.RecipeDetailsDtoConverter;
import de.birk.calory.usecase.recipe.converter.RecipePersistenceConverter;

/**
 * Usecase to find recipes.
 *
 * @author Marius Birk
 */
@Service
public class FindRecipeUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(FindRecipeUsecase.class);

  private final RecipeRepository recipeRepository;

  private final RecipePersistenceConverter recipePersistenceConverter;
  private final RecipeDetailsDtoConverter recipeDetailsDtoConverter;

  /**
   * Simple Constructor, that takes a repository and created corresponding converters.
   *
   * @param recipeRepository a recipe repository, connected to the database
   */
  public FindRecipeUsecase(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
    this.recipeDetailsDtoConverter = new RecipeDetailsDtoConverter();
    this.recipePersistenceConverter = new RecipePersistenceConverter();
  }

  /**
   * Find a recipe by his uuid.
   *
   * @param id the identifier of the recipe
   *
   * @return the found recipe with all needed foods
   */
  public RecipeDetailsDto findRecipeById(UUID id) {
    LOG.debug("Rezept-Anfrage fuer ID {} erhalten", id);
    RecipePersistence recipePersistence = this.recipeRepository.findById(id)
        .orElseThrow(() -> {
          LOG.warn("Rezept-Anfrage fuer nicht vorhandene ID {} abgelehnt", id);
          return new NoSuchElementException("Recipe not found");
        });
    Recipe recipe = recipePersistenceConverter.convertFromDto(recipePersistence);
    return this.recipeDetailsDtoConverter.convertFromEntity(recipe);
  }

  /**
   * Find all Recipes.
   *
   * @return all found recipes with all the needed food items
   */
  public List<RecipeDetailsDto> findAllRecipes() {
    LOG.debug("Anfrage fuer alle Rezepte erhalten");
    List<RecipePersistence> recipePersistence = this.recipeRepository.findAll();
    List<Recipe> recipes = recipePersistenceConverter.convertFromDtos(recipePersistence);

    List<RecipeDetailsDto> result = this.recipeDetailsDtoConverter.convertFromEntities(recipes);
    LOG.debug("Anfrage fuer alle Rezepte ausgefuehrt: {} Treffer", result.size());
    return result;
  }
}
