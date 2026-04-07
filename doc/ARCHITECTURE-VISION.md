# TopLogic Agentic System — Architecture Vision & Implementation Plan

**Date:** 2026-04-01
**Purpose:** North-star architectural reference for building the custom agentic framework

---

## 1. The Vision (Keep This In Mind Always)

> An Agentic System capable of examining a TopLogic application and editing it.
> Helping users (humans or other agents) answer their need in the best and most efficient way possible.

Every line of code you write serves this vision. When in doubt about a design decision, come back here.

---

## 2. The Ten Pillars

These are non-negotiable architectural requirements. The framework must support all ten from day one — not as afterthoughts.

### Pillar 1: Security-First Execution
Every tool call, every piece of data returned, every action taken passes through the user's TopLogic security context. The agent cannot see or do more than the user is allowed to. This is not a filter on output — it's enforced at the tool execution layer.

### Pillar 2: Multi-User Concurrency
X users simultaneously, where a "user" can be a person, an external agent, or another system. The framework must be async-first, with per-user isolation, shared resource pools, backpressure, and graceful degradation.

### Pillar 3: Transactional Editing with Locking & Rollback
When an agent edits the application (model, layouts, workflows), the edits happen inside a transaction that can be reviewed, committed, or rolled back. Application locking prevents race conditions between concurrent editors (human or agent).

### Pillar 4: Deep Context Management
Token tracking, proactive compaction, anti-hallucination strategies. The agent must not lose track of a task because the context window filled up. Inspired by Claude Code's 7 recovery paths.

### Pillar 5: Any LLM Provider
A developer configures which model to use for which agent, with specific options (max tokens, thinking level, temperature, etc.). The framework abstracts providers cleanly.

### Pillar 6: Performance & Efficiency
Parallel tool calls, minimal overhead, streaming responses, no unnecessary LLM round-trips. Smart tool selection to avoid redundant calls.

### Pillar 7: Human-in-the-Loop
Chat interface showing current task, agent progress, what's being worked on. Users can approve, deny, redirect, or cancel agent actions.

### Pillar 8: Configurable Agents
Developers define custom agents (system prompt, model, model configs, tools, skills) via TopLogic configuration. Agents can join the chat infrastructure, run as background agents, or be triggered via buttons/hooks.

### Pillar 9: Live Configuration
Developers can edit agent configurations in the running application. Changes take effect immediately without restart.

### Pillar 10: Extensible Tool & Skill Ecosystem
New tools and skills can be added without framework changes. Dual factory pattern (reflective + script-based) already proven in tl-ai.

---

## 3. Answer: Yes, Replace LangChain4j

Given the ten pillars above, **LangChain4j is the wrong foundation**. Here's why, mapped to each pillar:

| Pillar | LangChain4j Support | Custom Framework |
|--------|-------------------|-----------------|
| **1. Security** | None. Tools execute unconditionally. | Permission layer wraps every tool call with user's security context. |
| **2. Multi-User** | Single-user AiServices proxy. Pool workarounds. | Async-first. Virtual threads. Request queue. Per-user isolation. |
| **3. Transactions** | None. No concept of edit sessions. | Transaction context wraps agent turns. Lock manager prevents races. |
| **4. Context Mgmt** | Token window drops old messages. No recovery. | Compaction, recovery paths, token budgets, anti-hallucination. |
| **5. Any Provider** | 20+ providers (overkill — you use 3). | Clean wrappers for the 3 you need. Add more later. |
| **6. Performance** | Synchronous loop blocks threads. | Async loop. Parallel tool execution. Streaming-first. |
| **7. Human-in-Loop** | None. | Permission checks, progress events, task visibility. |
| **8. Config Agents** | Code-only configuration. | XML/TopLogic config. Live reload. |
| **9. Live Config** | Requires restart. | Hot-reload from TopLogic configuration store. |
| **10. Extensibility** | @Tool annotation only. | Dual factory + script-based + skill system. Already proven. |

**LangChain4j gives you provider wrappers and a basic tool loop. You need to own everything else anyway. The provider wrappers are ~400 LOC each. It's not worth the dependency.**

**Keep LangChain4j only for:** Vector index integration (`langchain4j-qdrant`). This is a thin wrapper around the Qdrant client and doesn't pollute your agent architecture.

