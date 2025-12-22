package com.api.pako.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    private static final String IMAGE = "postgis/postgis:17-3.6-alpine";

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse(IMAGE).asCompatibleSubstituteFor("postgres")
    );

    @Autowired
    MockMvcTester mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @BeforeAll
    static void startContainers() {
        POSTGRES.start();
    }

    @AfterAll
    static void stopContainers() {
        POSTGRES.stop();
    }

    PostgreSQLContainer postgres() {
        return POSTGRES;
    }

    protected final String dtoToJson(Object dto) {
        return objectMapper.writeValueAsString(dto);
    }

    protected final JsonNode stringToJson(String data) {
        return objectMapper.readTree(data);
    }

    protected final <T> T jsonToDto(JsonNode json, Class<T> target) {
        return objectMapper.treeToValue(json, target);
    }
}
