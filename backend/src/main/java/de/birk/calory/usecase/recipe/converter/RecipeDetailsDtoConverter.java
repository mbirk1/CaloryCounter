package de.birk.calory.usecase.recipe.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * @author Marius Birk
 */
public class RecipeDetailsDtoConverter extends Converter<RecipeDetailsDto, Recipe> {

  /**
   * Constructor. Converts detailed recipe dtos to recipes.
   */
  public RecipeDetailsDtoConverter() {
    super(
        dto -> new Recipe(
        dto.getId(),
        dto.getName(),
        dto.getFoods().stream().map(
          food -> new Food(
            food.getUuid(),
            food.getName(),
            food.getCalory(),
            food.getGrams()
        )
      ).toList()
      ),
        entity -> new RecipeDetailsDto(
        entity.getId(),
        entity.getName(),
        entity.getFoods().stream().map(
          food -> new FoodDetailsDto(
            food.getId(),
            food.getName(),
            food.getCalory(),
            food.getGrams()
          )
        ).toList())
    );
  }
}
