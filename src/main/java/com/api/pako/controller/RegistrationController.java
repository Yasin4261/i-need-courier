package com.api.pako.controller;

import com.api.pako.dto.*;
import com.api.pako.service.BusinessRegistrationService;
import com.api.pako.service.CourierRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Unified Registration Controller.
 * Handles registration for both Couriers and Businesses.
 */
@RestController
@RequestMapping("/api/v1/auth/register")
@CrossOrigin(origins = "*")
@Tag(name = "Registration", description = "User registration endpoints for Couriers and Businesses")
@Slf4j
public class RegistrationController {

    private final CourierRegistrationService courierRegistrationService;
    private final BusinessRegistrationService businessRegistrationService;

    public RegistrationController(CourierRegistrationService courier,
                                  BusinessRegistrationService business) {
        this.courierRegistrationService = courier;
        this.businessRegistrationService = business;
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
    public ApiResponse<CourierRegistrationResponse> registerCourier(
            @Valid @RequestBody CourierRegistrationRequest request) {

        log.info("Courier registration request for: {}", request.getEmail());

        CourierRegistrationResponse response = courierRegistrationService.register(request);
        return ApiResponse.ok(response, "Courier registration successful");
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
    public ApiResponse<BusinessRegistrationResponse> registerBusiness(
            @Valid @RequestBody BusinessRegistrationRequest request) {

        log.info("Business registration request for: {}", request.getName());

        BusinessRegistrationResponse response = businessRegistrationService.register(request);
        return ApiResponse.ok(response, "Business registration successful. Pending approval.");
    }
}

