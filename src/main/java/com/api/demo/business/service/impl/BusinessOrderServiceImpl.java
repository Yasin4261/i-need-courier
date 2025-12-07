package com.api.demo.business.service.impl;

import com.api.demo.business.dto.OrderCreateRequest;
import com.api.demo.business.dto.OrderResponse;
import com.api.demo.business.dto.OrderUpdateRequest;
import com.api.demo.business.service.BusinessOrderService;
import com.api.demo.exception.InvalidOrderOperationException;
import com.api.demo.exception.OrderNotFoundException;
import com.api.demo.exception.UnauthorizedAccessException;
import com.api.demo.model.Business;
import com.api.demo.model.Courier;
import com.api.demo.model.Order;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.repository.BusinessRepository;
import com.api.demo.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BusinessOrderService
 */
@Service
@Transactional
@Slf4j
public class BusinessOrderServiceImpl implements BusinessOrderService {

    private final OrderRepository orderRepository;
    private final BusinessRepository businessRepository;

    public BusinessOrderServiceImpl(OrderRepository orderRepository, BusinessRepository businessRepository) {
        this.orderRepository = orderRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request, Long businessId) {
        log.info("Creating order for business ID: {}", businessId);

        // Get business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));

        // Verify business is active
        if (!business.canLogin()) {
            throw new InvalidOrderOperationException("Business account is not active. Cannot create orders.");
        }

        // Create order entity
        Order order = new Order();
        order.setBusiness(business);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(request.getPriority());

        // Set business contact info
        order.setBusinessContactPerson(business.getContactPerson());
        order.setBusinessPhone(business.getPhone());

        // Set pickup details
        order.setPickupAddress(request.getPickupAddress());
        order.setPickupAddressDescription(request.getPickupAddressDescription());
        order.setPickupContactPerson(request.getPickupContactPerson());

        // Set delivery details
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryAddressDescription(request.getDeliveryAddressDescription());

        // Set customer details
        order.setEndCustomerName(request.getEndCustomerName());
        order.setEndCustomerPhone(request.getEndCustomerPhone());

        // Set package details
        order.setPackageDescription(request.getPackageDescription());
        order.setPackageWeight(request.getPackageWeight());
        order.setPackageCount(request.getPackageCount());

        // Set payment details
        order.setPaymentType(request.getPaymentType());
        order.setDeliveryFee(request.getDeliveryFee());
        order.setCollectionAmount(request.getCollectionAmount());

        // Set notes and timestamps
        order.setBusinessNotes(request.getBusinessNotes());
        order.setScheduledPickupTime(request.getScheduledPickupTime());
        order.setOrderDate(LocalDateTime.now());

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long businessId) {
        log.info("Fetching order ID: {} for business ID: {}", orderId, businessId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify ownership
        verifyOrderOwnership(order, businessId);

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(Long businessId, OrderStatus status) {
        log.info("Fetching orders for business ID: {}, status: {}", businessId, status);

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByBusinessIdAndStatusOrderByPriorityAndCreatedAt(businessId, status);
        } else {
            orders = orderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        }

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request, Long businessId) {
        log.info("Updating order ID: {} for business ID: {}", orderId, businessId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify ownership
        verifyOrderOwnership(order, businessId);

        // Verify order is modifiable
        if (!order.isModifiable()) {
            throw new InvalidOrderOperationException(
                    "Cannot modify order in status: " + order.getStatus() + ". Only PENDING orders can be modified."
            );
        }

        // Update fields if provided
        if (request.getPickupAddress() != null) {
            order.setPickupAddress(request.getPickupAddress());
        }
        if (request.getPickupAddressDescription() != null) {
            order.setPickupAddressDescription(request.getPickupAddressDescription());
        }
        if (request.getPickupContactPerson() != null) {
            order.setPickupContactPerson(request.getPickupContactPerson());
        }
        if (request.getDeliveryAddress() != null) {
            order.setDeliveryAddress(request.getDeliveryAddress());
        }
        if (request.getDeliveryAddressDescription() != null) {
            order.setDeliveryAddressDescription(request.getDeliveryAddressDescription());
        }
        if (request.getEndCustomerName() != null) {
            order.setEndCustomerName(request.getEndCustomerName());
        }
        if (request.getEndCustomerPhone() != null) {
            order.setEndCustomerPhone(request.getEndCustomerPhone());
        }
        if (request.getPackageDescription() != null) {
            order.setPackageDescription(request.getPackageDescription());
        }
        if (request.getPackageWeight() != null) {
            order.setPackageWeight(request.getPackageWeight());
        }
        if (request.getPackageCount() != null) {
            order.setPackageCount(request.getPackageCount());
        }
        if (request.getPriority() != null) {
            order.setPriority(request.getPriority());
        }
        if (request.getPaymentType() != null) {
            order.setPaymentType(request.getPaymentType());
        }
        if (request.getDeliveryFee() != null) {
            order.setDeliveryFee(request.getDeliveryFee());
        }
        if (request.getCollectionAmount() != null) {
            order.setCollectionAmount(request.getCollectionAmount());
        }
        if (request.getBusinessNotes() != null) {
            order.setBusinessNotes(request.getBusinessNotes());
        }
        if (request.getScheduledPickupTime() != null) {
            order.setScheduledPickupTime(request.getScheduledPickupTime());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", orderId);

        return mapToResponse(updatedOrder);
    }

    @Override
    public void deleteOrder(Long orderId, Long businessId) {
        log.info("Deleting order ID: {} for business ID: {}", orderId, businessId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify ownership
        verifyOrderOwnership(order, businessId);

        // Verify order is deletable (only PENDING)
        if (!order.isPending()) {
            throw new InvalidOrderOperationException(
                    "Cannot delete order in status: " + order.getStatus() + ". Only PENDING orders can be deleted."
            );
        }

        orderRepository.delete(order);
        log.info("Order deleted successfully: {}", orderId);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long businessId, String reason) {
        log.info("Cancelling order ID: {} for business ID: {}, reason: {}", orderId, businessId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Verify ownership
        verifyOrderOwnership(order, businessId);

        // Verify order is cancellable
        if (!order.isCancellable()) {
            throw new InvalidOrderOperationException(
                    "Cannot cancel order in status: " + order.getStatus() +
                            ". Only PENDING or ASSIGNED orders can be cancelled."
            );
        }

        // Update status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);

        // Add cancellation note
        String cancellationNote = "Cancelled by business. Reason: " + (reason != null ? reason : "Not specified");
        String existingNotes = order.getBusinessNotes();
        if (existingNotes != null && !existingNotes.isEmpty()) {
            order.setBusinessNotes(existingNotes + "\n" + cancellationNote);
        } else {
            order.setBusinessNotes(cancellationNote);
        }

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", orderId);

        return mapToResponse(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatistics(Long businessId) {
        log.info("Fetching order statistics for business ID: {}", businessId);

        OrderStatistics stats = new OrderStatistics();
        stats.setTotalOrders(orderRepository.countByBusinessId(businessId));
        stats.setPendingOrders(orderRepository.countByBusinessIdAndStatus(businessId, OrderStatus.PENDING));
        stats.setAssignedOrders(orderRepository.countByBusinessIdAndStatus(businessId, OrderStatus.ASSIGNED));
        stats.setInTransitOrders(orderRepository.countByBusinessIdAndStatus(businessId, OrderStatus.IN_TRANSIT));
        stats.setDeliveredOrders(orderRepository.countByBusinessIdAndStatus(businessId, OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByBusinessIdAndStatus(businessId, OrderStatus.CANCELLED));

        return stats;
    }

    /**
     * Verify that the order belongs to the requesting business
     */
    private void verifyOrderOwnership(Order order, Long businessId) {
        if (!order.belongsTo(businessId)) {
            log.warn("Business ID: {} attempted to access order ID: {} belonging to business ID: {}",
                    businessId, order.getId(), order.getBusiness().getId());
            throw new UnauthorizedAccessException("You can only access your own orders");
        }
    }

    /**
     * Generate unique order number
     * Format: ORD-YYYYMMDD-XXX
     */
    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNumber;
        int attempt = 0;
        do {
            attempt++;
            orderNumber = String.format("ORD-%s-%03d", datePrefix, attempt);
        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setPriority(order.getPriority());

        // Business info
        if (order.getBusiness() != null) {
            response.setBusinessId(order.getBusiness().getId());
            response.setBusinessName(order.getBusiness().getName());
        }
        response.setBusinessContactPerson(order.getBusinessContactPerson());
        response.setBusinessPhone(order.getBusinessPhone());

        // Courier info
        if (order.getCourier() != null) {
            Courier courier = order.getCourier();
            response.setCourierId(courier.getId());
            response.setCourierName(courier.getName());
            response.setCourierPhone(courier.getPhone());
        }

        // Addresses
        response.setPickupAddress(order.getPickupAddress());
        response.setPickupAddressDescription(order.getPickupAddressDescription());
        response.setPickupContactPerson(order.getPickupContactPerson());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryAddressDescription(order.getDeliveryAddressDescription());

        // Customer
        response.setEndCustomerName(order.getEndCustomerName());
        response.setEndCustomerPhone(order.getEndCustomerPhone());

        // Package
        response.setPackageDescription(order.getPackageDescription());
        response.setPackageWeight(order.getPackageWeight());
        response.setPackageCount(order.getPackageCount());

        // Payment
        response.setPaymentType(order.getPaymentType());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setCollectionAmount(order.getCollectionAmount());

        // Notes
        response.setBusinessNotes(order.getBusinessNotes());
        response.setCourierNotes(order.getCourierNotes());

        // Timestamps
        response.setScheduledPickupTime(order.getScheduledPickupTime());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setOrderDate(order.getOrderDate());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }
}

