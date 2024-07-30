package de.birk.calory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

@SpringBootTest
public abstract class AbstractTestBase {

  @Autowired
  protected MockMvc mockMvc;

  public AbstractTestBase() {
  }

  protected InputStream readResourceAsStream(String file) {
    try {
      return new ClassPathResource(file).getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String readResourceAsString(String file) throws IOException {
    return StreamUtils.copyToString(readResourceAsStream(file), Charset.defaultCharset());
  }

  protected ReadContext asJson(MvcResult mockResult) {
    try {
      String content = mockResult.getResponse().getContentAsString();
      return JsonPath.parse(content);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
