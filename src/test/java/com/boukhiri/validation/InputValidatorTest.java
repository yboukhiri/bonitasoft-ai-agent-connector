package com.boukhiri.validation;

import com.boukhiri.config.ConnectorConstants;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for InputValidator.
 */
class InputValidatorTest {

    private final Connector connectorInstance = Mockito.mock(Connector.class);

    @Test
    @DisplayName("Should pass validation with all valid inputs")
    void should_pass_validation_with_all_valid_inputs() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",  // agentUrl
                () -> "jwt-token",                   // authToken
                () -> "What is the deadline?",       // question
                () -> "sk-test-key",                 // llmApiKey
                () -> "gpt-4o-mini",                 // llmModel
                () -> 3,                             // topK
                () -> 0.7,                           // minConfidence
                () -> true,                          // requireSources
                () -> 10000,                         // timeoutMs
                () -> 700,                           // maxTokens
                () -> 0.1                            // temperature
        );

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("Should fail when agentUrl is null")
    void should_fail_when_agentUrl_is_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> null,                          // agentUrl missing
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("agentUrl");
    }

    @Test
    @DisplayName("Should fail when agentUrl is invalid")
    void should_fail_when_agentUrl_is_invalid() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "not-a-valid-url",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("URL");
    }

    @Test
    @DisplayName("Should fail when question is null")
    void should_fail_when_question_is_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> null,                          // question missing
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("question");
    }

    @Test
    @DisplayName("Should fail when question is empty")
    void should_fail_when_question_is_empty() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "   ",                         // empty question
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("question");
    }

    @Test
    @DisplayName("Should fail when llmApiKey is null")
    void should_fail_when_llmApiKey_is_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> null,                          // llmApiKey missing
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("llmApiKey");
    }

    @Test
    @DisplayName("Should fail when topK is out of range")
    void should_fail_when_topK_is_out_of_range() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> 15,                           // topK out of range (1-10)
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("topK");
    }

    @Test
    @DisplayName("Should fail when minConfidence is out of range")
    void should_fail_when_minConfidence_is_out_of_range() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> 1.5,                          // minConfidence out of range (0.0-1.0)
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("minConfidence");
    }

    @Test
    @DisplayName("Should fail when timeoutMs is out of range")
    void should_fail_when_timeoutMs_is_out_of_range() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> 500,                          // timeoutMs below minimum (1000)
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("timeoutMs");
    }

    @Test
    @DisplayName("Should fail when maxTokens is out of range")
    void should_fail_when_maxTokens_is_out_of_range() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> 5000,                         // maxTokens out of range (100-4000)
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("maxTokens");
    }

    @Test
    @DisplayName("Should fail when temperature is out of range")
    void should_fail_when_temperature_is_out_of_range() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> 3.0                          // temperature out of range (0.0-2.0)
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("temperature");
    }

    @Test
    @DisplayName("Should pass with optional fields null")
    void should_pass_with_optional_fields_null() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,                          // authToken optional
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,                          // llmModel optional
                () -> null,                          // topK optional
                () -> null,                          // minConfidence optional
                () -> null,                          // requireSources optional
                () -> null,                          // timeoutMs optional
                () -> null,                          // maxTokens optional
                () -> null                           // temperature optional
        );

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("Should fail when authToken is wrong type")
    void should_fail_when_authToken_is_wrong_type() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> 12345,                         // authToken wrong type
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("authToken");
    }

    @Test
    @DisplayName("Should fail when requireSources is wrong type")
    void should_fail_when_requireSources_is_wrong_type() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test question",
                () -> "sk-test-key",
                () -> null,
                () -> null,
                () -> null,
                () -> "not a boolean",               // requireSources wrong type
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        assertThat(exception.getMessage()).containsIgnoringCase("requireSources");
    }

    @Test
    @DisplayName("Should collect multiple errors")
    void should_collect_multiple_errors() {
        InputValidator validator = new InputValidator(
                connectorInstance,
                () -> null,                          // Missing agentUrl
                () -> null,
                () -> null,                          // Missing question
                () -> null,                          // Missing llmApiKey
                () -> null,
                () -> 15,                            // topK out of range
                () -> null,
                () -> null,
                () -> null,
                () -> null,
                () -> null
        );

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                validator::validate
        );
        
        // Should contain multiple error messages
        assertThat(exception.getMessage()).containsIgnoringCase("agentUrl");
        assertThat(exception.getMessage()).containsIgnoringCase("question");
        assertThat(exception.getMessage()).containsIgnoringCase("llmApiKey");
        assertThat(exception.getMessage()).containsIgnoringCase("topK");
    }

    @Test
    @DisplayName("Should accept valid URL with different schemes")
    void should_accept_valid_url_with_different_schemes() {
        InputValidator validator1 = new InputValidator(
                connectorInstance,
                () -> "http://localhost:8000/run",
                () -> null,
                () -> "Test",
                () -> "sk-key",
                () -> null, () -> null, () -> null, () -> null, () -> null, () -> null, () -> null
        );
        assertDoesNotThrow(validator1::validate);

        InputValidator validator2 = new InputValidator(
                connectorInstance,
                () -> "https://api.example.com/run",
                () -> null,
                () -> "Test",
                () -> "sk-key",
                () -> null, () -> null, () -> null, () -> null, () -> null, () -> null, () -> null
        );
        assertDoesNotThrow(validator2::validate);
    }
}
