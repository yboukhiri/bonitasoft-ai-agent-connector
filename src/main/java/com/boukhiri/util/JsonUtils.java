package com.boukhiri.util;

import com.boukhiri.exception.AgentCommunicationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * Utility class for JSON serialization and deserialization.
 * 
 * <p>This class provides a centralized place for all JSON operations,
 * following the Single Responsibility Principle. It wraps the Gson library
 * to provide a clean API and consistent error handling.</p>
 * 
 * <p>Why Gson over Jackson?</p>
 * <ul>
 *   <li>Smaller footprint (single JAR vs multiple modules)</li>
 *   <li>Simpler API for basic Map serialization</li>
 *   <li>No annotation configuration needed</li>
 *   <li>Good performance for our use case</li>
 * </ul>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public final class JsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static final Gson GSON_COMPACT = new Gson();

    private JsonUtils() {
        // Prevent instantiation - utility class
    }

    /**
     * Serializes a Map to a JSON string.
     * 
     * @param data The Map to serialize
     * @return JSON string representation
     */
    public static String toJson(Map<String, Object> data) {
        return GSON_COMPACT.toJson(data);
    }

    /**
     * Serializes a Map to a pretty-printed JSON string.
     * 
     * @param data The Map to serialize
     * @return Pretty-printed JSON string
     */
    public static String toPrettyJson(Map<String, Object> data) {
        return GSON.toJson(data);
    }

    /**
     * Serializes any object to a JSON string.
     * 
     * @param object The object to serialize
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        return GSON_COMPACT.toJson(object);
    }

    /**
     * Deserializes a JSON string to a Map.
     * 
     * @param json The JSON string to parse
     * @return Map representation of the JSON
     * @throws AgentCommunicationException if parsing fails
     */
    public static Map<String, Object> fromJson(String json) throws AgentCommunicationException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return GSON.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        } catch (JsonSyntaxException e) {
            throw new AgentCommunicationException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a JSON string to a specific type.
     * 
     * @param json The JSON string to parse
     * @param clazz The target class
     * @param <T> The type to deserialize to
     * @return Instance of the specified type
     * @throws AgentCommunicationException if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws AgentCommunicationException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new AgentCommunicationException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a string is valid JSON.
     * 
     * @param json The string to validate
     * @return true if the string is valid JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            GSON.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}

