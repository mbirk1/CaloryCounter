package de.birk.calory.usecase.recipe.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeFoodEntryDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.domain.recipe.RecipeFoodEntry;
import de.birk.calory.exception.ValidationException;

public class RecipeDetailsDtoConverterTest {

  @Test
  public void convertEntityToDtoIncludesAggregatesAndFoodEntriesTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Food food = new Food(UUID.randomUUID(), "Food", BigDecimal.valueOf(200),
        BigDecimal.valueOf(100));
    Recipe entity = new Recipe(UUID.randomUUID(), "Recipe",
        List.of(new RecipeFoodEntry(food, BigDecimal.valueOf(50))), 2);

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    RecipeFoodEntryDetailsDto firstEntry = dto.getFoods().getFirst();
    assertThat(dto.getUuid()).isEqualTo(entity.getId());
    assertThat(dto.getName()).isEqualTo(entity.getName());
    assertThat(dto.getServings()).isEqualTo(2);
    assertThat(dto.getFoods()).hasSize(1);
    assertThat(firstEntry.getFood().getUuid()).isEqualTo(food.getId());
    assertThat(firstEntry.getFood().getName()).isEqualTo(food.getName());
    assertThat(firstEntry.getAmountGrams()).isEqualByComparingTo("50");
    assertThat(dto.getTotalCalories()).isEqualByComparingTo("100.00");
    assertThat(dto.getCaloriesPerServing()).isEqualByComparingTo("50.00");
    assertThat(dto.getDiet()).isEqualTo(entity.getDiet().name());
  }

  @Test
  public void convertDtoToEntityReconstructsEntriesTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    UUID foodId = UUID.randomUUID();
    RecipeDetailsDto dto = new RecipeDetailsDto(
        UUID.randomUUID(), "Recipe", 1,
        List.of(new RecipeFoodEntryDetailsDto(
            new FoodDetailsDto(foodId, "Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100)),
            BigDecimal.valueOf(100)
        )),
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        "VEGAN"
    );

    // Act
    Recipe entity = converter.convertFromDto(dto);

    // Assert
    assertThat(entity.getId()).isEqualTo(dto.getUuid());
    assertThat(entity.getName()).isEqualTo(dto.getName());
    assertThat(entity.getServings()).isEqualTo(1);
    assertThat(entity.getEntries()).hasSize(1);
    assertThat(entity.getEntries().getFirst().getFood().getId()).isEqualTo(foodId);
    assertThat(entity.getEntries().getFirst().getAmountGrams()).isEqualByComparingTo("100");
  }

  @Test
  public void convertEntityWithEmptyEntriesToDtoTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Recipe entity = new Recipe(UUID.randomUUID(), "Recipe", List.of(), 1);

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    assertThat(dto.getFoods()).isEmpty();
    assertThat(dto.getTotalCalories()).isEqualByComparingTo("0.00");
    assertThat(dto.getDiet()).isEqualTo("VEGAN");
  }

  @Test
  public void convertEntityWithNullIdToDtoTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    Recipe entity = new Recipe(null, "Recipe", List.of(), 1);

    // Act
    RecipeDetailsDto dto = converter.convertFromEntity(entity);

    // Assert
    assertThat(dto.getUuid()).isNull();
    assertThat(entity.getName()).isEqualTo(dto.getName());
  }

  @Test
  public void convertDtoWithNullNameToEntityThrowsValidationExceptionTest() {
    // Arrange
    RecipeDetailsDtoConverter converter = new RecipeDetailsDtoConverter();
    RecipeDetailsDto dto = new RecipeDetailsDto(
        UUID.randomUUID(), null, 1, List.of(),
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
        "VEGAN"
    );

    // Act & Assert
    assertThatThrownBy(() -> converter.convertFromDto(dto))
        .isInstanceOf(ValidationException.class);
  }
}
