package com.boukhiri.validation;

import com.boukhiri.config.ConnectorConstants;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Validates connector input parameters.
 * 
 * <p>This class follows the Single Responsibility Principle by focusing
 * solely on input validation. It provides both individual validation methods
 * and a fluent API for building validation chains.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * InputValidator validator = new InputValidator(this::getInputParameter);
 * validator.validate(); // Throws ConnectorValidationException if invalid
 * </pre>
 * 
 * @author Yassine Boukhiri
 * @version 1.0.0
 */
public class InputValidator {

    private static final Logger LOGGER = Logger.getLogger(InputValidator.class.getName());

    private final Supplier<Object> inputDataSupplier;
    private final Supplier<Object> paramsSupplier;
    private final Supplier<Object> timeoutSupplier;
    private final Supplier<Object> apiKeySupplier;
    private final Object connectorInstance;

    /**
     * Creates a new InputValidator.
     * 
     * @param connectorInstance The connector instance (for exception context)
     * @param inputDataSupplier Supplier for the input data parameter
     * @param paramsSupplier Supplier for the params parameter
     * @param timeoutSupplier Supplier for the timeout parameter
     * @param apiKeySupplier Supplier for the API key parameter
     */
    public InputValidator(
            Object connectorInstance,
            Supplier<Object> inputDataSupplier,
            Supplier<Object> paramsSupplier,
            Supplier<Object> timeoutSupplier,
            Supplier<Object> apiKeySupplier) {
        this.connectorInstance = connectorInstance;
        this.inputDataSupplier = inputDataSupplier;
        this.paramsSupplier = paramsSupplier;
        this.timeoutSupplier = timeoutSupplier;
        this.apiKeySupplier = apiKeySupplier;
    }

    /**
     * Validates all input parameters.
     * 
     * @throws ConnectorValidationException if any validation fails
     */
    public void validate() throws ConnectorValidationException {
        LOGGER.fine("Starting input validation...");
        
        List<String> errors = new ArrayList<>();
        
        // Validate input data (mandatory)
        validateInputData(errors);
        
        // Validate params (optional, but must be Map if provided)
        validateParams(errors);
        
        // Validate timeout (optional, but must be positive if provided)
        validateTimeout(errors);
        
        // Validate API key (optional, format check if provided)
        validateApiKey(errors);
        
        if (!errors.isEmpty()) {
            String message = String.join("; ", errors);
            LOGGER.warning("Validation failed: " + message);
            throw new ConnectorValidationException(connectorInstance, message);
        }
        
        LOGGER.fine("All input parameters validated successfully.");
    }

    /**
     * Validates the input data parameter.
     */
    private void validateInputData(List<String> errors) {
        Object inputData = inputDataSupplier.get();
        
        if (inputData == null) {
            errors.add("Mandatory parameter '" + ConnectorConstants.INPUT_DATA + "' is missing. " +
                    "Please provide input data as a Map (e.g., [question: 'Your question'])");
            return;
        }
        
        if (!(inputData instanceof Map)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_DATA + "' must be a Map, " +
                    "but received: " + inputData.getClass().getSimpleName());
        }
    }

    /**
     * Validates the params parameter.
     */
    private void validateParams(List<String> errors) {
        Object params = paramsSupplier.get();
        
        if (params != null && !(params instanceof Map)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_PARAMS + "' must be a Map if provided, " +
                    "but received: " + params.getClass().getSimpleName());
        }
    }

    /**
     * Validates the timeout parameter.
     */
    private void validateTimeout(List<String> errors) {
        Object timeout = timeoutSupplier.get();
        
        if (timeout != null) {
            if (!(timeout instanceof Integer)) {
                errors.add("Parameter '" + ConnectorConstants.INPUT_TIMEOUT_MS + "' must be an Integer, " +
                        "but received: " + timeout.getClass().getSimpleName());
                return;
            }
            
            int timeoutValue = (Integer) timeout;
            if (timeoutValue <= 0) {
                errors.add("Parameter '" + ConnectorConstants.INPUT_TIMEOUT_MS + "' must be positive, " +
                        "but received: " + timeoutValue);
            }
        }
    }

    /**
     * Validates the API key parameter.
     */
    private void validateApiKey(List<String> errors) {
        Object apiKey = apiKeySupplier.get();
        
        if (apiKey != null && !(apiKey instanceof String)) {
            errors.add("Parameter '" + ConnectorConstants.INPUT_API_SECRET_KEY + "' must be a String, " +
                    "but received: " + apiKey.getClass().getSimpleName());
        }
        // Note: Empty string is allowed (means no auth)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATIC UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Validates that a Map is not null and not empty.
     * 
     * @param map The map to validate
     * @param paramName The parameter name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void requireNonEmptyMap(Map<?, ?> map, String paramName) {
        if (map == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
        if (map.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be empty");
        }
    }

    /**
     * Validates that a value is a positive integer.
     * 
     * @param value The value to validate
     * @param paramName The parameter name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void requirePositiveInt(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be positive, got: " + value);
        }
    }
}

