package com.phumlanidev.cartservice.repository;


import com.phumlanidev.cartservice.model.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

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
