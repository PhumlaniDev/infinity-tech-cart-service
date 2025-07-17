package com.phumlanidev.cartservice.client;

import com.phumlanidev.cartservice.config.ProductFeingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@FeignClient(
        name = "product-service", // if using Eureka
        configuration = ProductFeingConfig.class
)
public interface ProductClient {

  @GetMapping("/api/v1/products/{productId}/price")
  BigDecimal getProductPriceById(Long productId);
}
