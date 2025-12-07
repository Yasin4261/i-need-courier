package com.api.pako.business.dto;

import com.api.pako.model.enums.OrderPriority;
import com.api.pako.model.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for updating an existing order
 * Only allows updating specific fields when order is in PENDING status
 */
@Setter
@Getter
@NoArgsConstructor
public class OrderUpdateRequest {

    // Getters and Setters
    @Size(min = 10, max = 500, message = "Pickup address must be between 10 and 500 characters")
    private String pickupAddress;

    @Size(max = 500, message = "Pickup address description cannot exceed 500 characters")
    private String pickupAddressDescription;

    @Size(max = 100, message = "Pickup contact person name cannot exceed 100 characters")
    private String pickupContactPerson;

    @Size(min = 10, max = 500, message = "Delivery address must be between 10 and 500 characters")
    private String deliveryAddress;

    @Size(max = 500, message = "Delivery address description cannot exceed 500 characters")
    private String deliveryAddressDescription;

    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String endCustomerName;

    @Pattern(regexp = "^\\+90\\d{10}$", message = "Phone must be in format: +90XXXXXXXXXX")
    private String endCustomerPhone;

    @Size(max = 1000, message = "Package description cannot exceed 1000 characters")
    private String packageDescription;

    @Positive(message = "Package weight must be positive")
    @DecimalMax(value = "1000.00", message = "Package weight cannot exceed 1000kg")
    private BigDecimal packageWeight;

    @Min(value = 1, message = "Package count must be at least 1")
    @Max(value = 100, message = "Package count cannot exceed 100")
    private Integer packageCount;

    private OrderPriority priority;

    private PaymentType paymentType;

    @Positive(message = "Delivery fee must be positive")
    @DecimalMax(value = "10000.00", message = "Delivery fee cannot exceed 10,000")
    private BigDecimal deliveryFee;

    @PositiveOrZero(message = "Collection amount must be positive or zero")
    @DecimalMax(value = "100000.00", message = "Collection amount cannot exceed 100,000")
    private BigDecimal collectionAmount;

    @Size(max = 1000, message = "Business notes cannot exceed 1000 characters")
    private String businessNotes;

    private LocalDateTime scheduledPickupTime;

}

