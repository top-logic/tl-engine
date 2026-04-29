# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Custom AI agent framework built on TopLogic, replacing LangChain4j with a purpose-built streaming agent loop. Multi-module Maven build (parent: `tl-parent-all` version `8.0.0-alpha3`).

| Module | Role |
|--------|------|
| **tl-ai-tool-api** | Shared tool contracts (`Tool`, `ToolResult`, `ToolMode`, `@ToolMethod`, `@P`, `ToolSchema`). No implementations — just interfaces and annotations. |
| **tl-ai-core** | Agent loop engine, multi-provider LLM streaming, tool dispatcher & implementations, MCP client, multi-agent delegation, memory/compaction, TL-Script bindings |
| **tl-ai-mcp-server** | MCP protocol server (SDK 1.1.2) exposing TopLogic model/tools to external AI clients over HTTP at `/mcp/*` |
| **tl-chat** | Agentic chat UI — REST API + SSE streaming servlet, vanilla JS frontend, session management, human-in-the-loop approval |
| **tl-ai-vector-index** | Semantic search via embedding models (Mistral) and vector stores (Qdrant/JVector), background indexing daemon |
| **tl-ai-demo** | Application module bundling all above for deployment |

**Module dependency order:** `tl-ai-tool-api` → `tl-ai-core` → `tl-ai-mcp-server` / `tl-chat` / `tl-ai-vector-index` → `tl-ai-demo`

## Build Commands

```bash
mvn clean install                      # Build entire project
mvn clean install -pl tl-ai-core       # Build specific module
mvn test -pl tl-ai-core               # Test specific module
mvn clean install -DskipTests          # Skip tests
```

## Architecture

### Tool API (tl-ai-tool-api)

Extracted contract module (package `com.top_logic.tl.ai.tool.api`) enabling tools to be defined without depending on tl-ai-core internals. Key types:

- `Tool` interface — `execute(Map<String, Object> args, ToolExecutionContext context) → ToolResult`
- `ToolMode` enum — READ (parallel-safe), USER_SCOPED (parallel-safe), GLOBAL (serial + locked)
- `@ToolMethod` / `@P` — annotation framework for reflection-based tool creation
- `ToolExecutionContext` — marker interface; concrete `ToolContext` lives in tl-ai-core
- `ToolRegistryService` — service-level registry with qualified names `{factory}.{tool}`

### Agent Loop (tl-ai-core `agent/`)

`AgentLoop` is the central execution engine: builds `ProviderRequest` → streams LLM response → extracts `ToolCall`s → executes tools → loops until stop condition. `AgentFactory` constructs agents from KB definitions, wiring provider, tools, MCP servers, and skills.

Key flow: `AgentFactory.runAgent()` → `AgentLoop` → `LLMProvider.stream()` → `StreamHandler` → `ToolDispatcher.execute()` → repeat.

Stop conditions: `StopReason` — COMPLETE, MAX_TURNS, ERROR, CONTEXT_OVERFLOW, REJECTED, ABORTED.

### Provider System (tl-ai-core `provider/`)

`LLMProvider` interface with streaming-first design. Four implementations: `AnthropicProvider`, `OpenAIProvider`, `MistralProvider`, `XAIProvider`. All use Java `HttpClient` + msgbuf JSON.

`LLMProviderRegistry` (TopLogic service) maps model names → providers with auto-discovery via `ProviderModelDiscovery`. Supports configurable base URLs for OpenAI-compatible endpoints (local models, custom providers).

### Tool System (tl-ai-core `tools/`)

- `AnnotatedTools` — reflection engine scanning `@ToolMethod` methods into `Tool` instances (lives in tl-ai-tool-api)
- `ToolDispatcher` — frozen registry with JSON Schema validation
- Subpackages: `model/` (browser/editor/search), `script/` (TL-Script executor), `interaction/` (delegation, ask-user, skill), `task/` (task store, task binding), `config/` (schema, layouts), `integration/` (Gitea), `registry/` (factories), `util/` (serialization, i18n)

### MCP Integration

**Client side** (tl-ai-core `mcp/`): `McpServerRegistry` manages connections to external MCP servers (stdio/HTTP transports). `McpTool` wraps remote tools with sanitized names `mcp__{server}__{tool}`. Agents reference MCP servers by name.

