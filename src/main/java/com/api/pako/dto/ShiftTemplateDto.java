package com.api.pako.dto;

import com.api.pako.model.enums.ShiftRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * ShiftTemplate DTO - Vardiya şablonları için transfer objesi
 */

@Setter
@Getter
@NoArgsConstructor
public class ShiftTemplateDto {

    private Long templateId;
    private String name;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShiftRole defaultRole;
    private Integer maxCouriers;
    private Boolean isActive;

    public ShiftTemplateDto(Long templateId, String name, LocalTime startTime, LocalTime endTime) {
        this.templateId = templateId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}

