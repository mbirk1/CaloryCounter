package de.birk.calory.service;

import de.birk.calory.controller.model.FoodDto;
import de.birk.calory.domain.food.Food;
import de.birk.calory.persistence.FoodRepository;
import de.birk.calory.service.exceptions.FoodNotExistException;

import java.util.Optional;
import java.util.UUID;

public class FoodService {

    private FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public FoodDto findFood(UUID id) throws FoodNotExistException {
        Optional<Food> optional = this.foodRepository.findById(id);
        if (optional.isEmpty()) {
            throw new FoodNotExistException();
        }
        Food food = optional.get();
        return new FoodDto(food.getId(), food.getName(), food.getCalory());
    }
}
