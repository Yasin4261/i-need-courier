package com.api.demo.dto;

import com.api.demo.model.enums.ShiftRole;
import com.api.demo.model.enums.ShiftStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Shift DTO - Vardiya bilgileri için transfer objesi
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDto {

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

}

