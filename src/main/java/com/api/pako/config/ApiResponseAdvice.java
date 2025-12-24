package com.api.pako.config;

import com.api.pako.dto.ApiResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, @NonNull Class converterType) {
        return returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ResponseEntity<?>) {
            return body;
        }

        if (body instanceof ApiResponse<?> r) {
            response.setStatusCode(HttpStatus.valueOf(r.getCode()));
            return r;
        }

        return ResponseEntity.ok(body);
    }

    @Override
    public @Nullable Map<String, Object> determineWriteHints(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType) {
        return ResponseBodyAdvice.super.determineWriteHints(body, returnType, selectedContentType, selectedConverterType);
    }
}
