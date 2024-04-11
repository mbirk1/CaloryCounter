package de.birk.calory.usecase.recipe;

import java.util.function.Function;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.converter.Converter;

public class RecipeDetailsDtoConverter extends Converter<RecipeDetailsDto, Recipe> {
  public RecipeDetailsDtoConverter() {
    super(
        dto -> new Recipe(dto.getId(), dto.getName(), dto.getFoods().stream().map(
            food -> new Food(food.getId(), food.getName(), food.getCalory(), food.getGrams())
        ).toList()),
        entity -> new RecipeDetailsDto(entity.getId(), entity.getName(),
            entity.getFoods().stream().map(
                food -> new Food(food.getId(), food.getName(), food.getCalory(), food.getGrams())
            ).toList())
    );
  }
}
