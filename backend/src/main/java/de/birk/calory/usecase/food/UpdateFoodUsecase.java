package de.birk.calory.usecase.food;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.UpdateFoodDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.ValidationException;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Usecase for updating an existing food item's editable fields.
 *
 * @author Marius Birk
 */
@Component
public class UpdateFoodUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(UpdateFoodUsecase.class);

  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter persistenceConverter;
  private final FoodDetailsDtoConverter detailsDtoConverter;

  /**
   * Basic Constructor takes a repository and creates the corresponding converters.
   *
   * @param foodRepository is used to load and persist food entities
   */
  public UpdateFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.persistenceConverter = new FoodPersistenceConverter();
    this.detailsDtoConverter = new FoodDetailsDtoConverter();
  }

  /**
   * Updates an existing food item's editable fields (name, calory, grams, diet and the
   * macronutrients), leaving its provenance fields (brand, category, imageUrl, source,
   * externalId) untouched.
   *
   * @param id the identifier of the food item to update
   * @param updateFoodDto the submitted, editable fields
   * @return the updated food item
   * @throws java.util.NoSuchElementException if no food item exists for {@code id}
   * @throws de.birk.calory.exception.ValidationException if a core field is missing, or calory
   *     or a macronutrient falls outside its plausible range
   */
  public FoodDetailsDto updateFood(UUID id, UpdateFoodDto updateFoodDto) {
    LOG.debug(
        "Aktualisierungsanfrage fuer Lebensmittel {} erhalten: name='{}', calory={}, grams={}",
        id, updateFoodDto.getName(), updateFoodDto.getCalory(), updateFoodDto.getGrams());
    FoodPersistence existingPersistence = this.foodRepository.findById(id)
        .orElseThrow(() -> {
          LOG.warn("Aktualisierungsanfrage fuer nicht vorhandenes Lebensmittel {}", id);
          return new NoSuchElementException();
        });
    Food existing = this.persistenceConverter.convertFromDto(existingPersistence);

    Food updated = new Food(
        id,
        updateFoodDto.getName(),
        updateFoodDto.getCalory(),
        updateFoodDto.getGrams(),
        existing.getBrand(),
        existing.getCategory(),
        updateFoodDto.getFat(),
        updateFoodDto.getSaturatedFat(),
        updateFoodDto.getCarbohydrates(),
        updateFoodDto.getSugar(),
        updateFoodDto.getFiber(),
        updateFoodDto.getProtein(),
        updateFoodDto.getSalt(),
        updateFoodDto.getSodium(),
        existing.getImageUrl(),
        existing.getSource(),
        existing.getExternalId(),
        updateFoodDto.getDiet() == null ? Diet.UNKNOWN : Diet.valueOf(updateFoodDto.getDiet())
    );
    try {
      updated.validatePlausibility();
    } catch (ValidationException e) {
      LOG.warn(
          "Aktualisierung von Lebensmittel {} abgelehnt: unplausibler Wert (calory={})",
          id, updateFoodDto.getCalory());
      throw e;
    }

    FoodPersistence saved = this.foodRepository.save(
        this.persistenceConverter.convertFromEntity(updated));
    Food savedFood = this.persistenceConverter.convertFromDto(saved);
    LOG.info("Lebensmittel {} ('{}') erfolgreich aktualisiert", id, savedFood.getName());

    return this.detailsDtoConverter.convertFromEntity(savedFood);
  }
}
