package de.birk.calory.adapter.primary;

import de.birk.calory.adapter.primary.annotations.GetRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackRestController {
  @GetRequest(value = "/{path:[^\\.]*}")
  public String redirect() {
      return "forward:/index.html";
  }
}
