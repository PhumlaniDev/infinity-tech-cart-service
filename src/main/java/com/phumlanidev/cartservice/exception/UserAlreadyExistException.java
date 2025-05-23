package com.phumlanidev.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserAlreadyExistException extends RuntimeException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public UserAlreadyExistException(String message) {
    super(message);
  }
}
