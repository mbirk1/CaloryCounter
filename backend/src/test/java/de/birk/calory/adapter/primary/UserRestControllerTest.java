package de.birk.calory.adapter.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
public class UserRestControllerTest extends AbstractTestBase {

  @Test
  @DisplayName("returns the current user with profileCompleted=false before onboarding")
  public void getCurrentUserBeforeOnboardingTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("auth-test-user@example.com"))
        .andExpect(jsonPath("$.profileCompleted").value(false));
  }

  @Test
  @DisplayName("rejects requests without a bearer token")
  public void getCurrentUserWithoutTokenTest() throws Exception {
    this.mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("rejects requests with a malformed bearer token")
  public void getCurrentUserWithInvalidTokenTest() throws Exception {
    this.mockMvc.perform(
        get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token")
    ).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("rejects requests with a non-bearer authorization scheme")
  public void getCurrentUserWithNonBearerSchemeTest() throws Exception {
    this.mockMvc.perform(
        get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
    ).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("does not require authentication for non-api paths")
  public void nonApiPathIsPubliclyAccessibleTest() throws Exception {
    this.mockMvc.perform(get("/some-client-route"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("completes onboarding and marks the profile as completed")
  public void updateProfileCompletesOnboardingTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/updateProfile.json");

    this.mockMvc.perform(
            put("/api/users/me/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.profileCompleted").value(true))
        .andExpect(jsonPath("$.activityLevel").value("VERY_ACTIVE"));

    this.mockMvc.perform(
            get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.profileCompleted").value(true));
  }

  @Test
  @DisplayName("rejects onboarding with an invalid activity level")
  public void updateProfileWithInvalidActivityLevelTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = "{\"height\":180,\"weight\":75,\"activityLevel\":\"NOT_A_LEVEL\"}";

    this.mockMvc.perform(
        put("/api/users/me/profile")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isNotAcceptable());
  }

  private String registerAndGetAccessToken() throws Exception {
    String content = readResourceAsString("/http-bodies/registerUser.json");
    MvcResult mvcResult = this.mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();
    return asJson(mvcResult).read("$.accessToken");
  }
}
