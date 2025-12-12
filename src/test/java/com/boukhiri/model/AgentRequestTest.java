package com.boukhiri.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AgentRequest DTO.
 */
class AgentRequestTest {

    @Test
    @DisplayName("Should build request with all fields")
    void should_build_request_with_all_fields() {
        Map<String, Object> input = Map.of("question", "What is the deadline?");
        Map<String, Object> params = Map.of("top_k", 3, "min_confidence", 0.65);

        AgentRequest request = AgentRequest.builder()
                .input(input)
                .params(params)
                .build();

        assertThat(request.getInput()).containsEntry("question", "What is the deadline?");
        assertThat(request.getParams()).containsEntry("top_k", 3);
    }

    @Test
    @DisplayName("Should return empty maps when not provided")
    void should_return_empty_maps_when_not_provided() {
        AgentRequest request = AgentRequest.builder().build();

        assertThat(request.getInput()).isEmpty();
        assertThat(request.getParams()).isEmpty();
    }

    @Test
    @DisplayName("Should be immutable - input map modification should not affect request")
    void should_be_immutable() {
        Map<String, Object> input = new HashMap<>();
        input.put("question", "original");

        AgentRequest request = AgentRequest.builder()
                .input(input)
                .build();

        // Modify the original map
        input.put("question", "modified");

        // Request should still have the original value
        assertThat(request.getInput().get("question")).isEqualTo("original");
    }

    @Test
    @DisplayName("Should convert to map correctly")
    void should_convert_to_map() {
        AgentRequest request = AgentRequest.builder()
                .input(Map.of("question", "test"))
                .params(Map.of("top_k", 5))
                .build();

        Map<String, Object> map = request.toMap();

        assertThat(map).containsKey("input");
        assertThat(map).containsKey("params");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> inputMap = (Map<String, Object>) map.get("input");
        assertThat(inputMap).containsEntry("question", "test");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void should_implement_equals_and_hashCode() {
        AgentRequest request1 = AgentRequest.builder()
                .input(Map.of("question", "test"))
                .build();

        AgentRequest request2 = AgentRequest.builder()
                .input(Map.of("question", "test"))
                .build();

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }
}

