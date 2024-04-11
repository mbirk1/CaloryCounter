package de.birk.calory.usecase.recipe;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;

@Component
public class CreateRecipeUsecase {

  private final RecipeRepository recipeRepository;
  private RecipeDtoConverter recipeDtoConverter;
  private RecipePersistenceConverter recipePersistenceConverter;
  private RecipeDetailsDtoConverter recipeDetailsDtoConverter;

  public CreateRecipeUsecase(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
    this.recipeDtoConverter = new RecipeDtoConverter();
    this.recipePersistenceConverter = new RecipePersistenceConverter();
    this.recipeDetailsDtoConverter = new RecipeDetailsDtoConverter();
  }

  public RecipeDetailsDto createRecipe(RecipeDto dto) {
    Recipe recipe = this.recipeDtoConverter.convertFromDto(dto);
    RecipePersistence recipePersistence = this.recipePersistenceConverter.convertFromEntity(recipe);
    this.recipeRepository.save(recipePersistence);

    return this.recipeDetailsDtoConverter.convertFromEntity(recipe);

  }
}
