package com.api.demo.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Business entity representing a company that places delivery orders.
 */
@Entity
@Table(name = "businesses")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "business_code", unique = true, length = 50)
    private String businessCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(name = "address_description", columnDefinition = "TEXT")
    private String addressDescription;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms")
    private PaymentTerms paymentTerms = PaymentTerms.POSTPAID;

    @Column(name = "credit_limit", precision = 10, scale = 2, nullable = false)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BusinessStatus status = BusinessStatus.PENDING;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum PaymentTerms {
        PREPAID,
        POSTPAID,
        CASH_ON_DELIVERY
    }

    public enum BusinessStatus {
        PENDING,    // Awaiting approval
        ACTIVE,     // Can login and use system
        SUSPENDED,  // Temporarily blocked
        INACTIVE    // Deactivated
    }

    // Constructors
    public Business() {
    }

    public Business(String name, String email, String phone, String passwordHash) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = BusinessStatus.PENDING;
        this.isActive = true;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean canLogin() {
        return this.status == BusinessStatus.ACTIVE && this.isActive;
    }

    public void activate() {
        this.status = BusinessStatus.ACTIVE;
        this.isActive = true;
    }

    public void suspend() {
        this.status = BusinessStatus.SUSPENDED;
    }

    public void deactivate() {
        this.status = BusinessStatus.INACTIVE;
        this.isActive = false;
    }


}

