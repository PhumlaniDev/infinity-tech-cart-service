package com.phumlanidev.cartservice.controller;

import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.service.impl.CartServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Comment: this is the placeholder for documentation.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartServiceImpl cartService;

  /**
   * Comment: this is the placeholder for documentation.
   */
  @GetMapping
  public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal Jwt jwt) {
    String userId = getUserId(jwt);
    CartDto cart = cartService.getCartByUser(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(cart);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @PostMapping("/add")
  public ResponseEntity<Void> addProductToCart(@AuthenticationPrincipal Jwt jwt,
                                               Long productId, Integer quantity) {
    cartService.addProductToCart(getUserId(jwt), productId, quantity);
    return ResponseEntity.ok().build();
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @PatchMapping("/item/{itemId}")
  public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable Long itemId, Integer quantity) {
    cartService.updateCartItemQuantity(getUserId(jwt), itemId, quantity);
    return ResponseEntity.ok().build();
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @DeleteMapping("/item/{itemId}")
  public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable Long itemId) {
    cartService.removeCartItem(getUserId(jwt), itemId);
    return ResponseEntity.ok().build();
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @DeleteMapping("/clear")
  public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
    cartService.clearCart(getUserId(jwt));
    return ResponseEntity.ok().build();
  }

  private String getUserId(Jwt jwt) {
    return jwt.getSubject(); // Keycloak userId (UUID)
  }
}
