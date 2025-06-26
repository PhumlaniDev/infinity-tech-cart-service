package com.phumlanidev.cartservice.service.impl;

import com.phumlanidev.cartservice.config.JwtAuthenticationConverter;
import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.mapper.CartMapper;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import com.phumlanidev.cartservice.repository.CartRepository;
import com.phumlanidev.cartservice.service.ICartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Comment: this is the placeholder for documentation.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

  private final CartRepository cartRepository;
  private final CartMapper cartMapper;
  private final HttpServletRequest request;
  private final AuditLogServiceImpl auditLogService;
  private final ProductServiceImpl productService;
  private JwtAuthenticationConverter jwtAuthenticationConverter;


  @Override
  public CartDto getCartByUser(String userId) {
    Cart cart = getOrCreateCart(userId);
    log.info("🛒 Retrieved cart with {} items for user {}", cart.getItems().size(), userId);
    return cartMapper.toDto(cart, new CartDto());
  }

  @Override
  public void addProductToCart(String userId, Long productId, Integer quantity) {
    Cart cart = getOrCreateCart(userId);

    // Fetch product price from product service
    BigDecimal productPrice = productService.getProductPriceById(productId);

    Optional<CartItem> existingItemOpt = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst();

    if (existingItemOpt.isPresent()) {
      CartItem existingItem = existingItemOpt.get();
      existingItem.setQuantity(existingItem.getQuantity());
      existingItem.setPrice(productPrice);
    } else {
      CartItem newItem = CartItem.builder()
              .productId(productId)
              .quantity(quantity)
              .price(productPrice)
              .cart(cart) // Set the cart reference
              .build();
      cart.getItems().add(newItem);
    }
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    logAudit("ADD_PRODUCT_CART",
            "Product added to cart: " + productId + ", Quantity: " + quantity);
  }

  @Override
  public void removeCartItem(String userId, Long cartItemId) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().removeIf(item -> item.getCartItemsId().equals(cartItemId));
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    logAudit("REMOVE_CART_ITEM",
            "Cart item removed: " + cartItemId);
  }

  @Override
  public Cart getOrCreateCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(Cart.builder()
                    .userId(userId)
                    .totalPrice(BigDecimal.ZERO)
                    .items(new ArrayList<>())
                    .build()));

    logAudit("GET_OR_CREATE_CART",
            "Cart retrieved or created for user: " + userId);

    return cart;
  }

  @Override
  public void clearCart(String userId) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().clear();
    cart.setTotalPrice(BigDecimal.ZERO);
    cartRepository.save(cart);

    logAudit("CLEAR_CART",
            "Cart cleared for user: " + userId);
  }

  @Override
  public void updateCartItemQuantity(String userId, Long cartItemId, Integer quantity) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().stream()
            .filter(item -> item.getCartItemsId().equals(cartItemId))
            .findFirst()
            .ifPresent(item -> item.setQuantity(quantity));
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    logAudit("UPDATE_CART_ITEM_QUANTITY",
            "Cart item quantity updated: " + cartItemId + ", New Quantity: " + quantity);
  }

  @Override
  public void recalculateCartTotal(Cart cart) {
    BigDecimal total = cart.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalPrice(total);
  }

  private void logAudit(String action, String details) {
    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";
    Jwt jwt = jwtAuthenticationConverter.getJwt();
    String userId = jwtAuthenticationConverter.extractUserId(jwt);


    auditLogService.log(
            action,
            userId,
            username,
            clientIp,
            details
    );
  }
}