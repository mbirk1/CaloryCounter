package de.birk.calory.exception;

/**
 * Exception, that is thrown if a client-supplied sort field, sort direction, or diet filter
 * value does not match one of the server's allowed values - these are only ever validated
 * against a fixed positive list, never passed straight through to {@code Sort.by(...)} or
 * {@code Enum.valueOf(...)}, to avoid an unvalidated client input causing a 500 error.
 *
 * @author Marius Birk
 */
public class InvalidSortParameterException extends RuntimeException {

  public InvalidSortParameterException(String message) {
    super(message);
  }
}
