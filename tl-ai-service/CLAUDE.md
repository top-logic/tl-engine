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

### Multi-Agent Orchestrator (Chat Entrypoint)

The chat UI sends user messages to **OrchestratorAgent** as the single entrypoint. It reasons about what the user wants, calls **zero, one, or several** specialists in sequence via **AgentInvoker** and a **work-order handoff**, then synthesizes the final answer.

```mermaid
flowchart TB
    subgraph UI["tl-chat (ChatControl)"]
        U[User message]
        U --> Send[SendMessage]
        Send --> Exec[executeOrchestrator]
        Exec --> Tracker["new ToolCallTracker()<br/>ToolCallTracking.setTracker(tracker)"]
        Tracker --> OChat["OrchestratorAgent.chat(chatMemory, userMessage)"]
        Poll["PollAIProgress"] --> Timeline["tracker.getTimeline()"]
        Timeline --> Render["Render AgentCall / ToolCall<br/>+ final answer"]
    end

    subgraph Orch["tl-ai-service: Orchestrator"]
        OChat --> Build["Build OrchestratorRouter<br/>+ AgentDelegationTools + SkillTool<br/>+ mistral-large-latest"]
        Build --> Router["OrchestratorRouter.chat(trimmed)"]
        Router --> LLM["LLM: plan steps"]
        LLM --> Tool["Call 0..N tools in sequence<br/>goal, task, context per call"]
        Tool --> T1[requestModelExploration]
        Tool --> T2[requestModelCreation]
        Tool --> T3[requestTLScriptHelp]
        Tool --> T4[SkillTool]
    end

    subgraph Invoker["AgentInvoker (handoff; records AgentCall)"]
        T1 --> I1["invokeExplorer(Orchestrator, workOrder)"]
        T2 --> I2["invokeCreator(Orchestrator, workOrder)"]
        T3 --> I3["invokeTLScript(Orchestrator, workOrder)"]
        I1 --> ME
        I2 --> MC
        I3 --> TL
    end

    subgraph Specialists["Specialist agents"]
        ME[ModelExplorer.explore]
        MC[ModelEditor.create]
        TL[TLScriptAgent.answer]
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

### Handoff Contract (AgentWorkOrder)

- **goal** – Short intent (e.g. "Explore the model and return tool-backed facts")
- **task** – Concrete question or instruction
- **context** – Optional hints (type/part names, module, entity ids)
- **expectedOutputFormat** – Optional (e.g. "JSON with elementIds, fields, annotations")

Callers must fill these fields instead of pasting the full user message.

### Execution Timeline (Observability)

- **ToolCallTracker** holds a unified timeline: `getTimeline()` returns `List<TraceEntry>` in order
- **TraceEntry** is either **ToolCallTraceEntry** or **AgentCallTraceEntry**
- The caller must set `ToolCallTracking.setTracker(tracker)` before calling the Orchestrator

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
