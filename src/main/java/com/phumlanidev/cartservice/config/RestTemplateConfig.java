package com.phumlanidev.cartservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Comment: this is the placeholder for documentation.
 */
@Configuration
public class RestTemplateConfig {

  /**
   * Comment: this is the placeholder for documentation.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
