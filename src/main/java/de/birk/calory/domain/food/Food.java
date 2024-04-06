package de.birk.calory.domain.food;

import java.math.BigDecimal;
import java.util.UUID;

import de.birk.calory.exception.ValidationException;


public class Food extends AbstractEntity<UUID> {

    private UUID id;
    private final String name;
    private final BigDecimal calory;

    protected Food() {
        // default constructor with zero/empty values
        calory = BigDecimal.ZERO;
        name = "";
    }

    public Food(String name, BigDecimal calory){
        this.id = UUID.randomUUID();
        this.name = name;
        this.calory = calory;
        validate();
    }

    public Food(UUID id, String name, BigDecimal calory) throws ValidationException {
        this.id = id;
        this.name = name;
        this.calory = calory;
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

    protected void validate() throws ValidationException {
        if (this.name == null || this.name.isEmpty() || this.name.isBlank()) {
            throw new ValidationException();
        }
        if (this.calory == null) {
            throw new ValidationException();
        }
    }
}
