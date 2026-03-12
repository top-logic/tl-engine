# Output Handler Redesign

## Problem

The current background agent output handling has several issues:

1. **Confusing handler options** — Four handler types are selectable, including the base `OutputHandler` interface which is just a marker. `AuditLogHandler` and `SchedulerLogHandler` overlap in purpose.
2. **Brittle result parsing** — `ResultParser` parses free-text LLM responses for keywords like `"PASS"`, `"BLOCK:"`, `"NO ISSUES FOUND"`. This is fragile and review-agent-specific. Other agent types produce entirely different output formats.
3. **Rigid authority model** — The `ADVISORY` vs `AUTHORITATIVE` config flag is tightly coupled to the parsing logic and doesn't generalize.

## Design

### Remove

- **`ResultParser`** — No more free-text parsing of LLM responses.
- **`AgentResult`** — The PASS/BLOCK/ISSUE_FOUND status enum and its factory methods.
- **`AuditLogHandler`**, **`SchedulerLogHandler`**, **`NotificationOutputHandler`** — All three built-in handler implementations.
- **`authority`** config field — No longer needed; authority is implicit (see `block_turn` tool below).
- **`notifyOnBlock`**, **`blockMessage`** config fields — Replaced by the `block_turn` tool's reason parameter.
- **`OutputHandlerContext`** — No longer wraps `AgentResult`; replaced by passing raw response and `BackgroundContext` directly.

### New: `block_turn` Tool

A tool that developers can add to any background agent. Its presence makes the agent authoritative — no config flag needed.

**Semantics by trigger type:**

- **Pre-turn agent** calls `block_turn(reason)` — The user's message never reaches the main entry-point agent. The reason is shown as a system message in the chat.
- **Post-turn agent** calls `block_turn(reason)` — The main agent's answer is discarded. The reason is shown instead.

**Constraints:**

- Only relevant for `CHAT` scope agents. Application-scope agents have no turn to block.
- If the agent does not call `block_turn`, the turn proceeds normally (advisory behavior).

### New: OutputHandler Types

The `OutputHandler` base interface remains a `ConfigurationItem`. Two concrete types replace the old three:

#### 1. Java Class Handler

Follows the standard TopLogic `PolymorphicConfiguration` pattern:

```java
public interface JavaOutputHandler extends OutputHandler, PolymorphicConfiguration<OutputHandlerImplementation> {
}
```

The implementation class:

```java
public abstract class OutputHandlerImplementation extends AbstractConfiguredInstance<JavaOutputHandler> {

    public OutputHandlerImplementation(InstantiationContext context, JavaOutputHandler config) {
        super(context, config);
    }

    public abstract void handleOutput(String response, BackgroundContext context);
}
```

**XML configuration:**

```xml
<output-handler config:interface="...JavaOutputHandler"
    class="com.example.MyCustomHandler"/>
```

- Not shown in the UI (advanced/developer option).
- Receives the raw LLM response string and `BackgroundContext`.

#### 2. TL-Script Handler

Uses the proper `Expr` type with `ExprFormat` for inline TL-Script:

```java
public interface ScriptOutputHandler extends OutputHandler {

    @Mandatory
    @Format(ExprFormat.class)
    Expr getScript();
}
```

**XML configuration:**

```xml
<output-handler config:interface="...ScriptOutputHandler">
    <script>
        response -> $response.length() > 0
    </script>
</output-handler>
```

- Shown in UI with syntax-highlighted TL-Script editor.
- The raw LLM response string is passed as the first argument using standard TL-Script lambda syntax.

### Execution Pipeline Changes

`BackgroundAgentExecutor.execute()` simplifies to:

1. Resolve input adapter, gather input.
2. Build and invoke the LLM agent.
3. Check if `block_turn` was called — handle accordingly.
4. Run output handlers, passing raw response + `BackgroundContext`.

The `parseResult()` method and all related branching on `authority` are removed.

### Migration

Existing agent configurations that use `AuditLogHandler`, `SchedulerLogHandler`, or `NotificationOutputHandler` must be updated:

- Replace with a `ScriptOutputHandler` or `JavaOutputHandler` that performs the equivalent logging/notification.
- Replace `authority="AUTHORITATIVE"` with adding the `block_turn` tool to the agent's tool set.
- Remove `notify-on-block` and `block-message` attributes.
