package de.birk.calory.domain.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.domain.AbstractEntity;
import de.birk.calory.exception.ValidationException;


public class Food extends AbstractEntity<UUID> {

  private UUID id;
  private final String name;
  private final BigDecimal calory;
  private final BigDecimal grams;

  protected Food() {
    // default constructor with zero/empty values
    calory = BigDecimal.ZERO;
    name = "";
    grams = BigDecimal.ZERO;
  }

  public Food(String name, BigDecimal calory, BigDecimal grams) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.calory = calory;
    this.grams = grams;
    validate();
  }

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
