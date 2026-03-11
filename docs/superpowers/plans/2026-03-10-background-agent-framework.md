# Background Agent Framework Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generalize the hardcoded ReviewAgent into a declarative, pluggable background agent framework that supports post-turn, pre-turn, scheduled, and on-demand agents.

**Architecture:** Unified pipeline (Input → Agent → Output) integrated into the existing AgentRegistry. Background agents are regular agents with an additional `<background>` config block. Chat-scoped triggers are handled by ChatProcessor; scheduled triggers bridge to TopLogic's Scheduler via `BackgroundAgentTask extends StateHandlingTask`.

**Tech Stack:** Java 21, TopLogic 7.10.0-SNAPSHOT, LangChain4j, JUnit (TestCase), TopLogic Scheduler

**Spec:** `docs/superpowers/specs/2026-03-10-background-agent-framework-design.md`

---

## File Structure

### New files in `tl-ai-service`

| File | Responsibility |
|------|---------------|
| `agents/background/BackgroundConfig.java` | ConfigurationItem: scope, authority, trigger, input, output-handlers |
| `agents/background/AgentResult.java` | Pipeline result record: status, message, reason, details |
| `agents/background/ResultParser.java` | Parses LLM free-text into AgentResult (authoritative vs advisory rules) |
| `agents/background/BackgroundAgentExecutor.java` | Central pipeline: resolve config → gather input → invoke agent → parse result → run handlers |
| `agents/background/trigger/TriggerConfig.java` | Base config interface for trigger types |
| `agents/background/trigger/PostTurnTrigger.java` | Config for post-turn firing |
| `agents/background/trigger/PreTurnTrigger.java` | Config for pre-turn (timeout, fail-policy) |
| `agents/background/trigger/ScheduledTrigger.java` | Config wrapping TopLogic SchedulingAlgorithm |
| `agents/background/trigger/OnDemandTrigger.java` | Config for REST API trigger (allowed-roles) |
| `agents/background/input/InputConfig.java` | Base config interface for input types (marker) |
| `agents/background/input/ChatContextInput.java` | Config: content enum (LAST_TURN, FULL_CHAT) |
| `agents/background/input/ScriptInput.java` | Config: TLScript expression string |
| `agents/background/input/AutonomousInput.java` | Config: trigger-message string |
| `agents/background/input/ProviderInput.java` | Config: PolymorphicConfiguration<InputProvider> |
| `agents/background/input/InputAdapter.java` | Interface: `gatherInput(BackgroundContext) → String` |
| `agents/background/input/ScriptInputAdapter.java` | Evaluates TLScript from config |
| `agents/background/input/AutonomousInputAdapter.java` | Returns trigger-message string |
| `agents/background/input/ProviderInputAdapter.java` | Delegates to Java InputProvider |
| `agents/background/input/InputProvider.java` | Interface for custom Java input providers |
| `agents/background/output/OutputHandler.java` | Base config interface for output handlers |
| `agents/background/output/AuditLogHandler.java` | Writes to system log via Logger |
| `agents/background/output/SchedulerLogHandler.java` | Writes to Scheduler TaskLog |
| `agents/background/context/BackgroundContext.java` | Base context: agent name, trigger type, timestamp |
| `agents/background/context/ChatContext.java` | Extends with chat messages, tool timeline, session |
| `agents/background/context/ApplicationContext.java` | Extends with trigger source |
| `agents/background/scheduler/BackgroundAgentTask.java` | StateHandlingTask bridge to executor |

### New files in `tl-chat`

| File | Responsibility |
|------|---------------|
| `background/ChatContextAdapter.java` | InputAdapter using ConversationSerializer |
| `background/NotificationOutputHandler.java` | OutputHandler pushing to notification stores |
| `background/BackgroundNotificationStore.java` | Generalized from ReviewNotificationStore |
| `background/GlobalNotificationStore.java` | Singleton for application-scoped notifications |
| `background/BackgroundAgentNotification.java` | Generalized from ReviewNotification |
| `api/messages/BackgroundAlertEvent.java` | Generalized from ReviewAlertEvent |

### New test files

| File | Responsibility |
|------|---------------|
| `tl-ai-service: agents/background/TestResultParser.java` | Unit tests for result parsing rules |
| `tl-ai-service: agents/background/TestBackgroundAgentExecutor.java` | Pipeline integration tests |

### Modified files

| File | Changes |
|------|---------|
| `tl-ai-service: agents/AgentConfig.java` | Add `BACKGROUND` constant + `getBackground()` getter |
| `tl-ai-service: agents/AgentRegistry.java` | Add `getBackgroundAgents()` query, validate background configs at startup |
| `tl-chat: layout/ChatProcessor.java` | Replace `triggerReviewAgent()` with generic post-turn/pre-turn loops |
| `tl-chat: api/ChatServlet.java` | Replace `handleReview()` with `handleBackgroundTrigger()`, add `/background/` routing |
| `tl-chat: api/ChatSSEHandler.java` | Replace `review_alert` with `background_alert`, drain both stores |
| `tl-chat: layout/TaskProgressTracker.java` | Replace single AtomicBoolean with per-agent Map, replace ReviewNotificationStore |
| `tl-chat: webapp/script/chat-client.js` | Replace `review_alert` listener with `background_alert` |
| `tl-ai-service: autoconf/AgentRegistry.config.xml` (demo) | Add `<background>` block to ReviewAgent |

### Deleted files

| File | Replacement |
|------|-------------|
| `tl-chat: layout/ReviewNotification.java` | `BackgroundAgentNotification` |
| `tl-chat: layout/ReviewNotificationStore.java` | `BackgroundNotificationStore` |
| `tl-chat: api/messages/ReviewAlertEvent.java` | `BackgroundAlertEvent` |

---

## Task Dependencies

