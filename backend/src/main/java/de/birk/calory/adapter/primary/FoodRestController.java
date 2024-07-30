package de.birk.calory.adapter.primary;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PostRequest;
import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.usecase.food.CreateFoodUsecase;
import de.birk.calory.usecase.food.FindFoodUsecase;

@RestController
@RequestMapping("/food")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:3000")
public class FoodRestController {
  private final FindFoodUsecase findFoodUsecase;
  private final CreateFoodUsecase createFoodUsecase;

  public FoodRestController(
      FindFoodUsecase findFoodUsecase,
      CreateFoodUsecase createFoodUsecase) {
    this.findFoodUsecase = findFoodUsecase;
    this.createFoodUsecase = createFoodUsecase;
  }

  @GetRequest()
  public List<FoodDetailsDto> getAllFoods() {
    return this.findFoodUsecase.findAllFoods();
  }

  @GetRequest("/{id}")
  public FoodDetailsDto getFood(@PathVariable UUID id) {
    return this.findFoodUsecase.findFoodById(id);
  }

  @PostRequest
  public FoodDetailsDto createFood(@RequestBody FoodDto foodDto) {
    return this.createFoodUsecase.createFood(foodDto);
  }
}
