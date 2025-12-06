package com.api.demo.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "on_duty_couriers")
public class OnDutyCourier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_id", nullable = false, unique = true)
    private Long courierId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "on_duty_since", nullable = false)
    private OffsetDateTime onDutySince;

    @Column(name = "source", nullable = false)
    private String source = "app";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Constructors
    public OnDutyCourier() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public OffsetDateTime getOnDutySince() {
        return onDutySince;
    }

    public void setOnDutySince(OffsetDateTime onDutySince) {
        this.onDutySince = onDutySince;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "OnDutyCourier{" +
                "id=" + id +
                ", courierId=" + courierId +
                ", shiftId=" + shiftId +
                ", onDutySince=" + onDutySince +
                ", source='" + source + '\'' +
                '}';
    }
}

