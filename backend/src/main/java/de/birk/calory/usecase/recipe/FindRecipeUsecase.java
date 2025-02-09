package de.birk.calory.usecase.recipe;

import java.util.List;
import java.util.UUID;

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
    RecipePersistence recipePersistence = this.recipeRepository.findById(id)
        .orElseThrow();
    Recipe recipe = recipePersistenceConverter.convertFromDto(recipePersistence);
    return this.recipeDetailsDtoConverter.convertFromEntity(recipe);
  }

  /**
   * Find all Recipes.
   *
   * @return all found recipes with all the needed food items
   */
  public List<RecipeDetailsDto> findAllRecipes() {
    List<RecipePersistence> recipePersistence = this.recipeRepository.findAll();
    List<Recipe> recipes = recipePersistenceConverter.convertFromDtos(recipePersistence);

    return this.recipeDetailsDtoConverter.convertFromEntities(recipes);
  }
}
