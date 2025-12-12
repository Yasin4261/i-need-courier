package com.api.demo.model;

import com.api.demo.model.enums.OrderPriority;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity representing delivery orders in the system
 * Maps to 'orders' table in database
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false)
    private OrderPriority priority = OrderPriority.NORMAL;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    @ToString.Exclude
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    @ToString.Exclude
    private Courier courier;

    // Business contact info
    @Column(name = "business_contact_person", length = 100)
    private String businessContactPerson;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    // End customer info
    @Column(name = "end_customer_name", length = 100)
    private String endCustomerName;

    @Column(name = "end_customer_phone", length = 20)
    private String endCustomerPhone;

    // Pickup details
    @Column(name = "pickup_address", nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "pickup_address_description", columnDefinition = "TEXT")
    private String pickupAddressDescription;

    @Column(name = "pickup_contact_person", length = 100)
    private String pickupContactPerson;

    // Delivery details
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_address_description", columnDefinition = "TEXT")
    private String deliveryAddressDescription;

    // Package details
    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    @Column(name = "package_weight", precision = 8, scale = 2)
    private BigDecimal packageWeight;

    @Column(name = "package_count")
    private Integer packageCount = 1;

    // Payment details
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_type")
    private PaymentType paymentType = PaymentType.CASH;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "collection_amount", precision = 10, scale = 2)
    private BigDecimal collectionAmount = BigDecimal.ZERO;

    // Notes
    @Column(name = "courier_notes", columnDefinition = "TEXT")
    private String courierNotes;

    @Column(name = "business_notes", columnDefinition = "TEXT")
    private String businessNotes;

    // Timestamps
    @Column(name = "scheduled_pickup_time")
    private LocalDateTime scheduledPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isModifiable() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.ASSIGNED;
    }

    public boolean belongsTo(Long businessId) {
        return this.business != null && this.business.getId().equals(businessId);
    }
}