```
Task 1 (configs) ──→ Task 2 (ResultParser) ──→ Task 4 (executor)
Task 1 (configs) ──→ Task 3 (context) ──────→ Task 4 (executor)
Task 5 (AgentConfig/Registry) ──────────────→ Task 4 (executor)
Task 4 (executor) ──→ Task 6 (scheduler bridge)
Task 7 (notifications) ──→ Task 8 (adapters) ──→ Task 9 (TaskProgressTracker)
Task 9 ──→ Task 10 (ChatProcessor) ──→ Task 11 (SSEHandler/Servlet)
Task 11 ──→ Task 12 (JS) ──→ Task 13 (cleanup/config) ──→ Task 14 (verify)
```

Tasks 1, 2, 3, 5 can run in parallel. Task 4 depends on all four. Tasks 7, 8 can run in parallel with chunk 1.

---

## Chunk 1: Core Framework (tl-ai-service)

### Task 1: Config Interfaces — Enums, TriggerConfig, InputConfig, OutputHandler

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/BackgroundConfig.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/trigger/TriggerConfig.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/trigger/PostTurnTrigger.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/trigger/PreTurnTrigger.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/trigger/ScheduledTrigger.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/trigger/OnDemandTrigger.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/input/InputAdapter.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/input/InputProvider.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/input/ScriptInputAdapter.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/input/AutonomousInputAdapter.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/input/ProviderInputAdapter.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/output/OutputHandler.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/output/AuditLogHandler.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/output/SchedulerLogHandler.java`

All paths below are relative to `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/`.

- [ ] **Step 1: Create `BackgroundConfig.java`**

The root config interface. Contains two enums (`Scope`, `Authority`) and references to polymorphic sub-configs for trigger, input, and output-handlers.

```java
// Package: com.top_logic.ai.service.agents.background
// Extends: ConfigurationItem
// Enums: Scope (CHAT, APPLICATION), Authority (ADVISORY, AUTHORITATIVE)
// Properties:
//   @Name("scope") Scope getScope()                                       default CHAT
//   @Name("authority") Authority getAuthority()                           default ADVISORY
//   @Name("notify-on-block") boolean getNotifyOnBlock()                   default true
//   @Name("block-message") String getBlockMessage()                       default ""
//   @Name("trigger") PolymorphicConfiguration<TriggerConfig> getTrigger() mandatory
//   @Name("input") PolymorphicConfiguration<InputConfig> getInput()       mandatory (use interface marker)
//   @Name("output-handlers") List<PolymorphicConfiguration<OutputHandler>> getOutputHandlers()
```

Use TopLogic `@Name`, `@Mandatory`, `@BooleanDefault`, `@StringDefault` annotations. Follow patterns from `AgentConfig.java` (lines 62-180) for constant naming: `String SCOPE = "scope"` with `/** @see #getScope() */`.

- [ ] **Step 2: Create `TriggerConfig.java` and its four implementations**

`TriggerConfig` is a marker interface extending `ConfigurationItem`. Four implementations:

- `PostTurnTrigger extends TriggerConfig` — no additional properties
- `PreTurnTrigger extends TriggerConfig` — `@Name("timeout") @LongDefault(30000) long getTimeout()`, `@Name("fail-policy") FailPolicy getFailPolicy()` with enum `FailPolicy { OPEN, CLOSED }` defaulting to `OPEN`
- `ScheduledTrigger extends TriggerConfig` — `@Name("schedules") List<PolymorphicConfiguration<SchedulingAlgorithm>> getSchedules()`. Import `com.top_logic.util.sched.task.schedule.SchedulingAlgorithm` from tl-engine-7.
- `OnDemandTrigger extends TriggerConfig` — `@Name("allowed-roles") @Format(CommaSeparatedStrings.class) List<String> getAllowedRoles()`

**Note:** For `ScheduledTrigger`, ensure the `tl-ai-service/pom.xml` has `com.top_logic` as a dependency (it already depends on `tl-core` which includes the scheduler). If compilation fails on `SchedulingAlgorithm`, check `com.top_logic.util.sched.task.schedule.SchedulingAlgorithm` import path.

- [ ] **Step 3: Create `InputAdapter.java` interface and `InputProvider.java` interface**

```java
// InputAdapter — functional interface
// Package: com.top_logic.ai.service.agents.background.input
public interface InputAdapter {
    String gatherInput(BackgroundContext context);
}

// InputProvider — extension point for custom Java providers
// Package: com.top_logic.ai.service.agents.background.input
public interface InputProvider extends ConfiguredInstance<InputProvider.Config> {
    interface Config extends PolymorphicConfiguration<InputProvider> {}
    String provide(BackgroundContext context);
}
```

Also create the polymorphic `InputConfig` marker interface extending `ConfigurationItem`, with sub-types:
- `ChatContextInput extends InputConfig` — `@Name("content") Content getContent()` with enum `Content { LAST_TURN, FULL_CHAT }`
- `ScriptInput extends InputConfig` — `@Name("script") @Mandatory String getScript()`
- `AutonomousInput extends InputConfig` — `@Name("trigger-message") @Mandatory String getTriggerMessage()`
- `ProviderInput extends InputConfig` — `@Name("provider") @Mandatory PolymorphicConfiguration<InputProvider> getProvider()`

**Note:** `ChatContextInput` is defined in `tl-ai-service` as a config interface only. The adapter that interprets it (`ChatContextAdapter`) lives in `tl-chat` to avoid circular dependencies.

- [ ] **Step 4: Create `ScriptInputAdapter.java`, `AutonomousInputAdapter.java`, `ProviderInputAdapter.java`**

```java
// ScriptInputAdapter
public class ScriptInputAdapter implements InputAdapter {
    private final String _script;
    public ScriptInputAdapter(ScriptInput config) { _script = config.getScript(); }
    @Override
    public String gatherInput(BackgroundContext context) {
        // Use TLScriptEngine to evaluate _script
        // See com.top_logic.model.search.expr for TLScript evaluation patterns
        // Wrap in ThreadContextManager.inSystemInteraction() if needed
        return TLScriptEngine.evaluate(_script);
    }
}

// AutonomousInputAdapter — trivial
public class AutonomousInputAdapter implements InputAdapter {
    private final String _message;
    public AutonomousInputAdapter(AutonomousInput config) { _message = config.getTriggerMessage(); }
    @Override
    public String gatherInput(BackgroundContext context) { return _message; }
}