---

## 4. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Entry Points                                  │
│  ┌──────────┐  ┌──────────────┐  ┌────────────┐  ┌──────────────┐  │
│  │ Chat UI  │  │ MCP Server   │  │ Background │  │ Button/Hook  │  │
│  │ (SSE)    │  │ (HTTP)       │  │ Triggers   │  │ Invocations  │  │
│  └────┬─────┘  └──────┬───────┘  └─────┬──────┘  └──────┬───────┘  │
└───────┼────────────────┼────────────────┼────────────────┼──────────┘
        │                │                │                │
        ▼                ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Session Manager                                  │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ Per-user session: SecurityContext, Memory, TokenBudget,     │    │
│  │ TransactionScope, ProgressChannel                           │    │
│  └─────────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Agent Orchestrator                              │
│  ┌───────────┐  ┌──────────────┐  ┌────────────────────────────┐   │
│  │ Agent     │  │ Permission   │  │ Transaction Manager        │   │
│  │ Registry  │  │ Manager      │  │ (Lock + Rollback)          │   │
│  │ (config)  │  │ (rules +     │  │                            │   │
│  │           │  │  security)   │  │                            │   │
│  └─────┬─────┘  └──────┬───────┘  └────────────┬───────────────┘   │
│        │               │                        │                    │
│        ▼               ▼                        ▼                    │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    Agent Loop (THE CORE)                     │    │
│  │  ┌─────────┐  ┌───────────┐  ┌──────────┐  ┌───────────┐  │    │
│  │  │ Build   │  │ Call LLM  │  │ Execute  │  │ Recovery  │  │    │
│  │  │ Context │→ │ (stream)  │→ │ Tools    │→ │ & Compact │  │    │
│  │  │         │  │           │  │ (parallel)│  │           │  │    │
│  │  └─────────┘  └───────────┘  └──────────┘  └───────────┘  │    │
│  │       ↑                                          │          │    │
│  │       └──────────────────────────────────────────┘          │    │
│  │                    (loop until done)                         │    │
│  └─────────────────────────────────────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Provider Layer                                 │
│  ┌──────────┐  ┌────────────┐  ┌───────────┐  ┌────────────────┐   │
│  │ OpenAI   │  │ Anthropic  │  │ Mistral   │  │ Future         │   │
│  │ Provider │  │ Provider   │  │ Provider  │  │ Providers      │   │
│  └──────────┘  └────────────┘  └───────────┘  └────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Shared Connection Pool + Request Queue + Backpressure        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Tool Ecosystem                                │
│  ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌────────────┐  │
│  │ Model   │ │ Script  │ │ Layout   │ │ Config  │ │ Integration│  │
│  │ Tools   │ │ Tools   │ │ Tools    │ │ Tools   │ │ Tools      │  │
│  └─────────┘ └─────────┘ └──────────┘ └─────────┘ └────────────┘  │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ Tool Registry (Reflective + Script + Custom Factories)      │    │
│  └─────────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ Security Filter: Every tool call filtered through user's     │    │
│  │ TopLogic SecurityContext before execution                    │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. Core Interfaces — The Clean API

This is the foundation. Get these right and everything else follows. Designed to be simple, composable, and testable.

### 5.1 The Agent Loop (The Heart)

```java
/**
 * The core agent loop. Takes a user message, runs LLM + tools in a loop,
 * returns the final response. This is the most important class in the system.
 *
 * Responsibilities:
 * - Build context (system prompt + memory + tool specs)
 * - Call LLM with streaming
 * - Detect and execute tool calls (with permission checks)
 * - Manage token budget and trigger compaction
 * - Handle errors with recovery strategies
 * - Stream progress events to the caller
 */
public interface AgentLoop {

    /**
     * Run the agent loop for a single user turn.
     *
     * @param request  The user's message + context
     * @param listener Receives streaming events (progress, tool calls, responses)
     * @return The final agent response (after all tool calls complete)
     */
    AgentResponse run(AgentRequest request, AgentListener listener);
}
```

### 5.2 LLM Provider

