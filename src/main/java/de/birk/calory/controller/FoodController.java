package de.birk.calory.controller;

import de.birk.calory.models.food.FoodDto;
import de.birk.calory.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Qualifier;
import java.util.UUID;

@RestController("/food")
public class FoodController {

    private FoodService foodService;

    @Autowired
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/{id}")
    public FoodDto getFood(UUID id){
        return FoodDto.of(foodService.findFood(id));
    }

}