// ProviderInputAdapter — delegates to configured InputProvider
public class ProviderInputAdapter implements InputAdapter {
    private final InputProvider _provider;
    public ProviderInputAdapter(ProviderInput config) {
        _provider = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
            .getInstance(config.getProvider());
    }
    @Override
    public String gatherInput(BackgroundContext context) { return _provider.provide(context); }
}
```

**Note for ScriptInputAdapter:** Look at how `tl-ai-mcp-server` evaluates TLScript (search for `executeTLScript` in the MCP server module) to find the correct evaluation API. The script evaluation must run within a TopLogic `ThreadContext`.

- [ ] **Step 5: Create `OutputHandler.java` base interface, `AuditLogHandler.java`, `SchedulerLogHandler.java`**

`OutputHandler` is a marker interface extending `ConfigurationItem`. Each implementation is a config interface with a static `handle()` utility or an instantiated handler.

Given the spec's `instanceof` dispatching pattern, define output handlers as config interfaces. The `BackgroundAgentExecutor` will dispatch on their type:

```java
// OutputHandler — marker interface
public interface OutputHandler extends ConfigurationItem {}

// AuditLogHandler — no config properties, just presence triggers audit logging
public interface AuditLogHandler extends OutputHandler {}

// SchedulerLogHandler — no config properties, auto-added for scheduled agents
public interface SchedulerLogHandler extends OutputHandler {}
```

`NotificationHandler` is defined in `tl-chat` (Task 5) since it depends on notification stores.

- [ ] **Step 6: Compile check**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS (no errors in new config interfaces)

- [ ] **Step 7: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/
git commit -m "feat: add background agent config interfaces and input/output framework"
```

---

### Task 2: AgentResult and ResultParser with Tests

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/AgentResult.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/ResultParser.java`
- Create: `tl-ai-service/src/test/java/com/top_logic/ai/service/agents/background/TestResultParser.java`

- [ ] **Step 1: Write failing tests for ResultParser**

Create `TestResultParser.java` extending `junit.framework.TestCase`:

```java
// Test authoritative parsing:
// testAuthoritativePass() — "PASS" → status=PASS
// testAuthoritativePassCaseInsensitive() — "pass" → status=PASS
// testAuthoritativeBlock() — "BLOCK: contains PII" → status=BLOCK, reason="contains PII"
// testAuthoritativeBlockTrimmed() — "BLOCK:   spaces  " → reason="spaces"
// testAuthoritativeUnrecognizedFailOpen() — "random text" with failPolicy=OPEN → status=PASS
// testAuthoritativeUnrecognizedFailClosed() — "random text" with failPolicy=CLOSED → status=BLOCK
// testAuthoritativeNullResponse() — null → PASS (fail-open) or BLOCK (fail-closed)

// Test advisory parsing:
// testAdvisoryNoIssues() — "No issues found." → status=PASS
// testAdvisoryNoIssuesCaseInsensitive() — "NO ISSUES FOUND" → status=PASS
// testAdvisoryEmpty() — "" → status=PASS
// testAdvisoryNull() — null → status=PASS
// testAdvisoryIssueFound() — "Found 3 violations" → status=ISSUE_FOUND, message="Found 3 violations"
// testAdvisoryCannotBlock() — advisory never produces BLOCK status
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -pl tl-ai-service -Dtest=TestResultParser -q`
Expected: Compilation errors (classes don't exist yet)

- [ ] **Step 3: Create `AgentResult.java`**

```java
// Record with: Status status, String message, String reason, Map<String, Object> details
// Status enum: PASS, ISSUE_FOUND, BLOCK
// Factory methods: AgentResult.pass(), AgentResult.issueFound(message), AgentResult.block(reason, blockMessageTemplate)
// The block() method replaces {reason} in the template
```

- [ ] **Step 4: Create `ResultParser.java`**

```java
// Static methods:
// parseAuthoritative(String response, PreTurnTrigger.FailPolicy failPolicy) → AgentResult
// parseAdvisory(String response) → AgentResult
// Private: extractBlockReason(String response) → String
// Private: resolveBlockMessage(String template, String reason) → String
```

Implement the exact rules from the spec (design doc lines 210-226).

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestResultParser -q`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/AgentResult.java
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/ResultParser.java
git add tl-ai-service/src/test/java/com/top_logic/ai/service/agents/background/TestResultParser.java
git commit -m "feat: add AgentResult and ResultParser with tests"
```

---

### Task 3: BackgroundContext hierarchy

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/context/BackgroundContext.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/context/ChatContext.java`
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/context/ApplicationContext.java`

- [ ] **Step 1: Create `BackgroundContext.java`**

```java
// Base class with: String agentName, String triggerType, Instant timestamp
// Constructor: BackgroundContext(String agentName, String triggerType)
// timestamp set to Instant.now() in constructor
```

- [ ] **Step 2: Create `ChatContext.java`**

Uses `Object` for tl-chat types to avoid circular dependency. `ChatContextAdapter` in `tl-chat` casts these.

```java
// Extends BackgroundContext
// Fields (all stored as Object to avoid tl-chat dependency):
//   private final String _userMessage;
//   private final String _agentResponse;
//   private final Object _completedTask;  // ChatTask from tl-chat, cast by ChatContextAdapter
//   private final Object _chat;           // Chat from tl-chat, cast by ChatContextAdapter
//
// Factory methods with explicit signatures:
//   public static ChatContext lastTurn(String agentName, String userMessage,
//       String agentResponse, Object completedTask)
//     → sets triggerType="post-turn", _chat=null
//
//   public static ChatContext fullChat(String agentName, Object chat)
//     → sets triggerType="pre-turn", _userMessage=null, _agentResponse=null, _completedTask=null
//
// Getters: getUserMessage(), getAgentResponse(), getCompletedTask(), getChat()
```

- [ ] **Step 3: Create `ApplicationContext.java`**

```java
// Extends BackgroundContext
// Additional fields: String triggerSource ("scheduled", "manual", etc.)
// Factory methods:
//   ApplicationContext.scheduled(agentName)
//   ApplicationContext.manual(agentName, String userName)
```

- [ ] **Step 4: Compile check**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/context/
git commit -m "feat: add BackgroundContext hierarchy"
```

