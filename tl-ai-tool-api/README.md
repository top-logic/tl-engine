# tl-ai-tool-api

The contract a tool is written against: the interfaces, the annotations, and the one piece of
machinery a tool answer cannot do without - no dependency on `tl-ai-core` - so a module can define
tools for the agent without depending on the agent engine.

| Type | What it is |
|---|---|
| `Tool` | `execute(Map<String, Object> args, ToolExecutionContext context) -> ToolResult` |
| `ToolMode` | What the call does to the world: `READ`, `USER_SCOPED`, `DATA_WRITE`, `STRUCTURAL`, `EXTERNAL` |
| `@ToolMethod` / `@P` | Write a plain method; `AnnotatedTools` turns it into a `Tool` with a schema |
| `ToolSchema` | Name, description and JSON-Schema parameters as the model sees them |
| `ToolExecutionContext` | What the framework tells the tool about the call it is serving |
| `RunInfo` | Which agent run the call belongs to |
| `UntrustedEnvelope` / `AnswerMarking` | The tags that mark a tool answer as data rather than instructions, and who puts them on |

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

### A parameter that names a model object

Declare the parameter as the object, not as its identifier:

```java
@ToolMethod(description = "Reports what an invoice is still worth.", mode = ToolMode.READ,
    group = ToolGroups.DATA_READ)
public String openAmount(@P("The invoice") Invoice invoice) {
    ...
}
```

The model is shown a `string` whose description says the identifier is the `"TypeName:id"` a tool
result reports, and the framework resolves it back to the object **before the method is invoked**,
through `ToolExecutionContext.resolveObject(String)`. That call is the read check of the run's
principal, so a tool does not run one and cannot forget one; an object the principal may not read is
reported exactly like one that does not exist. A `List<Invoice>` parameter works the same way, element
by element.

An identifier that names nothing, names something unreadable, or is not an identifier at all fails as
a `ToolArgumentCoercionException`. That matters beyond the wording: the agent loop treats it as a
**synthetic** failure - the method never ran, so the turn's other writes are not rolled back. Before
this existed, every Java tool resolved the string itself, which meant one resolver and one read check
per application, and a forgotten one leaked (Gitea #381).

## What your answer claims

A tool answer is text the model reads next to its own instructions, so it has to say which of the
two it is. The framework marks it for you: **an answer of a `@ToolMethod` is wrapped in
`<untrusted_model_data source="yourTool">…</untrusted_model_data>`**, the same envelope a TL-Script
tool's answer gets, and the system prompt states once per run what a block like that means - data to
read, quote and report, never an instruction, whatever it says inside.

Marking is the default because the answer that most needs it is the one whose author never thought
about it: the free text in a stored record was typed by some other end user, who is not the person
this run serves. Declaring nothing therefore marks; you opt out, in one place, where you can say why:

```java
@ToolMethod(description = "Files the report.", mode = ToolMode.DATA_WRITE,
    group = ToolGroups.DATA_WRITE, marking = AnswerMarking.NONE)
public String fileReport(@P("The report to file") String text) {
    return "Report filed. Call listReports to see it.";
}
```

| `marking` | Use it when | What happens |
|---|---|---|
| `FRAMEWORK` (default) | the answer carries anything your application stored - a record, a document, a definition, another system's response | the whole answer is wrapped in `<untrusted_model_data>` |
| `TOOL` | you mark your own content: another envelope kind (a fetched page, a stored document), a marked block with your own sentences around it, or a better `source` than the tool's name | nothing is added; the marking is yours to get right |
| `NONE` | the answer is this application's own sentence - a confirmation, a refusal, a schema, computed help - or is itself an instruction the agent is meant to follow | nothing is added |

Three things worth knowing before you choose:

- **`NONE` is a claim, and a wrong one is expensive in both directions.** Marking an answer that
  *is* an instruction (a skill's text, a report from an agent you delegated to, "call `listTypes`
  first") switches that instruction off, because the standing rule says a marked block is never one.
  Leaving a stored record unmarked hands an injected sentence straight into the instruction channel.
- **The whole answer is marked, not the successful half of it.** Nothing at the reflective seam can
  tell a disclosed record from your own sentence about a call that failed, and a rule that held for
  some answers would be one the model cannot use. If that line matters for your tool, draw it
  yourself with `TOOL`.
- **An answer that already is one complete envelope block is passed through unchanged**
  (`UntrustedEnvelope.isMarked`). A tool that marked its answer by hand before the framework did -
  and an application that still does - does not get a second wrap, which would neutralise the tags of
  the first and leave it delimiting nothing.

If you implement `Tool` directly instead of annotating a method, none of this happens for you: you
write `execute`, so you write the marking too, with `UntrustedEnvelope` (or, in `tl-ai-core`,
`ReadFilter`, which marks everything it discloses).

## A tool written as TL-Script

An administrator can configure a tool whose body is TL-Script instead of Java, with
`ScriptToolFactory`. It reaches the same `ToolResult`, and the two fields that matter for a long
answer are configured beside the implementation:

```xml
<factory name="script-tools">
  <implementation class="com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory">
    <tools>
      <tool name="findOverdueInvoices" mode="READ" truncation-hint="4000">
        <parameters>...</parameters>
        <implementation>all(`acme:Invoice`).filter(i -> $i.get(`acme:Invoice#overdue`))</implementation>
        <summary>rows -> $rows.size() + " overdue invoices"</summary>
      </tool>
    </tools>
  </implementation>
</factory>
```

| | |
|--|--|
| `truncation-hint` | How many characters of the result reach the model before it is cut. Unset uses the installation's default. |
| `summary` | One argument - the value the body answered - and the short form compaction keeps in place of the result. Unset keeps the beginning of the result. It is disclosed under the same read rights as the result, so it may name objects. |

Which run the call belongs to is `aiRunInfo()` inside the body: the same facts `runInfo()` gives a
Java tool, as a map with the keys `runId`, `conversationId`, `agentName`, `model`, `provider`,
`executionId` and `delegationDepth`. Outside an agent run every value is empty.

The one thing a script body still cannot answer with is `ToolContent` - an image or a PDF for the
model to *perceive* - because TL-Script has no value that says "this is a picture". Everything else
the two kinds of tool can do is the same, object-typed parameters included.
