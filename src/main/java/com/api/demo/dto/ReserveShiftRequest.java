package com.api.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Vardiya rezerve etme isteği
 */
@Setter
@Getter
@NoArgsConstructor
public class ReserveShiftRequest {

    // Getters and Setters
    @NotNull(message = "Vardiya şablonu ID'si gereklidir")
    private Long templateId;

    @NotNull(message = "Vardiya tarihi gereklidir")
    private LocalDate shiftDate;

    private String notes;

    public ReserveShiftRequest(Long templateId, LocalDate shiftDate) {
        this.templateId = templateId;
        this.shiftDate = shiftDate;
    }

}

