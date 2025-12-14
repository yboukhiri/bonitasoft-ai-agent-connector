package com.boukhiri.client;

import com.boukhiri.exception.AgentCommunicationException;
import com.boukhiri.model.AgentRequest;
import com.boukhiri.model.AgentResponse;

/**
 * Interface defining the contract for communicating with AI Agents.
 * 
 * <p>This interface follows the Interface Segregation Principle (ISP) by
 * providing a minimal, focused API for agent communication. Implementations
 * can use different HTTP clients (Java HttpClient, OkHttp, etc.) without
 * affecting the connector logic.</p>
 * 
 * <p>Following the Dependency Inversion Principle (DIP), the connector
 * depends on this abstraction rather than concrete HTTP client implementations.</p>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public interface AgentClient {

    /**
     * Sends a request to the AI Agent and returns the response.
     * 
     * @param request The request data to send
     * @return The agent's response
     * @throws AgentCommunicationException if communication fails
     */
    AgentResponse sendRequest(AgentRequest request) throws AgentCommunicationException;

    /**
     * Configures the agent API URL.
     * 
     * @param agentUrl The agent API endpoint URL
     */
    void setAgentUrl(String agentUrl);

    /**
     * Configures the JWT Bearer token for authentication.
     * 
     * @param authToken The JWT Bearer token (will be sent as Authorization: Bearer token)
     */
    void setAuthToken(String authToken);

    /**
     * Configures the request timeout.
     * 
     * @param timeoutMs Timeout in milliseconds
     */
    void setTimeoutMs(int timeoutMs);

    /**
     * Checks if the client is properly configured and ready to make requests.
     * 
     * @return true if the client can make requests
     */
    boolean isConfigured();
}
