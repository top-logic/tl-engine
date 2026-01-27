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
│   ├── AIService.java                    # Main service - manages AI model access
│   ├── ChatModelFactory.java                 # Factory interface for chat models
│   ├── providers/                            # Provider-specific implementations
│   │   ├── OpenAIChatModelFactory.java       # OpenAI (GPT-3.5, GPT-4)
│   │   ├── AnthropicChatModelFactory.java    # Anthropic (Claude)
│   │   └── MistralChatModelFactory.java      # Mistral AI
│   ├── scripting/                            # TL-Script integration
│   │   ├── OpenAIScriptFunctions.java        # aiChat() function
│   │   └── JsonSchemaConverter.java          # Map to JSON schema conversion
│   ├── agents/                               # Agentic workflows and orchestrator
│   │   ├── OrchestratorAgent.java            # Single chat entrypoint; routes to specialists
│   │   ├── AgentInvoker.java                 # Agent-to-agent calls (no agent-as-tool)
│   │   ├── handoff/
│   │   │   └── AgentWorkOrder.java           # Structured handoff DTO (goal, task, context)
│   │   ├── ModelExplorer.java                # Read-only model exploration agent
│   │   ├── ModelCreator.java                 # Model creation agent
│   │   ├── TLScriptAgent.java                # TLScript specialist agent
│   │   ├── UMLSpecificationAgent.java        # Multi-agent UML workflow
│   │   ├── UMLDesigner.java                  # UML design agent
│   │   ├── UMLCritic.java                    # UML review agent
│   │   ├── UMLScorer.java                    # UML scoring agent
│   │   └── UMLParser.java                    # UML parsing agent
│   └── tools/                                # Tool and trace infrastructure
│       ├── ToolCallTracker.java              # Records tool calls + agent calls; getTimeline()
│       ├── ToolCallTracking.java             # Thread-local trace for current run
│       ├── TraceEntry.java                   # TOOL_CALL | AGENT_CALL timeline entry
│       ├── TraceEventKind.java               # TOOL_CALL, AGENT_CALL
│       └── help/
│           ├── RequestModelExplorationTool.java  # Structured exploration (goal/task/context)
│           └── TLScriptReferenceTool.java        # TLScript function docs
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
- `AIService` (src/main/java/com/top_logic/ai/service/AIService.java:48) - Main service singleton
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

### Multi-Agent Orchestrator (Chat Entrypoint)

The chat UI sends user messages to **OrchestratorAgent** as the single entrypoint. The orchestrator thinks: what does the user want, what steps are needed, do any need a specialist? It may call **zero, one, or several** specialists in sequence; after each result it reasons “is it enough?” and either calls another tool or synthesizes the final answer. It does not expose other agents as @Tool; it uses **AgentInvoker** and a **work-order handoff** so sub-agents receive distilled requests, not the full user prompt.

**Flow diagram:**

```mermaid
flowchart TB
    subgraph UI["tl-chat (ChatControl)"]
        U[User message]
        U --> Send[SendMessage]
        Send --> Exec[executeOrchestrator]
        Exec --> Tracker["new ToolCallTracker()<br/>ToolCallTracking.setTracker(tracker)"]
        Tracker --> OChat["OrchestratorAgent.chat(userMessage)"]
        Poll["PollAIProgress"] --> Timeline["tracker.getTimeline()"]
        Timeline --> Render["Render AgentCall / ToolCall<br/>+ final answer"]
    end

    subgraph Orch["tl-ai-service: Orchestrator"]
        OChat --> Build["Build OrchestratorRouter<br/>+ OrchestratorTools<br/>+ mistral-large-latest"]
        Build --> Router["OrchestratorRouter.chat(trimmed)"]
        Router --> LLM["LLM: plan steps<br/>(no keywords)"]
        LLM --> Tool["Call 0..N tools in sequence<br/>goal, task, context per call"]
        Tool --> T1[delegateToModelExplorer]
        Tool --> T2[delegateToModelCreator]
        Tool --> T3[delegateToTLScriptSpecialist]
    end

    subgraph Invoker["AgentInvoker (handoff; records AgentCall per call)"]
        T1 --> I1["invokeExplorer(Orchestrator, workOrder)"]
        T2 --> I2["invokeCreator(Orchestrator, workOrder)"]
        T3 --> I3["invokeTLScript(Orchestrator, workOrder)"]
        I1 --> ME
        I2 --> MC
        I3 --> TL
    end

    subgraph Specialists["Specialist agents"]
        ME[ModelExplorer.explore(prompt)<br/>tools: ModelBrowser, InstanceBrowser,<br/>TypeAnalysis, ModelSearch, …]
        MC[UMLSpecificationAgent.execute(requirements)<br/>Designer → Critic → Scorer → Parser → Creator]
        TL[TLScriptAgent.answer(message)<br/>tools: TLScriptReferenceTool,<br/>RequestModelExplorationTool]
    end

    ME -.->|result| I1
    MC -.->|result| I2
    TL -.->|result| I3
    I1 --> Router
    I2 --> Router
    I3 --> Router
    Router --> OChat
    OChat --> Render
```

