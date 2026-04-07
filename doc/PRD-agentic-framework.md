# Product Requirements Document: TopLogic Agentic Framework

**Product:** tl-ai-core — Custom Agentic Framework for TopLogic
**Version:** 1.0
**Date:** 2026-04-01
**Status:** Draft
**Author:** Architecture Analysis Session

---

## Table of Contents

1. [Overview](#1-overview)
2. [Goals & Non-Goals](#2-goals--non-goals)
3. [User Personas](#3-user-personas)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [System Architecture](#6-system-architecture)
7. [Feature Specifications](#7-feature-specifications)
8. [Migration Plan](#8-migration-plan)
9. [Testing Strategy](#9-testing-strategy)
10. [Risk Analysis](#10-risk-analysis)
11. [Milestones & Timeline](#11-milestones--timeline)
12. [Glossary](#12-glossary)

---

## 1. Overview

### 1.1 Problem Statement

TopLogic's current AI integration (`tl-ai`) is built on LangChain4j — a Java framework designed for single-user, synchronous AI applications. As the system scales to multi-user enterprise deployments, LangChain4j's architectural limitations create increasing friction:

- **No security model** — tools execute unconditionally; the agent can access data the user shouldn't see
- **No context management** — conversations degrade as context fills; no compaction or recovery
- **Synchronous agent loop** — blocks threads during LLM calls; limits concurrent users
- **Beta dependency risk** — core multi-agent features depend on `1.11.0-beta19` APIs
- **No transaction support** — agent edits are immediate and irreversible
- **No permission system** — no way to restrict what the agent can do per user
- **No backpressure** — concurrent users can overwhelm LLM APIs

### 1.2 Solution

Build `tl-ai-core` — a custom agentic framework tailored to TopLogic's needs, inspired by Claude Code's architecture and informed by LangChain4j's patterns. The framework replaces LangChain4j for the agent loop, tool execution, memory management, and provider abstraction, while keeping LangChain4j only for vector indexing (`langchain4j-qdrant`).

### 1.3 Product Vision

> An Agentic System capable of examining a TopLogic application and editing it. Helping users (humans or other agents) answer their need in the best and most efficient way possible.

---

## 2. Goals & Non-Goals

### 2.1 Goals

| ID | Goal | Priority | Rationale |
|----|------|----------|-----------|
| G1 | Own the agent loop with full control over execution, recovery, and streaming | P0 | Foundation for all other features |
| G2 | Enforce per-user security on every tool call and data access | P0 | Enterprise requirement; prevents data leaks |
| G3 | Support X concurrent users with graceful degradation | P0 | Multi-tenant enterprise system |
| G4 | Manage context intelligently (compaction, token tracking, recovery) | P0 | Prevents hallucination and conversation degradation |
| G5 | Support transactional editing with locking and rollback | P1 | Prevents race conditions between editors |
| G6 | Support any LLM provider via clean abstraction | P1 | Flexibility for customers |
| G7 | Enable developer-configurable agents (live, no restart) | P1 | Core TopLogic value proposition |
| G8 | Parallel tool execution for performance | P1 | Reduces response latency |
| G9 | Stream progress to chat UI in real-time | P1 | User experience |
| G10 | Remove dependency on LangChain4j beta APIs | P1 | Stability |
| G11 | Support multi-agent orchestration (delegation, routing) | P2 | Advanced use cases |
| G12 | Support background/scheduled agents | P2 | Automation |

### 2.2 Non-Goals

| Non-Goal | Reason |
|----------|--------|
| Build a general-purpose AI framework | This is for TopLogic only. Don't over-abstract. |
| Support 20+ LLM providers | Start with 3 (OpenAI, Anthropic, Mistral). Add more if needed. |
| Replace the chat UI | Keep existing tl-chat. Connect it to new backend. |
| Replace the MCP server | Keep existing tl-ai-mcp-server. |
| Replace vector indexing | Keep LangChain4j-Qdrant. |
| Build a RAG pipeline | Use existing VectorIndexSearchTool. |
| Compete with LangChain4j as a framework | This is internal infrastructure, not a product. |

---

## 3. User Personas

### 3.1 Developer (Application Creator)

**Role:** Creates and maintains TopLogic applications

**Needs:**
- Use the agent to create/edit model elements, layouts, workflows, services
- Configure custom agents with specific system prompts, tools, and models
- See and modify agent configurations live (no restart)
- Integrate custom agents into the chat infrastructure or as standalone/background agents
- Nearly full permissions — can do almost everything

**Pain points with current system:**
- Cannot safely test agent edits (no rollback)
- Cannot configure agents without code changes
- Context degrades in long editing sessions

### 3.2 User (Application Consumer)

**Role:** Uses the TopLogic application built by a developer

**Needs:**
- Ask the agent questions about the application ("How do I create an order?")
- Request temporary views to visualize data structures
- Ask the agent to perform business logic that doesn't have a UI button yet
- See only data they're authorized to see

**Pain points with current system:**
- Agent can potentially show restricted data (no security filtering)
- No way to restrict agent capabilities per user role

### 3.3 External Agent / System

**Role:** Another AI agent or system integrating with TopLogic

**Needs:**
- Call TopLogic's agent for information via MCP or API
- Execute specific tasks (create record, query data)
- Respect same security constraints as a human user

**Pain points with current system:**
- MCP server doesn't enforce security context per client

### 3.4 System Administrator

**Role:** Manages the TopLogic deployment

**Needs:**
- Monitor agent usage (tokens, costs, active sessions)
- Set per-user/per-role token budgets
- Configure LLM provider API keys and models
- View agent execution timelines for debugging

**Pain points with current system:**
- No per-user token tracking
- No cost visibility
- Limited observability

---

## 4. Functional Requirements

### FR-1: Agent Loop (P0)

#### FR-1.1: Core Loop

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-1.1.1 | The agent loop takes a user message and produces a response through iterative LLM calls and tool executions | A simple conversation ("What is 2+2?") returns a correct response |
| FR-1.1.2 | The loop continues until the LLM produces a response without tool calls, or a termination condition is met | Tool-using conversations complete after all tools execute |
| FR-1.1.3 | The loop has a configurable maximum iteration limit (default: 50) | Loop terminates after 50 iterations with appropriate message |
| FR-1.1.4 | The loop tracks token usage per turn and cumulatively | TokenUsage available after each turn |

#### FR-1.2: LLM Interaction

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-1.2.1 | LLM responses are streamed token-by-token | AgentListener.onThinking() called with partial text as tokens arrive |
| FR-1.2.2 | Tool call requests are detected from the LLM stream | Tool calls parsed correctly for OpenAI, Anthropic, and Mistral formats |
| FR-1.2.3 | Multiple tool calls in a single LLM response are supported | 3 tool calls in one response → 3 tools executed |
| FR-1.2.4 | System prompt is built from agent config + tool descriptions | System prompt includes tool specs and agent instructions |

#### FR-1.3: Error Recovery

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-1.3.1 | Context overflow triggers automatic memory compaction and retry | "Prompt too long" → compact → retry → success |
| FR-1.3.2 | Max output token errors trigger retry with higher limit | Up to 3 retries with 2x limit increase |
| FR-1.3.3 | Rate limit errors (429) trigger wait-and-retry with backoff | Respects Retry-After header; exponential backoff |
| FR-1.3.4 | Provider errors (500/503) trigger retry with configurable backoff | Up to 3 retries; backoff: 1s, 2s, 4s |
| FR-1.3.5 | Tool execution errors are reported to the LLM as tool results (not thrown to user) | LLM receives "Error: ..." and adapts |
| FR-1.3.6 | Permission denials are reported to the LLM as tool results | LLM receives "Denied: not permitted" and adapts |

### FR-2: LLM Provider Abstraction (P0)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-2.1 | Support OpenAI-compatible API (GPT-4, GPT-4o, etc.) | Chat completion with tool calls works |
| FR-2.2 | Support Anthropic API (Claude models) | Messages API with tool_use blocks works |
| FR-2.3 | Support Mistral API | Chat completion with tool calls works |
| FR-2.4 | Each provider supports streaming responses | Tokens streamed as they arrive |
| FR-2.5 | Each provider supports tool/function calling | Tool specs sent; tool calls received and parsed |
| FR-2.6 | Provider-specific features are configurable (thinking level, extended output, etc.) | AnthropicProvider supports thinkingType, thinkingBudgetTokens |
| FR-2.7 | ModelConfig is per-agent (temperature, maxTokens, topP, topK, etc.) | Different agents can use different configs |
| FR-2.8 | Shared connection pool per provider with configurable limits | 10 connections per provider, shared across users |
| FR-2.9 | Request queue with backpressure to prevent API overload | Max 10 concurrent API calls per provider; queue excess |

### FR-3: Tool System (P0)

#### FR-3.1: Tool Definition

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-3.1.1 | Tools are defined via `@Tool` annotation on Java methods | Existing TopLogic tools work with minimal changes |
| FR-3.1.2 | Tool parameters are described via `@P` annotation | LLM receives parameter descriptions |
| FR-3.1.3 | Tool specifications (JSON Schema) are auto-generated from annotations | Generated spec matches method signature |
| FR-3.1.4 | Tools can also be defined via script/config (ScriptToolFactory) | XML-defined tools work without Java code |
| FR-3.1.5 | Tools declare read-only vs. write, concurrency-safety, and required permission level | Framework enforces these declarations |

#### FR-3.2: Tool Execution

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-3.2.1 | Tool arguments are deserialized from LLM JSON to Java types | String, int, boolean, List, Map, custom types supported |
| FR-3.2.2 | Concurrency-safe tools execute in parallel when multiple are requested | 3 read-only tools → execute concurrently → ~1x latency (not 3x) |
| FR-3.2.3 | Non-concurrent tools execute sequentially | Write tools execute one at a time |
| FR-3.2.4 | Tool results exceeding a token limit are truncated | Results > 4000 tokens truncated with "[truncated]" marker |
| FR-3.2.5 | Tool progress is reported to AgentListener during execution | Long-running tools send progress updates |
| FR-3.2.6 | Unknown tool calls (hallucinated by LLM) return an error result with available tool names | LLM learns correct tool names |

#### FR-3.3: Tool Registry

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-3.3.1 | Tools are registered in a central registry by name | `registry.get("model.search")` returns the tool |
| FR-3.3.2 | Tools can be filtered by security context (user may not see all tools) | User without write permission doesn't see write tools |
| FR-3.3.3 | Tool specs sent to LLM only include tools the current agent is configured to use | Agent with `tools="model-browser,script-exec"` only sends those 2 specs |
| FR-3.3.4 | Registry supports hot-reload when tool configuration changes | New tool added via config → available without restart |

### FR-4: Security Layer (P0)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-4.1 | Every tool execution receives the user's SecurityContext | ToolContext.security() is never null |
| FR-4.2 | Tools that access TopLogic data filter results through SecurityContext | ModelBrowserTool only returns types the user can see |
| FR-4.3 | Write tools check SecurityContext before modifying data | Attempt to edit without write permission → ToolResult.denied() |
| FR-4.4 | Permission checks happen BEFORE tool execution, not after | Denied tools return immediately without executing |
| FR-4.5 | The LLM only sees tool specs for tools the user is permitted to use | User without write access doesn't see ModelEditorTool in the tool list |
| FR-4.6 | Security context is derived from the user's TopLogic session/identity | Existing TopLogic role/permission system is reused |
| FR-4.7 | Permission denials are logged for audit | SecurityAuditLog records: who, what tool, when, why denied |
| FR-4.8 | Permission rules can be configured per agent (e.g., "user-chat" agent has read-only tools only) | Agent config `permissionLevel="READ"` restricts to read-only tools |

### FR-5: Memory & Context Management (P0)

#### FR-5.1: Conversation Memory

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-5.1.1 | Conversation memory stores user messages, assistant messages, and tool results | All message types persisted |
| FR-5.1.2 | Memory provides messages up to a specified token budget | `getMessages(100000)` returns messages fitting within 100K tokens |
| FR-5.1.3 | Token estimation is model-aware (different tokenizers per provider) | Anthropic and OpenAI use different token estimators |
| FR-5.1.4 | Memory is isolated per user session | User A's memory is never visible to User B |

#### FR-5.2: Compaction

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-5.2.1 | Proactive compaction triggers when token usage exceeds 80% of budget | At 80%: summarize oldest messages |
| FR-5.2.2 | Reactive compaction triggers on "prompt too long" API errors | Error → compact → retry |
| FR-5.2.3 | Compaction summarizes old messages via LLM while keeping recent messages verbatim | Last 10 messages always verbatim; older ones summarized |
| FR-5.2.4 | Compaction preserves critical context (system prompt, key decisions, ongoing task) | Summary includes: current task, key findings, decisions made |
| FR-5.2.5 | Tool result compaction truncates oversized individual results | Single tool result > 4000 tokens → truncated |
| FR-5.2.6 | Compaction events are reported to AgentListener | `listener.onCompaction()` called |

#### FR-5.3: Token Tracking

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-5.3.1 | Token usage tracked per LLM call (input + output) | Accurate to within 5% of actual usage |
| FR-5.3.2 | Cumulative token usage tracked per conversation | Running total across all turns |
| FR-5.3.3 | Per-user token budget can be set | User exceeding budget → graceful stop with message |
| FR-5.3.4 | Token usage reported to AgentListener after each LLM call | `listener.onUsage(usage)` called |
| FR-5.3.5 | Cost estimation based on model pricing | `budget.estimatedCostUSD()` returns dollar amount |

### FR-6: Transaction & Locking (P1)

#### FR-6.1: Edit Transactions

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-6.1.1 | Write operations happen within a TransactionScope | `transaction.begin()` → edits → `transaction.commit()` or `transaction.rollback()` |
| FR-6.1.2 | If the agent fails during editing, changes are rolled back automatically | Exception during tool execution → all changes reverted |
| FR-6.1.3 | Transaction integrates with TopLogic's KnowledgeBase transaction system | Uses existing `KnowledgeBase.commit()` / `KnowledgeBase.rollback()` |
| FR-6.1.4 | Users can review agent edits before committing (approval mode) | Agent proposes changes → user approves → commit |
| FR-6.1.5 | Transaction scope is configurable: per-tool-call, per-turn, or per-conversation | Developer chooses granularity |

#### FR-6.2: Application Locking

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-6.2.1 | When an agent starts editing, it acquires a lock on the affected scope | Lock prevents concurrent editing |
| FR-6.2.2 | Lock scope is configurable (application-wide, module-level, type-level) | Different granularities for different use cases |
| FR-6.2.3 | Locks have a timeout to prevent indefinite blocking | Default 5 minutes; configurable |
| FR-6.2.4 | Lock contention is reported to the LLM | "Cannot edit: locked by User X since 2 minutes ago" |
| FR-6.2.5 | Locks are released on transaction commit, rollback, or timeout | No orphaned locks |
| FR-6.2.6 | Lock status is visible in the UI | User sees "Application locked by Developer A" |

### FR-7: Streaming & Progress (P1)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-7.1 | LLM response text is streamed token-by-token to AgentListener | `onThinking()` called with each token |
| FR-7.2 | Tool start/complete events are streamed | `onToolStart()` before execution; `onToolComplete()` after |
| FR-7.3 | Long-running tools report progress during execution | `onToolProgress("model.search", "Found 15 types, scanning...")` |
| FR-7.4 | Agent delegation events are streamed | `onDelegation("ModelExplorer", "Analyzing customer type")` |
| FR-7.5 | Error events are streamed | `onError()` with structured error info |
| FR-7.6 | AgentListener events are bridged to SSE for the chat UI | Existing tl-chat SSE handler receives events from new framework |
| FR-7.7 | Execution timeline is recorded for post-hoc inspection | Timeline entries: tool calls, agent calls, timestamps, durations |

### FR-8: Agent Configuration (P1)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-8.1 | Agents are defined via TopLogic configuration (XML or config store) | No Java code required to create a new agent |
| FR-8.2 | Agent config includes: name, description, model, system prompt, tools, skills, type | All fields configurable |
| FR-8.3 | Model config is per-agent: temperature, maxTokens, topP, topK, thinkingLevel | Each agent can use different settings |
| FR-8.4 | Agent type determines behavior: CHAT (interactive), BACKGROUND (triggered), STANDALONE (API) | Three distinct execution modes |
| FR-8.5 | Agents can be marked as entry-point (main chat agent) or specialist (delegated to) | Entry-point agent receives user messages |
| FR-8.6 | Configuration changes take effect immediately (hot-reload) | Change system prompt in UI → next message uses new prompt |
| FR-8.7 | Agent config supports skill references (Markdown prompt fragments) | `skills="model-exploration,sql-writing"` |
| FR-8.8 | Agent config supports delegation targets | `delegates="model-explorer,script-agent"` |

### FR-9: Multi-Agent Orchestration (P2)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-9.1 | Entry-point agent can delegate tasks to specialist agents | LLM calls `delegate_to_ModelExplorer("Find customer types")` |
| FR-9.2 | Delegation tools are auto-generated from agent configuration | Adding a new agent in config → delegation tool appears automatically |
| FR-9.3 | Sub-agents have their own memory (configurable window size) | Sub-agent sees last 20 messages (default) |
| FR-9.4 | Sub-agent results are returned to the orchestrating agent | Result from ModelExplorer → visible to entry-point agent |
| FR-9.5 | Delegation events appear in the execution timeline | Timeline shows: Agent→SubAgent→Tool→Result |
| FR-9.6 | Sub-agents inherit the user's SecurityContext | Sub-agent can't access data the user can't see |
| FR-9.7 | Sub-agents inherit the active TransactionScope | Sub-agent edits within same transaction |

### FR-10: Background & Standalone Agents (P2)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-10.1 | Background agents are triggered by cron-like schedules | `schedule="0 9 * * 1-5"` triggers weekday mornings |
| FR-10.2 | Background agents are triggered by events (pre-turn, post-turn, chat phrase) | Event triggers work as configured |
| FR-10.3 | Standalone agents are invoked via API or UI button | `POST /api/agent/run?name=my-agent&input=...` |
| FR-10.4 | Background/standalone agents execute with a configured SecurityContext | Admin-level or specific service account |
| FR-10.5 | Background agent execution is logged with full timeline | Audit trail for automated actions |

### FR-11: Provider Management (P1)

| ID | Requirement | Acceptance Criteria |
|----|------------|-------------------|
| FR-11.1 | LLM providers are configured via TopLogic config store | API keys, base URLs, available models |
| FR-11.2 | Provider connection pool is shared across all users | 10 connections per provider; configurable |
| FR-11.3 | Request queue enforces per-provider rate limits | Max 10 concurrent API calls; queue excess |
| FR-11.4 | Rate limit responses (429) trigger automatic retry with backoff | Respects Retry-After; exponential backoff |
| FR-11.5 | Provider health is monitored | Unhealthy provider → fallback to alternative |
| FR-11.6 | Provider-specific features are configurable per model | Anthropic thinking, OpenAI JSON mode, etc. |

---

## 5. Non-Functional Requirements

### NFR-1: Performance

| ID | Requirement | Target | Measurement |
|----|------------|--------|-------------|
| NFR-1.1 | Time to first token (TTFT) | < 2 seconds | From user message submit to first streamed token |
| NFR-1.2 | Tool execution overhead | < 50ms per tool | Time between LLM response and tool invocation |
| NFR-1.3 | Concurrent users | 50+ simultaneous conversations | Load test with 50 concurrent chat sessions |
| NFR-1.4 | Memory per conversation | < 2MB baseline | JVM heap measurement per active session |
| NFR-1.5 | Context compaction time | < 3 seconds | Time to summarize and rebuild context |
| NFR-1.6 | Agent loop latency overhead | < 100ms per iteration | Time spent in framework code (not LLM/tool) |

### NFR-2: Reliability

| ID | Requirement | Target |
|----|------------|--------|
| NFR-2.1 | Agent loop availability | 99.9% (excluding LLM provider outages) |
| NFR-2.2 | No data loss on crash | Conversation memory persisted to KnowledgeBase |
| NFR-2.3 | Transaction rollback on failure | 100% of failed edit transactions rolled back |
| NFR-2.4 | Graceful degradation under load | Queue requests instead of rejecting |

### NFR-3: Security

| ID | Requirement | Target |
|----|------------|--------|
| NFR-3.1 | Security filter enforcement | 100% of tool calls pass through SecurityContext |
| NFR-3.2 | No cross-user data leakage | Memory, transactions, sessions fully isolated |
| NFR-3.3 | API key security | Keys stored encrypted; never logged or returned to clients |
| NFR-3.4 | Audit logging | All tool executions and permission denials logged |

### NFR-4: Maintainability

| ID | Requirement | Target |
|----|------------|--------|
| NFR-4.1 | Core framework size | < 5,000 LOC |
| NFR-4.2 | Test coverage | > 80% for core module |
| NFR-4.3 | Zero external framework dependencies | Only: HTTP client (JDK built-in), JSON (Jackson), logging (SLF4J) |
| NFR-4.4 | Provider wrappers independently testable | Each provider has its own test suite |

### NFR-5: Extensibility

| ID | Requirement | Target |
|----|------------|--------|
| NFR-5.1 | Add new tool without framework changes | `@Tool` annotation + register in config |
| NFR-5.2 | Add new LLM provider without framework changes | Implement `LLMProvider` interface |
| NFR-5.3 | Add new agent without code changes | TopLogic config + restart (or hot-reload) |
| NFR-5.4 | Add new trigger type without framework changes | Implement `TriggerConfig` interface |

---

## 6. System Architecture

### 6.1 Module Structure

```
tl-ai/
├── tl-ai-core/                    ← NEW: The custom framework
│   ├── loop/                      Agent loop, recovery, iteration
│   ├── provider/                  LLM provider abstraction + implementations
│   ├── tool/                      Tool system (registry, execution, specs)
│   ├── memory/                    Conversation memory + compaction
│   ├── security/                  Permission manager, security context bridge
│   ├── transaction/               Transaction scope, lock manager
│   ├── tracking/                  Token budget, execution timeline
│   ├── streaming/                 AgentListener, progress events
│   ├── config/                    Agent configuration, model config
│   └── message/                   Message types (user, assistant, tool, system)
│
├── tl-ai-service/                 ← EXISTING: Modified to use tl-ai-core
│   ├── agents/                    AgentRegistry (adapted to new framework)
│   ├── tools/                     Domain tools (unchanged @Tool methods)
│   ├── providers/                 REMOVED (moved to tl-ai-core/provider/)
│   └── skills/                    Skill system (unchanged)
│
├── tl-chat/                       ← EXISTING: Connect to new AgentListener
│   ├── ChatProcessor.java         Uses DefaultAgentLoop instead of UntypedAgent
│   └── ChatSSEHandler.java        Bridges AgentListener → SSE events
│
├── tl-ai-mcp-server/              ← EXISTING: Unchanged
├── tl-ai-vector-index/            ← EXISTING: Unchanged (keeps langchain4j-qdrant)
└── tl-ai-demo/                    ← EXISTING: Updated examples
```

### 6.2 Dependency Graph

```
tl-ai-core (no LangChain4j dependency)
    ├── java.net.http (JDK built-in HTTP client)
    ├── jackson (JSON serialization)
    ├── slf4j (logging)
    └── TopLogic core (SecurityContext, KnowledgeBase, TLModel)

tl-ai-service
    ├── tl-ai-core
    └── TopLogic platform

tl-chat
    ├── tl-ai-core (AgentListener interface)
    └── tl-ai-service

tl-ai-vector-index
    ├── langchain4j-qdrant (ONLY remaining LangChain4j dependency)
    └── qdrant-client

tl-ai-mcp-server
    ├── tl-ai-service
    └── mcp SDK 0.15.0
```

### 6.3 Data Flow

```
User Message (HTTP/SSE)
    │
    ▼
ChatServlet → ChatProcessor
    │
    ├── Resolve SecurityContext from TopLogic session
    ├── Resolve AgentConfig from AgentRegistry
    ├── Create/restore ConversationMemory
    │
    ▼
DefaultAgentLoop.run(request, listener)
    │
    ├── Build context (system prompt + memory + tool specs)
    │   └── Tool specs filtered by SecurityContext
    │
    ├── Call LLM Provider (streaming)
    │   ├── listener.onThinking(token)        → SSE to UI
    │   └── Parse tool calls from stream
    │
    ├── Execute tools (parallel where safe)
    │   ├── Permission check (SecurityContext)
    │   ├── listener.onToolStart()            → SSE to UI
    │   ├── Tool.execute(args, toolContext)
    │   │   └── toolContext.security filters data
    │   ├── listener.onToolComplete()         → SSE to UI
    │   └── Add result to memory
    │
    ├── Check token budget
    │   ├── > 80%: proactive compaction
    │   └── Exceeded: graceful stop
    │
    └── Loop or return response
         └── listener.onResponse()            → SSE to UI
```

---

## 7. Feature Specifications

### 7.1 Agent Loop — Detailed Specification

#### States

```
INITIALIZING → CALLING_LLM → PROCESSING_RESPONSE
                    ↑                    │
                    │              ┌─────┴──────┐
                    │              ▼             ▼
                    │        EXECUTING_TOOLS   COMPLETE
                    │              │
                    └──────────────┘

Error states:  RECOVERING → (back to CALLING_LLM or COMPLETE)
```

#### Recovery Matrix

| Error | Recovery Action | Max Retries | Backoff |
|-------|----------------|-------------|---------|
| `context_too_long` | Compact memory → retry | 2 | None |
| `max_output_tokens` | Double limit → retry | 3 | None |
| `rate_limit (429)` | Wait Retry-After → retry | 5 | Exponential |
| `server_error (500/503)` | Wait → retry | 3 | 1s, 2s, 4s |
| `tool_execution_error` | Report to LLM as result | N/A | N/A |
| `permission_denied` | Report to LLM as result | N/A | N/A |
| `token_budget_exceeded` | Compact → if still exceeded: stop | 1 | N/A |
| `max_iterations` | Stop with message | N/A | N/A |
| `lock_contention` | Report to LLM | N/A | N/A |

#### Configuration

```java
public record AgentLoopConfig(
    int maxIterations,              // Default: 50
    int maxToolResultTokens,        // Default: 4000
    double compactionThreshold,     // Default: 0.80
    int compactionKeepRecentCount,  // Default: 10 messages
    int maxOutputTokenRetries,      // Default: 3
    int maxRateLimitRetries,        // Default: 5
    int maxServerErrorRetries,      // Default: 3
    Duration rateLimitBaseBackoff,  // Default: 1s
    boolean parallelToolExecution,  // Default: true
    int maxParallelTools            // Default: 5
) {}
```

### 7.2 Provider — Detailed Specification

#### Request/Response Format

Each provider implementation translates between the framework's `ChatRequest`/`ChatResponse` and the provider's native API format.

**OpenAI Translation:**
```
ChatRequest → OpenAI Chat Completion Request
├── messages → messages (with role mapping)
├── tools → tools (function calling format)
├── config.temperature → temperature
├── config.maxTokens → max_tokens
└── config.responseFormat → response_format

OpenAI Chat Completion Response → ChatResponse
├── choices[0].message.content → content
├── choices[0].message.tool_calls → toolCalls
├── usage → usage
└── choices[0].finish_reason → finishReason
```

**Anthropic Translation:**
```
ChatRequest → Anthropic Messages Request
├── messages → messages (with content blocks)
├── tools → tools (tool definitions)
├── config.maxTokens → max_tokens
├── config.thinkingLevel → thinking (extended thinking)
└── system prompt → system parameter

Anthropic Messages Response → ChatResponse
├── content[text blocks] → content
├── content[tool_use blocks] → toolCalls
├── usage → usage
└── stop_reason → finishReason
```

#### Connection Management

```
Provider Pool
├── OpenAI:    HttpClient (shared, JDK 11+)
│              ├── Max connections: 10
│              ├── Timeout: 120s
│              └── Keep-alive: 30s
├── Anthropic: HttpClient (shared)
│              └── Same config
└── Mistral:   HttpClient (shared)
               └── Same config

Request Queue (per provider)
├── Max concurrent: 10 (configurable)
├── Queue size: 100 (configurable)
├── Queue full: reject with RetryLater
└── Priority: FIFO (future: priority by user role)
```

### 7.3 Security — Detailed Specification

#### Permission Check Flow

```
Tool Call Requested by LLM
    │
    ▼
1. Is tool in agent's tool list?
   ├── No → ToolResult.error("Unknown tool")
   └── Yes ↓
    │
    ▼
2. Does user's permission level meet tool's requirement?
   ├── Tool requires WRITE, user has READ → ToolResult.denied()
   └── Meets requirement ↓
    │
    ▼
3. SecurityContext.canUseTool(tool)?
   ├── TopLogic security says no → ToolResult.denied()
   └── Allowed ↓
    │
    ▼
4. Execute tool with SecurityContext in ToolContext
   └── Tool internally uses context.security() to filter data
    │
    ▼
5. Log execution to audit trail
```

#### Permission Levels

```
READ        → Can use read-only tools (browse, search, query)
WRITE       → Can use write tools (create, edit, delete)
ADMIN       → Can use admin tools (config changes, user management)
SYSTEM      → Internal-only (background agents, system operations)
```

#### Data Filtering

```java
// Inside ModelBrowserTool.execute():
List<TLClass> allTypes = model.getClasses();
List<TLClass> visibleTypes = context.security().filterReadable(allTypes);
// Only visibleTypes returned to LLM — user never sees restricted types
```

### 7.4 Memory — Detailed Specification

#### Compaction Algorithm

```
Input: messages[] (full history), tokenBudget, keepRecentCount

1. Separate messages into:
   - system: system messages (always kept)
   - recent: last keepRecentCount messages (always kept verbatim)
   - old: everything else (candidates for compaction)

2. Estimate tokens:
   - systemTokens = estimateTokens(system)
   - recentTokens = estimateTokens(recent)
   - available = tokenBudget - systemTokens - recentTokens - BUFFER

3. If old messages fit in available → return all (no compaction needed)

4. Summarize old messages:
   summary = LLM.chat("Summarize this conversation, preserving:
     - Current task and goal
     - Key decisions made
     - Important findings
     - Tool results that are still relevant
     Keep it concise but complete.")

5. Create summary message:
   summaryMsg = SystemMessage("[Conversation summary: " + summary + "]")

6. Return: system + [summaryMsg] + recent
```

#### Memory Persistence

```
Active Conversation → In-memory (ConversationMemory instance)
                          │
                          ├── On each turn: persist to KnowledgeBase
                          ├── On session end: final persist
                          └── On crash: last persisted state recoverable

Restore: KnowledgeBase → ConversationMemory (on session resume)
```

---

## 8. Migration Plan

### 8.1 Phase Overview

```
Phase 0: Setup                              Week 0
Phase 1: Core Framework (Loop + Provider)    Week 1
Phase 2: Tool System + Security             Week 1-2
Phase 3: Memory + Compaction                Week 2
Phase 4: Integration (Connect to tl-chat)   Week 2-3
Phase 5: Transaction + Locking              Week 3
Phase 6: Multi-Agent Orchestration          Week 3-4
Phase 7: Remove LangChain4j Dependencies    Week 4
Phase 8: Agent Configuration Hot-Reload     Week 4-5
```

### 8.2 Phase Details

#### Phase 0: Setup (Day 1)

- [ ] Create `tl-ai-core` Maven module
- [ ] Add to parent POM
- [ ] Set up package structure
- [ ] Add minimal dependencies (Jackson, SLF4J)
- [ ] Create base interfaces (AgentLoop, LLMProvider, Tool, etc.)

#### Phase 1: Core Framework (Week 1)

- [ ] Implement `Message` types (UserMessage, AssistantMessage, ToolResultMessage, SystemMessage)
- [ ] Implement `ChatRequest`, `ChatResponse`, `TokenUsage` records
- [ ] Implement `ModelConfig` record
- [ ] Implement `AnthropicProvider` (start with one provider)
  - [ ] HTTP client setup (java.net.http)
  - [ ] Messages API integration
  - [ ] Streaming response parsing
  - [ ] Tool call detection and parsing
  - [ ] Extended thinking support
- [ ] Implement `DefaultAgentLoop` (core loop without tools)
  - [ ] System prompt building
  - [ ] LLM call with streaming
  - [ ] Error recovery (retry, backoff)
  - [ ] AgentListener event dispatching
- [ ] Implement `WindowMemory` (simple sliding window)
- [ ] Implement `TokenBudget` tracking
- [ ] **TEST:** Simple conversation works end-to-end

#### Phase 2: Tool System + Security (Week 1-2)

- [ ] Implement `ToolSpec` (JSON Schema representation)
- [ ] Implement `ToolSpecGenerator` (reflection over `@Tool` methods)
- [ ] Implement `ToolExecutor` (argument deserialization, method invocation)
- [ ] Implement `ToolRegistry` (name-based lookup, filtering)
- [ ] Integrate tool execution into `DefaultAgentLoop`
  - [ ] Tool call detection from LLM response
  - [ ] Sequential and parallel execution
  - [ ] Tool result injection back into conversation
  - [ ] Result truncation for oversized outputs
- [ ] Implement `SecurityContext` bridge to TopLogic security
- [ ] Implement `PermissionManager` (permission checks before tool execution)
- [ ] Implement security filtering in `ToolRegistry.getSpecs()`
- [ ] **TEST:** Agent uses tools correctly; security blocks unauthorized access

#### Phase 3: Memory + Compaction (Week 2)

- [ ] Implement `CompactingMemory` (LLM-based summarization)
- [ ] Implement proactive compaction trigger (80% threshold)
- [ ] Implement reactive compaction (on context overflow)
- [ ] Implement tool result truncation
- [ ] Implement token estimation per provider
- [ ] Implement memory persistence to KnowledgeBase
- [ ] **TEST:** Long conversation compacts and continues successfully

#### Phase 4: Integration (Week 2-3)

- [ ] Modify `ChatProcessor` to use `DefaultAgentLoop` instead of `UntypedAgent`
- [ ] Bridge `AgentListener` events to existing SSE handler
- [ ] Implement `OpenAiProvider` and `MistralProvider`
- [ ] Adapt `AgentRegistry` to build agents using new framework
- [ ] Port existing tools (no tool code changes needed — same `@Tool` annotations)
- [ ] **TEST:** Full chat flow works through existing UI

#### Phase 5: Transaction + Locking (Week 3)

- [ ] Implement `TransactionScope` wrapping TopLogic KnowledgeBase transactions
- [ ] Implement `LockManager` with configurable scope and timeout
- [ ] Integrate transaction/locking into `ToolContext`
- [ ] Add approval mode (user reviews before commit)
- [ ] **TEST:** Agent edits are transactional; concurrent edits are locked

#### Phase 6: Multi-Agent Orchestration (Week 3-4)

- [ ] Implement delegation tool auto-generation from agent config
- [ ] Implement sub-agent memory isolation (per-agent window)
- [ ] Implement orchestrator pattern (work-order handoff)
- [ ] Integrate delegation into execution timeline
- [ ] **TEST:** Entry-point agent delegates to specialist; results flow back

#### Phase 7: Remove LangChain4j (Week 4)

- [ ] Remove from POM: `langchain4j`, `langchain4j-agentic`, `langchain4j-mcp`
- [ ] Remove from POM: `langchain4j-open-ai`, `langchain4j-anthropic`, `langchain4j-mistral-ai`
- [ ] Keep in POM: `langchain4j-qdrant` (vector indexing only)
- [ ] Clean up unused imports and adapter code
- [ ] **TEST:** Full regression testing; no LangChain4j classes loaded except Qdrant

#### Phase 8: Agent Configuration Hot-Reload (Week 4-5)

- [ ] Implement configuration change detection
- [ ] Implement hot-reload of agent configs without restart
- [ ] Implement live system prompt editing
- [ ] Implement live tool assignment changes
- [ ] **TEST:** Change agent config in UI → next message uses new config

---

## 9. Testing Strategy

### 9.1 Unit Tests

| Component | Test Focus | Count |
|-----------|-----------|-------|
| `DefaultAgentLoop` | Loop termination, error recovery, iteration limits | 15+ |
| `AnthropicProvider` | Request/response translation, streaming, error handling | 10+ |
| `OpenAiProvider` | Request/response translation, streaming, tool calls | 10+ |
| `MistralProvider` | Request/response translation, streaming | 8+ |
| `ToolSpecGenerator` | Reflection, JSON Schema generation, edge cases | 10+ |
| `ToolExecutor` | Argument deserialization, method invocation, error handling | 10+ |
| `ToolRegistry` | Registration, lookup, filtering, hot-reload | 8+ |
| `WindowMemory` | Add/get, window sliding, token limits | 8+ |
| `CompactingMemory` | Compaction triggers, summarization, message ordering | 10+ |
| `PermissionManager` | Permission checks, rule matching, denial tracking | 10+ |
| `TransactionScope` | Begin/commit/rollback, error handling | 8+ |
| `LockManager` | Acquire/release, timeout, contention | 8+ |
| `TokenBudget` | Tracking, estimation, budget exhaustion | 5+ |

### 9.2 Integration Tests

| Scenario | What It Tests |
|----------|--------------|
| Simple conversation | Loop + Provider + Memory end-to-end |
| Tool-using conversation | Loop + Tools + Registry |
| Permission-denied scenario | Security + Tools + Loop error handling |
| Long conversation with compaction | Memory + Compaction + Loop recovery |
| Concurrent users (10) | Thread safety, session isolation, pool management |
| Agent delegation | Multi-agent + Memory isolation + Timeline |
| Transaction rollback | Transaction + Locking + Tool error |
| Rate limit handling | Provider + Request queue + Backpressure |
| Provider failover | Provider health check + Fallback |

### 9.3 Load Tests

| Scenario | Target |
|----------|--------|
| 50 concurrent conversations (mixed read/write) | All complete within 60s |
| 100 concurrent conversations (read-only) | All complete within 30s |
| 10 concurrent editing sessions (with locking) | Lock contention handled gracefully |
| Long conversation (100 turns) | No degradation; compaction works |

---

## 10. Risk Analysis

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Provider API format changes | Low | Medium | Versioned API clients; integration tests catch regressions |
| Compaction loses critical context | Medium | High | Keep recent messages verbatim; test with real conversations; user can view full history |
| Race conditions in multi-user | Medium | High | Extensive concurrency testing; lock granularity; transaction isolation |
| Token estimation inaccuracy | Medium | Low | Use conservative estimates; buffer in budget; provider-specific tokenizers |
| Security bypass via prompt injection | Medium | High | Security enforced at tool level (not prompt level); data filtered before return |
| Performance regression vs LangChain4j | Low | Medium | Benchmark before/after; the custom loop should be faster (less abstraction) |
| Migration breaks existing functionality | Medium | High | Parallel running period; feature flags; extensive regression testing |
| Lock manager deadlocks | Low | High | Timeout-based locks; no nested locks; lock ordering |
| Hot-reload introduces inconsistency | Medium | Medium | Atomic config swaps; version numbers on configs |

---

## 11. Milestones & Timeline

### Milestone 1: "It Talks" (End of Week 1)
- Agent loop works with Anthropic provider
- Simple conversations produce correct responses
- Token tracking works
- Streaming works

### Milestone 2: "It Uses Tools" (End of Week 2)
- Tool system works (register, discover, execute)
- Parallel tool execution works
- Security checks block unauthorized tools
- Memory compaction works for long conversations

### Milestone 3: "It Replaces LangChain4j" (End of Week 3)
- All 3 providers work (OpenAI, Anthropic, Mistral)
- Chat UI connected to new framework
- All existing tools ported (no tool code changes)
- Transaction + locking works for edit sessions

### Milestone 4: "It's Production-Ready" (End of Week 4-5)
- Multi-agent delegation works
- LangChain4j removed (except Qdrant)
- Agent config hot-reload works
- Load testing passes (50 concurrent users)
- Security audit passes
- Full regression testing complete

---

## 12. Glossary

| Term | Definition |
|------|-----------|
| **Agent Loop** | The core execution cycle: build context → call LLM → execute tools → repeat |
| **AgentListener** | Interface receiving streaming events from the agent loop |
| **Compaction** | Summarizing old conversation messages to fit within token budget |
| **LLM Provider** | Abstraction over a specific LLM API (OpenAI, Anthropic, Mistral) |
| **SecurityContext** | The user's identity and permissions, derived from TopLogic session |
| **Tool** | A function the agent can invoke (e.g., ModelBrowser, TLScript executor) |
| **ToolContext** | Execution context for a tool: security, transaction, progress reporter |
| **ToolSpec** | JSON Schema description of a tool's parameters, sent to the LLM |
| **TransactionScope** | A scope within which edits can be committed or rolled back |
| **TokenBudget** | Tracks cumulative token usage and enforces per-user limits |
| **Work-Order** | Structured handoff from orchestrator agent to specialist agent |
| **Entry-Point Agent** | The main agent that receives user messages in the chat |
| **Specialist Agent** | An agent that handles specific tasks, delegated to by the entry-point |
| **Background Agent** | An agent triggered by time/event, not by user interaction |
| **Hot-Reload** | Applying configuration changes without restarting the application |
| **Backpressure** | Mechanism to slow down request processing when the system is overloaded |

---

*This PRD is a living document. Update it as requirements evolve and implementation progresses.*
