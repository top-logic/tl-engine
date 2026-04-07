# Claude Code vs tl-ai: Deep Architectural Comparison Report

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Context Window Management & Compaction](#2-context-window-management--compaction)
3. [Concurrent Tool Execution](#3-concurrent-tool-execution)
4. [Streaming Token Deltas](#4-streaming-token-deltas)
5. [Max Output Token Escalation](#5-max-output-token-escalation)
6. [Tool Result Size Management](#6-tool-result-size-management)
7. [Prompt Caching](#7-prompt-caching)
8. [Permission & Safety Model](#8-permission--safety-model)
9. [Error Handling & Retry Logic](#9-error-handling--retry-logic)
10. [Agent Spawning & Context Forking](#10-agent-spawning--context-forking)
11. [System Prompt Construction](#11-system-prompt-construction)
12. [Recommendations & Priority](#12-recommendations--priority)

---

## 1. Architecture Overview

### Side-by-Side

| Aspect | Claude Code | tl-ai |
|--------|------------|-------|
| Language | TypeScript (Bun runtime) | Java 21 (Maven, servlet container) |
| LLM Integration | Direct Anthropic SDK | LangChain4j abstraction layer |
| Agent Loop | Custom async generator (`query()`) with full control | LangChain4j `AiService` proxy (opaque loop) |
| Streaming | Native async generators, token-level deltas | Polling-based SSE (200ms interval), message-level |
| Context Management | 3-tier compaction (auto, micro, reactive) | Fixed sliding window (`MessageWindowChatMemory`) |
| Tool Execution | Concurrent with `StreamingToolExecutor` | Sequential (LangChain4j default) |
| Configuration | YAML frontmatter + env vars + feature flags | TopLogic XML config system |
| Persistence | File-based (memdir, transcripts) | KnowledgeBase (relational DB) |
| UI | Terminal (Ink/React) + IDE extensions | Web chat (JSP + SSE + JS) |

### Key Philosophical Difference

**Claude Code owns its agent loop entirely.** The `query()` async generator gives total control over retries, compaction, streaming, token budgets, and recovery. This is the foundation that enables every advanced feature.

**tl-ai delegates to LangChain4j's `AiService` proxy.** The agent loop is opaque — you call `agent.invoke()` and get a result. This gives you multi-provider support for free but limits control over mid-loop decisions.

```
Claude Code:
  query() generator → stream API → process deltas → dispatch tools concurrently
    → check token budget → auto-compact if needed → retry on error → yield results
    → loop until done

tl-ai:
  agent.invoke(message) → [LangChain4j handles everything internally] → return result
```

**Implication:** The highest-value adoptions require working *around* or *on top of* LangChain4j, or selectively replacing its abstractions with custom ones.

---

## 2. Context Window Management & Compaction

### The Gap

This is the **single biggest architectural gap**. Claude Code has a sophisticated 3-tier system; tl-ai has a fixed sliding window.

### Claude Code: 3-Tier Compaction

**Tier 1 — Auto-Compact (Token Threshold Trigger)**

Triggers when context approaches model limits. Uses the LLM itself to summarize history.

```typescript
// apiMicrocompact.ts - Trigger thresholds
const DEFAULT_MAX_INPUT_TOKENS = 180_000  // Fire at 180k tokens
const DEFAULT_TARGET_INPUT_TOKENS = 40_000 // Keep last 40k tokens

const strategy: ContextEditStrategy = {
  type: 'clear_tool_uses_20250919',
  trigger: {
    type: 'input_tokens',
    value: triggerThreshold,        // 180k
  },
  clear_at_least: {
    type: 'input_tokens',
    value: triggerThreshold - keepTarget,  // Clear at least 140k
  },
  clear_tool_inputs: TOOLS_CLEARABLE_RESULTS,  // Shell, Glob, Grep, FileRead, etc.
}
```

After compaction, Claude Code re-injects critical context:

```typescript
// compact.ts - Post-compact re-injection
// 1. Recently read files (up to 5)
// 2. Deferred tool listings
// 3. Agent listings
// 4. MCP server instructions
// 5. Active skill attachments
// 6. Plan file if in plan mode
const summaryMessages: UserMessage[] = [
  createUserMessage({
    content: getCompactUserSummaryMessage(summary, suppressFollowUpQuestions, transcriptPath),
    isCompactSummary: true,
    isVisibleInTranscriptOnly: true,
  }),
]
```

**Tier 2 — Micro-Compact (Cache-Editing)**

Incrementally removes old tool results via API-level cache edits WITHOUT invalidating the cached prompt prefix:

```typescript
// microCompact.ts - Cache-aware deletion
export type PendingCacheEdits = {
  trigger: 'auto'
  deletedToolIds: string[]
  baselineCacheDeletedTokens: number  // Track cumulative deletions
}
// Does NOT modify local messages — only tells the API to skip cached content
```

**Tier 3 — Reactive Compact (Emergency)**

Triggers on `prompt-too-long` API errors. Last resort before failing.

### tl-ai: Fixed Sliding Window

```java
// Current: Simple message count limit
ChatMemory subMemory = MessageWindowChatMemory.withMaxMessages(20);

// PinnedUserMessageChatMemory preserves the user's request (good start!)
// But when the window slides, all intermediate context is LOST — not summarized
```

### What To Build: `SummarizingChatMemory`

```java
/**
 * ChatMemory that summarizes older messages instead of dropping them.
 * When token count exceeds threshold, calls LLM to compress older messages
 * into a summary block, preserving system prompt and recent messages verbatim.
 *
 * Architecture:
 *   [SystemMessage] [Summary of turns 1-N] [Recent messages N+1 to current]
 */
public class SummarizingChatMemory implements ChatMemory {

    /** Token threshold at which compaction triggers. */
    private static final int COMPACTION_TRIGGER_TOKENS = 100_000;

    /** Target token count after compaction. */
    private static final int COMPACTION_TARGET_TOKENS = 30_000;

    private final ChatMemory _delegate;
    private final ChatModel _summarizer;
    private final Tokenizer _tokenizer;
    private String _summary;  // Compressed older context

    public SummarizingChatMemory(ChatMemory delegate, ChatModel summarizer, Tokenizer tokenizer) {
        _delegate = delegate;
        _summarizer = summarizer;
        _tokenizer = tokenizer;
    }

    @Override
    public void add(ChatMessage message) {
        _delegate.add(message);
        maybeCompact();
    }

    private void maybeCompact() {
        List<ChatMessage> messages = _delegate.messages();
        int totalTokens = estimateTokens(messages);

        if (totalTokens <= COMPACTION_TRIGGER_TOKENS) {
            return;
        }

        // Find split point: keep recent messages within TARGET, compress the rest
        int keepFrom = messages.size();
        int keptTokens = 0;
        while (keepFrom > 1 && keptTokens < COMPACTION_TARGET_TOKENS) {
            keepFrom--;
            keptTokens += estimateTokens(messages.get(keepFrom));
        }

        // Summarize messages [1..keepFrom) (skip system message at index 0)
        List<ChatMessage> toSummarize = messages.subList(1, keepFrom);
        if (toSummarize.isEmpty()) {
            return;
        }

        String summaryPrompt = buildSummaryPrompt(toSummarize);
        // Use a cheap/fast model for summarization
        String newSummary = _summarizer.chat(summaryPrompt);

        // Replace old messages with summary
        _summary = (_summary != null ? _summary + "\n\n" : "") + newSummary;

        // Rebuild: [SystemMessage] [SummaryMessage] [Recent messages]
        List<ChatMessage> kept = new ArrayList<>(messages.subList(keepFrom, messages.size()));
        _delegate.clear();
        _delegate.add(messages.get(0));  // System message
        _delegate.add(UserMessage.from("[Prior conversation summary]\n" + _summary));
        for (ChatMessage msg : kept) {
            _delegate.add(msg);
        }
    }

    private String buildSummaryPrompt(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Summarize this conversation segment concisely. ");
        sb.append("Preserve: key decisions, tool results, discovered facts, errors encountered. ");
        sb.append("Drop: verbose tool output, intermediate reasoning, repeated information.\n\n");
        for (ChatMessage msg : messages) {
            sb.append(msg.type()).append(": ").append(msg.text()).append("\n\n");
        }
        return sb.toString();
    }

    private int estimateTokens(List<ChatMessage> messages) {
        return messages.stream().mapToInt(this::estimateTokens).sum();
    }

    private int estimateTokens(ChatMessage message) {
        // Rough estimate: 1 token ≈ 4 chars
        return message.text().length() / 4;
    }

    @Override
    public List<ChatMessage> messages() { return _delegate.messages(); }
    @Override public Object id() { return _delegate.id(); }
    @Override public void clear() { _summary = null; _delegate.clear(); }
}
```

**Integration point in `ChatProcessor.executeOrchestrator()`:**

```java
// Replace:
ChatMemory effectiveMemory = new PinnedUserMessageChatMemory(chatMemory);

// With:
ChatModel summarizer = aiService.getChatModel("fast-cheap-model"); // e.g. haiku
Tokenizer tokenizer = new OpenAiTokenizer(); // or Anthropic equivalent
ChatMemory summarizing = new SummarizingChatMemory(chatMemory, summarizer, tokenizer);
ChatMemory effectiveMemory = new PinnedUserMessageChatMemory(summarizing);
```

---

## 3. Concurrent Tool Execution

### Claude Code: StreamingToolExecutor

Tools declare whether they're safe to run concurrently. Safe tools execute in parallel; unsafe tools get exclusive access.

```typescript
// Tool.ts - Each tool declares concurrency safety
isConcurrencySafe(input: z.infer<Input>): boolean

// Default: NOT concurrent-safe
const TOOL_DEFAULTS = {
  isConcurrencySafe: (_input?: unknown) => false,
}

// BashTool: conditional — read-only commands are concurrent-safe
isConcurrencySafe(input) {
  return this.isReadOnly?.(input) ?? false
}

// TaskGetTool: always concurrent-safe (read-only)
isConcurrencySafe() {
  return true
}
```

The executor manages a queue with status tracking:

```typescript
// StreamingToolExecutor.ts

// Core concurrency check:
private canExecuteTool(isConcurrencySafe: boolean): boolean {
  const executingTools = this.tools.filter(t => t.status === 'executing')
  return (
    executingTools.length === 0 ||
    (isConcurrencySafe && executingTools.every(t => t.isConcurrencySafe))
  )
}

// Queue processing — order-preserving:
private async processQueue(): Promise<void> {
  for (const tool of this.tools) {
    if (tool.status !== 'queued') continue
    if (this.canExecuteTool(tool.isConcurrencySafe)) {
      await this.executeTool(tool)  // Starts execution, doesn't wait
    } else {
      if (!tool.isConcurrencySafe) break  // Non-concurrent tool blocks queue
    }
  }
}

// Results yielded in order, with progress streaming:
*getCompletedResults(): Generator<MessageUpdate, void> {
  for (const tool of this.tools) {
    // Always yield pending progress messages immediately
    while (tool.pendingProgress.length > 0) {
      yield { message: tool.pendingProgress.shift()! }
    }
    if (tool.status === 'completed' && tool.results) {
      tool.status = 'yielded'
      for (const message of tool.results) {
        yield { message, newContext: this.toolUseContext }
      }
    } else if (tool.status === 'executing' && !tool.isConcurrencySafe) {
      break  // Stop yielding if blocking non-concurrent tool
    }
  }
}
```

Additionally, **sibling error cascading** — when a Bash tool errors, it cancels all sibling tools:

```typescript
if (isErrorResult && tool.block.name === BASH_TOOL_NAME) {
  this.hasErrored = true
  this.siblingAbortController.abort('sibling_error')
}
```

### tl-ai: Sequential Execution

LangChain4j's `AiService` proxy executes tools one at a time. No concurrency.

### What To Build: `ConcurrentToolExecutor`

Since LangChain4j doesn't expose a hook for custom tool execution, you'd need to either:

**Option A: Wrap the ToolExecutor at registration time** (simpler, works within LangChain4j)

```java
/**
 * Wraps tool executors to support concurrent execution of read-only tools.
 * When LangChain4j returns multiple tool_use blocks, this executor
 * partitions them into concurrent (read-only) and exclusive batches.
 */
public class ConcurrentToolExecutorWrapper {

    /** Tools whose execution is always safe to parallelize. */
    private static final Set<String> CONCURRENT_SAFE_PREFIXES = Set.of(
        "model-browser.",    // Read-only model queries
        "model-search.",     // Search operations
        "script-doc.",       // Documentation lookups
        "help."              // Help lookups
    );

    private static final Set<String> NEVER_CONCURRENT_PREFIXES = Set.of(
        "model-modification.",  // Writes
        "model-annotation-editor.",  // Writes
        "script-exec."  // Side effects
    );

    public static boolean isConcurrencySafe(String toolName) {
        return CONCURRENT_SAFE_PREFIXES.stream().anyMatch(toolName::startsWith);
    }
}
```

**Option B: Custom agent loop** (more work, full control like Claude Code)

Replace `AgenticServices.agentBuilder()` with a custom loop that:
1. Calls the LLM API directly
2. Parses tool_use blocks from the response
3. Partitions into concurrent/exclusive batches
4. Executes concurrent batch in parallel via `CompletableFuture.allOf()`
5. Feeds results back to the LLM
6. Loops until stop_reason = "end_turn"

This is the more impactful approach but requires significant refactoring.

---

## 4. Streaming Token Deltas

### The Gap

Claude Code streams individual tokens as they arrive. The user sees the response being typed character by character. tl-ai waits for the full response, then displays it at once.

### Claude Code: Token-Level Streaming

```typescript
// claude.ts - Processes streaming events from Anthropic API
for await (const event of messageStream) {
  if (event.type === 'content_block_delta') {
    if (event.delta.type === 'text_delta') {
      // Each delta contains a few tokens of text
      yield createAssistantDelta(event.delta.text)
    }
    if (event.delta.type === 'input_json_delta') {
      // Tool input being streamed
      yield createToolInputDelta(event.delta.partial_json)
    }
  }
}

// web/lib/api/stream.ts - SSE parsing for web clients
for await (const message of deps.callModel({...})) {
  if (event.type === "content_block_delta" && event.delta.type === "text_delta") {
    tokensReceived += event.delta.text.length;
  }
  opts?.onProgress?.({ tokensReceived, elapsedMs, isComplete: false });
  yield event;
}
```

### tl-ai: Message-Level Updates

```java
// ChatProcessor.java - Agent invocation blocks until complete
String result = ThreadContextManager.inSystemInteraction(ChatProcessor.class,
    () -> (String) agent.invoke(Map.of("message", trimmed)));
// ^^ This blocks until the ENTIRE response is generated

// SSE handler polls for completed messages
state.addPendingMessage(new ChatMessage("Agent", result));
```

### What To Build: `StreamingChatModel` Integration

LangChain4j supports `StreamingChatLanguageModel`. Use it to push token deltas through SSE:

```java
/**
 * Adapter that bridges LangChain4j's StreamingChatLanguageModel to the
 * ChatSSEHandler via token delta events.
 */
public class StreamingAgentExecutor {

    private final StreamingChatLanguageModel _model;
    private final AIProcessingState _state;

    public CompletableFuture<String> executeStreaming(String systemPrompt,
            List<ChatMessage> messages, String userMessage) {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder fullResponse = new StringBuilder();

        _model.chat(buildRequest(systemPrompt, messages, userMessage),
            new StreamingChatModelListener() {
                @Override
                public void onPartialResponse(String token) {
                    fullResponse.append(token);
                    // Push delta to SSE immediately
                    _state.addTokenDelta(token);
                }

                @Override
                public void onComplete(ChatResponse response) {
                    future.complete(fullResponse.toString());
                }

                @Override
                public void onError(Throwable error) {
                    future.completeExceptionally(error);
                }
            });

        return future;
    }
}
```

New SSE event type:

```java
// ChatSSEHandler.java - New event emission
private void emitTokenDelta(String delta) {
    sendEvent("token_delta", Map.of("text", delta));
}

// In pollAndEmit():
List<String> deltas = state.consumeTokenDeltas();
for (String delta : deltas) {
    emitTokenDelta(delta);
}
```

Client-side rendering:

```javascript
// Chat UI JavaScript
eventSource.addEventListener('token_delta', function(event) {
    const data = JSON.parse(event.data);
    appendToCurrentMessage(data.text);  // Append incrementally
});
```

---

## 5. Max Output Token Escalation

### Claude Code: Start Low, Escalate on Demand

Claude Code starts with a conservative 8k token limit and escalates to 64k if the model hits the cap:

```typescript
// context.ts - Constants
export const CAPPED_DEFAULT_MAX_TOKENS = 8_000
export const ESCALATED_MAX_TOKENS = 64_000

// query.ts - Escalation logic
if (isWithheldMaxOutputTokens(lastMessage)) {
  // Step 1: Escalate from 8k → 64k (single retry, no user message)
  if (maxOutputTokensOverride === undefined) {
    state = {
      ...state,
      maxOutputTokensOverride: ESCALATED_MAX_TOKENS,
      transition: { reason: 'max_output_tokens_escalate' },
    }
    continue  // Retry immediately
  }
}

// Step 2: If 64k also hits cap, inject recovery message and continue
if (maxOutputTokensRecoveryCount < MAX_OUTPUT_TOKENS_RECOVERY_LIMIT) {
  const recoveryMessage = createUserMessage({
    content:
      'Output token limit hit. Resume directly — no apology, no recap. ' +
      'Pick up mid-thought. Break remaining work into smaller pieces.',
    isMeta: true,
  })
  state = {
    ...state,
    messages: [...messagesForQuery, ...assistantMessages, recoveryMessage],
    maxOutputTokensRecoveryCount: maxOutputTokensRecoveryCount + 1,
  }
  continue  // Retry with recovery prompt
}
```

### tl-ai: Fixed Limits

```java
// AgentRegistry.buildChatModel() sets maxTokens from config
// If the model hits the limit, the response is silently truncated
// No retry, no escalation
```

### What To Build

Since LangChain4j handles the agent loop, you can't intercept mid-loop. Two options:

**Option A: Generous default** — Set `maxTokens` high from the start (e.g. 16k for Anthropic, 4k for Mistral). Simple, wasteful on short responses but prevents truncation.

**Option B: Custom response interceptor** — Wrap the `ChatModel` to detect truncated responses and re-invoke:

```java
public class EscalatingMaxTokensChatModel implements ChatModel {

    private static final int INITIAL_MAX_TOKENS = 4096;
    private static final int ESCALATED_MAX_TOKENS = 16384;

    private final ChatModel _delegate;
    private int _currentMaxTokens = INITIAL_MAX_TOKENS;

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatRequest adjusted = withMaxTokens(request, _currentMaxTokens);
        ChatResponse response = _delegate.chat(adjusted);

        if (response.finishReason() == FinishReason.LENGTH && _currentMaxTokens < ESCALATED_MAX_TOKENS) {
            // Response was truncated — retry with higher limit
            _currentMaxTokens = ESCALATED_MAX_TOKENS;
            adjusted = withMaxTokens(request, _currentMaxTokens);
            response = _delegate.chat(adjusted);
        }

        return response;
    }
}
```

---

## 6. Tool Result Size Management

### Claude Code: Disk Persistence + Budget Tracking

Large tool results are persisted to disk, not kept in memory:

```typescript
// diskOutput.ts
const MAX_TASK_OUTPUT_BYTES = 5 * 1024 * 1024 * 1024  // 5GB cap

append(content: string): void {
  if (this.#capped) return  // Silently drop after cap
  this.#bytesWritten += content.length
  if (this.#bytesWritten > MAX_TASK_OUTPUT_BYTES) {
    this.#capped = true
    this.#queue.push('\n[output truncated: exceeded 5GB disk cap]\n')
  } else {
    this.#queue.push(content)
  }
}
```

Micro-compact deletes old tool results from the cached prompt via API-level edits:

```typescript
// microCompact.ts - Delete tool results without breaking cache
export type PendingCacheEdits = {
  trigger: 'auto'
  deletedToolIds: string[]
  baselineCacheDeletedTokens: number
}
```

Clearable tools (whose results can be safely deleted):
- Shell commands, Glob, Grep, FileRead, WebFetch, WebSearch (results)
- FileEdit, FileWrite, NotebookEdit (invocations)

### tl-ai: Simple Truncation

```java
// ToolCallTracker.java - All results stored in memory
private final Map<String, Object> _toolResults = new HashMap<>();
private final Map<String, String> _rawResultJsons = new HashMap<>();

// TimelineAgentListener.java - Truncation at recording time
private static final int RESULT_SUMMARY_MAX_LEN = 100_000;

static String truncate(String s) {
    if (s == null) return "";
    if (s.length() <= RESULT_SUMMARY_MAX_LEN) return s;
    return s.substring(0, RESULT_SUMMARY_MAX_LEN) + "...[truncated]";
}
```

### What To Build

**Phase 1: Aggregate token budget tracking**

```java
public class ToolResultBudget {

    /** Maximum cumulative tokens from tool results in context. */
    private static final int MAX_TOOL_RESULT_TOKENS = 50_000;

    private int _totalTokens = 0;
    private final List<String> _resultRefs = new ArrayList<>();  // Ordered by age

    public void recordResult(String ref, String resultJson) {
        int tokens = resultJson.length() / 4;  // Rough estimate
        _totalTokens += tokens;
        _resultRefs.add(ref);

        // If over budget, summarize oldest results
        while (_totalTokens > MAX_TOOL_RESULT_TOKENS && _resultRefs.size() > 1) {
            String oldest = _resultRefs.remove(0);
            // Replace with summary in the tracker
            summarizeOldResult(oldest);
        }
    }
}
```

**Phase 2: Selective result summarization** (uses cheap LLM to compress verbose tool outputs like model-browser results into key facts)

---

## 7. Prompt Caching

### Claude Code: Sophisticated Cache Management

```typescript
// claude.ts - Cache control configuration
export function getCacheControl({ scope, querySource }) {
  return {
    type: 'ephemeral',
    ...(should1hCacheTTL(querySource) && { ttl: '1h' }),
    ...(scope === 'global' && { scope }),
  }
}

// promptCacheBreakDetection.ts - Break detection
const CACHE_TTL_5MIN_MS = 5 * 60 * 1000
const CACHE_TTL_1HOUR_MS = 60 * 60 * 1000
const MIN_CACHE_MISS_TOKENS = 2_000

// Detect break: cache read dropped >5% AND absolute drop > 2k tokens
const tokenDrop = prevCacheRead - cacheReadTokens
if (cacheReadTokens >= prevCacheRead * 0.95 || tokenDrop < MIN_CACHE_MISS_TOKENS) {
  return  // Not a break
}
```

System prompt sections are memoized and cached:

```typescript
// systemPromptSections.ts
export function systemPromptSection(name, compute) {
  return { name, compute, cacheBreak: false }
}

// Volatile sections explicitly marked (breaks cache every turn):
export function DANGEROUS_uncachedSystemPromptSection(name, compute, reason) {
  return { name, compute, cacheBreak: true }
}
```

### tl-ai: No Caching

System prompt is rebuilt and sent in full on every LLM call. No `cache_control` parameter.

### What To Build

For Anthropic models, add `cache_control` at the provider level:

```java
// AnthropicChatModelFactory.java or a custom interceptor
// When building the Anthropic request, add cache_control to the system message:
//
// {
//   "role": "system",
//   "content": [
//     {
//       "type": "text",
//       "text": "...",
//       "cache_control": { "type": "ephemeral" }
//     }
//   ]
// }
//
// This tells Anthropic to cache the system prompt for 5 minutes (default)
// or 1 hour (with ttl: "1h").
// Saves significant cost on repeated agent calls with same system prompt.
```

This requires either:
1. A LangChain4j extension that supports `cache_control` in the Anthropic provider
2. A raw HTTP interceptor that adds the parameter to outgoing requests

---

## 8. Permission & Safety Model

### Claude Code: Layered Permission System

```typescript
// permissions.ts - Permission modes
type PermissionMode = 'default' | 'plan' | 'acceptEdits' | 'bypassPermissions' | 'auto'

// Tools declare safety characteristics:
interface Tool {
  isReadOnly(input): boolean       // Can never cause side effects
  isDestructive(input): boolean    // Could cause irreversible damage
  isConcurrencySafe(input): boolean // Safe to run in parallel
  checkPermissions(input, context): PermissionResult  // Tool-specific logic
}

// Rule matching supports tool-level and content-level patterns:
// "Bash(git *)"       → auto-allow git commands
// "FileEdit(/src/*)"  → auto-allow edits in /src/
// "mcp__server1"      → auto-allow all tools from MCP server

// Decision flow:
// 1. Check deny rules → DENY
// 2. Check ask rules → ASK
// 3. Tool.checkPermissions() → tool-specific
// 4. Check mode (bypass/plan/acceptEdits) → ALLOW
// 5. Check allow rules → ALLOW
// 6. Default → ASK
```

### tl-ai: Basic HITL

```java
// UserInteractionStore for human-in-the-loop approval
// No tool-level safety classification
// No pattern-based auto-approve rules
// No destructive/read-only metadata on tools
```

### What To Build

Add metadata to `ToolSpecification` or a parallel registry:

```java
public enum ToolSafety {
    READ_ONLY,       // model-browser.*, model-search.*, help.*
    SIDE_EFFECT,     // script-exec.*, task.*
    DESTRUCTIVE      // model-modification.deleteType, etc.
}

public class ToolSafetyRegistry {

    private static final Map<String, ToolSafety> SAFETY_MAP = Map.of(
        "model-browser.", ToolSafety.READ_ONLY,
        "model-search.", ToolSafety.READ_ONLY,
        "model-modification.", ToolSafety.SIDE_EFFECT,
        "script-exec.", ToolSafety.SIDE_EFFECT
    );

    public static ToolSafety getSafety(String toolName) {
        return SAFETY_MAP.entrySet().stream()
            .filter(e -> toolName.startsWith(e.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(ToolSafety.SIDE_EFFECT);
    }
}
```

Then use in pre-turn safety agents or the entry-point agent's system prompt to gate destructive operations.

---

## 9. Error Handling & Retry Logic

### Claude Code: Comprehensive Retry with Fallback

```typescript
// withRetry.ts - Retry classification
// Retryable: 429 (rate limit), 529 (overloaded), 500-599 (server)
// Non-retryable: 4xx (except 429), validation errors

// Key features:
// 1. Consecutive 529 tracking → after 3, switch to fallback model
if (consecutive529Errors >= MAX_529_RETRIES && fallbackModel) {
  throw new FallbackTriggeredError()  // Caught by query loop to switch models
}

// 2. Fast mode handling
// Short retry-after (< threshold) → wait & retry with fast mode
// Long retry-after → switch to standard speed, continue

// 3. Persistent mode (unattended)
// Retries 429/529 indefinitely with up to 5min backoff

// 4. Stale connection detection
// ECONNRESET/EPIPE → disable keep-alive, get fresh client
```

### tl-ai: Basic Error Propagation

```java
// ChatProcessor.executeOrchestrator()
try {
    String result = ThreadContextManager.inSystemInteraction(ChatProcessor.class,
        () -> (String) agent.invoke(Map.of("message", trimmed)));
} catch (Exception ex) {
    taskTracker.failTask(taskId, formatErrorMessage(ex));
    throw ex;
}
// No retry, no fallback model, no rate-limit handling
```

### What To Build: `RetryingChatModel` Wrapper

```java
public class RetryingChatModel implements ChatModel {

    private static final int MAX_RETRIES = 3;
    private static final int[] BACKOFF_MS = {1000, 3000, 10000};

    private final ChatModel _primary;
    private final ChatModel _fallback;  // Cheaper/faster model for overload situations
    private int _consecutive529 = 0;

    @Override
    public ChatResponse chat(ChatRequest request) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                ChatResponse response = _primary.chat(request);
                _consecutive529 = 0;
                return response;
            } catch (HttpException e) {
                if (e.statusCode() == 429 || e.statusCode() == 529) {
                    if (e.statusCode() == 529) {
                        _consecutive529++;
                    }
                    if (_consecutive529 >= 3 && _fallback != null) {
                        Logger.warn("3 consecutive 529s, switching to fallback model",
                            RetryingChatModel.class);
                        return _fallback.chat(request);
                    }
                    if (attempt < MAX_RETRIES) {
                        sleep(BACKOFF_MS[attempt]);
                        continue;
                    }
                }
                throw e;  // Non-retryable
            }
        }
        throw new RuntimeException("Max retries exceeded");
    }
}
```

---

## 10. Agent Spawning & Context Forking

### Claude Code: Rich Subagent Isolation

```typescript
// forkSubagent.ts - Cache-parity fork
// All forks share identical prefix (for prompt caching):
const toolResultBlocks = toolUseBlocks.map(block => ({
  type: 'tool_result',
  tool_use_id: block.id,
  content: [{ type: 'text', text: 'Fork started — processing in background' }],
  // IDENTICAL across all forks → cache hit
}))

// Only the directive varies per fork:
const toolResultMessage = createUserMessage({
  content: [
    ...toolResultBlocks,  // Shared prefix
    { type: 'text', text: buildChildMessage(directive) },  // Per-fork suffix
  ],
})

// runAgent.ts - Rich isolation features:
// - Permission mode override per agent
// - Agent-specific MCP servers (additive, cleaned up on exit)
// - Tool scoping via allowedTools
// - Async mode (independent abort controller)
// - Transcript grouping per agent
// - Cleanup: kill shell tasks, clear hooks, clear MCP servers
```

Recursive fork guard:

```typescript
export function isInForkChild(messages: MessageType[]): boolean {
  return messages.some(m =>
    m.message.content.some(block =>
      block.type === 'text' && block.text.includes('<fork_boilerplate>')
    )
  )
}
```

### tl-ai: Two Spawning Patterns

**1. Delegation (persistent memory):**

```java
// AgentRegistry.createDelegationExecutor()
ChatMemory agentMemory = subAgentMemories.computeIfAbsent(targetName,
    k -> MessageWindowChatMemory.withMaxMessages(SUB_AGENT_MEMORY_MAX_MESSAGES));
// Session-scoped: retains context across multiple delegations
```

**2. SubagentTool (ephemeral):**

```java
// SubagentTool callback in AgentRegistry
ChatMemory subMemory = MessageWindowChatMemory.withMaxMessages(30);
// Fresh memory every time — no persistence
// Tool scoping via ToolScope.resolveAll()
// Model override via config or parameter
```

### What To Adopt

tl-ai's spawning is functional but missing:

1. **Cleanup on failure**: Claude Code's `finally` block kills shell tasks, clears hooks, and cleans up MCP servers. tl-ai's subagent failures just propagate — no cleanup of partially-created resources.

2. **Context sharing for cache efficiency**: Claude Code's fork creates identical message prefixes so all forks share prompt cache. tl-ai rebuilds system prompts from scratch for each subagent.

3. **Recursion guard**: Claude Code prevents infinite fork recursion via `isInForkChild()`. tl-ai already removes the subagent section from the system prompt (`removeSubagentSection()`), which is a similar guard.

---

## 11. System Prompt Construction

### Claude Code: Memoized Sections with Cache Control

```typescript
// systemPromptSections.ts
// Sections are computed once and cached until /clear or /compact
export function systemPromptSection(name, compute) {
  return { name, compute, cacheBreak: false }
}

// Assembly in systemPrompt.ts:
// 1. Base prompt OR custom prompt OR agent prompt
// 2. Tool descriptions (dynamic)
// 3. MCP instructions
// 4. Memory content
// 5. System context (git status, file counts)
// 6. User-appended instructions

// Volatile sections explicitly marked:
export function DANGEROUS_uncachedSystemPromptSection(name, compute, reason) {
  return { name, compute, cacheBreak: true }
}
```

### tl-ai: Placeholder-Based Assembly

```java
// AgentRegistry.getSystemPrompt()
String prompt = config.getSystemPrompt();
prompt = replacePlaceholderOrAppend(prompt, "<<AVAILABLE_SKILLS>>", skillSection);
prompt = replacePlaceholderOrAppend(prompt, "<<AVAILABLE_TOOLS>>", toolSection);
prompt = replacePlaceholderOrAppend(prompt, "<<AVAILABLE_DELEGATES>>", delegationSection);
```

### What To Adopt

tl-ai's placeholder approach is clean and works well. The main improvement would be **caching the assembled prompt** per agent config hash to avoid rebuilding on every invocation within the same session:

```java
private final Map<String, String> _systemPromptCache = new ConcurrentHashMap<>();

public String getSystemPrompt(AgentConfig config) {
    String cacheKey = config.getName() + ":" + config.hashCode();
    return _systemPromptCache.computeIfAbsent(cacheKey, k -> buildSystemPrompt(config));
}

// Invalidate on config change:
public void invalidatePromptCache() {
    _systemPromptCache.clear();
}
```

---

## 12. Recommendations & Priority

### Tier 1: High Impact, Achievable Now

| # | Feature | Effort | Impact | How |
|---|---------|--------|--------|-----|
| 1 | **Streaming token deltas** | Medium | Huge UX | Use `StreamingChatLanguageModel`, add `token_delta` SSE event |
| 2 | **Summarizing compaction** | Medium | Long conversations | `SummarizingChatMemory` wrapper (code above) |
| 3 | **Retry with backoff** | Low | Reliability | `RetryingChatModel` wrapper (code above) |
| 4 | **Max token escalation** | Low | No truncation | `EscalatingMaxTokensChatModel` wrapper (code above) |

### Tier 2: Medium Impact, Medium Effort

| # | Feature | Effort | Impact | How |
|---|---------|--------|--------|-----|
| 5 | **Concurrent tool execution** | Medium-High | 2-5x faster | Custom tool orchestration or LangChain4j extension |
| 6 | **Prompt caching (Anthropic)** | Low-Medium | Cost savings | Provider-level `cache_control` parameter |
| 7 | **Tool result budget** | Medium | Context efficiency | Aggregate token tracking + old result summarization |
| 8 | **Tool safety classification** | Low | Safety | `ToolSafetyRegistry` with prefix-based classification |

### Tier 3: Future / Architectural

| # | Feature | Effort | Impact | How |
|---|---------|--------|--------|-----|
| 9 | **Custom agent loop** | High | Full control | Replace `AiService` proxy with custom loop (like Claude Code's `query()`) |
| 10 | **Agent resume** | High | Resilience | Serialize tracker + memory state, reconstruct on resume |
| 11 | **Model capability routing** | Low | Cleaner config | `ModelCapabilities` abstraction per provider |
| 12 | **Micro-compact (API cache edits)** | Medium | Cost savings | Requires Anthropic API support for cache_edits |

### The Big Question: Custom Agent Loop

The most impactful long-term decision is whether to replace LangChain4j's `AiService` proxy with a custom agent loop. Claude Code's `query()` generator is ~1400 lines but enables:
- Token-level streaming
- Mid-loop compaction
- Concurrent tool execution
- Max token escalation
- Reactive error recovery
- Token budget enforcement

All the Tier 1-2 items above are workarounds for not having loop control. A custom loop would make them all natural.

**Recommendation**: Start with the wrapper-based approach (Tiers 1-2) to get immediate value. If you find yourself fighting LangChain4j's abstractions repeatedly, invest in a custom loop for the Anthropic provider (keep LangChain4j for OpenAI/Mistral where the advanced features matter less).

---

*Report generated from deep analysis of Claude Code source at `/git/sota-agentic/claude-code-main` and tl-ai source at `/git/tl-ai`.*
