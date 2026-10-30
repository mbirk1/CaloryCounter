package de.birk.calory.usecase.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.adapter.primary.model.RecipeFoodEntryDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class CreateRecipeUsecaseUnitTest {

  @Mock
  private RecipeRepository recipeRepository;

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void createsARecipeWithResolvedFoodEntriesTest() {
    // Arrange
    UUID foodAId = UUID.randomUUID();
    UUID foodBId = UUID.randomUUID();
    when(this.foodRepository.findById(foodAId)).thenReturn(Optional.of(
        new FoodPersistence(foodAId, "Food A", BigDecimal.valueOf(200), BigDecimal.valueOf(100))
    ));
    when(this.foodRepository.findById(foodBId)).thenReturn(Optional.of(
        new FoodPersistence(foodBId, "Food B", BigDecimal.valueOf(50), BigDecimal.valueOf(100))
    ));
    when(this.recipeRepository.save(any(RecipePersistence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    RecipeDto dto = new RecipeDto("Recipe", 2, List.of(
        new RecipeFoodEntryDto(foodAId, BigDecimal.valueOf(100)),
        new RecipeFoodEntryDto(foodBId, BigDecimal.valueOf(200))
    ));
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act
    RecipeDetailsDto result = usecase.createRecipe(dto);

    // Assert
    assertThat(result.getName()).isEqualTo("Recipe");
    assertThat(result.getServings()).isEqualTo(2);
    assertThat(result.getFoods()).hasSize(2);
    // 200 + 100 = 300 total, 150 per serving
    assertThat(result.getTotalCalories()).isEqualByComparingTo("300.00");
    assertThat(result.getCaloriesPerServing()).isEqualByComparingTo("150.00");
  }

  @Test
  public void rejectsAnEmptyFoodsListTest() {
    // Arrange
    RecipeDto dto = new RecipeDto("Recipe", 1, List.of());
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createRecipe(dto)).isInstanceOf(ValidationException.class);
    verify(this.recipeRepository, never()).save(any());
  }

  @Test
  public void rejectsANullFoodsListTest() {
    // Arrange
    RecipeDto dto = new RecipeDto("Recipe", 1, null);
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createRecipe(dto)).isInstanceOf(ValidationException.class);
    verify(this.recipeRepository, never()).save(any());
  }

  @Test
  public void throwsNoSuchElementExceptionWhenAReferencedFoodDoesNotExistTest() {
    // Arrange
    UUID unknownFoodId = UUID.randomUUID();
    when(this.foodRepository.findById(unknownFoodId)).thenReturn(Optional.empty());
    RecipeDto dto = new RecipeDto("Recipe", 1, List.of(
        new RecipeFoodEntryDto(unknownFoodId, BigDecimal.valueOf(100))
    ));
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createRecipe(dto))
        .isInstanceOf(NoSuchElementException.class);
    verify(this.recipeRepository, never()).save(any());
  }

  @Test
  public void rejectsZeroServingsTest() {
    // Arrange
    UUID foodId = UUID.randomUUID();
    when(this.foodRepository.findById(foodId)).thenReturn(Optional.of(
        new FoodPersistence(foodId, "Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100))
    ));
    RecipeDto dto = new RecipeDto("Recipe", 0, List.of(
        new RecipeFoodEntryDto(foodId, BigDecimal.valueOf(100))
    ));
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createRecipe(dto)).isInstanceOf(ValidationException.class);
    verify(this.recipeRepository, never()).save(any());
  }

  @Test
  public void rejectsANonPositiveAmountGramsTest() {
    // Arrange
    UUID foodId = UUID.randomUUID();
    when(this.foodRepository.findById(foodId)).thenReturn(Optional.of(
        new FoodPersistence(foodId, "Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100))
    ));
    RecipeDto dto = new RecipeDto("Recipe", 1, List.of(
        new RecipeFoodEntryDto(foodId, BigDecimal.ZERO)
    ));
    CreateRecipeUsecase usecase = new CreateRecipeUsecase(this.recipeRepository, this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createRecipe(dto)).isInstanceOf(ValidationException.class);
    verify(this.recipeRepository, never()).save(any());
  }
}
