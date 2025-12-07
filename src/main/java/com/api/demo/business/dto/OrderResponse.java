package com.api.demo.business.dto;

import com.api.demo.model.enums.OrderPriority;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.model.enums.PaymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order response
 */
@Setter
@Getter
@NoArgsConstructor
public class OrderResponse {

    // Getters and Setters
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private OrderPriority priority;

    // Business information
    private Long businessId;
    private String businessName;
    private String businessContactPerson;
    private String businessPhone;

    // Courier information (nullable if not assigned)
    private Long courierId;
    private String courierName;
    private String courierPhone;

    // Pickup details
    private String pickupAddress;
    private String pickupAddressDescription;
    private String pickupContactPerson;

    // Delivery details
    private String deliveryAddress;
    private String deliveryAddressDescription;

    // Customer information
    private String endCustomerName;
    private String endCustomerPhone;

    // Package details
    private String packageDescription;
    private BigDecimal packageWeight;
    private Integer packageCount;

    // Payment details
    private PaymentType paymentType;
    private BigDecimal deliveryFee;
    private BigDecimal collectionAmount;

    // Notes
    private String businessNotes;
    private String courierNotes;

    // Timestamps
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

