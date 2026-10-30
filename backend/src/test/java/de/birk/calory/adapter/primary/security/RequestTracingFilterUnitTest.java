package de.birk.calory.adapter.primary.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import de.birk.calory.usecase.auth.TokenPrincipal;

public class RequestTracingFilterUnitTest {

  private final RequestTracingFilter filter = new RequestTracingFilter();

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  @Test
  public void reusesTheRequestIdHeaderWhenPresentTest() throws ServletException, IOException {
    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Request-Id", "given-request-id");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    String[] observedRequestId = new String[1];
    doAnswer(invocation -> {
      observedRequestId[0] = MDC.get("requestId");
      return null;
    }).when(filterChain).doFilter(any(), any());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(observedRequestId[0]).isEqualTo("given-request-id");
    assertThat(MDC.get("requestId")).isNull();
  }

  @Test
  public void generatesARequestIdWhenTheHeaderIsMissingTest() throws ServletException, IOException {
    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    String[] observedRequestId = new String[1];
    doAnswer(invocation -> {
      observedRequestId[0] = MDC.get("requestId");
      return null;
    }).when(filterChain).doFilter(any(), any());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert - a real, parseable UUID was generated
    assertThat(UUID.fromString(observedRequestId[0])).isNotNull();
  }

  @Test
  public void generatesARequestIdWhenTheHeaderIsBlankTest() throws ServletException, IOException {
    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Request-Id", "   ");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    String[] observedRequestId = new String[1];
    doAnswer(invocation -> {
      observedRequestId[0] = MDC.get("requestId");
      return null;
    }).when(filterChain).doFilter(any(), any());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(UUID.fromString(observedRequestId[0])).isNotNull();
  }

  @Test
  public void usesTheAuthenticatedPrincipalsEmailAsUserTest() throws ServletException, IOException {
    // Arrange
    TokenPrincipal principal = new TokenPrincipal(UUID.randomUUID(), "user@example.com");
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    String[] observedUser = new String[1];
    doAnswer(invocation -> {
      observedUser[0] = MDC.get("user");
      return null;
    }).when(filterChain).doFilter(any(), any());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(observedUser[0]).isEqualTo("user@example.com");
    assertThat(MDC.get("user")).isNull();
  }

  @Test
  public void usesAnonymousAsUserWhenUnauthenticatedTest() throws ServletException, IOException {
    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    String[] observedUser = new String[1];
    doAnswer(invocation -> {
      observedUser[0] = MDC.get("user");
      return null;
    }).when(filterChain).doFilter(any(), any());

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(observedUser[0]).isEqualTo("anonymous");
  }

  @Test
  public void clearsMdcEvenWhenTheFilterChainThrowsTest() throws ServletException, IOException {
    // Arrange - the whole point of the finally block: a downstream failure must not leak MDC
    // context into the next request handled by the same pooled servlet-container thread.
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);
    doThrow(new IllegalStateException("boom")).when(filterChain).doFilter(any(), any());

    // Act & Assert
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(IllegalStateException.class);
    assertThat(MDC.get("requestId")).isNull();
    assertThat(MDC.get("user")).isNull();
    verify(filterChain).doFilter(request, response);
  }
}
