package de.birk.calory.persistence;

import de.birk.calory.domain.food.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {

}
