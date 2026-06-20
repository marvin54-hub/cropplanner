package com.cropplanner.exception;

/**
 * Thrown when a request violates a business rule rather than basic field
 * validation — e.g. registering an email that already exists, or attempting
 * an illegal status transition. Mapped to HTTP 409/400 by GlobalExceptionHandler
 * depending on context.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
