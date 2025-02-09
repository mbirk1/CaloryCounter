package de.birk.calory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Base Class to start the Spring Boot Application.
 *
 * @author Marius Birk
 */
@SpringBootApplication
public class CaloryCounterApplication extends SpringBootServletInitializer {

  @Override
  protected final SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(CaloryCounterApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(CaloryCounterApplication.class, args);
  }

}
