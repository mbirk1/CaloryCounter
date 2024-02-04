package de.birk.calory.adapter.secondary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.birk.calory.domain.food.Food;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

}