The LLM may call several tools in sequence; after each result it reasons whether to call more or synthesize the final answer. The diagram shows one possible tool invocation; in general there are 0..N such calls per user message.

**Flow (numbered):**
1. **OrchestratorAgent.chat(userMessage)** – Builds an LLM-based **OrchestratorRouter** with three tools. The LLM **plans steps** (what does the user want, what’s needed, do any steps need a specialist?), calls **zero, one, or multiple** tools in sequence with distilled work orders, and after each result reasons “is it enough?”; when satisfied, it synthesizes and returns the final answer.
2. **OrchestratorRouter** system prompt (see `OrchestratorRouter.java`) defines “think first”, planning, work-order discipline (goal/task/context per step; never paste the full user message), and iterative behavior (after each tool result: is it enough? call more or synthesize?). Inspired by [orchestrator pattern](https://dev.to/akshaygupta1996/the-orchestrator-pattern-routing-conversations-to-specialized-ai-agents-33h8) and [system-prompts-and-models-of-ai-tools](https://github.com/x1xhlol/system-prompts-and-models-of-ai-tools).
3. **OrchestratorTools** – Three @Tool methods that call **AgentInvoker.invokeExplorer/Creator/TLScript**; each call records an **AgentCall** in the run's trace.

**Handoff contract (AgentWorkOrder):**
- **goal** – Short intent (e.g. “Explore the model and return tool-backed facts”).
- **task** – Concrete question or instruction.
- **context** – Optional hints (type/part names, module, entity ids).
- **expectedOutputFormat** – Optional (e.g. “JSON with elementIds, fields, annotations”).

Callers (Orchestrator or TLScript’s “Request Model Exploration” tool) must fill these fields instead of pasting the full user message.

**Execution timeline (observability):**
- **ToolCallTracker** holds a unified **timeline**: `getTimeline()` returns `List<TraceEntry>` in order.
- **TraceEntry** is either **ToolCallTraceEntry** (toolName, args, resultRef) or **AgentCallTraceEntry** (fromAgent, toAgent, inputSummary, resultSummary).
- The chat UI (tl-chat) maps this to **ChatTimelineEvent** and renders “Orchestrator → ModelExplorer” vs “listModules(...)” so AgentCall and ToolCall are clearly distinguished.
- The caller must set **ToolCallTracking.setTracker(tracker)** before calling Orchestrator (or any flow that uses AgentInvoker) so tools and agent calls write into the same trace.

**Specialist agents:**
- **ModelExplorer** – Read-only; uses model browser/search/analysis tools. Invoked only via AgentInvoker (or direct /explore-model path). System prompt accepts work-order-style input (Goal/Task/Context/Expected output format).
- **TLScriptAgent** – Has TLScriptReferenceTool and **RequestModelExplorationTool**. The latter takes goal/task/context and calls AgentInvoker.invokeExplorer("TLScriptAgent", workOrder), so TLScript gets model context via a structured handoff, not by forwarding the user prompt.
- **ModelCreator** – Used by UMLSpecificationAgent; Orchestrator routes “create/modify” to AgentInvoker.invokeCreator, which calls UMLSpecificationAgent.execute(workOrder.task()).

## Configuration

### Service Configuration

Configuration is in `src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml`:

```xml
<config service-class="com.top_logic.ai.service.AIService">
    <instance class="com.top_logic.ai.service.AIService"
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
