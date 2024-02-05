package de.birk.calory.adapter.primary;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.usecase.food.CreateFoodUsecase;
import de.birk.calory.usecase.food.FindFoodUsecase;

@RestController("/food")
public class FoodRestController {

    private final FindFoodUsecase findFoodUsecase;
    private final CreateFoodUsecase createFoodUseCase;

    public FoodRestController(FindFoodUsecase findFoodUsecase, CreateFoodUsecase createFoodUseCase) {
        this.findFoodUsecase = findFoodUsecase;
        this.createFoodUseCase = createFoodUseCase;
    }

    @GetMapping("/{id}")
    public FoodDetailsDto getFood(UUID id) {
        return findFoodUsecase.findFoodById(id);
    }

    @PostMapping("/")
    public FoodDetailsDto createFood(FoodDto foodDto) {
        return this.createFoodUseCase.createFood(foodDto);
    }
}
