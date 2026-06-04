package com.api.pako.dto;

import com.api.pako.model.Order;
import com.api.pako.model.enums.OrderPriority;
import com.api.pako.model.enums.OrderStatus;
import com.api.pako.model.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Courier-facing view of an {@link Order}.
 * <p>
 * Returned instead of the raw JPA entity so the API does not leak the entity
 * graph (e.g. lazy relations / credential fields) and is safe to serialize
 * outside an open transaction.
 */
@Getter
@Builder
public class CourierOrderResponse {

    private String orderNumber;
    private OrderStatus status;
    private OrderPriority priority;

    // Business information
    private String businessName;
    private String businessPhone;

    // Pickup details
    private String pickupAddress;
    private String pickupAddressDescription;
    private String pickupContactPerson;
    private Double pickupLatitude;
    private Double pickupLongitude;

    // Delivery details
    private String deliveryAddress;
    private String deliveryAddressDescription;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

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

    public static CourierOrderResponse from(Order order) {
        return CourierOrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .priority(order.getPriority())
                .businessName(order.getBusiness() != null ? order.getBusiness().getName() : null)
                .businessPhone(order.getBusinessPhone())
                .pickupAddress(order.getPickupAddress())
                .pickupAddressDescription(order.getPickupAddressDescription())
                .pickupContactPerson(order.getPickupContactPerson())
                .pickupLatitude(order.getPickupLatitude())
                .pickupLongitude(order.getPickupLongitude())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryAddressDescription(order.getDeliveryAddressDescription())
                .deliveryLatitude(order.getDeliveryLatitude())
                .deliveryLongitude(order.getDeliveryLongitude())
                .endCustomerName(order.getEndCustomerName())
                .endCustomerPhone(order.getEndCustomerPhone())
                .packageDescription(order.getPackageDescription())
                .packageWeight(order.getPackageWeight())
                .packageCount(order.getPackageCount())
                .paymentType(order.getPaymentType())
                .deliveryFee(order.getDeliveryFee())
                .collectionAmount(order.getCollectionAmount())
                .businessNotes(order.getBusinessNotes())
                .courierNotes(order.getCourierNotes())
                .scheduledPickupTime(order.getScheduledPickupTime())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
