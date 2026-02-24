# tl-ai-service

Core TopLogic module providing AI integration via LangChain4j 1.10.0 with thread-safe client pooling, multi-agent orchestration, and TL-Script integration.

## Core Architecture

### Thread-Safe Model Pooling

1. **InteractionContext Binding**: Each model is borrowed from a pool and bound to the current TopLogic `InteractionContext`
2. **Automatic Cleanup**: Models are automatically returned to the pool when the interaction context is destroyed
3. **Reuse Within Context**: Multiple calls within the same context return the same model instance
4. **Per-Model Pools**: Each configured AI model has its own connection pool

Key classes: `AIService` (main singleton), `ChatModelFactory` (factory interface), provider factories in `providers/`.

### Multi-Provider Support

- **OpenAIChatModelFactory**: GPT-3.5, GPT-4, GPT-4o
- **AnthropicChatModelFactory**: Claude 3 (Opus, Sonnet, Haiku)
- **MistralChatModelFactory**: Mistral AI (with auto-discovery)

### Configurable Entry Point Agent

The chat UI sends user messages to the **entry point agent** configured in AgentRegistry (via `entry-point="true"` on an `AgentConfig`). The entry point agent reasons about what the user wants, calls zero, one, or several specialists via **dynamic delegation tools** (`delegate_to_<name>`), then synthesizes the final answer.

```mermaid
flowchart TB
    subgraph UI["tl-chat (ChatControl)"]
        U[User message]
        U --> Send[SendMessage]
        Send --> Exec[executeOrchestrator]
        Exec --> Tracker["new ToolCallTracker()"]
        Tracker --> Registry["AgentRegistry.getEntryPointAgentName()"]
        Registry --> Build["buildUntypedAgent(name, chatMemory)"]
        Poll["PollAIProgress"] --> Timeline["tracker.getTimeline()"]
        Timeline --> Render["Render AgentCall / ToolCall + final answer"]
    end

    subgraph Agent["tl-ai-service: Entry Point Agent"]
        Build --> LLM["LLM: plan steps"]
        LLM --> Tool["Call 0..N tools"]
        Tool --> T1["MCP/Registry tools"]
        Tool --> T2["delegate_to_X tools"]
        Tool --> T3["SkillTool"]
    end

    subgraph Delegation["Dynamic Delegation (AgentRegistry.buildDelegationTools)"]
        T2 --> Del["buildUntypedAgent(targetName)"]
        Del --> Spec["Specialist agent"]
    end

    Spec -.->|result| LLM
    T1 -.->|result| LLM
    T3 -.->|result| LLM
    LLM --> Build
    Build --> Render
```

### Execution Timeline (Observability)

- **ToolCallTracker** holds a unified timeline: `getTimeline()` returns `List<TraceEntry>` in order
- **TraceEntry** is either **ToolCallTraceEntry** or **AgentCallTraceEntry**
- The caller must set `ToolCallTracking.setTracker(tracker)` before calling the entry point agent

## Configuration

Service config in `src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml`:

```xml
<config service-class="com.top_logic.ai.service.AIService">
    <instance class="com.top_logic.ai.service.AIService" default-model="gpt-4o">
        <factories>
            <factory class="com.top_logic.ai.service.providers.OpenAIChatModelFactory"
                models="gpt-4o, gpt-4o-mini" api-key="%OPENAI_API_KEY%"
                base-url="https://api.openai.com/v1" max-pool-size="10" />
        </factories>
    </instance>
</config>
```

API keys via environment variables: `tl_openai_apikey`, `tl_anthropic_api_key`, `tl_mistral_api_key`.

## Notes

- Core module (extends `tl-parent-core`), not an application
- Models pooled per model name, not per factory
- API keys must never be committed to version control
- To add a new AI provider, see the `/add-ai-provider` skill
