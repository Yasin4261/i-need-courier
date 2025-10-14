package com.api.demo.domain.model;

import com.api.demo.domain.valueobject.Email;
import com.api.demo.domain.valueobject.Password;
import com.api.demo.domain.valueobject.Phone;

import java.time.LocalDateTime;

public class Courier {
    private Long id;
    private String name;
    private final Email email;
    private Phone phone;
    private final Password password;
    private CourierStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Constructor for new courier
    public Courier(String name, Email email, Phone phone, Password password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = CourierStatus.INACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for existing courier (from database)
    public Courier(Long id, String name, Email email, Phone phone, Password password,
                   CourierStatus status, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public void activate() {
        this.status = CourierStatus.ONLINE;
    }

    public void deactivate() {
        this.status = CourierStatus.OFFLINE;
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == CourierStatus.ONLINE;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public Email getEmail() { return email; }
    public Phone getPhone() { return phone; }
    public Password getPassword() { return password; }
    public CourierStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    // Setters for updates
    public void setName(String name) { this.name = name; }
    public void setPhone(Phone phone) { this.phone = phone; }
    public void setStatus(CourierStatus status) { this.status = status; }
}
