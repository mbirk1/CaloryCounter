package de.birk.calory.usecase.food.duplicate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Thin indirection so {@code AnalyzeFoodDuplicatesUsecase#startAnalysis} can trigger the actual
 * analysis asynchronously - the FR-3 analogue of {@code FoodImportDispatcher}.
 *
 * <p>Spring's proxy-based {@code @Async} support only intercepts calls that go through a
 * different bean - calling an {@code @Async}-annotated method on {@code this} from within the
 * same class would silently run synchronously instead, blocking the HTTP request for the whole
 * analysis. Routing the call through this separate bean sidesteps that self-invocation pitfall.
 *
 * @author Marius Birk
 */
@Component
class DuplicateAnalysisDispatcher {

  /**
   * Dispatches the actual analysis work onto the dedicated duplicate-analysis executor.
   *
   * @param analysisTask the analysis work to run, bound to its source and job status
   */
  @Async("duplicateAnalysisExecutor")
  void dispatch(Runnable analysisTask) {
    analysisTask.run();
  }
}
