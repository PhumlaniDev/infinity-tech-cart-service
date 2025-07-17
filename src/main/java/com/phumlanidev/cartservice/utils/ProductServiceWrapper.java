package com.phumlanidev.cartservice.utils;

import com.phumlanidev.cartservice.client.ProductClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceWrapper {

  private final ProductClient productClient;

  @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
  @Retry(name = "productService")
  @TimeLimiter(name = "productService")
  public BigDecimal getProductPriceById(Long productId) {
    return productClient.getProductPriceById(productId);
  }
}
