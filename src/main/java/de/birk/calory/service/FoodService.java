package de.birk.calory.service;

import de.birk.calory.models.food.Food;
import de.birk.calory.persistence.FoodRepository;

import java.util.Optional;
import java.util.UUID;

public class FoodService {

    private FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Food findFood(UUID id) {
        Optional<Food> optional = this.foodRepository.findById(id);
        return optional.orElseGet(Food::new);
    }
}
