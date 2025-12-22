package com.api.pako.model;

import com.api.pako.model.enums.ShiftRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ShiftTemplate entity - Vardiya şablonları
 * Tekrar eden vardiya zaman dilimlerini tanımlar
 */
@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    @EqualsAndHashCode.Include
    private Long templateId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_role", length = 50)
    private ShiftRole defaultRole = ShiftRole.COURIER;

    @Column(name = "max_couriers")
    private Integer maxCouriers = 10;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ShiftTemplate(String name, String description, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
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
