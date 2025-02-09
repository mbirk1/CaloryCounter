package de.birk.calory.adapter.primary.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Default Get Request, that also defines which http response should be sent.
 *
 * @author Marius Birk
 */
@RequestMapping(method = RequestMethod.GET)
@Operation(summary = "Getting a resource")
@ApiResponse(responseCode = "200", description = "Element found")
@ApiResponse(responseCode = "404", description = "A required Element was not found")
@ApiResponse(responseCode = "406", description = "Validation failed.")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetRequest {

  /**
   * Parameter for the name.
   *
   * @return the name
   */
  String name() default "";

  /**
   * Parameter for the path.
   *
   * @return the path
   */
  @AliasFor("path")
  String[] value() default {};

  /**
   * Parameter for the value.
   *
   * @return the value
   */
  @AliasFor("value")
  String[] path() default {};
}
