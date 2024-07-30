package de.birk.calory.usecase.recipe;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.recipe.converter.RecipeDetailsDtoConverter;
import de.birk.calory.usecase.recipe.converter.RecipePersistenceConverter;

import java.util.List;
import java.util.UUID;

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

  public List<RecipeDetailsDto> findAllRecipes(){
    List<RecipePersistence> recipePersistence = this.recipeRepository.findAll();
    List<Recipe> recipes = recipePersistenceConverter.convertFromDtos(recipePersistence);

    return this.recipeDetailsDtoConverter.convertFromEntities(recipes);
  }
}
