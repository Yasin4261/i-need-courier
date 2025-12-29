package com.api.pako.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

/**
 * Standard API response wrapper.
 * Provides consistent response format across all endpoints.
 *
 * @param <T> Type of data being returned
 */
@Setter
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    // Getters and Setters
    private int code;
    private T data;
    private String message;
    private OffsetDateTime respondedAt;

    public ApiResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.respondedAt = OffsetDateTime.now();
    }

    // Static factory methods
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(HttpStatus.OK.value(), data, message);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data, HttpStatus.OK.getReasonPhrase());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), data, message);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), data, HttpStatus.CREATED.getReasonPhrase());
    }

    public static <T> ApiResponse<T> deleted(String message) {
        return new ApiResponse<>(HttpStatus.NO_CONTENT.value(), null, message);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, T payload) {
        return new ApiResponse<>(status.value(), payload, status.getReasonPhrase());
    }

    public static ApiResponse<Void> noContent(String msg) {
        return new ApiResponse<>(HttpStatus.NO_CONTENT.value(), null, msg);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", data=" + data +
                ", respondedAt=" + respondedAt.toString() +
                ", message='" + message + '\'' +
                '}';
    }
}


