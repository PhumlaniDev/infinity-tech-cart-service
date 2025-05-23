package com.phumlanidev.cartservice.exception.cart;


import com.phumlanidev.cartservice.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
public class CartItemNotFoundException extends BaseException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public CartItemNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
