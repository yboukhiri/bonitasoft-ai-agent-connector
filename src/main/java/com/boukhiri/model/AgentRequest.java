package com.boukhiri.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable Data Transfer Object representing a request to the AI Agent.
 * 
 * <p>This class encapsulates all data needed to make a request to the external
 * AI Agent. It follows the Builder pattern for flexible construction and is
 * immutable after creation for thread safety.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * AgentRequest request = AgentRequest.builder()
 *     .input(Map.of("question", "What is the deadline?"))
 *     .params(Map.of("top_k", 3))
 *     .build();
 * </pre>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class AgentRequest {

    private final Map<String, Object> input;
    private final Map<String, Object> params;

    private AgentRequest(Builder builder) {
        // Create defensive copies for immutability
        this.input = builder.input != null 
                ? Collections.unmodifiableMap(new HashMap<>(builder.input))
                : Collections.emptyMap();
        this.params = builder.params != null 
                ? Collections.unmodifiableMap(new HashMap<>(builder.params))
                : Collections.emptyMap();
    }

    /**
     * Gets the input data for the agent.
     * 
     * @return Unmodifiable map of input data
     */
    public Map<String, Object> getInput() {
        return input;
    }

    /**
     * Gets the optional parameters for the agent.
     * 
     * @return Unmodifiable map of parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Converts this request to a Map suitable for JSON serialization.
     * 
     * @return Map representation of the request
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("input", input);
        map.put("params", params);
        return map;
    }

    /**
     * Creates a new builder for constructing AgentRequest instances.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentRequest that = (AgentRequest) o;
        return Objects.equals(input, that.input) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, params);
    }

    @Override
    public String toString() {
        return "AgentRequest{" +
                "input=" + input +
                ", params=" + params +
                '}';
    }

    /**
     * Builder for constructing AgentRequest instances.
     */
    public static final class Builder {
        private Map<String, Object> input;
        private Map<String, Object> params;

        private Builder() {
        }

        public Builder input(Map<String, Object> input) {
            this.input = input;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public AgentRequest build() {
            return new AgentRequest(this);
        }
    }
}

