package com.phumlanidev.cartservice.exception;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comment: this is the placeholder for documentation.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                HttpHeaders headers,
                                                                HttpStatusCode status,
                                                                WebRequest request) {

    Map<String, String> validationErrors = new HashMap<>();
    List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

    validationErrorList.forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String validationMsg = error.getDefaultMessage();
      validationErrors.put(fieldName, validationMsg);
    });
    return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {

    ErrorResponseDto errorResponseDto =
        new ErrorResponseDto(request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(), LocalDateTime.now());

    return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex,
                                                                      WebRequest request) {
    ErrorResponseDto errorResponseDto =
        new ErrorResponseDto(request.getDescription(false), HttpStatus.NOT_FOUND, ex.getMessage(),
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @ExceptionHandler(UserAlreadyExistException.class)
  public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistException(
      UserAlreadyExistException ex, WebRequest request) {
    ErrorResponseDto errorResponseDto =
        new ErrorResponseDto(request.getDescription(false), HttpStatus.BAD_REQUEST, ex.getMessage(),
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
  }

  /**
   * Comment: this is the placeholder for documentation.
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponseDto> handleBaseException(BaseException ex,
                                                              WebRequest request) {
    ErrorResponseDto errorResponseDto =
        new ErrorResponseDto(request.getDescription(false), ex.getStatus(), ex.getMessage(),
            LocalDateTime.now());

    return new ResponseEntity<>(errorResponseDto, ex.getStatus());
  }

}