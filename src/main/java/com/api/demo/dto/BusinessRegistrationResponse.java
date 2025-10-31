package com.api.demo.dto;

/**
 * Response DTO for business registration.
 */
public class BusinessRegistrationResponse {

    private Long businessId;
    private String name;
    private String email;
    private String status;
    private String message;

    // Constructors
    public BusinessRegistrationResponse() {
    }

    public BusinessRegistrationResponse(Long businessId, String name, String email, String status, String message) {
        this.businessId = businessId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BusinessRegistrationResponse{" +
                "businessId=" + businessId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

