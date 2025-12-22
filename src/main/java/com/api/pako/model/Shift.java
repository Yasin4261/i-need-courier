package com.api.pako.model;

import com.api.pako.model.enums.ShiftRole;
import com.api.pako.model.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Shift entity - Kuryelerin vardiya kayıtları
 * Dökümantasyondaki shifts tablosunu temsil eder
 */
@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    @EqualsAndHashCode.Include
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

    public Shift(Long courierId, LocalDateTime startTime, LocalDateTime endTime, ShiftRole shiftRole) {
        this.courierId = courierId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftRole = shiftRole;
        this.status = ShiftStatus.RESERVED;
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
}
