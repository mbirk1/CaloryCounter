package de.birk.calory;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Simple ApplicationConfig for ComponentScan.
 *
 * @author Marius Birk
 */
@Configuration
@ComponentScan(basePackages = "de.birk.calory")
public class ApplicationConfig {
}
