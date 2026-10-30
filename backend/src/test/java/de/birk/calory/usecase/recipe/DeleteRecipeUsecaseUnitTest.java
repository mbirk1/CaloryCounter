package de.birk.calory.usecase.recipe;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.secondary.RecipeRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteRecipeUsecaseUnitTest {

  @Mock
  private RecipeRepository recipeRepository;

  @Test
  public void deletesTheRecipeByIdTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(this.recipeRepository.existsById(id)).thenReturn(true);
    DeleteRecipeUsecase usecase = new DeleteRecipeUsecase(this.recipeRepository);

    // Act
    usecase.deleteRecipe(id);

    // Assert
    verify(this.recipeRepository).deleteById(id);
  }

  @Test
  public void throwsNoSuchElementExceptionWhenTheRecipeDoesNotExistTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(this.recipeRepository.existsById(id)).thenReturn(false);
    DeleteRecipeUsecase usecase = new DeleteRecipeUsecase(this.recipeRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.deleteRecipe(id))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("Recipe not found");
    verify(this.recipeRepository, never()).deleteById(id);
  }
}
