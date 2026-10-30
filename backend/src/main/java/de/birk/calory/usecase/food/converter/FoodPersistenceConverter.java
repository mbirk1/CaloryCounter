package de.birk.calory.usecase.food.converter;

import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.domain.food.FoodSource;
import de.birk.calory.usecase.converter.Converter;

/**
 * Inheritor of the converter class.
 *
 * @author Marius Birk
 */
public class FoodPersistenceConverter extends Converter<FoodPersistence, Food> {

  /**
   * Converts from a FoodPersitence to a Food entity.
   */
  public FoodPersistenceConverter() {
    super(
        dto -> new Food.Builder()
            .id(dto.getId())
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
        entity -> new FoodPersistence.Builder()
            .id(entity.getId())
            .name(entity.getName())
            .calory(entity.getCalory())
            .grams(entity.getGrams())
            .brand(entity.getBrand())
            .category(entity.getCategory())
            .fat(entity.getFat())
            .saturatedFat(entity.getSaturatedFat())
            .carbohydrates(entity.getCarbohydrates())
            .sugar(entity.getSugar())
            .fiber(entity.getFiber())
            .protein(entity.getProtein())
            .salt(entity.getSalt())
            .sodium(entity.getSodium())
            .imageUrl(entity.getImageUrl())
            .source(entity.getSource() == null ? null : entity.getSource().name())
            .externalId(entity.getExternalId())
            .diet(entity.getDiet() == null ? null : entity.getDiet().name())
            .vitaminA(entity.getVitaminA())
            .vitaminD(entity.getVitaminD())
            .vitaminE(entity.getVitaminE())
            .vitaminK(entity.getVitaminK())
            .vitaminB1(entity.getVitaminB1())
            .vitaminB2(entity.getVitaminB2())
            .vitaminB6(entity.getVitaminB6())
            .vitaminB12(entity.getVitaminB12())
            .vitaminC(entity.getVitaminC())
            .folate(entity.getFolate())
            .potassium(entity.getPotassium())
            .calcium(entity.getCalcium())
            .magnesium(entity.getMagnesium())
            .iron(entity.getIron())
            .zinc(entity.getZinc())
            .iodine(entity.getIodine())
            .build()
    );
  }
}
