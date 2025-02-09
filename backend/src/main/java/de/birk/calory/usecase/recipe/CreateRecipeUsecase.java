package de.birk.calory.usecase.recipe;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.recipe.converter.RecipeDetailsDtoConverter;
import de.birk.calory.usecase.recipe.converter.RecipeDtoConverter;
import de.birk.calory.usecase.recipe.converter.RecipePersistenceConverter;

/**
 * Usecase to create a recipe.
 *
 * @author Marius Birk
 */
@Component
public class CreateRecipeUsecase {

  private final RecipeRepository recipeRepository;
  private final RecipeDtoConverter recipeDtoConverter;
  private final RecipePersistenceConverter recipePersistenceConverter;
  private final RecipeDetailsDtoConverter recipeDetailsDtoConverter;

  /**
   * Simple constructor that receives a repository and creates corresponding converters.
   *
   * @param recipeRepository repository for recipes
   */
  public CreateRecipeUsecase(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
    this.recipeDtoConverter = new RecipeDtoConverter();
    this.recipePersistenceConverter = new RecipePersistenceConverter();
    this.recipeDetailsDtoConverter = new RecipeDetailsDtoConverter();
  }

  /**
   * Creates a new Recipe and returns it.
   *
   * @param dto the dto with all necessary properties
   *
   * @return a new recipe with a generated uuid
   */
  public RecipeDetailsDto createRecipe(RecipeDto dto) {
    Recipe recipe = this.recipeDtoConverter.convertFromDto(dto);
    RecipePersistence recipePersistence = this.recipePersistenceConverter.convertFromEntity(recipe);
    this.recipeRepository.save(recipePersistence);

    return this.recipeDetailsDtoConverter.convertFromEntity(recipe);
  }
}
