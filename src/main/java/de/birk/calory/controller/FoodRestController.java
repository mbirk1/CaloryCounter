package de.birk.calory.controller;

import de.birk.calory.controller.model.FoodDto;
import de.birk.calory.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController("/food")
public class FoodRestController {

    private FoodService foodService;

    @Autowired
    public FoodRestController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/{id}")
    public FoodDto getFood(UUID id) {
        return FoodDto.of(foodService.findFood(id));
    }

}
