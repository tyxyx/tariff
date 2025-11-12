package com.tariff.backend.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
    MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new HashMap<>();
    ex
      .getBindingResult()
      .getAllErrors()
      .forEach(error -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
      });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(NotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NO_CONTENT);
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<String> handleInternalServerError(
    InternalServerErrorException ex
  ) {
    return new ResponseEntity<>(
      ex.getMessage(),
      HttpStatus.INTERNAL_SERVER_ERROR
    );
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleUserAlreadyExists(
    UserAlreadyExistsException ex
  ) {
    Map<String, String> body = Collections.singletonMap(
      "message",
      ex.getMessage()
    );
    return new ResponseEntity<>(body, HttpStatus.CONFLICT); // 409
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(
    IllegalArgumentException ex
  ) {
    Map<String, String> body = Collections.singletonMap(
      "message",
      ex.getMessage()
    );
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST); // 400
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCredentials(
    InvalidCredentialsException ex
  ) {
    Map<String, String> body = Collections.singletonMap(
      "message",
      ex.getMessage()
    );
    return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED); // 401
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<Map<String, String>> handleInvalidSignature(
    SignatureException ex
  ) {
    Map<String, String> body = Collections.singletonMap(
      "message",
      "The JWT signature is invalid"
    );
    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN); // 403
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<Map<String, String>> handleExpiredJwt(
    ExpiredJwtException ex
  ) {
    Map<String, String> body = Collections.singletonMap(
      "message",
      "The JWT token has expired"
    );
    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN); // 403
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
      Map<String, String> body = Collections.singletonMap(
              "message",
              ex.getMessage()
      );
      return new ResponseEntity<>(body, HttpStatus.NOT_FOUND); // 404
  }

}
