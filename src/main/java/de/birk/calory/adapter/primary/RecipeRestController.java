package de.birk.calory.adapter.primary;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.usecase.recipe.CreateRecipeUsecase;
import de.birk.calory.usecase.recipe.FindRecipeUsecase;

@RestController
@RequestMapping("/recipe")
public class RecipeRestController {

  private FindRecipeUsecase findRecipeUsecase;

  private CreateRecipeUsecase createRecipeUsecase;

  public RecipeRestController(
      FindRecipeUsecase findRecipeUsecase,
      CreateRecipeUsecase createRecipeUsecase
  ) {
    this.findRecipeUsecase = findRecipeUsecase;
    this.createRecipeUsecase = createRecipeUsecase;
  }

  @GetMapping("/{id}")
  public RecipeDetailsDto getRecipe(@PathVariable UUID id) {
    return this.findRecipeUsecase.findRecipeById(id);
  }

  @PostMapping
  public RecipeDetailsDto createRecipe(@RequestBody RecipeDto recipeDto) {
    return this.createRecipeUsecase.createRecipe(recipeDto);
  }
}