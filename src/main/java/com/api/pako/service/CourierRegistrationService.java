package com.api.pako.service;

import com.api.pako.dto.CourierRegistrationRequest;
import com.api.pako.dto.CourierRegistrationResponse;
import com.api.pako.exception.CourierAlreadyExistsException;
import com.api.pako.model.Courier;
import com.api.pako.repository.CourierRepository;
import com.api.pako.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class CourierRegistrationService {

    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public CourierRegistrationService(CourierRepository courierRepository,
                                      PasswordEncoder passwordEncoder,
                                      JwtTokenProvider jwtTokenProvider) {
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public CourierRegistrationResponse register(CourierRegistrationRequest request) {
        log.info("Attempting to register courier: {}", request.getEmail());

        if (courierRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new CourierAlreadyExistsException("Courier already exists with email: " + request.getEmail());
        }

        if (courierRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone already exists: {}", request.getPhone());
            throw new CourierAlreadyExistsException("Phone number already in use: " + request.getPhone());
        }

        Courier courier = new Courier();
        courier.setName(request.getName());
        courier.setEmail(request.getEmail());
        courier.setPhone(request.getPhone());
        courier.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        courier.setStatus(Courier.CourierStatus.ONLINE);

        Courier savedCourier = courierRepository.save(courier);

        log.info("Courier registered successfully with ID: {}", savedCourier.getId());

        return new CourierRegistrationResponse(
            savedCourier.getId(),
            savedCourier.getName(),
            savedCourier.getEmail(),
            "Registration successful"
        );
    }

}

