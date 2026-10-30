package de.birk.calory.adapter.primary.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import de.birk.calory.usecase.auth.TokenPrincipal;

/**
 * Populates SLF4J's MDC with a {@code requestId} and the authenticated user's email for the
 * duration of a single request, so every log line written anywhere in the application while
 * handling that request - without any usecase having to pass this information around itself -
 * can be traced back to it and, where authenticated, to the user who triggered it.
 *
 * <p>Registered after {@link JwtAuthenticationFilter} in the security filter chain
 * ({@code SecurityConfig}), so {@link SecurityContextHolder} already holds the authenticated
 * {@link TokenPrincipal} (if any) by the time this filter runs.
 *
 * @author Marius Birk
 */
@Component
public class RequestTracingFilter extends OncePerRequestFilter {

  static final String REQUEST_ID_HEADER = "X-Request-Id";
  static final String REQUEST_ID_MDC_KEY = "requestId";
  static final String USER_MDC_KEY = "user";
  private static final String ANONYMOUS_USER = "anonymous";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    MDC.put(REQUEST_ID_MDC_KEY, resolveRequestId(request));
    MDC.put(USER_MDC_KEY, resolveUser());
    try {
      filterChain.doFilter(request, response);
    } finally {
      // Always runs, including when a downstream filter/controller throws - servlet container
      // threads are pooled and reused for unrelated later requests, so leaving stale MDC values
      // behind here would leak one request's (or user's) context into another's log lines.
      MDC.clear();
    }
  }

  private String resolveRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    return (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
  }

  private String resolveUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof TokenPrincipal principal) {
      return principal.getEmail();
    }
    return ANONYMOUS_USER;
  }
}
