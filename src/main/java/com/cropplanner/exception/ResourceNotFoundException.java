package com.cropplanner.exception;

/**
 * Thrown whenever a requested entity (Crop, Schedule, User, etc.) cannot be
 * found by id or other lookup key. Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found with id: " + identifier);
    }
}
