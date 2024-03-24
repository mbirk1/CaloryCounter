package de.birk.calory.adapter.secondary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import de.birk.calory.adapter.secondary.model.FoodPersistence;

@Repository
public interface FoodRepository extends JpaRepository<FoodPersistence, UUID> {

}
