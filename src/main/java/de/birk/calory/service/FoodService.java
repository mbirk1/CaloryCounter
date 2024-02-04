package de.birk.calory.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.domain.food.Food;
import de.birk.calory.service.exceptions.FoodNotExistException;

@Service
public class FoodService {

  private final FoodRepository foodRepository;

  @Autowired
  public FoodService(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  public Food findFood(UUID id) throws FoodNotExistException {
    Optional<Food> optional = this.foodRepository.findById(id);
    if (optional.isEmpty()) {
      throw new FoodNotExistException();
    }
    return optional.get();
  }

  public Food createFood(Food food) {
    return this.foodRepository.save(food);
  }
}
