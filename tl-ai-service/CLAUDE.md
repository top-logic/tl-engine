# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **tl-ai-service** module, a TopLogic core module that provides AI integration capabilities through LangChain4j. It enables TopLogic applications to access Large Language Models (LLMs) with thread-safe client pooling and TL-Script integration.

**Key Technologies:**
- TopLogic 7.10.0-SNAPSHOT (enterprise application framework)
- LangChain4j 1.9.1 (AI/LLM integration library)
- LangChain4j Agentic 1.9.1-beta17 (agentic workflows)
- Apache Commons Pool (model pooling)

## Build Commands

### Build the module
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

### Run a specific test class
```bash
mvn test -Dtest=TestApp
```

### Skip tests during build
```bash
mvn clean install -DskipTests
```

### Package the module
```bash
mvn package
```

## Project Structure

This is a TopLogic **core module** (extends `tl-parent-core`), not an application module. It provides functionality to be used by TopLogic applications.

### Source Layout
```
tl-ai-service/
├── src/main/java/com/top_logic/ai/service/
│   ├── OpenAIService.java                    # Main service - manages AI model access
│   ├── ChatModelFactory.java                 # Factory interface for chat models
│   ├── providers/                            # Provider-specific implementations
│   │   ├── OpenAIChatModelFactory.java       # OpenAI (GPT-3.5, GPT-4)
│   │   ├── AnthropicChatModelFactory.java    # Anthropic (Claude)
│   │   └── MistralChatModelFactory.java      # Mistral AI
│   ├── scripting/                            # TL-Script integration
│   │   ├── OpenAIScriptFunctions.java        # aiChat() function
│   │   └── JsonSchemaConverter.java          # Map to JSON schema conversion
│   └── agents/                               # Agentic workflow examples
│       ├── UMLSpecificationAgent.java        # Multi-agent UML workflow
│       ├── UMLDesigner.java                  # UML design agent
│       ├── UMLCritic.java                    # UML review agent
│       ├── UMLScorer.java                    # UML scoring agent
│       ├── UMLParser.java                    # UML parsing agent
│       └── ModelCreator.java                 # Model creation agent
├── src/main/webapp/WEB-INF/
│   ├── web.xml                               # Servlet configuration
│   └── conf/
│       ├── tl-ai-openai.conf.xml             # Module registration
│       └── tl-ai-openai.conf.config.xml      # Service configuration
└── src/test/java/com/top_logic/ai/openai/
    └── TestApp.java                          # Test application
```

## Core Architecture

### Thread-Safe Model Pooling

The service uses a sophisticated pooling mechanism to manage AI model clients:

1. **InteractionContext Binding**: Each model is borrowed from a pool and bound to the current TopLogic `InteractionContext`
2. **Automatic Cleanup**: Models are automatically returned to the pool when the interaction context is destroyed
3. **Reuse Within Context**: Multiple calls within the same context return the same model instance
4. **Per-Model Pools**: Each configured AI model has its own connection pool

**Key Classes:**
- `OpenAIService` (src/main/java/com/top_logic/ai/service/OpenAIService.java:48) - Main service singleton
- `ChatModelFactory` - Factory interface for creating model pools
- Provider-specific factories in `providers/` package

### Multi-Provider Support

The service supports multiple AI providers through the factory pattern:

- **OpenAIChatModelFactory**: GPT-3.5, GPT-4, GPT-4o models
- **AnthropicChatModelFactory**: Claude 3 (Opus, Sonnet, Haiku)
- **MistralChatModelFactory**: Mistral AI models (with auto-discovery)

Each factory:
- Implements `ChatModelFactory` interface
- Creates provider-specific `ChatModel` instances
- Manages its own connection pool via Apache Commons Pool
- Can auto-discover available models from the provider

### TL-Script Integration

The `OpenAIScriptFunctions` class exposes AI capabilities to TL-Script:

**Main Function:** `aiChat(messages, model, responseFormat)`
- **messages**: Array of message objects with `role` and `content`
- **model**: Optional model name (uses default if not specified)
- **responseFormat**: Optional response structure (text, json, or json schema)

**Supported Content Types:**
- Text (strings)
- Binary data (images, audio, video, PDF) with MIME type detection
- Mixed content (text + multiple attachments)

**JSON Schema Support:**
The `JsonSchemaConverter` translates TL-Script map structures into LangChain4j JSON schemas for structured AI responses.

