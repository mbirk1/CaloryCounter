package de.birk.calory.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FoodNotExistException extends RuntimeException {

  public FoodNotExistException(String message) {
    super(message);
  }
}
