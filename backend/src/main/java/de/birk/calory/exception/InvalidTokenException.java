package de.birk.calory.exception;

/**
 * Exception, that is thrown if an access or refresh token is malformed,
 * has an invalid signature, is unknown or has already been used
 * (refresh token reuse detection).
 *
 * @author Marius Birk
 */
public class InvalidTokenException extends RuntimeException {
}
