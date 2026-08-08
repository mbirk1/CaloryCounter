package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.UpdateFoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class UpdateFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void updatesTheEditableFieldsAndKeepsProvenanceFieldsUnchangedTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence existing = new FoodPersistence(
        id, "Hafermilch", new BigDecimal("45"), new BigDecimal("100"),
        "Oatly", "Plant-based beverages", new BigDecimal("1.5"), new BigDecimal("0.2"),
        new BigDecimal("6.7"), new BigDecimal("4.1"), new BigDecimal("0.8"),
        new BigDecimal("1"), new BigDecimal("0.11"), new BigDecimal("0.04"),
        "https://example.com/oatly.png", "OPENFOODFACTS", "1234567890123", "VEGAN"
    );
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(existing));
    when(this.foodRepository.save(any(FoodPersistence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateFoodUsecase usecase = new UpdateFoodUsecase(this.foodRepository);
    UpdateFoodDto updateFoodDto = new UpdateFoodDto(
        "Hafermilch Bio", new BigDecimal("48"), new BigDecimal("100"), "VEGETARIAN",
        new BigDecimal("2"), new BigDecimal("0.3"), new BigDecimal("7"), new BigDecimal("4.5"),
        new BigDecimal("1"), new BigDecimal("1.2"), new BigDecimal("0.12"),
        new BigDecimal("0.05"));

    // Act
    FoodDetailsDto result = usecase.updateFood(id, updateFoodDto);

    // Assert - edited fields are applied
    assertThat(result.getName()).isEqualTo("Hafermilch Bio");
    assertThat(result.getCalory()).isEqualByComparingTo("48");
    assertThat(result.getDiet()).isEqualTo("VEGETARIAN");
    assertThat(result.getFat()).isEqualByComparingTo("2");

    // Assert - provenance fields are preserved from the existing row
    assertThat(result.getBrand()).isEqualTo("Oatly");
    assertThat(result.getCategory()).isEqualTo("Plant-based beverages");
    assertThat(result.getImageUrl()).isEqualTo("https://example.com/oatly.png");
    assertThat(result.getSource()).isEqualTo("OPENFOODFACTS");
    assertThat(result.getExternalId()).isEqualTo("1234567890123");
  }

  @Test
  public void throwsNoSuchElementExceptionWhenTheFoodDoesNotExistTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(this.foodRepository.findById(id)).thenReturn(Optional.empty());
    UpdateFoodUsecase usecase = new UpdateFoodUsecase(this.foodRepository);
    UpdateFoodDto updateFoodDto = new UpdateFoodDto(
        "Name", new BigDecimal("50"), new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null);

    // Act & Assert
    assertThatThrownBy(() -> usecase.updateFood(id, updateFoodDto))
        .isInstanceOf(NoSuchElementException.class);
    verify(this.foodRepository, never()).save(any());
  }

  @Test
  public void rejectsAnImplausiblyHighCaloryValueTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence existing = new FoodPersistence(
        id, "Food", new BigDecimal("50"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(existing));
    UpdateFoodUsecase usecase = new UpdateFoodUsecase(this.foodRepository);
    UpdateFoodDto updateFoodDto = new UpdateFoodDto(
        "Food", new BigDecimal("1000"), new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null);

    // Act & Assert
    assertThatThrownBy(() -> usecase.updateFood(id, updateFoodDto))
        .isInstanceOf(ValidationException.class);
    verify(this.foodRepository, never()).save(any());
  }

  @Test
  public void rejectsANegativeMacronutrientValueTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence existing = new FoodPersistence(
        id, "Food", new BigDecimal("50"), new BigDecimal("100"));
    when(this.foodRepository.findById(id)).thenReturn(Optional.of(existing));
    UpdateFoodUsecase usecase = new UpdateFoodUsecase(this.foodRepository);
    UpdateFoodDto updateFoodDto = new UpdateFoodDto(
        "Food", new BigDecimal("50"), new BigDecimal("100"), null,
        new BigDecimal("-1"), null, null, null, null, null, null, null);

    // Act & Assert
    assertThatThrownBy(() -> usecase.updateFood(id, updateFoodDto))
        .isInstanceOf(ValidationException.class);
    verify(this.foodRepository, never()).save(any());
  }
}
