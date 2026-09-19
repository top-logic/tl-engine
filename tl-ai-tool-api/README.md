# tl-ai-tool-api

The contract a tool is written against. Interfaces and annotations only - no implementations, no
dependency on `tl-ai-core` - so a module can define tools for the agent without depending on the
agent engine.

| Type | What it is |
|---|---|
| `Tool` | `execute(Map<String, Object> args, ToolExecutionContext context) -> ToolResult` |
| `ToolMode` | What the call does to the world: `READ`, `USER_SCOPED`, `DATA_WRITE`, `STRUCTURAL`, `EXTERNAL` |
| `@ToolMethod` / `@P` | Write a plain method; `AnnotatedTools` turns it into a `Tool` with a schema |
| `ToolSchema` | Name, description and JSON-Schema parameters as the model sees them |
| `ToolExecutionContext` | What the framework tells the tool about the call it is serving |
| `RunInfo` | Which agent run the call belongs to |

## How a tool learns which run it is in

`ToolExecutionContext.runInfo()` answers it:

```java
@ToolMethod(description = "Files the current draft under the agent that produced it.",
    mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE)
public String fileDraft(ToolExecutionContext context, @P("The draft text to file") String text) {
    RunInfo run = context.runInfo();
    return archive.store(text, run.agentName(), run.runId());
}
```

`RunInfo` carries, all as plain `String`s:

| Field | Meaning |
|---|---|
| `runId()` | This run. A new one per chat message. |
| `conversationId()` | The conversation (agent session). The same across all runs of one chat - key a per-conversation resource on this, not on `runId()`. |
| `agentName()` | The agent handling the run. |
| `model()` / `provider()` | What it talks to. |
| `executionId()` | The persistent `AgentExecution` row of this run, for handing to a service that resolves it. `null` when the run is not recorded. |
| `delegationDepth()` / `parent()` / `root()` | Where in a delegation chain this run sits. A sub-agent reads its own facts and, through `parent()`, the run that delegated it. |

Two rules:

- **Every field may be `null`, and reading a `null` one is not an error.** A run without a recorded
  session has no `conversationId()`; an unrecorded run has no `executionId()`. Outside an agent run
  altogether - a tool called from TL-Script, from a test, or over the MCP endpoint - `runInfo()`
  answers `RunInfo.NONE`, where everything is `null` and `isKnown()` is `false`. It is never itself
  `null`.
- **These are the facts as of the moment the run started**, captured on the thread that knew them.
  They do not change during the run.

Code *beneath* a tool - application code, a listener, a TL-Script function - gets no context
parameter. It reads the same record from the thread instead:

```java
RunInfo run = RunInfoBinding.current();   // com.top_logic.tl.ai.core.execution
```

`ToolExecutions.execute` binds it around every tool call, on the thread the tool actually runs on
(a worker thread for the tools of a concurrent batch), and unbinds it when the call returns. Outside
a tool call it answers `RunInfo.NONE`, never the previous task's run.

### Why it is a record of strings, and not the `AgentExecution`

A tool in `tl-ai-core` can also reach the run's persistent `AgentExecution` through
`ToolContext.executionHandle().get()`. **That wrapper is an opaque token, not something to read.**

The framework writes that row on `AgentExecutionStore`'s own writer thread, in its own session, at a
revision *newer* than the one the tool's interaction reads at - the interaction is opened before the
run starts, and a TopLogic sub-session's view only advances at sanctioned points. So every attribute
access on it from a tool fails:

```
com.top_logic.knowledge.service.db2.DeletedObjectAccess:
  Object (ID: AgentExecution:10006; Wrapper class: ...AgentExecutionImpl)
  was deleted before or created after access revision 87.
```

That was Gitea #405, and it is not a race: it happens on every run, and reading the handle as the
very first statement of the tool fails identically. Nothing on the tool's side can fix it, because
nothing on the tool's side can make a row from a newer revision visible to a pinned view.

What a tool may do with the wrapper is **hand it to `AgentExecutionStore`**, which dereferences it on
its writer thread, where it is visible - that is how `AgentTaskMirror` and the `askUser` recorder
work, and `AgentExecutionStore.sessionOf(execution)` resolves the run's session the same way. What a
tool must **read** is `runInfo()`.

## Writing a tool

Either implement `Tool` directly, or - usually - annotate a method and let `AnnotatedTools` build the
schema from its signature:

```java
@ToolMethod(description = "Looks a contract up by its number.", mode = ToolMode.READ,
    group = ToolGroups.DATA_READ)
public String findContract(@P("The contract number, e.g. C-2026-0042") String number) {
    ...
}
```

The module is compiled with `-parameters`, so parameter names reach the schema. Declare `ToolMode`
honestly: `READ` tools need no approval and may be dispatched concurrently with other `READ` tools;
everything else runs serially, asks for approval, and (except `EXTERNAL`) enrols in the turn's
transaction.

### A tool that changes a long text

If your tool writes a long text value - a document body, a prompt, a stored note - do **not** take
the whole new value as a parameter. The model then has to emit the entire text to change a sentence
of it, which costs the whole value in output tokens and lets the untouched parts drift.

Take an anchor and a replacement instead, and apply them with
`com.top_logic.tl.ai.core.text.TextEdits`, which is `String` in and a structured result out:

```java
TextEdits.Result edit = TextEdits.replace(current, oldText, newText);
if (!edit.applied()) {
    return "error: " + edit.message();   // says what to do next, in a sentence a model can act on
}
store(edit.text());
```

It enforces the part where reliability lives - a unique match or nothing happens, a match cascade
that tolerates line endings and whitespace and reports when it fell back, and a miss that says where
the anchor stopped agreeing with the text. Offer a verbatim read beside it: an anchored edit without
a verbatim read to anchor on is a guess. `text-edit.readText` / `text-edit.editText` in `tl-ai-core`
are the generic pair; the contract is the JavaDoc of `TextEdits`.
