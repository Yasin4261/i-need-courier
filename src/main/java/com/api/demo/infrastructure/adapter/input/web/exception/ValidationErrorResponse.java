package com.api.demo.infrastructure.adapter.input.web.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse {
    private String code;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse() {}

    public ValidationErrorResponse(String code, String message, Map<String, String> fieldErrors, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = timestamp;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
