package com.api.pako.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables {@link GeocodingProperties} binding for the geocoding service.
 */
@Configuration
@EnableConfigurationProperties(GeocodingProperties.class)
public class GeocodingConfig {
}