### Agentic Workflows

The `agents/` package demonstrates LangChain4j Agentic framework capabilities:

**UMLSpecificationAgent** - Multi-agent workflow example:
1. **Designer Agent** → Creates UML specification
2. **Critic Agent** → Reviews and provides feedback
3. **Scorer Agent** → Assigns numeric quality score
4. **Loop** → Iterates until score ≥ 0.8 or max 5 iterations
5. **Parser Agent** → Extracts model requirements
6. **Creator Agent** → Creates model using MCP tools

This demonstrates:
- Sequential planning with `SequentialPlanner`
- Loop agents with exit conditions
- State management across agents
- MCP (Model Context Protocol) tool integration
- Multi-model coordination (Mistral Large + Pixtral)

## Configuration

### Service Configuration

Configuration is in `src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml`:

```xml
<config service-class="com.top_logic.ai.service.OpenAIService">
    <instance class="com.top_logic.ai.service.OpenAIService"
        default-model="gpt-4o"
    >
        <factories>
            <factory
                class="com.top_logic.ai.service.providers.OpenAIChatModelFactory"
                models="gpt-4o, gpt-4o-mini, gpt-3.5-turbo"
                api-key="%OPENAI_API_KEY%"
                base-url="https://api.openai.com/v1"
                max-pool-size="10"
                max-idle-models="5"
            />
            <!-- Additional factories for Anthropic, Mistral, etc. -->
        </factories>
    </instance>
</config>
```

### Environment Variables

API keys should be provided via environment variables (referenced as `%OPENAI_API_KEY%` in config):

```bash
export OPENAI_API_KEY="sk-..."
export ANTHROPIC_API_KEY="sk-ant-..."
export MISTRAL_API_KEY="..."
```

### Factory Configuration Properties

Each `ChatModelFactory` supports:
- **models**: Comma-separated list of model names (leave empty for auto-discovery)
- **api-key**: API key (use environment variable references)
- **base-url**: Provider API endpoint
- **max-pool-size**: Maximum number of model instances in pool
- **max-idle-models**: Maximum idle instances to keep

## Adding a New AI Provider

To add support for a new AI provider:

1. **Create Factory Class** implementing `ChatModelFactory`:
   ```java
   public class MyProviderChatModelFactory implements ChatModelFactory {
       @Override
       public ChatModel createChatModel() {
           // Initialize provider-specific ChatModel
       }

       @Override
       public ObjectPool<ChatModel> createPool(String modelName) {
           // Create and configure model pool
       }
   }
   ```

2. **Add Configuration Interface** extending `ChatModelFactory.Config`:
   ```java
   public interface Config<I extends MyProviderChatModelFactory>
           extends ChatModelFactory.Config<I> {
       // Provider-specific configuration properties
   }
   ```

3. **Register in Configuration** (`tl-ai-openai.conf.config.xml`):
   ```xml
   <factory
       class="com.top_logic.ai.service.providers.MyProviderChatModelFactory"
       models="model-name-1, model-name-2"
       api-key="%MY_PROVIDER_API_KEY%"
   />
   ```

## Coding Conventions

### JavaDoc Standards

**DO NOT** add `@author` tags to JavaDoc. Author information is tracked through git.

### Configuration Property Constants

Always include `@see` references to getter methods:

```java
/**
 * Configuration property name for my property.
 *
 * @see #getMyProperty()
 */
String MY_PROPERTY = "my-property";
```

### Exception Handling in Services

- **ConfigurationError/ConfigurationException**: ONLY for user-facing errors
- **RuntimeException**: For technical service errors

```java
@Override
protected void startUp() {
    super.startUp();
    try {
        // Service initialization
    } catch (RuntimeException ex) {
        throw ex;  // Re-throw as-is
    } catch (Exception ex) {
        throw new RuntimeException("Failed to start service: " + ex.getMessage(), ex);
    }
}
```

## Notes

- This is a **core module** (extends `tl-parent-core`), not an application
- Models are pooled per model name, not per factory
- InteractionContext binding ensures thread-safe usage
- API keys must never be committed to version control
- The service gracefully handles missing API keys (logs warning, continues with available providers)
- Auto-discovery of models requires provider support (e.g., Mistral AI supports it, OpenAI/Anthropic require explicit configuration)
