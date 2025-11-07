package com.api.demo.business.controller;

import com.api.demo.business.dto.OrderCreateRequest;
import com.api.demo.business.dto.OrderResponse;
import com.api.demo.business.dto.OrderUpdateRequest;
import com.api.demo.business.service.BusinessOrderService;
import com.api.demo.dto.ApiResponse;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class BusinessOrderController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessOrderController.class);

    private final BusinessOrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    public BusinessOrderController(BusinessOrderService orderService, JwtTokenProvider jwtTokenProvider) {
        this.orderService = orderService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Create a new order
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new delivery order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} creating new order", businessId);

        OrderResponse response = orderService.createOrder(request, businessId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }

    /**
     * Get all orders for the authenticated business
     */
    @GetMapping
    @Operation(summary = "Get all orders", description = "Get all orders for the authenticated business, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} fetching orders with status: {}", businessId, status);

        List<OrderResponse> orders = orderService.getAllOrders(businessId, status);

        String message = status != null
                ? "Orders with status " + status + " fetched successfully"
                : "All orders fetched successfully";

        return ResponseEntity.ok(ApiResponse.success(orders, message));
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Get detailed information about a specific order")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} fetching order {}", businessId, orderId);

        OrderResponse response = orderService.getOrderById(orderId, businessId);

        return ResponseEntity.ok(ApiResponse.success(response, "Order fetched successfully"));
    }

    /**
     * Update an existing order
     */
    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Update an existing order (only PENDING orders can be updated)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} updating order {}", businessId, orderId);

        OrderResponse response = orderService.updateOrder(orderId, request, businessId);

        return ResponseEntity.ok(ApiResponse.success(response, "Order updated successfully"));
    }

    /**
     * Delete an order
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order", description = "Delete an order (only PENDING orders can be deleted)")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} deleting order {}", businessId, orderId);

        orderService.deleteOrder(orderId, businessId);

        return ResponseEntity.ok(ApiResponse.success(null, "Order deleted successfully"));
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only PENDING/ASSIGNED orders can be cancelled)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} cancelling order {} with reason: {}", businessId, orderId, reason);

        OrderResponse response = orderService.cancelOrder(orderId, businessId, reason);

        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Get order statistics for the authenticated business")
    public ResponseEntity<ApiResponse<BusinessOrderService.OrderStatistics>> getStatistics(
            @RequestHeader("Authorization") String authHeader) {

        Long businessId = extractBusinessIdFromToken(authHeader);
        logger.info("Business {} fetching order statistics", businessId);

        BusinessOrderService.OrderStatistics stats = orderService.getOrderStatistics(businessId);

        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics fetched successfully"));
    }

    /**
     * Extract business ID from JWT token
     */
    private Long extractBusinessIdFromToken(String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}

