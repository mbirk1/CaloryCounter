package de.birk.calory.usecase.food;

import java.util.UUID;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.food.converter.FoodDtoConverter;
import de.birk.calory.usecase.food.converter.converter.FoodPersistenceConverter;

@Component
public class CreateFoodUsecase {

    private final FoodRepository foodRepository;

    public CreateFoodUsecase(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public FoodDetailsDto createFood(FoodDto foodDto) {
        Food food = FoodDtoConverter.convertToEntity(foodDto);
        FoodPersistence foodPersistence = FoodPersistenceConverter.convertToPersistence(food);
        this.foodRepository.save(foodPersistence);
        return FoodDtoConverter.convertToDetails(foodPersistence.getId(), foodPersistence.getName(), foodPersistence.getCalory());
    }
}
