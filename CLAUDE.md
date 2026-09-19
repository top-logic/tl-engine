# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Custom AI agent framework built on TopLogic, replacing LangChain4j with a purpose-built streaming agent loop. Multi-module Maven build (parent: `tl-parent-core` version `8.0.0-alpha8`; `tl-ai-demo` pins `tl-parent-app` at the same engine version separately).

| Module | Role |
|--------|------|
| **tl-ai-tool-api** | Shared tool contracts (`Tool`, `ToolResult`, `ToolMode`, `@ToolMethod`, `@P`, `ToolSchema`). No implementations — just interfaces and annotations. |
| **tl-ai-core** | Agent loop engine, multi-provider LLM streaming, tool dispatcher & implementations, MCP client, multi-agent delegation, memory/compaction, TL-Script bindings |
| **tl-ai-mcp-server** | MCP protocol server (SDK 1.1.2) exposing TopLogic model/tools to external AI clients over HTTP at `/mcp/*`. **Off by default** - the endpoint authenticates nobody, see `doc/security/mcp-endpoint.md` |
| **tl-chat** | Agentic chat UI - REST API + SSE streaming servlet, React frontend (`ui/`, Vite), session management, human-in-the-loop approval |
| **tl-chat-classic** | The frozen classic `chat-client.js` control for LayoutServlet applications - the "Albert" tab. Opt-in: a host gets it by depending on this module. |
| **tl-ai-vector-index** | Semantic search via embedding models (Mistral) and vector stores (Qdrant/JVector), background indexing daemon |
| **tl-ai-demo** | Application module bundling all above for deployment |

**Module dependency order:** `tl-ai-tool-api` → `tl-ai-core` → `tl-ai-mcp-server` / `tl-chat` / `tl-ai-vector-index`; `tl-chat` → `tl-chat-classic`; all → `tl-ai-demo`

## Build Commands

```bash
mvn clean install                      # Build entire project
mvn clean install -pl tl-ai-core       # Build specific module
mvn test -pl tl-ai-core               # Test specific module
mvn clean install -DskipTests          # Skip tests
```

### Browser end-to-end tests

The `tools/web` browser tests launch a real Chromium, so they are off unless
`-Dtl.ai.browser.e2e=true` is given. Without it they report as **skipped** (never as passed) - see
`BrowserE2e` and `AssumptionAwareRunner` in `tl-ai-core/src/test/java`. To run them:

```bash
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.browser.e2e=true -Dtest='TestBrowser*'
```

Playwright provisions Chromium into `~/.cache/ms-playwright` on first use; it must be the full
Chromium build, not `chrome-headless-shell` (a plain `playwright install chromium` supplies both).
Tests that reach public sites skip themselves separately when DNS does not resolve.

### Visual harnesses

The `Visual*` classes assert nothing: they render a transcript to stdout for a human to read, and
two of them spend a real Anthropic API call doing it. They use the same gate, behind
`-Dtl.ai.visual=true` - see `VisualHarness` next to `AssumptionAwareRunner`. Without it they report
as **skipped**. To run them:

```bash
source env.sh   # for the two that call the API
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.visual=true -Dtest='Visual*'
```

The two that call the API skip themselves separately when `ANTHROPIC_API_KEY` is not in the
environment.

### Prompt-injection end-to-end tests

Whether the untrusted-content envelope holds is a question about what a model does with a string, so
`TestPromptInjectionBarrier` puts a real model in front of an injected tool result and asserts which
tools ran. Real API calls, so it uses the same gate, behind `-Dtl.ai.injection.e2e=true` - see
`PromptInjectionE2e` next to `AssumptionAwareRunner`. Without it the tests report as **skipped**, and
without `ANTHROPIC_API_KEY` they skip for that reason instead.

```bash
source env.sh
mvn -pl tl-ai-core -am test -DskipTests=false -Dtl.ai.injection.e2e=true -Dtest='TestPromptInjection*'
```

## Architecture

### Tool API (tl-ai-tool-api)

Extracted contract module (package `com.top_logic.tl.ai.tool.api`) enabling tools to be defined without depending on tl-ai-core internals. Key types:

