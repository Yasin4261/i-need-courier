package com.api.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private OrderPriority priority = OrderPriority.NORMAL;

    // İşletme bilgileri (sipariş veren)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "business_contact_person")
    private String businessContactPerson;

    @Column(name = "business_phone")
    private String businessPhone;

    // Kurye ve koordinatör bilgileri
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private Coordinator coordinator;

    // Son müşteri bilgileri (teslimat alacak kişi)
    @Column(name = "end_customer_name", nullable = false)
    private String endCustomerName;

    @Column(name = "end_customer_phone")
    private String endCustomerPhone;

    // Alım lokasyonu (işletmeden)
    @Column(name = "pickup_address", nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "pickup_address_description", columnDefinition = "TEXT")
    private String pickupAddressDescription;

    @Column(name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(name = "pickup_longitude")
    private Double pickupLongitude;

    @Column(name = "pickup_location_name")
    private String pickupLocationName;

    @Column(name = "pickup_contact_person")
    private String pickupContactPerson;

    // Teslimat lokasyonu (son müşteriye)
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_address_description", columnDefinition = "TEXT")
    private String deliveryAddressDescription;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @Column(name = "delivery_location_name")
    private String deliveryLocationName;

    // Paket bilgileri
    @Column(name = "package_description", nullable = false, length = 500)
    private String packageDescription;

    @Column(name = "package_weight")
    private Double packageWeight;

    @Column(name = "package_count")
    private Integer packageCount = 1;

    @Column(name = "package_value", precision = 10, scale = 2)
    private BigDecimal packageValue;

    // Ödeme bilgileri
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType = PaymentType.BUSINESS_ACCOUNT;

    @Column(name = "delivery_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal deliveryFee;

    @Column(name = "collection_amount", precision = 10, scale = 2)
    private BigDecimal collectionAmount = BigDecimal.ZERO;

    // Notlar ve dökümanlar
    @Column(name = "courier_notes", columnDefinition = "TEXT")
    private String courierNotes;

    @Column(name = "business_notes", columnDefinition = "TEXT")
    private String businessNotes;

    @Column(name = "receipt_image_url", length = 500)
    private String receiptImageUrl;

    @Column(name = "delivery_proof_url", length = 500)
    private String deliveryProofUrl;

    // Zaman bilgileri
    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "scheduled_pickup_time")
    private LocalDateTime scheduledPickupTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        orderDate = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = "ORD-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum OrderStatus {
        PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED
    }

    public enum OrderPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    public enum PaymentType {
        CASH, CARD, ONLINE, BUSINESS_ACCOUNT
    }

    // Constructors
    public Order() {}

    public Order(Business business, String endCustomerName, String pickupAddress, String deliveryAddress, String packageDescription, BigDecimal deliveryFee) {
        this.business = business;
        this.endCustomerName = endCustomerName;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.packageDescription = packageDescription;
        this.deliveryFee = deliveryFee;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public OrderPriority getPriority() { return priority; }
    public void setPriority(OrderPriority priority) { this.priority = priority; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public String getBusinessContactPerson() { return businessContactPerson; }
    public void setBusinessContactPerson(String businessContactPerson) { this.businessContactPerson = businessContactPerson; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public Courier getCourier() { return courier; }
    public void setCourier(Courier courier) { this.courier = courier; }

    public Coordinator getCoordinator() { return coordinator; }
    public void setCoordinator(Coordinator coordinator) { this.coordinator = coordinator; }

    public String getEndCustomerName() { return endCustomerName; }
    public void setEndCustomerName(String endCustomerName) { this.endCustomerName = endCustomerName; }

    public String getEndCustomerPhone() { return endCustomerPhone; }
    public void setEndCustomerPhone(String endCustomerPhone) { this.endCustomerPhone = endCustomerPhone; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getPickupAddressDescription() { return pickupAddressDescription; }
    public void setPickupAddressDescription(String pickupAddressDescription) { this.pickupAddressDescription = pickupAddressDescription; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryAddressDescription() { return deliveryAddressDescription; }
    public void setDeliveryAddressDescription(String deliveryAddressDescription) { this.deliveryAddressDescription = deliveryAddressDescription; }

    public String getPackageDescription() { return packageDescription; }
    public void setPackageDescription(String packageDescription) { this.packageDescription = packageDescription; }

    public Double getPackageWeight() { return packageWeight; }
    public void setPackageWeight(Double packageWeight) { this.packageWeight = packageWeight; }

    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getCollectionAmount() { return collectionAmount; }
    public void setCollectionAmount(BigDecimal collectionAmount) { this.collectionAmount = collectionAmount; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public String getCourierNotes() { return courierNotes; }
    public void setCourierNotes(String courierNotes) { this.courierNotes = courierNotes; }

    public String getBusinessNotes() { return businessNotes; }
    public void setBusinessNotes(String businessNotes) { this.businessNotes = businessNotes; }

    public String getReceiptImageUrl() { return receiptImageUrl; }
    public void setReceiptImageUrl(String receiptImageUrl) { this.receiptImageUrl = receiptImageUrl; }

    public String getDeliveryProofUrl() { return deliveryProofUrl; }
    public void setDeliveryProofUrl(String deliveryProofUrl) { this.deliveryProofUrl = deliveryProofUrl; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getScheduledPickupTime() { return scheduledPickupTime; }
    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) { this.scheduledPickupTime = scheduledPickupTime; }

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Additional getters for convenience
    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    public String getPickupLocationName() { return pickupLocationName; }
    public void setPickupLocationName(String pickupLocationName) { this.pickupLocationName = pickupLocationName; }

    public String getPickupContactPerson() { return pickupContactPerson; }
    public void setPickupContactPerson(String pickupContactPerson) { this.pickupContactPerson = pickupContactPerson; }

    public Double getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(Double deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }

    public Double getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(Double deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }

    public String getDeliveryLocationName() { return deliveryLocationName; }
    public void setDeliveryLocationName(String deliveryLocationName) { this.deliveryLocationName = deliveryLocationName; }

    public BigDecimal getPackageValue() { return packageValue; }
    public void setPackageValue(BigDecimal packageValue) { this.packageValue = packageValue; }
}
