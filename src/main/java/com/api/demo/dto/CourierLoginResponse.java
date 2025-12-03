package com.api.demo.dto;

import lombok.*;

/**
 * Response DTO for courier login.
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CourierLoginResponse {

    // Getters and Setters
    private String token;
    private Long courierId;
    private String name;
    private String email;
    private String status;
}

