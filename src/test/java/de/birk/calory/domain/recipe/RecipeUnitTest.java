package de.birk.calory.domain.recipe;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.birk.calory.domain.AbstractEntityUnitTest;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;

public class RecipeUnitTest extends AbstractEntityUnitTest {

    @Test
    public void createRecipeWithValidNameAndListOfFoodsTest() {
        List<Food> foods = new ArrayList<>();
        foods.add(new Food("Food 1", BigDecimal.valueOf(100), BigDecimal.valueOf(200)));
        foods.add(new Food("Food 2", BigDecimal.valueOf(150), BigDecimal.valueOf(300)));

        Recipe recipe = new Recipe("Recipe 1", foods);

        assertThat(recipe.getName()).isEqualTo("Recipe 1");
        assertThat(recipe.getFoods()).isEqualTo(foods);
    }

    @Test
    public void createRecipeWithEmptyNameAndEmptyListOfFoodsTest() {
        List<Food> foods = new ArrayList<>();

        Recipe recipe = new Recipe("", foods);

        assertThat(recipe.getName()).isEqualTo("");
        assertThat(recipe.getFoods()).isEqualTo(foods);
    }

    @Test
    public void createRecipeWithValidNameAndNullListOfFoodsTest() {
        Recipe recipe = new Recipe("Recipe 1", null);

        assertThat(recipe.getName()).isEqualTo("Recipe 1");
        assertThat(recipe.getFoods()).isNull();
    }

    @Test
    public void test_getFoods() {
        List<Food> foods = new ArrayList<>();
        foods.add(new Food("Food 1", BigDecimal.valueOf(100), BigDecimal.valueOf(200)));
        foods.add(new Food("Food 2", BigDecimal.valueOf(150), BigDecimal.valueOf(300)));

        Recipe recipe = new Recipe("Recipe 1", foods);

        assertThat(recipe.getFoods()).isEqualTo(foods);
    }

    @Test
    public void createRecipeWithNullNameTest() {
        List<Food> foods = new ArrayList<>();
        foods.add(new Food("Food 1", BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

        assertThatThrownBy(() -> new Recipe(null, foods)).isInstanceOf(ValidationException.class);
    }

    // creating a new Recipe object with empty list of Food objects should raise a ValidationException
    @Test
    public void test_createRecipeWithEmptyListOfFoods() {
        assertThrows(ValidationException.class, () -> {
            Recipe recipe = new Recipe("Recipe 1", new ArrayList<>());
        });
    }

    // creating a new Recipe object with null list of Food objects should raise a ValidationException
    @Test
    public void test_createRecipeWithNullListOfFoods() {
        assertThrows(ValidationException.class, () -> {
            Recipe recipe = new Recipe("Recipe 1", null);
        });
    }

    // calling validate() on a Recipe object with null name should raise a ValidationException
    @Test
    public void test_validateWithNullName() {
        List<Food> foods = new ArrayList<>();
        foods.add(new Food("Food 1", BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

        Recipe recipe = new Recipe(null, foods);

        assertThrows(ValidationException.class, () -> {
            recipe.validate();
        });
    }

    // calling validate() on a Recipe object with empty list of Food objects should raise a ValidationException
    @Test
    public void test_validateWithEmptyListOfFoods() {
        Recipe recipe = new Recipe("Recipe 1", new ArrayList<>());

        assertThrows(ValidationException.class, () -> {
            recipe.validate();
        });
    }
}
