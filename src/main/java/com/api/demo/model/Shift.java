package com.api.demo.model;

import com.api.demo.model.enums.ShiftRole;
import com.api.demo.model.enums.ShiftStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Shift entity - Kuryelerin vardiya kayıtları
 * Dökümantasyondaki shifts tablosunu temsil eder
 */
@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_role", nullable = false, length = 50)
    private ShiftRole shiftRole = ShiftRole.COURIER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShiftStatus status = ShiftStatus.RESERVED;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Shift() {
    }

    public Shift(Long courierId, LocalDateTime startTime, LocalDateTime endTime, ShiftRole shiftRole) {
        this.courierId = courierId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftRole = shiftRole;
        this.status = ShiftStatus.RESERVED;
    }

    // Getters and Setters
    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ShiftRole getShiftRole() {
        return shiftRole;
    }

    public void setShiftRole(ShiftRole shiftRole) {
        this.shiftRole = shiftRole;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Shift{" +
                "shiftId=" + shiftId +
                ", courierId=" + courierId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", shiftRole=" + shiftRole +
                ", status=" + status +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", createdAt=" + createdAt +
                '}';
    }
}

