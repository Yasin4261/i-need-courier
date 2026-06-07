package com.api.pako.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for address geocoding (see docs/adr/ADR-001).
 *
 * <p>Bound from the {@code geocoding.*} namespace in application properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "geocoding")
public class GeocodingProperties {

    /** When {@code false}, the geocoding service short-circuits and resolves nothing. */
    private boolean enabled = true;

    /**
     * Identifying {@code User-Agent} header sent with every request. The Nominatim usage
     * policy requires a valid identifying header — anonymous requests may be blocked.
     */
    private String userAgent = "i-need-courier/1.0 (+https://github.com/Yasin4261/i-need-courier)";

    /**
     * Maximum requests per second to the geocoder. The public Nominatim instance allows at
     * most 1 req/s. A value {@code <= 0} disables throttling (useful in tests).
     */
    private double rateLimitPerSecond = 1.0;

    private final Nominatim nominatim = new Nominatim();

    @Getter
    @Setter
    public static class Nominatim {

        /** Base URL of the Nominatim instance. */
        private String baseUrl = "https://nominatim.openstreetmap.org";

        /** ISO country code(s) used to bias/limit results, e.g. {@code tr}. */
        private String countryCodes = "tr";
    }
}
