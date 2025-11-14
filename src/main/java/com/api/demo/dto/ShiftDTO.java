package com.api.demo.dto;

import com.api.demo.model.enums.ShiftRole;
import com.api.demo.model.enums.ShiftStatus;
import java.time.LocalDateTime;

/**
 * Shift DTO - Vardiya bilgileri için transfer objesi
 */
public class ShiftDTO {
    private Long shiftId;
    private Long courierId;
    private String courierName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ShiftRole shiftRole;
    private ShiftStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public ShiftDTO() {
    }

    public ShiftDTO(Long shiftId, Long courierId, LocalDateTime startTime, LocalDateTime endTime,
                    ShiftRole shiftRole, ShiftStatus status) {
        this.shiftId = shiftId;
        this.courierId = courierId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftRole = shiftRole;
        this.status = status;
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

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
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
}

