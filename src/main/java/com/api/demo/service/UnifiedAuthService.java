package com.api.demo.service;

import com.api.demo.dto.UnifiedLoginRequest;
import com.api.demo.dto.UnifiedLoginResponse;
import com.api.demo.exception.InvalidCredentialsException;
import com.api.demo.model.Business;
import com.api.demo.model.Courier;
import com.api.demo.repository.BusinessRepository;
import com.api.demo.repository.CourierRepository;
import com.api.demo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UnifiedAuthService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedAuthService.class);
    private final CourierRepository courierRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UnifiedAuthService(CourierRepository courierRepository, BusinessRepository businessRepository,
                             PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.courierRepository = courierRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UnifiedLoginResponse login(UnifiedLoginRequest request) {
        logger.info("Unified login attempt for email: {}", request.getEmail());

        Optional<Courier> courierOpt = courierRepository.findByEmail(request.getEmail());
        if (courierOpt.isPresent()) {
            return loginCourier(courierOpt.get(), request.getPassword());
        }

        Optional<Business> businessOpt = businessRepository.findByEmail(request.getEmail());
        if (businessOpt.isPresent()) {
            return loginBusiness(businessOpt.get(), request.getPassword());
        }

        logger.warn("Login failed: User not found with email: {}", request.getEmail());
        throw new InvalidCredentialsException("Invalid email or password");
    }

    private UnifiedLoginResponse loginCourier(Courier courier, String password) {
        if (!passwordEncoder.matches(password, courier.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (courier.getStatus() != Courier.CourierStatus.AVAILABLE) {
            throw new InvalidCredentialsException("Your courier account is not active");
        }

        courierRepository.save(courier);

        String token = jwtTokenProvider.generateToken(courier.getId(), courier.getEmail(), "COURIER");

        return new UnifiedLoginResponse(token, courier.getId(), courier.getEmail(),
                courier.getName(), "COURIER", courier.getStatus().toString(), "Login successful");
    }

    private UnifiedLoginResponse loginBusiness(Business business, String password) {
        if (!passwordEncoder.matches(password, business.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!business.canLogin()) {
            throw new InvalidCredentialsException("Your business account is " + business.getStatus());
        }

        business.recordLogin();
        businessRepository.save(business);

        String token = jwtTokenProvider.generateToken(business.getId(), business.getEmail(), "BUSINESS");

        return new UnifiedLoginResponse(token, business.getId(), business.getEmail(),
                business.getName(), "BUSINESS", business.getStatus().toString(), "Login successful");
    }
}

