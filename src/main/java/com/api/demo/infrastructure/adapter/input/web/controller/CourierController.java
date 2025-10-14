package com.api.demo.infrastructure.adapter.input.web.controller;

import com.api.demo.domain.model.Courier;
import com.api.demo.domain.port.output.CourierRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courier")
@CrossOrigin(origins = "*")
@Tag(name = "Courier Management", description = "Kurye yönetim işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class CourierController {

    private final CourierRepository courierRepository;

    public CourierController(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @GetMapping("/profile")
    @Operation(
        summary = "Kurye profil bilgilerini getir",
        description = "Giriş yapmış kuryenin profil bilgilerini döndürür"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profil bilgileri başarıyla alındı"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Geçersiz veya eksik JWT token"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Kurye bulunamadı"
        )
    })
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Long courierId = (Long) authentication.getPrincipal();

        Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new RuntimeException("Courier not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", courier.getId());
        profile.put("name", courier.getName());
        profile.put("email", courier.getEmail().getValue());
        profile.put("phone", courier.getPhone().getNumber());
        profile.put("status", courier.getStatus().name());
        profile.put("createdAt", courier.getCreatedAt());
        profile.put("lastLoginAt", courier.getLastLoginAt());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/status")
    @Operation(
        summary = "Kurye durumunu güncelle",
        description = "Kuryenin çevrimiçi/çevrimdışı durumunu günceller"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Durum başarıyla güncellendi"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Geçersiz durum değeri"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Geçersiz veya eksik JWT token"
        )
    })
    public ResponseEntity<Map<String, String>> updateStatus(
            @Parameter(description = "Yeni durum (ONLINE/OFFLINE)", required = true)
            @RequestParam String status,
            Authentication authentication) {
        Long courierId = (Long) authentication.getPrincipal();

        Courier courier = courierRepository.findById(courierId)
            .orElseThrow(() -> new RuntimeException("Courier not found"));

        // Simple status update logic - you can enhance this
        switch (status.toUpperCase()) {
            case "ONLINE":
                courier.activate();
                break;
            case "OFFLINE":
                courier.deactivate();
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }

        courierRepository.save(courier);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Status updated successfully");
        response.put("newStatus", courier.getStatus().name());

        return ResponseEntity.ok(response);
    }
}
