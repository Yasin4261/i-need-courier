package com.api.demo.controller;

import com.api.demo.dto.ApiResponse;
import com.api.demo.model.OrderAssignment;
import com.api.demo.service.OrderAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/courier/assignments")
public class CourierAssignmentController {

    private final OrderAssignmentService orderAssignmentService;

    public CourierAssignmentController(OrderAssignmentService orderAssignmentService) {
        this.orderAssignmentService = orderAssignmentService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingAssignments(
            Authentication authentication) {

        Long courierId = extractCourierId(authentication);
        List<OrderAssignment> assignments = orderAssignmentService.getPendingAssignments(courierId);

        List<Map<String, Object>> response = assignments.stream().map(assignment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("assignmentId", assignment.getId());
            map.put("orderId", assignment.getOrderId());
            map.put("assignedAt", assignment.getAssignedAt());
            map.put("timeoutAt", assignment.getTimeoutAt());
            map.put("status", assignment.getStatus().toString());

            // Kalan süre bilgisi ekle (saniye cinsinden)
            if (assignment.getTimeoutAt() != null) {
                java.time.OffsetDateTime now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
                long remainingSeconds = java.time.Duration.between(now, assignment.getTimeoutAt()).getSeconds();
                map.put("remainingSeconds", Math.max(0, remainingSeconds));
                map.put("timeoutWarning", remainingSeconds < 60 ? "Süre dolmak üzere!" : null);
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Bekleyen atamalar"));
    }

    @PostMapping("/{assignmentId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptAssignment(
            Authentication authentication,
            @PathVariable Long assignmentId) {

        Long courierId = extractCourierId(authentication);
        orderAssignmentService.acceptAssignment(assignmentId, courierId);

        return ResponseEntity.ok(ApiResponse.success(null, "Sipariş kabul edildi"));
    }

    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAssignment(
            Authentication authentication,
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> request) {

        Long courierId = extractCourierId(authentication);
        String reason = request.getOrDefault("reason", "Belirtilmedi");

        orderAssignmentService.rejectAssignment(assignmentId, courierId, reason);

        return ResponseEntity.ok(ApiResponse.success(null, "Sipariş reddedildi, başka kuryeye atanıyor"));
    }

    private Long extractCourierId(Authentication authentication) {
        // Principal is userId (Long) set by JwtAuthenticationFilter
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }

        throw new IllegalStateException("Expected principal to be userId (Long), but got: " +
                                       (principal != null ? principal.getClass() : "null"));
    }
}

