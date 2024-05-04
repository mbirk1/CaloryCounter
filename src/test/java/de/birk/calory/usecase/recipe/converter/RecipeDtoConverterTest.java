package de.birk.calory.usecase.recipe.converter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.exception.ValidationException;

public class RecipeDtoConverterTest {

  @Test
  public void convertRecipeDtoToEntitySuccessfullyTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    RecipeDto recipeDto = new RecipeDto("Recipe 1", List.of(
        new FoodDetailsDto(
            UUID.randomUUID(),
            "Food 1",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        ),
        new FoodDetailsDto(
            UUID.randomUUID(),
            "Food 2",
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(300)
        )
    ));

    // Act
    Recipe recipe = converter.convertFromDto(recipeDto);

    // Assert
    FoodDetailsDto firstDto = recipeDto.getFoods().getFirst();
    FoodDetailsDto secondDto = recipeDto.getFoods().get(1);
    Food firstFood = recipe.getFoods().getFirst();
    Food secondFood = recipe.getFoods().get(1);

    Assertions.assertThat(recipeDto.getName()).isEqualTo(recipe.getName());
    Assertions.assertThat(recipeDto.getFoods().size()).isEqualTo(recipe.getFoods().size());
    Assertions.assertThat(firstDto.getName()).isEqualTo(firstFood.getName());
    Assertions.assertThat(firstDto.getCalory()).isEqualTo(firstFood.getCalory());
    Assertions.assertThat(firstDto.getGrams()).isEqualTo(firstFood.getGrams());
    Assertions.assertThat(secondDto.getName()).isEqualTo(secondFood.getName());
    Assertions.assertThat(secondDto.getCalory()).isEqualTo(secondFood.getCalory());
    Assertions.assertThat(secondDto.getGrams()).isEqualTo(secondFood.getGrams());
  }

  @Test
  public void convertEntityToRecipeDtoSuccessfullyTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    Recipe recipe = new Recipe(UUID.randomUUID(), "Recipe 1", List.of(
        new Food(
            UUID.randomUUID(),
            "Food 1",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        ),
        new Food(
            UUID.randomUUID(),
            "Food 2",
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(300)
        )
    ));

    // Act
    RecipeDto recipeDto = converter.convertFromEntity(recipe);

    // Assert
    Food firstFood = recipe.getFoods().getFirst();
    Food secondFood = recipe.getFoods().get(1);
    FoodDetailsDto firstDto = recipeDto.getFoods().getFirst();
    FoodDetailsDto secondDto = recipeDto.getFoods().get(1);

    Assertions.assertThat(recipe.getName()).isEqualTo(recipeDto.getName());
    Assertions.assertThat(recipe.getFoods().size()).isEqualTo(recipeDto.getFoods().size());
    Assertions.assertThat(firstFood.getName()).isEqualTo(firstDto.getName());
    Assertions.assertThat(firstFood.getCalory()).isEqualTo(firstDto.getCalory());
    Assertions.assertThat(firstFood.getGrams()).isEqualTo(firstDto.getGrams());
    Assertions.assertThat(secondFood.getName()).isEqualTo(secondDto.getName());
    Assertions.assertThat(secondFood.getCalory()).isEqualTo(secondDto.getCalory());
    Assertions.assertThat(secondFood.getGrams()).isEqualTo(secondDto.getGrams());
  }

  @Test
  public void handleEmptyListOfFoodsInRecipeDtoTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    RecipeDto recipeDto = new RecipeDto("Recipe 1", List.of());

    // Act
    Recipe recipe = converter.convertFromDto(recipeDto);

    // Assert
    Assertions.assertThat(recipeDto.getName()).isEqualTo(recipe.getName());
    Assertions.assertThat(recipe.getFoods().isEmpty()).isTrue();
  }

  @Test
  public void handleEmptyListOfFoodsInRecipeEntityTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    Recipe recipe = new Recipe(UUID.randomUUID(), "Recipe 1", List.of());

    // Act
    RecipeDto recipeDto = converter.convertFromEntity(recipe);

    // Assert
    Assertions.assertThat(recipe.getName()).isEqualTo(recipeDto.getName());
    Assertions.assertThat(recipeDto.getFoods().isEmpty()).isTrue();
  }

//  @Test
//  public void handleNullValuesInRecipeDtoTest() {
//    // Arrange
//    RecipeDtoConverter converter = new RecipeDtoConverter();
//    RecipeDto recipeDto = new RecipeDto(null, null);
//
//    // Act
//    Recipe recipe = converter.convertFromDto(recipeDto);
//
//    // Assert
//    Assertions.assertThat(recipe.getName()).isNull();
//    Assertions.assertThat(recipe.getFoods().isEmpty()).isTrue();
//  }

  @Test
  public void throwExceptionWhenRecipeDtoIsNullTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    RecipeDto recipeDto = null;

    // Assert
    Assertions.assertThatThrownBy(()
        -> converter.convertFromDto(recipeDto)
    ).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void throwExceptionWhenRecipeEntityIsNullTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    Recipe recipe = null;

    // Assert
    Assertions.assertThatThrownBy(() ->
        converter.convertFromEntity(recipe)
    ).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void throwExceptionWhenRecipeDtoNameIsNullOrEmptyTest() {
    // Arrange
    RecipeDtoConverter converter = new RecipeDtoConverter();
    RecipeDto recipeDto1 = new RecipeDto(null, List.of());
    RecipeDto recipeDto2 = new RecipeDto("", List.of());

    // Assert
    Assertions.assertThatThrownBy(() ->
        converter.convertFromDto(recipeDto1)
    ).isInstanceOf(ValidationException.class);
    Assertions.assertThatThrownBy(() ->
        converter.convertFromDto(recipeDto2)
    ).isInstanceOf(ValidationException.class);
  }
}

