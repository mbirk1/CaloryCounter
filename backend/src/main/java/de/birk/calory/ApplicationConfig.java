package de.birk.calory;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Simple ApplicationConfig for ComponentScan.
 *
 * @author Marius Birk
 */
@Configuration
@ComponentScan(basePackages = "de.birk.calory")
public class ApplicationConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins("http://localhost:4200")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("*")
        .allowCredentials(true);
  }
}
