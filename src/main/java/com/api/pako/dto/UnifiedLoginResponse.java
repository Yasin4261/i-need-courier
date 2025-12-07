package com.api.pako.dto;

import com.api.pako.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unified login response for all user types.
 * Contains JWT token with role information embedded.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnifiedLoginResponse {

    private String token;
    private Long userId;
    private String email;
    private String name;
    private UserType userType;
    // TODO : Make the status enum
    private String status;
    private String message;

}

