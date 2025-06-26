package com.phumlanidev.cartservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@Slf4j
public class BaseEntity {

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt = Instant.now();

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @LastModifiedDate
  @Column(insertable = false)
  private Instant updatedAt;

  @LastModifiedBy
  @Column(insertable = false)
  private String updatedBy;

  @PrePersist
  public void prePersist() {
    log.info("Setting createdAt: {}", createdAt);
  }
}