**Server side** (tl-ai-mcp-server): Exposes TopLogic model as MCP resources (`toplogic://instance/{tid}`, `toplogic://model/modules/...`) and adapts all registered tools via `McpToolAdapter`. Supports dynamic TL-Script-based resource templates.

### Event System (tl-ai-core `agent/`)

`AgentEvent` sealed interface with 25 record subtypes covering run/turn/tool/approval/transaction/memory lifecycle. `AgentEventListener` enables real-time SSE streaming and structured logging.

### Chat UI (tl-chat)

`ChatServlet` at `/chat/*` handles REST + SSE. `ChatProcessor` orchestrates background agent execution. `ChatSSEHandler` implements `AgentEventListener` for real-time push (50ms flush batching, 30+ event types). Frontend is vanilla JS with state machine (IDLE/STREAMING/AWAITING_APPROVAL/COMPLETED/ERROR/CANCELLED).

`ChatSessionManager` maintains per-session state (Chat + ConversationMemory + ConversationLog) with LRU cache. `ChatHistoryService` persists to KnowledgeBase.

### Security & Approval

Multi-layer: `SecurityContext.hasToolPermission()` → `ExecutionMode` gate (GLOBAL tools blocked in PRODUCTION) → `PermissionDecision` (Allowed/Denied/NeedsApproval) → `ApprovalCallback` for human-in-the-loop. Session-level approval caching.

### Memory & Compaction (tl-ai-core `memory/`)

`ConversationMemory` wraps conversation with hybrid token estimation (API baseline + byte heuristic). `CompactionStrategy` interface: `SummarizingStrategy` (LLM-based) and `TruncatingStrategy`. Reactive retry on CONTEXT_OVERFLOW.

### Multi-Agent Delegation

`DelegationTool` creates isolated sub-agent runs (max depth 3). Parent passes `TransactionScope` and `SecurityContext`. Child events recorded in parent's `ConversationLog`.

## Project Structure

Standard Maven layout. Key package roots:

| Module | Base package |
|--------|-------------|
| tl-ai-tool-api | `com.top_logic.tl.ai.tool.api` (+ `.registry`) |
| tl-ai-core | `com.top_logic.tl.ai.core.*` (agent, provider, tools, message, memory, mcp, session, scripting) |
| tl-chat | `com.top_logic.ai.chat.*` (api, layout, history) |
| tl-ai-mcp-server | `com.top_logic.ai.mcp.server.*` (tools, resources, dynamic, completions) |
| tl-ai-vector-index | `com.top_logic.ai.vector.index.*` |

**Key config files per module:**
- Model definition: `src/main/webapp/WEB-INF/model/tl.ai.<module>.model.xml`
- Application config: `src/main/webapp/WEB-INF/conf/tl-ai-<module>.conf.xml`
- Auto-config: `src/main/webapp/WEB-INF/autoconf/model.tl.ai.<module>.config.xml`

**TopLogic parent POMs:** `tl-parent-core` for library modules, `tl-parent-app` for application modules.

**Important:** Modules use `web-fragment.xml` (in `src/main/resources/META-INF/`) for servlet registration, not `web.xml`. Only top-level applications use `web.xml`.

## Coding Rules

- **DO NOT** manually edit `messages_en.properties` / `messages_de.properties` (auto-generated by build)
- **DO NOT** add `@author` tags to JavaDoc (use git history)
- **DO NOT** create hand-written POJO DTOs — use msgbuf `.proto` files (see `/create-dto` skill)
- **DO NOT** use Jackson (`ObjectMapper`) — use msgbuf `JsonWriter`/`JsonReader`
- **DO NOT** add Claude signatures or `Co-Authored-By` lines to commits
- **DO** include user prompts as the last section of commit messages (see `/commit` command)
- **DO** include `@see` on config property constants pointing to getter (see `/toplogic-service` skill)
- Use `RuntimeException` for service startup errors, `ConfigurationError` only for user-facing errors (see `/toplogic-service` skill)

## Notes

- UTF-8 encoding throughout
- Test framework: JUnit (via TestCase base class)
- All LLM providers use streaming — no blocking request/response calls
- Services are TopLogic `ConfiguredManagedClass` singletons: `LLMProviderRegistry`, `ToolRegistryService`, `McpServerRegistry`
- Thread-local bindings: `TaskStoreBinding`, `UserInteractionStoreBinding`, `ConversationLogListenerBinding`
- Maven compiler configured with `parameters=true` for reflection-based tool annotation support
