package de.birk.calory;

import jakarta.transaction.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@Transactional
@Testcontainers
public @interface IntegrationTest {

}
