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
import de.birk.calory.exception.FoodInUseException;
import de.birk.calory.exception.InvalidCredentialsException;
import de.birk.calory.exception.InvalidSortParameterException;
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

  // "Food not found" is the one message DeleteFoodUsecase deliberately passes to
  // NoSuchElementException, precisely so this handler can give this specific, newly
  // user-facing case a clear German message - every other .orElseThrow() call site in the
  // codebase throws with no message and keeps getting the generic English fallback below,
  // unchanged, since that text isn't shown to end users anywhere else today.
  private static final String FOOD_NOT_FOUND_MESSAGE = "Food not found";

  @ExceptionHandler(value = {NoSuchElementException.class})
  protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
    String bodyOfResponse = FOOD_NOT_FOUND_MESSAGE.equals(ex.getMessage())
        ? "Dieses Lebensmittel wurde bereits gelöscht."
        : "Requested element not found.";
    return handleExceptionInternal(
        ex,
        bodyOfResponse,
        new HttpHeaders(),
        HttpStatus.NOT_FOUND,
        request
    );
  }

  @ExceptionHandler(value = {FoodInUseException.class})
  protected ResponseEntity<Object> handleFoodInUse(RuntimeException e, WebRequest request) {
    String body = "Dieses Lebensmittel ist Teil eines Rezepts und kann nicht gelöscht werden.";
    return handleExceptionInternal(
        e,
        body,
        new HttpHeaders(),
        HttpStatus.CONFLICT,
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

  @ExceptionHandler(value = {InvalidSortParameterException.class})
  protected ResponseEntity<Object> handleInvalidSortParameter(
      RuntimeException e,
      WebRequest request
  ) {
    return handleExceptionInternal(
        e,
        e.getMessage(),
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
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