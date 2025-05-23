package com.phumlanidev.cartservice.mapper;

import com.phumlanidev.cartservice.dto.CartItemDto;
import com.phumlanidev.cartservice.model.CartItem;
import org.springframework.stereotype.Component;

/**
 * Comment: this is the placeholder for documentation.
 */
@Component
public class CartItemMapper {
  
  /**
   * Comment: this is the placeholder for documentation.
   */
  public CartItem toEntity(CartItem cartItem, CartItemDto cartItemDto) {
    cartItem.setCart(cartItemDto.getCart());
    cartItem.setProductId(cartItemDto.getProductId());
    cartItem.setQuantity(cartItemDto.getQuantity());
    cartItem.setPrice(cartItemDto.getPrice());
    return cartItem;
  }
  
  /**
   * Comment: this is the placeholder for documentation.
   */
  public CartItemDto toDto(CartItem cartItem, CartItemDto cartItemDto) {
    cartItemDto.setCart(cartItem.getCart());
    cartItemDto.setProductId(cartItem.getProductId());
    cartItemDto.setQuantity(cartItem.getQuantity());
    cartItemDto.setPrice(cartItemDto.getPrice());
    return cartItemDto;
  }
}
