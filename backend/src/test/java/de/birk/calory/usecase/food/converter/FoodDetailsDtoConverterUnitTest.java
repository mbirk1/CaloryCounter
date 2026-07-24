package de.birk.calory.usecase.food.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.exception.ValidationException;

public class FoodDetailsDtoConverterUnitTest {

  @Test
  public void convertValidFoodEntityToFoodDetailsDtoTest() throws ValidationException {
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
    assertThat(food.getId()).isEqualTo(result.getUuid());
    assertThat(food.getName()).isEqualTo(result.getName());
    assertThat(food.getCalory()).isEqualTo(result.getCalory());
  }

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

  @Test
  public void convertValidFoodDetailsDtoToFoodEntityTest() throws ValidationException {
    // Arrange
    FoodDetailsDto food = new FoodDetailsDto(
        UUID.randomUUID(),
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    Food result = foodDetailsDtoConverter.convertFromDto(food);

    // Assert
    assertThat(food.getUuid()).isEqualTo(result.getId());
    assertThat(food.getName()).isEqualTo(result.getName());
    assertThat(food.getCalory()).isEqualTo(result.getCalory());
  }




  @Test
  public void convertValidFoodEntitiesToFoodDetailsDtoTest() throws ValidationException {
    // Arrange
    Food first = new Food(
        UUID.randomUUID(),
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    List<Food> foods = List.of(first);
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    List<FoodDetailsDto> result = foodDetailsDtoConverter.convertFromEntities(foods);

    // Assert
    assertThat(first.getId()).isEqualTo(result.getFirst().getUuid());
    assertThat(first.getName()).isEqualTo(result.getFirst().getName());
    assertThat(first.getCalory()).isEqualTo(result.getFirst().getCalory());
  }

  @Test
  public void throwNullPointerExceptionIfInputFoodEntitiesIsNullTest() {
    // Arrange
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act and Assert
    assertThatThrownBy(
        () -> foodDetailsDtoConverter.convertFromEntities(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void throwNullPointerExceptionIfInputFoodDetailsDtosIsNullTest() {
    // Arrange
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act and Assert
    assertThatThrownBy(() -> foodDetailsDtoConverter.convertFromDtos(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void convertValidFoodDetailsDtosToFoodEntitiesTest() throws ValidationException {
    // Arrange
    FoodDetailsDto food = new FoodDetailsDto(
        UUID.randomUUID(),
        "Apple",
        new BigDecimal("100"),
        new BigDecimal("100")
    );
    List<FoodDetailsDto> foodDetailsDtos = List.of(food);
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    List<Food> result = foodDetailsDtoConverter.convertFromDtos(foodDetailsDtos);

    // Assert
    assertThat(food.getUuid()).isEqualTo(result.getFirst().getId());
    assertThat(food.getName()).isEqualTo(result.getFirst().getName());
    assertThat(food.getCalory()).isEqualTo(result.getFirst().getCalory());
  }

  @Test
  public void convertFromEntityWithExtendedFieldsTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(),
        "Cola",
        new BigDecimal("42.00"),
        new BigDecimal("100"),
        "Acme",
        "Beverages",
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("10.60"),
        new BigDecimal("10.60"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.01"),
        new BigDecimal("0.00"),
        "https://example.com/cola.png",
        FoodSource.OPENFOODFACTS,
        "1234567890123"
    );
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    FoodDetailsDto result = foodDetailsDtoConverter.convertFromEntity(food);

    // Assert
    assertThat(result.getBrand()).isEqualTo("Acme");
    assertThat(result.getCategory()).isEqualTo("Beverages");
    assertThat(result.getImageUrl()).isEqualTo("https://example.com/cola.png");
    assertThat(result.getSource()).isEqualTo("OPENFOODFACTS");
    assertThat(result.getExternalId()).isEqualTo("1234567890123");
  }

  @Test
  public void convertFromEntityWithNullSourceLeavesSourceNullTest() {
    // Arrange
    Food food = new Food(
        UUID.randomUUID(), "Apple", new BigDecimal("52"), new BigDecimal("100"),
        null, null, null, null, null, null, null, null, null, null, null, null, null
    );
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    FoodDetailsDto result = foodDetailsDtoConverter.convertFromEntity(food);

    // Assert
    assertThat(result.getSource()).isNull();
  }

  @Test
  public void convertFromDtoWithExtendedFieldsTest() {
    // Arrange
    FoodDetailsDto dto = new FoodDetailsDto(
        UUID.randomUUID(),
        "Cola",
        new BigDecimal("42.00"),
        new BigDecimal("100"),
        "Acme",
        "Beverages",
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("10.60"),
        new BigDecimal("10.60"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.01"),
        new BigDecimal("0.00"),
        "https://example.com/cola.png",
        "OPENFOODFACTS",
        "1234567890123"
    );
    FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();

    // Act
    Food result = foodDetailsDtoConverter.convertFromDto(dto);

    // Assert
    assertThat(result.getBrand()).isEqualTo("Acme");
    assertThat(result.getSource()).isEqualTo(FoodSource.OPENFOODFACTS);
    assertThat(result.getExternalId()).isEqualTo("1234567890123");
  }
}
