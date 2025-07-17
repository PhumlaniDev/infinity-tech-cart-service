package com.phumlanidev.cartservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProductFeingConfig {

  private final KeycloakTokenProvider keycloakTokenProvider;

  @Value("${keycloak.resource}")
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      String token = keycloakTokenProvider.getAccessToken(clientId, clientSecret);
      requestTemplate.header("Authorization", "Bearer " + token);
    };
  }

}
