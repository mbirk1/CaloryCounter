package de.birk.calory.adapter;

import java.util.NoSuchElementException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import de.birk.calory.exception.EmailAlreadyInUseException;
import de.birk.calory.exception.InvalidCredentialsException;
import de.birk.calory.exception.InvalidTokenException;
import de.birk.calory.exception.TokenExpiredException;
import de.birk.calory.exception.ValidationException;


/**
 * Implementation of the ResponseEntityExceptionHandler.
 *
 * <p>Implements necessary methods to return http error codes to the client,
 * if any server errors happen.
 */
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
  protected ResponseEntity<Object> handleValidationException(
          RuntimeException e,
          WebRequest request
  ) {
    String body = "Validation failed.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.NOT_ACCEPTABLE,
        request
    );
  }

  @ExceptionHandler(value = {EmailAlreadyInUseException.class})
  protected ResponseEntity<Object> handleEmailAlreadyInUse(
      RuntimeException e,
      WebRequest request
  ) {
    String body = "Email is already in use.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.CONFLICT,
        request
    );
  }

  @ExceptionHandler(value = {InvalidCredentialsException.class})
  protected ResponseEntity<Object> handleInvalidCredentials(
      RuntimeException e,
      WebRequest request
  ) {
    String body = "Invalid credentials.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.UNAUTHORIZED,
        request
    );
  }

  @ExceptionHandler(value = {InvalidTokenException.class, TokenExpiredException.class})
  protected ResponseEntity<Object> handleInvalidToken(
      RuntimeException e,
      WebRequest request
  ) {
    String body = "Invalid or expired token.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.UNAUTHORIZED,
        request
    );
  }
}