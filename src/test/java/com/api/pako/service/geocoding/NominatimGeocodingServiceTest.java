package com.api.pako.service.geocoding;

import com.api.pako.config.GeocodingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class NominatimGeocodingServiceTest {

    private GeocodingProperties properties;
    private MockRestServiceServer server;
    private NominatimGeocodingService underTest;

    @BeforeEach
    void setUp() {
        properties = new GeocodingProperties();
        properties.setEnabled(true);
        properties.setUserAgent("test-agent/1.0");
        properties.setRateLimitPerSecond(0); // disable throttling so tests stay fast
        properties.getNominatim().setBaseUrl("https://nominatim.test");
        properties.getNominatim().setCountryCodes("tr");

        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        underTest = new NominatimGeocodingService(properties, builder);
    }

    @Test
    void resolvesCoordinatesAndSendsCompliantRequest() {
        // GIVEN
        var body = """
                [{"lat":"40.9906","lon":"29.0246","display_name":"Kadikoy, Istanbul, Turkiye"}]
                """;
        server.expect(requestTo(startsWith("https://nominatim.test/search")))
                .andExpect(method(GET))
                // MockRestServiceServer compares the raw (URL-encoded) query value
                .andExpect(queryParam("q", "Bagdat%20Caddesi%20100,%20Istanbul"))
                .andExpect(queryParam("format", "jsonv2"))
                .andExpect(queryParam("limit", "1"))
                .andExpect(queryParam("countrycodes", "tr"))
                .andExpect(header("User-Agent", "test-agent/1.0"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        // WHEN
        var result = underTest.geocode("Bagdat Caddesi 100, Istanbul");

        // THEN
        assertThat(result).hasValueSatisfying(coordinates -> {
            assertThat(coordinates.latitude()).isEqualTo(40.9906);
            assertThat(coordinates.longitude()).isEqualTo(29.0246);
        });
        server.verify();
    }

    @Test
    void returnsEmptyWhenNoMatch() {
        // GIVEN
        server.expect(requestTo(startsWith("https://nominatim.test/search")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        // WHEN / THEN
        assertThat(underTest.geocode("nowhere at all")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyOnUpstreamServerError() {
        // GIVEN
        server.expect(requestTo(startsWith("https://nominatim.test/search")))
                .andRespond(withServerError());

        // WHEN / THEN - transport failure must not propagate
        assertThat(underTest.geocode("Bagdat Caddesi 100, Istanbul")).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyWhenLatLonAreNotNumeric() {
        // GIVEN
        var body = """
                [{"lat":"not-a-number","lon":"29.0246","display_name":"broken"}]
                """;
        server.expect(requestTo(startsWith("https://nominatim.test/search")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        // WHEN / THEN
        assertThat(underTest.geocode("Bagdat Caddesi 100, Istanbul")).isEmpty();
        server.verify();
    }

    @Test
    void resolvesNothingAndSkipsRequestWhenDisabled() {
        // GIVEN geocoding is turned off
        properties.setEnabled(false);

        // WHEN / THEN - no HTTP call is made
        assertThat(underTest.geocode("Bagdat Caddesi 100, Istanbul")).isEmpty();
        server.verify();
    }

    @Test
    void resolvesNothingAndSkipsRequestForBlankAddress() {
        // WHEN / THEN - blank input short-circuits before any HTTP call
        assertThat(underTest.geocode("   ")).isEmpty();
        assertThat(underTest.geocode(null)).isEmpty();
        server.verify();
    }
}
