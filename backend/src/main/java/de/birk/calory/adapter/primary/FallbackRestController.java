package de.birk.calory.adapter.primary;

import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.GetRequest;

/**
 * Fallback Controller to redirect to the index.html .
 */
@RestController
public class FallbackRestController {
  @GetRequest(value = "/{path:[^\\.]*}")
  public String redirect() {
    return "redirect:/index.html";
  }
}
