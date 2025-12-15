# AI Agent Connector for Bonita

A custom Bonita connector that enables Bonita BPM processes to communicate with an external RAG (Retrieval-Augmented Generation) Agent via HTTP. This connector allows you to integrate intelligent question-answering capabilities into your Bonita workflows.

## Overview

The AI Agent Connector provides a seamless integration between Bonita processes and a RAG agent service. It handles:
- HTTP communication with the agent API
- JWT authentication
- Request/response mapping
- Error handling and validation
- LLM configuration (OpenAI, Anthropic, etc.)

## Prerequisites

### Java
- **Java JDK 17** or higher
- Verify your Java version:
```bash
java -version
```

### Maven
- **Maven 3.6+** (3.8+ recommended)
- Verify your Maven version:
```bash
mvn -version
```

### Bonita Studio
- **Bonita Studio 2024.3** (Community or Subscription edition)
- Download from: https://www.bonitasoft.com/downloads

## Building the Connector

1. **Clone or navigate to the project directory:**
```bash
cd aiAgentConnector
```

2. **Build the project:**
```bash
mvn clean package
```

This will:
- Compile the Java source code
- Run unit tests
- Package the connector as a JAR file
- Generate the connector definition and implementation files

3. **Locate the generated JAR:**
After building, you'll find the connector JAR file in the `target/` directory. The filename follows the pattern:
```
target/aiAgentConnector-<VERSION>.jar
```

Where `<VERSION>` is the version specified in `pom.xml` (e.g., `1.0.6`, `1.0.7`, etc.). Check the `target/` folder for the exact filename after building.

## Installing the Connector in Bonita Studio

### Step 1: Open Bonita Studio
Launch Bonita Studio and open your project (or create a new one).

### Step 2: Import the Connector
1. Go to **Window** → **Preferences** (or **File** → **Preferences** on macOS)
2. Navigate to **Bonita** → **Connectors**
3. Click **Add...**
4. Browse to the `target/` folder in your connector project
5. Select the JAR file (named `aiAgentConnector-<VERSION>.jar` where `<VERSION>` matches the version in `pom.xml`)
6. Click **OK** to import

Alternatively, you can:
- Copy the JAR file to your Bonita Studio connectors directory
- Or use the **Extensions** → **Connectors** menu in Bonita Studio

### Step 3: Verify Installation
1. In Bonita Studio, create or open a process
2. Add a connector activity
3. You should see **AI Agent Connector** in the connector list (under the **AI** category)

## Using the Connector

### Connector Inputs

| Input | Type | Required | Description |
|-------|------|----------|-------------|
| `agentUrl` | String | Yes | The URL of the RAG agent API (e.g., `http://localhost:8000/run`) |
| `authToken` | String | No | JWT Bearer token for authentication (required if agent has auth enabled) |
| `question` | String | Yes | The question to ask the RAG agent |
| `llmApiKey` | String | Yes | LLM provider API key (OpenAI, Anthropic, etc.) |
| `llmModel` | String | No | LLM model name (default: `gpt-4o-mini`) |
| `topK` | Integer | No | Number of documents to retrieve (default: 3, range: 1-10) |
| `minConfidence` | Double | No | Minimum confidence threshold (default: 0.0, range: 0.0-1.0) |
| `requireSources` | Boolean | No | Include source citations in response (default: true) |
| `timeoutMs` | Integer | No | Request timeout in milliseconds (default: 30000) |
| `maxTokens` | Integer | No | Maximum response tokens (default: 700, range: 100-4000) |
| `temperature` | Double | No | LLM temperature for response generation (default: 0.1, range: 0.0-2.0) |

### Connector Outputs

| Output | Type | Description |
|--------|------|-------------|
| `status` | String | Response status: `ok`, `low_confidence`, or `error` |
| `answer` | String | The agent's answer text |
| `sources` | List | List of source documents used (List of Maps with `title` and `version`) |
| `confidence` | Double | Confidence score (0.0-1.0) |
| `reasoning` | String | Explanation of how the answer was derived |
| `errorCode` | String | Error code if status is `error` |
| `errorMessage` | String | Human-readable error message if status is `error` |

### Example Usage in Bonita Process

