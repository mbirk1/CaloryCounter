package de.birk.calory.exception;

/**
 * Exception, that is thrown if a food item is deleted while it is still referenced by at
 * least one recipe.
 *
 * @author Marius Birk
 */
public class FoodInUseException extends RuntimeException {
}
