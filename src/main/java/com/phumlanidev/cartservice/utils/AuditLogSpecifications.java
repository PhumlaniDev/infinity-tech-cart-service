package com.phumlanidev.cartservice.utils;



import com.phumlanidev.cartservice.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

/**
 * Comment: this is the placeholder for documentation.
 */
public class AuditLogSpecifications {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public static Specification<AuditLog> hasUserId(String userId) {
    return (root, query, cb) -> userId == null ? null :
            cb.equal(root.get("userId"), userId);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  public static Specification<AuditLog> hasAction(String action) {
    return (root, query, cb) -> action == null ? null :
            cb.equal(root.get("action"), action);
  }
}
