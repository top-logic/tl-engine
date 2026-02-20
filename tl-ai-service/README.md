# TL AI Service

AI integration module for TopLogic applications, providing seamless access to Large Language Models (LLMs) through LangChain4j with thread-safe client pooling and TL-Script integration.

## Overview

The `tl-ai-service` module enables TopLogic applications to integrate with various AI providers including OpenAI, Anthropic, and Mistral. It provides:

- **Thread-safe model pooling**: Automatic client lifecycle management bound to TopLogic's InteractionContext
- **Multiple provider support**: OpenAI, Anthropic (Claude), and Mistral AI models
- **TL-Script integration**: AI functions accessible directly from TL-Script expressions
- **Structured output**: JSON schema support for type-safe AI responses
- **Flexible configuration**: XML-based configuration with per-model settings

## Features

### AI Model Management

- **Automatic pooling**: Chat models are pooled and reused across requests
- **Thread context binding**: Models automatically bound to the current interaction context
- **Lifecycle management**: Automatic cleanup when context is destroyed
- **Default model support**: Configure a default model for simpler usage

### TL-Script Functions

Direct access to AI capabilities from TL-Script:

```javascript
// Simple chat
aiChat([{ "role": "user", "content": "What is TopLogic?" }])

// Multi-turn conversation
aiChat([
  { "role": "system", "content": "You are a helpful assistant." },
  { "role": "user", "content": "Hello!" },
  { "role": "assistant", "content": "Hi! How can I help you?" },
  { "role": "user", "content": "Tell me about TopLogic." }
], "gpt-4o")

// Structured JSON output
aiChat($messages, "gpt-4o", {
  "type": "json",
  "jsonSchema": {
    "name": "PersonInfo",
    "schema": {
      "properties": {
        "name": {"type": "string", "description": "Person's name"},
        "age": {"type": "integer", "description": "Person's age"},
        "status": {
          "type": "string",
          "enum": ["active", "inactive"],
          "description": "Account status"
        }
      },
      "required": ["name", "age"]
    }
  }
})

// Multimodal input (images, documents)
aiChat([{
  "role": "user",
  "content": [
    "What's in this image?",
    $imageData  // Binary data
  ]
}], "gpt-4o")
```

### Supported Content Types

