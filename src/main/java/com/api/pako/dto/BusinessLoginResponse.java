package com.api.pako.dto;

import lombok.*;

/**
 * Response DTO for business login.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BusinessLoginResponse {

    // Getters and Setters
    private String token;
    private Long businessId;
    private String name;
    private String email;
    private String status;
    private String message;

}

