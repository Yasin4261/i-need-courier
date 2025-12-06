package com.api.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

    // Static factory methods
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, data, message);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}


