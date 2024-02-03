package de.birk.calory.adapter.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import de.birk.calory.AbstractTestBase;
import de.birk.calory.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
public class FoodRestControllerTest extends AbstractTestBase {

  @Test
  public void getFoodTest() throws Exception {
    String id = "a97da1bd-30ec-4267-9a1f-0df987d4ccc9";
    this.mockMvc.perform(
            get("/" + id)
        )
        .andDo(print());
  }
}
