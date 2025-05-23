package com.phumlanidev.cartservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogDto {

  private String id;
  private String userId;
  private String username;
  private String action;
  private String ip;
  private String details;
  private Instant timestamp;
}
