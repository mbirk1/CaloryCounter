package de.birk.calory.exception;

/**
 * Exception, that is thrown if an access or refresh token is well-formed
 * and known, but has expired.
 *
 * @author Marius Birk
 */
public class TokenExpiredException extends RuntimeException {
}
