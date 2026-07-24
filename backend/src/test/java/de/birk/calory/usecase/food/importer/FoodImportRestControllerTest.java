package de.birk.calory.usecase.food.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.ReadContext;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;
import de.birk.calory.adapter.secondary.FoodRepository;
import de.birk.calory.adapter.secondary.model.FoodPersistence;

/**
 * Integration test for the CSV import REST endpoints ({@code POST /api/food/import},
 * {@code GET /api/food/import/{jobId}}).
 *
 * <p>Lives in {@code usecase.food.importer} rather than {@code adapter.primary} (unlike the
 * other {@code *RestControllerTest} classes): it needs to inspect the database directly via
 * {@link FoodRepository} to verify what the asynchronous import actually persisted, and only the
 * usecase layer is allowed to depend on secondary adapters.
 *
 * @author Marius Birk
 */
@IntegrationTest
@Transactional
public class FoodImportRestControllerTest extends AbstractTestBase {

  // Matches src/test/resources/csv/sample-food-import.csv: the external ids of the six rows
  // that pass the quality filter and are expected to be imported.
  private static final List<String> IMPORTED_EXTERNAL_IDS =
      List.of("0001", "0002", "0006", "0007", "0009", "0010");

  @Autowired
  private FoodRepository foodRepository;

  // The import runs in a background thread via @Async, so its writes commit in their own
  // transaction and are never rolled back by this test's @Transactional. Cleaning them up has
  // to happen off the test thread too - anything run directly here would just join (and later
  // be rolled back with) the test's own transaction instead of durably deleting the rows.
  @AfterEach
  public void cleanUpImportedRows() throws Exception {
    CompletableFuture.runAsync(() -> {
      for (String externalId : IMPORTED_EXTERNAL_IDS) {
        this.foodRepository.findByExternalId(externalId).ifPresent(this.foodRepository::delete);
      }
    }).get(10, TimeUnit.SECONDS);
  }

  @Test
  @DisplayName("imports a CSV file asynchronously and reports correct counters")
  public void importsSampleCsvFileTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    byte[] csvBytes = readResourceAsStream("/csv/sample-food-import.csv").readAllBytes();
    MockMultipartFile file =
        new MockMultipartFile("file", "sample-food-import.csv", "text/csv", csvBytes);

    MvcResult startResult = this.mockMvc.perform(
        multipart("/api/food/import")
            .file(file)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
    ).andExpect(status().isAccepted()).andReturn();

    String jobId = asJson(startResult).read("$.jobId");
    ReadContext finalStatus = pollUntilFinished(jobId, accessToken);

    assertThat((String) finalStatus.read("$.state")).isEqualTo("COMPLETED");
    assertThat((Integer) finalStatus.read("$.processedRows")).isEqualTo(10);
    assertThat((Integer) finalStatus.read("$.importedCount")).isEqualTo(6);
    assertThat((Integer) finalStatus.read("$.skippedCount")).isEqualTo(4);
    assertThat((Integer) finalStatus.read("$.errorCount")).isEqualTo(0);

    Optional<FoodPersistence> cola = this.foodRepository.findByExternalId("0001");
    assertThat(cola).isPresent();
    assertThat(cola.get().getName()).isEqualTo("Cola");
    assertThat(cola.get().getBrand()).isEqualTo("Acme Beverages");
    assertThat(cola.get().getSource()).isEqualTo("OPENFOODFACTS");
    assertThat(cola.get().getCalory()).isEqualByComparingTo("42.00");

    Optional<FoodPersistence> multiBrand = this.foodRepository.findByExternalId("0007");
    assertThat(multiBrand).isPresent();
    assertThat(multiBrand.get().getBrand()).isEqualTo("Snacky");

    assertThat(this.foodRepository.findByExternalId("0003")).isEmpty();
    assertThat(this.foodRepository.findByExternalId("0004")).isEmpty();
    assertThat(this.foodRepository.findByExternalId("0005")).isEmpty();
    assertThat(this.foodRepository.findByExternalId("0008")).isEmpty();
  }

  private ReadContext pollUntilFinished(String jobId, String accessToken) throws Exception {
    for (int attempt = 0; attempt < 50; attempt++) {
      MvcResult result = this.mockMvc.perform(
          get("/api/food/import/{jobId}", jobId)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
      ).andReturn();
      ReadContext context = asJson(result);
      String state = context.read("$.state");
      if (!"RUNNING".equals(state)) {
        return context;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("Import job did not finish within the expected time");
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
