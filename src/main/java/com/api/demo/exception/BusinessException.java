package com.api.demo.exception;

/**
 * İş mantığı hatalarını temsil eden exception sınıfı
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

