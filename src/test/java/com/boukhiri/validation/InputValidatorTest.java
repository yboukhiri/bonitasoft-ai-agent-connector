package com.boukhiri.validation;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for InputValidator.
 */
class InputValidatorTest {

    private final Object connectorInstance = new Object();

    @Test
    @DisplayName("Should pass validation with all valid inputs")
    void should_pass_validation_with_all_valid_inputs() {
        Map<String, Object> input = Map.of("question", "test");
        Map<String, Object> params = Map.of("top_k", 3);
        Integer timeout = 5000;
        String apiKey = "sk-test-key";

        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> input,
                () -> params,
                () -> timeout,
                () -> apiKey
        );

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("Should fail when input is null")
    void should_fail_when_input_is_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("input");
    }

    @Test
    @DisplayName("Should fail when input is not a Map")
    void should_fail_when_input_is_not_a_map() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "not a map",
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("Map");
    }

    @Test
    @DisplayName("Should fail when params is not a Map")
    void should_fail_when_params_is_not_a_map() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> Map.of("question", "test"),
                () -> "not a map",
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("params");
    }

    @Test
    @DisplayName("Should fail when timeout is negative")
    void should_fail_when_timeout_is_negative() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> Map.of("question", "test"),
                () -> null,
                () -> -1000,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("timeoutMs");
    }

    @Test
    @DisplayName("Should fail when timeout is zero")
    void should_fail_when_timeout_is_zero() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> Map.of("question", "test"),
                () -> null,
                () -> 0,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("positive");
    }

    @Test
    @DisplayName("Should fail when timeout is wrong type")
    void should_fail_when_timeout_is_wrong_type() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> Map.of("question", "test"),
                () -> null,
                () -> "not an integer",
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).contains("Integer");
    }

    @Test
    @DisplayName("Should pass with optional fields null")
    void should_pass_with_optional_fields_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> Map.of("question", "test"),
                () -> null,  // params optional
                () -> null,  // timeout optional
                () -> null   // apiKey optional
        );

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("Should collect multiple errors")
    void should_collect_multiple_errors() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> null,           // Missing input
                () -> "not a map",    // Wrong type
                () -> -1,             // Negative
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        
        // Should contain multiple error messages
        assertThat(exception.getMessage()).contains("input");
        assertThat(exception.getMessage()).contains("params");
        assertThat(exception.getMessage()).contains("timeoutMs");
    }
}

