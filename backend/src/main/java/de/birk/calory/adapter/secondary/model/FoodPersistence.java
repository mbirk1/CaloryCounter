package de.birk.calory.adapter.secondary.model;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Persistence entity for the food items.
 *
 * @author Marius Birk
 */
@Entity
@Table(name = "tab_calory_food")
public class FoodPersistence {

  @Id
  @Column
  private UUID id;
  @Column
  private String name;
  @Column(name = "calory_count")
  private BigDecimal calory;

  @Column(name = "grams")
  private BigDecimal grams;

  @ManyToMany(mappedBy = "foods")
  private Set<RecipePersistence> recipe;

  public FoodPersistence() {
    //for JPA
  }

  /**
   * Constructor to create an instance of the object, that can be saved in the database.
   *
   * @param id the identifier
   * @param name the name
   * @param calory amount of calories for the grams
   * @param grams amount of grams
   */
  public FoodPersistence(UUID id, String name, BigDecimal calory, BigDecimal grams) {
    this.id = id;
    this.name = name;
    this.calory = calory;
    this.grams = grams;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCalory() {
    return calory;
  }

  public BigDecimal getGrams() {
    return grams;
  }
}

