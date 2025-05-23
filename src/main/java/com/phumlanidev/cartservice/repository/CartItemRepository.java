package com.phumlanidev.cartservice.repository;

import com.phumlanidev.cartservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Comment: this is the placeholder for documentation.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {


}
