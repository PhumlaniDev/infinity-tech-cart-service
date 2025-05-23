package com.phumlanidev.cartservice.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
public class CartDto {

  private Long cartId;
  private String userId; // foreign key reference
  private BigDecimal totalPrice;
  private List<CartItemDto> cartItems;
}
