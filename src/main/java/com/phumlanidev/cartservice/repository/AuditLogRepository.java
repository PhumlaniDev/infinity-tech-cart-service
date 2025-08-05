package com.phumlanidev.cartservice.repository;


import com.phumlanidev.cartservice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment: this is the placeholder for documentation.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {

  /**
   * Comment: this is the placeholder for documentation.
   */
  List<AuditLog> findByUserId(String userId);
}
