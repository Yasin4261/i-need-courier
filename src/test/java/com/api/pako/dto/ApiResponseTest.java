package com.api.pako.dto;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ApiResponseTest {

    @Test
    void ok() {
        // GIVEN
        var payload = List.of("test");

        // WHEN
        var underTest = ApiResponse.ok(payload, "Test message");

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(underTest.getMessage()).isEqualTo("Test message");
        assertThat(underTest.getData()).isEqualTo(payload);
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }

    @Test
    void okWithoutMessage() {
        // GIVEN
        var payload = List.of("test");

        // WHEN
        var underTest = ApiResponse.ok(payload);

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(underTest.getMessage()).isEqualTo(HttpStatus.OK.getReasonPhrase());
        assertThat(underTest.getData()).isEqualTo(payload);
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }

    @Test
    void created() {
        // GIVEN
        var payload = List.of("test");

        // WHEN
        var underTest = ApiResponse.created(payload, "Test message");

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(underTest.getMessage()).isEqualTo("Test message");
        assertThat(underTest.getData()).isEqualTo(payload);
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }

    @Test
    void createdWithoutMessage() {
        // GIVEN
        var payload = List.of("test");

        // WHEN
        var underTest = ApiResponse.created(payload);

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(underTest.getMessage()).isEqualTo(HttpStatus.CREATED.getReasonPhrase());
        assertThat(underTest.getData()).isEqualTo(payload);
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }

    @Test
    void deleted() {
        // GIVEN WHEN
        var underTest = ApiResponse.deleted("Successfully deleted");

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(underTest.getMessage()).isEqualTo("Successfully deleted");
        assertThat(underTest.getData()).isNull();
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }

    @Test
    void noContent() {
        // GIVEN WHEN
        var underTest = ApiResponse.noContent("Your request queued");

        // THEN
        assertThat(underTest.getCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(underTest.getMessage()).isEqualTo("Your request queued");
        assertThat(underTest.getData()).isNull();
        assertThat(underTest.getRespondedAt()).isCloseToUtcNow(within(1, ChronoUnit.SECONDS));
    }
}