package com.api.demo.controller;

import com.api.demo.dto.ApiResponse;
import com.api.demo.dto.UnifiedLoginRequest;
import com.api.demo.dto.UnifiedLoginResponse;
import com.api.demo.service.UnifiedAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Unified Authentication", description = "Single login endpoint for all user types")
public class UnifiedAuthController {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedAuthController.class);
    private final UnifiedAuthService unifiedAuthService;

    public UnifiedAuthController(UnifiedAuthService unifiedAuthService) {
        this.unifiedAuthService = unifiedAuthService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Unified login for all users",
        description = "Login endpoint for Couriers, Businesses, and Admins. System automatically detects user type."
    )
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> login(@Valid @RequestBody UnifiedLoginRequest request) {
        logger.info("Unified login request for: {}", request.getEmail());
        UnifiedLoginResponse response = unifiedAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}

