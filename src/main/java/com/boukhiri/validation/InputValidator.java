package com.boukhiri.validation;

import com.boukhiri.config.ConnectorConstants;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Validates connector input parameters for the RAG Agent API.
 * 
 * <p>This class follows the Single Responsibility Principle by focusing
 * solely on input validation. It validates all parameters according to
 * the API specification constraints.</p>
 * 
 * <h2>Validation Rules</h2>
 * <ul>
 *   <li><strong>agentUrl</strong>: Mandatory, valid URL</li>
 *   <li><strong>question</strong>: Mandatory, non-empty string</li>
 *   <li><strong>llmApiKey</strong>: Mandatory, non-empty string</li>
 *   <li><strong>topK</strong>: Optional, integer 1-10</li>
 *   <li><strong>minConfidence</strong>: Optional, double 0.0-1.0</li>
 *   <li><strong>requireSources</strong>: Optional, boolean</li>
 *   <li><strong>timeoutMs</strong>: Optional, integer 1000-300000</li>
 *   <li><strong>maxTokens</strong>: Optional, integer 100-4000</li>
 *   <li><strong>temperature</strong>: Optional, double 0.0-2.0</li>
 * </ul>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public class InputValidator {

    private static final Logger LOGGER = Logger.getLogger(InputValidator.class.getName());

    private final Connector connectorInstance;
    private final Supplier<Object> agentUrlSupplier;
    private final Supplier<Object> authTokenSupplier;
    private final Supplier<Object> questionSupplier;
    private final Supplier<Object> llmApiKeySupplier;
    private final Supplier<Object> llmModelSupplier;
    private final Supplier<Object> topKSupplier;
    private final Supplier<Object> minConfidenceSupplier;
    private final Supplier<Object> requireSourcesSupplier;
    private final Supplier<Object> timeoutMsSupplier;
    private final Supplier<Object> maxTokensSupplier;
    private final Supplier<Object> temperatureSupplier;

    /**
     * Creates a new InputValidator with all parameter suppliers.
     * 
     * @param connectorInstance The connector instance (for exception context)
     * @param agentUrlSupplier Supplier for the agent URL parameter
     * @param authTokenSupplier Supplier for the auth token parameter
     * @param questionSupplier Supplier for the question parameter
     * @param llmApiKeySupplier Supplier for the LLM API key parameter
     * @param llmModelSupplier Supplier for the LLM model parameter
     * @param topKSupplier Supplier for the top_k parameter
     * @param minConfidenceSupplier Supplier for the min_confidence parameter
     * @param requireSourcesSupplier Supplier for the require_sources parameter
     * @param timeoutMsSupplier Supplier for the timeout_ms parameter
     * @param maxTokensSupplier Supplier for the max_tokens parameter
     * @param temperatureSupplier Supplier for the temperature parameter
     */
    public InputValidator(
            Connector connectorInstance,
            Supplier<Object> agentUrlSupplier,
            Supplier<Object> authTokenSupplier,
            Supplier<Object> questionSupplier,
            Supplier<Object> llmApiKeySupplier,
            Supplier<Object> llmModelSupplier,
            Supplier<Object> topKSupplier,
            Supplier<Object> minConfidenceSupplier,
            Supplier<Object> requireSourcesSupplier,
            Supplier<Object> timeoutMsSupplier,
            Supplier<Object> maxTokensSupplier,
            Supplier<Object> temperatureSupplier) {
        this.connectorInstance = connectorInstance;
        this.agentUrlSupplier = agentUrlSupplier;
        this.authTokenSupplier = authTokenSupplier;
        this.questionSupplier = questionSupplier;
        this.llmApiKeySupplier = llmApiKeySupplier;
        this.llmModelSupplier = llmModelSupplier;
        this.topKSupplier = topKSupplier;
        this.minConfidenceSupplier = minConfidenceSupplier;
        this.requireSourcesSupplier = requireSourcesSupplier;
        this.timeoutMsSupplier = timeoutMsSupplier;
        this.maxTokensSupplier = maxTokensSupplier;
        this.temperatureSupplier = temperatureSupplier;
    }

    /**
     * Validates all input parameters.
     * 
     * @throws ConnectorValidationException if any validation fails
     */
    public void validate() throws ConnectorValidationException {
        LOGGER.fine("Starting input validation...");
        
        List<String> errors = new ArrayList<>();
        
        // Validate mandatory parameters
        validateAgentUrl(errors);
        validateQuestion(errors);
        validateLlmApiKey(errors);
        
        // Validate optional parameters with bounds
        validateAuthToken(errors);
        validateLlmModel(errors);
        validateTopK(errors);
        validateMinConfidence(errors);
        validateRequireSources(errors);
        validateTimeoutMs(errors);
        validateMaxTokens(errors);
        validateTemperature(errors);
        
        if (!errors.isEmpty()) {
            String message = String.join("; ", errors);
            LOGGER.warning("Validation failed: " + message);
            throw new ConnectorValidationException(connectorInstance, message);
        }
        
        LOGGER.fine("All input parameters validated successfully.");
    }

    /**
     * Validates the agentUrl parameter (mandatory, valid URL).
     */
    private void validateAgentUrl(List<String> errors) {
        Object agentUrl = agentUrlSupplier.get();
        
        if (agentUrl == null || agentUrl.toString().trim().isEmpty()) {
            errors.add("Mandatory parameter '" + ConnectorConstants.INPUT_AGENT_URL + "' is missing. " +
                    "Please provide the Agent API URL (e.g., http://localhost:8000/run)");
            return;
        }
        
        String urlStr = agentUrl.toString().trim();
        try {
            new URL(urlStr);
        } catch (MalformedURLException e) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_AGENT_URL + "' is not a valid URL: " + urlStr);
        }
    }

    /**
     * Validates the authToken parameter (optional, string).
     */
    private void validateAuthToken(List<String> errors) {
        Object authToken = authTokenSupplier.get();
        
        if (authToken != null && !(authToken instanceof String)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_AUTH_TOKEN + "' must be a String, " +
                    "but received: " + authToken.getClass().getSimpleName());
        }
        // Empty string is allowed (means no auth / auth disabled)
    }

    /**
     * Validates the question parameter (mandatory, non-empty string).
     */
    private void validateQuestion(List<String> errors) {
        Object question = questionSupplier.get();
        
        if (question == null || question.toString().trim().isEmpty()) {
            errors.add("Mandatory parameter '" + ConnectorConstants.INPUT_QUESTION + "' is missing. " +
                    "Please provide a question to ask the RAG agent");
            return;
        }
        
        if (!(question instanceof String)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_QUESTION + "' must be a String, " +
                    "but received: " + question.getClass().getSimpleName());
        }
    }

    /**
     * Validates the llmApiKey parameter (mandatory, non-empty string).
     */
    private void validateLlmApiKey(List<String> errors) {
        Object llmApiKey = llmApiKeySupplier.get();
        
        if (llmApiKey == null || llmApiKey.toString().trim().isEmpty()) {
            errors.add("Mandatory parameter '" + ConnectorConstants.INPUT_LLM_API_KEY + "' is missing. " +
                    "Please provide an OpenAI/Anthropic API key");
            return;
        }
        
        if (!(llmApiKey instanceof String)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_LLM_API_KEY + "' must be a String, " +
                    "but received: " + llmApiKey.getClass().getSimpleName());
        }
    }

    /**
     * Validates the llmModel parameter (optional, string).
     */
    private void validateLlmModel(List<String> errors) {
        Object llmModel = llmModelSupplier.get();
        
        if (llmModel != null && !(llmModel instanceof String)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_LLM_MODEL + "' must be a String, " +
                    "but received: " + llmModel.getClass().getSimpleName());
        }
    }

    /**
     * Validates the topK parameter (optional, integer 1-10).
     */
    private void validateTopK(List<String> errors) {
        Object topK = topKSupplier.get();
        
        if (topK == null) {
            return; // Optional parameter
        }
        
        if (!(topK instanceof Integer)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TOP_K + "' must be an Integer, " +
                    "but received: " + topK.getClass().getSimpleName());
            return;
        }
        
        int value = (Integer) topK;
        if (value < ConnectorConstants.MIN_TOP_K || value > ConnectorConstants.MAX_TOP_K) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TOP_K + "' must be between " +
                    ConnectorConstants.MIN_TOP_K + " and " + ConnectorConstants.MAX_TOP_K + 
                    ", but received: " + value);
        }
    }

    /**
     * Validates the minConfidence parameter (optional, double 0.0-1.0).
     */
    private void validateMinConfidence(List<String> errors) {
        Object minConfidence = minConfidenceSupplier.get();
        
        if (minConfidence == null) {
            return; // Optional parameter
        }
        
        if (!(minConfidence instanceof Number)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_MIN_CONFIDENCE + "' must be a Number, " +
                    "but received: " + minConfidence.getClass().getSimpleName());
            return;
        }
        
        double value = ((Number) minConfidence).doubleValue();
        if (value < ConnectorConstants.MIN_CONFIDENCE || value > ConnectorConstants.MAX_CONFIDENCE) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_MIN_CONFIDENCE + "' must be between " +
                    ConnectorConstants.MIN_CONFIDENCE + " and " + ConnectorConstants.MAX_CONFIDENCE + 
                    ", but received: " + value);
        }
    }

    /**
     * Validates the requireSources parameter (optional, boolean).
     */
    private void validateRequireSources(List<String> errors) {
        Object requireSources = requireSourcesSupplier.get();
        
        if (requireSources != null && !(requireSources instanceof Boolean)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_REQUIRE_SOURCES + "' must be a Boolean, " +
                    "but received: " + requireSources.getClass().getSimpleName());
        }
    }

    /**
     * Validates the timeoutMs parameter (optional, integer 1000-300000).
     */
    private void validateTimeoutMs(List<String> errors) {
        Object timeoutMs = timeoutMsSupplier.get();
        
        if (timeoutMs == null) {
            return; // Optional parameter
        }
        
        if (!(timeoutMs instanceof Integer)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TIMEOUT_MS + "' must be an Integer, " +
                    "but received: " + timeoutMs.getClass().getSimpleName());
            return;
        }
        
        int value = (Integer) timeoutMs;
        if (value < ConnectorConstants.MIN_TIMEOUT_MS || value > ConnectorConstants.MAX_TIMEOUT_MS) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TIMEOUT_MS + "' must be between " +
                    ConnectorConstants.MIN_TIMEOUT_MS + " and " + ConnectorConstants.MAX_TIMEOUT_MS + 
                    ", but received: " + value);
        }
    }

    /**
     * Validates the maxTokens parameter (optional, integer 100-4000).
     */
    private void validateMaxTokens(List<String> errors) {
        Object maxTokens = maxTokensSupplier.get();
        
        if (maxTokens == null) {
            return; // Optional parameter
        }
        
        if (!(maxTokens instanceof Integer)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_MAX_TOKENS + "' must be an Integer, " +
                    "but received: " + maxTokens.getClass().getSimpleName());
            return;
        }
        
        int value = (Integer) maxTokens;
        if (value < ConnectorConstants.MIN_MAX_TOKENS || value > ConnectorConstants.MAX_MAX_TOKENS) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_MAX_TOKENS + "' must be between " +
                    ConnectorConstants.MIN_MAX_TOKENS + " and " + ConnectorConstants.MAX_MAX_TOKENS + 
                    ", but received: " + value);
        }
    }

    /**
     * Validates the temperature parameter (optional, double 0.0-2.0).
     */
    private void validateTemperature(List<String> errors) {
        Object temperature = temperatureSupplier.get();
        
        if (temperature == null) {
            return; // Optional parameter
        }
        
        if (!(temperature instanceof Number)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TEMPERATURE + "' must be a Number, " +
                    "but received: " + temperature.getClass().getSimpleName());
            return;
        }
        
        double value = ((Number) temperature).doubleValue();
        if (value < ConnectorConstants.MIN_TEMPERATURE || value > ConnectorConstants.MAX_TEMPERATURE) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_TEMPERATURE + "' must be between " +
                    ConnectorConstants.MIN_TEMPERATURE + " and " + ConnectorConstants.MAX_TEMPERATURE + 
                    ", but received: " + value);
        }
    }
}
