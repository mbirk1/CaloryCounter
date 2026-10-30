package de.birk.calory.domain.recipe;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.AbstractEntityUnitTest;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.exception.ValidationException;

public class RecipeUnitTest extends AbstractEntityUnitTest {

  private Food foodWithMacros(String name, String calory, String grams, String fat, String diet) {
    return new Food.Builder()
        .id(UUID.randomUUID())
        .name(name)
        .calory(new BigDecimal(calory))
        .grams(new BigDecimal(grams))
        .fat(fat == null ? null : new BigDecimal(fat))
        .source(FoodSource.MANUAL)
        .diet(Diet.valueOf(diet))
        .build();
  }

  @Test
  public void createRecipeWithValidNameAndListOfEntriesTest() {
    List<RecipeFoodEntry> entries = new ArrayList<>();
    entries.add(new RecipeFoodEntry(
        new Food("Food 1", BigDecimal.valueOf(100), BigDecimal.valueOf(200)),
        BigDecimal.valueOf(50)));

    Recipe recipe = new Recipe("Recipe 1", entries, 2);

    assertThat(recipe.getName()).isEqualTo("Recipe 1");
    assertThat(recipe.getEntries()).isEqualTo(entries);
    assertThat(recipe.getServings()).isEqualTo(2);
  }

