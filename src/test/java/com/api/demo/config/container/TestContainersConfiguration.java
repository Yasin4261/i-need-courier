package com.api.demo.config.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

    private static final String IMAGE = "postgis/postgis:17-3.6-alpine";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(
                DockerImageName.parse(IMAGE).asCompatibleSubstituteFor("postgres")
        );
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
