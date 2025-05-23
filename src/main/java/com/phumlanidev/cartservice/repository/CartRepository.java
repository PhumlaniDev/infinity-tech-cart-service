package com.phumlanidev.cartservice.repository;


import com.phumlanidev.cartservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Comment: this is the placeholder for documentation.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  /**
   * Comment: this is the placeholder for documentation.
   */
  Optional<Cart> findByUserId(String userId);

}
