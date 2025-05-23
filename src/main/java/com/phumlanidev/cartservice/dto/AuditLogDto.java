package com.phumlanidev.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
