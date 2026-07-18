package de.birk.calory.adapter.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
public class AuthRestControllerTest extends AbstractTestBase {

  @Test
  @DisplayName("registers a new user and returns an access token plus a refresh cookie")
  public void registerReturnsTokensTest() throws Exception {
    String content = readResourceAsString("/http-bodies/registerUser.json");

    this.mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.profileCompleted").value(false))
        .andExpect(cookie().exists("refreshToken"))
        .andExpect(cookie().httpOnly("refreshToken", true));
  }

  @Test
  @DisplayName("rejects registration with an already used email")
  public void registerWithDuplicateEmailTest() throws Exception {
    String content = readResourceAsString("/http-bodies/registerUser.json");

    this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isOk());

    this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isConflict());
  }

  @Test
  @DisplayName("logs in with valid credentials")
  public void loginWithValidCredentialsTest() throws Exception {
    String registerContent = readResourceAsString("/http-bodies/registerUser.json");
    this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerContent)
    ).andExpect(status().isOk());

    String loginContent = readResourceAsString("/http-bodies/loginUser.json");
    this.mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginContent)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  @DisplayName("rejects login with a wrong password")
  public void loginWithWrongPasswordTest() throws Exception {
    String registerContent = readResourceAsString("/http-bodies/registerUser.json");
    this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerContent)
    ).andExpect(status().isOk());

    String content = "{\"email\":\"auth-test-user@example.com\",\"password\":\"wrong-password\"}";
    this.mockMvc.perform(
        post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("rejects login for an unknown email")
  public void loginWithUnknownEmailTest() throws Exception {
    String content = "{\"email\":\"unknown@example.com\",\"password\":\"wrong-password\"}";
    this.mockMvc.perform(
        post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("rotates a valid refresh token")
  public void refreshRotatesTokenTest() throws Exception {
    Cookie refreshCookie = registerAndGetRefreshCookie();

    this.mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(cookie().exists("refreshToken"));
  }

  @Test
  @DisplayName("rejects a reused (already rotated) refresh token")
  public void refreshRejectsReusedTokenTest() throws Exception {
    Cookie refreshCookie = registerAndGetRefreshCookie();

    this.mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
        .andExpect(status().isOk());

    this.mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("rejects a refresh request without a cookie")
  public void refreshWithoutCookieTest() throws Exception {
    this.mockMvc.perform(post("/api/auth/refresh"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("logs out and the refresh token can no longer be used")
  public void logoutRevokesRefreshTokenTest() throws Exception {
    Cookie refreshCookie = registerAndGetRefreshCookie();

    this.mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie))
        .andExpect(status().isNoContent());

    this.mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("logout without a cookie is a no-op")
  public void logoutWithoutCookieTest() throws Exception {
    this.mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isNoContent());
  }

  private Cookie registerAndGetRefreshCookie() throws Exception {
    String content = readResourceAsString("/http-bodies/registerUser.json");
    MvcResult mvcResult = this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();
    return mvcResult.getResponse().getCookie("refreshToken");
  }
}
