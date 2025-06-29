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
  CartDto getCartByUser();

  /**
   * Comment: this is the placeholder for documentation.
   */
  void addProductToCart(Long productId, Integer quantity);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void removeCartItem(Long cartItemId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  Cart getOrCreateCart(String userId);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void clearCart();

  /**
   * Comment: this is the placeholder for documentation.
   */
  void updateCartItemQuantity(Long cartItemId, Integer quantity);

  /**
   * Comment: this is the placeholder for documentation.
   */
  void recalculateCartTotal(Cart cart);
}
