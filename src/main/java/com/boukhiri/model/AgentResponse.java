package com.boukhiri.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable Data Transfer Object representing a response from the AI Agent.
 * 
 * <p>This class encapsulates the parsed response from the external AI Agent,
 * including status, output data, usage metrics, and any error information.</p>
 * 
 * <h2>Response Structure</h2>
 * <ul>
 *   <li><strong>status</strong>: ok, low_confidence, or error</li>
 *   <li><strong>output</strong>: Contains answer, sources, confidence, reasoning</li>
 *   <li><strong>usage</strong>: Performance metrics (latency, tokens, model)</li>
 *   <li><strong>error</strong>: Error message if status is "error"</li>
 * </ul>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class AgentResponse {

    private final String status;
    private final Map<String, Object> output;
    private final Map<String, Object> usage;
    private final String error;

    private AgentResponse(Builder builder) {
        this.status = builder.status;
        this.output = builder.output != null 
                ? Collections.unmodifiableMap(new HashMap<>(builder.output))
                : Collections.emptyMap();
        this.usage = builder.usage != null 
                ? Collections.unmodifiableMap(new HashMap<>(builder.usage))
                : Collections.emptyMap();
        this.error = builder.error;
    }

    /**
     * Gets the response status.
     * 
     * @return Status string (ok, low_confidence, or error)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the output data from the agent.
     * 
     * @return Unmodifiable map containing answer, sources, etc.
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * Gets the usage metrics.
     * 
     * @return Unmodifiable map containing latency, tokens, model info
     */
    public Map<String, Object> getUsage() {
        return usage;
    }

    /**
     * Gets the error message, if any.
     * 
     * @return Error message or null if no error
     */
    public String getError() {
        return error;
    }

    /**
     * Checks if this response indicates an error.
     * 
     * @return true if status is "error" or error message is present
     */
    public boolean isError() {
        return "error".equals(status) || error != null;
    }

    /**
     * Checks if this response indicates low confidence.
     * 
     * @return true if status is "low_confidence"
     */
    public boolean isLowConfidence() {
        return "low_confidence".equals(status);
    }

    /**
     * Creates a new builder for constructing AgentResponse instances.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an error response with the given message.
     * 
     * @param errorMessage The error message
     * @return AgentResponse with error status
     */
    public static AgentResponse error(String errorMessage) {
        return builder()
                .status("error")
                .error(errorMessage)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentResponse that = (AgentResponse) o;
        return Objects.equals(status, that.status) 
                && Objects.equals(output, that.output) 
                && Objects.equals(usage, that.usage) 
                && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, output, usage, error);
    }

    @Override
    public String toString() {
        return "AgentResponse{" +
                "status='" + status + '\'' +
                ", output=" + output +
                ", usage=" + usage +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * Builder for constructing AgentResponse instances.
     */
    public static final class Builder {
        private String status = "ok";
        private Map<String, Object> output;
        private Map<String, Object> usage;
        private String error;

        private Builder() {
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder output(Map<String, Object> output) {
            this.output = output;
            return this;
        }

        public Builder usage(Map<String, Object> usage) {
            this.usage = usage;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public AgentResponse build() {
            return new AgentResponse(this);
        }
    }
}

