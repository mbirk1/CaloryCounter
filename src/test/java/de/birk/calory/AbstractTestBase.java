package de.birk.calory;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SuppressWarnings("rawtypes")
public class AbstractTestBase {

    @Container
    private final PostgreSQLContainer postgresqlContainer;

    public AbstractTestBase() {
        this.postgresqlContainer = new  PostgreSQLContainer("postgres:13.1-alpine")
                .withDatabaseName("calory-db")
                .withUsername("root")
                .withPassword("root");
    }
}
