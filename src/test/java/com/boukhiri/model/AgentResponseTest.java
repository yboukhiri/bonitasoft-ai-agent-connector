package com.boukhiri.model;

import com.boukhiri.config.ConnectorConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
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
                .answer("Test answer")
                .sources(List.of(Map.of("title", "Doc1", "version", "2023")))
                .confidence(0.95)
                .reasoning("Found in document")
                .build();

        assertThat(response.getStatus()).isEqualTo("ok");
        assertThat(response.getAnswer()).isEqualTo("Test answer");
        assertThat(response.getSources()).hasSize(1);
        assertThat(response.getConfidence()).isEqualTo(0.95);
        assertThat(response.getReasoning()).isEqualTo("Found in document");
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should detect error status")
    void should_detect_error_status() {
        AgentResponse response = AgentResponse.builder()
                .status("error")
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Something went wrong")
                .build();

        assertThat(response.isError()).isTrue();
        assertThat(response.isLowConfidence()).isFalse();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getErrorMessage()).isEqualTo("Something went wrong");
    }

    @Test
    @DisplayName("Should detect low confidence status")
    void should_detect_low_confidence_status() {
        AgentResponse response = AgentResponse.builder()
                .status("low_confidence")
                .answer("Uncertain answer")
                .confidence(0.3)
                .build();

        assertThat(response.isLowConfidence()).isTrue();
        assertThat(response.isError()).isFalse();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should detect success status")
    void should_detect_success_status() {
        AgentResponse response = AgentResponse.builder()
                .status("ok")
                .answer("Test answer")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isError()).isFalse();
        assertThat(response.isLowConfidence()).isFalse();
    }

    @Test
    @DisplayName("Should create error response using factory method")
    void should_create_error_response() {
        AgentResponse response = AgentResponse.error("VALIDATION_ERROR", "Invalid input");

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getErrorMessage()).isEqualTo("Invalid input");
        assertThat(response.isError()).isTrue();
    }

    @Test
    @DisplayName("Should create error response from exception")
    void should_create_error_response_from_exception() {
        Exception e = new IllegalArgumentException("Test exception");
        AgentResponse response = AgentResponse.error("INTERNAL_ERROR", e);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getErrorMessage()).isEqualTo("Test exception");
        assertThat(response.getErrorDetails()).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    @DisplayName("Should return empty list when sources not provided")
    void should_return_empty_list_when_sources_not_provided() {
        AgentResponse response = AgentResponse.builder()
                .status("ok")
                .answer("Test")
                .build();

        assertThat(response.getSources()).isEmpty();
    }

    @Test
    @DisplayName("Should have default ok status")
    void should_have_default_ok_status() {
        AgentResponse response = AgentResponse.builder().build();

        assertThat(response.getStatus()).isEqualTo("ok");
    }

    @Test
    @DisplayName("Should parse response from map - success case")
    void should_parse_response_from_map_success() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "ok");
        responseMap.put("output", Map.of(
                "answer", "Test answer",
                "sources", List.of(Map.of("title", "Doc1", "version", "2023")),
                "confidence", 0.9,
                "reasoning", "Found in document"
        ));
        responseMap.put("usage", Map.of("latency_ms", 150));
        responseMap.put("error", null);

        AgentResponse response = AgentResponse.fromMap(responseMap);

        assertThat(response.getStatus()).isEqualTo("ok");
        assertThat(response.getAnswer()).isEqualTo("Test answer");
        assertThat(response.getSources()).hasSize(1);
        assertThat(response.getConfidence()).isEqualTo(0.9);
        assertThat(response.getReasoning()).isEqualTo("Found in document");
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should parse response from map - error case")
    void should_parse_response_from_map_error() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "error");
        responseMap.put("output", null);
        responseMap.put("usage", null);
        responseMap.put("error", Map.of(
                "code", "VALIDATION_ERROR",
                "message", "Invalid question",
                "details", "Question cannot be empty"
        ));

        AgentResponse response = AgentResponse.fromMap(responseMap);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getErrorMessage()).isEqualTo("Invalid question");
        assertThat(response.getErrorDetails()).isEqualTo("Question cannot be empty");
        assertThat(response.getAnswer()).isNull();
    }

    @Test
    @DisplayName("Should handle confidence as integer from JSON")
    void should_handle_confidence_as_integer() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "ok");
        responseMap.put("output", Map.of(
                "answer", "Test",
                "confidence", 1  // Integer instead of double
        ));
        responseMap.put("usage", Map.of());
        responseMap.put("error", null);

        AgentResponse response = AgentResponse.fromMap(responseMap);

        assertThat(response.getConfidence()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should handle missing output section")
    void should_handle_missing_output_section() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "ok");
        responseMap.put("usage", Map.of());
        responseMap.put("error", null);

        AgentResponse response = AgentResponse.fromMap(responseMap);

        assertThat(response.getStatus()).isEqualTo("ok");
        assertThat(response.getAnswer()).isNull();
        assertThat(response.getSources()).isEmpty();
    }

    @Test
    @DisplayName("Should handle string error (legacy format)")
    void should_handle_string_error() {
        Map<String, Object> responseMap = Map.of(
                "status", "error",
                "error", "Simple error message"
        );

        AgentResponse response = AgentResponse.fromMap(responseMap);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getErrorMessage()).isEqualTo("Simple error message");
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Should return error response when map is null")
    void should_return_error_when_map_is_null() {
        AgentResponse response = AgentResponse.fromMap(null);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getErrorMessage()).contains("Empty response");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void should_implement_equals_and_hashCode() {
        AgentResponse response1 = AgentResponse.builder()
                .status("ok")
                .answer("Test")
                .confidence(0.9)
                .build();

        AgentResponse response2 = AgentResponse.builder()
                .status("ok")
                .answer("Test")
                .confidence(0.9)
                .build();

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Should format toString correctly for error")
    void should_format_toString_for_error() {
        AgentResponse response = AgentResponse.builder()
                .status("error")
                .errorCode("VALIDATION_ERROR")
                .errorMessage("Invalid input")
                .build();

        String toString = response.toString();
        assertThat(toString).contains("error");
        assertThat(toString).contains("VALIDATION_ERROR");
        assertThat(toString).contains("Invalid input");
    }

    @Test
    @DisplayName("Should format toString correctly for success")
    void should_format_toString_for_success() {
        AgentResponse response = AgentResponse.builder()
                .status("ok")
                .answer("This is a very long answer that should be truncated in the toString method")
                .confidence(0.9)
                .sources(List.of(Map.of("title", "Doc1"), Map.of("title", "Doc2")))
                .build();

        String toString = response.toString();
        assertThat(toString).contains("ok");
        assertThat(toString).contains("2 documents");
        assertThat(toString).contains("0.9");
        // Answer should be truncated
        assertThat(toString).contains("...");
    }
}
