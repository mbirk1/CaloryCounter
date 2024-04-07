package de.birk.calory.adapter.primary;

import de.birk.calory.usecase.recipe.CreateRecipeUsecase;
import de.birk.calory.usecase.recipe.FindRecipeUsecase;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipe")
public class RecipeRestController {

    private FindRecipeUsecase findRecipeUsecase;

    private CreateRecipeUsecase createRecipeUsecase;

    public RecipeRestController(
        FindRecipeUsecase findRecipeUsecase,
        CreateRecipeUsecase createRecipeUsecase
    ){
        this.findRecipeUsecase = findRecipeUsecase;
        this.createRecipeUsecase = createRecipeUsecase;

    }
}
