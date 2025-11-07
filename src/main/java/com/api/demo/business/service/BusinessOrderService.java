package com.api.demo.business.service;

import com.api.demo.business.dto.OrderCreateRequest;
import com.api.demo.business.dto.OrderResponse;
import com.api.demo.business.dto.OrderUpdateRequest;
import com.api.demo.model.enums.OrderStatus;

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
    class OrderStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long assignedOrders;
        private long inTransitOrders;
        private long deliveredOrders;
        private long cancelledOrders;

        // Getters and Setters
        public long getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(long totalOrders) {
            this.totalOrders = totalOrders;
        }

        public long getPendingOrders() {
            return pendingOrders;
        }

        public void setPendingOrders(long pendingOrders) {
            this.pendingOrders = pendingOrders;
        }

        public long getAssignedOrders() {
            return assignedOrders;
        }

        public void setAssignedOrders(long assignedOrders) {
            this.assignedOrders = assignedOrders;
        }

        public long getInTransitOrders() {
            return inTransitOrders;
        }

        public void setInTransitOrders(long inTransitOrders) {
            this.inTransitOrders = inTransitOrders;
        }

        public long getDeliveredOrders() {
            return deliveredOrders;
        }

        public void setDeliveredOrders(long deliveredOrders) {
            this.deliveredOrders = deliveredOrders;
        }

        public long getCancelledOrders() {
            return cancelledOrders;
        }

        public void setCancelledOrders(long cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
        }
    }
}

