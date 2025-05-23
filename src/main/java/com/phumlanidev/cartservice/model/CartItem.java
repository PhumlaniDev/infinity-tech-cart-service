package com.phumlanidev.cartservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Comment: this is the placeholder for documentation.
 */
@Entity
@Table
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long cartItemsId;
  @ManyToOne
  @JoinColumn(name = "cart_id")
  private Cart cart;
  private Long productId;
  private BigDecimal quantity;
  private BigDecimal price;
}