# Background Agent Framework — Design Spec

**Date:** 2026-03-10
**Status:** Approved
**Scope:** Generalize the ReviewAgent concept into a declarative, pluggable background agent framework.

## Problem

The ReviewAgent is currently hardcoded in `ChatProcessor`, `ChatServlet`, and `ChatSSEHandler`. Adding another background agent (security gate, compliance checker, model validator) would require duplicating Java code. We need a framework where background agents are declared in configuration, not code.

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Architecture | Unified Pipeline (Input → Agent → Output) | Single abstraction covers all lifecycle patterns |
| Config location | Same AgentRegistry, `<background>` block on AgentConfig | All are agents — different lifecycle, not different nature |
| Authority model | Configurable per agent (advisory vs authoritative) | Fits TopLogic's configuration-driven philosophy |
| Input modes | Combined: Provider, Script, Autonomous, ChatContext | Each agent picks what fits its use case |
| Scheduling | Leverage existing TopLogic `Scheduler` + `StateHandlingTask` | Reuses cluster-aware scheduling, admin UI, concurrency |
| Output | Tools (during execution) + OutputHandlers (post-execution) | Both active side-effects and declarative result processing |
| Security | Role-based via `allowed-roles` + TopLogic `AccessManager` | Reuses existing RBAC infrastructure |

## Configuration Model

A background agent is a regular agent with an additional `<background>` config block. Agents without this block remain conversational agents (unchanged behavior).

The `<background>` block uses TopLogic's standard `PolymorphicConfiguration` pattern for polymorphic sub-configs (`trigger`, `input`, `output-handlers`). Each implementation is identified by its `class` attribute as is standard in TopLogic XML configuration.

### Example: ReviewAgent (post-turn observer)

```xml
<agent name="ReviewAgent" tools="gitea.create_gitea_issue">
  <system-prompt>You are a quality reviewer...</system-prompt>

  <background scope="chat" authority="advisory">
    <trigger class="...PostTurnTrigger" />
    <input class="...ChatContextInput" content="last-turn" />
    <output-handlers>
      <handler class="...NotificationHandler" severity="warning" />
    </output-handlers>
  </background>
</agent>
```

### Example: SecurityGate (pre-turn interceptor, authoritative)

```xml
<agent name="SecurityGate" tools="tl.executeTLScript">
  <system-prompt>
    You are a security compliance gate. Analyze the user's request.
    You MUST respond with exactly one of:
    - "PASS" if the request is allowed
    - "BLOCK: &lt;reason&gt;" if it violates business rules
    No other output format is accepted.
  </system-prompt>

  <background
    scope="chat"
    authority="authoritative"
    notify-on-block="true"
    block-message="This response was filtered by security policy: {reason}">
    <trigger class="...PreTurnTrigger" timeout="30000" fail-policy="open" />
    <input class="...ChatContextInput" content="full-chat" />
    <output-handlers>
      <handler class="...AuditLogHandler" />
    </output-handlers>
  </background>
</agent>
```

### Example: ComplianceChecker (scheduled, autonomous)

```xml
<agent name="ComplianceChecker"
  tools="tl.listInstances,tl.executeTLScript,gitea.create_gitea_issue">
  <system-prompt>
    You are a compliance agent. Check all SensitiveRecord instances
    for data retention policy violations...
  </system-prompt>

  <background scope="application" authority="advisory">
    <trigger class="...ScheduledTrigger">
      <schedules>
        <daily time-of-day="02:00" />
      </schedules>
    </trigger>
    <input class="...AutonomousInput"
           trigger-message="Run your daily compliance check." />
    <output-handlers>
      <handler class="...AuditLogHandler" />
      <handler class="...NotificationHandler" severity="error" />
    </output-handlers>
  </background>
</agent>
```

### Example: ModelQualityChecker (on-demand, TLScript input)

