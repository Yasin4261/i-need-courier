package com.api.demo.dto;

/**
 * Response DTO for courier login.
 */
public class CourierLoginResponse {

    private String token;
    private Long courierId;
    private String name;
    private String email;
    private String status;

    // Constructors
    public CourierLoginResponse() {
    }

    public CourierLoginResponse(String token, Long courierId, String name, String email, String status) {
        this.token = token;
        this.courierId = courierId;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CourierLoginResponse{" +
                "courierId=" + courierId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

