package com.api.demo.service;

import com.api.demo.dto.BusinessRegistrationRequest;
import com.api.demo.dto.BusinessRegistrationResponse;
import com.api.demo.exception.CourierAlreadyExistsException;
import com.api.demo.model.Business;
import com.api.demo.repository.BusinessRepository;
import com.api.demo.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class BusinessRegistrationService {

    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public BusinessRegistrationService(BusinessRepository businessRepository,
                                       PasswordEncoder passwordEncoder,
                                       JwtTokenProvider jwtTokenProvider) {
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public BusinessRegistrationResponse register(BusinessRegistrationRequest request) {
        log.info("Attempting to register business: {}", request.getName());

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

}