```java
/**
 * Abstraction over LLM providers. Intentionally minimal.
 * One implementation per provider (OpenAI, Anthropic, Mistral).
 */
public interface LLMProvider {

    /** Send a chat request and stream the response. */
    void chat(ChatRequest request, StreamHandler handler);

    /** Provider metadata. */
    String name();
    List<String> availableModels();
}

public interface StreamHandler {
    void onToken(String token);
    void onToolCall(ToolCallRequest toolCall);
    void onComplete(ChatResponse response);
    void onError(LLMException error);
}

public record ChatRequest(
    String model,
    List<Message> messages,
    List<ToolSpec> tools,
    ModelConfig config  // temperature, maxTokens, thinkingLevel, etc.
) {}

public record ChatResponse(
    String content,
    List<ToolCallRequest> toolCalls,
    TokenUsage usage,
    FinishReason finishReason
) {}

public record TokenUsage(int inputTokens, int outputTokens, int totalTokens) {}
```

### 5.3 Tool System

```java
/**
 * A tool that the agent can invoke. Security-aware.
 * Tools declare their capabilities so the framework can enforce permissions.
 */
public interface Tool {
    String name();
    String description();
    ToolSpec spec();  // JSON Schema for parameters

    /**
     * Execute the tool within the given security context.
     * The tool MUST respect the security context — it cannot access
     * data or perform actions the user is not permitted to.
     *
     * @param args     Deserialized arguments from LLM
     * @param context  Execution context (security, transaction, progress)
     * @return Tool result (sent back to LLM)
     */
    ToolResult execute(ToolArgs args, ToolContext context);

    /** Can this tool run in parallel with other tools? */
    default boolean isConcurrencySafe() { return true; }

    /** Does this tool modify application state? */
    default boolean isReadOnly() { return false; }

    /** What permission level is required? */
    default PermissionLevel requiredPermission() {
        return isReadOnly() ? PermissionLevel.READ : PermissionLevel.WRITE;
    }
}

/**
 * Context passed to every tool execution. Carries the user's
 * security context, the active transaction, and progress reporting.
 */
public record ToolContext(
    SecurityContext security,       // Who is the user? What can they see/do?
    TransactionScope transaction,   // Active edit transaction (if any)
    ProgressReporter progress,      // Report progress to UI
    AgentContext agentContext        // Parent agent state
) {}
```

### 5.4 Security

```java
/**
 * The security layer. Wraps TopLogic's existing security system.
 * Every tool call is filtered through this.
 */
public interface SecurityContext {

    /** Can this user invoke this tool? */
    boolean canUseTool(Tool tool);

    /** Can this user see this model element / data? */
    boolean canRead(TLObject object);

    /** Can this user modify this model element / data? */
    boolean canWrite(TLObject object);

    /** Filter a collection to only elements the user can see. */
    <T extends TLObject> List<T> filterReadable(Collection<T> objects);

    /** The underlying TopLogic person/account. */
    TLPerson getUser();
}
```

### 5.5 Memory & Context Management

```java
/**
 * Conversation memory with compaction support.
 * Unlike LangChain4j's ChatMemory, this actively manages token budget.
 */
public interface ConversationMemory {

    void addUserMessage(String content);
    void addAssistantMessage(String content);
    void addToolResult(String toolName, String result);

    /** Get messages for the next LLM call, respecting token budget. */
    List<Message> getMessages(int tokenBudget);

    /** Estimate current token usage. */
    int estimateTokens();

    /** Force compaction (summarize old messages). */
    void compact(LLMProvider summarizer);

    /** Get full conversation history (for persistence). */
    List<Message> getFullHistory();
}
```

### 5.6 Transactions & Locking

```java
/**
 * Manages edit transactions. When an agent edits the application,
 * changes happen inside a scope that can be committed or rolled back.
 */
public interface TransactionScope {

    /** Begin an edit transaction. Acquires locks as needed. */
    void begin();

    /** Commit all changes made during this transaction. */
    void commit();

    /** Roll back all changes. Application returns to pre-transaction state. */
    void rollback();

    /** Is there an active transaction? */
    boolean isActive();

    /**
     * Execute an action within this transaction.
     * If the action throws, the transaction is rolled back.
     */
    <T> T execute(Supplier<T> action);
}

/**
 * Prevents concurrent edits to the same application.
 */
public interface LockManager {

    /** Try to acquire an edit lock. Returns false if already locked. */
    boolean tryLock(String scope, SecurityContext user, Duration timeout);

    /** Release the edit lock. */
    void unlock(String scope, SecurityContext user);

    /** Who holds the lock? */
    Optional<LockInfo> getLockInfo(String scope);
}
```

