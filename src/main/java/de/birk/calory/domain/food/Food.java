package de.birk.calory.domain.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tab_calory_food")
public class Food {

    @Id
    @Column
    private UUID id;
    @Column
    private String name;
    @Column(name = "calory_count")
    private BigDecimal calory;

    public Food() {
        //for JPA
    }

    public Food(UUID id, String name, BigDecimal calory) {
        this.id = id;
        this.name = name;
        this.calory = calory;
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
}
