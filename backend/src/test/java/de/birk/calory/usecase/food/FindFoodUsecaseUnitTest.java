package de.birk.calory.usecase.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import de.birk.calory.adapter.primary.model.FoodDetailsDto;
import de.birk.calory.adapter.primary.model.PageResponseDto;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;
import de.birk.calory.exception.InvalidSortParameterException;

@ExtendWith(MockitoExtension.class)
public class FindFoodUsecaseUnitTest {

  @Mock
  private FoodRepository foodRepository;

  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  public void attachLogAppender() {
    logAppender = new ListAppender<>();
    logAppender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(FindFoodUsecase.class);
    // The root logger defaults to INFO, so DEBUG events never reach this appender otherwise -
    // Logback filters by level on the logger itself, before any appender ever sees the event.
    logger.setLevel(Level.DEBUG);
    logger.addAppender(logAppender);
  }

  @AfterEach
  public void detachLogAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger(FindFoodUsecase.class);
    logger.detachAppender(logAppender);
    logger.setLevel(null);
  }

  @Test
  public void findsAFoodByIdAndLogsTheRequestTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    FoodPersistence food = FoodPersistence.builder().id(id).name("Apfel")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
    when(foodRepository.findById(id)).thenReturn(Optional.of(food));
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act
    FoodDetailsDto result = usecase.findFoodById(id);

    // Assert
    assertThat(result.getName()).isEqualTo("Apfel");
    assertThat(logAppender.list)
        .anyMatch(event -> event.getLevel() == Level.DEBUG
            && event.getFormattedMessage().contains(id.toString()));
  }

  @Test
  public void logsAWarningAndThrowsWhenTheFoodIsNotFoundTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(foodRepository.findById(id)).thenReturn(Optional.empty());
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findFoodById(id))
        .isInstanceOf(NoSuchElementException.class);
    assertThat(logAppender.list)
        .anyMatch(event -> event.getLevel() == Level.WARN
            && event.getFormattedMessage().contains(id.toString()));
  }

  @Test
  public void findsAllFoodsWithDefaultParametersTest() {
    // Arrange
    FoodPersistence food = FoodPersistence.builder().id(UUID.randomUUID()).name("Apfel")
        .calory(BigDecimal.TEN).grams(BigDecimal.valueOf(100)).build();
    when(foodRepository.search(isNull(), isNull(), any()))
        .thenReturn(new PageImpl<>(List.of(food), PageRequest.of(0, 20), 1));
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act
    PageResponseDto<FoodDetailsDto> result =
        usecase.findAllFoods(0, 20, null, null, "name", "asc");

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findsAllFoodsFilteredBySearchAndDietTest() {
    // Arrange
    when(foodRepository.search(eq("Apfel"), eq("VEGAN"), any()))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act
    PageResponseDto<FoodDetailsDto> result =
        usecase.findAllFoods(0, 20, " Apfel ", "vegan", "calory", "desc");

    // Assert
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  public void rejectsAnInvalidSortFieldTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findAllFoods(0, 20, null, null, "brand", "asc"))
        .isInstanceOf(InvalidSortParameterException.class);
  }

  @Test
  public void rejectsAnInvalidSortDirectionTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findAllFoods(0, 20, null, null, "name", "sideways"))
        .isInstanceOf(InvalidSortParameterException.class);
  }

  @Test
  public void rejectsAnInvalidDietFilterTest() {
    // Arrange
    FindFoodUsecase usecase = new FindFoodUsecase(foodRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findAllFoods(0, 20, null, "NOT_A_DIET", "name", "asc"))
        .isInstanceOf(InvalidSortParameterException.class);
  }
}
