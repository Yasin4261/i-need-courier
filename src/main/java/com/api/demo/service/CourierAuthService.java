package com.api.demo.service;

import com.api.demo.dto.CourierLoginRequest;
import com.api.demo.dto.CourierLoginResponse;
import com.api.demo.dto.CourierRegistrationRequest;
import com.api.demo.dto.CourierRegistrationResponse;
import com.api.demo.exception.CourierAlreadyExistsException;
import com.api.demo.exception.InvalidCredentialsException;
import com.api.demo.model.Courier;
import com.api.demo.repository.CourierRepository;
import com.api.demo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourierAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CourierAuthService.class);

    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public CourierAuthService(CourierRepository courierRepository,
                             PasswordEncoder passwordEncoder,
                             JwtTokenProvider jwtTokenProvider) {
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public CourierRegistrationResponse register(CourierRegistrationRequest request) {
        logger.info("Attempting to register courier: {}", request.getEmail());

        if (courierRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new CourierAlreadyExistsException("Courier already exists with email: " + request.getEmail());
        }

        if (courierRepository.existsByPhone(request.getPhone())) {
            logger.warn("Registration failed: Phone already exists: {}", request.getPhone());
            throw new CourierAlreadyExistsException("Phone number already in use: " + request.getPhone());
        }

        Courier courier = new Courier();
        courier.setName(request.getName());
        courier.setEmail(request.getEmail());
        courier.setPhone(request.getPhone());
        courier.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        courier.setStatus(Courier.CourierStatus.ONLINE);

        Courier savedCourier = courierRepository.save(courier);

        logger.info("Courier registered successfully with ID: {}", savedCourier.getId());

        return new CourierRegistrationResponse(
            savedCourier.getId(),
            savedCourier.getName(),
            savedCourier.getEmail(),
            "Registration successful"
        );
    }

    public CourierLoginResponse login(CourierLoginRequest request) {
        logger.info("Login attempt for courier email: {}", request.getEmail());

        Courier courier = courierRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                logger.warn("Login failed: Courier not found with email: {}", request.getEmail());
                return new InvalidCredentialsException("Invalid email or password");
            });

        if (!passwordEncoder.matches(request.getPassword(), courier.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(courier.getId(), courier.getEmail(), "COURIER");

        logger.info("Login successful for courier ID: {} ({})", courier.getId(), courier.getName());

        return new CourierLoginResponse(
            token,
            courier.getId(),
            courier.getName(),
            courier.getEmail(),
            "Login successful"
        );
    }
}

