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
    private String agentUrl;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        connector = new AIAgentConnector();
        wireMockServer.resetAll();
        agentUrl = "http://localhost:" + wireMockServer.port() + "/run";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should throw exception when agentUrl is missing")
    void should_throw_exception_when_agentUrl_is_missing() {
        Map<String, Object> params = createMinimalParams();
        params.remove(ConnectorConstants.INPUT_AGENT_URL);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("agentUrl");
    }

    @Test
    @DisplayName("Should throw exception when agentUrl is invalid")
    void should_throw_exception_when_agentUrl_is_invalid() {
        Map<String, Object> params = createMinimalParams();
        params.put(ConnectorConstants.INPUT_AGENT_URL, "not-a-valid-url");
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("URL");
    }

    @Test
    @DisplayName("Should throw exception when question is missing")
    void should_throw_exception_when_question_is_missing() {
        Map<String, Object> params = createMinimalParams();
        params.remove(ConnectorConstants.INPUT_QUESTION);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("question");
    }

    @Test
    @DisplayName("Should throw exception when llmApiKey is missing")
    void should_throw_exception_when_llmApiKey_is_missing() {
        Map<String, Object> params = createMinimalParams();
        params.remove(ConnectorConstants.INPUT_LLM_API_KEY);
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("llmApiKey");
    }

    @Test
    @DisplayName("Should throw exception when topK is out of range")
    void should_throw_exception_when_topK_is_out_of_range() {
        Map<String, Object> params = createMinimalParams();
        params.put(ConnectorConstants.INPUT_TOP_K, 15); // Out of range (1-10)
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("topK");
    }

    @Test
    @DisplayName("Should throw exception when timeoutMs is out of range")
    void should_throw_exception_when_timeoutMs_is_out_of_range() {
        Map<String, Object> params = createMinimalParams();
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 500); // Below minimum (1000)
        connector.setInputParameters(params);

        ConnectorValidationException exception = assertThrows(
                ConnectorValidationException.class,
                () -> connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).containsIgnoringCase("timeoutMs");
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
                    "sources": [
                        {"title": "Employee Onboarding Procedure", "version": "2023-06"}
                    ],
                    "confidence": 0.92,
                    "reasoning": "Found in onboarding policy document"
                },
                "usage": {
                    "latency_ms": 150,
                    "tokens_in": 45,
                    "tokens_out": 78,
                    "model": "gpt-4o-mini"
                },
                "error": null
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = createValidParameters();
        connector.setInputParameters(params);
        connector.connect();

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("ok");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ANSWER)).isNotNull();
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ANSWER).toString()).contains("5 business days");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_CONFIDENCE)).isEqualTo(0.92);
        assertThat(outputs.get(ConnectorConstants.OUTPUT_REASONING)).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sources = (List<Map<String, Object>>) outputs.get(ConnectorConstants.OUTPUT_SOURCES);
        assertThat(sources).hasSize(1);
        assertThat(sources.get(0)).containsKey("title");

        // Verify request was made with correct payload
        verify(postRequestedFor(urlEqualTo("/run"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing("rag_qa"))
                .withRequestBody(containing("question")));
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
                    "confidence": 0.35,
                    "reasoning": "Limited relevant documents found"
                },
                "usage": {"latency_ms": 100, "tokens_in": 30, "tokens_out": 40, "model": "gpt-4o-mini"},
                "error": null
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = createValidParameters();
        connector.setInputParameters(params);
        connector.connect();

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("low_confidence");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ANSWER)).isNotNull();
        assertThat(outputs.get(ConnectorConstants.OUTPUT_CONFIDENCE)).isEqualTo(0.35);
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR_CODE)).isNull();
    }

    @Test
    @DisplayName("Should handle error response from agent")
    void should_handle_error_response_from_agent() throws ConnectorException {
        // Arrange
        String agentResponse = """
            {
                "status": "error",
                "output": null,
                "usage": null,
                "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "Invalid question format",
                    "details": "Question cannot be empty"
                }
            }
            """;

        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(agentResponse)));

        Map<String, Object> params = createValidParameters();
        connector.setInputParameters(params);
        connector.connect();

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("error");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR_CODE)).isEqualTo("VALIDATION_ERROR");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR_MESSAGE)).asString().contains("Invalid question format");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ANSWER)).isNull();
    }

    @Test
    @DisplayName("Should include authorization header when authToken provided")
    void should_include_authorization_header() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ok\",\"output\":{\"answer\":\"test\",\"sources\":[],\"confidence\":0.9,\"reasoning\":\"test\"},\"usage\":{},\"error\":null}")));

        Map<String, Object> params = createValidParameters();
        params.put(ConnectorConstants.INPUT_AUTH_TOKEN, "jwt-token-12345");
        connector.setInputParameters(params);
        connector.connect();

        // Act
        connector.execute();

        // Assert - Verify authorization header was sent
        verify(postRequestedFor(urlEqualTo("/run"))
                .withHeader("Authorization", equalTo("Bearer jwt-token-12345")));
    }

    @Test
    @DisplayName("Should not include authorization header when authToken is empty")
    void should_not_include_authorization_header_when_empty() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ok\",\"output\":{\"answer\":\"test\",\"sources\":[],\"confidence\":0.9,\"reasoning\":\"test\"},\"usage\":{},\"error\":null}")));

        Map<String, Object> params = createValidParameters();
        params.put(ConnectorConstants.INPUT_AUTH_TOKEN, ""); // Empty token
        connector.setInputParameters(params);
        connector.connect();

        // Act
        connector.execute();

        // Assert - Verify authorization header was NOT sent
        verify(postRequestedFor(urlEqualTo("/run"))
                .withoutHeader("Authorization"));
    }

    @Test
    @DisplayName("Should handle HTTP 500 error from agent")
    void should_handle_http_500_error() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Map<String, Object> params = createValidParameters();
        connector.setInputParameters(params);
        connector.connect();

        // Act
        Map<String, Object> outputs = connector.execute();

        // Assert
        assertThat(outputs.get(ConnectorConstants.OUTPUT_STATUS)).isEqualTo("error");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR_CODE)).isEqualTo("INTERNAL_ERROR");
        assertThat(outputs.get(ConnectorConstants.OUTPUT_ERROR_MESSAGE)).asString().contains("500");
    }

    @Test
    @DisplayName("Should use custom parameters in request")
    void should_use_custom_parameters_in_request() throws ConnectorException {
        // Arrange
        stubFor(post(urlEqualTo("/run"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ok\",\"output\":{\"answer\":\"test\",\"sources\":[],\"confidence\":0.9,\"reasoning\":\"test\"},\"usage\":{},\"error\":null}")));

        Map<String, Object> params = createValidParameters();
        params.put(ConnectorConstants.INPUT_TOP_K, 5);
        params.put(ConnectorConstants.INPUT_MIN_CONFIDENCE, 0.7);
        params.put(ConnectorConstants.INPUT_LLM_MODEL, "gpt-4");
        params.put(ConnectorConstants.INPUT_TEMPERATURE, 0.5);
        params.put(ConnectorConstants.INPUT_MAX_TOKENS, 1000);
        params.put(ConnectorConstants.INPUT_REQUIRE_SOURCES, false);
        connector.setInputParameters(params);
        connector.connect();

        // Act
        connector.execute();

        // Assert - Verify request contains custom parameters
        verify(postRequestedFor(urlEqualTo("/run"))
                .withRequestBody(containing("\"top_k\":5"))
                .withRequestBody(containing("\"min_confidence\":0.7"))
                .withRequestBody(containing("\"llm_model\":\"gpt-4\""))
                .withRequestBody(containing("\"temperature\":0.5"))
                .withRequestBody(containing("\"max_tokens\":1000"))
                .withRequestBody(containing("\"require_sources\":false")));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates minimal valid parameters (only mandatory fields).
     */
    private Map<String, Object> createMinimalParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_AGENT_URL, agentUrl);
        params.put(ConnectorConstants.INPUT_QUESTION, "What is the deadline?");
        params.put(ConnectorConstants.INPUT_LLM_API_KEY, "sk-test-key");
        return params;
    }

    /**
     * Creates a valid set of parameters for testing.
     */
    private Map<String, Object> createValidParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(ConnectorConstants.INPUT_AGENT_URL, agentUrl);
        params.put(ConnectorConstants.INPUT_QUESTION, "What is the deadline for onboarding?");
        params.put(ConnectorConstants.INPUT_LLM_API_KEY, "sk-test-key");
        params.put(ConnectorConstants.INPUT_LLM_MODEL, "gpt-4o-mini");
        params.put(ConnectorConstants.INPUT_TOP_K, 3);
        params.put(ConnectorConstants.INPUT_TIMEOUT_MS, 10000);
        return params;
    }
}