  @Test
  public void createRecipeWithEmptyNameTest() {
    assertThatThrownBy(() -> new Recipe("", new ArrayList<>(), 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithBlankNameTest() {
    assertThatThrownBy(() -> new Recipe("    ", new ArrayList<>(), 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithNullNameTest() {
    assertThatThrownBy(() -> new Recipe(null, new ArrayList<>(), 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithNullListOfEntriesTest() {
    assertThatThrownBy(() -> new Recipe("Recipe 1", null, 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithEmptyListOfEntriesIsAllowedTest() {
    // An empty-entries recipe must stay constructible at the domain level (used by the
    // vacuous-VEGAN diet case below); "at least one food" is enforced by CreateRecipeUsecase,
    // not by Recipe itself.
    Recipe recipe = new Recipe("Recipe 1", new ArrayList<>(), 1);

    assertThat(recipe.getEntries().size()).isEqualTo(0);
  }

  @Test
  public void createRecipeWithZeroServingsTest() {
    assertThatThrownBy(() -> new Recipe("Recipe 1", new ArrayList<>(), 0))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithNegativeServingsTest() {
    assertThatThrownBy(() -> new Recipe("Recipe 1", new ArrayList<>(), -1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithZeroAmountGramsEntryTest() {
    List<RecipeFoodEntry> entries = List.of(new RecipeFoodEntry(
        new Food("Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100)), BigDecimal.ZERO));

    assertThatThrownBy(() -> new Recipe("Recipe 1", entries, 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithNegativeAmountGramsEntryTest() {
    List<RecipeFoodEntry> entries = List.of(new RecipeFoodEntry(
        new Food("Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100)),
        BigDecimal.valueOf(-10)));

    assertThatThrownBy(() -> new Recipe("Recipe 1", entries, 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithNullAmountGramsEntryTest() {
    List<RecipeFoodEntry> entries = List.of(new RecipeFoodEntry(
        new Food("Food", BigDecimal.valueOf(100), BigDecimal.valueOf(100)), null));

    assertThatThrownBy(() -> new Recipe("Recipe 1", entries, 1))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  public void emptyConstructorTest() {
    Recipe recipe = new Recipe();

    assertThat(recipe.getName()).isEqualTo("");
    assertThat(recipe.getEntries().size()).isEqualTo(0);
    assertThat(recipe.getServings()).isEqualTo(1);
  }

  @Test
  public void withIdConstructorTest() {
    UUID uuid = UUID.randomUUID();
    Recipe recipe = new Recipe(uuid, "Recipe", new ArrayList<>(), 3);

    assertThat(recipe.getId()).isEqualTo(uuid);
    assertThat(recipe.getName()).isEqualTo("Recipe");
    assertThat(recipe.getEntries().size()).isEqualTo(0);
    assertThat(recipe.getServings()).isEqualTo(3);
  }

  @Test
  public void totalCaloriesScalesProportionallyToAmountUsedTest() {
    // Food: 200 kcal per 100g. Entry uses 50g -> proportional 100 kcal.
    Food food = new Food("Food", BigDecimal.valueOf(200), BigDecimal.valueOf(100));
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(food, BigDecimal.valueOf(50))), 1);

    assertThat(recipe.getTotalCalories()).isEqualByComparingTo("100.00");
  }

  @Test
  public void totalCaloriesSumsAcrossMultipleEntriesTest() {
    Food foodA = new Food("A", BigDecimal.valueOf(200), BigDecimal.valueOf(100));
    Food foodB = new Food("B", BigDecimal.valueOf(50), BigDecimal.valueOf(100));
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(foodA, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(foodB, BigDecimal.valueOf(200))
    ), 1);

    // 200 + 100 = 300
    assertThat(recipe.getTotalCalories()).isEqualByComparingTo("300.00");
  }

  @Test
  public void nullMacroOnAnEntryIsSkippedNotTreatedAsZeroTest() {
    Food foodWithFat = foodWithMacros("A", "100", "100", "10", "VEGAN");
    Food foodWithoutFat = foodWithMacros("B", "100", "100", null, "VEGAN");
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(foodWithFat, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(foodWithoutFat, BigDecimal.valueOf(100))
    ), 1);

    // Only foodWithFat contributes - foodWithoutFat's null fat is skipped, not counted as 0.
    assertThat(recipe.getTotalFat()).isEqualByComparingTo("10.00");
  }

  @Test
  public void nonTerminatingRatioDoesNotThrowAndRoundsToTwoDecimalsTest() {
    // amount/foodGrams = 100/300 = 0.3333... - a non-terminating decimal.
    Food food = new Food("Food", BigDecimal.valueOf(90), BigDecimal.valueOf(300));
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(food, BigDecimal.valueOf(100))), 1);

    assertThat(recipe.getTotalCalories()).isEqualByComparingTo("30.00");
  }

  @Test
  public void caloriesPerServingDividesTotalByServingsTest() {
    Food food = new Food("Food", BigDecimal.valueOf(400), BigDecimal.valueOf(100));
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(food, BigDecimal.valueOf(100))), 4);

    assertThat(recipe.getCaloriesPerServing()).isEqualByComparingTo("100.00");
  }

  @Test
  public void recipeWithoutEntriesIsVacuouslyVeganTest() {
    Recipe recipe = new Recipe("Recipe", new ArrayList<>(), 1);

    assertThat(recipe.getDiet()).isEqualTo(Diet.VEGAN);
  }

  @Test
  public void recipeWithOnlyVeganFoodsIsVeganTest() {
    Food a = foodWithMacros("A", "100", "100", null, "VEGAN");
    Food b = foodWithMacros("B", "100", "100", null, "VEGAN");
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(a, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(b, BigDecimal.valueOf(100))
    ), 1);

    assertThat(recipe.getDiet()).isEqualTo(Diet.VEGAN);
  }

  @Test
  public void addingAVegetarianFoodDowngradesAnAllVeganRecipeTest() {
    // Matches the ticket's literal acceptance example.
    Food a = foodWithMacros("A", "100", "100", null, "VEGAN");
    Food b = foodWithMacros("B", "100", "100", null, "VEGAN");
    Food c = foodWithMacros("C", "100", "100", null, "VEGETARIAN");
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(a, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(b, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(c, BigDecimal.valueOf(100))
    ), 1);

    assertThat(recipe.getDiet()).isEqualTo(Diet.VEGETARIAN);
  }

  @Test
  public void unknownDietFoodIsTheWeakestLinkTest() {
    Food a = foodWithMacros("A", "100", "100", null, "VEGAN");
    Food b = foodWithMacros("B", "100", "100", null, "NON_VEGETARIAN");
    Food c = foodWithMacros("C", "100", "100", null, "UNKNOWN");
    Recipe recipe = new Recipe("Recipe", List.of(
        new RecipeFoodEntry(a, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(b, BigDecimal.valueOf(100)),
        new RecipeFoodEntry(c, BigDecimal.valueOf(100))
    ), 1);

    assertThat(recipe.getDiet()).isEqualTo(Diet.UNKNOWN);
  }
}
