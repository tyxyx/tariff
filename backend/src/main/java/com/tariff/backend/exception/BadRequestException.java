package com.tariff.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Custom exception class for handling resource not found scenario
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class BadRequestException extends RuntimeException {

    // Constructor that accepts a custom error message
    public BadRequestException(String message) {
        super(message);
    }
    
}