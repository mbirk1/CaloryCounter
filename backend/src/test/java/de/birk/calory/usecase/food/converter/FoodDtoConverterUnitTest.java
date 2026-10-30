package de.birk.calory.usecase.food.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.exception.ValidationException;

public class FoodDtoConverterUnitTest {

  @Test
  public void convertToEntityWithValidFoodDtoTest() {
    // Arrange
    FoodDto foodDto = minimalDto("Apple", null);
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
    assertThat(foodDto.getName()).isEqualTo(result.getName());
    assertThat(foodDto.getCalory()).isEqualTo(result.getCalory());
  }

  @Test
  public void convertToEntityWithNullDietDefaultsToUnknownTest() {
    // Arrange
    FoodDto foodDto = minimalDto("Apple", null);
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
    assertThat(result.getDiet()).isEqualTo(Diet.UNKNOWN);
  }

  @Test
  public void convertToEntityWithExplicitDietTest() {
    // Arrange
    FoodDto foodDto = minimalDto("Tofu", "VEGAN");
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
    assertThat(result.getDiet()).isEqualTo(Diet.VEGAN);
  }

  @Test
  public void convertToEntityCarriesOverBrandCategoryImageUrlAndMacronutrientsTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Hafermilch",
        new BigDecimal("45"),
        new BigDecimal("100"),
        "VEGAN",
        "Oatly",
        "Plant-based beverages",
        "https://example.com/oatly.png",
        new BigDecimal("1.5"),
        new BigDecimal("0.2"),
        new BigDecimal("6.7"),
        new BigDecimal("4.1"),
        new BigDecimal("0.8"),
        new BigDecimal("1"),
        new BigDecimal("0.11"),
        new BigDecimal("0.04"),
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
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
  }

  @Test
  public void convertToEntityAlwaysSetsSourceToManualAndExternalIdToNullTest() {
    // Arrange - source and externalId are system-controlled, not part of FoodDto at all.
    FoodDto foodDto = minimalDto("Apple", null);
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    Food result = foodDtoConverter.convertFromDto(foodDto);

    // Assert
    assertThat(result.getSource()).isEqualTo(FoodSource.MANUAL);
    assertThat(result.getExternalId()).isNull();
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
  public void convertToDtoCarriesOverBrandCategoryImageUrlAndMacronutrientsTest() {
    // Arrange
    Food food = new Food.Builder()
        .id(UUID.randomUUID())
        .name("Hafermilch")
        .calory(new BigDecimal("45"))
        .grams(new BigDecimal("100"))
        .brand("Oatly")
        .category("Plant-based beverages")
        .fat(new BigDecimal("1.5"))
        .saturatedFat(new BigDecimal("0.2"))
        .carbohydrates(new BigDecimal("6.7"))
        .sugar(new BigDecimal("4.1"))
        .fiber(new BigDecimal("0.8"))
        .protein(new BigDecimal("1"))
        .salt(new BigDecimal("0.11"))
        .sodium(new BigDecimal("0.04"))
        .imageUrl("https://example.com/oatly.png")
        .source(FoodSource.MANUAL)
        .externalId(null)
        .diet(Diet.VEGAN)
        .build();
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act
    FoodDto result = foodDtoConverter.convertFromEntity(food);

    // Assert
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
    assertThat(result.getDiet()).isEqualTo("VEGAN");
  }

  @Test
  public void convertToEntityWithNullNameFoodDtoTest() {
    // Arrange
    FoodDto foodDto = minimalDto(null, null);
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act and Assert
    assertThatThrownBy(() -> foodDtoConverter.convertFromDto(foodDto))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void convertToEntityWithNullCaloryTest() {
    // Arrange
    FoodDto foodDto = new FoodDto(
        "Apple", null, new BigDecimal("100"), null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
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
        "Apple", new BigDecimal("100"), null, null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
    FoodDtoConverter foodDtoConverter = new FoodDtoConverter();

    // Act & Assert
    assertThatThrownBy(() -> foodDtoConverter.convertFromDto(foodDto))
        .isInstanceOf(ValidationException.class);
  }

  private FoodDto minimalDto(String name, String diet) {
    return new FoodDto(
        name, new BigDecimal("100"), new BigDecimal("100"), diet,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null
    );
  }
}