---

### Task 4: BackgroundAgentExecutor — the pipeline

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/BackgroundAgentExecutor.java`
- Create: `tl-ai-service/src/test/java/com/top_logic/ai/service/agents/background/TestBackgroundAgentExecutor.java`

- [ ] **Step 1: Write test for executor pipeline**

In `TestBackgroundAgentExecutor.java`, test the `resolveInputAdapter` dispatching:

```java
// testResolveAutonomousInput() — AutonomousInput config → AutonomousInputAdapter
// testResolveScriptInput() — ScriptInput config → ScriptInputAdapter
// testResolveProviderInput() — ProviderInput config → ProviderInputAdapter
// testResolveChatContextInput() — ChatContextInput config → throws (no adapter in tl-ai-service)
```

**Note:** The ChatContextAdapter lives in `tl-chat`, so `BackgroundAgentExecutor` must support registering external adapters. Add a `registerInputAdapter(Class<? extends InputConfig>, Function<InputConfig, InputAdapter>)` method, or use a service-loader/callback pattern that `tl-chat` hooks into at startup.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -pl tl-ai-service -Dtest=TestBackgroundAgentExecutor -q`
Expected: Compilation failure

- [ ] **Step 3: Implement `BackgroundAgentExecutor.java`**

```java
public class BackgroundAgentExecutor {
    private final AgentRegistry _registry;

    public BackgroundAgentExecutor(AgentRegistry registry) { _registry = registry; }

    // Main synchronous pipeline
    public AgentResult execute(String agentName, BackgroundContext context) {
        AgentConfig agentConfig = _registry.getAgentConfig(agentName);
        BackgroundConfig bgConfig = agentConfig.getBackground();
        if (bgConfig == null) {
            throw new IllegalArgumentException("Agent '" + agentName + "' has no background config");
        }

        // 1. Resolve input
        InputAdapter adapter = resolveInputAdapter(bgConfig.getInput());
        String input = adapter.gatherInput(context);

        // 2. Build and invoke agent
        UntypedAgent agent = _registry.buildUntypedAgent(agentName, null, null, new HashMap<>());
        String response = ThreadContextManager.inSystemInteraction(
            BackgroundAgentExecutor.class,
            () -> (String) agent.invoke(Map.of("message", input)));

        // 3. Parse result
        AgentResult result;
        if (bgConfig.getAuthority() == BackgroundConfig.Authority.AUTHORITATIVE) {
            PreTurnTrigger.FailPolicy policy = PreTurnTrigger.FailPolicy.OPEN;
            if (bgConfig.getTrigger() instanceof PreTurnTrigger pt) {
                policy = pt.getFailPolicy();
            }
            result = ResultParser.parseAuthoritative(response, policy);
            if (result.status() == AgentResult.Status.BLOCK && bgConfig.getBlockMessage() != null) {
                result = result.withBlockMessage(
                    bgConfig.getBlockMessage().replace("{reason}", result.reason()));
            }
        } else {
            result = ResultParser.parseAdvisory(response);
        }

        // 4. Run output handlers
        runOutputHandlers(bgConfig, result, context);

        return result;
    }

    // Async wrapper for post-turn / on-demand
    public void executeAsync(String agentName, BackgroundContext context, Runnable onComplete) {
        // Submit to background executor, catch exceptions, call onComplete in finally
    }

    // Input adapter resolution via instanceof dispatch + external registry
    // External modules (tl-chat) register adapters for types they own:
    private static final Map<Class<?>, Function<Object, InputAdapter>> _externalAdapters =
        new ConcurrentHashMap<>();

    /**
     * Registers an input adapter factory for a config type defined in another module.
     * Called from tl-chat startup to register ChatContextAdapter without circular dependency.
     */
    public static <T> void registerInputAdapterFactory(
            Class<T> configType, Function<T, InputAdapter> factory) {
        _externalAdapters.put(configType, cfg -> factory.apply(configType.cast(cfg)));
    }

    InputAdapter resolveInputAdapter(InputConfig inputConfig) {
        if (inputConfig instanceof AutonomousInput cfg) {
            return new AutonomousInputAdapter(cfg);
        } else if (inputConfig instanceof ScriptInput cfg) {
            return new ScriptInputAdapter(cfg);
        } else if (inputConfig instanceof ProviderInput cfg) {
            return new ProviderInputAdapter(cfg);
        }
        // Check external adapters (e.g. ChatContextInput → ChatContextAdapter from tl-chat)
        Function<Object, InputAdapter> factory = _externalAdapters.get(inputConfig.getClass());
        if (factory != null) {
            return factory.apply(inputConfig);
        }
        throw new IllegalArgumentException("No adapter for input config: " + inputConfig.getClass());
    }

    // Output handler dispatching via instanceof + external registry (same pattern)
    private static final Map<Class<?>, BiConsumer<Object, OutputHandlerContext>> _externalHandlers =
        new ConcurrentHashMap<>();

    public static <T> void registerOutputHandlerFactory(
            Class<T> handlerType, BiConsumer<T, OutputHandlerContext> handler) {
        _externalHandlers.put(handlerType, (cfg, ctx) -> handler.accept(handlerType.cast(cfg), ctx));
    }

    private void runOutputHandlers(BackgroundConfig bgConfig, AgentResult result,
            BackgroundContext context) {
        for (PolymorphicConfiguration<OutputHandler> handlerCfg : bgConfig.getOutputHandlers()) {
            OutputHandler handler = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
                .getInstance(handlerCfg);
            try {
                if (handler instanceof AuditLogHandler) {
                    Logger.info("Background agent '" + context.getAgentName() + "': " +
                        result.status() + " - " + result.message(), BackgroundAgentExecutor.class);
                } else {
                    // Check external handlers (NotificationOutputHandler from tl-chat)
                    BiConsumer<Object, OutputHandlerContext> ext =
                        _externalHandlers.get(handler.getClass());
                    if (ext != null) {
                        ext.accept(handler, new OutputHandlerContext(result, context));
                    }
                }
            } catch (Exception ex) {
                Logger.error("Output handler failed for agent '" + context.getAgentName() + "'",
                    ex, BackgroundAgentExecutor.class);
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestBackgroundAgentExecutor -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/BackgroundAgentExecutor.java
git add tl-ai-service/src/test/java/com/top_logic/ai/service/agents/background/TestBackgroundAgentExecutor.java
git commit -m "feat: add BackgroundAgentExecutor pipeline"
```

