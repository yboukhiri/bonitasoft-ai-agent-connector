package com.boukhiri.model;

import com.boukhiri.config.ConnectorConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable Data Transfer Object representing a response from the RAG Agent API.
 * 
 * <p>This class encapsulates the parsed response from the external AI Agent,
 * including status, output data, usage metrics, and any error information.</p>
 * 
 * <h2>Success Response Structure (status: "ok" or "low_confidence")</h2>
 * <pre>
 * {
 *   "status": "ok",
 *   "output": {
 *     "answer": "The answer text...",
 *     "sources": [{"title": "Document Title", "version": "2023-06"}],
 *     "confidence": 0.85,
 *     "reasoning": "Explanation of how the answer was derived"
 *   },
 *   "usage": {
 *     "latency_ms": 1234,
 *     "model": "gpt-4o-mini",
 *     "tokens_in": 150,
 *     "tokens_out": 50
 *   },
 *   "error": null
 * }
 * </pre>
 * 
 * <h2>Error Response Structure (status: "error")</h2>
 * <pre>
 * {
 *   "status": "error",
 *   "output": null,
 *   "usage": null,
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Human-readable error message",
 *     "details": "Technical details for debugging"
 *   }
 * }
 * </pre>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class AgentResponse {

    private final String status;
    
    // Output fields (from output object)
    private final String answer;
    private final List<Map<String, Object>> sources;
    private final Double confidence;
    private final String reasoning;
    
    // Error fields (from error object)
    private final String errorCode;
    private final String errorMessage;
    private final String errorDetails;

    private AgentResponse(Builder builder) {
        this.status = builder.status != null ? builder.status : ConnectorConstants.STATUS_OK;
        this.answer = builder.answer;
        this.sources = builder.sources != null 
                ? Collections.unmodifiableList(builder.sources)
                : Collections.emptyList();
        this.confidence = builder.confidence;
        this.reasoning = builder.reasoning;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.errorDetails = builder.errorDetails;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gets the response status.
     * 
     * @return Status string (ok, low_confidence, or error)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the agent's answer text.
     * 
     * @return The answer text, or null if error
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Gets the source documents used to generate the answer.
     * 
     * @return Unmodifiable list of source maps (each with title/version)
     */
    public List<Map<String, Object>> getSources() {
        return sources;
    }

    /**
     * Gets the confidence score.
     * 
     * @return Confidence score (0.0-1.0), or null if error
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * Gets the reasoning explanation.
     * 
     * @return Explanation of how the answer was derived, or null if error
     */
    public String getReasoning() {
        return reasoning;
    }

    /**
     * Gets the error code if status is error.
     * 
     * @return Error code (e.g., VALIDATION_ERROR, LLM_ERROR), or null if no error
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the human-readable error message.
     * 
     * @return Error message, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the technical error details.
     * 
     * @return Technical details for debugging, or null if no error
     */
    public String getErrorDetails() {
        return errorDetails;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATUS HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Checks if this response indicates an error.
     * 
     * @return true if status is "error"
     */
    public boolean isError() {
        return ConnectorConstants.STATUS_ERROR.equals(status);
    }

    /**
     * Checks if this response indicates low confidence.
     * 
     * @return true if status is "low_confidence"
     */
    public boolean isLowConfidence() {
        return ConnectorConstants.STATUS_LOW_CONFIDENCE.equals(status);
    }

    /**
     * Checks if this response indicates success (ok or low_confidence).
     * 
     * @return true if status is "ok" or "low_confidence"
     */
    public boolean isSuccess() {
        return ConnectorConstants.STATUS_OK.equals(status) 
                || ConnectorConstants.STATUS_LOW_CONFIDENCE.equals(status);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a new builder for constructing AgentResponse instances.
     * 
     * @return New Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an error response with the given code and message.
     * 
     * @param errorCode The error code
     * @param errorMessage The human-readable error message
     * @return AgentResponse with error status
     */
    public static AgentResponse error(String errorCode, String errorMessage) {
        return builder()
                .status(ConnectorConstants.STATUS_ERROR)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Creates an error response from an exception.
     * 
     * @param errorCode The error code
     * @param e The exception
     * @return AgentResponse with error status
     */
    public static AgentResponse error(String errorCode, Exception e) {
        return builder()
                .status(ConnectorConstants.STATUS_ERROR)
                .errorCode(errorCode)
                .errorMessage(e.getMessage())
                .errorDetails(e.getClass().getName())
                .build();
    }

    /**
     * Parses a raw API response map into an AgentResponse.
     * 
     * @param responseMap The parsed JSON response as a Map
     * @return AgentResponse instance
     */
    @SuppressWarnings("unchecked")
    public static AgentResponse fromMap(Map<String, Object> responseMap) {
        if (responseMap == null) {
            return error(ConnectorConstants.ERROR_INTERNAL, "Empty response from agent");
        }

        Builder builder = builder()
                .status((String) responseMap.get("status"));

        // Parse output section (for success responses)
        Object output = responseMap.get("output");
        if (output instanceof Map) {
            Map<String, Object> outputMap = (Map<String, Object>) output;
            builder.answer((String) outputMap.get("answer"));
            builder.reasoning((String) outputMap.get("reasoning"));
            
            // Parse confidence (can be Double or Integer from JSON)
            Object conf = outputMap.get("confidence");
            if (conf instanceof Number) {
                builder.confidence(((Number) conf).doubleValue());
            }
            
            // Parse sources list
            Object sourcesObj = outputMap.get("sources");
            if (sourcesObj instanceof List) {
                builder.sources((List<Map<String, Object>>) sourcesObj);
            }
        }

        // Parse error section (for error responses)
        Object error = responseMap.get("error");
        if (error instanceof Map) {
            Map<String, Object> errorMap = (Map<String, Object>) error;
            builder.errorCode((String) errorMap.get("code"));
            builder.errorMessage((String) errorMap.get("message"));
            builder.errorDetails((String) errorMap.get("details"));
        } else if (error instanceof String) {
            // Handle simple string error (legacy format)
            builder.errorMessage((String) error);
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentResponse that = (AgentResponse) o;
        return Objects.equals(status, that.status) 
                && Objects.equals(answer, that.answer) 
                && Objects.equals(sources, that.sources) 
                && Objects.equals(confidence, that.confidence) 
                && Objects.equals(reasoning, that.reasoning) 
                && Objects.equals(errorCode, that.errorCode) 
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, answer, sources, confidence, reasoning, errorCode, errorMessage);
    }

    @Override
    public String toString() {
        if (isError()) {
            return "AgentResponse{" +
                    "status='" + status + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
        return "AgentResponse{" +
                "status='" + status + '\'' +
                ", answer='" + (answer != null ? answer.substring(0, Math.min(50, answer.length())) + "..." : null) + '\'' +
                ", confidence=" + confidence +
                ", sources=" + sources.size() + " documents" +
                '}';
    }

    /**
     * Builder for constructing AgentResponse instances.
     */
    public static final class Builder {
        private String status;
        private String answer;
        private List<Map<String, Object>> sources;
        private Double confidence;
        private String reasoning;
        private String errorCode;
        private String errorMessage;
        private String errorDetails;

        private Builder() {
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder answer(String answer) {
            this.answer = answer;
            return this;
        }

        public Builder sources(List<Map<String, Object>> sources) {
            this.sources = sources;
            return this;
        }

        public Builder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public AgentResponse build() {
            return new AgentResponse(this);
        }
    }
}
