package com.phumlanidev.cartservice.service.impl;

import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.mapper.CartMapper;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import com.phumlanidev.cartservice.repository.CartRepository;
import com.phumlanidev.cartservice.service.ICartService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Comment: this is the placeholder for documentation.
 */

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

  private final CartRepository cartRepository;
  private final CartMapper cartMapper;
  private final HttpServletRequest request;
  private final AuditLogServiceImpl auditLogService;
  private final ProductServiceImpl productService;


  @Override
  public CartDto getCartByUser(String userId) {
    Cart cart = getOrCreateCart(userId);
    return cartMapper.toDto(cart, new CartDto());
  }

  @Override
  public void addProductToCart(String userId, Long productId, BigDecimal quantity) {
    Cart cart = getOrCreateCart(userId);

    // Fetch product price from product service
    BigDecimal productPrice = productService.getProductPriceById(productId);

    CartItem item = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElse(CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .quantity(BigDecimal.ZERO)
                    .price(productPrice).build());

    item.setQuantity(item.getQuantity().add(quantity));
    item.setPrice(productPrice);
    cart.getItems().add(item);
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";

    auditLogService.log(
            "PRODUCT_CART_ADDED",
            String.valueOf(userId),
            username,
            clientIp,
            "Product added to cart: " + productId);
  }

  @Override
  public void removeCartItem(String userId, Long cartItemId) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().removeIf(item -> item.getCartItemsId().equals(cartItemId));
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";



    auditLogService.log(
            "REMOVE_PRODUCT_CART",
            String.valueOf(userId),
            username,
            clientIp,
            "Product removed from cart: " + cartItemId);
  }

  @Override
  public Cart getOrCreateCart(String userId) {
    Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(Cart.builder()
                    .userId(userId)
                    .totalPrice(BigDecimal.ZERO)
                    .items(new ArrayList<>())
                    .build()));

    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";

    auditLogService.log(
            "CART_CREATED",
            String.valueOf(userId),
            username,
            clientIp,
            "Cart created for user: " + userId);

    return cart;
  }

  @Override
  public void clearCart(String userId) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().clear();
    cart.setTotalPrice(BigDecimal.ZERO);
    cartRepository.save(cart);

    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";

    auditLogService.log(
            "CART_CLEARED",
            String.valueOf(userId),
            username,
            clientIp,
            "Cart cleared for user: " + userId);
  }

  @Override
  public void updateCartItemQuantity(String userId, Long cartItemId, BigDecimal quantity) {
    Cart cart = getOrCreateCart(userId);
    cart.getItems().stream()
            .filter(item -> item.getCartItemsId().equals(cartItemId))
            .findFirst()
            .ifPresent(item -> item.setQuantity(quantity));
    recalculateCartTotal(cart);
    cartRepository.save(cart);

    String clientIp = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth != null ? auth.getName() : "anonymous";

    auditLogService.log(
            "UPDATE_CART_ITEM",
            String.valueOf(userId),
            username,
            clientIp,
            "Cart item updated: " + cartItemId);
  }

  @Override
  public void recalculateCartTotal(Cart cart) {
    BigDecimal total = cart.getItems().stream()
            .map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalPrice(total);
  }
}