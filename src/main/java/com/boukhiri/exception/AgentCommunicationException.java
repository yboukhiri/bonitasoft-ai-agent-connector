package com.boukhiri.exception;

/**
 * Exception thrown when communication with the AI Agent fails.
 * 
 * <p>This exception wraps various failure scenarios including:</p>
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>Timeout exceeded</li>
 *   <li>Invalid response from agent</li>
 *   <li>HTTP error status codes</li>
 * </ul>
 * 
 * <p>By using a domain-specific exception, we encapsulate the underlying
 * implementation details (HttpClient, JSON parsing, etc.) and provide
 * a clean abstraction to the connector layer.</p>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public class AgentCommunicationException extends Exception {

    private final int httpStatusCode;
    private final boolean isTimeout;

    /**
     * Creates an exception with a message.
     * 
     * @param message Description of what went wrong
     */
    public AgentCommunicationException(String message) {
        super(message);
        this.httpStatusCode = -1;
        this.isTimeout = false;
    }

    /**
     * Creates an exception with a message and cause.
     * 
     * @param message Description of what went wrong
     * @param cause The underlying exception
     */
    public AgentCommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = -1;
        this.isTimeout = cause instanceof java.net.http.HttpTimeoutException;
    }

    /**
     * Creates an exception for HTTP errors.
     * 
     * @param message Description of what went wrong
     * @param httpStatusCode The HTTP status code received
     */
    public AgentCommunicationException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.isTimeout = false;
    }

    /**
     * Gets the HTTP status code if this was an HTTP error.
     * 
     * @return HTTP status code, or -1 if not applicable
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Checks if this exception was caused by a timeout.
     * 
     * @return true if the request timed out
     */
    public boolean isTimeout() {
        return isTimeout;
    }

    /**
     * Checks if this was an HTTP error (4xx or 5xx status code).
     * 
     * @return true if an HTTP error status was received
     */
    public boolean isHttpError() {
        return httpStatusCode >= 400;
    }
}

