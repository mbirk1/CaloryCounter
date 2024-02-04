package de.birk.calory.adapter.primary.model;

import java.math.BigDecimal;
import java.util.UUID;

public class FoodDetailsDto {

  private final UUID uuid;
  private final String name;
  private final BigDecimal calory;

  public FoodDetailsDto(UUID uuid, String name, BigDecimal calory) {
    this.uuid = uuid;
    this.name = name;
    this.calory = calory;
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCalory() {
    return calory;
  }
}
