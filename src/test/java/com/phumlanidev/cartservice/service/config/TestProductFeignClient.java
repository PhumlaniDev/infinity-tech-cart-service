package com.phumlanidev.cartservice.service.config;

import feign.RequestInterceptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestProductFeignClient {

  @Bean("testRequestInterceptor")
  @Primary
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {};
  }

  @Bean
  @Primary
  public JwtDecoder jwtDecoder() {
    return token -> {
      // Return a minimal valid Jwt so any secured endpoint
      // doesn't blow up during context wiring
      return Jwt.withTokenValue(token)
              .header("alg", "none")
              .claim("sub", "test-user")
              .claim("preferred_username", "phumlani")
              .issuedAt(java.time.Instant.now())
              .expiresAt(java.time.Instant.now().plusSeconds(3600))
              .build();
    };
  }
}
