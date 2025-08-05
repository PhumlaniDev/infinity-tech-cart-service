package com.phumlanidev.cartservice.utils;

import com.phumlanidev.cartservice.client.ProductClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceWrapper {

  private final ProductClient productClient;

  @CircuitBreaker(name = "productService", fallbackMethod = "getProductPriceByIdFallback")
  @Retry(name = "productService")
//  @TimeLimiter(name = "productService")
  public BigDecimal getProductPriceById(Long productId) {
    return productClient.getProductPriceById(productId);
  }

  public BigDecimal getProductPriceByIdFallback(Long productId, Throwable ex) {
    log.error("Getting product price failed: {}", ex.getMessage());
    return BigDecimal.ZERO;
  }
}
