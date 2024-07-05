package de.birk.calory.usecase.food.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

public class FoodDtoConverterUnitTest {


  @Test
  public void convertToEntityWithValidFoodDtoTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
    assertThat(foodDto.getName()).isEqualTo(result.getName());
    assertThat(foodDto.getCalory()).isEqualTo(result.getCalory());
  }


  @Test
  public void convertToDetailsWithValidInputTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(),
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    FoodDetailsDto result = foodDetailsDtoConverter.convertFromEntity(food);

    // Assert
    assertThat(result.getUuid()).isEqualTo(food.getId());
    assertThat(result.getName()).isEqualTo(food.getName());
    assertThat(result.getCalory()).isEqualTo(food.getCalory());
  }

  @Test
  public void convertToDtoWithValidInputTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(),
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    FoodDto result = foodDtoConverter.convertFromEntity(food);

    // Assert
    assertThat(result.getName()).isEqualTo(food.getName());
    assertThat(result.getCalory()).isEqualTo(food.getCalory());
  }

  @Test
  public void convertToEntityWithNullNameFoodDtoTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        null,
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();


    // Act and Assert
    assertThatThrownBy(() -> foodDtoConverter.convertFromDto(foodDto))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void convertToEntityWithNullCaloryTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Apple",
        null,
        new BigDecimal("100")
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act & Assert
    assertThatThrownBy(() -> foodDtoConverter.convertFromDto(foodDto))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void convertToEntityWithNullGramsTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Apple",
        new BigDecimal("100"),
        null
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act & Assert
    assertThatThrownBy(() -> foodDtoConverter.convertFromDto(foodDto))
        .isInstanceOf(ValidationException.class);
  }
}
