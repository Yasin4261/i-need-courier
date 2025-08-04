package com.api.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_code", unique = true, nullable = false)
    private String businessCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "address_description", columnDefinition = "TEXT")
    private String addressDescription;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms")
    private PaymentTerms paymentTerms = PaymentTerms.POSTPAID;

    @Column(name = "credit_limit", precision = 10, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (businessCode == null) {
            businessCode = "BUS" + String.format("%06d", System.currentTimeMillis() % 1000000);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum PaymentTerms {
        PREPAID,        // Ön ödeme
        POSTPAID,       // Vadeli ödeme
        CASH_ON_DELIVERY // Kapıda tahsilat
    }

    // Constructors
    public Business() {}

    public Business(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessCode() { return businessCode; }
    public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAddressDescription() { return addressDescription; }
    public void setAddressDescription(String addressDescription) { this.addressDescription = addressDescription; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public PaymentTerms getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(PaymentTerms paymentTerms) { this.paymentTerms = paymentTerms; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Set<Order> getOrders() { return orders; }
    public void setOrders(Set<Order> orders) { this.orders = orders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
