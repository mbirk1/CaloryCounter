package de.birk.calory.adapter.secondary;

import de.birk.calory.domain.food.Food;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, UUID> {

}
