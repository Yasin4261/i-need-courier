package com.api.demo.controller;

import com.api.demo.dto.ApiResponse;
import com.api.demo.exception.InvalidOrderOperationException;
import com.api.demo.exception.OrderNotFoundException;
import com.api.demo.exception.UnauthorizedAccessException;
import com.api.demo.model.Order;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/courier/orders")
public class CourierOrderController {


    private final OrderRepository orderRepository;

    public CourierOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        Long courierId = extractCourierId(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        // Verify courier owns this order
        if (order.getCourier() == null || !order.getCourier().getId().equals(courierId)) {
            throw new RuntimeException("Bu sipariş size atanmamış");
        }

        return ResponseEntity.ok(ApiResponse.success(order, "Sipariş detayları"));
    }

    /**
     * Pickup order (mark as PICKED_UP)
     */
    @PostMapping(value = "/{orderId}/pickup", consumes = {"*/*"})
    public ResponseEntity<ApiResponse<Order>> pickupOrder(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam(required = false) String notes) {

        Long courierId = extractCourierId(authentication);
        log.info("Pickup request - Courier: {}, Order: {}, Notes: {}", courierId, orderId, notes);

        // Order'ı bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Pickup failed - Order {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("Order found - ID: {}, Status: {}, CourierId: {}",
                    order.getId(), order.getStatus(),
                    order.getCourier() != null ? order.getCourier().getId() : "NULL");

        // Courier kontrolü (detaylı logging)
        if (order.getCourier() == null) {
            log.error("Pickup failed - Order {} has no courier assigned! Order status: {}",
                        orderId, order.getStatus());
            throw new UnauthorizedAccessException(
                "Bu sipariş henüz bir kuryeye atanmamış. Lütfen önce siparişi kabul edin."
            );
        }

        if (!order.getCourier().getId().equals(courierId)) {
            log.error("Pickup failed - Order {} belongs to courier {} but requested by courier {}",
                        orderId, order.getCourier().getId(), courierId);
            throw new UnauthorizedAccessException(
                String.format("Bu sipariş size ait değil. Sipariş kurye %d'ye atanmış.",
                             order.getCourier().getId())
            );
        }

        // Status kontrolü
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            log.error("Pickup failed - Order {} has invalid status: {}. Expected: ASSIGNED",
                        orderId, order.getStatus());
            throw new InvalidOrderOperationException(
                String.format("Bu sipariş pickup yapılamaz. Mevcut durum: %s, Beklenen: ASSIGNED",
                             order.getStatus())
            );
        }

        // Pickup yap
        order.setStatus(OrderStatus.PICKED_UP);
        order.setUpdatedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            order.setCourierNotes(notes);
        }

        orderRepository.save(order);
        log.info("Pickup successful - Order {} picked up by courier {}", orderId, courierId);

        return ResponseEntity.ok(ApiResponse.success(order, "Sipariş alındı (PICKED_UP)"));
    }

    /**
     * Start delivery (mark as IN_TRANSIT)
     */
    @PostMapping("/{orderId}/start-delivery")
    public ResponseEntity<ApiResponse<Order>> startDelivery(
            Authentication authentication,
            @PathVariable Long orderId) {

        Long courierId = extractCourierId(authentication);
        log.info("Start delivery request - Courier: {}, Order: {}", courierId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Start delivery failed - Order {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("Order found - ID: {}, Status: {}, CourierId: {}",
                    order.getId(), order.getStatus(),
                    order.getCourier() != null ? order.getCourier().getId() : "NULL");

        if (order.getCourier() == null || !order.getCourier().getId().equals(courierId)) {
            log.error("Start delivery failed - Order {} courier mismatch. Expected: {}, Got: {}",
                        orderId, order.getCourier() != null ? order.getCourier().getId() : "NULL", courierId);
            throw new UnauthorizedAccessException("Bu sipariş size ait değil");
        }

        if (order.getStatus() != OrderStatus.PICKED_UP) {
            log.error("Start delivery failed - Order {} invalid status: {}. Expected: PICKED_UP",
                        orderId, order.getStatus());
            throw new InvalidOrderOperationException(
                String.format("Teslimat başlatılamaz. Mevcut durum: %s, Beklenen: PICKED_UP",
                             order.getStatus())
            );
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Start delivery successful - Order {} now IN_TRANSIT by courier {}", orderId, courierId);

        return ResponseEntity.ok(ApiResponse.success(order, "Teslimat başladı (IN_TRANSIT)"));
    }

    /**
     * Complete delivery (mark as DELIVERED)
     */
    @PostMapping(value = "/{orderId}/complete", consumes = {"*/*"})
    public ResponseEntity<ApiResponse<Order>> completeDelivery(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Double collectionAmount) {

        Long courierId = extractCourierId(authentication);
        log.info("Complete delivery request - Courier: {}, Order: {}, Notes: {}, Amount: {}",
                   courierId, orderId, notes, collectionAmount);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Complete delivery failed - Order {} not found", orderId);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("Order found - ID: {}, Status: {}, CourierId: {}",
                    order.getId(), order.getStatus(),
                    order.getCourier() != null ? order.getCourier().getId() : "NULL");

        if (order.getCourier() == null || !order.getCourier().getId().equals(courierId)) {
            log.error("Complete delivery failed - Order {} courier mismatch. Expected: {}, Got: {}",
                        orderId, order.getCourier() != null ? order.getCourier().getId() : "NULL", courierId);
            throw new UnauthorizedAccessException("Bu sipariş size ait değil");
        }

        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            log.error("Complete delivery failed - Order {} invalid status: {}. Expected: IN_TRANSIT",
                        orderId, order.getStatus());
            throw new InvalidOrderOperationException(
                String.format("Teslimat tamamlanamaz. Mevcut durum: %s, Beklenen: IN_TRANSIT",
                             order.getStatus())
            );
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());

        if (notes != null && !notes.isBlank()) {
            order.setCourierNotes(notes);
            log.debug("Added courier notes to order {}", orderId);
        }

        if (collectionAmount != null) {
            order.setCollectionAmount(java.math.BigDecimal.valueOf(collectionAmount));
            log.debug("Set collection amount {} for order {}", collectionAmount, orderId);
        }

        orderRepository.save(order);
        log.info("Complete delivery successful - Order {} delivered by courier {}", orderId, courierId);

        return ResponseEntity.ok(ApiResponse.success(order, "Sipariş teslim edildi (DELIVERED)"));
    }

    private Long extractCourierId(Authentication authentication) {
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

