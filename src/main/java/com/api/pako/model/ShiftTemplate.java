package com.api.pako.model;

import com.api.pako.model.enums.ShiftRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ShiftTemplate entity - Vardiya şablonları
 * Tekrar eden vardiya zaman dilimlerini tanımlar
 */
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
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

    // Constructors
    public ShiftTemplate() {
    }

    public ShiftTemplate(String name, String description, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ShiftRole getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(ShiftRole defaultRole) {
        this.defaultRole = defaultRole;
    }

    public Integer getMaxCouriers() {
        return maxCouriers;
    }

    public void setMaxCouriers(Integer maxCouriers) {
        this.maxCouriers = maxCouriers;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        return "ShiftTemplate{" +
                "templateId=" + templateId +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", defaultRole=" + defaultRole +
                ", maxCouriers=" + maxCouriers +
                ", isActive=" + isActive +
                '}';
    }
}

