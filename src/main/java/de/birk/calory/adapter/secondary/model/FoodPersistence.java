package de.birk.calory.adapter.secondary.model;

import de.birk.calory.adapter.secondary.FoodRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

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

