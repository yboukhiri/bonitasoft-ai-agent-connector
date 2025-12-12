package com.boukhiri;

import com.boukhiri.config.ConnectorConstants;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive test suite for AIAgentConnector.
 * 
 * <p>Tests cover:</p>
 * <ol>
 *   <li><strong>Validation Tests</strong>: Input parameter validation</li>
 *   <li><strong>Integration Tests</strong>: End-to-end with WireMock</li>
 *   <li><strong>Error Handling Tests</strong>: Various failure scenarios</li>
 * </ol>
 * 
 * @author Yassine Boukhiri
 */
class AIAgentConnectorTest {

    private static WireMockServer wireMockServer;
    private AIAgentConnector connector;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        // Set environment variable for tests
        System.setProperty("AI_AGENT_URL", wireMockServer.baseUrl() + "/run");
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        System.clearProperty("AI_AGENT_URL");
    }

    @BeforeEach
    void setUp() {
        connector = new AIAgentConnector();
        wireMockServer.resetAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION TESTS - input parameter
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should throw exception when input is missing")
    void should_throw_exception_when_input_is_missing() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("input");
    }

    @Test
    @DisplayName("Should throw exception when input is not a Map")
    void should_throw_exception_when_input_is_not_a_map() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, "not a map");
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("Map");
    }

    @Test
    @DisplayName("Should accept empty Map as input")
    void should_accept_empty_map_as_input() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, new HashMap<>());
        connector.setInputParameters(params);

        assertDoesNotThrow(() -> connector.validateInputParameters());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION TESTS - params parameter
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should throw exception when params is not a Map")
    void should_throw_exception_when_params_is_not_a_map() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "test"));
        params.put(ConnectorConstants.INPUT_PARAMS, "not a map");
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("params");
    }

    @Test
    @DisplayName("Should accept null params")
    void should_accept_null_params() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "test"));
        // params not set (null)
        connector.setInputParameters(params);

        assertDoesNotThrow(() -> connector.validateInputParameters());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION TESTS - timeoutMs parameter
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should throw exception when timeoutMs is negative")
    void should_throw_exception_when_timeout_is_negative() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "test"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, -1000);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("timeoutMs");
    }

    @Test
    @DisplayName("Should throw exception when timeoutMs is zero")
    void should_throw_exception_when_timeout_is_zero() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "test"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 0);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("timeoutMs");
    }

    @Test
    @DisplayName("Should accept valid parameters")
    void should_accept_valid_parameters() {
        Map<String, Object> params = createValidParameters();
        connector.setInputParameters(params);

        assertDoesNotThrow(() -> connector.validateInputParameters());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS - Happy Path
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should execute RAG QA request successfully")
    void should_execute_rag_qa_request_successfully() throws ConnectorException {
        // Arrange
        String agentResponse = """
            {
                "status": "ok",
                "output": {
                    "answer": "New employees must complete onboarding within 5 business days.",
                    "sources": [{"title": "Employee Onboarding Procedure", "uri": "onboarding_policy.txt", "page": 1}],
                    "confidence": 0.92
                },
                "usage": {
                    "latency_ms": 150,
                    "tokens_in": 45,
                    "tokens_out": 78,
                    "model": "gpt-4"
                },
                "error": null
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, 
                Map.of("question", "What is the deadline for completing the employee onboarding process?"));
        params.put(ConnectorConstants.INPUT_PARAMS, 
                Map.of("top_k", 3, "min_confidence", 0.65));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("ok");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR)).isNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) outputs.get(ConnectorConstants.OUTPUT_DATA);
        assertThat(output).containsKey("answer");
        assertThat(output.get("answer").toString()).contains("5 business days");

        @SuppressWarnings("unchecked")
        Map<String, Object> usage = (Map<String, Object>) outputs.get(ConnectorConstants.OUTPUT_USAGE);
        assertThat(usage).containsKey("model");

        // Verify request was made with correct payload
        verify(postRequestedFor(urlEqualTo("/run"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing("question")));
    }

    @Test
    @DisplayName("Should handle conflict resolution response")
    void should_handle_conflict_resolution_response() throws ConnectorException {
        // Arrange - Agent detects conflict and resolves it
        String agentResponse = """
            {
                "status": "ok",
                "output": {
                    "answer": "Current policy requires reporting within 72 hours (based on the 2023 procedure). The 2022 version required 48 hours but is outdated.",
                    "sources": [
                        {"title": "Security Incident Procedure 2023", "uri": "incident_policy_2023.txt", "page": 1},
                        {"title": "Security Incident Procedure 2022", "uri": "incident_policy_2022.txt", "page": 1}
                    ],
                    "confidence": 0.88,
                    "reasoning": "Conflict detected between 2022 and 2023 policies. Favoring 2023 version as most recent."
                },
                "usage": {"latency_ms": 200, "tokens_in": 120, "tokens_out": 95, "model": "gpt-4"},
                "error": null
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, 
                Map.of("question", "How long do I have to report a data incident?"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("ok");

        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) outputs.get(ConnectorConstants.OUTPUT_DATA);
        assertThat(output.get("answer").toString()).contains("72 hours");
        assertThat(output).containsKey("reasoning");
        assertThat(output).containsKey("sources");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sources = (List<Map<String, Object>>) output.get("sources");
        assertThat(sources).hasSize(2);
    }

    @Test
    @DisplayName("Should handle low confidence response")
    void should_handle_low_confidence_response() throws ConnectorException {
        // Arrange
        String agentResponse = """
            {
                "status": "low_confidence",
                "output": {
                    "answer": "I found some information but I'm not confident in my answer.",
                    "sources": [],
                    "confidence": 0.35
                },
                "usage": {"latency_ms": 100, "tokens_in": 30, "tokens_out": 40, "model": "gpt-4"},
                "error": null
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "What is the meaning of life?"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("low_confidence");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR)).isNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) outputs.get(ConnectorConstants.OUTPUT_DATA);
        assertThat(((Number) output.get("confidence")).doubleValue()).isLessThan(0.5);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRATION TESTS - Error Handling
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should handle HTTP 500 error from agent")
    void should_handle_http_500_error() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "Test question"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("error");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR).toString()).contains("500");
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void should_handle_malformed_json_response() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ not valid json")));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "Test question"));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("error");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR).toString()).containsIgnoringCase("json");
    }

    @Test
    @DisplayName("Should include authorization header when API key provided")
    void should_include_authorization_header() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ok\",\"output\":{},\"usage\":{},\"error\":null}")));

        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "Test"));
        params.put(ConnectorConstants.INPUT_API_SECRET_KEY, "sk-test-api-key-12345");
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 5000);

        connector.setInputParameters(params);

        // Act
        connector.execute();

        // Assert - Verify authorization header was sent
        verify(postRequestedFor(urlEqualTo("/run"))
                .withHeader("Authorization", equalTo("Bearer sk-test-api-key-12345")));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a valid set of parameters for testing.
     */
    private Map<String, Object> createValidParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_DATA, Map.of("question", "What is the deadline for onboarding?"));
        params.put(ConnectorConstants.INPUT_PARAMS, Map.of("top_k", 3));
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 10000);
        return params;
    }
}
