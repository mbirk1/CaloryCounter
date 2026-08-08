package de.birk.calory.adapter.primary.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;
import de.birk.calory.usecase.auth.TokenPrincipal;
import de.birk.calory.usecase.auth.ValidateTokenUsecase;

/**
 * Reads the {@code Authorization: Bearer} header of incoming requests,
 * delegates validation to {@link ValidateTokenUsecase} and, if the token is
 * valid, populates the {@link SecurityContextHolder} with a
 * {@link TokenPrincipal}.
 *
 * <p>Requests without a token, or with an invalid one, simply proceed
 * unauthenticated - {@code SecurityConfig} decides which endpoints require
 * authentication.
 *
 * @author Marius Birk
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final ValidateTokenUsecase validateTokenUsecase;

  public JwtAuthenticationFilter(ValidateTokenUsecase validateTokenUsecase) {
    this.validateTokenUsecase = validateTokenUsecase;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX.length());
      try {
        authenticate(token, request);
      } catch (InvalidTokenException | TokenExpiredException e) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(String token, HttpServletRequest request) {
    TokenPrincipal principal = this.validateTokenUsecase.validate(token);
    UsernamePasswordAuthenticationToken authentication =
        UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
