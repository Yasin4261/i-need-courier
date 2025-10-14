package com.api.demo.infrastructure.adapter.input.web.controller;

import com.api.demo.application.dto.CourierLoginRequest;
import com.api.demo.application.dto.CourierLoginResponse;
import com.api.demo.application.dto.CourierRegistrationRequest;
import com.api.demo.application.dto.CourierRegistrationResponse;
import com.api.demo.domain.port.input.CourierLoginUseCase;
import com.api.demo.domain.port.input.CourierRegistrationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courier/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Courier Authentication", description = "Kurye kimlik doğrulama işlemleri")
public class CourierAuthController {

    private final CourierLoginUseCase courierLoginUseCase;
    private final CourierRegistrationUseCase courierRegistrationUseCase;

    public CourierAuthController(CourierLoginUseCase courierLoginUseCase,
                               CourierRegistrationUseCase courierRegistrationUseCase) {
        this.courierLoginUseCase = courierLoginUseCase;
        this.courierRegistrationUseCase = courierRegistrationUseCase;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Kurye kayıt işlemi",
        description = "Yeni bir kurye hesabı oluşturur"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Kayıt başarılı",
            content = @Content(schema = @Schema(implementation = CourierRegistrationResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Bu email ile kayıtlı kurye zaten mevcut"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Geçersiz veri girişi"
        )
    })
    public ResponseEntity<CourierRegistrationResponse> register(@Valid @RequestBody CourierRegistrationRequest request) {
        CourierRegistrationResponse response = courierRegistrationUseCase.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Kurye giriş işlemi",
        description = "Email ve şifre ile giriş yaparak JWT token alır"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Giriş başarılı",
            content = @Content(schema = @Schema(implementation = CourierLoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Geçersiz kimlik bilgileri"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Kurye bulunamadı"
        )
    })
    public ResponseEntity<CourierLoginResponse> login(@Valid @RequestBody CourierLoginRequest request) {
        CourierLoginResponse response = courierLoginUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}
