package com.api.pako.business.service;

import com.api.pako.business.dto.OrderCreateRequest;
import com.api.pako.business.dto.OrderResponse;
import com.api.pako.business.dto.OrderUpdateRequest;
import com.api.pako.model.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Service interface for business order operations
 */
public interface BusinessOrderService {

    /**
     * Create a new order
     *
     * @param request Order creation data
     * @param businessId ID of the business creating the order
     * @return Created order response
     */
    OrderResponse createOrder(OrderCreateRequest request, Long businessId);

    /**
     * Get order by ID
     * Only allows business to view their own orders
     *
     * @param orderId Order ID
     * @param businessId Business ID making the request
     * @return Order response
     */
    OrderResponse getOrderById(Long orderId, Long businessId);

    /**
     * Get all orders for a business
     *
     * @param businessId Business ID
     * @param status Optional status filter
     * @return List of orders
     */
    List<OrderResponse> getAllOrders(Long businessId, OrderStatus status);

    /**
     * Update an existing order
     * Only allows updating orders in PENDING status
     *
     * @param orderId Order ID to update
     * @param request Update data
     * @param businessId Business ID making the request
     * @return Updated order response
     */
    OrderResponse updateOrder(Long orderId, OrderUpdateRequest request, Long businessId);

    /**
     * Delete an order
     * Only allows deleting orders in PENDING status
     *
     * @param orderId Order ID to delete
     * @param businessId Business ID making the request
     */
    void deleteOrder(Long orderId, Long businessId);

    /**
     * Cancel an order
     * Only allows cancelling orders in PENDING or ASSIGNED status
     *
     * @param orderId Order ID to cancel
     * @param businessId Business ID making the request
     * @param reason Cancellation reason (optional)
     * @return Cancelled order response
     */
    OrderResponse cancelOrder(Long orderId, Long businessId, String reason);

    /**
     * Get order statistics for a business
     *
     * @param businessId Business ID
     * @return Order statistics
     */
    OrderStatistics getOrderStatistics(Long businessId);

    /**
     * Inner class for order statistics
     */
    @Setter
    @Getter
    class OrderStatistics {
        // Getters and Setters
        private long totalOrders;
        private long pendingOrders;
        private long assignedOrders;
        private long inTransitOrders;
        private long deliveredOrders;
        private long cancelledOrders;
    }
}

