package com.boukhiri.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AgentResponse DTO.
 */
class AgentResponseTest {

    @Test
    @DisplayName("Should build response with all fields")
    void should_build_response_with_all_fields() {
        AgentResponse response = AgentResponse.builder()
                .status("ok")
                .output(Map.of("answer", "Test answer"))
                .usage(Map.of("latency_ms", 150))
                .error(null)
                .build();

        assertThat(response.getStatus()).isEqualTo("ok");
        assertThat(response.getOutput()).containsEntry("answer", "Test answer");
        assertThat(response.getUsage()).containsEntry("latency_ms", 150);
        assertThat(response.getError()).isNull();
    }

    @Test
    @DisplayName("Should detect error status")
    void should_detect_error_status() {
        AgentResponse response = AgentResponse.builder()
                .status("error")
                .error("Something went wrong")
                .build();

        assertThat(response.isError()).isTrue();
        assertThat(response.isLowConfidence()).isFalse();
    }

    @Test
    @DisplayName("Should detect low confidence status")
    void should_detect_low_confidence_status() {
        AgentResponse response = AgentResponse.builder()
                .status("low_confidence")
                .build();

        assertThat(response.isLowConfidence()).isTrue();
        assertThat(response.isError()).isFalse();
    }

    @Test
    @DisplayName("Should create error response using factory method")
    void should_create_error_response() {
        AgentResponse response = AgentResponse.error("Network error");

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getError()).isEqualTo("Network error");
        assertThat(response.isError()).isTrue();
    }

    @Test
    @DisplayName("Should return empty maps when not provided")
    void should_return_empty_maps_when_not_provided() {
        AgentResponse response = AgentResponse.builder().build();

        assertThat(response.getOutput()).isEmpty();
        assertThat(response.getUsage()).isEmpty();
    }

    @Test
    @DisplayName("Should have default ok status")
    void should_have_default_ok_status() {
        AgentResponse response = AgentResponse.builder().build();

        assertThat(response.getStatus()).isEqualTo("ok");
    }
}

