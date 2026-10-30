package de.birk.calory.usecase.recipe.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.RecipeFoodEntryPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.domain.recipe.RecipeFoodEntry;

public class RecipePersistenceConverterTest {

  @Test
  public void convertPersistenceToEntityPreservesFullFoodDataTest() {
    // Arrange - regression test for the bug where every recipe converter used to rebuild Food
    // via the lossy 4-arg constructor, discarding brand/macros/diet/image/source/externalId.
    RecipePersistenceConverter converter = new RecipePersistenceConverter();
    RecipePersistence recipePersistence = new RecipePersistence(UUID.randomUUID(), "Recipe", 2);
    FoodPersistence foodPersistence = new FoodPersistence.Builder()
        .id(UUID.randomUUID())
        .name("Hafermilch")
        .calory(BigDecimal.valueOf(45))
        .grams(BigDecimal.valueOf(100))
        .brand("Oatly")
        .category("Plant-based beverages")
        .fat(BigDecimal.valueOf(1.5))
        .saturatedFat(BigDecimal.valueOf(0.2))
        .carbohydrates(BigDecimal.valueOf(6.7))
        .sugar(BigDecimal.valueOf(4.1))
        .fiber(BigDecimal.valueOf(0.8))
        .protein(BigDecimal.valueOf(1))
        .salt(BigDecimal.valueOf(0.11))
        .sodium(BigDecimal.valueOf(0.04))
        .imageUrl("https://example.com/oatly.png")
        .source("MANUAL")
        .externalId(null)
        .diet("VEGAN")
        .build();
    recipePersistence.setFoodEntries(List.of(
        new RecipeFoodEntryPersistence(recipePersistence, foodPersistence, BigDecimal.valueOf(50))
    ));

    // Act
    Recipe entity = converter.convertFromDto(recipePersistence);

    // Assert
    Food food = entity.getEntries().getFirst().getFood();
    assertThat(food.getBrand()).isEqualTo("Oatly");
    assertThat(food.getCategory()).isEqualTo("Plant-based beverages");
    assertThat(food.getFat()).isEqualByComparingTo("1.5");
    assertThat(food.getDiet()).isEqualTo(Diet.VEGAN);
    assertThat(food.getImageUrl()).isEqualTo("https://example.com/oatly.png");
    assertThat(entity.getServings()).isEqualTo(2);
    assertThat(entity.getEntries().getFirst().getAmountGrams()).isEqualByComparingTo("50");
  }

  @Test
  public void convertEntityToPersistencePreservesFullFoodDataTest() {
    // Arrange
    RecipePersistenceConverter converter = new RecipePersistenceConverter();
    Food food = new Food.Builder()
        .id(UUID.randomUUID())
        .name("Hafermilch")
        .calory(BigDecimal.valueOf(45))
        .grams(BigDecimal.valueOf(100))
        .brand("Oatly")
        .category("Plant-based beverages")
        .fat(BigDecimal.valueOf(1.5))
        .source(FoodSource.MANUAL)
        .diet(Diet.VEGAN)
        .build();
    Recipe recipe = new Recipe(UUID.randomUUID(), "Recipe",
        List.of(new RecipeFoodEntry(food, BigDecimal.valueOf(50))), 3);

    // Act
    RecipePersistence persistence = converter.convertFromEntity(recipe);

    // Assert
    FoodPersistence foodPersistence = persistence.getFoodEntries().getFirst().getFood();
    assertThat(persistence.getServings()).isEqualTo(3);
    assertThat(persistence.getFoodEntries()).hasSize(1);
    assertThat(persistence.getFoodEntries().getFirst().getRecipe()).isSameAs(persistence);
    assertThat(foodPersistence.getBrand()).isEqualTo("Oatly");
    assertThat(foodPersistence.getFat()).isEqualByComparingTo("1.5");
    assertThat(foodPersistence.getDiet()).isEqualTo("VEGAN");
    assertThat(persistence.getFoodEntries().getFirst().getAmountGrams())
        .isEqualByComparingTo("50");
  }
}
