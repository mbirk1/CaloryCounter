package de.birk.calory.adapter.primary;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.usecase.food.CreateFoodUsecase;
import de.birk.calory.usecase.food.FindFoodUsecase;

@RestController
@RequestMapping("/food")
public class FoodRestController {
  private final FindFoodUsecase findFoodUsecase;
  private final CreateFoodUsecase createFoodUsecase;

  public FoodRestController(
      FindFoodUsecase findFoodUsecase,
      CreateFoodUsecase createFoodUsecase
  ) {
    this.findFoodUsecase = findFoodUsecase;
    this.createFoodUsecase = createFoodUsecase;
  }

  @GetMapping("/{id}")
  public FoodDetailsDto getFood(@PathVariable UUID id) {
    return this.findFoodUsecase.findFoodById(id);
  }

  @PostMapping
  public FoodDetailsDto createFood(@RequestBody FoodDto foodDto) {
    return this.createFoodUsecase.createFood(foodDto);
  }
}
