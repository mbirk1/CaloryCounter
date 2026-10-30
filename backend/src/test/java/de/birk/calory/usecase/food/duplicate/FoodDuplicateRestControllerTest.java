package de.birk.calory.usecase.food.duplicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;
import de.birk.calory.adapter.secondary.DuplicateClusterRepository;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.DuplicateClusterMemberPersistence;
import de.birk.calory.adapter.secondary.model.DuplicateClusterPersistence;

/**
 * Integration test for the duplicate-detection review workflow REST endpoints
 * ({@code /api/food/duplicates/**}): analyze, poll, list, merge and reject.
 *
 * <p>Lives in {@code usecase.food.duplicate} rather than {@code adapter.primary} (unlike most
 * other {@code *RestControllerTest} classes, but matching {@code FoodImportRestControllerTest}):
 * it needs to inspect the database directly via {@link FoodRepository} and
 * {@link DuplicateClusterRepository} to clean up what the asynchronous analysis actually
 * persisted, and only the usecase layer is allowed to depend on secondary adapters.
 *
 * <p>Deliberately NOT {@code @Transactional}, for the same reason: the analysis runs in a
 * background thread via {@code @Async}, on its own database connection, and would never see the
 * foods created earlier in the same test if those inserts were still sitting inside an
 * uncommitted, test-scoped transaction. Every request therefore commits for real, and created
 * rows are cleaned up explicitly in {@link #cleanUp()} instead.
 *
 * @author Marius Birk
 */
@IntegrationTest
public class FoodDuplicateRestControllerTest extends AbstractTestBase {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final List<UUID> createdFoodIds = new ArrayList<>();

  @Autowired
  private FoodRepository foodRepository;

  @Autowired
  private DuplicateClusterRepository duplicateClusterRepository;

  @AfterEach
  public void cleanUp() throws Exception {
    CompletableFuture.runAsync(() -> {
      for (DuplicateClusterPersistence cluster : this.duplicateClusterRepository.findAll()) {
        boolean touchesCreatedFood = cluster.getMembers().stream()
            .map(DuplicateClusterMemberPersistence::getFood)
            .anyMatch(food -> this.createdFoodIds.contains(food.getId()));
        if (touchesCreatedFood) {
          this.duplicateClusterRepository.delete(cluster);
        }
      }
      for (UUID foodId : this.createdFoodIds) {
        this.foodRepository.findById(foodId).ifPresent(this.foodRepository::delete);
      }
    }).get(10, TimeUnit.SECONDS);
  }

  @Test
  @DisplayName("analyzes, lists and merges a proposed duplicate cluster")
  public void analyzeAndMergeClusterTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String canonicalId = createFood(accessToken, "Apfel", 52, 0.2);
    String duplicateId = createFood(accessToken, "Apfel rot", 52, 0.2);

    ReadContext finalStatus = analyzeAndPollUntilFinished(accessToken, "MANUAL");
    assertThat((String) finalStatus.read("$.state")).isEqualTo("COMPLETED");

    Map<String, Object> cluster = findClusterContaining(accessToken, canonicalId, duplicateId);
    String clusterId = (String) cluster.get("id");

    String mergeBody = objectMapper.writeValueAsString(Map.of("canonicalFoodId", canonicalId));
    this.mockMvc.perform(
        post("/api/food/duplicates/{clusterId}/merge", clusterId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mergeBody)
    ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));

    this.mockMvc.perform(
        get("/api/food/{id}", duplicateId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isNotFound());
    this.mockMvc.perform(
        get("/api/food/{id}", canonicalId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isOk());
  }

  @Test
  @DisplayName("rejects a cluster and excludes it from the next analysis run")
  public void rejectClusterExcludesItFromReanalysisTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String foodOne = createFood(accessToken, "Birne", 57, 0.1);
    String foodTwo = createFood(accessToken, "Birne Williams", 57, 0.1);

    analyzeAndPollUntilFinished(accessToken, "MANUAL");
    Map<String, Object> cluster = findClusterContaining(accessToken, foodOne, foodTwo);
    String clusterId = (String) cluster.get("id");

    this.mockMvc.perform(
        post("/api/food/duplicates/{clusterId}/reject", clusterId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));

    analyzeAndPollUntilFinished(accessToken, "MANUAL");
    boolean reappeared = pendingClusters(accessToken).stream()
        .anyMatch(c -> containsBothFoods(members(c), foodOne, foodTwo));
    assertThat(reappeared).isFalse();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> findClusterContaining(String accessToken, String idA, String idB)
      throws Exception {
    return pendingClusters(accessToken).stream()
        .filter(c -> containsBothFoods(members(c), idA, idB))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected a cluster containing both foods"));
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> pendingClusters(String accessToken) throws Exception {
    MvcResult result = this.mockMvc.perform(
        get("/api/food/duplicates").param("status", "PENDING")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isOk()).andReturn();
    return asJson(result).read("$.content");
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> members(Map<String, Object> cluster) {
    return (List<Map<String, Object>>) cluster.get("members");
  }

  @SuppressWarnings("unchecked")
  private boolean containsBothFoods(List<Map<String, Object>> members, String idA, String idB) {
    boolean hasA = members.stream()
        .anyMatch(m -> idA.equals(((Map<String, Object>) m.get("food")).get("uuid")));
    boolean hasB = members.stream()
        .anyMatch(m -> idB.equals(((Map<String, Object>) m.get("food")).get("uuid")));
    return hasA && hasB;
  }

  private String createFood(String accessToken, String name, double calory, double fat)
      throws Exception {
    String body = objectMapper.writeValueAsString(
        Map.of("name", name, "calory", calory, "grams", 100, "fat", fat));
    MvcResult result = this.mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
    ).andReturn();
    String id = asJson(result).read("$.uuid");
    this.createdFoodIds.add(UUID.fromString(id));
    return id;
  }

  private ReadContext analyzeAndPollUntilFinished(String accessToken, String source)
      throws Exception {
    MvcResult startResult = this.mockMvc.perform(
        post("/api/food/duplicates/analyze").param("source", source)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isAccepted()).andReturn();
    String jobId = asJson(startResult).read("$.jobId");

    for (int attempt = 0; attempt < 50; attempt++) {
      MvcResult result = this.mockMvc.perform(
          get("/api/food/duplicates/analyze/{jobId}", jobId)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
      ).andReturn();
      ReadContext context = asJson(result);
      String state = context.read("$.state");
      if (!"RUNNING".equals(state)) {
        return context;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Analysis job did not finish within the expected time");
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
