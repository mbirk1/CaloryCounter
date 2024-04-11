package de.birk.calory.usecase.recipe;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class FindRecipeUsecase {

  private final RecipeRepository recipeRepository;

  private RecipePersistenceConverter recipePersistenceConverter;
  private RecipeDetailsDtoConverter recipeDetailsDtoConverter;

  public FindRecipeUsecase(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
    this.recipeDetailsDtoConverter = new RecipeDetailsDtoConverter();
    this.recipePersistenceConverter = new RecipePersistenceConverter();
  }

  public RecipeDetailsDto findRecipeById(UUID id) {
    RecipePersistence recipePersistence = this.recipeRepository.findById(id)
        .orElseThrow();
    Recipe recipe = recipePersistenceConverter.convertFromDto(recipePersistence);
    return this.recipeDetailsDtoConverter.convertFromEntity(recipe);
  }
}
