package com.api.demo.application.usecase;

import com.api.demo.application.dto.CourierRegistrationRequest;
import com.api.demo.application.dto.CourierRegistrationResponse;
import com.api.demo.domain.exception.CourierAlreadyExistsException;
import com.api.demo.domain.model.Courier;
import com.api.demo.domain.port.input.CourierRegistrationUseCase;
import com.api.demo.domain.port.output.CourierRepository;
import com.api.demo.domain.port.output.PasswordEncoder;
import com.api.demo.domain.valueobject.Email;
import com.api.demo.domain.valueobject.Password;
import com.api.demo.domain.valueobject.Phone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourierRegistrationUseCaseImpl implements CourierRegistrationUseCase {

    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;

    public CourierRegistrationUseCaseImpl(CourierRepository courierRepository,
                                          PasswordEncoder passwordEncoder) {
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CourierRegistrationResponse register(CourierRegistrationRequest request) {
        Email email = new Email(request.getEmail());

        // Check if courier already exists
        if (courierRepository.existsByEmail(email)) {
            throw new CourierAlreadyExistsException("Courier already exists with email: " + request.getEmail());
        }

        // Create new courier
        Phone phone = new Phone(request.getPhone());
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Password password = new Password(hashedPassword);

        Courier newCourier = new Courier(request.getName(), email, phone, password);

        // Save courier
        Courier savedCourier = courierRepository.save(newCourier);

        return new CourierRegistrationResponse(
            savedCourier.getId(),
            savedCourier.getName(),
            savedCourier.getEmail().getValue(),
            "Courier registered successfully"
        );
    }
}
