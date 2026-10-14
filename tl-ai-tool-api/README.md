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
| `ReadSource` | A source a tool read, which the answer may cite - see "How a tool says what it read" |
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

The framework writes that row on `AgentExecutionStore`'s own writer thread, in its own session, and a
tool reads at the revision its turn took (see [What a call reads of the
database](#what-a-call-reads-of-the-database)). Whether that revision has the row depends on when the
store wrote it and on how the run was started - a run inside somebody else's transaction, a delegated
one among them, does not move its view at all. Where it does not have it, every attribute access on
it fails:

```
com.top_logic.knowledge.service.db2.DeletedObjectAccess:
  Object (ID: AgentExecution:10006; Wrapper class: ...AgentExecutionImpl)
  was deleted before or created after access revision 87.
```

That was Gitea #405. Until #647 a run's view never moved, so it failed on every run, even as the very
first statement of the tool. Now it fails on some runs only, which makes reading it worse, not better.

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

### A tool that needs something a run may lack

Some tools can only act where the run has something: `present` needs a page that shows what it puts
on a channel. Offered anyway, the model calls such a tool and is refused, often after a prompt told it
to call exactly this tool. Say it instead, once for the whole run:

```java
@ToolMethod(description = "...", mode = ToolMode.READ, unavailable = "noPlaceToShow")
public String present(...) { ... }

/** Why this run has no place to show anything, or null when it has one. */
protected String noPlaceToShow(ToolExecutionContext context) { ... }
```

`AgentLoop` asks `Tool.unavailableReason(context)` before every request and leaves a tool that answers
out of the schemas the model is sent; a call that names it anyway is answered with the reason and the
tool does not run. It is not a permission - nothing is withheld from the user - so it says what the run
lacks, not which grant is missing. Keep the method cheap and free of side effects, and let it depend on
what the run is attached to rather than on the turn: its answer changes the tool array, which every
provider caches by prefix. The tool still refuses the same calls when it runs, because not every
caller asks first.

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

### Declaring the types a tool works on

A tool whose type is fixed can name it, and the name is enforced:

```java
@ToolMethod(description = "Lists the customers of a region.", mode = ToolMode.READ,
    group = ToolGroups.DATA_READ, types = "myapp:Customer")
public String listCustomers(@P("The region") String region) {
    ...
}
```

Before the tool runs, the acting account's READ rights are asked for every declared type. An account
that may read **none** of them is refused with a sentence naming the type, and the tool is left out of
the tool schemas the model is sent - so the model gets "you have no read access to `myapp:Customer`"
rather than an empty list it reports to the user as "there are no customers". One readable declaration
is enough: a tool naming two types still does the half of its job the account is entitled to.

Declaring is optional and costs nothing when left out - a tool that declares nothing is offered and
runs as before, and `listInstances`, the TL-Script tool and the search tools legitimately declare
nothing because their type comes from an argument. What protects the data is the per-object check
either way.

**Declare a type only where its `Read` grant in `<security-config>` goes to a role accounts hold on
the security root.** This check reads roles there, like every other tool-level check. A type whose read
right is derived *per object* - as `tl.ai.screenshots:Screenshot` derives `AiBinaryOwner` from the
screenshot's own owner - must not be declared, or the owner is refused their own object. Where you
cannot tell, declare nothing. See `doc/security/agent-security-architecture.md`, section 3.3.3.

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
  *is* an instruction (a skill's text, "call `listTypes` first") switches that instruction off,
  because the standing rule says a marked block is never one.
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

## What a call reads of the database

A turn reads one state of the database. Before the first call of a turn is checked, the agent loop
moves the run's view to the newest commit, and once more after an approval that first call waited
for, because somebody may have saved something meanwhile. From the first call that runs on, every
call of the turn reads that state, whatever other threads commit; the next turn reads the newer one
(Gitea #647).

So a tool reads what other people, scheduled jobs and earlier turns committed without doing anything
for it. One kind of tool has to:

**A tool that waits for other threads within its call** - it starts helpers that write and commit in
transactions of their own, and goes on to work with what they wrote - calls `catchUpWithCommits()`
once they are done:

```java
for (Future<?> helper : helpers) {
    helper.get();                   // each one commits in a transaction of its own
}
context.catchUpWithCommits();       // from here on this call reads what they committed
```

Without it the call reads what was there before the helpers wrote, and a change it makes to what they
wrote is a change of an outdated version: the turn's commit fails with "Object could not be updated,
as it has changes that are based on an outdated version (preventing lost update)", and the turn is
rolled back. What the turn has changed itself is kept. A commit of a helper that changed what the
turn has changed as well is a real conflict, and the turn is rolled back either way.

`catchUpWithCommits()` is `HistoryUtils.updateSessionRevision()` with the conflict turned into an
exception the tool need not handle. Call it rather than the engine: what it promises is the
framework's rule, and the engine method is how the framework keeps it today.

A run started inside somebody else's transaction - a script in a command that opened one, a delegated
run inside a turn that has written - does not move its view between turns: that would merge other
threads' commits into a transaction that is not the run's. Such a run reads what its caller reads.

## When the run is stopped

A run is stopped through its `CancellationToken` (`context.cancellationToken()`), never by
interrupting a thread. H2 reads its database file through an NIO `FileChannel`, and an interrupt that
reaches a thread while - or before - it reads one closes the channel for every thread of the
application, for good: every later click answers "Database operation failed" until the server is
restarted (Gitea #901).

So a tool **never interrupts a thread** - no `Thread.interrupt()`, no `Future.cancel(true)`, no
`shutdownNow()` on an executor whose tasks may touch the knowledge base. A tool that loops looks at
`isCancelled()`. A tool that waits for something only an interrupt can end early - an HTTP request, a
future of another thread - waits through the token, which interrupts exactly that wait and takes the
interrupt back when it ends:

```java
HttpResponse<String> response = context.cancellationToken()
    .whileBlocked(() -> client.send(request, BodyHandlers.ofString()));
String answer = context.cancellationToken().await(future, 30, TimeUnit.SECONDS);
```

A stopped wait ends with a `CancellationException`. Wrap the wait alone, never code that reads the
knowledge base. Code below a tool that is handed no context finds the run's token in
`RunCancellationBinding` (tl-ai-core), which offers the same two methods.

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

### What bounds a failure

`truncation-hint` - and `truncationHint` on a Java `ToolResult` - bounds an answer the tool
**succeeded** with. A failure is bounded too, but only by the installation's ceiling
(`max-tool-result-chars` on the agent's entry in `AgentRuntimeService`'s `<agent-overrides>`, or the
default): the agent loop cuts a `ToolState.Failed` text to the same budget, keeping head and tail
and naming the dropped middle, and emits the same `ToolCallResultTruncated` event an operator sees
for a success. The failed flag the
providers send as `is_error` survives the cut, so a truncated failure still arrives as a failure.

There is deliberately no per-tool hint for the error path. `ToolResult.failure(...)` carries none,
and the failures that are hardest to bound never pass a `ToolResult` at all - an exception out of a
Java tool or a TL-Script body, or the error body a remote MCP server answered with, whose length is
not this application's to choose. A hint would bound only the smallest of those three and would let
a tool lift the ceiling off its own error path, which is the cost the ceiling exists for: an
oversized answer is paid again on every later request of the run.

Which run the call belongs to is `aiRunInfo()` inside the body: the same facts `runInfo()` gives a
Java tool, as a map with the keys `runId`, `conversationId`, `agentName`, `model`, `provider`,
`executionId` and `delegationDepth`. Outside an agent run every value is empty.

The one thing a script body still cannot answer with is `ToolContent` - an image or a PDF for the
model to *perceive* - because TL-Script has no value that says "this is a picture". Everything else
the two kinds of tool can do is the same, object-typed parameters included.

## How a tool says what it read

An answer cites with links in its text, and the framework keeps a link as a footnote only when a tool
of the run read the source it points at. The framework's own read tools register what they read. A
tool of an application that reads something of its own registers it with
`ToolExecutionContext.readSource`:

```java
@ToolMethod(description = "Reads one statement by its code.", mode = ToolMode.READ, group = ToolGroups.DATA_READ)
public String readStatement(@P("The statement's code, e.g. FACT-001") String code, ToolExecutionContext context) {
    Statement statement = statements.byCode(code);
    context.readSource(ReadSource.of("statement", statement.code(), statement.title()));
    return statement.render();
}
```

- `kind` is one the application declared in `CitationKinds`; a source of an undeclared kind is never
  cited.
- `id` is the reference the answer will write - an object id for `[text](tl:<id>)`, or whatever the
  application's own `CitationMarkerSyntax` finds in the text.
- Outside an agent run `readSource` does nothing, so a tool never fails for calling it.

