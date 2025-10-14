package com.api.demo.application.usecase;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;
import com.api.demo.domain.exception.CourierNotFoundException;
import com.api.demo.domain.exception.InvalidCredentialsException;
import com.api.demo.domain.model.Courier;
import com.api.demo.domain.port.input.CourierLoginUseCase;
import com.api.demo.domain.port.output.CourierRepository;
import com.api.demo.domain.port.output.PasswordEncoder;
import com.api.demo.domain.port.output.TokenService;
import com.api.demo.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourierLoginUseCaseImpl implements CourierLoginUseCase {

    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public CourierLoginUseCaseImpl(CourierRepository courierRepository,
                                   PasswordEncoder passwordEncoder,
                                   TokenService tokenService) {
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public CourierLoginResponse login(CourierLoginRequest request) {
        Email email = new Email(request.getEmail());

        Courier courier = courierRepository.findByEmail(email)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), courier.getPassword().getHashedValue())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Update last login
        courier.recordLogin();
        courierRepository.updateLastLogin(courier.getId());

        // Generate JWT token
        String token = tokenService.generateToken(courier.getId(), courier.getEmail().getValue());

        return new CourierLoginResponse(
            token,
            courier.getId(),
            courier.getName(),
            courier.getEmail().getValue(),
            courier.getStatus().name()
        );
    }
}
