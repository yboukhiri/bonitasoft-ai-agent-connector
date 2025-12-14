package com.boukhiri.model;

import com.boukhiri.config.ConnectorConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable Data Transfer Object representing a request to the RAG Agent API.
 * 
 * <p>This class encapsulates all data needed to make a request to the external
 * AI Agent. It follows the Builder pattern for flexible construction and is
 * immutable after creation for thread safety.</p>
 * 
 * <h2>API Request Structure</h2>
 * <pre>
 * {
 *   "task": "rag_qa",
 *   "input": {
 *     "question": "string (required)"
 *   },
 *   "params": {
 *     "top_k": 3,
 *     "min_confidence": 0.0,
 *     "require_sources": true,
 *     "llm_api_key": "sk-xxx",
 *     "llm_model": "gpt-4o-mini",
 *     "temperature": 0.1,
 *     "timeout_ms": 30000,
 *     "max_tokens": 700
 *   }
 * }
 * </pre>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * AgentRequest request = AgentRequest.builder()
 *     .question("What is the deadline for onboarding?")
 *     .llmApiKey("sk-xxx")
 *     .llmModel("gpt-4o-mini")
 *     .topK(3)
 *     .build();
 * </pre>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class AgentRequest {

    // Input fields
    private final String question;
    
    // Params fields
    private final int topK;
    private final double minConfidence;
    private final boolean requireSources;
    private final String llmApiUrl;
    private final String llmApiKey;
    private final String llmModel;
    private final double temperature;
    private final int timeoutMs;
    private final int maxTokens;

    private AgentRequest(Builder builder) {
        this.question = Objects.requireNonNull(builder.question, "question is required");
        this.topK = builder.topK;
        this.minConfidence = builder.minConfidence;
        this.requireSources = builder.requireSources;
        this.llmApiUrl = builder.llmApiUrl;
        this.llmApiKey = Objects.requireNonNull(builder.llmApiKey, "llmApiKey is required");
        this.llmModel = builder.llmModel;
        this.temperature = builder.temperature;
        this.timeoutMs = builder.timeoutMs;
        this.maxTokens = builder.maxTokens;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════════════

    public String getQuestion() {
        return question;
    }

    public int getTopK() {
        return topK;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public boolean isRequireSources() {
        return requireSources;
    }

    public String getLlmApiUrl() {
        return llmApiUrl;
    }

    public String getLlmApiKey() {
        return llmApiKey;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Converts this request to a Map suitable for JSON serialization.
     * 
     * <p>The structure matches the RAG Agent API contract exactly:</p>
     * <pre>
     * {
     *   "task": "rag_qa",
     *   "input": { "question": "..." },
     *   "params": { ... }
     * }
     * </pre>
     * 
     * @return Map representation of the request
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        // Fixed task type
        map.put("task", ConnectorConstants.TASK_RAG_QA);
        
        // Input section
        Map<String, Object> input = new HashMap<>();
        input.put("question", question);
        map.put("input", input);
        
        // Params section
        Map<String, Object> params = new HashMap<>();
        params.put("top_k", topK);
        params.put("min_confidence", minConfidence);
        params.put("require_sources", requireSources);
        params.put("llm_api_key", llmApiKey);
        params.put("llm_model", llmModel);
        params.put("temperature", temperature);
        params.put("timeout_ms", timeoutMs);
        params.put("max_tokens", maxTokens);
        
        // Optional: llm_api_url only if set
        if (llmApiUrl != null && !llmApiUrl.trim().isEmpty()) {
            params.put("llm_api_url", llmApiUrl);
        }
        
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
        return topK == that.topK 
                && Double.compare(that.minConfidence, minConfidence) == 0 
                && requireSources == that.requireSources 
                && Double.compare(that.temperature, temperature) == 0 
                && timeoutMs == that.timeoutMs 
                && maxTokens == that.maxTokens 
                && Objects.equals(question, that.question) 
                && Objects.equals(llmApiUrl, that.llmApiUrl) 
                && Objects.equals(llmApiKey, that.llmApiKey) 
                && Objects.equals(llmModel, that.llmModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, topK, minConfidence, requireSources, llmApiUrl, 
                llmApiKey, llmModel, temperature, timeoutMs, maxTokens);
    }

    @Override
    public String toString() {
        // Don't log sensitive data (llmApiKey)
        return "AgentRequest{" +
                "question='" + question + '\'' +
                ", topK=" + topK +
                ", minConfidence=" + minConfidence +
                ", requireSources=" + requireSources +
                ", llmModel='" + llmModel + '\'' +
                ", temperature=" + temperature +
                ", timeoutMs=" + timeoutMs +
                ", maxTokens=" + maxTokens +
                '}';
    }

    /**
     * Builder for constructing AgentRequest instances.
     * 
     * <p>All optional parameters have sensible defaults matching the API specification.</p>
     */
    public static final class Builder {
        private String question;
        private int topK = ConnectorConstants.DEFAULT_TOP_K;
        private double minConfidence = ConnectorConstants.DEFAULT_MIN_CONFIDENCE;
        private boolean requireSources = ConnectorConstants.DEFAULT_REQUIRE_SOURCES;
        private String llmApiUrl;
        private String llmApiKey;
        private String llmModel = ConnectorConstants.DEFAULT_LLM_MODEL;
        private double temperature = ConnectorConstants.DEFAULT_TEMPERATURE;
        private int timeoutMs = ConnectorConstants.DEFAULT_TIMEOUT_MS;
        private int maxTokens = ConnectorConstants.DEFAULT_MAX_TOKENS;

        private Builder() {
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        public Builder minConfidence(double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder requireSources(boolean requireSources) {
            this.requireSources = requireSources;
            return this;
        }

        public Builder llmApiUrl(String llmApiUrl) {
            this.llmApiUrl = llmApiUrl;
            return this;
        }

        public Builder llmApiKey(String llmApiKey) {
            this.llmApiKey = llmApiKey;
            return this;
        }

        public Builder llmModel(String llmModel) {
            this.llmModel = llmModel;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public AgentRequest build() {
            return new AgentRequest(this);
        }
    }
}
