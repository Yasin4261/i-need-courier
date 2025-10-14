package com.api.demo.infrastructure.adapter.input.web.exception;

import com.api.demo.domain.exception.CourierAlreadyExistsException;
import com.api.demo.domain.exception.CourierNotFoundException;
import com.api.demo.domain.exception.InvalidCredentialsException;
import com.api.demo.domain.exception.InvalidEmailException;
import com.api.demo.domain.exception.InvalidPasswordException;
import com.api.demo.infrastructure.adapter.input.web.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CourierNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCourierNotFoundException(CourierNotFoundException ex) {
        logger.error("Courier not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(CourierAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCourierAlreadyExistsException(CourierAlreadyExistsException ex) {
        logger.error("Courier already exists: {}", ex.getMessage());
        return ResponseEntity.status(409).body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.error("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(401).body(ApiResponse.error(401, ex.getMessage()));
    }

    @ExceptionHandler({InvalidEmailException.class, InvalidPasswordException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(RuntimeException ex) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(400).body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Request validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(400).body(ApiResponse.error(400, errors, "Request validation failed"));
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadSqlGrammarException(BadSqlGrammarException ex) {
        logger.error("SQL Grammar error: {}", ex.getMessage(), ex);

        String userMessage = "Database operation failed";
        String detailedMessage = ex.getMessage();

        // SQL hatalarını daha anlaşılır hale getir
        if (detailedMessage.contains("column") && detailedMessage.contains("does not exist")) {
            userMessage = "Database schema mismatch - missing column";
        } else if (detailedMessage.contains("type") && detailedMessage.contains("but expression is of type")) {
            userMessage = "Database type mismatch error";
        }

        return ResponseEntity.status(500).body(ApiResponse.error(500, userMessage + " (Details: " + detailedMessage + ")"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(DataAccessException ex) {
        logger.error("Database access error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(ApiResponse.error(500, "Database operation failed: " + ex.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Object>> handleSQLException(SQLException ex) {
        logger.error("SQL error: {}", ex.getMessage(), ex);
        String message = "SQL operation failed: " + ex.getMessage() + " (SQL State: " + ex.getSQLState() + ")";
        return ResponseEntity.status(500).body(ApiResponse.error(500, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Geliştirme ortamında daha detaylı bilgi ver
        String detailedMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        if (ex.getCause() != null) {
            detailedMessage += " (Caused by: " + ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage() + ")";
        }

        return ResponseEntity.status(500).body(ApiResponse.error(500, detailedMessage));
    }
}
