package com.phumlanidev.cartservice.dto;


import com.phumlanidev.cartservice.model.Cart;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
public class CartItemDto {

  private Cart cart; // foreign key reference
  private Long productId; // foreign key reference
  private BigDecimal quantity;
  private BigDecimal price;
}
