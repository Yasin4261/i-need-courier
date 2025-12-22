package com.api.pako.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CheckInRequest {

    private String notes;
    private Double latitude;
    private Double longitude;
}

