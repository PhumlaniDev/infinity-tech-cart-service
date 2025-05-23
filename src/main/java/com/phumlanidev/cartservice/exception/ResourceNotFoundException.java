package com.phumlanidev.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
    super("%s not found with the given input data %s: %s"
            .formatted(resourceName, fieldName, fieldValue));
  }
}
