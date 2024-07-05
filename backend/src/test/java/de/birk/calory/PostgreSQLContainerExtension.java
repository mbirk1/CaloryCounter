package de.birk.calory;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLContainerExtension
    implements TestExecutionListener, BeforeEachCallback, Ordered {
  private static final PostgreSQLContainer PSQL = new PostgreSQLContainer("postgres:16.3");

  static {
    PSQL.start();

    setProperties();
  }

  private static void setProperties() {
    System.setProperty("spring.datasource.url", PSQL.getJdbcUrl());
    System.setProperty("spring.datasource.username", PSQL.getUsername());
    System.setProperty("spring.datasource.password", PSQL.getPassword());
    System.setProperty("spring.datasource.driver-class-name", PSQL.getDriverClassName());
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    setProperties();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

}
