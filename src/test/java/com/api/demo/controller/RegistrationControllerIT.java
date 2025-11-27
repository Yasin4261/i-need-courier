package com.api.demo.controller;

import com.api.demo.config.container.TestContainersConfiguration;
import com.api.demo.dto.BusinessRegistrationRequest;
import com.api.demo.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
@AutoConfigureJsonTesters
public class RegistrationControllerIT {

    @LocalServerPort
    private Integer port;

    @Autowired
    private PostgreSQLContainer postgres;

    @Autowired
    BusinessRepository businessRepository;

    private final RestClient restClient = RestClient.create();

    @BeforeEach
    void setUp() {
        businessRepository.deleteAll();
    }

    @ParameterizedTest(name = "POST /auth/register{0} returns 400 for empty body", quoteTextArguments = false)
    @ValueSource(strings = {"/courier", "/business"})
    void postWithoutPathReturnsBadRequestForEmptyBody(String path) throws IOException {
        // GIVEN WHEN
        var response = restClient.post()
                .uri(endpoint(path))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange((req, res) -> res);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /auth/register/business registers and responds with ApiResponse wrapper")
    void postToBusinessRegistersABusiness() throws IOException {
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

        // WHEN THEN
        restClient.post()
                .uri(endpoint("/business"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((req, res) -> {
                    // todo - 201 created instead?
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
                    var actual = res.bodyTo(String.class);
                    // todo - needs a better approach for asserting ApiResponse wrapped DTOs
                    assertThat(actual).isEqualToIgnoringWhitespace(expectedResponse);
                    return actual;
                });

    }


    private String endpoint(String endpoint) {
        return "http://localhost" + ":" + port + "/api/v1/auth/register" + endpoint;
    }
}
