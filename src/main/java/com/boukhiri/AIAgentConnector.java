package com.boukhiri;

import com.boukhiri.client.AgentClient;
import com.boukhiri.client.HttpAgentClient;
import com.boukhiri.config.ConnectorConstants;
import com.boukhiri.exception.AgentCommunicationException;
import com.boukhiri.model.AgentRequest;
import com.boukhiri.model.AgentResponse;
import com.boukhiri.validation.InputValidator;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI Agent Connector for Bonita BPM.
 * 
 * <p>This connector enables Bonita processes to communicate with a RAG (Retrieval-Augmented
 * Generation) Agent via HTTP POST requests. It sends questions to the agent and retrieves
 * answers with source documents and confidence scores.</p>
 * 
 * <h2>Architecture</h2>
 * <p>This class follows SOLID principles by acting as a thin orchestrator that
 * delegates responsibilities to specialized classes:</p>
 * <ul>
 *   <li>{@link InputValidator} - Handles all input validation</li>
 *   <li>{@link AgentClient} - Abstracts HTTP communication</li>
 *   <li>{@link AgentRequest}/{@link AgentResponse} - Immutable DTOs</li>
 * </ul>
 * 
 * <h2>Connector Lifecycle</h2>
 * <ol>
 *   <li>{@link #validateInputParameters()} - Validates inputs via InputValidator</li>
 *   <li>{@link #connect()} - Initializes the AgentClient with configuration</li>
 *   <li>{@link #executeBusinessLogic()} - Orchestrates the request/response flow</li>
 *   <li>{@link #disconnect()} - Cleanup</li>
 * </ol>
 * 
 * <h2>Inputs</h2>
 * <ul>
 *   <li><strong>agentUrl</strong> (String, mandatory): Agent API URL</li>
 *   <li><strong>authToken</strong> (String, optional): JWT Bearer token</li>
 *   <li><strong>question</strong> (String, mandatory): The question to ask</li>
 *   <li><strong>llmApiKey</strong> (String, mandatory): OpenAI/Anthropic API key</li>
 *   <li><strong>llmModel</strong> (String, optional): Model name (default: gpt-4o-mini)</li>
 *   <li><strong>topK</strong> (Integer, optional): Documents to retrieve (default: 3)</li>
 *   <li><strong>minConfidence</strong> (Double, optional): Confidence threshold (default: 0.0)</li>
 *   <li><strong>requireSources</strong> (Boolean, optional): Include sources (default: true)</li>
 *   <li><strong>timeoutMs</strong> (Integer, optional): Request timeout (default: 30000)</li>
 *   <li><strong>maxTokens</strong> (Integer, optional): Max response tokens (default: 700)</li>
 *   <li><strong>temperature</strong> (Double, optional): LLM temperature (default: 0.1)</li>
 * </ul>
 * 
 * <h2>Outputs</h2>
 * <ul>
 *   <li><strong>status</strong>: ok, low_confidence, or error</li>
 *   <li><strong>answer</strong>: The agent's answer text</li>
 *   <li><strong>sources</strong>: List of source documents used</li>
 *   <li><strong>confidence</strong>: Confidence score (0.0-1.0)</li>
 *   <li><strong>reasoning</strong>: How the answer was derived</li>
 *   <li><strong>errorCode</strong>: Error code if status=error</li>
 *   <li><strong>errorMessage</strong>: Error message if status=error</li>
 * </ul>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 * @see <a href="https://documentation.bonitasoft.com/bonita/latest/process/connector-archetype">Bonita Connector Archetype</a>
 */
public class AIAgentConnector extends AbstractConnector {

    private static final Logger LOGGER = Logger.getLogger(AIAgentConnector.class.getName());

    /** The HTTP client for agent communication */
    private AgentClient agentClient;

    // ═══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Default constructor required by Bonita.
     * 
     * <p>No business logic in constructor - following Bonita best practices.
     * All initialization happens in {@link #connect()}.</p>
     */
    public AIAgentConnector() {
        // No-op: Bonita requires a default constructor
    }

    /**
     * Constructor for dependency injection (testing).
     * 
     * @param agentClient The agent client to use
     */
    public AIAgentConnector(AgentClient agentClient) {
        this.agentClient = agentClient;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Validates all input parameters before execution.
     * 
     * <p>Delegates to {@link InputValidator} for all validation logic.</p>
     * 
     * @throws ConnectorValidationException if any validation fails
     */
    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        LOGGER.fine("Validating AI Agent Connector input parameters...");
        
        InputValidator validator = new InputValidator(
                this,
                () -> getInputParameter(ConnectorConstants.INPUT_AGENT_URL),
                () -> getInputParameter(ConnectorConstants.INPUT_AUTH_TOKEN),
                () -> getInputParameter(ConnectorConstants.INPUT_QUESTION),
                () -> getInputParameter(ConnectorConstants.INPUT_LLM_API_KEY),
                () -> getInputParameter(ConnectorConstants.INPUT_LLM_MODEL),
                () -> getInputParameter(ConnectorConstants.INPUT_TOP_K),
                () -> getInputParameter(ConnectorConstants.INPUT_MIN_CONFIDENCE),
                () -> getInputParameter(ConnectorConstants.INPUT_REQUIRE_SOURCES),
                () -> getInputParameter(ConnectorConstants.INPUT_TIMEOUT_MS),
                () -> getInputParameter(ConnectorConstants.INPUT_MAX_TOKENS),
                () -> getInputParameter(ConnectorConstants.INPUT_TEMPERATURE)
        );
        
        validator.validate();
    }

    /**
     * Initializes the HTTP client for agent communication.
     * 
     * @throws ConnectorException if initialization fails
     */
    @Override
    public void connect() throws ConnectorException {
        LOGGER.info("Initializing AI Agent Connector...");
        
        try {
            // Create client if not injected (normal case)
            if (agentClient == null) {
                agentClient = new HttpAgentClient();
            }
            
            // Configure client with input parameters
            String agentUrl = getStringInput(ConnectorConstants.INPUT_AGENT_URL);
            if (agentUrl != null) {
                agentClient.setAgentUrl(agentUrl);
            }
            
            String authToken = getStringInput(ConnectorConstants.INPUT_AUTH_TOKEN);
            if (authToken != null && !authToken.trim().isEmpty()) {
                agentClient.setAuthToken(authToken);
            }
            
            Integer timeout = getIntegerInput(ConnectorConstants.INPUT_TIMEOUT_MS);
            if (timeout != null) {
                agentClient.setTimeoutMs(timeout);
            } else {
                agentClient.setTimeoutMs(ConnectorConstants.DEFAULT_TIMEOUT_MS);
            }
            
            LOGGER.info("AI Agent Connector initialized successfully.");
            
        } catch (Exception e) {
            throw new ConnectorException("Failed to initialize connector: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the main business logic.
     * 
     * <p>This method:</p>
     * <ol>
     *   <li>Builds an AgentRequest from input parameters</li>
     *   <li>Sends the request via AgentClient</li>
     *   <li>Maps the AgentResponse to connector outputs</li>
     * </ol>
     * 
     * @throws ConnectorException if execution fails critically
     */
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        LOGGER.info("Executing AI Agent request...");
        
        try {
            // Build request from inputs
            AgentRequest request = buildRequest();
            
            // Send request and get response
            AgentResponse response = agentClient.sendRequest(request);
            
            // Map response to outputs
            mapResponseToOutputs(response);
            
            LOGGER.info("AI Agent request completed with status: " + response.getStatus());
            
        } catch (AgentCommunicationException e) {
            LOGGER.log(Level.WARNING, "Agent communication failed: " + e.getMessage(), e);
            setErrorOutputs(ConnectorConstants.ERROR_INTERNAL, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            throw new ConnectorException("Failed to execute AI Agent request: " + e.getMessage(), e);
        }
    }

    /**
     * Cleanup after execution.
     * 
     * @throws ConnectorException never thrown
     */
    @Override
    public void disconnect() throws ConnectorException {
        LOGGER.fine("Disconnecting AI Agent Connector...");
        agentClient = null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Builds an AgentRequest from connector inputs.
     */
    private AgentRequest buildRequest() {
        AgentRequest.Builder builder = AgentRequest.builder()
                .question(getStringInput(ConnectorConstants.INPUT_QUESTION))
                .llmApiKey(getStringInput(ConnectorConstants.INPUT_LLM_API_KEY));
        
        // Optional parameters with defaults
        String llmModel = getStringInput(ConnectorConstants.INPUT_LLM_MODEL);
        if (llmModel != null && !llmModel.trim().isEmpty()) {
            builder.llmModel(llmModel);
        }
        
        Integer topK = getIntegerInput(ConnectorConstants.INPUT_TOP_K);
        if (topK != null) {
            builder.topK(topK);
        }
        
        Double minConfidence = getDoubleInput(ConnectorConstants.INPUT_MIN_CONFIDENCE);
        if (minConfidence != null) {
            builder.minConfidence(minConfidence);
        }
        
        Boolean requireSources = getBooleanInput(ConnectorConstants.INPUT_REQUIRE_SOURCES);
        if (requireSources != null) {
            builder.requireSources(requireSources);
        }
        
        Integer timeoutMs = getIntegerInput(ConnectorConstants.INPUT_TIMEOUT_MS);
        if (timeoutMs != null) {
            builder.timeoutMs(timeoutMs);
        }
        
        Integer maxTokens = getIntegerInput(ConnectorConstants.INPUT_MAX_TOKENS);
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }
        
        Double temperature = getDoubleInput(ConnectorConstants.INPUT_TEMPERATURE);
        if (temperature != null) {
            builder.temperature(temperature);
        }
        
        return builder.build();
    }

    /**
     * Maps AgentResponse fields to connector outputs.
     */
    private void mapResponseToOutputs(AgentResponse response) {
        setOutputParameter(ConnectorConstants.OUTPUT_STATUS, response.getStatus());
        setOutputParameter(ConnectorConstants.OUTPUT_ANSWER, response.getAnswer());
        setOutputParameter(ConnectorConstants.OUTPUT_SOURCES, response.getSources());
        setOutputParameter(ConnectorConstants.OUTPUT_CONFIDENCE, response.getConfidence());
        setOutputParameter(ConnectorConstants.OUTPUT_REASONING, response.getReasoning());
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR_CODE, response.getErrorCode());
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR_MESSAGE, response.getErrorMessage());
    }

    /**
     * Sets error outputs when request fails.
     */
    private void setErrorOutputs(String errorCode, String errorMessage) {
        setOutputParameter(ConnectorConstants.OUTPUT_STATUS, ConnectorConstants.STATUS_ERROR);
        setOutputParameter(ConnectorConstants.OUTPUT_ANSWER, null);
        setOutputParameter(ConnectorConstants.OUTPUT_SOURCES, Collections.emptyList());
        setOutputParameter(ConnectorConstants.OUTPUT_CONFIDENCE, null);
        setOutputParameter(ConnectorConstants.OUTPUT_REASONING, null);
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR_CODE, errorCode);
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR_MESSAGE, errorMessage);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INPUT HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gets a String input parameter safely.
     */
    private String getStringInput(String paramName) {
        Object value = getInputParameter(paramName);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets an Integer input parameter safely.
     */
    private Integer getIntegerInput(String paramName) {
        Object value = getInputParameter(paramName);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Gets a Double input parameter safely.
     */
    private Double getDoubleInput(String paramName) {
        Object value = getInputParameter(paramName);
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * Gets a Boolean input parameter safely.
     */
    private Boolean getBooleanInput(String paramName) {
        Object value = getInputParameter(paramName);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
}
