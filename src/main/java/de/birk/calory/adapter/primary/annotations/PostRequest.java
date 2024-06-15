package de.birk.calory.adapter.primary.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RequestMapping(method = RequestMethod.POST)
@ApiResponse(responseCode = "201", description = "Element successful created")
@ApiResponse(responseCode = "404", description = "A required Element was not found")
@ApiResponse(responseCode = "406", description = "Validation failed.")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostRequest {
}