### 5.7 Agent Configuration

```java
/**
 * Configuration for an agent. Loaded from TopLogic config store.
 * Can be modified live by developers — changes take effect immediately.
 */
public record AgentConfig(
    String name,
    String description,
    String model,
    ModelConfig modelConfig,
    String systemPrompt,
    List<String> toolNames,        // Which tools this agent can use
    List<String> skillNames,       // Which skills this agent has
    boolean isEntryPoint,          // Is this the main chat agent?
    AgentType type,                // CHAT, BACKGROUND, STANDALONE
    TriggerConfig trigger,         // For background agents
    PermissionLevel permissionLevel,
    int memoryWindowSize,
    Map<String, String> metadata
) {}

public enum AgentType {
    CHAT,           // Part of chat infrastructure
    BACKGROUND,     // Triggered by time/event
    STANDALONE      // Invoked via button/API
}
```

### 5.8 Progress & Streaming

```java
/**
 * Listener for agent loop events. Drives the chat UI.
 */
public interface AgentListener {

    /** Agent is thinking (LLM streaming text). */
    void onThinking(String partialText);

    /** Agent is calling a tool. */
    void onToolStart(String toolName, Map<String, Object> args);

    /** Tool execution progress update. */
    void onToolProgress(String toolName, String status);

    /** Tool completed. */
    void onToolComplete(String toolName, ToolResult result);

    /** Agent delegated to a sub-agent. */
    void onDelegation(String agentName, String task);

    /** Final response ready. */
    void onResponse(AgentResponse response);

    /** Error occurred. */
    void onError(AgentException error);

    /** Token usage update. */
    void onUsage(TokenUsage usage);
}
```

---

## 6. What To Build First

### The Priority Stack

The order matters. Each layer builds on the previous one. You can test each layer independently before moving to the next.

```
Layer 6: Agent Configuration (live reload, developer UX)     ← later
Layer 5: Multi-Agent Orchestration (delegation, routing)     ← later
Layer 4: Transaction & Locking (edit sessions, rollback)     ← after core works
Layer 3: Security Layer (permission checks per tool call)    ← after tools work
Layer 2: Tool System (registry, execution, parallel)         ← after loop works
Layer 1: Agent Loop + Provider + Memory (THE CORE)           ← START HERE
```

### Layer 1: The Core (Start Here — Week 1)

**What:** The agent loop that takes a message, calls an LLM, executes tools, and returns a response. With memory and context management.

**Why first:** Everything else depends on this. If the loop is right, everything else is clean. If the loop is wrong, everything else is a workaround.

**Components:**

```
tl-ai-core/
├── src/main/java/com/top_logic/ai/core/
│   ├── loop/
│   │   ├── AgentLoop.java              (interface)
│   │   └── DefaultAgentLoop.java       (implementation — THE file)
│   ├── provider/
│   │   ├── LLMProvider.java            (interface)
│   │   ├── ChatRequest.java            (record)
│   │   ├── ChatResponse.java           (record)
│   │   ├── StreamHandler.java          (interface)
│   │   ├── ModelConfig.java            (record)
│   │   └── impl/
│   │       └── AnthropicProvider.java  (start with one provider)
│   ├── memory/
│   │   ├── ConversationMemory.java     (interface)
│   │   ├── WindowMemory.java           (sliding window)
│   │   └── CompactingMemory.java       (with LLM summarization)
│   ├── message/
│   │   ├── Message.java                (sealed interface)
│   │   ├── UserMessage.java            (record)
│   │   ├── AssistantMessage.java       (record)
│   │   ├── ToolResultMessage.java      (record)
│   │   └── SystemMessage.java          (record)
│   └── tracking/
│       ├── TokenUsage.java             (record)
│       └── TokenBudget.java            (tracks cumulative usage)
```

**Test scenario for Layer 1:**
```java
// Create a provider
LLMProvider provider = new AnthropicProvider(apiKey, "claude-sonnet-4-6");

// Create memory
ConversationMemory memory = new CompactingMemory(provider, 100_000);

// Create the loop (no tools yet — just conversation)
AgentLoop loop = new DefaultAgentLoop(provider, memory);

// Run it
AgentResponse response = loop.run(
    AgentRequest.of("What is 2 + 2?"),
    new LoggingListener()  // prints events to console
);

assert response.content().contains("4");
assert response.usage().totalTokens() > 0;
```

