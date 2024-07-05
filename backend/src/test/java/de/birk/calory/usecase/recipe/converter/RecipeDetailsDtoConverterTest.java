package de.birk.calory.usecase.recipe.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.exception.ValidationException;

public class RecipeDetailsDtoConverterTest {
  @Test
  public void convertValidRecipeDetailsDtoToEntityTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    RecipeDetailsDto dto = new RecipeDetailsDto(UUID.randomUUID(), "Recipe",
        List.of((new FoodDetailsDto(UUID.randomUUID(), "Food", BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)))));

    // Act
    Recipe entity = converter.convertFromDto(dto);

    // Assert
    Food firstFood = entity.getFoods().getFirst();
    FoodDetailsDto firstDetail = dto.getFoods().getFirst();

    assertThat(entity).isNotNull();
    assertThat(dto.getId()).isEqualTo(entity.getId());
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(dto.getFoods().size()).isEqualTo(entity.getFoods().size());
    assertThat(firstDetail.getUuid()).isEqualTo(firstFood.getId());
    assertThat(firstDetail.getName()).isEqualTo(firstFood.getName());
    assertThat(firstDetail.getCalory()).isEqualTo(firstFood.getCalory());
    assertThat(firstDetail.getGrams()).isEqualTo(firstFood.getGrams());
  }

  @Test
  public void convertEntityToValidRecipeDetailsDtoTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Recipe entity = new Recipe(UUID.randomUUID(), "Recipe",
        List.of(new Food(
            UUID.randomUUID(),
            "Food",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        )));

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    Food firstFood = entity.getFoods().getFirst();
    FoodDetailsDto firstDetail = dto.getFoods().getFirst();

    assertThat(dto).isNotNull();
    assertThat(entity.getId()).isEqualTo(dto.getId());
    assertThat(entity.getName()).isEqualTo(dto.getName());
    assertThat(entity.getFoods().size()).isEqualTo(dto.getFoods().size());
    assertThat(firstFood.getId()).isEqualTo(firstDetail.getUuid());
    assertThat(firstFood.getName()).isEqualTo(firstDetail.getName());
    assertThat(firstFood.getCalory()).isEqualTo(firstDetail.getCalory());
    assertThat(firstFood.getGrams()).isEqualTo(firstDetail.getGrams());
  }

  @Test
  public void convertDtoWithEmptyFoodsListToEntityTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    RecipeDetailsDto dto =
        new RecipeDetailsDto(UUID.randomUUID(), "Recipe", List.of());

    // Act
    Recipe entity = converter.convertFromDto(dto);

    // Assert
    assertThat(entity).isNotNull();
    assertThat(dto.getId()).isEqualTo(entity.getId());
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(entity.getFoods().isEmpty()).isTrue();
  }

  @Test
  public void convertEntityWithEmptyFoodsListToDtoTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Recipe entity = new Recipe(UUID.randomUUID(), "Recipe", List.of());

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    assertThat(dto).isNotNull();
    assertThat(entity.getId()).isEqualTo(dto.getId());
    assertThat(entity.getName()).isEqualTo(dto.getName());
    assertThat(dto.getFoods().isEmpty()).isTrue();
  }

  @Test
  public void convertDtoWithNullIdToEntityTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    RecipeDetailsDto dto = new RecipeDetailsDto(null, "Recipe",
        List.of(new FoodDetailsDto(
            UUID.randomUUID(),
            "Food",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        )));

    // Act
    Recipe entity = converter.convertFromDto(dto);

    // Assert
    FoodDetailsDto firstDetails = dto.getFoods().getFirst();
    Food firstFood = entity.getFoods().getFirst();

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(dto.getFoods().size()).isEqualTo(entity.getFoods().size());
    assertThat(firstDetails.getUuid()).isEqualTo(firstFood.getId());
    assertThat(firstDetails.getName()).isEqualTo(firstFood.getName());
    assertThat(firstDetails.getCalory()).isEqualTo(firstFood.getCalory());
    assertThat(firstDetails.getGrams()).isEqualTo(firstFood.getGrams());
  }

  @Test
  public void convertEntityWithNullIdToDtoTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Recipe entity = new Recipe(null, "Recipe",
        List.of(new Food(
            UUID.randomUUID(),
            "Food",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        )));

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    Food firstFood = entity.getFoods().getFirst();
    FoodDetailsDto firstDetail = dto.getFoods().getFirst();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isNull();
    assertThat(entity.getName()).isEqualTo(dto.getName());
    assertThat(entity.getFoods().size()).isEqualTo(dto.getFoods().size());
    assertThat(firstFood.getId()).isEqualTo(firstDetail.getUuid());
    assertThat(firstFood.getName()).isEqualTo(firstDetail.getName());
    assertThat(firstFood.getCalory()).isEqualTo(firstDetail.getCalory());
    assertThat(firstFood.getGrams()).isEqualTo(firstDetail.getGrams());
  }

  @Test
  public void convertDtoWithNullNameToEntityTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    RecipeDetailsDto dto = new RecipeDetailsDto(UUID.randomUUID(), null,
        List.of(new FoodDetailsDto(
            UUID.randomUUID(),
            "Food",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(200)
        )));

    // Act
    assertThatThrownBy(() ->
        converter.convertFromDto(dto)
    ).isInstanceOf(ValidationException.class);
  }
}