- `Tool` interface — `execute(Map<String, Object> args, ToolExecutionContext context) → ToolResult`
- `ToolMode` enum - READ (parallel-safe, no approval); USER_SCOPED / DATA_WRITE / STRUCTURAL (serial, need approval, enrol in the turn's transaction); EXTERNAL (serial, needs approval, no transaction - the change lands in another system)
- `@ToolMethod` / `@P` — annotation framework for reflection-based tool creation
- `ToolExecutionContext` — marker interface; concrete `ToolContext` lives in tl-ai-core
- `ToolRegistryService` — service-level registry with qualified names `{factory}.{tool}`

### Agent Loop (tl-ai-core `agent/`)

`AgentLoop` is the central execution engine: builds `ProviderRequest` → streams LLM response → extracts `ToolCall`s → executes tools → loops until stop condition. `AgentFactory` constructs agents from KB definitions, wiring provider, tools, MCP servers, and skills.

Key flow: `AgentFactory.runAgent()` → `AgentLoop` → `LLMProvider.stream()` → `StreamHandler` → `ToolCatalog.execute()` → repeat.

Stop conditions: `StopReason` — COMPLETE, MAX_TURNS, ERROR, CONTEXT_OVERFLOW, REJECTED, ABORTED.

### Environment block (tl-ai-core `agent/environment/`)

Every run is told, in its system prompt, who it is working for: user, UI language, date in the
user's time zone, agent, model, application, the host's stable facts and the account's own
preference text. `AgentFactory.buildConfig` composes one `<environment>` block and fills the
`{{...}}` placeholders; a prompt that names none gets the block appended, so every shipped agent has
it without an edit. An unknown placeholder is refused where it is written, not at run time.

Nothing in the block may change within a conversation - it sits in front of every request and the
providers' prompt caches key on it: day granularity, and only facts marked `stable`. Per-turn facts
are #438.

The wording is a shipped, translated resource (`tl.ai.environment.template`) that `LLMService`'s
`<environment>` section may override, the facts come from the agent
definition and from `<chat-conversation><context>`, the preference text is
`tl.accounts:Person#aiPreferences`, and the filled block is recorded on
`tl.ai.execution:AgentExecution#environment`. See `tl-ai-core/doc/environment-block.md`.

### Provider System (tl-ai-core `provider/`)

`LLMProvider` interface with streaming-first design. Four implementations: `AnthropicProvider`, `OpenAIProvider`, `MistralProvider`, `XAIProvider`. All use Java `HttpClient` + msgbuf JSON.

`LLMProviderRegistry` (TopLogic service) maps model names → providers with auto-discovery via `ProviderModelDiscovery`. Supports configurable base URLs for OpenAI-compatible endpoints (local models, custom providers).

### Tool System (tl-ai-core `tools/`)

- `AnnotatedTools` — reflection engine scanning `@ToolMethod` methods into `Tool` instances (lives in tl-ai-tool-api)
- `ToolCatalog` — frozen registry with JSON Schema validation
- Subpackages: `model/` (browser/editor/search), `script/` (TL-Script executor), `interaction/` (delegation, ask-user, skill), `task/` (task store, task binding), `config/` (schema, layouts), `integration/` (Gitea), `registry/` (factories), `util/` (serialization, i18n)

### MCP Integration

**Client side** (tl-ai-core `mcp/`): `McpServerRegistry` manages connections to external MCP servers (stdio/HTTP transports). `McpTool` wraps remote tools with sanitized names `mcp__{server}__{tool}`. Agents reference MCP servers by name.

**Server side** (tl-ai-mcp-server): Exposes TopLogic model as MCP resources (`toplogic://instance/{tid}`, `toplogic://model/modules/...`) and adapts all registered tools via `McpToolAdapter`. Supports dynamic TL-Script-based resource templates.

### Event System (tl-ai-core `agent/`)

`AgentEvent` sealed interface with 25 record subtypes covering run/turn/tool/approval/transaction/memory lifecycle. `AgentEventListener` enables real-time SSE streaming and structured logging.

### Chat UI (tl-chat)

`ChatServlet` at `/chat/*` handles REST + SSE. `ChatProcessor` orchestrates background agent execution. `ChatSSEHandler` implements `AgentEventListener` for real-time push (50ms flush batching, 30+ event types).

Two front ends consume that one API:

- **React** (`tl-chat/ui/`, the current one): TypeScript + Vite, mounted into the React view layer by `AgentChatControl` / the `<agent-chat/>` view element. See `tl-chat/CLAUDE.md` for the toolchain and the dev loop.
- **Legacy** (`tl-chat-classic`, own module since #398): `chat-client.js` and the classic `ChatControl` for LayoutServlet applications. Still shipped and still working; frozen, not developed further. A host gets the "Albert" tab by depending on `tl-chat-classic`; depending on `tl-chat` alone gives the API and the React chat only. See `tl-chat-classic/CLAUDE.md`.

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

- **DO NOT** manually edit `messages_en.properties` - the build's doclet regenerates it wholesale from the `@en` JavaDoc tags
- **DO** write both `@en` and `@de` on every `ResKey` constant, and copy the `@de` text verbatim into the module's `messages_de.properties`. No generator reads `@de` (Gitea #331): the doclet exports `@en` to `messages_en.properties`, and `tl-maven-plugin:translate` machine-translates from that English alone - silently, and often wrongly ("Stornierung beantragen" for a cancel-run command). It does that for a key with no German yet **and** for a key whose English changed since the last build, so rewording an `@en` text discards the hand-written German of that key without a word. `@de` is the authored source of truth and `messages_de.properties` is maintained by hand; the tag alone ships nothing.
  Adding a resource key is therefore three edits, and the guard checks all three:
  1. `@en` on the constant - the doclet writes `messages_en.properties` from it, never edit that file.
  2. `@de` on the same constant - required. A constant with `@en` and no `@de` fails the build's check, because its German would be an unread machine translation.
  3. The same German text in the module's `src/main/java/META-INF/messages_de.properties`.
  Then run the guard: `mvn -pl tl-ai-core test -DskipTests=false -Dtest=TestGermanResourcesMatchJavaDoc`. It walks every module of the reactor, so run it once from `tl-ai-core` whichever module you changed. It fails when a documented constant has no `@de`, when the shipped German is not the `@de` text, when a `@de` text reaches no bundle, and when a bundle holds a `[missing:...]` generator placeholder.
  Config-property labels and tooltips have no `@en`/`@de` mechanism - the doclet derives them from the JavaDoc prose and the German stays machine-translated, so read it after adding a config option.
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
