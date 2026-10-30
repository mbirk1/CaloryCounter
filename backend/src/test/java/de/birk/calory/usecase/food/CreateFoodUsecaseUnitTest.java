package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
public class CreateFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void createsAFoodItemWithBrandCategoryImageUrlAndMacronutrientsTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Hafermilch", new BigDecimal("45"), new BigDecimal("100"), "VEGAN",
        "Oatly", "Plant-based beverages", "https://example.com/oatly.png",
        new BigDecimal("1.5"), new BigDecimal("0.2"), new BigDecimal("6.7"),
        new BigDecimal("4.1"), new BigDecimal("0.8"), new BigDecimal("1"),
        new BigDecimal("0.11"), new BigDecimal("0.04"),
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    when(this.foodRepository.save(any(FoodPersistence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act
    FoodDetailsDto result = usecase.createFood(foodDto);

    // Assert
    assertThat(result.getName()).isEqualTo("Hafermilch");
    assertThat(result.getCalory()).isEqualByComparingTo("45");
    assertThat(result.getDiet()).isEqualTo("VEGAN");
    assertThat(result.getBrand()).isEqualTo("Oatly");
    assertThat(result.getCategory()).isEqualTo("Plant-based beverages");
    assertThat(result.getImageUrl()).isEqualTo("https://example.com/oatly.png");
    assertThat(result.getFat()).isEqualByComparingTo("1.5");
    assertThat(result.getSaturatedFat()).isEqualByComparingTo("0.2");
    assertThat(result.getCarbohydrates()).isEqualByComparingTo("6.7");
    assertThat(result.getSugar()).isEqualByComparingTo("4.1");
    assertThat(result.getFiber()).isEqualByComparingTo("0.8");
    assertThat(result.getProtein()).isEqualByComparingTo("1");
    assertThat(result.getSalt()).isEqualByComparingTo("0.11");
    assertThat(result.getSodium()).isEqualByComparingTo("0.04");
    assertThat(result.getSource()).isEqualTo("MANUAL");
    assertThat(result.getExternalId()).isNull();
  }

  @Test
  public void createsAFoodItemWithOnlyTheRequiredFieldsTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Apple", new BigDecimal("52"), new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    when(this.foodRepository.save(any(FoodPersistence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act
    FoodDetailsDto result = usecase.createFood(foodDto);

    // Assert
    assertThat(result.getName()).isEqualTo("Apple");
    assertThat(result.getDiet()).isEqualTo("UNKNOWN");
    assertThat(result.getBrand()).isNull();
    assertThat(result.getFat()).isNull();
  }

  @Test
  public void returnsTheEntityAsPersistedRatherThanThePreSaveInstanceTest() {
    // Arrange - the usecase should convert and return whatever foodRepository.save(...)
    // returned, not the pre-save instance it was called with.
    FoodDto foodDto = new FoodDto(
        "Apple", new BigDecimal("52"), new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    when(this.foodRepository.save(any(FoodPersistence.class))).thenAnswer(invocation -> {
      FoodPersistence submitted = invocation.getArgument(0);
      return new FoodPersistence(
          submitted.getId(), "Apple (persisted)", submitted.getCalory(), submitted.getGrams());
    });
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act
    FoodDetailsDto result = usecase.createFood(foodDto);

    // Assert
    assertThat(result.getName()).isEqualTo("Apple (persisted)");
  }

  @Test
  public void rejectsAnImplausiblyHighCaloryValueTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Food", new BigDecimal("5000"), new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createFood(foodDto))
        .isInstanceOf(ValidationException.class);
    verify(this.foodRepository, never()).save(any());
  }

  @Test
  public void rejectsANegativeMacronutrientValueTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Food", new BigDecimal("50"), new BigDecimal("100"), null,
        null, null, null, new BigDecimal("-1"), null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createFood(foodDto))
        .isInstanceOf(ValidationException.class);
    verify(this.foodRepository, never()).save(any());
  }

  @Test
  public void rejectsAnImplausiblyHighMacronutrientValueTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Food", new BigDecimal("50"), new BigDecimal("100"), null,
        null, null, null, null, null, new BigDecimal("150"), null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    CreateFoodUsecase usecase = new CreateFoodUsecase(this.foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.createFood(foodDto))
        .isInstanceOf(ValidationException.class);
    verify(this.foodRepository, never()).save(any());
  }
}
