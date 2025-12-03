package com.api.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for courier registration.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CourierRegistrationResponse {

    // Getters and Setters
    private Long courierId;
    private String name;
    private String email;
    private String message;

}

