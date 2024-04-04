package de.birk.calory.usecase.food;

import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Food;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

@Component
public class CreateFoodUsecase {

    private final FoodRepository foodRepository;
    private FoodPersistenceConverter persistenceConverter;
    private FoodDtoConverter dtoConverter;
    private FoodDetailsDtoConverter detailsDtoConverter;

    public CreateFoodUsecase(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
        this.persistenceConverter = new FoodPersistenceConverter();
        this.dtoConverter = new FoodDtoConverter();
        this.detailsDtoConverter = new FoodDetailsDtoConverter();
    }

    public FoodDetailsDto createFood(FoodDto foodDto) {
        Food food = this.dtoConverter.convertFromDto(foodDto);
        FoodPersistence foodPersistence = this.persistenceConverter.convertFromEntity(food);
        this.foodRepository.save(foodPersistence);
        return this.detailsDtoConverter.convertFromEntity(food);
    }
}
