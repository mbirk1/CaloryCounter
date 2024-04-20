package de.birk.calory.adapter.primary;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.ReadContext;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
@AutoConfigureRestDocs(outputDir = "doc/snippets/default")
public class RecipeRestControllerTest extends AbstractTestBase {

  @Test
  @DisplayName("create and get a Recipe")
  public void createGetRecipeTest() throws Exception {
    String content = readResourceAsString("/http-bodies/createRecipe.json");
    MvcResult mvcResult = mockMvc.perform(
        post("/recipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
    ).andReturn();

    ReadContext readContext = asJson(mvcResult);
    String id = readContext.read("$.id");

    //this.mockMvc.perform(
    //        get("/recipe/{id}", UUID.fromString(id))
    //    )
    //    .andExpect(status().isOk())
    //    .andDo(print())
    //    //.andExpect(jsonPath("$.uuid").value(id))
    //    //.andExpect(jsonPath("$.name").value("food"))
    //    //.andExpect(jsonPath("$.calory").value(1312))
    //    //.andExpect(jsonPath("$.grams").value(100))
    //    .andDo(document("request a recipe"));
  }
}
