package com.phumlanidev.cartservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Comment: this is the placeholder for documentation.
 */
@Data
@AllArgsConstructor
public class ErrorResponseDto {

  private String apiPath;

  private HttpStatus errorCode;

  private String errorMessage;

  private LocalDateTime errorTime;
}
