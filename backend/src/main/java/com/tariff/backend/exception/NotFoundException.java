package com.tariff.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// throw when resource not found
@ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "No results Found")
public class NotFoundException extends RuntimeException {

    // Constructor that accepts a custom error message
    public NotFoundException(String message) {
        super(message);
    }
    
}