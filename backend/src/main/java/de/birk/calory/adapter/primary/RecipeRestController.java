package de.birk.calory.adapter.primary;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.DeleteRequest;
import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PostRequest;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.usecase.recipe.CreateRecipeUsecase;
import de.birk.calory.usecase.recipe.DeleteRecipeUsecase;
import de.birk.calory.usecase.recipe.FindRecipeUsecase;

/**
 * RestController for all recipe correlated requests.
 *
 * @author Marius Birk
 */
@RestController
@RequestMapping("/api/recipe")
@CrossOrigin(origins = "http://localhost:4200")
public class RecipeRestController {

  private final FindRecipeUsecase findRecipeUsecase;

  private final CreateRecipeUsecase createRecipeUsecase;

  private final DeleteRecipeUsecase deleteRecipeUsecase;

  /**
   * Constructor injecting all recipe usecases.
   *
   * @param findRecipeUsecase usecase to find recipes
   * @param createRecipeUsecase usecase to create a recipe
   * @param deleteRecipeUsecase usecase to delete a recipe
   */
  public RecipeRestController(
          FindRecipeUsecase findRecipeUsecase,
          CreateRecipeUsecase createRecipeUsecase,
          DeleteRecipeUsecase deleteRecipeUsecase
  ) {
    this.findRecipeUsecase = findRecipeUsecase;
    this.createRecipeUsecase = createRecipeUsecase;
    this.deleteRecipeUsecase = deleteRecipeUsecase;
  }

  @GetRequest("/{id}")
  public RecipeDetailsDto getRecipe(@PathVariable UUID id) {
    return this.findRecipeUsecase.findRecipeById(id);
  }

  @GetRequest()
  public List<RecipeDetailsDto> getAllRecipes() {
    return this.findRecipeUsecase.findAllRecipes();
  }

  @PostRequest
  public ResponseEntity<RecipeDetailsDto> createRecipe(@RequestBody RecipeDto recipeDto) {
    RecipeDetailsDto dto = this.createRecipeUsecase.createRecipe(recipeDto);
    return new ResponseEntity<>(dto, HttpStatus.CREATED);
  }

  @DeleteRequest("/{id}")
  public void deleteRecipe(@PathVariable UUID id) {
    this.deleteRecipeUsecase.deleteRecipe(id);
  }
}
