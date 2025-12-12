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
    
    /** OpenAI-style API secret key for authentication */
    public static final String INPUT_API_SECRET_KEY = "apiSecretKey";
    
    /** Main input data as Map (e.g., {question: "..."}) */
    public static final String INPUT_DATA = "input";
    
    /** Optional parameters for agent customization */
    public static final String INPUT_PARAMS = "params";
    
    /** Request timeout in milliseconds */
    public static final String INPUT_TIMEOUT_MS = "timeoutMs";

    // ═══════════════════════════════════════════════════════════════════════════
    // OUTPUT PARAMETER NAMES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Response status: ok, low_confidence, error */
    public static final String OUTPUT_STATUS = "status";
    
    /** Main output containing answer, sources, confidence, reasoning */
    public static final String OUTPUT_DATA = "output";
    
    /** Performance metrics: latency_ms, tokens_in, tokens_out, model */
    public static final String OUTPUT_USAGE = "usage";
    
    /** Error message if request failed, null otherwise */
    public static final String OUTPUT_ERROR = "error";

    // ═══════════════════════════════════════════════════════════════════════════
    // DEFAULT VALUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Default timeout in milliseconds (30 seconds) */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    
    /** Default agent endpoint - can be overridden via environment variable */
    public static final String DEFAULT_AGENT_URL = "http://localhost:8000/run";
    
    /** Environment variable name for agent URL override */
    public static final String ENV_AGENT_URL = "AI_AGENT_URL";

    // ═══════════════════════════════════════════════════════════════════════════
    // HTTP CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** HTTP Content-Type header for JSON requests */
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    /** Authorization header prefix for Bearer tokens */
    public static final String AUTH_BEARER_PREFIX = "Bearer ";

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE STATUS VALUES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Status indicating successful response */
    public static final String STATUS_OK = "ok";
    
    /** Status indicating low confidence in the answer */
    public static final String STATUS_LOW_CONFIDENCE = "low_confidence";
    
    /** Status indicating an error occurred */
    public static final String STATUS_ERROR = "error";
}

