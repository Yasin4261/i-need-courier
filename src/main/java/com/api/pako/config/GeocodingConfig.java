package com.api.pako.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Enables {@link GeocodingProperties} binding and provides the {@link RestClient} used by the
 * geocoding service, pre-configured with the base URL and the identifying {@code User-Agent}
 * required by the Nominatim usage policy.
 */
@Configuration
@EnableConfigurationProperties(GeocodingProperties.class)
public class GeocodingConfig {

    @Bean
    RestClient geocodingRestClient(GeocodingProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getNominatim().getBaseUrl())
                .defaultHeader("User-Agent", properties.getUserAgent())
                .build();
    }
}
