package com.api.demo.controller;

import com.api.demo.dto.*;
import com.api.demo.service.BusinessAuthService;
import com.api.demo.service.CourierAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Unified Registration Controller.
 * Handles registration for both Couriers and Businesses.
 */
@RestController
@RequestMapping("/api/v1/auth/register")
@CrossOrigin(origins = "*")
@Tag(name = "Registration", description = "User registration endpoints for Couriers and Businesses")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    private final CourierAuthService courierAuthService;
    private final BusinessAuthService businessAuthService;

    public RegistrationController(CourierAuthService courierAuthService,
                                 BusinessAuthService businessAuthService) {
        this.courierAuthService = courierAuthService;
        this.businessAuthService = businessAuthService;
    }

    /**
     * Register a new courier.
     */
    @PostMapping("/courier")
    @Operation(
        summary = "Register new courier",
        description = "Register a new courier with name, email, phone, and password. " +
                     "After registration, use /api/v1/auth/login to get JWT token with COURIER role."
    )
    public ResponseEntity<ApiResponse<CourierRegistrationResponse>> registerCourier(
            @Valid @RequestBody CourierRegistrationRequest request) {

        logger.info("Courier registration request for: {}", request.getEmail());

        CourierRegistrationResponse response = courierAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Courier registration successful"));
    }

    /**
     * Register a new business.
     */
    @PostMapping("/business")
    @Operation(
        summary = "Register new business",
        description = "Register a new business with name, email, phone, password, and address. " +
                     "Status will be PENDING until approved by admin. " +
                     "After approval and using /api/v1/auth/login, you'll get JWT token with BUSINESS role."
    )
    public ResponseEntity<ApiResponse<BusinessRegistrationResponse>> registerBusiness(
            @Valid @RequestBody BusinessRegistrationRequest request) {

        logger.info("Business registration request for: {}", request.getName());

        BusinessRegistrationResponse response = businessAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Business registration successful. Pending approval."));
    }
}

