package de.birk.calory.adapter;

import de.birk.calory.exception.ValidationException;

import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerImpl extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {NoSuchElementException.class})
  protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
    String bodyOfResponse = "Requested element not found.";
    return handleExceptionInternal(
        ex,
        bodyOfResponse,
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request
    );
  }

  @ExceptionHandler(value = {ValidationException.class})
  protected ResponseEntity<Object> handleValidationException(RuntimeException e, WebRequest request){
    String body = "Validation failed.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request
    );
  }
}