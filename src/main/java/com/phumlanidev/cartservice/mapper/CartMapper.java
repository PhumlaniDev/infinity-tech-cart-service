package com.phumlanidev.cartservice.mapper;


import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.dto.CartItemDto;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Comment: this is the placeholder for documentation.
 */
@Component
@RequiredArgsConstructor
public class CartMapper {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public Cart toEntity(CartDto cartDto, Cart cart) {
    cart.setUserId(cartDto.getUserId());
    cart.setTotalPrice(cartDto.getTotalPrice());
    cart.setItems(cartDto.getCartItems().stream()
            .map(item -> {
              CartItem i = new CartItem();
              i.setProductId(item.getProductId());
              i.setQuantity(item.getQuantity());
              i.setPrice(item.getPrice());
              return i;
            })
            .collect(Collectors.toList()));
    return cart;
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  public CartDto toDto(Cart cart, CartDto dto) {
    dto.setUserId(cart.getUserId());
    dto.setTotalPrice(cart.getTotalPrice());
    dto.setCartItems(cart.getItems().stream()
            .map(item -> {
              CartItemDto i = new CartItemDto();
              i.setProductId(item.getProductId());
              i.setQuantity(item.getQuantity());
              i.setPrice(item.getPrice());
              return i;
            })
            .collect(Collectors.toList()));
    return dto;
  }

}
