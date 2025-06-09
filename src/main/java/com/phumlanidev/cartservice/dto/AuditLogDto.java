package com.phumlanidev.cartservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogDto {

  @NotBlank(message = "ID is required")
  private String id;
  @NotBlank(message = "User ID is required")
  private String userId;
  @NotBlank(message = "Username is required")
  private String username;
  @NotBlank(message = "Action is required")
  private String action;
  @NotBlank(message = "IP address is required")
  private String ip;
  @NotBlank(message = "Details are required")
  private String details;
  @NotNull(message = "Timestamp is required")
  private Instant timestamp;
}
