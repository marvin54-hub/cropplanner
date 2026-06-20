package com.cropplanner.exception;

/**
 * Thrown when the caller is not authenticated, or is authenticated but not
 * permitted to access/modify a given resource (e.g. another user's schedule).
 * Mapped to HTTP 401/403 by GlobalExceptionHandler.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