**When this works, you have:** A working LLM conversation with streaming, memory, and token tracking. No tools yet, but the loop is proven.

### Layer 2: Tool System (Week 1-2)

**What:** Tool registry, tool execution within the agent loop, parallel tool calls.

**Components:**

```
│   ├── tool/
│   │   ├── Tool.java                   (interface — as defined above)
│   │   ├── ToolSpec.java               (JSON Schema representation)
│   │   ├── ToolArgs.java               (deserialized arguments)
│   │   ├── ToolResult.java             (execution result)
│   │   ├── ToolContext.java            (security + transaction + progress)
│   │   ├── ToolRegistry.java           (name → Tool lookup)
│   │   ├── ToolSpecGenerator.java      (reflection: @Tool → ToolSpec)
│   │   └── ToolExecutor.java           (invoke method, handle errors)
```

**Test scenario for Layer 2:**
```java
// Define a simple tool
public class CalculatorTools {
    @Tool("Add two numbers")
    public String add(@P("first number") int a, @P("second number") int b) {
        return String.valueOf(a + b);
    }
}

// Register it
ToolRegistry registry = new ToolRegistry();
registry.register(new CalculatorTools());

// Create loop with tools
AgentLoop loop = new DefaultAgentLoop(provider, memory, registry);

// Run — the LLM should call the add tool
AgentResponse response = loop.run(
    AgentRequest.of("What is 17 + 25?"),
    new LoggingListener()
);

// Listener should have received:
// onToolStart("add", {a: 17, b: 25})
// onToolComplete("add", "42")
// onResponse("17 + 25 = 42")
```

**When this works, you have:** A fully functional agent that can call tools. You can now port your existing TopLogic tools (ModelBrowser, TLScript, etc.) to this framework.

### Layer 3: Security (Week 2)

**What:** Wrap every tool call with the user's security context. Filter data. Check permissions.

**Test scenario:**
```java
// User with read-only access
SecurityContext readOnlyUser = SecurityContext.forUser(regularUser);

// This should work (read-only tool)
ToolContext ctx = new ToolContext(readOnlyUser, null, progress, agentCtx);
ToolResult result = modelBrowserTool.execute(args, ctx);

// This should be denied (write tool)
ToolResult denied = modelEditorTool.execute(args, ctx);
assert denied.isDenied();
assert denied.message().contains("not permitted");
```

### Layer 4: Transactions & Locking (Week 2-3)

**What:** Edit sessions with commit/rollback. Lock manager for concurrent editors.

### Layer 5: Multi-Agent Orchestration (Week 3)

**What:** Agent delegation, routing, background agents, triggers.

### Layer 6: Agent Configuration (Week 3-4)

**What:** Live configuration via TopLogic config store. Hot-reload.

---

## 7. Design Principles (Non-Negotiable)

### 7.1 Security Context Flows Down, Never Up

```
User → Session → AgentLoop → Tool → TopLogic API
  └─── SecurityContext is passed through every layer ───┘

NEVER: Tool checks its own permissions.
ALWAYS: Framework checks permissions BEFORE calling the tool.
         Tool receives pre-filtered data.
```

### 7.2 The Agent Loop Owns Error Recovery

```
If LLM call fails       → retry with backoff, fallback model
If context too large     → compact memory, retry
If tool fails            → report error to LLM (not to user)
If permission denied     → report denial to LLM (let it adapt)
If token budget exceeded → compact or gracefully stop
If lock unavailable      → report to LLM, suggest alternatives
```

The loop handles ALL errors. Tools and providers just throw. The loop decides what to do.

### 7.3 Everything Is Streaming

```
NO:  response = loop.run(request);  // blocks for 30 seconds
YES: loop.run(request, listener);   // streams events immediately
```

Users must see progress. The UI must update in real-time. Every tool call, every token, every delegation — streamed.

### 7.4 Tools Are Pure Functions (As Much As Possible)

