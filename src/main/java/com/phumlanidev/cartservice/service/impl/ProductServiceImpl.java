package com.phumlanidev.cartservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

  private final RestTemplate restTemplate;

  public BigDecimal getProductPriceById(Long productId) {
    String productServiceUrl = "http://localhost:9400/api/v1/products/" + productId + "/price";
    return restTemplate.getForObject(productServiceUrl, BigDecimal.class);
  }
}
