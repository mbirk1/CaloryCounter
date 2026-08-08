package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.dao.DataIntegrityViolationException;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.FoodInUseException;

@ExtendWith(MockitoExtension.class)
public class DeleteFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void deletesTheFoodAndReturnsTheRemainingItemsTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence toDelete = new FoodPersistence(
        id, "Tofu", new BigDecimal("120"), new BigDecimal("100"));
    FoodPersistence remaining = new FoodPersistence(
        UUID.randomUUID(), "Apfel", new BigDecimal("52"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(toDelete));
    when(this.foodRepository.findAll()).thenReturn(List.of(remaining));
    DeleteFoodUsecase usecase = new DeleteFoodUsecase(this.foodRepository);

    // Act
    List<FoodDetailsDto> result = usecase.deleteFood(id);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getName()).isEqualTo("Apfel");
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
