package com.tariff.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// throw when user is performing something that he is not authorized to do
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unauthorized")
public class ForbiddenException extends RuntimeException {

    // Constructor that accepts a custom error message
    public ForbiddenException(String message) {
        super(message);
    }
    
}