package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.FoodInUseException;

@ExtendWith(MockitoExtension.class)
public class DeleteFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void deletesTheFoodByIdTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence toDelete = new FoodPersistence(
        id, "Tofu", new BigDecimal("120"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(toDelete));
    DeleteFoodUsecase usecase = new DeleteFoodUsecase(this.foodRepository);

    // Act
    usecase.deleteFood(id);

    // Assert
    verify(this.foodRepository).delete(toDelete);
  }

  @Test
  public void doesNotLoadOrConvertTheRestOfTheTableTest() {
    // Arrange - a delete must stay a cheap, single primary-key operation regardless of how
    // many other rows exist; it must never load the rest of the table to build a response.
    UUID id = UUID.randomUUID();
    FoodPersistence toDelete = new FoodPersistence(
        id, "Tofu", new BigDecimal("120"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(toDelete));
    DeleteFoodUsecase usecase = new DeleteFoodUsecase(this.foodRepository);

    // Act
    usecase.deleteFood(id);

    // Assert
    verify(this.foodRepository, never()).findAll();
  }

  @Test
  public void throwsNoSuchElementExceptionWhenTheFoodDoesNotExistTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(this.foodRepository.findById(id)).thenReturn(Optional.empty());
    DeleteFoodUsecase usecase = new DeleteFoodUsecase(this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.deleteFood(id))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("Food not found");
  }

  @Test
  public void translatesAForeignKeyViolationIntoAFoodInUseExceptionTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence usedInRecipe = new FoodPersistence(
        id, "Tofu", new BigDecimal("120"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(usedInRecipe));
    doThrow(new DataIntegrityViolationException("FK violation"))
        .when(this.foodRepository).delete(usedInRecipe);
    DeleteFoodUsecase usecase = new DeleteFoodUsecase(this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.deleteFood(id))
        .isInstanceOf(FoodInUseException.class);
  }
}
