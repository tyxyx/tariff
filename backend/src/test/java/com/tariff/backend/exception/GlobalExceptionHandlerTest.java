package com.tariff.backend.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void handleValidationExceptions_shouldReturnBadRequestWithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "Email is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Email is required", response.getBody().get("email"));
    }

    @Test
    public void handleBadRequest_shouldReturnBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid request");

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request", response.getBody());
    }

    @Test
    public void handleResourceNotFound_shouldReturnNoContent() {
        NotFoundException ex = new NotFoundException("Resource not found");

        ResponseEntity<String> response = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Resource not found", response.getBody());
    }

    @Test
    public void handleInternalServerError_shouldReturnInternalServerError() {
        InternalServerErrorException ex = new InternalServerErrorException("Server error");

        ResponseEntity<String> response = exceptionHandler.handleInternalServerError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server error", response.getBody());
    }

    @Test
    public void handleUserAlreadyExists_shouldReturnConflict() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User already exists", response.getBody().get("message"));
    }

    @Test
    public void handleIllegalArgument_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Illegal argument", response.getBody().get("message"));
    }

    @Test
    public void handleInvalidCredentials_shouldReturnUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().get("message"));
    }

    @Test
    public void handleInvalidSignature_shouldReturnForbidden() {
        SignatureException ex = mock(SignatureException.class);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidSignature(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The JWT signature is invalid", response.getBody().get("message"));
    }

    @Test
    public void handleExpiredJwt_shouldReturnForbidden() {
        ExpiredJwtException ex = mock(ExpiredJwtException.class);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleExpiredJwt(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The JWT token has expired", response.getBody().get("message"));
    }

    @Test
    public void handleUsernameNotFound_shouldReturnNotFound() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUsernameNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().get("message"));
    }
}
