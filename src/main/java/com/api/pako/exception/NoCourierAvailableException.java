package com.api.pako.exception;

/**
 * Exception thrown when no courier is available for assignment
 */
public class NoCourierAvailableException extends RuntimeException {

    public NoCourierAvailableException(String message) {
        super(message);
    }

    public NoCourierAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

