package de.birk.calory.adapter.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.ReadContext;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
public class FoodRestControllerTest extends AbstractTestBase {

  @Test
  @DisplayName("creates and gets a Food Item")
  public void createAndGetAllFoodTest() throws Exception {
    String content = readResourceAsString("/http-bodies/createFood.json");

    MvcResult mvcResult = mockMvc.perform(
        post("/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext context = asJson(mvcResult);
    String id = context.read("$.uuid");

    this.mockMvc.perform(
            get("/food", UUID.fromString(id))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].uuid").value(id))
        .andExpect(jsonPath("$.[0].name").value("food"))
        .andExpect(jsonPath("$.[0].calory").value(1312))
        .andExpect(jsonPath("$.[0].grams").value(100));
  }

  @Test
  @DisplayName("creates and gets a Food Item")
  public void createAndGetFoodTest() throws Exception {
    String content = readResourceAsString("/http-bodies/createFood.json");

    MvcResult mvcResult = mockMvc.perform(
        post("/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext context = asJson(mvcResult);
    String id = context.read("$.uuid");

    this.mockMvc.perform(
            get("/food/{id}", UUID.fromString(id))
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
    this.mockMvc.perform(
            get("/food/{id}", UUID.randomUUID())
        )
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Tries to get a non existing Fooditem")
  public void getUnvalidatedFoodAndCatchExceptionTest() throws Exception {
    String content = readResourceAsString("/http-bodies/createFood.json");

    content = content.replace("food", "");

    this.mockMvc.perform(
        post("/food")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andExpect(status().isNotAcceptable());
  }
}
