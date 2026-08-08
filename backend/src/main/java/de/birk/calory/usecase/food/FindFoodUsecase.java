package de.birk.calory.usecase.food;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.domain.food.Diet;
import de.birk.calory.domain.food.Food;
import de.birk.calory.exception.InvalidSortParameterException;
import de.birk.calory.usecase.food.converter.FoodDetailsDtoConverter;
import de.birk.calory.usecase.food.converter.FoodPersistenceConverter;

/**
 * Usecase to find food items in the repository.
 *
 * @author Marius Birk
 */
@Service
public class FindFoodUsecase {

  private static final Logger LOG = LoggerFactory.getLogger(FindFoodUsecase.class);

  /**
   * The only fields clients are allowed to sort by, matching {@link FoodPersistence}'s Java
   * field names (not its {@code @Column} names) - {@code Sort} is resolved by Spring Data
   * against the JPA entity's attribute names, so e.g. {@code calory} is correct here even though
   * the underlying database column is {@code calory_count}.
   */
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "calory", "grams");

  private final FoodRepository foodRepository;
  private final FoodPersistenceConverter persistenceConverter;
  private final FoodDetailsDtoConverter detailsDtoConverter;

  /**
   * Simple constructor that takes a repository and creates corresponding converters.
   *
   * @param foodRepository the food repository that connects to the database
   *
   */
  public FindFoodUsecase(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
    this.persistenceConverter = new FoodPersistenceConverter();
    this.detailsDtoConverter = new FoodDetailsDtoConverter();
  }

  /**
   * Finds a page of food items, optionally filtered by a case-insensitive substring match on the
   * name and/or an exact diet match, sorted by a server-validated field and direction.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @param search a case-insensitive substring to match against the name, or {@code null}/blank
   *     to not filter by name
   * @param diet the name of the {@code Diet} enum constant to filter by, or {@code null}/blank
   *     to not filter by diet
   * @param sort the field to sort by, must be one of {@link #ALLOWED_SORT_FIELDS}
   * @param direction the sort direction ({@code asc}/{@code desc}, case-insensitive)
   * @return the requested page of food items
   * @throws InvalidSortParameterException if {@code sort}, {@code direction} or {@code diet}
   *     is not one of the allowed values
   */
  public PageResponseDto<FoodDetailsDto> findAllFoods(
      int page, int size, String search, String diet, String sort, String direction) {
    LOG.debug(
        "Lebensmittel-Anfrage erhalten: page={}, size={}, search='{}', diet='{}', sort='{}', "
            + "direction='{}'",
        page, size, search, diet, sort, direction);

    Sort resolvedSort = Sort.by(parseDirection(direction), sortField(sort));
    Pageable pageable = PageRequest.of(page, size, resolvedSort);
    String normalizedSearch = blankToNull(search);
    String normalizedDiet = normalizeDiet(diet);

    Page<FoodPersistence> result =
        this.foodRepository.search(normalizedSearch, normalizedDiet, pageable);

    LOG.debug(
        "Lebensmittel-Anfrage ausgefuehrt: normalizedSearch='{}', normalizedDiet='{}', "
            + "sortField='{}', direction='{}' -> {} Treffer auf {} Seiten (aktuelle Seite {})",
        normalizedSearch, normalizedDiet, sort, resolvedSort, result.getTotalElements(),
        result.getTotalPages(), result.getNumber());

    List<Food> foods = this.persistenceConverter.convertFromDtos(result.getContent());
    List<FoodDetailsDto> content = this.detailsDtoConverter.convertFromEntities(foods);

    return new PageResponseDto<>(
        content, result.getNumber(), result.getSize(), result.getTotalElements(),
        result.getTotalPages(), result.isLast()
    );
  }

  /**
   * Finds a specific foot item with its uuid.
   *
   * @param uuid the identifier for the food item
   *
   * @return the food item
   */
  public FoodDetailsDto findFoodById(UUID uuid) {
    FoodPersistence foodPersistence = this.foodRepository.findById(uuid)
        .orElseThrow();

    Food food = this.persistenceConverter.convertFromDto(foodPersistence);
    return this.detailsDtoConverter.convertFromEntity(food);
  }

  private String sortField(String sort) {
    if (sort == null || !ALLOWED_SORT_FIELDS.contains(sort)) {
      LOG.warn(
          "Ungueltiges Sortierfeld '{}' angefragt, erlaubt sind {}", sort, ALLOWED_SORT_FIELDS);
      throw new InvalidSortParameterException(
          "Invalid sort field '" + sort + "', allowed values are " + ALLOWED_SORT_FIELDS);
    }
    return sort;
  }

  private Sort.Direction parseDirection(String direction) {
    try {
      return Sort.Direction.fromString(direction == null ? "asc" : direction);
    } catch (IllegalArgumentException e) {
      LOG.warn("Ungueltige Sortierrichtung '{}' angefragt, erlaubt sind asc, desc", direction);
      throw new InvalidSortParameterException(
          "Invalid sort direction '" + direction + "', allowed values are asc, desc");
    }
  }

  private String normalizeDiet(String diet) {
    String trimmed = blankToNull(diet);
    if (trimmed == null) {
      return null;
    }
    try {
      return Diet.valueOf(trimmed.toUpperCase()).name();
    } catch (IllegalArgumentException e) {
      LOG.warn("Ungueltiger Diaet-Filter '{}' angefragt, erlaubt sind {}", diet, Diet.values());
      throw new InvalidSortParameterException(
          "Invalid diet filter '" + diet + "', allowed values are "
              + List.of(Diet.values()));
    }
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value.trim();
  }
}
