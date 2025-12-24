package com.api.pako.controller;

import com.api.pako.dto.BusinessRegistrationRequest;
import com.api.pako.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationControllerIT extends AbstractIntegrationTest {

    @Autowired
    BusinessRepository businessRepository;

    @BeforeEach
    void setUp() {
        businessRepository.deleteAll();
    }

    @ParameterizedTest(name = "POST /auth/register{0} returns 400 for empty body", quoteTextArguments = false)
    @ValueSource(strings = {"/courier", "/business"})
    void postWithoutPathReturnsBadRequestForEmptyBody(String path) {
        // GIVEN WHEN
        var response = mockMvc.post()
                .uri("/api/v1/auth/register" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // THEN
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /auth/register/business registers and responds with ApiResponse wrapper")
    void postToBusinessRegistersABusiness() {
        // GIVEN
        var request = new BusinessRegistrationRequest();
        request.setName("Crispy Krab");
        request.setEmail("mr.krabs@crispy.com");
        request.setPhone("1234567890");
        request.setPassword("abc123");
        request.setAddress("Bikini Bottom, NYC");
        request.setContactPerson("Sponge Bob");
        request.setBusinessType("restaurant");

        var expectedResponse = """
                {
                  "code": 200,
                  "data": {
                    "businessId": 1,
                    "name": "Crispy Krab",
                    "email": "mr.krabs@crispy.com",
                    "status": "PENDING",
                    "message": "Registration successful. Pending approval."
                    },
                  "message":"Business registration successful. Pending approval."
                }
                """;

        // WHEN
        var response = mockMvc
                .post()
                .uri("/api/v1/auth/register/business")
                .content(dtoToJson(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();

        // THEN
        assertThat(response)
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .hasPath(".respondedAt")
                .isLenientlyEqualTo(expectedResponse);
    }
}