1. **Add a Connector Activity** to your process
2. **Select "AI Agent Connector"** from the connector list
3. **Configure the inputs:**
   - `agentUrl`: `http://localhost:8000/run` (or your agent URL)
   - `authToken`: Your JWT token (if authentication is enabled)
   - `question`: `What is the deadline for employee onboarding?`
   - `llmApiKey`: Your OpenAI/Anthropic API key
   - `llmModel`: `gpt-4o-mini` (optional)
   - Other parameters as needed

4. **Use the outputs** in subsequent activities:
   - Display `answer` in a form
   - Store `confidence` in a process variable
   - Log `reasoning` for audit purposes
   - Handle `errorCode` and `errorMessage` for error scenarios

### Using Process Variables

All inputs support process variable expressions. You can reference process variables using:
```
${processVariableName}
```

Example:
- `agentUrl`: `${agentUrlVariable}`
- `question`: `${userQuestion}`
- `llmApiKey`: `${llmApiKeyVariable}`

## Generating JWT Authentication Token

If the RAG agent has authentication enabled, you'll need to generate a JWT token. Use this command (replace `my-secret-key` with your actual `JWT_SECRET_KEY`):

```bash
python -c "import jwt; from datetime import datetime, timedelta, timezone; print(jwt.encode({'sub': 'bonita', 'exp': datetime.now(timezone.utc) + timedelta(hours=24)}, 'my-secret-key', algorithm='HS256'))"
```

The token will be valid for 24 hours. Store it in a process variable or connector input.

## Project Structure

```
aiAgentConnector/
├── pom.xml                                    # Maven project configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/boukhiri/
│   │   │       ├── AIAgentConnector.java     # Main connector class
│   │   │       ├── client/                   # HTTP client implementation
│   │   │       ├── config/                   # Configuration constants
│   │   │       ├── exception/                # Custom exceptions
│   │   │       ├── model/                    # Request/Response DTOs
│   │   │       ├── util/                     # Utility classes
│   │   │       └── validation/               # Input validation
│   │   └── resources-filtered/
│   │       ├── aiAgentConnector.def          # Connector definition
│   │       ├── aiAgentConnector.impl         # Connector implementation
│   │       └── connector.png                # Connector icon
│   └── test/
│       └── java/                             # Unit tests
└── target/                                   # Build output (JAR file here)
    └── aiAgentConnector-<VERSION>.jar        # Version matches pom.xml
```

## Development

### Running Tests

```bash
mvn test
```

### Building Without Tests

```bash
mvn clean package -DskipTests
```

### IDE Setup

The project is configured for:
- Java 17
- Maven 3.6+
- Standard Maven directory structure

Import the project into your IDE (IntelliJ IDEA, Eclipse, VS Code) as a Maven project.

## Architecture

The connector follows SOLID principles and Bonita best practices:

- **AIAgentConnector**: Main connector class that orchestrates the flow
- **InputValidator**: Handles all input validation logic
- **HttpAgentClient**: Abstracts HTTP communication with the agent
- **AgentRequest/AgentResponse**: Immutable DTOs for request/response
- **ConnectorConstants**: Centralized constants for inputs/outputs

## Troubleshooting

### Connector Not Appearing in Bonita Studio
- Ensure the JAR file was built successfully
- Check that the JAR is in the correct format (not a ZIP)
- Verify Bonita Studio version compatibility (2024.3+)
- Restart Bonita Studio after importing

### Connection Errors
- Verify the `agentUrl` is correct and the agent is running
- Check network connectivity
- Ensure authentication token is valid (if auth is enabled)
- Review agent logs for errors

### Validation Errors
- Check that all required inputs are provided
- Verify input types match expected types
- Ensure process variable expressions are correctly formatted

## Integration with RAG Agent

This connector is designed to work with the RAG Agent service. Ensure:
1. The RAG agent is running and accessible at the `agentUrl`
2. Authentication is configured consistently (if enabled)
3. LLM API keys are valid and have sufficient quota
4. Network connectivity between Bonita and the agent

For more information about the RAG Agent, see the `agent/README.md` file.

## Documentation

For detailed information about:
- Bonita connector development: https://documentation.bonitasoft.com/bonita/latest/process/connector-archetype
- Challenge requirements: See `technical-challenge-genai-engineer.pdf` in the project root

## License

This project is part of the Bonita GenAI Engineer Technical Challenge.

