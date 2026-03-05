package com.phumlanidev.cartservice.controller;

import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.service.impl.CartServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartServiceImpl cartService;

  @GetMapping
  public ResponseEntity<CartDto> getCart() {
    CartDto cart = cartService.getCartByUser();
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(cart);
  }

  @PostMapping("/add")
  public ResponseEntity<Void> addProductToCart(@Valid Long productId, Integer quantity) {
    cartService.addProductToCart(productId, quantity);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/item/{itemId}")
  public ResponseEntity<Void> updateQuantity(@Valid @PathVariable Long itemId, @RequestParam Integer quantity) {
    cartService.updateCartItemQuantity(itemId, quantity);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/item/{itemId}")
  public ResponseEntity<Void> removeCartItem(@Valid @PathVariable Long itemId) {
    cartService.removeCartItem(itemId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/clear")
  public ResponseEntity<Void> clearCart() {
    cartService.clearCart();
    return ResponseEntity.ok().build();
  }
}
