package com.phumlanidev.cartservice.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.phumlanidev.cartservice.config.JwtAuthenticationConverter;
import com.phumlanidev.cartservice.config.KeycloakTokenProvider;
import com.phumlanidev.cartservice.dto.CartDto;
import com.phumlanidev.cartservice.model.Cart;
import com.phumlanidev.cartservice.model.CartItem;
import com.phumlanidev.cartservice.repository.CartRepository;
import com.phumlanidev.cartservice.service.config.TestProductFeignClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;


@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestProductFeignClient.class)
public class CartServiceImplIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("cart_db_test")
          .withUsername("test")
          .withPassword("test");

  @Container
  static KafkaContainer kafka = new KafkaContainer(
          DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
  );

  @Container
  static GenericContainer<?> redis = new GenericContainer<>(
          DockerImageName.parse("redis:7.2-alpine")
  ).withExposedPorts(6379);

//  WireMock
  static WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());

  @BeforeAll
  static void startWireMock() {
    wireMock.start();
    WireMock.configureFor("localhost", wireMock.port());
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {

//    PostgreSQL
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

//    Kafka
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

//    Redis
    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", () -> redis.getMappedPort(6379));

//    Feign
    registry.add("spring.cloud.openfeign.client.config.product-service.url", () -> "http://localhost:" + wireMock.port());

//    Disable eureka
    registry.add("eureka.client.enabled", () -> "false");
    registry.add("eureka.client.register-with-eureka", () -> "false");
    registry.add("eureka.client.fetch-registry", () -> "false");
  }

  @MockitoBean
  private JwtAuthenticationConverter jwtAuthConverter;

  @MockitoBean
  private AuditLogServiceImpl auditLogService;

  @Autowired
  private CartServiceImpl cartService;

  @Autowired
  private CartRepository cartRepository;

  @MockitoBean  // ← add this
  private KeycloakTokenProvider keycloakTokenProvider;

  private static final String USER_ID = "user-integration-123";
  private static final String USERNAME = "phumlani";
  private static final String CLIENT_IP = "127.0.0.1";
  private static final Long PRODUCT_ID = 42L;
  private static final BigDecimal PRODUCT_PRICE = new BigDecimal("299.99");

  @BeforeEach
  void setUp() {
//    clean slate before every test
    cartRepository.deleteAll();
    wireMock.resetAll();

//    stub jwt converter
    lenient().when(jwtAuthConverter.getCurrentUserId()).thenReturn(USER_ID);
    lenient().when(jwtAuthConverter.getCurrentUsername()).thenReturn(USERNAME);

//    when(keycloakTokenProvider.getAccessToken(any(), any())).thenReturn("dummy-test-token");

//    stub product price
    wireMock.stubFor(get(urlPathEqualTo("/api/v1/products/" + PRODUCT_ID + "/price"))
//            .withHeader("Authorization", equalTo("Bearer dummy-test-token"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(PRODUCT_PRICE.toString())));
  }

  @Nested
  @DisplayName("getCartByUser()")
  class GetCartByUser {

    @Test
    @DisplayName("create a new cart in the DB when user has no existing cart")
    void shouldCreateNewCartInDatabaseWhenNoneExist() {
      CartDto result = cartService.getCartByUser();

      assertThat(result).isNotNull();


//      verify persisted in real db
      Optional<Cart> persisted = cartRepository.findByUserId(USER_ID);
      assertThat(persisted).isPresent();
      assertThat(persisted.get().getUserId()).isEqualTo(USER_ID);
      assertThat(persisted.get().getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(persisted.get().getItems()).isEmpty();
    }

    @Test
    @DisplayName("returns existing cart without creating a duplicate")
    void shouldReturnExistingCartWithoutCreatingDuplicate() {
      // Arrange: pre-seed a cart
      cartRepository.save(Cart.builder()
              .userId(USER_ID)
              .totalPrice(BigDecimal.ZERO)
              .items(new ArrayList<>())
              .build());

      cartService.getCartByUser();
      cartService.getCartByUser(); // call twice

      // Only one cart should exist
      assertThat(cartRepository.findAll())
              .filteredOn(c -> c.getUserId().equals(USER_ID))
              .hasSize(1);
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // addProductToCart()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("addProductToCart()")
  class AddProductToCart {

    @Test
    @DisplayName("persists a new CartItem in the database via real Feign+WireMock price fetch")
    void shouldPersistNewCartItemWithPriceFromProductService() {
      cartService.addProductToCart(PRODUCT_ID, 2);

      Cart saved = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(saved.getItems()).hasSize(1);

      CartItem item = saved.getItems().getFirst();
      assertThat(item.getProductId()).isEqualTo(PRODUCT_ID);
      assertThat(item.getQuantity()).isEqualTo(2);
      assertThat(item.getPrice()).isEqualByComparingTo(PRODUCT_PRICE);
    }

    @Test
    @DisplayName("recalculates and persists correct total price in the database")
    void shouldPersistCorrectTotalPrice() {
      cartService.addProductToCart(PRODUCT_ID, 3);

      Cart saved = cartRepository.findByUserId(USER_ID).orElseThrow();
      // 299.99 × 3 = 899.97
      assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("899.97"));
    }

    @Test
    @DisplayName("falls back to price 0 when product service is unavailable (circuit breaker)")
    void shouldFallbackToZeroPriceWhenProductServiceIsDown() {
      // Override WireMock to simulate product service being down
      wireMock.stubFor(get(urlPathEqualTo("/api/v1/products/" + PRODUCT_ID + "/price"))
              .willReturn(aResponse().withStatus(503)));

      cartService.addProductToCart(PRODUCT_ID, 1);

      Cart saved = cartRepository.findByUserId(USER_ID).orElseThrow();
      // Fallback method returns BigDecimal.ZERO
      assertThat(saved.getItems().getFirst().getPrice())
              .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("adds multiple different products and accumulates total correctly")
    void shouldAccumulateTotalAcrossMultipleProducts() {
      Long secondProductId = 99L;
      BigDecimal secondPrice = new BigDecimal("100.00");

      wireMock.stubFor(get(urlPathEqualTo("/api/v1/products/" + secondProductId + "/price"))
              .willReturn(aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                      .withBody(secondPrice.toString())));

      cartService.addProductToCart(PRODUCT_ID, 1);    // 299.99
      cartService.addProductToCart(secondProductId, 2); // 200.00

      Cart saved = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(saved.getItems()).hasSize(2);
      // 299.99 + 200.00 = 499.99
      assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("499.99"));
    }

    @Test
    @DisplayName("actually hits the WireMock product endpoint exactly once per addProductToCart call")
    void shouldCallProductServiceExactlyOnce() {
      cartService.addProductToCart(PRODUCT_ID, 1);

      wireMock.verify(1, getRequestedFor(
              urlPathEqualTo("/api/v1/products/" + PRODUCT_ID + "/price")));
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // removeCartItem()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("removeCartItem()")
  class RemoveCartItem {

    @Test
    @DisplayName("removes item from DB and recalculates total")
    void shouldRemoveItemFromDatabaseAndRecalculateTotal() {
      // Arrange: add two items via the service
      Long secondProductId = 99L;
      wireMock.stubFor(get(urlPathEqualTo("/api/v1/products/" + secondProductId + "/price"))
              .willReturn(aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                      .withBody("100.00")));

      cartService.addProductToCart(PRODUCT_ID, 1);      // 299.99
      cartService.addProductToCart(secondProductId, 2); // 200.00

      Cart cartBefore = cartRepository.findByUserId(USER_ID).orElseThrow();
      Long itemToRemoveId = cartBefore.getItems().stream()
              .filter(i -> i.getProductId().equals(PRODUCT_ID))
              .findFirst().orElseThrow().getCartItemsId();

      // Act
      cartService.removeCartItem(itemToRemoveId);

      // Assert
      Cart cartAfter = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(cartAfter.getItems()).hasSize(1);
      assertThat(cartAfter.getItems().getFirst().getProductId()).isEqualTo(secondProductId);
      // Only 200.00 remains
      assertThat(cartAfter.getTotalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("cart remains unchanged when a non-existent cartItemId is given")
    void shouldLeaveCartUnchangedWhenItemNotFound() {
      cartService.addProductToCart(PRODUCT_ID, 1);

      cartService.removeCartItem(99999L); // non-existent

      Cart cart = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(cart.getItems()).hasSize(1); // untouched
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // clearCart()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("clearCart()")
  class ClearCart {

    @Test
    @DisplayName("removes all items from the DB and sets total to zero")
    void shouldClearAllItemsFromDatabase() {
      cartService.addProductToCart(PRODUCT_ID, 2);

      cartService.clearCart();

      Cart cart = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(cart.getItems()).isEmpty();
      assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("does not throw and leaves total at zero when cart is already empty")
    void shouldHandleAlreadyEmptyCartGracefully() {
      // Force cart creation first
      cartService.getCartByUser();

      assertThatCode(() -> cartService.clearCart()).doesNotThrowAnyException();

      Cart cart = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // updateCartItemQuantity()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("updateCartItemQuantity()")
  class UpdateCartItemQuantity {

    @Test
    @DisplayName("updates item quantity and recalculates total in the database")
    void shouldUpdateQuantityAndRecalculateTotalInDatabase() {
      cartService.addProductToCart(PRODUCT_ID, 1); // 299.99

      Cart cartBefore = cartRepository.findByUserId(USER_ID).orElseThrow();
      Long cartItemId = cartBefore.getItems().getFirst().getCartItemsId();

      cartService.updateCartItemQuantity(cartItemId, 4);

      Cart cartAfter = cartRepository.findByUserId(USER_ID).orElseThrow();
      assertThat(cartAfter.getItems().getFirst().getQuantity()).isEqualTo(4);
      // 299.99 × 4 = 1199.96
      assertThat(cartAfter.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1199.96"));
    }

    @Test
    @DisplayName("does not affect other items when updating one item's quantity")
    void shouldNotAffectOtherItemsWhenUpdatingQuantity() {
      Long secondProductId = 55L;
      wireMock.stubFor(get(urlPathEqualTo("/api/v1/products/" + secondProductId + "/price"))
              .willReturn(aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                      .withBody("50.00")));

      cartService.addProductToCart(PRODUCT_ID, 1);      // 299.99
      cartService.addProductToCart(secondProductId, 2); // 100.00

      Cart cartBefore = cartRepository.findByUserId(USER_ID).orElseThrow();
      Long firstItemId = cartBefore.getItems().stream()
              .filter(i -> i.getProductId().equals(PRODUCT_ID))
              .findFirst().orElseThrow().getCartItemsId();

      cartService.updateCartItemQuantity(firstItemId, 3);

      Cart cartAfter = cartRepository.findByUserId(USER_ID).orElseThrow();
      CartItem secondItem = cartAfter.getItems().stream()
              .filter(i -> i.getProductId().equals(secondProductId))
              .findFirst().orElseThrow();

      assertThat(secondItem.getQuantity()).isEqualTo(2); // unchanged
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // getOrCreateCart()
  // ═══════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("getOrCreateCart()")
  class GetOrCreateCart {

    @Test
    @DisplayName("persists a new cart with zero total and empty items list")
    void shouldPersistNewCartWithDefaults() {
      Cart cart = cartService.getOrCreateCart(USER_ID);

      assertThat(cart.getCartId()).isNotNull(); // DB assigned an ID
      assertThat(cart.getUserId()).isEqualTo(USER_ID);
      assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("returns the same cart on repeated calls without creating duplicates")
    void shouldReturnSameCartOnRepeatedCalls() {
      Cart first  = cartService.getOrCreateCart(USER_ID);
      Cart second = cartService.getOrCreateCart(USER_ID);

      assertThat(first.getCartId()).isEqualTo(second.getCartId());
      assertThat(cartRepository.findAll()).hasSize(1);
    }
  }
}
