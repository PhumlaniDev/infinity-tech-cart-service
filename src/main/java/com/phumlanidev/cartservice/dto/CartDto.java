package com.phumlanidev.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
public class CartDto {

  private String userId; // foreign key reference
  private BigDecimal totalPrice;

  @JsonProperty("items") // Must match the JSON key from cart-service
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<CartItemDto> cartItems = new ArrayList<>();
}
