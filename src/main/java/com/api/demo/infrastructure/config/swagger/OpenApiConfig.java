package com.api.demo.infrastructure.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    private final Environment environment;

    public OpenApiConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("I Need Courier API")
                .version("1.0.0")
                .description("Kurye hizmetleri için JWT tabanlı API dokümantasyonu")
                .contact(new Contact()
                    .name("API Support")
                    .email("support@ineedcourier.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));

        // Profile-based server configuration
        if (Arrays.asList(environment.getActiveProfiles()).contains("docker")) {
            // Docker environment - external access
            openAPI.servers(List.of(
                new Server().url("http://localhost:8081").description("Docker Development Server (External)"),
                new Server().url("http://localhost:8080").description("Docker Development Server (Internal)")
            ));
        } else {
            // Local development environment
            openAPI.servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Local Development Server")
            ));
        }

        return openAPI;
    }
}
