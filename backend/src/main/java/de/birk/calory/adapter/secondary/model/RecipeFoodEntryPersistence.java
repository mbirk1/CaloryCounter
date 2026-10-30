package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Persistence entity for a single food entry within a recipe, i.e. one row of
 * {@code tab_calory_recipe_food}: which food, in which recipe, and the amount (in grams) used.
 *
 * <p>Mapped as its own entity rather than a plain {@code @ManyToMany} join table, since JPA
 * cannot add extra columns (here: {@code amount_grams}) to a bare join table.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_recipe_food")
@IdClass(RecipeFoodEntryId.class)
public class RecipeFoodEntryPersistence {

  @Id
  @ManyToOne
  @JoinColumn(name = "recipe_id")
  private RecipePersistence recipe;

  @Id
  @ManyToOne
  @JoinColumn(name = "food_id")
  private FoodPersistence food;

  @Column(name = "amount_grams")
  private BigDecimal amountGrams;

  public RecipeFoodEntryPersistence() {
  }

  /**
   * Creates a recipe-food entry.
   *
   * @param recipe the owning recipe
   * @param food the referenced food item
   * @param amountGrams the amount of the food item used, in grams
   */
  public RecipeFoodEntryPersistence(RecipePersistence recipe, FoodPersistence food,
      BigDecimal amountGrams) {
    this.recipe = recipe;
    this.food = food;
    this.amountGrams = amountGrams;
  }

  public RecipePersistence getRecipe() {
    return recipe;
  }

  public FoodPersistence getFood() {
    return food;
  }

  public BigDecimal getAmountGrams() {
    return amountGrams;
  }
}
