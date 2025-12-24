package com.api.pako.courier.controller;

import com.api.pako.dto.*;
import com.api.pako.model.enums.ShiftStatus;
import com.api.pako.service.ShiftService;
import jakarta.validation.Valid;
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
    public ApiResponse<List<ShiftTemplateDto>> getShiftTemplates() {
        List<ShiftTemplateDto> templates = shiftService.getAvailableShiftTemplates();
        return ApiResponse.ok(templates, "Vardiya şablonları başarıyla getirildi");
    }

    /**
     * Vardiya rezerve et
     * POST /api/v1/courier/shifts/reserve
     */
    @PostMapping("/reserve")
    public ApiResponse<ShiftDto> reserveShift(
            Authentication authentication,
            @Valid @RequestBody ReserveShiftRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);
        ShiftDto shift = shiftService.reserveShift(courierId, request);

        return ApiResponse.created(shift, "Vardiya başarıyla rezerve edildi");
    }

    /**
     * Gelecek vardiyalarımı görüntüle
     * GET /api/v1/courier/shifts/upcoming
     */
    @GetMapping("/upcoming")
    public ApiResponse<List<ShiftDto>> getUpcomingShifts(Authentication authentication) {
        Long courierId = extractCourierIdFromAuth(authentication);
        List<ShiftDto> shifts = shiftService.getUpcomingShifts(courierId);
        return ApiResponse.ok(shifts, "Gelecek vardiyalar getirildi");
    }

    /**
     * Tüm vardiyalarımı görüntüle (isteğe bağlı durum filtresi ile)
     * GET /api/v1/courier/shifts/my-shifts?status=CHECKED_IN
     */
    @GetMapping("/my-shifts")
    public ApiResponse<List<ShiftDto>> getMyCourierShifts(
            Authentication authentication,
            @RequestParam(required = false) ShiftStatus status) {

        Long courierId = extractCourierIdFromAuth(authentication);
        List<ShiftDto> shifts = shiftService.getCourierShifts(courierId, status);
        return ApiResponse.ok(shifts, "Vardiyalar başarıyla getirildi");
    }

    /**
     * Aktif vardiyamı görüntüle
     * GET /api/v1/courier/shifts/active
     */
    @GetMapping("/active")
    public ApiResponse<ShiftDto> getActiveShift(Authentication authentication) {
        Long courierId = extractCourierIdFromAuth(authentication);
        ShiftDto shift = shiftService.getActiveShift(courierId);

        if (shift == null) {
            return ApiResponse.ok(null, "Aktif vardiya bulunamadı");
        }

        return ApiResponse.ok(shift, "Aktif vardiya getirildi");
    }

    /**
     * Vardiyaya check-in yap (giriş)
     * POST /api/v1/courier/shifts/{shiftId}/check-in
     */
    @PostMapping("/{shiftId}/check-in")
    public ApiResponse<ShiftDto> checkIn(
            Authentication authentication,
            @PathVariable Long shiftId,
            @RequestBody(required = false) CheckInRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);

        if (request == null) {
            request = new CheckInRequest();
        }

        ShiftDto shift = shiftService.checkIn(courierId, shiftId, request);
        return ApiResponse.ok(shift, "Vardiyaya giriş başarılı");
    }

    /**
     * Vardiyadan check-out yap (çıkış)
     * POST /api/v1/courier/shifts/{shiftId}/check-out
     */
    @PostMapping("/{shiftId}/check-out")
    public ApiResponse<ShiftDto> checkOut(
            Authentication authentication,
            @PathVariable Long shiftId,
            @RequestBody(required = false) CheckOutRequest request) {

        Long courierId = extractCourierIdFromAuth(authentication);

        if (request == null) {
            request = new CheckOutRequest();
        }

        ShiftDto shift = shiftService.checkOut(courierId, shiftId, request);
        return ApiResponse.ok(shift, "Vardiyadan çıkış başarılı");
    }

    /**
     * Vardiya rezervasyonunu iptal et
     * DELETE /api/v1/courier/shifts/{shiftId}/cancel
     */
    @DeleteMapping("/{shiftId}/cancel")
    public ApiResponse<Void> cancelShift(
            Authentication authentication,
            @PathVariable Long shiftId) {

        Long courierId = extractCourierIdFromAuth(authentication);
        shiftService.cancelShift(courierId, shiftId);

        return ApiResponse.noContent("Vardiya rezervasyonu iptal edildi");
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

