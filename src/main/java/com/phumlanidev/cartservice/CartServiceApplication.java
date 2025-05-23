package com.phumlanidev.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Comment: this is the placeholder for documentation.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class CartServiceApplication {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public static void main(String[] args) {
    SpringApplication.run(CartServiceApplication.class, args);
  }

}
