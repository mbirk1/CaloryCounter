package de.birk.calory.usecase.food.converter;

import java.util.UUID;

import de.birk.calory.adapter.primary.model.FoodDto;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * <p>{@code source} is always hardcoded to {@link FoodSource#MANUAL} and {@code externalId} to
 * {@code null} in both directions - a manually created food item is by definition not from an
 * external import, and that fact is system-determined rather than a client-supplied value.
 *
 * @author Marius Birk
 */
public class FoodDtoConverter extends Converter<FoodDto, Food> {

  /**
   * Converts a FoodDto to a Food Entity.
   */
  public FoodDtoConverter() {
    super(
        dto -> new Food.Builder()
            .id(UUID.randomUUID())
            .name(dto.getName())
            .calory(dto.getCalory())
            .grams(dto.getGrams())
            .brand(dto.getBrand())
            .category(dto.getCategory())
            .fat(dto.getFat())
            .saturatedFat(dto.getSaturatedFat())
            .carbohydrates(dto.getCarbohydrates())
            .sugar(dto.getSugar())
            .fiber(dto.getFiber())
            .protein(dto.getProtein())
            .salt(dto.getSalt())
            .sodium(dto.getSodium())
            .imageUrl(dto.getImageUrl())
            .source(FoodSource.MANUAL)
            .externalId(null)
            .diet(dto.getDiet() == null ? Diet.UNKNOWN : Diet.valueOf(dto.getDiet()))
            .vitaminA(dto.getVitaminA())
            .vitaminD(dto.getVitaminD())
            .vitaminE(dto.getVitaminE())
            .vitaminK(dto.getVitaminK())
            .vitaminB1(dto.getVitaminB1())
            .vitaminB2(dto.getVitaminB2())
            .vitaminB6(dto.getVitaminB6())
            .vitaminB12(dto.getVitaminB12())
            .vitaminC(dto.getVitaminC())
            .folate(dto.getFolate())
            .potassium(dto.getPotassium())
            .calcium(dto.getCalcium())
            .magnesium(dto.getMagnesium())
            .iron(dto.getIron())
            .zinc(dto.getZinc())
            .iodine(dto.getIodine())
            .build(),
        entity -> new FoodDto(
            entity.getName(),
            entity.getCalory(),
            entity.getGrams(),
            entity.getDiet() == null ? null : entity.getDiet().name(),
            entity.getBrand(),
            entity.getCategory(),
            entity.getImageUrl(),
            entity.getFat(),
            entity.getSaturatedFat(),
            entity.getCarbohydrates(),
            entity.getSugar(),
            entity.getFiber(),
            entity.getProtein(),
            entity.getSalt(),
            entity.getSodium(),
            entity.getVitaminA(),
            entity.getVitaminD(),
            entity.getVitaminE(),
            entity.getVitaminK(),
            entity.getVitaminB1(),
            entity.getVitaminB2(),
            entity.getVitaminB6(),
            entity.getVitaminB12(),
            entity.getVitaminC(),
            entity.getFolate(),
            entity.getPotassium(),
            entity.getCalcium(),
            entity.getMagnesium(),
            entity.getIron(),
            entity.getZinc(),
            entity.getIodine()
        )
    );
  }
}
