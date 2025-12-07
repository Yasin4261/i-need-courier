package com.api.pako.controller;

import com.api.pako.dto.UnifiedLoginRequest;
import com.api.pako.dto.UnifiedLoginResponse;
import com.api.pako.model.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedAuthControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /api/v1/auth/login successfully logins user")
    void loginBusiness() throws IOException {
        // GIVEN
        var request = new UnifiedLoginRequest();
        request.setEmail("keanu@reeves.com");
        request.setPassword("test123");

        // See test-migrations for response content
        var expectedResponse = new UnifiedLoginResponse();
        expectedResponse.setUserId(42L);
        expectedResponse.setEmail(request.getEmail());
        expectedResponse.setName("Jack's Burger");
        expectedResponse.setUserType(UserType.BUSINESS);
        expectedResponse.setStatus("ACTIVE");
        expectedResponse.setMessage("Login successful");

        // WHEN
        var response = mockMvc
                .post()
                .uri("/api/v1/auth/login")
                .content(dtoToJson(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        var json = stringToJson(response.getResponse().getContentAsString());
        var actualDto = jsonToDto(json.path("data"), UnifiedLoginResponse.class);

        // THEN
        assertThat(actualDto).usingRecursiveComparison()
                .ignoringFields("token")
                .isEqualTo(expectedResponse);
        assertThat(actualDto)
                .extracting("token")
                .satisfies(token -> assertThat(token).asString().startsWith("eyJhbGciOiJIUzM4NCJ9"));
        assertThat(response)
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}