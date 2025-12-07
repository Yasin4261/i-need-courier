package com.api.demo.model;

import com.api.demo.model.enums.OrderPriority;
import com.api.demo.model.enums.OrderStatus;
import com.api.demo.model.enums.PaymentType;
import jakarta.persistence.*;
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
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
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

    // Constructors
    public Order() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderPriority getPriority() {
        return priority;
    }

    public void setPriority(OrderPriority priority) {
        this.priority = priority;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public String getBusinessContactPerson() {
        return businessContactPerson;
    }

    public void setBusinessContactPerson(String businessContactPerson) {
        this.businessContactPerson = businessContactPerson;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getEndCustomerName() {
        return endCustomerName;
    }

    public void setEndCustomerName(String endCustomerName) {
        this.endCustomerName = endCustomerName;
    }

    public String getEndCustomerPhone() {
        return endCustomerPhone;
    }

    public void setEndCustomerPhone(String endCustomerPhone) {
        this.endCustomerPhone = endCustomerPhone;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupAddressDescription() {
        return pickupAddressDescription;
    }

    public void setPickupAddressDescription(String pickupAddressDescription) {
        this.pickupAddressDescription = pickupAddressDescription;
    }

    public String getPickupContactPerson() {
        return pickupContactPerson;
    }

    public void setPickupContactPerson(String pickupContactPerson) {
        this.pickupContactPerson = pickupContactPerson;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryAddressDescription() {
        return deliveryAddressDescription;
    }

    public void setDeliveryAddressDescription(String deliveryAddressDescription) {
        this.deliveryAddressDescription = deliveryAddressDescription;
    }

    public String getPackageDescription() {
        return packageDescription;
    }

    public void setPackageDescription(String packageDescription) {
        this.packageDescription = packageDescription;
    }

    public BigDecimal getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(BigDecimal packageWeight) {
        this.packageWeight = packageWeight;
    }

    public Integer getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(Integer packageCount) {
        this.packageCount = packageCount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getCollectionAmount() {
        return collectionAmount;
    }

    public void setCollectionAmount(BigDecimal collectionAmount) {
        this.collectionAmount = collectionAmount;
    }

    public String getCourierNotes() {
        return courierNotes;
    }

    public void setCourierNotes(String courierNotes) {
        this.courierNotes = courierNotes;
    }

    public String getBusinessNotes() {
        return businessNotes;
    }

    public void setBusinessNotes(String businessNotes) {
        this.businessNotes = businessNotes;
    }

    public LocalDateTime getScheduledPickupTime() {
        return scheduledPickupTime;
    }

    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) {
        this.scheduledPickupTime = scheduledPickupTime;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

