package com.phumlanidev.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
public class CartDto {

  @NotBlank(message = "Cart ID is required")
  private String userId; // foreign key reference
  @NotNull(message = "Total price is required")
  private BigDecimal totalPrice;
  @NotNull(message = "Total quantity is required")
  @JsonProperty("items") // Must match the JSON key from cart-service
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<CartItemDto> cartItems = new ArrayList<>();
}
