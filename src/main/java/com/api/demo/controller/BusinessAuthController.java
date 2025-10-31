package com.api.demo.controller;

import com.api.demo.dto.*;
import com.api.demo.service.BusinessAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for business authentication operations.
 * Handles business registration and login endpoints.
 */
@RestController
@RequestMapping("/api/v1/business/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Business Authentication", description = "Business registration and login APIs")
public class BusinessAuthController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAuthController.class);

    private final BusinessAuthService businessAuthService;

    public BusinessAuthController(BusinessAuthService businessAuthService) {
        this.businessAuthService = businessAuthService;
    }

    /**
     * Register a new business.
     *
     * @param request Registration request with business details
     * @return Response with business ID and confirmation
     */
    @PostMapping("/register")
    @Operation(summary = "Register new business",
               description = "Register a new business with name, email, phone, password, and address. Status will be PENDING until approved by admin.")
    public ResponseEntity<ApiResponse<BusinessRegistrationResponse>> register(
            @Valid @RequestBody BusinessRegistrationRequest request) {

        logger.info("Business registration request received for: {}", request.getName());

        BusinessRegistrationResponse response = businessAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Business registration successful. Pending approval."));
    }

    /**
     * Login business and get JWT token.
     *
     * @param request Login request with email and password
     * @return Response with JWT token and business details
     */
    @PostMapping("/login")
    @Operation(summary = "Login business",
               description = "Authenticate business and receive JWT token. Business must be ACTIVE to login.")
    public ResponseEntity<ApiResponse<BusinessLoginResponse>> login(
            @Valid @RequestBody BusinessLoginRequest request) {

        logger.info("Business login request received for email: {}", request.getEmail());

        BusinessLoginResponse response = businessAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}

