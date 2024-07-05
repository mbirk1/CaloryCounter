package de.birk.calory.domain.recipe;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    assertThatThrownBy(() -> new Recipe("", foods)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void createRecipeWithBlankNameAndEmptyListOfFoodsTest() {
    List<Food> foods = new ArrayList<>();

    assertThatThrownBy(() -> new Recipe("    ", foods)).isInstanceOf(ValidationException.class);
  }

  @Test
  public void getFoodsTest() {
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

  @Test
  public void createRecipeWithNullListOfFoodsTest() {
    assertThatThrownBy(() -> {
      new Recipe("Recipe 1", null);
    }).isInstanceOf(ValidationException.class);
  }

  @Test
  public void emptyConstructorTest(){
    Recipe recipe = new Recipe();

    assertThat(recipe.getName()).isEqualTo("");
    assertThat(recipe.getFoods().size()).isEqualTo(0);
  }

  @Test
  public void withIdConstructorTest(){
    UUID uuid = UUID.randomUUID();
    Recipe recipe = new Recipe(uuid, "Recipe", new ArrayList<>());

    assertThat(recipe.getId()).isEqualTo(uuid);
    assertThat(recipe.getName()).isEqualTo("Recipe");
    assertThat(recipe.getFoods().size()).isEqualTo(0);
  }
}
