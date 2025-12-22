package com.api.pako.exception;

import com.api.pako.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent and detailed error responses across the application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                "Validation failed for one or more fields",
                request.getRequestURI()
        );

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorResponse.addValidationError(fieldName, errorMessage);
        });

        log.warn("Validation error on {}: {}", request.getRequestURI(), errorResponse.getValidationErrors());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle courier already exists exception.
     */
    @ExceptionHandler(CourierAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCourierAlreadyExists(
            CourierAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Courier already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handle invalid credentials exception.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Invalid credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                401,
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Handle order not found exception.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Order not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handle unauthorized access exception.
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized access: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                403,
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handle invalid order operation exception.
     */
    @ExceptionHandler(InvalidOrderOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderOperation(
            InvalidOrderOperationException ex,
            HttpServletRequest request) {

        log.warn("Invalid order operation: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle no courier available exception.
     */
    @ExceptionHandler(NoCourierAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoCourierAvailableException(
            NoCourierAvailableException ex,
            HttpServletRequest request) {

        log.warn("No courier available: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                503,
                "Service Unavailable",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }

    /**
     * Handle assignment not found exception.
     */
    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotFoundException(
            AssignmentNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Assignment not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handle assignment not owned exception.
     */
    @ExceptionHandler(AssignmentNotOwnedException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotOwnedException(
            AssignmentNotOwnedException ex,
            HttpServletRequest request) {

        log.warn("Assignment not owned: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                403,
                "Forbidden",
                "Bu atama size ait değil",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handle assignment expired exception.
     */
    @ExceptionHandler(AssignmentExpiredException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentExpiredException(
            AssignmentExpiredException ex,
            HttpServletRequest request) {

        log.warn("Assignment expired: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                410,
                "Gone",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(errorResponse);
    }

    /**
     * Handle invalid assignment status exception.
     */
    @ExceptionHandler(InvalidAssignmentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAssignmentStatusException(
            InvalidAssignmentStatusException ex,
            HttpServletRequest request) {

        log.warn("Invalid assignment status: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handle business logic exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business exception: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle all other runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Handle malformed JSON or unreadable request body.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed JSON request: {}", ex.getMessage());

        String detailedMessage = "Malformed JSON request body. Please check your JSON syntax.";
        if (ex.getCause() != null) {
            String cause = ex.getCause().getMessage();
            if (cause != null && cause.contains("Unexpected character")) {
                detailedMessage = "Invalid JSON syntax. Check for missing quotes, commas, or brackets.";
            } else if (cause != null && cause.contains("Cannot deserialize")) {
                detailedMessage = "Invalid data type in JSON. " + cause;
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                detailedMessage,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle wrong HTTP method (e.g., POST when GET is expected).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? String.join(", ", ex.getSupportedHttpMethods().stream().map(method -> method.name()).toList())
                : "N/A";

        ErrorResponse errorResponse = new ErrorResponse(
                405,
                "Method Not Allowed",
                String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                        ex.getMethod(), supportedMethods),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorResponse);
    }

    /**
     * Handle unsupported media type (e.g., missing Content-Type: application/json).
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Unsupported media type: {}", ex.getContentType());

        String supportedTypes = ex.getSupportedMediaTypes() != null
                ? ex.getSupportedMediaTypes().toString()
                : "application/json";

        ErrorResponse errorResponse = new ErrorResponse(
                415,
                "Unsupported Media Type",
                String.format("Content type '%s' is not supported. Supported types: %s",
                        ex.getContentType(), supportedTypes),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorResponse);
    }

    /**
     * Handle missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        log.warn("Missing parameter: {}", ex.getParameterName());

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                String.format("Required parameter '%s' of type '%s' is missing",
                        ex.getParameterName(), ex.getParameterType()),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle type mismatch (e.g., passing string when Long is expected).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Type mismatch for parameter: {}", ex.getName());

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                String.format("Parameter '%s' should be of type '%s' but received: '%s'",
                        ex.getName(), expectedType, ex.getValue()),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle illegal state (e.g., authentication issues in controllers).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error("Illegal state: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "A system error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Handle illegal argument (e.g., invalid enum values).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                "Invalid argument: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("System error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An internal server error occurred: " + ex.getMessage() + ". Please contact support if the problem persists.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