```xml
<agent name="ModelQualityChecker" tools="gitea.create_gitea_issue">
  <system-prompt>
    You are a model quality agent. Review the model data provided
    and identify missing descriptions, naming violations...
  </system-prompt>

  <background scope="application" authority="advisory">
    <trigger class="...OnDemandTrigger">
      <allowed-roles>admin, auditor</allowed-roles>
    </trigger>
    <input class="...ScriptInput">
      <script>
        all(`myModule:MyType`)
          .map(t -> t.get(`name`) + ": " + t.get(`description`))
          .toString("\n")
      </script>
    </input>
    <output-handlers>
      <handler class="...NotificationHandler" severity="info" />
    </output-handlers>
  </background>
</agent>
```

### Java Config Interface

All config interfaces follow TopLogic's standard patterns: `ConfigurationItem` for fixed-structure sub-configs, `PolymorphicConfiguration` for polymorphic sub-elements with `class` discriminator. Trigger, input, and output-handler configs are **config interfaces only** — the `BackgroundAgentExecutor` dispatches on their type via `instanceof` to select the appropriate runtime behavior.

```
AgentConfig (existing interface, extended)
  @Name("background")
  + getBackground() : BackgroundConfig           // null = conversational agent

BackgroundConfig extends ConfigurationItem       // fixed structure, not polymorphic
  @Name("scope")     getScope() : Scope          // CHAT, APPLICATION
  @Name("authority") getAuthority() : Authority   // ADVISORY, AUTHORITATIVE
  @Name("notify-on-block") getNotifyOnBlock()     // only for AUTHORITATIVE
  @Name("block-message") getBlockMessage()        // template with {reason}
  @Name("trigger")   getTrigger() : PolymorphicConfiguration<TriggerConfig>
  @Name("input")     getInput() : PolymorphicConfiguration<InputConfig>
  @Name("output-handlers") getOutputHandlers() : List<PolymorphicConfiguration<OutputHandler>>

TriggerConfig extends ConfigurationItem (polymorphic sub-types)
  PostTurnTrigger extends TriggerConfig          // fires after each chat turn
  PreTurnTrigger extends TriggerConfig           // fires before agent responds
    @Name("timeout") getTimeout() : long         // max wait in ms (default 30000)
    @Name("fail-policy") getFailPolicy()         // OPEN or CLOSED
  ScheduledTrigger extends TriggerConfig         // wraps TopLogic SchedulingAlgorithm
    @Name("schedules") getSchedules()            // List<PolymorphicConfiguration<SchedulingAlgorithm>>
  OnDemandTrigger extends TriggerConfig          // manual via REST API
    @Name("allowed-roles") getAllowedRoles()     // List<String>

InputConfig extends ConfigurationItem (polymorphic sub-types)
  ChatContextInput extends InputConfig           // serializes chat via ConversationSerializer
    @Name("content") getContent() : Content      // enum: LAST_TURN, FULL_CHAT
  ScriptInput extends InputConfig                // evaluates TLScript expression
    @Name("script") getScript() : String
  AutonomousInput extends InputConfig            // just a trigger message, agent uses tools
    @Name("trigger-message") getTriggerMessage() : String
  ProviderInput extends InputConfig              // custom Java InputProvider
    @Name("provider") getProvider() : PolymorphicConfiguration<InputProvider>

OutputHandler extends ConfigurationItem (polymorphic sub-types)
  NotificationHandler extends OutputHandler
    @Name("severity") getSeverity() : Severity   // INFO, WARNING, ERROR
  AuditLogHandler extends OutputHandler
  SchedulerLogHandler extends OutputHandler
```

**Dispatching pattern in BackgroundAgentExecutor:**
The executor resolves the config into runtime behavior via `instanceof`:
```java
InputConfig inputConfig = backgroundConfig.getInput();
if (inputConfig instanceof ChatContextInput cfg) {
    input = new ChatContextAdapter(cfg).gatherInput(context);
} else if (inputConfig instanceof ScriptInput cfg) {
    input = new ScriptInputAdapter(cfg).gatherInput(context);
} // ... etc
```

## Execution Engine

### BackgroundAgentExecutor

Central service that runs any background agent through the pipeline. All trigger sources call into this one executor.

