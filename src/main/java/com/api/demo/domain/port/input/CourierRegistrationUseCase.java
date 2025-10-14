package com.api.demo.domain.port.input;

import com.api.demo.application.dto.CourierRegistrationRequest;
import com.api.demo.application.dto.CourierRegistrationResponse;

public interface CourierRegistrationUseCase {
    CourierRegistrationResponse register(CourierRegistrationRequest request);
}