---

### Task 5: Extend AgentConfig and AgentRegistry

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentConfig.java`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentRegistry.java`

- [ ] **Step 1: Add `getBackground()` to AgentConfig**

In `AgentConfig.java`:
1. Add constant after line 179: `String BACKGROUND = "background";` with `/** @see #getBackground() */`
2. Add to `@DisplayOrder` array (line 87, before closing `}`): `AgentConfig.BACKGROUND`
3. Add getter method after the existing getters:

```java
/**
 * Optional background agent configuration.
 *
 * <p>
 * When set, this agent becomes a background agent that is triggered by the framework
 * rather than by user messages. When {@code null}, the agent is a normal conversational
 * agent (unchanged behavior).
 * </p>
 *
 * @see BackgroundConfig
 */
@Name(BACKGROUND)
BackgroundConfig getBackground();
```

- [ ] **Step 2: Add `getBackgroundAgents()` to AgentRegistry**

In `AgentRegistry.java`, add a public method after `getAgentConfig()` (around line 307):

```java
/**
 * Returns the names of all background agents matching the given trigger type and scope.
 */
public List<String> getBackgroundAgents(
        Class<? extends TriggerConfig> triggerType, BackgroundConfig.Scope scope) {
    List<String> result = new ArrayList<>();
    for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
        BackgroundConfig bg = entry.getValue().getBackground();
        if (bg == null) continue;
        if (bg.getScope() != scope) continue;
        PolymorphicConfiguration<TriggerConfig> triggerCfg = bg.getTrigger();
        if (triggerCfg != null && triggerType.isAssignableFrom(triggerCfg.getImplementationClass())) {
            result.add(entry.getKey());
        }
    }
    return result;
}

/**
 * Checks whether the named agent has a background configuration.
 */
public boolean isBackgroundAgent(String agentName) {
    AgentConfig config = getAgentConfig(agentName);
    return config != null && config.getBackground() != null;
}
```

- [ ] **Step 3: Add background agent validation to `startUp()`**

In `AgentRegistry.startUp()` (after the entry-point validation at line 246), add:

```java
// Validate background agent configurations
for (Map.Entry<String, AgentConfig> entry : getConfig().getAgents().entrySet()) {
    BackgroundConfig bg = entry.getValue().getBackground();
    if (bg == null) continue;
    String name = entry.getKey();

    // Background agents must not be entry points
    if (entry.getValue().isEntryPoint()) {
        throw new RuntimeException(
            "Agent '" + name + "' cannot be both entry-point and background agent.");
    }

    // Chat-context input requires chat scope
    if (bg.getInput() != null) {
        Object inputImpl = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
            .getInstance(bg.getInput());
        if (inputImpl instanceof ChatContextInput && bg.getScope() != BackgroundConfig.Scope.CHAT) {
            throw new RuntimeException(
                "Agent '" + name + "': chat-context input requires scope='chat'.");
        }
    }

    Logger.info("Background agent '" + name + "' registered: scope=" + bg.getScope()
        + ", authority=" + bg.getAuthority() + ", trigger=" + bg.getTrigger().getImplementationClass().getSimpleName(),
        AgentRegistry.class);
}
```

- [ ] **Step 4: Compile check**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Run existing tests**

Run: `mvn test -pl tl-ai-service -q`
Expected: All existing tests still pass (no behavior change for agents without `<background>`)

- [ ] **Step 6: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentConfig.java
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentRegistry.java
git commit -m "feat: extend AgentConfig with background property and AgentRegistry with background agent queries"
```

---

### Task 6: BackgroundAgentTask — Scheduler bridge

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/scheduler/BackgroundAgentTask.java`

- [ ] **Step 1: Create `BackgroundAgentTask.java`**

```java
// Extends StateHandlingTask<BackgroundAgentTask.Config>
// Config extends StateHandlingTask.Config<BackgroundAgentTask>
//   @Name("agent-name") @Mandatory String getAgentName()
//
// runHook():
//   1. Get AgentRegistry.getInstance()
//   2. Create BackgroundAgentExecutor
//   3. Create ApplicationContext.scheduled(agentName)
//   4. Call executor.execute(agentName, context)
//   5. Log result to getLog():
//      - PASS → taskEnded(ResultType.SUCCESS, "No issues")
//      - ISSUE_FOUND → taskEnded(ResultType.WARNING, result.message())
//      - BLOCK → taskEnded(ResultType.WARNING, "Blocked: " + result.reason())
//   6. On exception → taskEnded(ResultType.ERROR, ex.getMessage())
```

Import `com.top_logic.util.sched.task.impl.StateHandlingTask` and `com.top_logic.util.sched.task.result.TaskResult.ResultType`.

- [ ] **Step 2: Compile check**

Run: `mvn compile -pl tl-ai-service -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/agents/background/scheduler/BackgroundAgentTask.java
git commit -m "feat: add BackgroundAgentTask as Scheduler bridge"
```

---

## Chunk 2: Chat Integration (tl-chat)

### Task 7: Generalize notification infrastructure

