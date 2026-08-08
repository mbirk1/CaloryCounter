package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.InvalidSortParameterException;

@ExtendWith(MockitoExtension.class)
public class FindFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  @Test
  public void usesDefaultSortByNameAscendingWhenNoParametersGivenTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act
    usecase.findAllFoods(0, 20, null, null, "name", "asc");

    // Assert
    Pageable pageable = capturePageable();
    Sort.Order order = pageable.getSort().getOrderFor("name");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
  }

  @Test
  public void passesTheSearchTermThroughToTheRepositoryTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act
    usecase.findAllFoods(0, 20, "Apfel", null, "name", "asc");

    // Assert
    verify(foodRepository).search(eq("Apfel"), isNull(), any(Pageable.class));
  }

  @Test
  public void treatsABlankSearchTermAsNoFilterTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act
    usecase.findAllFoods(0, 20, "   ", null, "name", "asc");

    // Assert
    verify(foodRepository).search(isNull(), isNull(), any(Pageable.class));
  }

  @Test
  public void passesTheNormalizedDietFilterThroughToTheRepositoryTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act - lower-case input is normalized to the enum constant's upper-case name
    usecase.findAllFoods(0, 20, null, "vegan", "name", "asc");

    // Assert
    verify(foodRepository).search(isNull(), eq("VEGAN"), any(Pageable.class));
  }

  @Test
  public void combinesSearchAndDietFilterTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act
    usecase.findAllFoods(0, 20, "Tofu", "VEGAN", "name", "asc");

    // Assert
    verify(foodRepository).search(eq("Tofu"), eq("VEGAN"), any(Pageable.class));
  }

  @Test
  public void sortsByCaloryDescendingTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    stubEmptySearch();

    // Act
    usecase.findAllFoods(0, 20, null, null, "calory", "desc");

    // Assert
    Pageable pageable = capturePageable();
    Sort.Order order = pageable.getSort().getOrderFor("calory");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  @Test
  public void rejectsAnUnknownSortFieldTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(
        () -> usecase.findAllFoods(0, 20, null, null, "externalId", "asc"))
        .isInstanceOf(InvalidSortParameterException.class);
  }

  @Test
  public void rejectsAnUnknownSortDirectionTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(
        () -> usecase.findAllFoods(0, 20, null, null, "name", "sideways"))
        .isInstanceOf(InvalidSortParameterException.class);
  }

  @Test
  public void rejectsAnUnknownDietFilterTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(
        () -> usecase.findAllFoods(0, 20, null, "PESCATARIAN", "name", "asc"))
        .isInstanceOf(InvalidSortParameterException.class);
  }

  @Test
  public void mapsThePageResultToADetailsDtoPageTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);
    FoodPersistence persisted = new FoodPersistence(
        UUID.randomUUID(), "Apple", new BigDecimal("52"), new BigDecimal("100"));
    when(foodRepository.search(any(), any(), any(Pageable.class))).thenReturn(
        new PageImpl<>(List.of(persisted), PageRequest.of(0, 20), 1));

    // Act
    PageResponseDto<FoodDetailsDto> result = usecase.findAllFoods(0, 20, null, null, "name", "asc");

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getName()).isEqualTo("Apple");
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  private void stubEmptySearch() {
    when(foodRepository.search(any(), any(), any(Pageable.class))).thenReturn(
        new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
  }

  private Pageable capturePageable() {
    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(foodRepository).search(any(), any(), captor.capture());
    return captor.getValue();
  }
}
