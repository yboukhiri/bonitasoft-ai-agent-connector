package com.boukhiri.config;

/**
 * Centralized constants for the AI Agent Connector.
 * 
 * <p>This class holds all configuration values, input/output parameter names,
 * and default values. Following the Single Responsibility Principle, all
 * configuration is managed in one place for easy maintenance.</p>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class ConnectorConstants {

    private ConnectorConstants() {
        // Prevent instantiation - utility class
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INPUT PARAMETER NAMES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Agent API URL (e.g., http://localhost:8000/run) */
    public static final String INPUT_AGENT_URL = "agentUrl";
    
    /** JWT Bearer token for authentication */
    public static final String INPUT_AUTH_TOKEN = "authToken";
    
    /** The question to ask the RAG agent */
    public static final String INPUT_QUESTION = "question";
    
    /** OpenAI/Anthropic API key */
    public static final String INPUT_LLM_API_KEY = "llmApiKey";
    
    /** LLM model name (e.g., gpt-4o-mini) */
    public static final String INPUT_LLM_MODEL = "llmModel";
    
    /** Number of documents to retrieve (1-10) */
    public static final String INPUT_TOP_K = "topK";
    
    /** Confidence threshold (0.0-1.0) */
    public static final String INPUT_MIN_CONFIDENCE = "minConfidence";
    
    /** Include sources in response */
    public static final String INPUT_REQUIRE_SOURCES = "requireSources";
    
    /** Request timeout in milliseconds (1000-300000) */
    public static final String INPUT_TIMEOUT_MS = "timeoutMs";
    
    /** Maximum response tokens (100-4000) */
    public static final String INPUT_MAX_TOKENS = "maxTokens";
    
    /** LLM temperature (0.0-2.0) */
    public static final String INPUT_TEMPERATURE = "temperature";

    // ═══════════════════════════════════════════════════════════════════════════
    // OUTPUT PARAMETER NAMES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Response status: ok, low_confidence, or error */
    public static final String OUTPUT_STATUS = "status";
    
    /** The agent's answer text */
    public static final String OUTPUT_ANSWER = "answer";
    
    /** Source documents used (List of Maps with title/version) */
    public static final String OUTPUT_SOURCES = "sources";
    
    /** Confidence score (0.0-1.0) */
    public static final String OUTPUT_CONFIDENCE = "confidence";
    
    /** Explanation of how the answer was derived */
    public static final String OUTPUT_REASONING = "reasoning";
    
    /** Error code if status=error (e.g., VALIDATION_ERROR, LLM_ERROR) */
    public static final String OUTPUT_ERROR_CODE = "errorCode";
    
    /** Human-readable error message if status=error */
    public static final String OUTPUT_ERROR_MESSAGE = "errorMessage";

    // ═══════════════════════════════════════════════════════════════════════════
    // DEFAULT VALUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Default timeout in milliseconds (30 seconds) */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    
    /** Default agent endpoint */
    public static final String DEFAULT_AGENT_URL = "http://localhost:8000/run";
    
    /** Default LLM model */
    public static final String DEFAULT_LLM_MODEL = "gpt-4o-mini";
    
    /** Default number of documents to retrieve */
    public static final int DEFAULT_TOP_K = 3;
    
    /** Default confidence threshold */
    public static final double DEFAULT_MIN_CONFIDENCE = 0.0;
    
    /** Default: include sources in response */
    public static final boolean DEFAULT_REQUIRE_SOURCES = true;
    
    /** Default max tokens */
    public static final int DEFAULT_MAX_TOKENS = 700;
    
    /** Default temperature */
    public static final double DEFAULT_TEMPERATURE = 0.1;
    
    /** Environment variable name for agent URL override */
    public static final String ENV_AGENT_URL = "AI_AGENT_URL";

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION BOUNDS
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static final int MIN_TOP_K = 1;
    public static final int MAX_TOP_K = 10;
    
    public static final double MIN_CONFIDENCE = 0.0;
    public static final double MAX_CONFIDENCE = 1.0;
    
    public static final int MIN_TIMEOUT_MS = 1000;
    public static final int MAX_TIMEOUT_MS = 300000;
    
    public static final int MIN_MAX_TOKENS = 100;
    public static final int MAX_MAX_TOKENS = 4000;
    
    public static final double MIN_TEMPERATURE = 0.0;
    public static final double MAX_TEMPERATURE = 2.0;

    // ═══════════════════════════════════════════════════════════════════════════
    // HTTP CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** HTTP Content-Type header for JSON requests */
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    /** Authorization header prefix for Bearer tokens */
    public static final String AUTH_BEARER_PREFIX = "Bearer ";
    
    /** Fixed task type for RAG QA requests */
    public static final String TASK_RAG_QA = "rag_qa";

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE STATUS VALUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Status indicating successful response */
    public static final String STATUS_OK = "ok";
    
    /** Status indicating low confidence in the answer */
    public static final String STATUS_LOW_CONFIDENCE = "low_confidence";
    
    /** Status indicating an error occurred */
    public static final String STATUS_ERROR = "error";
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ERROR CODES
    // ═══════════════════════════════════════════════════════════════════════════
    
    public static final String ERROR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_UNSUPPORTED_TASK = "UNSUPPORTED_TASK";
    public static final String ERROR_TIMEOUT = "TIMEOUT_ERROR";
    public static final String ERROR_LLM = "LLM_ERROR";
    public static final String ERROR_INTERNAL = "INTERNAL_ERROR";
}