```
Tool.execute(args, context) → result

Input:  args (from LLM) + context (security, transaction, progress)
Output: result (string or structured data)

Tools should NOT:
- Access global state
- Start threads
- Call other tools
- Manage their own transactions

Tools SHOULD:
- Use context.security to filter data
- Use context.transaction for writes
- Use context.progress to report status
- Return structured results
```

### 7.5 Configuration Is Data, Not Code

```
Agent definitions:     TopLogic config store (XML/live-editable)
Tool registration:     @Tool annotations (code) + ScriptFactory (config)
Skills:                Markdown files (editable by developers)
Provider config:       TopLogic config store (API keys, models, options)
Permission rules:      TopLogic security system (existing)
```

A developer should be able to create a new agent, add tools, set a system prompt, and have it working — without writing Java code or restarting the application.

### 7.6 Per-User Isolation

```
User A's conversation:  own memory, own token budget, own transaction
User B's conversation:  own memory, own token budget, own transaction

Shared resources:       LLM provider pool, tool registry, agent configs

NEVER shared:           conversation state, edit transactions, security context
```

---

## 8. DefaultAgentLoop — The Implementation Blueprint

This is the most important class. Here's the pseudocode for how it should work:

```java
public class DefaultAgentLoop implements AgentLoop {

    private final LLMProvider provider;
    private final ToolRegistry tools;
    private final PermissionManager permissions;
    private final ConversationMemory memory;
    private final TokenBudget budget;
    private final RecoveryStrategy recovery;

    @Override
    public AgentResponse run(AgentRequest request, AgentListener listener) {
        // 1. Add user message to memory
        memory.addUserMessage(request.content());

        // 2. The loop
        int maxIterations = 50;  // Safety limit
        for (int i = 0; i < maxIterations; i++) {

            // 3. Build context: system prompt + memory messages + tool specs
            List<Message> messages = memory.getMessages(budget.remaining());
            List<ToolSpec> toolSpecs = tools.getSpecs(request.securityContext());
            ChatRequest chatRequest = new ChatRequest(
                request.model(), messages, toolSpecs, request.modelConfig()
            );

            // 4. Call LLM with streaming
            ChatResponse response;
            try {
                response = callLLM(chatRequest, listener);
            } catch (ContextTooLargeException e) {
                // 5a. Recovery: compact and retry
                memory.compact(provider);
                listener.onCompaction();
                continue;
            } catch (MaxOutputTokensException e) {
                // 5b. Recovery: retry with higher limit
                request = request.withHigherOutputLimit();
                continue;
            } catch (RateLimitException e) {
                // 5c. Recovery: wait and retry
                Thread.sleep(e.retryAfterMs());
                continue;
            }

            // 6. Update token budget
            budget.add(response.usage());
            listener.onUsage(budget.cumulative());

            // 7. Add assistant message to memory
            memory.addAssistantMessage(response.content());

            // 8. Check for tool calls
            if (response.toolCalls().isEmpty()) {
                // Done! Return final response.
                return new AgentResponse(response.content(), budget.cumulative());
            }

            // 9. Execute tool calls (potentially in parallel)
            List<ToolResult> results = executeTools(
                response.toolCalls(), request.toolContext(), listener
            );

            // 10. Add tool results to memory
            for (ToolResult result : results) {
                memory.addToolResult(result.toolName(), result.content());
            }

            // 11. Check token budget
            if (budget.isExhausted()) {
                // Compact and check if we can continue
                memory.compact(provider);
                if (budget.isExhausted()) {
                    return AgentResponse.budgetExhausted(budget.cumulative());
                }
            }
        }

        return AgentResponse.maxIterationsReached(budget.cumulative());
    }

    private List<ToolResult> executeTools(
            List<ToolCallRequest> calls,
            ToolContext context,
            AgentListener listener) {

        // Separate into parallel-safe and sequential tools
        List<ToolCallRequest> parallel = new ArrayList<>();
        List<ToolCallRequest> sequential = new ArrayList<>();

        for (ToolCallRequest call : calls) {
            Tool tool = tools.get(call.name());
            if (tool == null) {
                // Hallucinated tool — report to LLM
                sequential.add(call);  // Will return error result
                continue;
            }

            // Permission check
            if (!permissions.check(tool, context.security())) {
                listener.onToolDenied(call.name(), "Not permitted");
                sequential.add(call);  // Will return denial result
                continue;
            }

            if (tool.isConcurrencySafe()) {
                parallel.add(call);
            } else {
                sequential.add(call);
            }
        }

        List<ToolResult> results = new ArrayList<>();

        // Execute parallel tools concurrently
        if (!parallel.isEmpty()) {
            results.addAll(executeParallel(parallel, context, listener));
        }

        // Execute sequential tools one by one
        for (ToolCallRequest call : sequential) {
            results.add(executeSingle(call, context, listener));
        }

        return results;
    }

    private ToolResult executeSingle(
            ToolCallRequest call,
            ToolContext context,
            AgentListener listener) {

        Tool tool = tools.get(call.name());
        if (tool == null) {
            return ToolResult.error(call.name(),
                "Unknown tool: " + call.name() + ". Available: " + tools.names());
        }

        if (!permissions.check(tool, context.security())) {
            return ToolResult.denied(call.name(),
                "User does not have permission to use " + call.name());
        }

        listener.onToolStart(call.name(), call.arguments());

        try {
            ToolArgs args = ToolArgs.deserialize(call.arguments(), tool.spec());
            ToolResult result = tool.execute(args, context);
            listener.onToolComplete(call.name(), result);

            // Truncate oversized results
            if (result.estimateTokens() > MAX_TOOL_RESULT_TOKENS) {
                result = result.truncate(MAX_TOOL_RESULT_TOKENS);
            }

            return result;
        } catch (Exception e) {
            ToolResult error = ToolResult.error(call.name(), e.getMessage());
            listener.onToolError(call.name(), e);
            return error;  // Report to LLM, not to user
        }
    }
}
```

