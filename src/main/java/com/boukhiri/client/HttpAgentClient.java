package com.boukhiri.client;

import com.boukhiri.config.ConnectorConstants;
import com.boukhiri.exception.AgentCommunicationException;
import com.boukhiri.model.AgentRequest;
import com.boukhiri.model.AgentResponse;
import com.boukhiri.util.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP implementation of the AgentClient interface.
 * 
 * <p>This class handles all HTTP communication with the AI Agent, including:</p>
 * <ul>
 *   <li>Request serialization (Java Map to JSON)</li>
 *   <li>HTTP POST with proper headers</li>
 *   <li>Response parsing (JSON to AgentResponse)</li>
 *   <li>Error handling and timeout management</li>
 * </ul>
 * 
 * <p>The class follows the Single Responsibility Principle by focusing solely
 * on HTTP communication. JSON handling is delegated to {@link JsonUtils}.</p>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public class HttpAgentClient implements AgentClient {

    private static final Logger LOGGER = Logger.getLogger(HttpAgentClient.class.getName());

    private final String agentUrl;
    private final HttpClient httpClient;
    private String apiSecretKey;
    private int timeoutMs;

    /**
     * Creates a new HttpAgentClient with the default agent URL.
     * 
     * <p>The URL is resolved in this order:</p>
     * <ol>
     *   <li>Environment variable AI_AGENT_URL if set</li>
     *   <li>Default value: http://localhost:8000/run</li>
     * </ol>
     */
    public HttpAgentClient() {
        this(resolveAgentUrl());
    }

    /**
     * Creates a new HttpAgentClient with a specific agent URL.
     * 
     * @param agentUrl The URL of the AI Agent endpoint
     */
    public HttpAgentClient(String agentUrl) {
        this.agentUrl = agentUrl;
        this.timeoutMs = ConnectorConstants.DEFAULT_TIMEOUT_MS;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        
        LOGGER.info("HttpAgentClient initialized with URL: " + agentUrl);
    }

    /**
     * Resolves the agent URL from environment/system property or defaults.
     * 
     * <p>Resolution order:</p>
     * <ol>
     *   <li>System property AI_AGENT_URL (for testing)</li>
     *   <li>Environment variable AI_AGENT_URL</li>
     *   <li>Default: http://localhost:8000/run</li>
     * </ol>
     */
    private static String resolveAgentUrl() {
        // Check system property first (useful for testing)
        String propUrl = System.getProperty(ConnectorConstants.ENV_AGENT_URL);
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }
        
        // Then check environment variable
        String envUrl = System.getenv(ConnectorConstants.ENV_AGENT_URL);
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        
        return ConnectorConstants.DEFAULT_AGENT_URL;
    }

    @Override
    public AgentResponse sendRequest(AgentRequest request) throws AgentCommunicationException {
        LOGGER.info("Sending request to AI Agent: " + agentUrl);
        
        try {
            // Serialize request to JSON
            String jsonPayload = JsonUtils.toJson(request.toMap());
            LOGGER.fine("Request payload: " + jsonPayload);
            
            // Build HTTP request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(agentUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", ConnectorConstants.CONTENT_TYPE_JSON)
                    .header("Accept", ConnectorConstants.CONTENT_TYPE_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
            
            // Add authorization header if API key is set
            if (apiSecretKey != null && !apiSecretKey.trim().isEmpty()) {
                requestBuilder.header("Authorization", 
                        ConnectorConstants.AUTH_BEARER_PREFIX + apiSecretKey.trim());
                LOGGER.fine("Authorization header added");
            }
            
            HttpRequest httpRequest = requestBuilder.build();
            
            // Execute request and measure latency
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Agent responded in %dms with status %d", 
                    latencyMs, response.statusCode()));
            
            // Process response
            return processResponse(response, latencyMs);
            
        } catch (HttpTimeoutException e) {
            LOGGER.log(Level.WARNING, "Request timed out after " + timeoutMs + "ms", e);
            throw new AgentCommunicationException("Request timed out after " + timeoutMs + "ms", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Network error: " + e.getMessage(), e);
            throw new AgentCommunicationException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentCommunicationException("Request interrupted", e);
        }
    }

    /**
     * Processes the HTTP response and converts it to an AgentResponse.
     */
    @SuppressWarnings("unchecked")
    private AgentResponse processResponse(HttpResponse<String> response, long latencyMs) 
            throws AgentCommunicationException {
        
        String body = response.body();
        
        // Check for HTTP errors
        if (response.statusCode() >= 400) {
            throw new AgentCommunicationException(
                    String.format("HTTP %d: %s", response.statusCode(), body),
                    response.statusCode());
        }
        
        // Parse JSON response
        Map<String, Object> responseMap = JsonUtils.fromJson(body);
        if (responseMap == null) {
            throw new AgentCommunicationException("Empty or null response from agent");
        }
        
        // Build AgentResponse
        AgentResponse.Builder builder = AgentResponse.builder()
                .status((String) responseMap.getOrDefault("status", ConnectorConstants.STATUS_OK));
        
        // Extract output
        Object output = responseMap.get("output");
        if (output instanceof Map) {
            builder.output((Map<String, Object>) output);
        }
        
        // Extract and enrich usage metrics
        Object usage = responseMap.get("usage");
        Map<String, Object> usageMap;
        if (usage instanceof Map) {
            usageMap = new java.util.HashMap<>((Map<String, Object>) usage);
        } else {
            usageMap = new java.util.HashMap<>();
        }
        // Ensure latency is recorded
        if (!usageMap.containsKey("latency_ms")) {
            usageMap.put("latency_ms", latencyMs);
        }
        builder.usage(usageMap);
        
        // Extract error
        Object error = responseMap.get("error");
        if (error != null) {
            builder.error(error.toString());
        }
        
        return builder.build();
    }

    @Override
    public void setApiSecretKey(String apiSecretKey) {
        this.apiSecretKey = apiSecretKey;
    }

    @Override
    public void setTimeoutMs(int timeoutMs) {
        if (timeoutMs > 0) {
            this.timeoutMs = timeoutMs;
        }
    }

    @Override
    public boolean isConfigured() {
        return agentUrl != null && !agentUrl.isEmpty();
    }

    /**
     * Gets the configured agent URL.
     * 
     * @return The agent URL
     */
    public String getAgentUrl() {
        return agentUrl;
    }
}

