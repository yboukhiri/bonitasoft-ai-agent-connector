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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI Agent Connector for Bonita BPM.
 * 
 * <p>This connector enables Bonita processes to communicate with external AI Agents
 * via HTTP POST requests. It supports RAG-based question answering and other AI tasks.</p>
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
 *   <li>{@link #connect()} - Initializes the AgentClient</li>
 *   <li>{@link #executeBusinessLogic()} - Orchestrates the request/response flow</li>
 *   <li>{@link #disconnect()} - Cleanup</li>
 * </ol>
 * 
 * <h2>Inputs</h2>
 * <ul>
 *   <li><strong>input</strong> (Map, mandatory): The main input data (e.g., {question: "..."})</li>
 *   <li><strong>params</strong> (Map, optional): Parameters like top_k, min_confidence</li>
 *   <li><strong>timeoutMs</strong> (Integer, optional): Request timeout (default: 30000)</li>
 *   <li><strong>apiSecretKey</strong> (String, optional): API key for authentication</li>
 * </ul>
 * 
 * <h2>Outputs</h2>
 * <ul>
 *   <li><strong>status</strong>: ok, low_confidence, or error</li>
 *   <li><strong>output</strong>: Contains answer, sources, confidence, reasoning</li>
 *   <li><strong>usage</strong>: Performance metrics</li>
 *   <li><strong>error</strong>: Error message if failed</li>
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
                () -> getInputParameter(ConnectorConstants.INPUT_DATA),
                () -> getInputParameter(ConnectorConstants.INPUT_PARAMS),
                () -> getInputParameter(ConnectorConstants.INPUT_TIMEOUT_MS),
                () -> getInputParameter(ConnectorConstants.INPUT_API_SECRET_KEY)
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
            String apiSecretKey = getStringInput(ConnectorConstants.INPUT_API_SECRET_KEY);
            if (apiSecretKey != null) {
                agentClient.setApiSecretKey(apiSecretKey);
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
    @SuppressWarnings("unchecked")
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
            setErrorOutputs(e.getMessage());
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
    @SuppressWarnings("unchecked")
    private AgentRequest buildRequest() {
        Map<String, Object> inputData = (Map<String, Object>) getInputParameter(ConnectorConstants.INPUT_DATA);
        Map<String, Object> params = (Map<String, Object>) getInputParameter(ConnectorConstants.INPUT_PARAMS);
        
        return AgentRequest.builder()
                .input(inputData)
                .params(params)
                .build();
    }

    /**
     * Maps AgentResponse fields to connector outputs.
     */
    private void mapResponseToOutputs(AgentResponse response) {
        setOutputParameter(ConnectorConstants.OUTPUT_STATUS, response.getStatus());
        setOutputParameter(ConnectorConstants.OUTPUT_DATA, response.getOutput());
        setOutputParameter(ConnectorConstants.OUTPUT_USAGE, response.getUsage());
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR, response.getError());
    }

    /**
     * Sets error outputs when request fails.
     */
    private void setErrorOutputs(String errorMessage) {
        setOutputParameter(ConnectorConstants.OUTPUT_STATUS, ConnectorConstants.STATUS_ERROR);
        setOutputParameter(ConnectorConstants.OUTPUT_DATA, java.util.Collections.emptyMap());
        setOutputParameter(ConnectorConstants.OUTPUT_USAGE, java.util.Collections.emptyMap());
        setOutputParameter(ConnectorConstants.OUTPUT_ERROR, errorMessage);
    }

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
        return null;
    }
}
