package de.birk.calory.adapter.primary.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT based security configuration.
 *
 * <p>This deliberately stays a self-contained resource server without a
 * full OAuth2 Authorization Server: {@code /api/auth/**} issues
 * self-signed JWTs via {@code TokenService}, and every other API endpoint
 * requires a valid bearer token. A future external identity provider
 * (Google/Apple, Keycloak) could be added via
 * {@code spring-boot-starter-oauth2-client} without touching this class,
 * since it only deals with validating already-issued tokens.
 *
 * @author Marius Birk
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final int BCRYPT_STRENGTH = 12;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  /**
   * Configures the stateless security filter chain.
   *
   * <p>Everything outside of {@code /api/**} (the Angular single page
   * application and its static assets, served from the same jar) stays
   * public, as does {@code /api/auth/**}. Every other {@code /api/**}
   * endpoint requires a valid access token. CORS preflight requests are
   * always permitted and delegated to the {@code CorsConfigurationSource}
   * already registered by {@code ApplicationConfig}, otherwise the browser
   * preflight for cross-origin requests carrying an Authorization header
   * would be rejected before Spring MVC ever sees it.
   *
   * @param http the security configuration builder
   * @return the configured filter chain
   * @throws Exception if the configuration cannot be built
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling ->
            handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(request -> !request.getRequestURI().startsWith("/api/"))
            .permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Password hashing bean, shared by registration and login.
   *
   * @return a BCrypt encoder with a work factor of {@value #BCRYPT_STRENGTH}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
  }
}
