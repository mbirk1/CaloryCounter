package de.birk.calory.usecase.recipe;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.primary.model.RecipeDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.adapter.secondary.model.RecipePersistence;
import de.birk.calory.domain.recipe.Recipe;
import de.birk.calory.domain.recipe.RecipeFoodEntry;
import de.birk.calory.exception.ValidationException;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;
import de.birk.calory.usecase.recipe.converter.RecipeDetailsDtoConverter;
import de.birk.calory.usecase.recipe.converter.RecipePersistenceConverter;

/**
 * Usecase to create a recipe.
 *
 * @author Marius Birk
 */
@Component
public class CreateRecipeUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(CreateRecipeUsecase.class);

  private final RecipeRepository recipeRepository;
  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter foodPersistenceConverter =
      new FoodPersistenceConverter();
  private final RecipePersistenceConverter recipePersistenceConverter =
      new RecipePersistenceConverter();
  private final RecipeDetailsDtoConverter recipeDetailsDtoConverter =
      new RecipeDetailsDtoConverter();

  /**
   * Simple constructor that receives repositories and creates corresponding converters.
   *
   * @param recipeRepository repository for recipes
   * @param foodRepository repository for foods, used to resolve each entry's referenced food
   */
  public CreateRecipeUsecase(RecipeRepository recipeRepository, FoodRepository foodRepository) {
    this.recipeRepository = recipeRepository;
    this.foodRepository = foodRepository;
  }

  /**
   * Creates a new Recipe and returns it, including its server-computed nutrition totals,
   * per-serving values and diet.
   *
   * <p>Every food entry is resolved server-side from its {@code foodId} - the client never
   * supplies nutrition data for a recipe's foods, only a reference and an amount, so a client
   * can never "smuggle in" arbitrary nutrition values for a recipe.
   *
   * @param dto the dto with all necessary properties
   *
   * @return the newly created recipe, as actually persisted
   */
  public RecipeDetailsDto createRecipe(RecipeDto dto) {
    LOG.debug("Rezept-Anlage angefragt: {}", dto.getName());
    if (dto.getFoods() == null || dto.getFoods().isEmpty()) {
      LOG.warn("Rezept-Anlage ohne Lebensmittel-Eintraege abgelehnt");
      throw new ValidationException();
    }

    List<RecipeFoodEntry> entries = dto.getFoods().stream().map(entryDto -> {
      FoodPersistence foodPersistence = foodRepository.findById(entryDto.getFoodId())
          .orElseThrow(() -> new NoSuchElementException("Referenced food not found"));
      return new RecipeFoodEntry(
          foodPersistenceConverter.convertFromDto(foodPersistence), entryDto.getAmountGrams());
    }).toList();

    Recipe recipe = new Recipe(UUID.randomUUID(), dto.getName(), entries, dto.getServings());

    RecipePersistence saved =
        recipeRepository.save(recipePersistenceConverter.convertFromEntity(recipe));
    Recipe savedRecipe = recipePersistenceConverter.convertFromDto(saved);
    LOG.info("Rezept '{}' ({}) erfolgreich angelegt", savedRecipe.getName(), savedRecipe.getId());
    return recipeDetailsDtoConverter.convertFromEntity(savedRecipe);
  }
}
