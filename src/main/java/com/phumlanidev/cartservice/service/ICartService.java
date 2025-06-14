package com.phumlanidev.cartservice.service;


import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.model.Cart;

/**
 * Comment: this is the placeholder for documentation.
 */
public interface ICartService {

  /**
   * Comment: this is the placeholder for documentation.
   */
  CartDto getCartByUser(String userId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void addProductToCart(String userId, Long productId, Integer quantity);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void removeCartItem(String userId, Long cartItemId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  Cart getOrCreateCart(String userId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void clearCart(String userId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void updateCartItemQuantity(String userId, Long cartItemId, Integer quantity);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void recalculateCartTotal(Cart cart);
}
