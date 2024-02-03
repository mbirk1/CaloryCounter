package de.birk.calory.adapter.primary;

import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.usecase.food.FindFoodUsecase;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/food")
public class FoodRestController {

  private final FindFoodUsecase findFoodUsecase;

  public FoodRestController(FindFoodUsecase findFoodUsecase) {
    this.findFoodUsecase = findFoodUsecase;
  }

  @GetMapping("/{id}")
  public FoodDto getFood(UUID id) {
    return findFoodUsecase.findFoodById(id);
  }

}
