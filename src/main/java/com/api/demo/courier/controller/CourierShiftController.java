package com.api.demo.courier.controller;

import com.api.demo.dto.*;
import com.api.demo.model.enums.ShiftStatus;
import com.api.demo.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kurye Vardiya Controller
 * Kuryelerin vardiya yönetimi için REST API endpoint'leri
 */
@RestController
@RequestMapping("/api/v1/courier/shifts")
public class CourierShiftController {

    private final ShiftService shiftService;

    public CourierShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * Mevcut vardiya şablonlarını listele
     * GET /api/v1/courier/shifts/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getShiftTemplates() {
        List<ShiftTemplateDto> templates = shiftService.getAvailableShiftTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates, "Vardiya şablonları başarıyla getirildi"));
    }

    /**
     * Vardiya rezerve et
     * POST /api/v1/courier/shifts/reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ShiftDto>> reserveShift(
            Authentication authentication,
            @Valid @RequestBody ReserveShiftRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);
        ShiftDto shift = shiftService.reserveShift(courierId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(shift, "Vardiya başarıyla rezerve edildi"));
    }

    /**
     * Gelecek vardiyalarımı görüntüle
     * GET /api/v1/courier/shifts/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ShiftDto>>> getUpcomingShifts(Authentication authentication) {
        Long courierId = extractCourierIdFromAuth(authentication);
        List<ShiftDto> shifts = shiftService.getUpcomingShifts(courierId);
        return ResponseEntity.ok(ApiResponse.success(shifts, "Gelecek vardiyalar getirildi"));
    }

    /**
     * Tüm vardiyalarımı görüntüle (isteğe bağlı durum filtresi ile)
     * GET /api/v1/courier/shifts/my-shifts?status=CHECKED_IN
     */
    @GetMapping("/my-shifts")
    public ResponseEntity<ApiResponse<List<ShiftDto>>> getMyCourierShifts(
            Authentication authentication,
            @RequestParam(required = false) ShiftStatus status) {

        Long courierId = extractCourierIdFromAuth(authentication);
        List<ShiftDto> shifts = shiftService.getCourierShifts(courierId, status);
        return ResponseEntity.ok(ApiResponse.success(shifts, "Vardiyalar başarıyla getirildi"));
    }

    /**
     * Aktif vardiyamı görüntüle
     * GET /api/v1/courier/shifts/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ShiftDto>> getActiveShift(Authentication authentication) {
        Long courierId = extractCourierIdFromAuth(authentication);
        ShiftDto shift = shiftService.getActiveShift(courierId);

        if (shift == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "Aktif vardiya bulunamadı"));
        }

        return ResponseEntity.ok(ApiResponse.success(shift, "Aktif vardiya getirildi"));
    }

    /**
     * Vardiyaya check-in yap (giriş)
     * POST /api/v1/courier/shifts/{shiftId}/check-in
     */
    @PostMapping("/{shiftId}/check-in")
    public ResponseEntity<ApiResponse<ShiftDto>> checkIn(
            Authentication authentication,
            @PathVariable Long shiftId,
            @RequestBody(required = false) CheckInRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);

        if (request == null) {
            request = new CheckInRequest();
        }

        ShiftDto shift = shiftService.checkIn(courierId, shiftId, request);
        return ResponseEntity.ok(ApiResponse.success(shift, "Vardiyaya giriş başarılı"));
    }

    /**
     * Vardiyadan check-out yap (çıkış)
     * POST /api/v1/courier/shifts/{shiftId}/check-out
     */
    @PostMapping("/{shiftId}/check-out")
    public ResponseEntity<ApiResponse<ShiftDto>> checkOut(
            Authentication authentication,
            @PathVariable Long shiftId,
            @RequestBody(required = false) CheckOutRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);

        if (request == null) {
            request = new CheckOutRequest();
        }

        ShiftDto shift = shiftService.checkOut(courierId, shiftId, request);
        return ResponseEntity.ok(ApiResponse.success(shift, "Vardiyadan çıkış başarılı"));
    }

    /**
     * Vardiya rezervasyonunu iptal et
     * DELETE /api/v1/courier/shifts/{shiftId}/cancel
     */
    @DeleteMapping("/{shiftId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelShift(
            Authentication authentication,
            @PathVariable Long shiftId) {

        Long courierId = extractCourierIdFromAuth(authentication);
        shiftService.cancelShift(courierId, shiftId);

        return ResponseEntity.ok(ApiResponse.success(null, "Vardiya rezervasyonu iptal edildi"));
    }

    /**
     * Authentication'dan courier ID'yi çıkar
     * JWT token'da userId claim olarak saklanıyor
     */
    private Long extractCourierIdFromAuth(Authentication authentication) {
        // JWT token'dan userId claim'ini al
        // Authentication.getPrincipal() userId'yi içeriyor (JwtAuthenticationFilter tarafından set ediliyor)
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }

        throw new IllegalStateException("JWT token'dan courier ID alınamadı");
    }
}

