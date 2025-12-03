package com.api.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for business login.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessLoginRequest {

    // Getters and Setters
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

}

