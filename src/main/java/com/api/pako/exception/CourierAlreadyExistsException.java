package com.api.pako.exception;

/**
 * Exception thrown when a courier already exists with the given email or phone.
 */
public class CourierAlreadyExistsException extends RuntimeException {

    public CourierAlreadyExistsException(String message) {
        super(message);
    }

    public CourierAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

