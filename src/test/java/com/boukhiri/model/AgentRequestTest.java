package com.boukhiri.model;

import com.boukhiri.config.ConnectorConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for AgentRequest DTO.
 */
class AgentRequestTest {

    @Test
    @DisplayName("Should build request with all fields")
    void should_build_request_with_all_fields() {
        AgentRequest request = AgentRequest.builder()
                .question("What is the deadline?")
                .llmApiKey("sk-test-key")
                .llmModel("gpt-4o-mini")
                .topK(5)
                .minConfidence(0.7)
                .requireSources(true)
                .temperature(0.2)
                .timeoutMs(20000)
                .maxTokens(1000)
                .build();

        assertThat(request.getQuestion()).isEqualTo("What is the deadline?");
        assertThat(request.getLlmApiKey()).isEqualTo("sk-test-key");
        assertThat(request.getLlmModel()).isEqualTo("gpt-4o-mini");
        assertThat(request.getTopK()).isEqualTo(5);
        assertThat(request.getMinConfidence()).isEqualTo(0.7);
        assertThat(request.isRequireSources()).isTrue();
        assertThat(request.getTemperature()).isEqualTo(0.2);
        assertThat(request.getTimeoutMs()).isEqualTo(20000);
        assertThat(request.getMaxTokens()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should use default values when optional fields not provided")
    void should_use_default_values() {
        AgentRequest request = AgentRequest.builder()
                .question("Test question")
                .llmApiKey("sk-test-key")
                .build();

        assertThat(request.getQuestion()).isEqualTo("Test question");
        assertThat(request.getLlmApiKey()).isEqualTo("sk-test-key");
        assertThat(request.getLlmModel()).isEqualTo(ConnectorConstants.DEFAULT_LLM_MODEL);
        assertThat(request.getTopK()).isEqualTo(ConnectorConstants.DEFAULT_TOP_K);
        assertThat(request.getMinConfidence()).isEqualTo(ConnectorConstants.DEFAULT_MIN_CONFIDENCE);
        assertThat(request.isRequireSources()).isEqualTo(ConnectorConstants.DEFAULT_REQUIRE_SOURCES);
        assertThat(request.getTemperature()).isEqualTo(ConnectorConstants.DEFAULT_TEMPERATURE);
        assertThat(request.getTimeoutMs()).isEqualTo(ConnectorConstants.DEFAULT_TIMEOUT_MS);
        assertThat(request.getMaxTokens()).isEqualTo(ConnectorConstants.DEFAULT_MAX_TOKENS);
    }

    @Test
    @DisplayName("Should throw exception when question is null")
    void should_throw_exception_when_question_is_null() {
        assertThrows(NullPointerException.class, () ->
                AgentRequest.builder()
                        .llmApiKey("sk-test-key")
                        .build()
        );
    }

    @Test
    @DisplayName("Should throw exception when llmApiKey is null")
    void should_throw_exception_when_llmApiKey_is_null() {
        assertThrows(NullPointerException.class, () ->
                AgentRequest.builder()
                        .question("Test question")
                        .build()
        );
    }

    @Test
    @DisplayName("Should convert to map with correct structure")
    void should_convert_to_map() {
        AgentRequest request = AgentRequest.builder()
                .question("What is the deadline?")
                .llmApiKey("sk-test-key")
                .topK(3)
                .build();

        Map<String, Object> map = request.toMap();

        // Verify top-level structure
        assertThat(map).containsKey("task");
        assertThat(map.get("task")).isEqualTo(ConnectorConstants.TASK_RAG_QA);
        assertThat(map).containsKey("input");
        assertThat(map).containsKey("params");

        // Verify input structure
        @SuppressWarnings("unchecked")
        Map<String, Object> input = (Map<String, Object>) map.get("input");
        assertThat(input).containsEntry("question", "What is the deadline?");

        // Verify params structure
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) map.get("params");
        assertThat(params).containsEntry("top_k", 3);
        assertThat(params).containsEntry("llm_api_key", "sk-test-key");
        assertThat(params).containsKey("llm_model");
        assertThat(params).containsKey("min_confidence");
        assertThat(params).containsKey("require_sources");
        assertThat(params).containsKey("temperature");
        assertThat(params).containsKey("timeout_ms");
        assertThat(params).containsKey("max_tokens");
    }

    @Test
    @DisplayName("Should include llm_api_url when provided")
    void should_include_llm_api_url_when_provided() {
        AgentRequest request = AgentRequest.builder()
                .question("Test")
                .llmApiKey("sk-test-key")
                .llmApiUrl("https://custom-api.example.com")
                .build();

        Map<String, Object> map = request.toMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) map.get("params");
        assertThat(params).containsEntry("llm_api_url", "https://custom-api.example.com");
    }

    @Test
    @DisplayName("Should not include llm_api_url when not provided")
    void should_not_include_llm_api_url_when_not_provided() {
        AgentRequest request = AgentRequest.builder()
                .question("Test")
                .llmApiKey("sk-test-key")
                .build();

        Map<String, Object> map = request.toMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) map.get("params");
        assertThat(params).doesNotContainKey("llm_api_url");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void should_implement_equals_and_hashCode() {
        AgentRequest request1 = AgentRequest.builder()
                .question("Test question")
                .llmApiKey("sk-test-key")
                .topK(3)
                .build();

        AgentRequest request2 = AgentRequest.builder()
                .question("Test question")
                .llmApiKey("sk-test-key")
                .topK(3)
                .build();

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should not include sensitive data in toString")
    void should_not_include_sensitive_data_in_toString() {
        AgentRequest request = AgentRequest.builder()
                .question("Test question")
                .llmApiKey("sk-secret-key-12345")
                .build();

        String toString = request.toString();
        assertThat(toString).doesNotContain("sk-secret-key-12345");
        assertThat(toString).contains("Test question");
    }
}
