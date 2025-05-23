package com.phumlanidev.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public ProductNotFoundException(String message) {
    super(message);
  }
}
