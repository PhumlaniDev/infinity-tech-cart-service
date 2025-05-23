package com.phumlanidev.cartservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
@Getter
public abstract class BaseException extends RuntimeException {

  private final HttpStatus status;

  /**
   * Comment: this is the placeholder for documentation.
   */
  protected BaseException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

}
