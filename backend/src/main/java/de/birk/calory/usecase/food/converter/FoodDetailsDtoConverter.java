package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.usecase.converter.Converter;

/**
 * An inheritor of the converter class. Converts a DTO to an entity.
 *
 * @author Marius Birk
 */
public class FoodDetailsDtoConverter extends Converter<FoodDetailsDto, Food> {

  /**
   * Basic Constructor.
   */
  public FoodDetailsDtoConverter() {
    super(
        dto -> new Food.Builder()
            .id(dto.getUuid())
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
            .source(
                dto.getSource() == null ? FoodSource.MANUAL : FoodSource.valueOf(dto.getSource()))
            .externalId(dto.getExternalId())
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
        entity -> new FoodDetailsDto(
            entity.getId(),
            entity.getName(),
            entity.getCalory(),
            entity.getGrams(),
            entity.getBrand(),
            entity.getCategory(),
            entity.getFat(),
            entity.getSaturatedFat(),
            entity.getCarbohydrates(),
            entity.getSugar(),
            entity.getFiber(),
            entity.getProtein(),
            entity.getSalt(),
            entity.getSodium(),
            entity.getImageUrl(),
            entity.getSource() == null ? null : entity.getSource().name(),
            entity.getExternalId(),
            entity.getDiet() == null ? null : entity.getDiet().name(),
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
