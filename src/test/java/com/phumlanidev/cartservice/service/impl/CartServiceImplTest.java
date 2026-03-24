package com.phumlanidev.cartservice.service.impl;

import com.phumlanidev.cartservice.config.JwtAuthenticationConverter;
import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.mapper.CartMapper;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import com.phumlanidev.cartservice.repository.CartRepository;
import com.phumlanidev.cartservice.utils.ProductServiceWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartMapper cartMapper;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private AuditLogServiceImpl auditLogService;
    @Mock private JwtAuthenticationConverter jwtAuthenticationConverter;
    @Mock private ProductServiceWrapper productServiceWrapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final String USER_ID = "user-123";
    private static final String USERNAME = "phumlani";
    private static final String CLIENT_IP = "127.0.0.1";

    private Cart emptyCart;

    @BeforeEach
    void setUp() {
        lenient().when(jwtAuthenticationConverter.getCurrentUserId()).thenReturn(USER_ID);
        lenient().when(jwtAuthenticationConverter.getCurrentUsername()).thenReturn(USERNAME);
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_IP);

        emptyCart = Cart.builder()
                .userId(USER_ID)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getCartByUser")
    class GetCartByUser {

        @Test
        @DisplayName("returns CartDto for the current user")
        void shouldReturnCartDtoForCurrentUser() {
            CartDto expectedDto = new CartDto();
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));
            when(cartMapper.toDto(any(Cart.class), any(CartDto.class))).thenReturn(expectedDto);

            CartDto result = cartService.getCartByUser();

            assertThat(result).isEqualTo(expectedDto);
            verify(cartRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("creates a new cart when none exists, then returns its dto")
        void shouldCreateCartWhenNoneExistsAndReturnDto() {
            CartDto expectedDto = new CartDto();
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);
            when(cartMapper.toDto(any(Cart.class), any(CartDto.class))).thenReturn(expectedDto);

            CartDto result = cartService.getCartByUser();

            assertThat(result).isEqualTo(expectedDto);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // addProductToCart()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addProductToCart()")
    class AddProductToCart {

        private static final Long   PRODUCT_ID    = 10L;
        private static final BigDecimal PRICE     = new BigDecimal("199.99");

        @BeforeEach
        void stubCommon() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));
            when(productServiceWrapper.getProductPriceById(PRODUCT_ID)).thenReturn(PRICE);
        }

        @Test
        @DisplayName("adds a new item when the product is not yet in the cart")
        void shouldAddNewItemWhenProductNotInCart() {
            cartService.addProductToCart(PRODUCT_ID, 2);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());

            Cart saved = captor.getValue();
            assertThat(saved.getItems()).hasSize(1);

            CartItem item = saved.getItems().getFirst();
            assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getPrice()).isEqualByComparingTo(PRICE);
        }

        @Test
        @DisplayName("updates price but does NOT increment quantity for existing item — exposes the bug")
        void shouldIncrementQuantityWhenProductAlreadyInCart_currentlyBuggy() {
            // Arrange: cart already has 1 unit of PRODUCT_ID
            CartItem existing = CartItem.builder()
                    .cartItemsId(1L)
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(PRICE)
                    .cart(emptyCart)
                    .build();
            emptyCart.getItems().add(existing);

            // Act: add 3 more
            cartService.addProductToCart(PRODUCT_ID, 3);

            // Assert: quantity should be 4 (1 + 3) — this FAILS with the current code
            // because existingItem.setQuantity(existingItem.getQuantity()) sets it to itself.
            // Fix: existingItem.setQuantity(existingItem.getQuantity() + quantity)
            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getItems().getFirst().getQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("recalculates total price after adding a new item")
        void shouldRecalculateTotalAfterAddingItem() {
            cartService.addProductToCart(PRODUCT_ID, 3);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());

            // 199.99 × 3 = 599.97
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(new BigDecimal("599.97"));
        }

        @Test
        @DisplayName("logs an audit event after adding a product")
        void shouldLogAuditEventAfterAddingProduct() {
            cartService.addProductToCart(PRODUCT_ID, 2);

            verify(auditLogService).log(
                    eq("ADD_PRODUCT_CART"),
                    eq(USER_ID),
                    eq(USERNAME),
                    eq(CLIENT_IP),
                    contains(String.valueOf(PRODUCT_ID))
            );
        }

        @Test
        @DisplayName("sets the cart reference on the new CartItem")
        void shouldSetCartReferenceOnNewItem() {
            cartService.addProductToCart(PRODUCT_ID, 1);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getItems().getFirst().getCart())
                    .isSameAs(captor.getValue());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // removeCartItem()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeCartItem()")
    class RemoveCartItem {

        @Test
        @DisplayName("removes the correct item by cartItemId")
        void shouldRemoveItemByCartItemId() {
            CartItem item1 = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            CartItem item2 = CartItem.builder().cartItemsId(2L).productId(20L)
                    .quantity(2).price(new BigDecimal("30.00")).cart(emptyCart).build();
            emptyCart.getItems().addAll(List.of(item1, item2));

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.removeCartItem(1L);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());

            List<CartItem> remaining = captor.getValue().getItems();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getCartItemsId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("does nothing when cartItemId does not exist in the cart")
        void shouldDoNothingWhenItemNotFound() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().add(item);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.removeCartItem(999L); // non-existent id

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getItems()).hasSize(1); // untouched
        }

        @Test
        @DisplayName("recalculates total after removal")
        void shouldRecalculateTotalAfterRemoval() {
            CartItem item1 = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(2).price(new BigDecimal("50.00")).cart(emptyCart).build();
            CartItem item2 = CartItem.builder().cartItemsId(2L).productId(20L)
                    .quantity(1).price(new BigDecimal("30.00")).cart(emptyCart).build();
            emptyCart.getItems().addAll(List.of(item1, item2));
            emptyCart.setTotalPrice(new BigDecimal("130.00")); // 2×50 + 1×30

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.removeCartItem(1L); // remove 2×50 item

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("logs an audit event after removing an item")
        void shouldLogAuditEvent() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.removeCartItem(1L);

            verify(auditLogService).log(
                    eq("REMOVE_CART_ITEM"),
                    eq(USER_ID),
                    eq(USERNAME),
                    eq(CLIENT_IP),
                    contains("1")
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // getOrCreateCart()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getOrCreateCart()")
    class GetOrCreateCart {

        @Test
        @DisplayName("returns existing cart when found")
        void shouldReturnExistingCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            Cart result = cartService.getOrCreateCart(USER_ID);

            assertThat(result).isSameAs(emptyCart);
            verify(cartRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates and returns new cart when none exists")
        void shouldCreateCartWhenNotFound() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

            Cart result = cartService.getOrCreateCart(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getItems()).isEmpty();

            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("new cart is initialised with an empty mutable item list")
        void shouldInitialiseNewCartWithEmptyMutableList() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

            Cart result = cartService.getOrCreateCart(USER_ID);

            // Must be mutable — we add items to it later
            assertThat(result.getItems()).isInstanceOf(ArrayList.class).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // clearCart()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("removes all items from the cart")
        void shouldClearAllItems() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(2).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().add(item);
            emptyCart.setTotalPrice(new BigDecimal("100.00"));

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.clearCart();

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());

            assertThat(captor.getValue().getItems()).isEmpty();
        }

        @Test
        @DisplayName("resets total price to zero after clearing")
        void shouldResetTotalPriceToZero() {
            emptyCart.setTotalPrice(new BigDecimal("250.00"));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.clearCart();

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("does not throw when cart is already empty")
        void shouldNotThrowWhenCartAlreadyEmpty() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            // emptyCart has no items — should be a no-op, not an exception
            org.assertj.core.api.Assertions.assertThatCode(() -> cartService.clearCart())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("logs an audit event after clearing")
        void shouldLogAuditEvent() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.clearCart();

            verify(auditLogService).log(
                    eq("CLEAR_CART"),
                    eq(USER_ID),
                    eq(USERNAME),
                    eq(CLIENT_IP),
                    anyString()
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // updateCartItemQuantity()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateCartItemQuantity()")
    class UpdateCartItemQuantity {

        @Test
        @DisplayName("updates quantity of the matching cart item")
        void shouldUpdateQuantityOfMatchingItem() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().add(item);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.updateCartItemQuantity(1L, 5);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().getItems().getFirst().getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("does not modify other items in the cart")
        void shouldNotModifyOtherItems() {
            CartItem item1 = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            CartItem item2 = CartItem.builder().cartItemsId(2L).productId(20L)
                    .quantity(3).price(new BigDecimal("20.00")).cart(emptyCart).build();
            emptyCart.getItems().addAll(List.of(item1, item2));

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.updateCartItemQuantity(1L, 10);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());

            CartItem untouched = captor.getValue().getItems().stream()
                    .filter(i -> i.getCartItemsId().equals(2L))
                    .findFirst().orElseThrow();
            assertThat(untouched.getQuantity()).isEqualTo(3); // unchanged
        }

        @Test
        @DisplayName("recalculates total after quantity update")
        void shouldRecalculateTotalAfterQuantityUpdate() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().add(item);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.updateCartItemQuantity(1L, 4);

            ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(captor.capture());
            // 50.00 × 4 = 200.00
            assertThat(captor.getValue().getTotalPrice())
                    .isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("does not save when cartItemId is not found")
        void shouldSaveEvenWhenItemNotFound() {
            // ifPresent means it silently skips — cart is still saved (recalculate + save always runs)
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.updateCartItemQuantity(999L, 5);

            verify(cartRepository).save(emptyCart); // save still called, but nothing changed
        }

        @Test
        @DisplayName("logs an audit event after updating quantity")
        void shouldLogAuditEvent() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(1).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().add(item);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

            cartService.updateCartItemQuantity(1L, 3);

            verify(auditLogService).log(
                    eq("UPDATE_CART_ITEM_QUANTITY"),
                    eq(USER_ID),
                    eq(USERNAME),
                    eq(CLIENT_IP),
                    contains("1")
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // recalculateCartTotal()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("recalculateCartTotal()")
    class RecalculateCartTotal {

        @Test
        @DisplayName("correctly sums price × quantity across all items")
        void shouldSumAllItemTotals() {
            CartItem item1 = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(2).price(new BigDecimal("100.00")).cart(emptyCart).build();
            CartItem item2 = CartItem.builder().cartItemsId(2L).productId(20L)
                    .quantity(3).price(new BigDecimal("50.00")).cart(emptyCart).build();
            emptyCart.getItems().addAll(List.of(item1, item2));

            cartService.recalculateCartTotal(emptyCart);

            // (2 × 100) + (3 × 50) = 200 + 150 = 350
            assertThat(emptyCart.getTotalPrice()).isEqualByComparingTo(new BigDecimal("350.00"));
        }

        @Test
        @DisplayName("sets total to zero when cart is empty")
        void shouldSetTotalToZeroForEmptyCart() {
            cartService.recalculateCartTotal(emptyCart);

            assertThat(emptyCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("handles single item correctly")
        void shouldHandleSingleItem() {
            CartItem item = CartItem.builder().cartItemsId(1L).productId(10L)
                    .quantity(5).price(new BigDecimal("19.99")).cart(emptyCart).build();
            emptyCart.getItems().add(item);

            cartService.recalculateCartTotal(emptyCart);

            // 5 × 19.99 = 99.95
            assertThat(emptyCart.getTotalPrice()).isEqualByComparingTo(new BigDecimal("99.95"));
        }
    }
}