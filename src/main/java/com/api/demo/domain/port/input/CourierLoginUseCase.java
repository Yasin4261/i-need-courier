package com.api.demo.domain.port.input;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;

public interface CourierLoginUseCase {
    CourierLoginResponse login(CourierLoginRequest request);
}
