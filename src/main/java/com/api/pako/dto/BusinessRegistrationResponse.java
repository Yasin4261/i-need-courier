package com.api.pako.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for business registration.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRegistrationResponse {

    // Getters and Setters
    private Long businessId;
    private String name;
    private String email;
    private String status;
    private String message;

}

