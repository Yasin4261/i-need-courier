package com.api.demo.application.dto;

public class CourierRegistrationResponse {
    private Long courierId;
    private String name;
    private String email;
    private String message;

    public CourierRegistrationResponse() {}

    public CourierRegistrationResponse(Long courierId, String name, String email, String message) {
        this.courierId = courierId;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
