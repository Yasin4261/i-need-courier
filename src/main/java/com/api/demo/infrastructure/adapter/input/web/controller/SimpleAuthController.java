package com.api.demo.infrastructure.adapter.input.web.controller;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;
import com.api.demo.application.dto.CourierRegistrationRequest;
import com.api.demo.application.dto.CourierRegistrationResponse;
import com.api.demo.infrastructure.adapter.service.SimpleAuthService;
import com.api.demo.infrastructure.adapter.input.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simple-auth")
@CrossOrigin(origins = "*")
@Tag(name = "Simple Auth", description = "Basitleştirilmiş kimlik doğrulama")
public class SimpleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAuthController.class);
    private final SimpleAuthService simpleAuthService;

    public SimpleAuthController(SimpleAuthService simpleAuthService) {
        this.simpleAuthService = simpleAuthService;
    }

    @PostMapping("/register")
    @Operation(summary = "Basit kayıt", description = "Basitleştirilmiş kurye kayıt işlemi")
    public ResponseEntity<ApiResponse<CourierRegistrationResponse>> register(@Valid @RequestBody CourierRegistrationRequest request) {
        try {
            CourierRegistrationResponse response = simpleAuthService.register(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());

            if (e.getMessage().contains("already exists") || e.getMessage().contains("already in use")) {
                return ResponseEntity.status(409).body(ApiResponse.error(409, e.getMessage()));
            }
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Basit giriş", description = "Basitleştirilmiş kurye giriş işlemi")
    public ResponseEntity<ApiResponse<CourierLoginResponse>> login(@Valid @RequestBody CourierLoginRequest request) {
        try {
            CourierLoginResponse response = simpleAuthService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (RuntimeException e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(ApiResponse.error(401, e.getMessage()));
        }
    }
}
