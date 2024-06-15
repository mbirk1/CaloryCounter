package de.birk.calory.adapter.primary;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PostRequest;
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

  @GetRequest("/{id}")
  public RecipeDetailsDto getRecipe(@PathVariable UUID id) {
    return this.findRecipeUsecase.findRecipeById(id);
  }

  @PostRequest
  public ResponseEntity<RecipeDetailsDto> createRecipe(@RequestBody RecipeDto recipeDto) {
    RecipeDetailsDto dto = this.createRecipeUsecase.createRecipe(recipeDto);
    return new ResponseEntity<>(dto, HttpStatus.CREATED);
  }
}