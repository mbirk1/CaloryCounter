package de.birk.calory.domain.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.exception.ValidationException;


/**
 * Entity that depicts a food item. It needs to display all important informations
 * that count for calories and recipes.
 *
 * @author Marius Birk
 */
public class Food extends AbstractEntity<UUID> {

  private UUID id;
  private final String name;
  private final BigDecimal calory;
  private final BigDecimal grams;

  /**
   * Default constructor with zero/empty values.
   */
  protected Food() {
    calory = BigDecimal.ZERO;
    name = "";
    grams = BigDecimal.ZERO;
  }

  /**
   * Constructor to create a basic food item.
   *
   * @param name the name of the food
   * @param calory amount of calories
   * @param grams amount of grams for the amount of calories
   */
  public Food(String name, BigDecimal calory, BigDecimal grams) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.calory = calory;
    this.grams = grams;
    validate();
  }

  /**
   * Constructor to create a basic food item with an id.
   *
   * @param id the identifier as uuid
   * @param name the name of the food
   * @param calory amount of calories
   * @param grams amount of grams for the amount of calories
   * @throws ValidationException validates the entity
   */
  public Food(UUID id, String name, BigDecimal calory, BigDecimal grams)
      throws ValidationException {
    this.id = id;
    this.name = name;
    this.calory = calory;
    this.grams = grams;
    validate();
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
    return this.grams;
  }

  protected void validate() throws ValidationException {
    if (this.grams == null) {
      throw new ValidationException();
    }
    if (this.name == null || this.name.isEmpty() || this.name.isBlank()) {
      throw new ValidationException();
    }
    if (this.calory == null) {
      throw new ValidationException();
    }
  }
}
