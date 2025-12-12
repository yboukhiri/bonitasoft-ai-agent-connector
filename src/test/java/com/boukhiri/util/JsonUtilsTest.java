package com.boukhiri.util;

import com.boukhiri.exception.AgentCommunicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for JsonUtils utility class.
 */
class JsonUtilsTest {

    @Test
    @DisplayName("Should serialize Map to JSON")
    void should_serialize_map_to_json() {
        Map<String, Object> data = Map.of(
                "question", "What is the deadline?",
                "count", 5
        );

        String json = JsonUtils.toJson(data);

        assertThat(json).contains("question");
        assertThat(json).contains("What is the deadline?");
        assertThat(json).contains("count");
    }

    @Test
    @DisplayName("Should deserialize JSON to Map")
    void should_deserialize_json_to_map() throws AgentCommunicationException {
        String json = "{\"answer\":\"5 business days\",\"confidence\":0.92}";

        Map<String, Object> map = JsonUtils.fromJson(json);

        assertThat(map).containsEntry("answer", "5 business days");
        assertThat(map).containsKey("confidence");
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON")
    void should_throw_exception_for_invalid_json() {
        String invalidJson = "{ not valid json }";

        assertThrows(AgentCommunicationException.class, 
                () -> JsonUtils.fromJson(invalidJson));
    }

    @Test
    @DisplayName("Should return null for empty string")
    void should_return_null_for_empty_string() throws AgentCommunicationException {
        assertThat(JsonUtils.fromJson("")).isNull();
        assertThat(JsonUtils.fromJson("   ")).isNull();
        assertThat(JsonUtils.fromJson(null)).isNull();
    }

    @Test
    @DisplayName("Should validate JSON correctly")
    void should_validate_json_correctly() {
        assertThat(JsonUtils.isValidJson("{\"key\":\"value\"}")).isTrue();
        assertThat(JsonUtils.isValidJson("[]")).isTrue();
        assertThat(JsonUtils.isValidJson("{ invalid }")).isFalse();
        assertThat(JsonUtils.isValidJson(null)).isFalse();
        assertThat(JsonUtils.isValidJson("")).isFalse();
    }

    @Test
    @DisplayName("Should handle nested objects")
    void should_handle_nested_objects() throws AgentCommunicationException {
        String json = """
            {
                "output": {
                    "answer": "test",
                    "sources": [{"title": "Doc1"}]
                }
            }
            """;

        Map<String, Object> map = JsonUtils.fromJson(json);

        assertThat(map).containsKey("output");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) map.get("output");
        assertThat(output).containsEntry("answer", "test");
    }
}

