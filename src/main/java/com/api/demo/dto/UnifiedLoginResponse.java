package com.api.demo.dto;

/**
 * Unified login response for all user types.
 * Contains JWT token with role information embedded.
 */
public class UnifiedLoginResponse {

    private String token;
    private Long userId;
    private String email;
    private String name;
    private String userType;  // COURIER, BUSINESS, ADMIN
    private String status;
    private String message;

    public UnifiedLoginResponse() {}

    public UnifiedLoginResponse(String token, Long userId, String email, String name,
                               String userType, String status, String message) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.status = status;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

