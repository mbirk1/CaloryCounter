package de.birk.calory.usecase.recipe.converter;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeFoodEntryDetailsDto;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.domain.recipe.RecipeFoodEntry;
import de.birk.calory.usecase.converter.Converter;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;

/**
 * Inheritor of the converter class. Converts between {@link Recipe} and its detailed response
 * DTO - the nutrition totals, per-serving values and diet on the DTO side are always computed
 * fresh from {@link Recipe}'s own aggregation methods, never persisted separately.
 *
 * @author Marius Birk
 */
public class RecipeDetailsDtoConverter extends Converter<RecipeDetailsDto, Recipe> {

  /**
   * Constructor. Converts detailed recipe dtos to recipes and the other way around.
   */
  public RecipeDetailsDtoConverter() {
    super(
        dto -> {
          FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();
          return new Recipe(
              dto.getUuid(),
              dto.getName(),
              dto.getFoods().stream()
                  .map(entry -> new RecipeFoodEntry(
                      foodDetailsDtoConverter.convertFromDto(entry.getFood()),
                      entry.getAmountGrams()
                  ))
                  .toList(),
              dto.getServings()
          );
        },
        entity -> {
          FoodDetailsDtoConverter foodDetailsDtoConverter = new FoodDetailsDtoConverter();
          return new RecipeDetailsDto(
              entity.getId(),
              entity.getName(),
              entity.getServings(),
              entity.getEntries().stream()
                  .map(entry -> new RecipeFoodEntryDetailsDto(
                      foodDetailsDtoConverter.convertFromEntity(entry.getFood()),
                      entry.getAmountGrams()
                  ))
                  .toList(),
              entity.getTotalCalories(),
              entity.getTotalFat(),
              entity.getTotalSaturatedFat(),
              entity.getTotalCarbohydrates(),
              entity.getTotalSugar(),
              entity.getTotalFiber(),
              entity.getTotalProtein(),
              entity.getTotalSalt(),
              entity.getTotalSodium(),
              entity.getCaloriesPerServing(),
              entity.getFatPerServing(),
              entity.getSaturatedFatPerServing(),
              entity.getCarbohydratesPerServing(),
              entity.getSugarPerServing(),
              entity.getFiberPerServing(),
              entity.getProteinPerServing(),
              entity.getSaltPerServing(),
              entity.getSodiumPerServing(),
              entity.getDiet().name()
          );
        }
    );
  }
}
