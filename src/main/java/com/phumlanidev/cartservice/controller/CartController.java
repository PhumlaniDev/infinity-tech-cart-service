package com.phumlanidev.cartservice.controller;

import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.service.impl.CartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartServiceImpl cartService;

  // Add methods to handle HTTP requests here, e.g., getCart, addProductToCart, removeCartItem, etc.
  @GetMapping
  public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal Jwt jwt){
    String userId = getUserId(jwt);
    CartDto cart = cartService .getCartByUser(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(cart);
  }

  @PostMapping("/add")
  public ResponseEntity<Void> addProductToCart(@AuthenticationPrincipal Jwt jwt, Long productId, BigDecimal quantity) {
    cartService.addProductToCart(getUserId(jwt), productId, quantity);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/item/{itemId}")
  public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable Long itemId, BigDecimal quantity) {
    cartService.updateCartItemQuantity(getUserId(jwt), itemId, quantity);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/item/{itemId}")
  public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId) {
    cartService.removeCartItem(getUserId(jwt), itemId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/clear")
  public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
    cartService.clearCart(getUserId(jwt));
    return ResponseEntity.ok().build();
  }

  private String getUserId(Jwt jwt) {
    return jwt.getClaimAsString("sub");
  }
}
