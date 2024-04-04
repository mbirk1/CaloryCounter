package de.birk.calory.usecase.food.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

public class FoodDetailsDtoConverterUnitTest {

    @Test
    public void convertValidFoodEntityToFoodDetailsDtoTest() throws ValidationException {
        // Arrange
        Food food = new Food(
            UUID.randomUUID(),
            "Apple",
            new BigDecimal("100")
        );
        FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

        // Act
        FoodDetailsDto result = foodDetailsDtoConverter.convertFromEntity(food);

        // Assert
        assertThat(food.getId()).isEqualTo(result.getUuid());
        assertThat(food.getName()).isEqualTo(result.getName());
        assertThat(food.getCalory()).isEqualTo(result.getCalory());
    }

    // Should throw a NullPointerException if the input Food entity is null
    @Test
    public void throwNullPointerExceptionIfInputFoodEntityIsNullTest() {
        // Arrange
        FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

        // Act and Assert
        assertThatThrownBy(
            () -> foodDetailsDtoConverter.convertFromEntity(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void throwNullPointerExceptionIfInputFoodDetailsDtoIsNullTest() {
        // Arrange
        FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

        // Act and Assert
        assertThatThrownBy(() -> foodDetailsDtoConverter.convertFromDto(null))
            .isInstanceOf(NullPointerException.class);
    }
}