---

## 9. What NOT To Build (Yet)

Focus is critical. These are important but NOT for the first iteration:

| Feature | Why Wait |
|---------|----------|
| **RAG pipeline** | Keep using LangChain4j-Qdrant for now. Port later if needed. |
| **MCP server** | Your existing MCPServerService works. Don't rewrite it. |
| **Chat UI** | Your existing tl-chat works. Connect it to the new loop. |
| **Background agents** | Get the core loop right first. Background is just a different trigger. |
| **Agent-to-agent communication** | Multi-agent orchestration comes after single-agent works. |
| **Skill system** | Port as-is from current tl-ai. No rewrite needed. |
| **Vector indexing** | Keep LangChain4j-Qdrant wrapper. Not worth replacing. |

---

## 10. Migration Strategy

### Phase 1: Build Core in Parallel (no breaking changes)

Create `tl-ai-core` module alongside existing code. Both systems run. Test new core with simple tools first.

### Phase 2: Port Tools One by One

Move tools from LangChain4j `@Tool` to new framework `@Tool` (keep same annotation, change execution path). Tools are the easiest to port — they're mostly independent.

### Phase 3: Switch Agent Loop

Replace `UntypedAgent.chat()` calls in `ChatProcessor` with `DefaultAgentLoop.run()`. This is the critical switchover.

### Phase 4: Remove LangChain4j Dependencies

Drop `langchain4j`, `langchain4j-agentic`, `langchain4j-mcp`, `langchain4j-anthropic`, `langchain4j-open-ai`, `langchain4j-mistral-ai` from POM. Keep only `langchain4j-qdrant` (for now).

### Phase 5: Enhance

Add compaction, recovery paths, permission rules, transaction management, locking — all the Claude Code-inspired features that LangChain4j couldn't support.

---

## 11. Success Criteria

You know you've succeeded when:

1. **A developer** can create a new agent via TopLogic configuration, assign it tools and a system prompt, and use it in the chat — without writing Java code or restarting.

2. **A user** can ask the agent complex questions about the application, get accurate answers filtered by their permissions, and request temporary views — all within the chat interface.

3. **Two developers** cannot edit the same application simultaneously — the locking mechanism prevents it, and rollback is available if an agent's edit was wrong.

4. **50 users** can chat with the agent simultaneously without performance degradation, with each user's conversation isolated and their security context enforced.

5. **A long conversation** (100+ turns) doesn't degrade — the compaction system keeps the context fresh and the agent focused.

6. **An LLM API failure** doesn't crash the conversation — the recovery system retries, falls back, or gracefully reports the issue.

---

*This document is the north star. Every design decision should be traceable to one of the ten pillars. When in doubt, come back here.*
