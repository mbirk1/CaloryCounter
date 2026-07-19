package de.birk.calory.usecase.food.importer;

import java.nio.file.Path;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Thin indirection so {@link FoodCsvImportUsecase#startImport} can trigger the actual import
 * asynchronously.
 *
 * <p>Spring's proxy-based {@code @Async} support only intercepts calls that go through a
 * different bean - calling an {@code @Async}-annotated method on {@code this} from within the
 * same class would silently run synchronously instead, blocking the HTTP request for the whole
 * import. Routing the call through this separate bean sidesteps that self-invocation pitfall.
 *
 * @author Marius Birk
 */
@Component
class FoodCsvImportDispatcher {

  /**
   * Dispatches the actual import work onto the dedicated CSV import executor.
   *
   * @param usecase the usecase instance that performs the import
   * @param filePath the staged, temporary copy of the uploaded file
   * @param status the job status to update while importing
   */
  @Async("csvImportExecutor")
  void dispatch(FoodCsvImportUsecase usecase, Path filePath, FoodImportJobStatus status) {
    usecase.importFromFile(filePath, status);
  }
}
