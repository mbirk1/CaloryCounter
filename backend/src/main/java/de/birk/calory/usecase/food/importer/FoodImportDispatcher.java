package de.birk.calory.usecase.food.importer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Thin indirection so a food import usecase's {@code startImport} method can trigger the actual
 * import asynchronously, regardless of the source file format.
 *
 * <p>Spring's proxy-based {@code @Async} support only intercepts calls that go through a
 * different bean - calling an {@code @Async}-annotated method on {@code this} from within the
 * same class would silently run synchronously instead, blocking the HTTP request for the whole
 * import. Routing the call through this separate bean sidesteps that self-invocation pitfall.
 *
 * @author Marius Birk
 */
@Component
class FoodImportDispatcher {

  /**
   * Dispatches the actual import work onto the dedicated food import executor.
   *
   * @param importTask the format-specific import work to run, e.g. a
   *     {@code usecase::importFromFile} method reference bound to its staged file and job status
   */
  @Async("foodImportExecutor")
  void dispatch(Runnable importTask) {
    importTask.run();
  }
}
