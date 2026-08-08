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

  private static final int CSV_IMPORT_POOL_SIZE = 1;
  private static final int CSV_IMPORT_QUEUE_CAPACITY = 10;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins("http://localhost:4200")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("*")
        .allowCredentials(true);
  }

  /**
   * Dedicated, bounded executor for CSV import jobs, used instead of Spring's default
   * {@code SimpleAsyncTaskExecutor} (which spawns an unbounded thread per invocation). A single
   * worker thread is enough - imports are IO/DB bound and running more than one at a time would
   * only make them compete for the database connection pool.
   *
   * @return the executor bean, referenced by name from {@code @Async("csvImportExecutor")}
   */
  @Bean(name = "csvImportExecutor")
  public Executor csvImportExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CSV_IMPORT_POOL_SIZE);
    executor.setMaxPoolSize(CSV_IMPORT_POOL_SIZE);
    executor.setQueueCapacity(CSV_IMPORT_QUEUE_CAPACITY);
    executor.setThreadNamePrefix("csv-import-");
    executor.initialize();
    return executor;
  }
}
