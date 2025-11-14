package com.api.demo.dto;

import com.api.demo.model.enums.ShiftRole;
import java.time.LocalTime;

/**
 * ShiftTemplate DTO - Vardiya şablonları için transfer objesi
 */
public class ShiftTemplateDTO {
    private Long templateId;
    private String name;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShiftRole defaultRole;
    private Integer maxCouriers;
    private Boolean isActive;

    // Constructors
    public ShiftTemplateDTO() {
    }

    public ShiftTemplateDTO(Long templateId, String name, LocalTime startTime, LocalTime endTime) {
        this.templateId = templateId;
        this.name = name;
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
}

