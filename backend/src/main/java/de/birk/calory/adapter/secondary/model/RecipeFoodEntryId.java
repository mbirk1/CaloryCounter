package de.birk.calory.adapter.secondary.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for {@link RecipeFoodEntryPersistence}, mirroring the existing
 * {@code (food_id, recipe_id)} composite primary key on {@code tab_calory_recipe_food}. Field
 * names must match {@link RecipeFoodEntryPersistence}'s {@code @Id} field names exactly, per the
 * {@code @IdClass} contract.
 *
 * @author Marius Birk
 */
public class RecipeFoodEntryId implements Serializable {

  private UUID recipe;
  private UUID food;

  public RecipeFoodEntryId() {
  }

  public RecipeFoodEntryId(UUID recipe, UUID food) {
    this.recipe = recipe;
    this.food = food;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof RecipeFoodEntryId)) {
      return false;
    }
    RecipeFoodEntryId that = (RecipeFoodEntryId) object;
    return Objects.equals(recipe, that.recipe) && Objects.equals(food, that.food);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipe, food);
  }
}
