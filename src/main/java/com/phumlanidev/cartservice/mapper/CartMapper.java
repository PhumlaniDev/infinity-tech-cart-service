package com.phumlanidev.cartservice.mapper;


import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.dto.CartItemDto;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


/**
 * Comment: this is the placeholder for documentation.
 */
@Component
@RequiredArgsConstructor
public class CartMapper {

  private final CartItemMapper cartItemMapper;

  /**
   * Comment: this is the placeholder for documentation.
   */
  public Cart toEntity(CartDto cartDto, Cart cart) {
    cart.setCartId(cartDto.getCartId());
    cart.setUserId(cartDto.getUserId());
    cart.setTotalPrice(cartDto.getTotalPrice());
    cart.setItems(cartDto.getCartItems().stream()
            .map(cartItemDto -> cartItemMapper.toEntity(new CartItem(), cartItemDto))
            .collect(Collectors.toList()));
    return cart;
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  public CartDto toDto(Cart cart, CartDto cartDto) {
    cartDto.setCartId(cart.getCartId());
    cartDto.setUserId(cart.getUserId());
    cartDto.setTotalPrice(cart.getTotalPrice());
    cartDto.setCartItems(cart.getItems().stream()
            .map(cartItemDto -> cartItemMapper.toDto(cartItemDto, new CartItemDto()))
            .toList());
    return cartDto;
  }
}
