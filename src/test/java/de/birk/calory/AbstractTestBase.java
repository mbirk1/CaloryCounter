package de.birk.calory;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SuppressWarnings("rawtypes")
@SpringBootTest
public abstract class AbstractTestBase {

  @Autowired
  protected MockMvc mockMvc;

  @Container
  private final PostgreSQLContainer postgresqlContainer;

  public AbstractTestBase() {
    this.postgresqlContainer = new PostgreSQLContainer("postgres:13.1-alpine")
        .withDatabaseName("calory-db")
        .withUsername("root")
        .withPassword("root");
  }
}
