package com.api.pako.service.geocoding;

import com.api.pako.config.GeocodingProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link GeocodingService} backed by an OpenStreetMap Nominatim instance.
 *
 * <p>Honours the Nominatim usage policy: an identifying {@code User-Agent} header is sent on
 * every request and calls are throttled to the configured rate (default 1 req/s). Any failure
 * — no match, malformed payload, or transport error — resolves to {@link Optional#empty()};
 * callers decide how to react (see docs/adr/ADR-001).
 */
@Service
@Slf4j
public class NominatimGeocodingService implements GeocodingService {

    private final GeocodingProperties properties;
    private final RestClient restClient;

    /** Serialises outbound calls so the configured rate limit is enforced process-wide. */
    private final ReentrantLock throttleLock = new ReentrantLock(true);
    private long lastRequestAtNanos;

    public NominatimGeocodingService(GeocodingProperties properties, RestClient geocodingRestClient) {
        this.properties = properties;
        this.restClient = geocodingRestClient;
    }

    @Override
    public Optional<Coordinates> geocode(String address) {
        if (!properties.isEnabled()) {
            log.debug("Geocoding is disabled; skipping lookup for address: {}", address);
            return Optional.empty();
        }
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        var query = normalize(address);

        throttle();

        try {
            var results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 1)
                            .queryParam("countrycodes", properties.getNominatim().getCountryCodes())
                            .build())
                    .retrieve()
                    .body(NominatimResult[].class);

            if (results == null || results.length == 0) {
                log.warn("No geocoding result for address: {}", address);
                return Optional.empty();
            }
            return results[0].toCoordinates();
        } catch (RestClientException ex) {
            log.warn("Geocoding request failed for address '{}': {}", address, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Clean up a free-text address to improve the geocoder's hit rate. Turkish addresses are
     * often written with slash-separated districts and abbreviated street types (e.g.
     * {@code "... Moda Cad. No:15 Kadıköy/İstanbul"}), which the public Nominatim instance
     * struggles to match. This expands the common abbreviations, turns {@code /} separators
     * into commas, and collapses redundant whitespace.
     */
    private String normalize(String address) {
        return address.strip()
                .replace('/', ',')
                .replaceAll("(?i)\\bMah\\.", "Mahallesi")
                .replaceAll("(?i)\\bCad\\.", "Caddesi")
                .replaceAll("(?i)\\bSok\\.", "Sokak")
                .replaceAll("(?i)\\bBulv\\.", "Bulvarı")
                .replaceAll("(?i)\\bApt\\.", "Apartmanı")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    /**
     * Block until at least {@code 1 / rateLimitPerSecond} seconds have elapsed since the last
     * outbound call. Skipped entirely when the rate limit is non-positive.
     */
    private void throttle() {
        var rate = properties.getRateLimitPerSecond();
        if (rate <= 0) {
            return;
        }
        var minIntervalNanos = (long) (1_000_000_000L / rate);
        throttleLock.lock();
        try {
            var waitNanos = (lastRequestAtNanos + minIntervalNanos) - System.nanoTime();
            if (waitNanos > 0) {
                Thread.sleep(Duration.ofNanos(waitNanos));
            }
            lastRequestAtNanos = System.nanoTime();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            throttleLock.unlock();
        }
    }

    /**
     * Subset of a Nominatim search result. Coordinates arrive as strings in the JSON payload.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon, @JsonProperty("display_name") String displayName) {

        Optional<Coordinates> toCoordinates() {
            try {
                return Optional.of(new Coordinates(Double.parseDouble(lat), Double.parseDouble(lon)));
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }
    }
}