```
execute(agentName, context) :
  1. Resolve BackgroundConfig from AgentRegistry
  2. Resolve InputAdapter from config (instantiate polymorphic InputConfig)
  3. Gather input: adapter.gatherInput(context) → String
  4. Build UntypedAgent via AgentRegistry.buildUntypedAgent()
  5. Invoke agent with input message
  6. Parse result into AgentResult (see Result Parsing below)
  7. Run OutputHandlers in order
  8. Return AgentResult
```

### AgentResult

```
AgentResult
  status:  PASS | ISSUE_FOUND | BLOCK
  message: String                        // the agent's full response text
  reason:  String                        // extracted reason (for BLOCK: parsed from "BLOCK: <reason>")
  details: Map<String, Object>           // additional metadata (agent name, timestamp, trigger type)
```

### Result Parsing

The executor parses the agent's free-text response into an `AgentResult` using these rules:

1. **For authoritative agents (pre-turn gates):**
   - Response starts with `PASS` (case-insensitive) → `status = PASS`
   - Response starts with `BLOCK:` → `status = BLOCK`, text after `BLOCK:` is extracted as `reason`
   - Response does not match either pattern → treated as `PASS` (fail-open default) or `BLOCK` (fail-closed), based on `PreTurnTrigger.failPolicy`
   - The `{reason}` placeholder in `block-message` is replaced with the extracted `reason` string

2. **For advisory agents (post-turn observers, scheduled, on-demand):**
   - Response contains "No issues found" (case-insensitive) or is empty → `status = PASS`
   - Any other non-empty response → `status = ISSUE_FOUND`, full response stored as `message`
   - Advisory agents cannot produce `BLOCK` status

3. **Tool-based side effects (e.g. ReviewAgent calling `create_gitea_issue`):**
   - Tool calls happen during agent execution (step 5), independent of result parsing
   - The agent's final text response is still parsed as above for OutputHandler routing
   - An agent that creates issues via tools AND returns "No issues found" is treated as PASS (the tool calls already happened)

### Trigger Integration

**Chat-scoped (ChatProcessor):**

```java
// PRE-TURN: synchronous, before Orchestrator
for (agent : registry.getBackgroundAgents(PRE_TURN, CHAT)) {
    result = executor.executeSync(agent, ChatContext.fullChat(chat));
    if (result.status == BLOCK) {
        addToChat(result.blockMessage);
        return;  // don't run Orchestrator
    }
}

// MAIN: Orchestrator runs as today
response = orchestrator.invoke(message);

// POST-TURN: async, after Orchestrator
for (agent : registry.getBackgroundAgents(POST_TURN, CHAT)) {
    executor.executeAsync(agent, ChatContext.lastTurn(msg, response, task));
}
```

**Application-scoped (Scheduler):**

`BackgroundAgentTask extends StateHandlingTask` bridges TopLogic's Scheduler to `BackgroundAgentExecutor`. Each scheduled background agent gets a `BackgroundAgentTask` registered during `AgentRegistry.startUp()`.

**On-demand (REST API):**

`POST /chat/background/{agentName}` — role-checked, async execution.

Response: `HTTP 202 Accepted` with JSON body `{"agentName": "...", "status": "submitted"}`. The caller does not wait for completion. Results are delivered through the agent's configured OutputHandlers (SSE notification if `NotificationHandler` is configured, system log if `AuditLogHandler` is configured, etc.). There is no polling endpoint in v1 — callers should use SSE or check logs.

### Input Adapters

| Adapter | Input Source | Valid Scope |
|---------|-------------|-------------|
| `ChatContextAdapter` | ConversationSerializer (existing) | Chat only |
| `ScriptInputAdapter` | TLScript expression from config | Both |
| `AutonomousInputAdapter` | Trigger message string | Both |
| `ProviderInputAdapter` | Custom Java `InputProvider` class | Both |

### Output Handlers

