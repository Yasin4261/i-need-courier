package com.api.pako.business.controller;

import com.api.pako.business.dto.OrderCreateRequest;
import com.api.pako.business.dto.OrderResponse;
import com.api.pako.business.dto.OrderUpdateRequest;
import com.api.pako.business.service.BusinessOrderService;
import com.api.pako.dto.ApiResponse;
import com.api.pako.model.enums.OrderStatus;
import com.api.pako.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for business order operations
 * Allows businesses to manage their delivery orders
 */
@RestController
@RequestMapping("/api/v1/business/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Business Orders", description = "Business order management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class BusinessOrderController {

    private final BusinessOrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.api.pako.service.OrderAssignmentService orderAssignmentService;

    /**
     * Create a new order
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new delivery order")
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        var businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} creating new order", businessId);

        var response = orderService.createOrder(request, businessId);

        // Auto-assign to next available courier (FIFO)
        try {
            orderAssignmentService.assignToNextAvailableCourier(response.getOrderId());
            log.info("Order {} auto-assigned to courier via FIFO", response.getOrderId());
        } catch (Exception e) {
            log.warn("Failed to auto-assign order {}: {}", response.getOrderId(), e.getMessage());
            // Order is still created, just not assigned yet
        }

        return ApiResponse.created(response, "Order created successfully");
    }

    /**
     * Get all orders for the authenticated business
     */
    @GetMapping
    @Operation(summary = "Get all orders", description = "Get all orders for the authenticated business, optionally filtered by status")
    public ApiResponse<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestHeader("Authorization") String authHeader) {

        var businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} fetching orders with status: {}", businessId, status);

        var orders = orderService.getAllOrders(businessId, status);
        var message = status != null
                ? "Orders with status " + status + " fetched successfully"
                : "All orders fetched successfully";

        return ApiResponse.ok(orders, message);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Get detailed information about a specific order")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} fetching order {}", businessId, orderId);

        OrderResponse response = orderService.getOrderById(orderId, businessId);

        return ApiResponse.ok(response, "Order fetched successfully");
    }

    /**
     * Update an existing order
     */
    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Update an existing order (only PENDING orders can be updated)")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} updating order {}", businessId, orderId);

        OrderResponse response = orderService.updateOrder(orderId, request, businessId);

        return ApiResponse.ok(response, "Order updated successfully");
    }

    /**
     * Delete an order
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order", description = "Delete an order (only PENDING orders can be deleted)")
    public ApiResponse<Void> deleteOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} deleting order {}", businessId, orderId);

        orderService.deleteOrder(orderId, businessId);

        return ApiResponse.deleted("Order deleted successfully");
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only PENDING/ASSIGNED orders can be cancelled)")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} cancelling order {} with reason: {}", businessId, orderId, reason);

        OrderResponse response = orderService.cancelOrder(orderId, businessId, reason);

        return ApiResponse.ok(response, "Order cancelled successfully");
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Get order statistics for the authenticated business")
    public ApiResponse<BusinessOrderService.OrderStatistics> getStatistics(
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        log.info("Business {} fetching order statistics", businessId);

        BusinessOrderService.OrderStatistics stats = orderService.getOrderStatistics(businessId);

        return ApiResponse.ok(stats, "Statistics fetched successfully");
    }

    /**
     * Extract business ID from a JWT token
     */
    private Long extractBusinessIdFromToken(String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}

