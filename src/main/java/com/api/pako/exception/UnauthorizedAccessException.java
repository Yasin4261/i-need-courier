package com.api.pako.exception;

/**
 * Exception thrown when a user tries to access or modify a resource they don't own
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

