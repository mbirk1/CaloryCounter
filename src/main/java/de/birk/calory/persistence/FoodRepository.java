package de.birk.calory.persistence;

import de.birk.calory.models.food.Food;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FoodRepository extends CrudRepository<Food, UUID> {

}
