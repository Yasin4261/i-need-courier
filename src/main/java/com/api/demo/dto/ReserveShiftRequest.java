package com.api.demo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Vardiya rezerve etme isteği
 */
public class ReserveShiftRequest {

    @NotNull(message = "Vardiya şablonu ID'si gereklidir")
    private Long templateId;

    @NotNull(message = "Vardiya tarihi gereklidir")
    private LocalDate shiftDate;

    private String notes;

    // Constructors
    public ReserveShiftRequest() {
    }

    public ReserveShiftRequest(Long templateId, LocalDate shiftDate) {
        this.templateId = templateId;
        this.shiftDate = shiftDate;
    }

    // Getters and Setters
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

