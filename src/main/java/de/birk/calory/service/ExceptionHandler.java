package de.birk.calory.service;

import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

public class ExceptionHandler extends ExceptionHandlerMethodResolver {
  /**
   * A constructor that finds {@link ExceptionHandler} methods in the given type.
   *
   * @param handlerType the type to introspect
   */
  public ExceptionHandler(Class<?> handlerType) {
    super(handlerType);
  }
}
