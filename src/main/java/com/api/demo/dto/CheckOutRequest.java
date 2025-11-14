package com.api.demo.dto;

/**
 * Check-out isteği
 */
public class CheckOutRequest {

    private String notes;
    private Double latitude;
    private Double longitude;

    // Constructors
    public CheckOutRequest() {
    }

    // Getters and Setters
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