**Files:**
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/background/BackgroundAgentNotification.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/background/BackgroundNotificationStore.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/background/GlobalNotificationStore.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/BackgroundAlertEvent.java`

- [ ] **Step 1: Create `BackgroundAgentNotification.java`**

Generalized from `ReviewNotification`:

```java
// Record: String title, String agentName, String category, String severity, Instant timestamp
// Same structure but with agentName added to identify which background agent produced it
```

- [ ] **Step 2: Create `BackgroundNotificationStore.java`**

Same pattern as `ReviewNotificationStore` but typed to `BackgroundAgentNotification`:

```java
// ConcurrentLinkedQueue<BackgroundAgentNotification>
// add(notification), drain() → List<BackgroundAgentNotification>
// Identical implementation, different type parameter
```

- [ ] **Step 3: Create `GlobalNotificationStore.java`**

Singleton for application-scoped notifications:

```java
// Same internal structure as BackgroundNotificationStore
// Singleton pattern: private static final GlobalNotificationStore INSTANCE = new GlobalNotificationStore();
// public static GlobalNotificationStore getInstance()
```

- [ ] **Step 4: Create `BackgroundAlertEvent.java`**

Generalized from `ReviewAlertEvent`:

```java
// public static String toJson(BackgroundAgentNotification notification)
// Same JSON structure but includes "agentName" field
// Copy escapeJson() from ReviewAlertEvent
```

- [ ] **Step 5: Compile check**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/background/
git add tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/BackgroundAlertEvent.java
git commit -m "feat: add generalized notification infrastructure for background agents"
```

---

### Task 8: ChatContextAdapter and NotificationOutputHandler

**Files:**
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/background/ChatContextAdapter.java`
- Create: `tl-chat/src/main/java/com/top_logic/ai/chat/background/NotificationOutputHandler.java`

- [ ] **Step 1: Create `ChatContextAdapter.java`**

```java
// Implements InputAdapter
// Constructor takes ChatContextInput config (for content mode: LAST_TURN vs FULL_CHAT)
// gatherInput(BackgroundContext context):
//   Cast context to ChatContext
//   If content == LAST_TURN:
//     return ConversationSerializer.serializeLastTurn(ctx.userMessage, ctx.agentResponse, ctx.completedTask)
//   If content == FULL_CHAT:
//     return ConversationSerializer.serializeFullChat(ctx.chat)
```

- [ ] **Step 2: Create `NotificationOutputHandler.java`**

This is the OutputHandler config interface (lives in tl-chat since it depends on notification stores):

```java
// Config interface extending OutputHandler
// @Name("severity") @StringDefault("warning") String getSeverity()
//
// Also provide a static handle() method used by BackgroundAgentExecutor dispatching:
//   handle(AgentResult result, BackgroundContext context, NotificationOutputHandler config):
//     if result.status == PASS → skip (no notification needed)
//     Create BackgroundAgentNotification(summarize(result.message), context.agentName, ...)
//     If context instanceof ChatContext → push to session-scoped BackgroundNotificationStore
//     Else → push to GlobalNotificationStore.getInstance()
```

- [ ] **Step 3: Register adapters with BackgroundAgentExecutor**

In `ChatServlet.init()` (or a static initializer block in `ChatContextAdapter`), register both the input adapter and the notification output handler:

```java
// Register ChatContextAdapter for ChatContextInput configs
BackgroundAgentExecutor.registerInputAdapterFactory(
    ChatContextInput.class, cfg -> new ChatContextAdapter((ChatContextInput) cfg));

// Register NotificationOutputHandler
BackgroundAgentExecutor.registerOutputHandlerFactory(
    NotificationOutputHandler.class,
    (handler, ctx) -> NotificationOutputHandler.handle(handler, ctx.result(), ctx.context()));
```

This bridges the tl-chat implementations into the tl-ai-service executor without circular dependencies. The static `registerInputAdapterFactory` / `registerOutputHandlerFactory` methods on `BackgroundAgentExecutor` use a `ConcurrentHashMap` (defined in Task 4).

- [ ] **Step 4: Compile check**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/background/ChatContextAdapter.java
git add tl-chat/src/main/java/com/top_logic/ai/chat/background/NotificationOutputHandler.java
git commit -m "feat: add ChatContextAdapter and NotificationOutputHandler"
```

---

### Task 9: Migrate TaskProgressTracker

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java`

- [ ] **Step 1: Replace review-specific fields with generalized fields**

In `TaskProgressTracker.java`:

1. Replace line 45 (`ReviewNotificationStore _reviewNotifications`) with:
   ```java
   private final BackgroundNotificationStore _backgroundNotifications = new BackgroundNotificationStore();
   ```

2. Replace line 47 (`AtomicBoolean _reviewInProgress`) with:
   ```java
   private final ConcurrentHashMap<String, AtomicBoolean> _backgroundAgentInProgress = new ConcurrentHashMap<>();
   ```

3. Replace `getReviewNotifications()` (lines 155-157) with:
   ```java
   public BackgroundNotificationStore getBackgroundNotifications() {
       return _backgroundNotifications;
   }
   ```

4. Replace `tryStartReview()` (lines 163-165) with:
   ```java
   public boolean tryStartBackgroundAgent(String agentName) {
       return _backgroundAgentInProgress
           .computeIfAbsent(agentName, k -> new AtomicBoolean(false))
           .compareAndSet(false, true);
   }
   ```

5. Replace `finishReview()` (lines 170-172) with:
   ```java
   public void finishBackgroundAgent(String agentName) {
       AtomicBoolean flag = _backgroundAgentInProgress.get(agentName);
       if (flag != null) {
           flag.set(false);
       }
   }
   ```

- [ ] **Step 2: Update imports**

Remove `ReviewNotificationStore` import, add `BackgroundNotificationStore` and `ConcurrentHashMap` imports.

- [ ] **Step 3: Compile check**

Run: `mvn compile -pl tl-chat -q`
Expected: Compilation errors in ChatProcessor and ChatSSEHandler (they still reference old methods). This is expected — we fix those in the next tasks.

- [ ] **Step 4: Commit (even with downstream errors)**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/TaskProgressTracker.java
git commit -m "refactor: generalize TaskProgressTracker for multiple background agents"
```

---