| Handler | Action | Auto-added? |
|---------|--------|-------------|
| `NotificationHandler` | Push to notification store (see Notification Routing) | No (configured) |
| `AuditLogHandler` | Write to TopLogic system log via `Logger` | No (configured) |
| `SchedulerLogHandler` | Write to Scheduler TaskLog | Yes (for scheduled agents) |

Future handlers (not in v1): EmailHandler, WebhookHandler, PersistenceHandler.

### Notification Routing

`NotificationHandler` pushes `BackgroundAgentNotification` records to a notification store. Routing depends on scope:

- **Chat-scoped agents:** Notification is pushed to the session-scoped `BackgroundNotificationStore` on `TaskProgressTracker` (same mechanism as today's `ReviewNotificationStore`, but generalized). The `ChatSSEHandler` drains and emits these as `background_alert` SSE events to the specific chat session.

- **Application-scoped agents:** Notification is pushed to a singleton `GlobalNotificationStore`. The `ChatSSEHandler` drains both session-scoped and global stores during its polling loop. All active SSE connections receive global notifications. This means any admin with the chat open sees scheduled agent alerts.

## Security

### On-demand endpoint

```
POST /chat/background/{agentName}
  1. ChatSessionFilter validates session (existing TopLogic filter on /chat/*)
  2. TLContext.currentUser() → authenticated Person
  3. Lookup agent's allowed-roles from OnDemandTrigger config
  4. If no roles configured → any authenticated user allowed
  5. If person.isAdmin() → always allowed
  6. Otherwise: AccessManager.hasRole(person, roles) → 403 if denied
```

### Other trigger types

- **Scheduled:** Runs in `SystemInteraction` context via Scheduler — no user, no role check. Access controlled by who can configure the Scheduler (admin).
- **Pre/post-turn:** Triggered within existing chat session — user already authenticated.

## Concurrency & Reliability

| Trigger | Execution | Concurrency | Failure |
|---------|-----------|-------------|---------|
| Post-turn | Async (background thread) | In-flight dedup per agent per session (AtomicBoolean) | Isolated — never affects chat |
| Pre-turn | Sync (blocks Orchestrator) | Sequential per request | Configurable via `fail-policy`: `open` = treat as PASS, `closed` = treat as BLOCK |
| Scheduled | Scheduler thread pool | Scheduler's `maxTask` limit | Logged to TaskLog, retried per schedule |
| On-demand | Async (background thread) | No limit | Result via configured OutputHandlers |

**Post-turn debouncing:** Each background agent gets an `AtomicBoolean` on the session-scoped `TaskProgressTracker`, keyed by agent name. `executeAsync` checks `compareAndSet(false, true)` before submitting and resets to `false` in the `finally` block. This prevents concurrent runs of the same agent within one session, while allowing different agents to run in parallel.

### Error Handling

Errors during background agent execution (InputAdapter throws, TLScript fails, LLM timeout, OutputHandler fails):

- **Post-turn agents:** Error is logged via `Logger.error()`. The chat is never affected. If `AuditLogHandler` is configured, the error is also recorded there.
- **Pre-turn agents:** Error handling follows `fail-policy`:
  - `open` (default): Error is logged, agent is treated as PASS, Orchestrator proceeds normally.
  - `closed`: Error is logged, agent is treated as BLOCK with a generic error message.
- **Scheduled agents:** Error is logged to `TaskLog` with `ResultType.ERROR`. The Scheduler shows the failure in the admin UI and retries on the next scheduled run.
- **On-demand agents:** Error is logged. If `NotificationHandler` is configured, an error notification is pushed.

## Component Map

### Module boundaries

The framework spans two modules. To avoid circular dependencies:

- **`tl-ai-service`** contains: `BackgroundConfig`, `BackgroundAgentExecutor`, `AgentResult`, all trigger/input/output/context classes, `InputAdapter` interface, `ScriptInputAdapter`, `AutonomousInputAdapter`, `ProviderInputAdapter`, `OutputHandler` interface, `AuditLogHandler`, `SchedulerLogHandler`, `BackgroundAgentTask`.
- **`tl-chat`** contains: `ChatContextAdapter` (needs `ConversationSerializer`, `Chat`, `ChatTask`), `NotificationHandler` (needs `BackgroundNotificationStore`, SSE integration), `GlobalNotificationStore`.

`tl-ai-service` defines the `InputAdapter` and `OutputHandler` interfaces. `tl-chat` provides chat-specific implementations. This follows the existing dependency direction: `tl-chat` depends on `tl-ai-service`, never the reverse.

### New classes (~20)

```
tl-ai-service: com.top_logic.ai.service.agents.background/
  BackgroundConfig              Config interface
  BackgroundAgentExecutor       Central pipeline
  AgentResult                   Pipeline result
  ResultParser                  Parses LLM text → AgentResult

  trigger/
    TriggerConfig               Polymorphic config interface
    PostTurnTrigger
    PreTurnTrigger
    ScheduledTrigger
    OnDemandTrigger

  input/
    InputAdapter                Interface: gatherInput(context) → String
    ScriptInputAdapter          Evaluates TLScript
    AutonomousInputAdapter      Returns trigger message
    ProviderInputAdapter        Delegates to Java InputProvider
    InputProvider               Interface for custom providers

  output/
    OutputHandler               Interface: handle(result, context)
    AuditLogHandler             Writes to system log
    SchedulerLogHandler         Writes to Scheduler TaskLog

  context/
    BackgroundContext            Base (agent name, trigger type, timestamp)
    ChatContext                  Extends: + chat messages, tool timeline
    ApplicationContext           Extends: + trigger source

  scheduler/
    BackgroundAgentTask          StateHandlingTask bridge to executor

tl-chat: com.top_logic.ai.chat.background/
  ChatContextAdapter            Reuses ConversationSerializer
  NotificationHandler           Pushes to notification stores
  BackgroundNotificationStore   Generalized from ReviewNotificationStore
  GlobalNotificationStore       Singleton for application-scoped notifications
  BackgroundAgentNotification   Generalized from ReviewNotification
```

### Modified classes

| Class | Changes |
|-------|---------|
| `AgentConfig` | + `@Name("background") getBackground()` property |
| `AgentRegistry` | + `getBackgroundAgents(trigger, scope)`, + register `BackgroundAgentTask`s with Scheduler at startup |
| `ChatProcessor` | Remove `triggerReviewAgent()`, add generic `triggerPostTurnAgents()` / `executePreTurnAgents()` |
| `ChatServlet` | Remove `handleReview()`, add generic `handleBackgroundTrigger()`. Keep `/chat/review` as alias redirecting to `/chat/background/ReviewAgent` for backwards compatibility. |
| `ChatSSEHandler` | Replace `review_alert` with generic `background_alert` event type. Drain both session-scoped `BackgroundNotificationStore` and `GlobalNotificationStore`. |
| `TaskProgressTracker` | Replace `_reviewInProgress` / `_reviewNotifications` with `Map<String, AtomicBoolean>` for per-agent debouncing and `BackgroundNotificationStore` for notifications. |
| `chat-client.js` | Replace `review_alert` SSE event listener with `background_alert`. Update notification rendering to use `BackgroundAgentNotification` fields. |

### Deleted classes (migrated into framework)

| Class | Replacement |
|-------|-------------|
| `ReviewNotification` | `BackgroundAgentNotification` (generalized) |
| `ReviewNotificationStore` | `BackgroundNotificationStore` (generalized) |
| `ReviewAlertEvent` | Generalized into `BackgroundAlertEvent` |

### ReviewAgent migration

All hardcoded ReviewAgent Java code in ChatProcessor and ChatServlet is deleted. The ReviewAgent becomes purely declarative — same behavior, zero agent-specific Java code. The existing `ConversationSerializer` is preserved and reused by `ChatContextAdapter`.

## Open TODOs

- **Testing strategy:** Unit tests for `ResultParser`, `BackgroundAgentExecutor` pipeline, each `InputAdapter`, each `OutputHandler`. Integration test for `BackgroundAgentTask` with Scheduler.
