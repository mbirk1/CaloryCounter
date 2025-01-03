package de.birk.calory.adapter.primary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackRestController {
  @GetMapping(value = "/{path:[^\\.]*}")
  public String redirect() {
      return "forward:/index.html";
  }
}
