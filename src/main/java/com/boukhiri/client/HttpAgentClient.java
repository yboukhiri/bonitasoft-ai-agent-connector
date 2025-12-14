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
 * HTTP implementation of the AgentClient interface for the RAG Agent API.
 * 
 * <p>This class handles all HTTP communication with the AI Agent, including:</p>
 * <ul>
 *   <li>Request serialization (AgentRequest to JSON)</li>
 *   <li>HTTP POST with proper headers (Content-Type, Authorization)</li>
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

    private String agentUrl;
    private String authToken;
    private int timeoutMs;
    private HttpClient httpClient;

    /**
     * Creates a new HttpAgentClient with default settings.
     * 
     * <p>The client must be configured with {@link #setAgentUrl(String)} before use.</p>
     */
    public HttpAgentClient() {
        this.timeoutMs = ConnectorConstants.DEFAULT_TIMEOUT_MS;
        this.agentUrl = ConnectorConstants.DEFAULT_AGENT_URL;
        initHttpClient();
    }

    /**
     * Creates a new HttpAgentClient with a specific agent URL.
     * 
     * @param agentUrl The URL of the AI Agent endpoint
     */
    public HttpAgentClient(String agentUrl) {
        this.agentUrl = agentUrl;
        this.timeoutMs = ConnectorConstants.DEFAULT_TIMEOUT_MS;
        initHttpClient();
    }

    /**
     * Initializes the HTTP client with current timeout settings.
     */
    private void initHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public AgentResponse sendRequest(AgentRequest request) throws AgentCommunicationException {
        if (agentUrl == null || agentUrl.trim().isEmpty()) {
            throw new AgentCommunicationException("Agent URL is not configured");
        }

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
            
            // Add authorization header if auth token is set
            if (authToken != null && !authToken.trim().isEmpty()) {
                requestBuilder.header("Authorization", 
                        ConnectorConstants.AUTH_BEARER_PREFIX + authToken.trim());
                LOGGER.fine("Authorization header added");
            }
            
            HttpRequest httpRequest = requestBuilder.build();
            
            // Execute request and measure latency
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Agent responded in %dms with HTTP status %d", 
                    latencyMs, response.statusCode()));
            
            // Process response
            return processResponse(response);
            
        } catch (HttpTimeoutException e) {
            LOGGER.log(Level.WARNING, "Request timed out after " + timeoutMs + "ms", e);
            return AgentResponse.error(ConnectorConstants.ERROR_TIMEOUT, 
                    "Request timed out after " + timeoutMs + "ms");
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
    private AgentResponse processResponse(HttpResponse<String> response) 
            throws AgentCommunicationException {
        
        String body = response.body();
        LOGGER.fine("Response body: " + body);
        
        // Check for HTTP errors
        if (response.statusCode() >= 400) {
            // Try to parse error response from body
            try {
                Map<String, Object> errorMap = JsonUtils.fromJson(body);
                if (errorMap != null) {
                    return AgentResponse.fromMap(errorMap);
                }
            } catch (Exception ignored) {
                // Failed to parse, return generic error
            }
            
            return AgentResponse.error(
                    ConnectorConstants.ERROR_INTERNAL,
                    String.format("HTTP %d: %s", response.statusCode(), body));
        }
        
        // Parse JSON response
        Map<String, Object> responseMap = JsonUtils.fromJson(body);
        if (responseMap == null) {
            throw new AgentCommunicationException("Empty or null response from agent");
        }
        
        // Convert to AgentResponse using the fromMap factory method
        return AgentResponse.fromMap(responseMap);
    }

    @Override
    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
        LOGGER.fine("Agent URL set to: " + agentUrl);
    }

    @Override
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        LOGGER.fine("Auth token configured");
    }

    @Override
    public void setTimeoutMs(int timeoutMs) {
        if (timeoutMs > 0) {
            this.timeoutMs = timeoutMs;
            // Recreate HTTP client with new timeout
            initHttpClient();
            LOGGER.fine("Timeout set to: " + timeoutMs + "ms");
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

    /**
     * Gets the configured timeout in milliseconds.
     * 
     * @return The timeout in milliseconds
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }
}
