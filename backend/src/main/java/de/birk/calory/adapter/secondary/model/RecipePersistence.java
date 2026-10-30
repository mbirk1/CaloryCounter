package de.birk.calory.adapter.secondary.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Persistence entity for the recipe.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_recipe")
public class RecipePersistence {

  @Id
  @Column
  private UUID id;

  @Column
  private String name;

  @Column
  private int servings;

  // Entries don't exist independently of their recipe (orphanRemoval), and are always saved/
  // deleted together with it (cascade = ALL) - the join-table-turned-entity is entirely owned
  // by the recipe side.
  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RecipeFoodEntryPersistence> foodEntries;

  public RecipePersistence() {
  }

  /**
   * Creates an instance of a persistence object for the database.
   *
   * @param id the identifier
   * @param name the name
   * @param servings the number of servings this recipe yields
   */
  public RecipePersistence(UUID id, String name, int servings) {
    this.id = id;
    this.name = name;
    this.servings = servings;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getServings() {
    return servings;
  }

  public List<RecipeFoodEntryPersistence> getFoodEntries() {
    return foodEntries;
  }

  public void setFoodEntries(List<RecipeFoodEntryPersistence> foodEntries) {
    this.foodEntries = foodEntries;
  }
}
