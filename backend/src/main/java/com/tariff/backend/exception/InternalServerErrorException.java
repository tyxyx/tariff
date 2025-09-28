package com.tariff.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// throw when the server breaks down
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal Server Error")
public class InternalServerErrorException extends RuntimeException {

    // Constructor that accepts a custom error message
    public InternalServerErrorException(String message) {
        super(message);
    }
    
}