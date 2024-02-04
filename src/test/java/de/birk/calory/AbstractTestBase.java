package de.birk.calory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
public abstract class AbstractTestBase {

  @Autowired
  protected MockMvc mockMvc;

  public AbstractTestBase() {
  }
}
