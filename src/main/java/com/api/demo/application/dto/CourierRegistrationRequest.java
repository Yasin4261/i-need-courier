package com.api.demo.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Kurye kayıt isteği")
public class CourierRegistrationRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Kuryenin adı ve soyadı", example = "Ahmet Yılmaz")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Kuryenin email adresi", example = "ahmet.yilmaz@example.com")
    private String email;

    @NotBlank(message = "Phone is required")
    @Schema(description = "Kuryenin telefon numarası", example = "+905551234567")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Schema(description = "Kurye şifresi (6-20 karakter)", example = "securePassword123")
    private String password;

    public CourierRegistrationRequest() {}

    public CourierRegistrationRequest(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
