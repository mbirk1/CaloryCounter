package de.birk.calory.usecase.recipe.converter;

import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.usecase.converter.Converter;

public class RecipeDtoConverter extends Converter<RecipeDto, Recipe> {
  public RecipeDtoConverter() {
    super(
        dto -> new Recipe(UUID.randomUUID(), dto.getName(), dto.getFoods().stream()
            .map(foodDetailsDto -> new Food(
                foodDetailsDto.getUuid(),
                foodDetailsDto.getName(),
                foodDetailsDto.getCalory(),
                foodDetailsDto.getGrams())
            ).toList()),
        entity -> new RecipeDto(entity.getName(), entity.getFoods().stream()
            .map(food -> new FoodDetailsDto(
                food.getId(),
                food.getName(),
                food.getCalory(),
                food.getGrams()
            )).toList()
        )
    );
  }
}
