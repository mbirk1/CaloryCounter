package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FoodDtoConverterUnitTest {

    @Test
    public void convertToEntityWithValidFoodDtoTest() {
        // Arrange
        FoodDto foodDto = new FoodDto("Apple", new BigDecimal("100"));

        // Act
        Food result = FoodDtoConverter.convertToEntity(foodDto);

        // Assert
        assertThat(foodDto.getName()).isEqualTo(result.getName());
        assertThat(foodDto.getCalory()).isEqualTo(result.getCalory());
    }

    @Test
    public void convertToEntityWithNullNameFoodDtoTest() {
        // Arrange
        FoodDto foodDto = new FoodDto(null, new BigDecimal("100"));

        // Act and Assert
        assertThatThrownBy(() -> FoodDtoConverter.convertToEntity(foodDto))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void convertToDetailsWithValidInputTest() {
        // Arrange
        UUID id = UUID.randomUUID();
        String name = "Apple";
        BigDecimal calory = new BigDecimal("100");

        // Act
        FoodDetailsDto result = FoodDtoConverter.convertToDetails(id, name, calory);

        // Assert
        assertThat(result.getUuid()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCalory()).isEqualTo(calory);
    }

    @Test
    public void convertToDtoWithValidInputTest() {
        // Arrange
        String name = "Apple";
        BigDecimal calory = new BigDecimal("100");

        // Act
        FoodDto result = FoodDtoConverter.convertToDto(name, calory);

        // Assert
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCalory()).isEqualTo(calory);
    }

    @Test
    public void convertToEntityWithNullCaloryTest() {
        // Arrange
        FoodDto foodDto = new FoodDto("Apple", null);

        // Act & Assert
        assertThatThrownBy(() -> FoodDtoConverter.convertToEntity(foodDto))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
