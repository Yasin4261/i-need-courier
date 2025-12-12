package com.api.demo.model;

import com.api.demo.model.enums.AssignmentStatus;
import com.api.demo.model.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_assignments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OrderAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    /**
     * Constructor with default values for new assignments
     */
    public static OrderAssignment createNew() {
        OrderAssignment assignment = new OrderAssignment();
        assignment.assignedAt = OffsetDateTime.now();
        assignment.status = AssignmentStatus.PENDING;
        assignment.assignmentType = AssignmentType.AUTO;
        assignment.createdAt = OffsetDateTime.now();
        assignment.updatedAt = OffsetDateTime.now();
        return assignment;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = OffsetDateTime.now();
        }
        if (this.assignedAt == null) {
            this.assignedAt = OffsetDateTime.now();
        }
        if (this.status == null) {
            this.status = AssignmentStatus.PENDING;
        }
        if (this.assignmentType == null) {
            this.assignmentType = AssignmentType.AUTO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
