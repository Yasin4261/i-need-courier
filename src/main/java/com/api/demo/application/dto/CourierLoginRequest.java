package com.api.demo.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Kurye giriş isteği")
public class CourierLoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Kuryenin email adresi", example = "kurye@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Kurye şifresi", example = "password123")
    private String password;

    public CourierLoginRequest() {}

    public CourierLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
