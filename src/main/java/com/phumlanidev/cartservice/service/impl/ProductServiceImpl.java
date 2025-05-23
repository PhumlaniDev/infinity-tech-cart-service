package com.phumlanidev.cartservice.service.impl;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Comment: this is the placeholder for documentation.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

  private final RestTemplate restTemplate;

  /**
   * Comment: this is the placeholder for documentation.
   */
  public BigDecimal getProductPriceById(Long productId) {
    String productServiceUrl = "http://localhost:9100/api/v1/products/" + productId + "/price";
    return restTemplate.getForObject(productServiceUrl, BigDecimal.class);
  }
}