The TL-Script integration supports various content types:
- **Text**: Plain text messages
- **Images**: image/* MIME types
- **Audio**: audio/* MIME types
- **Video**: video/* MIME types
- **PDF**: application/pdf documents
- **Mixed content**: Combine text with multiple binary data sources

### Response Format Options

Configure AI response structure:

1. **Default (text)**: Free-form text response
2. **Simple JSON**: `"json"` - JSON output without strict schema
3. **Structured JSON**: JSON schema with:
   - Type definitions (string, integer, number, boolean, array, object)
   - Required properties
   - Enum constraints
   - Nested objects
   - Array item types

## Maven Coordinates

```xml
<dependency>
  <groupId>com.top-logic</groupId>
  <artifactId>tl-ai-service</artifactId>
</dependency>
```

## Architecture

### Multi-Agent Orchestrator (Chat Entrypoint)

The chat UI sends user messages to **OrchestratorAgent** as the single entrypoint. The orchestrator is **LLM-based**: an LLM infers intent from the user message (no keyword matching) and calls specialist tools with a distilled work order (goal, task, context).

- **OrchestratorAgent.chat(chatMemory, userMessage)** – Builds **OrchestratorRouter** (LLM + four tools), runs the router with the user message. The LLM decides Explorer / Creator / TLScript / Skill and fills goal/task/context.
- **OrchestratorRouter** – Interface with system prompt defining routing rules, work-order discipline, and behavior. See `OrchestratorRouter.java`. Prompt style follows orchestrator-pattern and Cursor-style clarity ([orchestrator pattern](https://dev.to/akshaygupta1996/the-orchestrator-pattern-routing-conversations-to-specialized-ai-agents-33h8), [system-prompts-and-models-of-ai-tools](https://github.com/x1xhlol/system-prompts-and-models-of-ai-tools)).
- **AgentDelegationTools** – Three inner classes (Exploration, Creation, TLScript) with `@Tool` methods (`requestModelExploration`, `requestModelCreation`, `requestTLScriptHelp`) that call **AgentInvoker**. A fourth tool, **SkillTool**, provides on-demand skill lookup.
- **AgentInvoker** – Internal component for agent-to-agent calls. Records **AgentCall** events (fromAgent → toAgent) in the run’s trace. No agent is exposed as a @Tool to another agent.
- **AgentWorkOrder** (`com.top_logic.ai.service.agents.handoff`) – Structured handoff: `goal`, `task`, `context`, `expectedOutputFormat`.
- **Execution timeline** – **ToolCallTracker.getTimeline()** returns an ordered list of **TraceEntry** (TOOL_CALL or AGENT_CALL). The chat UI (tl-chat) renders “Orchestrator → ModelExplorer” vs “listModules(...)” so AgentCall and ToolCall are clearly distinguished. The caller must set **ToolCallTracking.setTracker(tracker)** before calling the orchestrator so tools and agent calls write into the same trace.
- **TLScriptAgent** – When it needs model context, it uses **RequestModelExplorationTool** with goal/task/context; that tool calls **AgentInvoker.invokeExplorer(“TLScriptAgent”, workOrder)** so the handoff is structured, not the full user prompt.

### Core Components

#### AIService
Main service managing AI model access and pooling.

- **Location**: `com.top_logic.ai.service.AIService`
- **Singleton**: Access via `AIService.getInstance()`
- **Thread-safe**: Models bound to InteractionContext
- **Configurable**: Via `tl-ai-service.config.xml`

#### ChatModelFactory
Factory interface for creating provider-specific chat models.

- **Location**: `com.top_logic.ai.service.ChatModelFactory`
- **Implementations**:
  - `OpenAIChatModelFactory` - OpenAI models (GPT-3.5, GPT-4, etc.)
  - `AnthropicChatModelFactory` - Anthropic models (Claude)
  - `MistralChatModelFactory` - Mistral AI models

#### TL-Script Functions
Script functions for AI integration.

- **Location**: `com.top_logic.ai.service.scripting.OpenAIScriptFunctions`
- **Prefix**: `ai`
- **Main function**: `aiChat(messages, model, responseFormat)`

#### JsonSchemaConverter
Utility for converting TL-Script maps to LangChain4j JSON schemas.

- **Location**: `com.top_logic.ai.service.scripting.JsonSchemaConverter`
- **Purpose**: Translate map-based schema definitions to typed objects
- **Features**: Supports all JSON schema types, required fields, enums

## Configuration

### Service Configuration

See service configuration `com.top_logic.ai.service.AIService` in `tl-ai-openai.conf.config.xml`:

### Environment Variables

Set API keys as environment variables:

```bash
export tl_openai_apikey="sk-..."
export tl_anthropic_api_key="sk-ant-..."
export tl_mistral_api_key="..."
```

Or pass corresponding system properties to the application.

## Adding a New Provider

1. Create a factory class implementing `ChatModelFactory`:
```java
public class MyProviderChatModelFactory  implements ChatModelFactory {

    @Override
    public ChatModel createChatModel() {
        // Initialize and return provider-specific ChatModel
    }
}
```

2. Add configuration interface with provider-specific properties
3. Register in `tl-ai-service.config.xml`

## Best Practices

### API Key Security
- Never commit API keys to version control
- Use environment variables: `${OPENAI_API_KEY}`
- Rotate keys regularly
- Use separate keys for dev/prod environments

### Performance
- Cache frequent responses when possible
- Use smaller models for simple tasks
- Set appropriate `max-tokens` limits
- Monitor token usage for cost control
