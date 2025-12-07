package com.api.pako.exception;

/**
 * Exception thrown when an invalid order operation is attempted
 */
public class InvalidOrderOperationException extends RuntimeException {

    public InvalidOrderOperationException(String message) {
        super(message);
    }

    public InvalidOrderOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