### Task 10: Migrate ChatProcessor

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ChatProcessor.java`

- [ ] **Step 1: Replace `triggerReviewAgent()` with generic background agent triggers**

1. Delete the `triggerReviewAgent()` method (lines 463-498) and `summarize()` helper (lines 500-510).

2. Replace the call at line 193 (`triggerReviewAgent(taskTracker, trimmed, result, taskTracker.getTask(taskId))`) with:

```java
// Trigger post-turn background agents
triggerPostTurnAgents(taskTracker, trimmed, result, taskTracker.getTask(taskId));
```

3. Add the new generic method:

```java
static void triggerPostTurnAgents(TaskProgressTracker taskTracker, String userMessage,
        String agentResponse, ChatTask completedTask) {
    AgentRegistry agentRegistry = AgentRegistry.getInstance();
    if (agentRegistry == null) return;

    List<String> postTurnAgents = agentRegistry.getBackgroundAgents(
        PostTurnTrigger.class, BackgroundConfig.Scope.CHAT);

    for (String agentName : postTurnAgents) {
        if (!taskTracker.tryStartBackgroundAgent(agentName)) {
            continue; // debounce: already running
        }

        String name = agentName; // effectively final for lambda
        BACKGROUND_EXECUTOR.submit(() -> {
            try {
                BackgroundAgentExecutor executor = new BackgroundAgentExecutor(agentRegistry);
                ChatContext context = ChatContext.lastTurn(name,
                    userMessage, agentResponse, completedTask);
                AgentResult result = executor.execute(name, context);

                if (result.status() != AgentResult.Status.PASS) {
                    BackgroundAgentNotification notification = new BackgroundAgentNotification(
                        summarizeText(result.message(), 200),
                        name,
                        result.status() == AgentResult.Status.BLOCK ? "block" : "review",
                        "warning",
                        Instant.now());
                    taskTracker.getBackgroundNotifications().add(notification);
                }
            } catch (Exception ex) {
                Logger.error("Background agent '" + name + "' failed: " + ex.getMessage(),
                    ex, ChatProcessor.class);
            } finally {
                taskTracker.finishBackgroundAgent(name);
            }
        });
    }
}
```

4. Add pre-turn support. Before the Orchestrator invocation (line 165 area), add:

```java
// Execute pre-turn background agents (synchronous, can block)
List<String> preTurnAgents = agentRegistry.getBackgroundAgents(
    PreTurnTrigger.class, BackgroundConfig.Scope.CHAT);
for (String preTurnAgent : preTurnAgents) {
    try {
        BackgroundAgentExecutor bgExecutor = new BackgroundAgentExecutor(agentRegistry);
        ChatContext bgContext = ChatContext.fullChat(preTurnAgent, taskTracker.getChat());
        AgentResult bgResult = bgExecutor.execute(preTurnAgent, bgContext);
        if (bgResult.status() == AgentResult.Status.BLOCK) {
            state.addPendingMessage(new ChatMessage("Agent", bgResult.message()));
            taskTracker.completeTask(taskId);
            return;
        }
    } catch (Exception ex) {
        Logger.error("Pre-turn agent '" + preTurnAgent + "' failed: " + ex.getMessage(),
            ex, ChatProcessor.class);
        // fail-open: continue to Orchestrator
    }
}
```

5. Keep a `summarizeText(String text, int maxLen)` utility (renamed from `summarize`).

- [ ] **Step 2: Update imports**

Add imports for `BackgroundAgentExecutor`, `BackgroundConfig`, `ChatContext`, `AgentResult`, `BackgroundAgentNotification`, `PostTurnTrigger`, `PreTurnTrigger`. Remove `ReviewNotification` import.

- [ ] **Step 3: Compile check**

Run: `mvn compile -pl tl-chat -q`
Expected: May still fail on ChatSSEHandler — fix in next task.

- [ ] **Step 4: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/layout/ChatProcessor.java
git commit -m "refactor: replace hardcoded ReviewAgent trigger with generic background agent pipeline"
```

---

### Task 11: Migrate ChatSSEHandler and ChatServlet

**Files:**
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatSSEHandler.java`
- Modify: `tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatServlet.java`

- [ ] **Step 1: Update ChatSSEHandler**

Replace lines 335-339 with:

```java
// Emit background agent alerts (session-scoped)
List<BackgroundAgentNotification> sessionAlerts = _tracker.getBackgroundNotifications().drain();
for (BackgroundAgentNotification alert : sessionAlerts) {
    sendEvent("background_alert", BackgroundAlertEvent.toJson(alert));
}

