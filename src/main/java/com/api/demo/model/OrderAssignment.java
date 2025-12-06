package com.api.demo.model;

import com.api.demo.model.enums.AssignmentStatus;
import com.api.demo.model.enums.AssignmentType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_assignments")
public class OrderAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "response_at")
    private OffsetDateTime responseAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private AssignmentType assignmentType;

    @Column(name = "timeout_at")
    private OffsetDateTime timeoutAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Constructors
    public OrderAssignment() {
        this.assignedAt = OffsetDateTime.now();
        this.status = AssignmentStatus.PENDING;
        this.assignmentType = AssignmentType.AUTO;
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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public OffsetDateTime getResponseAt() {
        return responseAt;
    }

    public void setResponseAt(OffsetDateTime responseAt) {
        this.responseAt = responseAt;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public OffsetDateTime getTimeoutAt() {
        return timeoutAt;
    }

    public void setTimeoutAt(OffsetDateTime timeoutAt) {
        this.timeoutAt = timeoutAt;
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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "OrderAssignment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", courierId=" + courierId +
                ", status=" + status +
                ", assignmentType=" + assignmentType +
                ", assignedAt=" + assignedAt +
                '}';
    }
}

