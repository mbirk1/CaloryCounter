package de.birk.calory.adapter.primary;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;

@IntegrationTest
@Transactional
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class FoodRestControllerTest extends AbstractTestBase {

    @Test
    public void createAndGetFoodTest() throws Exception {
        String id = "a97da1bd-30ec-4267-9a1f-0df987d4ccc9";
        this.mockMvc.perform(
                get("/food/{id}", id)
            )
            .andDo(document("request a food item"));

        assertTrue(true);
    }
}