// Emit global background agent alerts (application-scoped)
List<BackgroundAgentNotification> globalAlerts = GlobalNotificationStore.getInstance().drain();
for (BackgroundAgentNotification alert : globalAlerts) {
    sendEvent("background_alert", BackgroundAlertEvent.toJson(alert));
}
```

Update imports: remove `ReviewNotification`, `ReviewAlertEvent`; add `BackgroundAgentNotification`, `BackgroundAlertEvent`, `GlobalNotificationStore`.

- [ ] **Step 2: Update ChatServlet — replace handleReview with handleBackgroundTrigger**

1. Replace `PATH_REVIEW` constant (line 78) with:
   ```java
   private static final String PATH_REVIEW = "/review";
   private static final String PATH_BACKGROUND = "/background";
   ```

2. Update `doPost()` routing (line 100-102). Keep `/review` as backwards-compat alias:
   ```java
   if (pathInfo.equals(PATH_REVIEW) || pathInfo.equals(PATH_REVIEW + "/")) {
       handleBackgroundTrigger(req, resp, "ReviewAgent");
       return;
   }
   if (pathInfo.startsWith(PATH_BACKGROUND + "/")) {
       String agentName = pathInfo.substring(PATH_BACKGROUND.length() + 1);
       if (agentName.endsWith("/")) agentName = agentName.substring(0, agentName.length() - 1);
       handleBackgroundTrigger(req, resp, agentName);
       return;
   }
   ```

3. Replace `handleReview()` method (lines 227-274) with:
   ```java
   private void handleBackgroundTrigger(HttpServletRequest req, HttpServletResponse resp,
           String agentName) throws IOException {
       AgentRegistry registry = AgentRegistry.getInstance();
       if (registry == null || !registry.hasAgent(agentName)) {
           resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Agent not found: " + agentName);
           return;
       }
       if (!registry.isBackgroundAgent(agentName)) {
           resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not a background agent: " + agentName);
           return;
       }

       // Security: check allowed-roles for on-demand triggers
       AgentConfig agentConfig = registry.getAgentConfig(agentName);
       BackgroundConfig bgConfig = agentConfig.getBackground();
       TriggerConfig trigger = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
           .getInstance(bgConfig.getTrigger());
       if (trigger instanceof OnDemandTrigger onDemand) {
           List<String> allowedRoles = onDemand.getAllowedRoles();
           if (allowedRoles != null && !allowedRoles.isEmpty()) {
               Person person = TLContext.currentUser();
               if (person == null) {
                   resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                   return;
               }
               if (!person.isAdmin()) {
                   // Check roles via AccessManager
                   boolean hasRole = false;
                   for (String role : allowedRoles) {
                       // Resolve BoundRole by name and check
                       // Implementation detail: use AccessManager.getInstance().hasRole()
                   }
                   if (!hasRole) {
                       resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient permissions");
                       return;
                   }
               }
           }
       }

       // Submit async execution
       ChatProcessor.submitBackground(() -> {
           try {
               BackgroundAgentExecutor executor = new BackgroundAgentExecutor(registry);
               ApplicationContext context = ApplicationContext.manual(agentName,
                   TLContext.currentUser() != null ? TLContext.currentUser().getName() : "unknown");
               executor.execute(agentName, context);
           } catch (Exception ex) {
               Logger.error("On-demand background agent '" + agentName + "' failed",
                   ex, ChatServlet.class);
           }
       });

       // Return 202 Accepted
       resp.setStatus(HttpServletResponse.SC_ACCEPTED);
       resp.setContentType("application/json");
       resp.getWriter().write("{\"agentName\":\"" + agentName + "\",\"status\":\"submitted\"}");
   }
   ```

- [ ] **Step 3: Compile check**

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS (all references to old classes resolved)

- [ ] **Step 4: Commit**

```bash
git add tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatSSEHandler.java
git add tl-chat/src/main/java/com/top_logic/ai/chat/api/ChatServlet.java
git commit -m "refactor: migrate ChatSSEHandler and ChatServlet to background agent framework"
```

---

### Task 12: Update chat-client.js

**Files:**
- Modify: `tl-chat/src/main/webapp/script/chat-client.js`

- [ ] **Step 1: Replace `review_alert` with `background_alert`**

Around lines 1024-1033, replace:

```javascript
eventSource.addEventListener("review_alert", function (e) {
    log("SSE review_alert event", e.data);
```

with:

```javascript
eventSource.addEventListener("background_alert", function (e) {
    log("SSE background_alert event", e.data);
```

And update the error log at line 1031:

```javascript
log("SSE background_alert parse error", err);
```

The `showReviewAlert()` function (line 801) can keep its name or be renamed to `showBackgroundAlert()`. The JSON payload now includes `agentName` — optionally display it in the banner.

- [ ] **Step 2: Commit**

```bash
git add tl-chat/src/main/webapp/script/chat-client.js
git commit -m "refactor: update SSE event listener from review_alert to background_alert"
```

---

### Task 13: Delete old classes and update ReviewAgent config

**Files:**
- Delete: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotification.java`
- Delete: `tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotificationStore.java`
- Delete: `tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/ReviewAlertEvent.java`
- Modify: `tl-ai-demo/src/main/webapp/WEB-INF/autoconf/com.top_logic.ai.service.agents.AgentRegistry.config.xml`

- [ ] **Step 1: Delete old review-specific classes**

```bash
git rm tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotification.java
git rm tl-chat/src/main/java/com/top_logic/ai/chat/layout/ReviewNotificationStore.java
git rm tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/ReviewAlertEvent.java
```

- [ ] **Step 2: Verify no remaining references to deleted classes**

Search for `ReviewNotification`, `ReviewNotificationStore`, `ReviewAlertEvent` across the codebase. Fix any remaining references.

Run: `mvn compile -pl tl-chat -q`
Expected: BUILD SUCCESS (all references already migrated in previous tasks)

- [ ] **Step 3: Add `<background>` block to ReviewAgent in config XML**

In the AgentRegistry config XML, find the `ReviewAgent` declaration and add the background block:

```xml
<agent name="ReviewAgent"
    tools="gitea.create_gitea_issue">
    <system-prompt>... (keep existing) ...</system-prompt>

    <background scope="chat" authority="advisory">
        <trigger class="com.top_logic.ai.service.agents.background.trigger.PostTurnTrigger" />
        <input class="com.top_logic.ai.service.agents.background.input.ChatContextInput"
               content="last-turn" />
        <output-handlers>
            <handler class="com.top_logic.ai.chat.background.NotificationOutputHandler"
                     severity="warning" />
        </output-handlers>
    </background>
</agent>
```

- [ ] **Step 4: Full build**

Run: `mvn clean install -q`
Expected: BUILD SUCCESS across all modules

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: delete old ReviewNotification classes, add background config to ReviewAgent"
```

---

## Chunk 3: Verification

### Task 14: Full build and manual verification

- [ ] **Step 1: Full clean build with tests**

Run: `mvn clean install`
Expected: BUILD SUCCESS with all tests passing

- [ ] **Step 2: Verify ReviewAgent still works**

Start the demo app and verify:
1. Send a chat message
2. Confirm the ReviewAgent fires in the background (check server logs for "Background agent 'ReviewAgent'" log messages)
3. Confirm SSE `background_alert` events are received by the client
4. Confirm the `/chat/review` backwards-compat endpoint still works (now delegates to the generic handler)

- [ ] **Step 3: Verify no regressions**

Check that:
1. Normal chat flow (Orchestrator → specialists) works unchanged
2. Tool calls and delegations appear in timeline as before
3. Session persistence works
4. No errors in server logs during normal operation

- [ ] **Step 4: Final commit if any fixes needed**

```bash
git add -A
git commit -m "fix: address issues found during manual verification"
```
