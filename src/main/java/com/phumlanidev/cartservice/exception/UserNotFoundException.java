package com.phumlanidev.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends BaseException {
  /**
   * Comment: this is the placeholder for documentation.
   */
  public UserNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }

}
