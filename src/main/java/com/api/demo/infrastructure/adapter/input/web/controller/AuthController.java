package com.api.demo.infrastructure.adapter.input.web.controller;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;
import com.api.demo.domain.port.input.CourierLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    private final CourierLoginUseCase courierLoginUseCase;

    public AuthController(CourierLoginUseCase courierLoginUseCase) {
        this.courierLoginUseCase = courierLoginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<CourierLoginResponse> login(@Valid @RequestBody CourierLoginRequest request) {
        CourierLoginResponse response = courierLoginUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}
