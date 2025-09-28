package com.tariff.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// throw when the request body does not fit the criteria / when the user is trying to do something illegal
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class BadRequestException extends RuntimeException {

    // Constructor that accepts a custom error message
    public BadRequestException(String message) {
        super(message);
    }
    
}