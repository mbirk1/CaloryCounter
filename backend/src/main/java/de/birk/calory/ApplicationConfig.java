package de.birk.calory;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Simple ApplicationConfig for ComponentScan.
 *
 * @author Marius Birk
 */
@Configuration
@ComponentScan(basePackages = "de.birk.calory")
@EnableAsync
public class ApplicationConfig implements WebMvcConfigurer {

  private static final int FOOD_IMPORT_POOL_SIZE = 1;
  private static final int FOOD_IMPORT_QUEUE_CAPACITY = 10;
  private static final int DUPLICATE_ANALYSIS_POOL_SIZE = 1;
  private static final int DUPLICATE_ANALYSIS_QUEUE_CAPACITY = 10;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins("http://localhost:4200")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("*")
        .allowCredentials(true);
  }

  /**
   * Dedicated, bounded executor for food import jobs (CSV and BLS alike), used instead of
   * Spring's default {@code SimpleAsyncTaskExecutor} (which spawns an unbounded thread per
   * invocation). A single worker thread is enough - imports are IO/DB bound and running more
   * than one at a time would only make them compete for the database connection pool.
   *
   * @return the executor bean, referenced by name from {@code @Async("foodImportExecutor")}
   */
  @Bean(name = "foodImportExecutor")
  public Executor foodImportExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(FOOD_IMPORT_POOL_SIZE);
    executor.setMaxPoolSize(FOOD_IMPORT_POOL_SIZE);
    executor.setQueueCapacity(FOOD_IMPORT_QUEUE_CAPACITY);
    executor.setThreadNamePrefix("food-import-");
    executor.initialize();
    return executor;
  }

  /**
   * Dedicated, bounded executor for duplicate-detection analysis jobs (FR-3), separate from
   * {@link #foodImportExecutor()} since the two are unrelated background tasks - a single worker
   * thread is enough for the same reason as the import executor: this is IO/DB bound work, and
   * running more than one analysis at a time would only make it compete for the connection pool.
   *
   * @return the executor bean, referenced by name from
   *     {@code @Async("duplicateAnalysisExecutor")}
   */
  @Bean(name = "duplicateAnalysisExecutor")
  public Executor duplicateAnalysisExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(DUPLICATE_ANALYSIS_POOL_SIZE);
    executor.setMaxPoolSize(DUPLICATE_ANALYSIS_POOL_SIZE);
    executor.setQueueCapacity(DUPLICATE_ANALYSIS_QUEUE_CAPACITY);
    executor.setThreadNamePrefix("duplicate-analysis-");
    executor.initialize();
    return executor;
  }
}
