package de.birk.calory.adapter.primary;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PostRequest;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.usecase.recipe.CreateRecipeUsecase;
import de.birk.calory.usecase.recipe.FindRecipeUsecase;

@RestController
@RequestMapping("/recipe")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:3000")
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

  @GetRequest()
  public List<RecipeDetailsDto> getAllRecipes(){
    return this.findRecipeUsecase.findAllRecipes();
  }

  @PostRequest
  public ResponseEntity<RecipeDetailsDto> createRecipe(@RequestBody RecipeDto recipeDto) {
    RecipeDetailsDto dto = this.createRecipeUsecase.createRecipe(recipeDto);
    return new ResponseEntity<>(dto, HttpStatus.CREATED);
  }
}