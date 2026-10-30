package de.birk.calory.usecase.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import de.birk.calory.adapter.primary.model.RecipeDetailsDto;
import de.birk.calory.adapter.secondary.RecipeRepository;
import de.birk.calory.adapter.secondary.model.RecipePersistence;

@ExtendWith(MockitoExtension.class)
public class FindRecipeUsecaseUnitTest {

  @Mock
  private RecipeRepository recipeRepository;

  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  public void attachLogAppender() {
    logAppender = new ListAppender<>();
    logAppender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(FindRecipeUsecase.class);
    // The root logger defaults to INFO, so DEBUG events never reach this appender otherwise -
    // Logback filters by level on the logger itself, before any appender ever sees the event.
    logger.setLevel(Level.DEBUG);
    logger.addAppender(logAppender);
  }

  @AfterEach
  public void detachLogAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger(FindRecipeUsecase.class);
    logger.detachAppender(logAppender);
    logger.setLevel(null);
  }

  private RecipePersistence recipe(UUID id, String name) {
    RecipePersistence recipe = new RecipePersistence(id, name, 1);
    recipe.setFoodEntries(List.of());
    return recipe;
  }

  @Test
  public void findsARecipeByIdAndLogsTheRequestTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(recipeRepository.findById(id)).thenReturn(Optional.of(recipe(id, "Bowl")));
    FindRecipeUsecase usecase = new FindRecipeUsecase(recipeRepository);

    // Act
    RecipeDetailsDto result = usecase.findRecipeById(id);

    // Assert
    assertThat(result.getName()).isEqualTo("Bowl");
    assertThat(logAppender.list)
        .anyMatch(event -> event.getLevel() == Level.DEBUG
            && event.getFormattedMessage().contains(id.toString()));
  }

  @Test
  public void logsAWarningAndThrowsWhenTheRecipeIsNotFoundTest() {
    // Arrange
    UUID id = UUID.randomUUID();
    when(recipeRepository.findById(id)).thenReturn(Optional.empty());
    FindRecipeUsecase usecase = new FindRecipeUsecase(recipeRepository);

    // Act & Assert
    assertThatThrownBy(() -> usecase.findRecipeById(id))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("Recipe not found");
    assertThat(logAppender.list)
        .anyMatch(event -> event.getLevel() == Level.WARN
            && event.getFormattedMessage().contains(id.toString()));
  }

  @Test
  public void findsAllRecipesAndLogsTheHitCountTest() {
    // Arrange
    when(recipeRepository.findAll()).thenReturn(List.of(
        recipe(UUID.randomUUID(), "Bowl"), recipe(UUID.randomUUID(), "Salad")));
    FindRecipeUsecase usecase = new FindRecipeUsecase(recipeRepository);

    // Act
    List<RecipeDetailsDto> result = usecase.findAllRecipes();

    // Assert
    assertThat(result).hasSize(2);
    assertThat(logAppender.list)
        .anyMatch(event -> event.getLevel() == Level.DEBUG
            && event.getFormattedMessage().contains("2"));
  }
}
