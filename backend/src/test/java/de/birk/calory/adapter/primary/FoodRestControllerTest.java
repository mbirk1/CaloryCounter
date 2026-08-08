package de.birk.calory.adapter.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.ReadContext;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
public class FoodRestControllerTest extends AbstractTestBase {

  @Test
  @DisplayName("creates and gets a page of Food Items")
  public void createAndGetAllFoodTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    MvcResult mvcResult = mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext context = asJson(mvcResult);
    String id = context.read("$.uuid");

    this.mockMvc.perform(
            get("/api/food")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.uuid=='" + id + "')].name").value("food"))
        .andExpect(jsonPath("$.content[?(@.uuid=='" + id + "')].calory").value(1312))
        .andExpect(jsonPath("$.content[?(@.uuid=='" + id + "')].grams").value(100));
  }

  @Test
  @DisplayName("creates and gets a Food Item")
  public void createAndGetFoodTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    MvcResult mvcResult = mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext context = asJson(mvcResult);
    String id = context.read("$.uuid");

    this.mockMvc.perform(
            get("/api/food/{id}", UUID.fromString(id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value(id))
        .andExpect(jsonPath("$.name").value("food"))
        .andExpect(jsonPath("$.calory").value(1312))
        .andExpect(jsonPath("$.grams").value(100));
  }

  @Test
  @DisplayName("Tries to get a non existing Fooditem")
  public void getFoodAndCatchExceptionTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/food/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Tries to get a non existing Fooditem")
  public void getUnvalidatedFoodAndCatchExceptionTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    content = content.replace("food", "");

    this.mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isNotAcceptable());
  }

  @Test
  @DisplayName("creates and deletes a Food Item")
  public void createAndDeleteFoodTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    MvcResult mvcResult = mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext context = asJson(mvcResult);
    String id = context.read("$.uuid");

    this.mockMvc.perform(
            delete("/api/food/{id}", UUID.fromString(id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[?(@.uuid=='" + id + "')]").isEmpty());
  }

  @Test
  @DisplayName("tries to delete a non existing Fooditem")
  public void deleteNonExistingFoodAndCatchExceptionTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            delete("/api/food/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("rejects requests without a bearer token")
  public void getAllFoodsWithoutTokenTest() throws Exception {
    this.mockMvc.perform(get("/api/food"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("returns a page with the requested size and correct total counts")
  public void getAllFoodsWithCustomPageSizeTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    for (int i = 0; i < 3; i++) {
      mockMvc.perform(
          post("/api/food")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(content)
      ).andReturn();
    }

    this.mockMvc.perform(
            get("/api/food")
                .param("page", "0")
                .param("size", "2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.last").value(false));
  }

  @Test
  @DisplayName("returns the last page correctly")
  public void getAllFoodsReturnsLastPageTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    String content = readResourceAsString("/http-bodies/createFood.json");

    for (int i = 0; i < 3; i++) {
      mockMvc.perform(
          post("/api/food")
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(content)
      ).andReturn();
    }

    this.mockMvc.perform(
            get("/api/food")
                .param("page", "1")
                .param("size", "2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("defaults to page 0 with size 20 when no parameters are given")
  public void getAllFoodsUsesDefaultPageParametersTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/food")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  @DisplayName("search filters by a case-insensitive substring of the name")
  public void getAllFoodsFiltersByCaseInsensitiveSearchTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    createFood(accessToken, "Vollmilch", "0");
    createFood(accessToken, "Hafermilch", "0");
    createFood(accessToken, "Apfelsaft", "0");

    this.mockMvc.perform(
            get("/api/food")
                .param("search", "MILCH")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[?(@.name=='Vollmilch')]").exists())
        .andExpect(jsonPath("$.content[?(@.name=='Hafermilch')]").exists())
        .andExpect(jsonPath("$.content[?(@.name=='Apfelsaft')]").doesNotExist());
  }

  @Test
  @DisplayName("an empty search returns every food item again")
  public void getAllFoodsWithBlankSearchReturnsEverythingTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    createFood(accessToken, "Vollmilch", "0");
    createFood(accessToken, "Apfelsaft", "0");

    this.mockMvc.perform(
            get("/api/food")
                .param("search", "")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
  @DisplayName("diet filter only returns food items with a matching diet")
  public void getAllFoodsFiltersByDietTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    createFood(accessToken, "Tofu", "VEGAN");
    createFood(accessToken, "Kaese", "VEGETARIAN");
    createFood(accessToken, "Steak", "NON_VEGETARIAN");

    this.mockMvc.perform(
            get("/api/food")
                .param("diet", "VEGAN")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Tofu"))
        .andExpect(jsonPath("$.content[0].diet").value("VEGAN"));
  }

  @Test
  @DisplayName("search and diet filter combine with AND, not OR")
  public void getAllFoodsCombinesSearchAndDietFilterTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    createFood(accessToken, "Vegane Wurst", "VEGAN");
    createFood(accessToken, "Vegane Milch", "VEGAN");
    createFood(accessToken, "Fleischwurst", "NON_VEGETARIAN");

    this.mockMvc.perform(
            get("/api/food")
                .param("search", "wurst")
                .param("diet", "VEGAN")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Vegane Wurst"));
  }

  @Test
  @DisplayName("sorts by calory descending when requested")
  public void getAllFoodsSortsByCaloryDescendingTest() throws Exception {
    String accessToken = registerAndGetAccessToken();
    createFoodWithCalory(accessToken, "Low", 10);
    createFoodWithCalory(accessToken, "High", 900);
    createFoodWithCalory(accessToken, "Mid", 500);

    this.mockMvc.perform(
            get("/api/food")
                .param("sort", "calory")
                .param("direction", "desc")
                .param("size", "3")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("High"))
        .andExpect(jsonPath("$.content[1].name").value("Mid"))
        .andExpect(jsonPath("$.content[2].name").value("Low"));
  }

  @Test
  @DisplayName("rejects an unknown sort field with 400 instead of a 500")
  public void getAllFoodsRejectsAnInvalidSortFieldTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/food")
                .param("sort", "externalId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("rejects an unknown sort direction with 400 instead of a 500")
  public void getAllFoodsRejectsAnInvalidSortDirectionTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/food")
                .param("direction", "sideways")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("rejects an unknown diet filter with 400 instead of a 500")
  public void getAllFoodsRejectsAnInvalidDietFilterTest() throws Exception {
    String accessToken = registerAndGetAccessToken();

    this.mockMvc.perform(
            get("/api/food")
                .param("diet", "PESCATARIAN")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        )
        .andExpect(status().isBadRequest());
  }

  private void createFood(String accessToken, String name, String diet) throws Exception {
    String content = "{\"name\":\"" + name + "\",\"calory\":100,\"grams\":100,\"diet\":\""
        + diet + "\"}";
    mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();
  }

  private void createFoodWithCalory(String accessToken, String name, int calory)
      throws Exception {
    String content = "{\"name\":\"" + name + "\",\"calory\":" + calory + ",\"grams\":100}";
    mockMvc.perform(
        post("/api/food")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();
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
