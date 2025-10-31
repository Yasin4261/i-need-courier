package com.api.demo.service;

import com.api.demo.dto.BusinessLoginRequest;
import com.api.demo.dto.BusinessLoginResponse;
import com.api.demo.dto.BusinessRegistrationRequest;
import com.api.demo.dto.BusinessRegistrationResponse;
import com.api.demo.exception.CourierAlreadyExistsException;
import com.api.demo.exception.InvalidCredentialsException;
import com.api.demo.model.Business;
import com.api.demo.repository.BusinessRepository;
import com.api.demo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class BusinessAuthService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAuthService.class);

    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public BusinessAuthService(BusinessRepository businessRepository,
                              PasswordEncoder passwordEncoder,
                              JwtTokenProvider jwtTokenProvider) {
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public BusinessRegistrationResponse register(BusinessRegistrationRequest request) {
        logger.info("Attempting to register business: {}", request.getName());

        if (businessRepository.existsByEmail(request.getEmail())) {
            throw new CourierAlreadyExistsException("Business already exists with email: " + request.getEmail());
        }

        if (businessRepository.existsByPhone(request.getPhone())) {
            throw new CourierAlreadyExistsException("Phone number already in use: " + request.getPhone());
        }

        String businessCode = "BUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Business business = new Business();
        business.setBusinessCode(businessCode);
        business.setName(request.getName());
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        business.setAddress(request.getAddress());
        business.setContactPerson(request.getContactPerson());
        business.setBusinessType(request.getBusinessType());
        business.setStatus(Business.BusinessStatus.PENDING);
        business.setIsActive(true);
        business.setCreatedAt(LocalDateTime.now());

        Business saved = businessRepository.save(business);

        return new BusinessRegistrationResponse(
            saved.getId(),
            saved.getName(),
            saved.getEmail(),
            saved.getStatus().toString(),
            "Registration successful. Pending approval."
        );
    }

    public BusinessLoginResponse login(BusinessLoginRequest request) {
        logger.info("Login attempt for business email: {}", request.getEmail());

        Business business = businessRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), business.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!business.canLogin()) {
            throw new InvalidCredentialsException("Business account is " + business.getStatus());
        }

        business.recordLogin();
        businessRepository.save(business);

        String token = jwtTokenProvider.generateToken(business.getId(), business.getEmail());

        return new BusinessLoginResponse(
            token,
            business.getId(),
            business.getName(),
            business.getEmail(),
            business.getStatus().toString(),
            "Login successful"
        );
    }
}

