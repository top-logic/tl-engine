# tl-chat

Real-time agentic chat interface for TopLogic applications. Connects users to the multi-agent orchestrator (from `tl-ai-core`) and displays tool calls, agent handoffs, and AI responses as they happen.

**Parent:** `tl-parent-core` | **Dependencies:** `tl-core`, `tl-ai-core`

## Architecture

### Dual API: REST + SSE

Both served by `ChatServlet` at `/chat/*`:

1. **REST API** (JSON):
   - `POST /chat/send` – Send user message, starts background AI processing. Returns `{ taskId, status }`. Its `facts` are what the page states about the situation (`<chat-conversation><context>`), computed on the server and echoed back by the browser; the harness renders the stable ones into the run's system prompt and the message text stays untouched. `POST /chat/regenerate` carries them too.
   - `GET /chat/updates` – Poll for full chat state (messages).
   - `POST /chat/sessions` – Create new chat session.
   - `GET /chat/sessions` – List all sessions for current user.
   - `GET /chat/sessions/{id}` – Load session and activate server-side.
   - `PUT /chat/sessions/{id}/title` – Update session title.
   - `DELETE /chat/sessions/{id}` – Delete session (cascades over recorded executions, tasks, interactions).
   - `PUT /chat/preferences?chatId=…` – Per-user rail/pane layout of **one** chat. Its `chatId` is the key the layout is *stored* under, not the conversation the request comes from; the conversation travels beside it as `conversation=…`, like on every other call. The React UI cannot call `PersonalConfiguration` itself, so this wraps `setJSONValue` **and** `storePersonalConfiguration()` (transient otherwise). Key `tl-chat.layout`, holding one layout per `chat-id`. The body is what the user *decided*, so a fact they never touched is absent. There is no `GET`: the bootstrap travels with the control state (`ChatViewBinding.writeLayoutState`, re-read on every render), so a second round trip would only repeat it.

2. **SSE API** (real-time push):
   - `GET /chat/events` – SSE connection. `ChatSSEHandler` receives `AgentEvent`s and emits structured SSE frames: `connected`, `run_started`, `turn_started`, `text_delta`, `tool_started`, `tool_completed`, `approval_required`, `approval_resolved`, `interaction_resolved`, `message_complete`, `heartbeat`, etc.

`ChatSessionFilter` (mapped to `/chat/*`) requires a TopLogic session; `ChatServlet.doService`
additionally refuses anonymous ones with 401, because an application may hand a visitor a session
without a login. The chat has no anonymous mode: the chat controls pass `authenticated` to the
browser component, which shows a sign-in note rather than a composer. The note is the
conversation's alone - a `<chat-rail>` and a `<chat-dock>` render nothing at all, so a page states
it once (#643).

### Processing Flow

1. User sends message via `POST /chat/send` or TopLogic AJAX command
2. `ChatProcessor.sendMessage()` checks that the installation has room for the run (pool, per-account cap - both unlimited unless configured), adds the message to `Chat`, creates a `ConversationLog` turn, submits `AIBackgroundTask` to `ChatRunExecutor`
3. Background task resolves entry point agent from `AgentRegistry.getEntryPointAgentName()` and invokes it with `ConversationLogListener` bound for structured event recording
4. Agent events (tool calls, thinking, text deltas) are delivered to the SSE handler in real-time and recorded in the `ConversationLog`
5. `ChatProcessor.applyPollUpdates()` flushes pending messages into Chat
6. On completion, agent response added as `ChatMessage` and `ConversationLog` persisted

### Who decides that a call needs a person (#381)

`ChatProcessor.buildToolContext` no longer builds the run's `SecurityContext` itself. It asks
`RunSecurity.contextFor(new RunPrincipal(account, RunEntryPoint.CHAT, false, sessionId))`, and what
answers is the installation's configured `SecurityContextFactory` - by default the one backed by
TopLogic's roles and model access rights, which is what this line did before. A run whose tracker
says it has no window (#384) is announced as `SCHEDULED` and as a background run instead; everything
else on this path is the same.

An application substitutes that factory when its answer depends on **what** is being changed rather
than only on the tool's `ToolMode`: overwriting an empty section a machine wrote, overwriting what a
colleague wrote, and releasing a document to a customer are all `DATA_WRITE`, and only some of them
deserve an approval card. `ApprovalMode` cannot express that - it decides who answers a prompt, not
whether one is raised. The factory is asked once per run and returns a context whose
`checkApproval(Tool, ToolCall)` reads the call's arguments; extend `SecurityContextProxy` over
`DefaultSecurityContextFactory.defaultContext(run)` so that every other question keeps its answer.
Every other entry point - the TL-Script functions, the MCP endpoint, the prompt improver - goes
through the same call, so the substitution is one class and not five call sites. See
[the security architecture, 4.5](../doc/security/agent-security-architecture.md).

### Human-in-the-loop: a pending interaction belongs to the account (#393)

A run that needs a tool approval, or that asks a question through `askUser`, parks its agent thread
on a future in the `UserInteractionStore` of its own `AIProcessingState`. That state hangs on the
`TaskProgressTracker` of **one** conversation of one browser window, which the chat transport
addresses one at a time (`windowId` + `conversation` → sub-session → tracker). The request itself, though, is addressed to an
**account**: `InteractionRecorder` persists it as a `tl.ai.execution:InteractionRequest` with a
`responder`, and that is the rule this module follows everywhere.

| | |
|--|--|
| Listing what is pending | `GET /chat/interactions` – `AgentExecutionStore.pendingInteractionsFor(person)`, every chat of the account, whatever window asks. The client replays an approval **and a question** from it after a reload (`recoverRunState`). What the persisted row does not record - an approval's `toolMode`, a question's `multiSelect` and its parsed options - is read off the still-waiting run through `PendingInteractionRegistry`, so a rebuilt card asks what the agent asked (#466); nothing about the shape is persisted. A question row whose run is gone is therefore left out of the feed: its shape is unknown and an answer would reach nothing. |
| Answering | `POST /chat/respond` – the request id is looked up in `PendingInteractionRegistry`, among **this account's** running runs, not in the asking window's tracker. A second tab, a reloaded page and a new login therefore all reach the parked run. |
| Who may answer | The account named as the run's responder, and nobody else. Another account finds nothing and is told exactly what a caller naming a request that does not exist is told (`{"status":"ignored"}`), so the answer says nothing about which ids exist. |
| Order of effects | The decision is recorded (`ConversationLogListener.resolveApproval`, "approve for this session" onto the **run's own** tracker) *before* the future is completed. Completing it unparks the agent mid-turn, and it may call the next tool before this request has returned. |
| Telling the other windows | `interaction_resolved`, pushed to the account's other live chat UIs (`ChatEventHandlerRegistry.findOthers`). The run's own window is left out: it hears `approval_resolved` from the run itself. A chat UI says which account it is (`ChatEventHandler.getAccount()`) and takes the frame (`sendInteractionResolved`); both have no-op defaults, so a UI that never lists another window's requests is unaffected. |
| Timeouts | Unchanged, and still the backstop: `InteractionTimeoutService` bounds both waits (30 min by default), and `AgentExecutionStore` cancels every still-pending row at startup, because no run survives a restart. |

`PendingInteractionRegistry` is a process-wide register of the chat runs in flight, keyed by task id
and named by the account each run belongs to. `ChatProcessor` puts a run in when it creates the
run's interaction store and takes it out in the same `finally` that unbinds the store, so its
entries are bounded by the runs in flight. It is **not** what keeps a window alive - the run's own
background task already holds its tracker while it works. Its name is older than its second use
(#465, below), which is stopping a run rather than answering it.

### Stopping a run belongs to the account too (#465)

The same window barrier held for `POST /chat/cancel`, which started from the asking window's own
tracker: the stop button of a second tab, and of the page after a reload, silently did nothing while
the run went on spending the account's tokens to the end of its turns.

The client now names the chat it is stopping (`cancelRun(at, sessionId)` → `{"sessionId":…}`), and
the run is looked up by that chat among **this account's** runs
(`PendingInteractionRegistry.findBySession`). Three consequences:

- any window of the account stops the run, and the run's own state - its cancellation token, its
  thread, its log listener - is reached through the register rather than through the window;
- the stop hits the chat the user is looking at, not whatever else the asking window happens to be
  running;
- a chat of another account, and a chat with no run, answer the same `{"cancelled":false}`, so the
  answer says nothing about which chats exist or which of them are running.

A client that names no chat is answered exactly as before - whatever the asking window is running -
and so is a client whose body cannot be read: the stop button is the way out of a run that is going
wrong, and must not start refusing requests. The subagents of the run are cancelled through the run
window's `SubagentJobRegistry`, which a tracker only hands out for its *active* chat, so that step is
skipped when the run's window has moved on to another chat.

### Running an agent on a schedule (#384)

`com.top_logic.ai.chat.sched.AgentTask` is a configured `Task` of the engine's `Scheduler` that puts
one prompt to a named agent and records the run as a chat session of the `run-as` account. The prompt
is plain text or a TL-Script expression evaluated at the start of every run; the rest of the frame -
`name`, `enabled`, `schedules` - is the engine's own.

It goes through `ChatProcessor.sendMessage`, not through `AgentRunner`, and that is the whole point:
what an operator wants from a nightly agent is a **sitting** they can open in their chat history, not
an execution row. The task therefore does what a browser window would - a `TaskProgressTracker`, a
session created by `ChatHistoryService.createSession`, the `run-as` account bound to its sub-session -
and then waits for the run, skipping a start while the previous one is still working and interrupting
the agent thread when the scheduler stops it. It is not node-local: one cluster node runs it, because
an agent run writes and costs money.

**A scheduled run has nobody to ask,** and two things follow from that, set together by the task and
carried by `ChatProcessor.buildToolContext`:

| | |
|--|--|
| `TaskProgressTracker#isUnattended()` → `ToolContext#attended()` | `askUser` answers `AskUserTool.NOBODY_TO_ASK` at once instead of parking the agent thread until the 30-minute interaction timeout. Keyed on the context the entry point sets, never on what the prompt says. |
| `RunSecurity.contextFor(new RunPrincipal(account, SCHEDULED, background = true, ...))` | Approval is the compensating control for a write-classified tool, and a run that cannot show a prompt has lost it, so the run is additionally bounded by the policy's `<background-allowlist>`. The entry point the factory is told about is `RunEntryPoint.SCHEDULED` rather than `CHAT` (#381), because nobody typed this message. |

`isUnattended()` is deliberately **not** derived from the approval mode inside `ChatProcessor`: a user
who switches their own chat to `AUTO_APPROVE` has not left the room. Only a caller that knows there is
no window sets it - today, the task, and it sets it for `AUTO_APPROVE` only. A task configured
`approval-mode="MANUAL"` declares that a person will answer, which since #393 any window of that
account can, and is therefore an ordinary attended run started by a clock.

Approval prompts never blocked in `AUTO_APPROVE` - the callback is `ApprovalCallback.AUTO_APPROVE`,
which answers on the calling thread - so `askUser` was the one thing that could park an unattended
run. See `doc/scheduled-agent.md`.

### Key Classes

- **`ChatProcessor`** – Static utility: orchestrator execution, state sync, and the two pre-checks a send passes before anything is written down (pool room, per-account run cap)
- **`ChatRunExecutor`** – The pool the chat's agent runs execute on. Unbounded unless `<capacity>` on `AgentRuntimeService` (tl-ai-core) names a `chat-workers` ceiling, next to the per-account cap; with a ceiling a send that finds every worker busy waits, and it is refused only where `chat-queue` bounds the waiting room. See `tl-ai-core/doc/run-limits.md`
- **`TaskProgressTracker`** – Session-scoped chat state, manages processing states and session metadata
- **`ChatEventHandler`** – The contract a chat UI implements to observe a run: the `AgentEvent` stream plus the
  final-message and provider-error notifications `ChatProcessor` produces itself
- **`ChatEventHandlerRegistry`** – Interface-typed lookup of the chat UI observing a `TaskProgressTracker`;
  `ChatProcessor` resolves the handler here per event, so a UI other than the SSE servlet can register.
  `findOthers` is the account-wide direction: every *other* window of one account, for news that is
  the account's rather than one run's
- **`PendingInteractionRegistry`** – The chat runs in flight, keyed by task id and named by the account
  they belong to. What lets any window of that account answer a request the window that started the run
  is parked on, and stop the run itself - see "a pending interaction belongs to the account" and
  "stopping a run belongs to the account too"
- **`ChatSSEHandler`** – Push-based SSE implementation of `ChatEventHandler`
- **`Chat`** – Thread-safe in-memory container using `CopyOnWriteArrayList<ChatMessage>` (max 500)
- **`ChatSessionManager`** – Manages per-session in-memory state (Chat + ConversationMemory + ConversationLog) with LRU caching (max 5 sessions). Handles session switching with persist/load/rebuild cycle.
- **`ChatHistoryService`** – Static access to persistent `AgentSession` objects (kind CHAT) via `AgentExecutionStore` (tl-ai-core). Conversations are stored as one transcript per `AgentExecution` (written by `AgentRunner` at run end) and merged in run order on load.
- **`AgentTask`** (`…chat.sched`) – The scheduler task above. Not to be confused with
  `tl.ai.execution:AgentTask`, which is one entry of the plan an agent writes while it works
- **`ChatSessionRegistry`** – Bridges `WindowScope` (a chat control) and `HttpSession` (`ChatServlet`), keyed by `(sub-session, conversation)`: a window showing a page's chat beside a `<chat-dock>`'s has a `TaskProgressTracker` for each (#502). A caller that names no conversation registers and resolves the window's unnamed one. Published API: `tl-chat-classic` reads a classic window's id here, and `Consulting`'s own control registers here from outside this module.
- **`ChatViewBinding`** – Once per (sub-session, conversation): tracker, user, and the channels the agent may write there. The split elements (`<chat-conversation>`, `<chat-rail>`, `<chat-dock>`, the rail sections) delegate to it so several controls do not bind the window several times. The conversation is the `chat-id`; it is read as a key of its own because a chat that shared its conversation with nobody would key its layout and its tracker differently - the one-element `<agent-chat>` did, until #487 removed it. The layout preference is the exception: it is read whenever `writeLayoutState` is asked for it, which every chat control does from `onBeforeWrite` - see "where the chat's state lives". It is also where every join is tallied for `ChatJoinLog`.
- **`AgentViewRefreshService`** – Makes a model-bound element beside the transcript follow a running agent. A run commits from a background thread, and a window only sees model changes when its `GlobalModelEventForwarder` is asked to synthesize them - on a browser command or on the 30 s SSE heartbeat. The service listens on `AgentExecutionStore.ChangeListener`, coalesces a burst of writes (200 ms) and synthesizes per window, in that window's sub-session and **within the window's `Interaction`** (a `ReactControl`'s state map is a plain `HashMap`; the engine's SSE heartbeat synthesizes without it, and this deliberately does not copy that). The engine offers only a blocking `ReactWindowRegistry.beginInteraction()`, so `WindowInteraction` enters it on a thread of its own while the caller waits with a time limit, and calls the work off when the limit passes first - an agent thread must not wait for a browser command that may itself be waiting for the agent. **Coalescing never forgets a change**: a pass takes as long as it takes to enter every registered window and wait for its interaction, and a change that arrives while one is running is carried into the next pass rather than dropped - `PendingRefreshes` is the whole of that rule, and `TestAgentViewRefresh` is what holds it (#497). A window is registered with the account it is logged in as, read where a context is bound, because the pass runs on a scheduler thread that has none. Windows are registered by `ChatViewBinding` while a chat renders, which is a fork: the engine offers no way to enumerate an account's live windows. The same register and the same interaction serve the write direction: `inWindow` runs a caller's action in the window's sub-session within its interaction (after synthesizing, so the window knows the run's objects), and `hasWindow` is what lets a tool tell "this run is headless" from "this page allows nothing". A write waits longer for the interaction than a refresh does (`write-timeout-millis`, 5 s): a refresh that gives up loses nothing, a write has no second chance.
- **`ChatPresentTool`** – The `present` tool of a chat page (`com.top_logic.ai.chat.tools`), over the base `PresentTool` in tl-ai-core: the agent shows objects - found by a TL-Script query or named by id - as a table, a chart or a form on a channel of the page it is on. Registered from this module's own configuration, so every application that depends on `tl-chat` has the tool; **which** channel it may write is the page's decision (`<chat-conversation present="…">`, deny by default), and a run whose page displays no such channel is not offered the tool at all (#649). See "the agent writes a channel" below.
- **`PresentationPane`** – The pane of a `<chat-rail input="…">`: renders what stands on that channel for any type, and keeps the history "Zurück" walks. See "the rail's pane" below.
- **`ChatScope` / `ChatScopeElement`** – The `<chat>` container and the ambient value it puts into the child `ViewContext`. An element's conversation is its own `chat-id`, else the enclosing `<chat>`, else `default`.
- **`ChatJoinLog`** – Per render, which element joined which chat id; logged when the interaction ends, with a WARN for an element that joined nobody while another id has several.

## Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe message access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection and synchronized writer access

## Persistence Model

- **Module**: `tl.ai.execution` (tl-ai-core), **Types**: `AgentSession` (kind CHAT) + `AgentExecution` per run
- Session: `title`, `kind`, `owner`, `metadata` (approved tools, approval mode, tasks, skills), timestamps
- The approval mode in `metadata` is the chat's "auto-approve" switch, and only the switch writes it: `POST /chat/approval-mode` names the chat and stores the mode at once, and every other save of the bag keeps the stored mode, so no window writes back the mode it opened the chat with (#658). `GET /chat/sessions/{id}` answers it as `approvalMode`, which the switch shows.
- Execution: `runId`, `agentName`, `model`, `provider`, `status`, `stopReason`, token columns, `transcript` (CLOB)
- Transcript format: `ConversationLog` turns serialized via `ConversationLogSerializer` (tl-ai-core, `PersistedConversationV2`)
- Human-in-the-loop requests persist as `InteractionRequest` rows (approval/question/choice with resolution)
- The agent's task plan persists as `AgentTask` rows of the session. `TaskStore` (tl-ai-core) stays the truth of the running turn - it also carries `activeForm`, `owner` and the blocks/blocked-by sets, which the model does not - and `AgentTaskMirror` writes what it says onto the rows after every mutation, through `AgentExecutionStore.syncTasks` (one transaction, on the writer thread). The store is still serialized into the session metadata as well, because that is what restores it when a session is re-opened and what the SSE `task_update` frame and the App's own Fortschritt list read. So: the blob is the store's own persistence, the rows are the model a view binds to.
- Legacy `tl.ai.chat:ChatSession` single-blob sessions were migrated to this model eagerly at startup by a conversion service; both the legacy model and that service have since been removed (#487)

## Engine dependencies

The engine modules this one uses must be plain `compile` dependencies - never `optional`, never
`provided`. `tl-chat.conf.config.xml` registers services of this module unconditionally, and every
dependent that loads that configuration has to resolve their classes: `AgentViewRefreshService`
alone pulls in `ReactWindowRegistry` from `tl-layout-react`. An `optional` dependency does not reach
dependents, so the missing jar surfaces only when a downstream module boots the configuration, as a
`NoClassDefFoundError` out of the config reader (#415).

`tl-chat-classic` is that downstream module, and its own suite is what catches it, so after adding
or rescoping an engine dependency here run both suites:

```bash
mvn -o -pl tl-chat install -DskipTests=false
mvn -o -pl tl-chat-classic test -DskipTests=false
```

The second one needs the first: it resolves `tl-chat` from the local repository, not from the
reactor.

## Front end

The chat UI is a React application in `ui/`. It talks to the servlet above and to nothing else - no
TopLogic command handlers, no AJAX framework - so the same bundle would run against any host that
serves `/chat/*`.

This React chat is the only chat front end. A LayoutServlet application shows it in a classic tab
through the separate module `tl-chat-classic` (#618), which writes these same view elements into a
classic page; it replaced the hand-written `chat-client.js` control that lived there since #398. A
host gets the "Albert" tab by depending on `tl-chat-classic`; depending on `tl-chat` alone gives the
API and the React chat in React pages.

### Toolchain and build output

| | |
|--|--|
| Sources | `ui/src` (TypeScript, `.tsx`, `jsx: "react"` - the classic runtime, because React comes from the host) |
| Bundler | Vite library build, entries `ui/src/main.tsx` (`tl-chat-ui.js`) and `ui/src/index.ts` (`tl-chat-seams.js`) |
| Output | `src/main/webapp/script/tl-chat-ui.js`, `tl-chat-ui.css`, `tl-chat-seams.js` - **git-ignored build output, never commit it** |
| Node | installed by `frontend-maven-plugin` into `ui/node/` (v20.20.2 - 20.19 or newer, because jsdom 29 reaches `@exodus/bytes` through a `require()` of an ES module); a local Node 20.19+ works too. After a version bump, delete `ui/node/` once: the plugin swaps the binary but keeps the old bundled npm, which then fails with "Class extends value undefined is not a constructor or null". |
| Dependencies | `marked` (MIT) + `dompurify` (Apache-2.0 or MPL-2.0) for markdown, `highlight.js` (BSD-3-Clause) for code blocks - core plus 13 registered grammars. All three are bundled into `tl-chat-ui.js`, so their licences travel with the WAR. |

`mvn -pl tl-chat install` runs, in `generate-resources`: `npm install`, then `npm run typecheck`
(`tsc --noEmit`), then `npm run build`. The type check is a build step of its own because the Vite
library build strips types without checking them. `vitest run` comes later, in the `test` phase -
see below.

`tl-react-bridge` is the host's module: the build keeps it external and the browser resolves it
through a `<module-script specifier=…>`, so there is nothing in `node_modules` for a test run to
import. `vite.config.ts` therefore aliases it, for the vitest run only, to `ui/test/tl-react-bridge.ts`.
The stub hands out the **real** React (a dev dependency), so a test may render a component with
`react-dom/server` and assert on the markup - `chat-rail.test.ts` does. Everything else in it is the
smallest stand-in that behaves: the control state a test sets, a command sink it reads back, and a
marker element in place of a mounted child control. React must still never be imported from `react`
in `ui/src` - it is the host's instance, and a second copy breaks every hook; `chat-rail.test.ts`
pins that, which is what the missing dependency used to do.

The install, the type check and the bundle sit in `generate-resources`, because the bundle that goes
into the WAR must not be built from code that does not compile. **The `vitest run` does not** - it
is bound to the `test` phase, behind surefire (#450). Bound before `compile`, one broken frontend
test aborted the module, and the state of its ~190 Java tests was then not merely unreported but
unproduced: no `target/surefire-reports` directory existed at all. Behind surefire, a build always
has both answers, and a failing JS test still fails the build before `package`, so nothing that its
own tests reject is ever packaged or installed.

`-DskipTests` still runs the JS tests. The execution pins `skipTests` to `false` for that: the
frontend plugin honours `${skipTests}` for an execution in a testing phase, so without the pin the
move would have switched the JS tests off in every build that does not say `-DskipTests=false`,
which in this repository is the default. The flag is for the Java suites, whose KB setup is what
makes a build slow, while the whole vitest run is a second or two. What switches the frontend steps
off is `-Dskip.npm=true`.

`composer.test.tsx` is the one suite that mounts a component into a live DOM - `jsdom`, asked for by
that file's `@vitest-environment` docblock and by nothing else. It has to: what it guards only
exists between a real commit and the passive effects that follow it, and `react-dom/server` performs
neither. That is what the dependency buys, and it is why the run now takes ~3 s rather than ~2 s.

**The composer writes no state per keystroke.** A keystroke is a discrete event, so React commits it
on the microtask that follows; a `setState` that runs *after* that commit - from a passive effect,
typically - is a nested update, and fifty in a row with no idle frame in between is React's
"maximum update depth exceeded" (the minified error #185). A driver typing at zero delay, a paste,
an IME burst and a held key all produce exactly that cadence, so an effect of the composer's that
writes state on every `text` change makes the chat throw at the fifty-first character (#419). State
that follows the draft is therefore **derived** at render time, not stored and re-synchronised by an
effect; an effect may arm a timer, and the timer's own write is fine because it lands on an idle
frame. `composer.test.tsx` holds that line for typing, paste, composition and the "@" picker.

### Dev loop

```bash
cd tl-chat/ui
npm install
npm run typecheck          # tsc --noEmit
npm run build              # writes the bundle into src/main/webapp/script/
```

The bundle is served out of the `tl-chat` web-fragment WAR, so a demo application picks up a change
only after `mvn -pl tl-chat -am -DskipTests install` and a restart of the application.

### Embedding in the React view layer

The chat is composed from view elements: `<chat-conversation>` (transcript, composer, pickers),
`<chat-rail>` and `<chat-dock>`, all joined by their `chat-id`. Each control registers the
per-window `TaskProgressTracker` through `ChatViewBinding` and pushes the bootstrap state
(`windowId`, `userName`, `welcomeName`, `traceMode`, `promptImprovementEnabled`, `placeholder`) the
browser component reads. The one-element `<agent-chat/>` form was removed with #487: no application
used it, and the empty chat-id it needed was a special case in three live code paths.
The shipped browser component is registered as `TLAgentChat` in `ui/src/main.tsx`.

The session list is no element of this module's own: it is a `<data-list>` of
`tl.ai.execution:AgentSession` whose `selection` channel feeds `<chat-conversation input>`, which
is what `tl-ai-demo/chat-grouped.view.xml` configures. (The former `<chat-sessions/>` element was
deleted with #415 - `<data-list>` does the same job with no chat-specific code.) The agent's task
plan went the same way: the run mirrors it onto `tl.ai.execution:AgentTask` objects, so the
Fortschritt section is a `<data-list>` over those, and `<chat-progress/>` is gone. `useChat` is
**not** exported from `index.ts`: the store is internal to the bundle, so a host cannot write its
own element against it. New chat pieces live in `ui/src` and are registered next to `TLAgentChat`.

### Configuring `<data-list>`

`com.top_logic.ai.chat.view.list.DataListElement` / `DataListControl` + `ui/src/data-list.tsx`.
A **declared temporary fork**: the engine has no data-bound list that renders like the navbar with
selection, grouping and search, so the element lives here and is built on public engine API only, to
upstream as one clean diff.

| Option | What it is |
|--|--|
| `rows` | TL-Script producing the row objects, the way `<table>` binds its rows. Mandatory. |
| `<inputs><input channel="…"/>` | Channel values passed to `rows` as positional arguments. |
| `observed-types` | Types whose create/update/delete re-runs `rows`, so the list follows the model. |
| `<lines><line attribute=…\|<value>…` | What a row shows, top to bottom - a table's columns, stacked. `emphasis` is `primary` / `secondary`. |
| `group-by` | TL-Script of one row giving its group header; unset means a flat list. |
| `row-css-class` | TL-Script of one row giving extra classes on the row element - its *state* (`is-done`, `is-running`, `is-pending` are the ones the stylesheet defines). Called `css-class` up to 2.0.5; that name is now the engine's class of the list itself, and a row function written under it is refused at startup. |
| `css-class` | A class on the list's root element, as on every view element of the engine. |
| `selection` | Two-way channel holding the picked row object. |
| `count` | Channel receiving the number of rows, written with every serialisation - what a rail section's badge reads without asking the browser. |
| `search` / `collapsible` / `title` / `empty-text` | The list's own chrome. |
| `collapsed` | First-load state of a collapsible list; not remembered across page loads. |
| `drawer-below` | Host width in rem (default 44) below which a collapsible list becomes a drawer - see below. 0 keeps a column at every width. |
| `<commands><command>` | Header buttons above the list. Ordinary `<generic-command>`s. |
| `<row-commands><command>` | Commands offered on a single row - see below. |
| `row-channel` | Name of the local channel a row command reads its row from. Default `row`. |
| `row-actions` | `hover` (default) / `always` / `none` - when a row shows its trigger. |

#### Row commands

A `<row-commands>` entry is the same `<generic-command>` a header command is: label, image, clique,
tooltip, `<executability>` and any action chain. The row the menu was opened on reaches the command
through the **row channel** the list registers - which is what the command names in its `input`, and
what an `<executability>` rule therefore sees, re-evaluated each time the menu opens.

```xml
<data-list selection="chatSession" row-channel="session" row-actions="hover" …>
    <row-commands>
        <generic-command image="css:bi bi-pencil" input="session">
            <label><en>Rename</en><de>Umbenennen</de></label>
            <open-dialog dialog-view="tl-ai-demo/rename-session.view.xml" bind-input-to="model"/>
        </generic-command>
        <generic-command image="css:bi bi-trash" input="session" clique="delete">
            <label><en>Delete</en><de>Löschen</de></label>
            <confirm><expr>s -> …names the row…</expr></confirm>
            <execute-script function="s -> { aiSessionDelete($s); null; }"/>
            <write-channel name="chatSession"/>
        </generic-command>
    </row-commands>
</data-list>
```

What the element owns is the trigger; the **menu is the application's** (`ContextMenuOpener` /
`ReactMenuControl`), so its entries carry the app's typography, its cliques become separators, and
arrow keys, Enter and Escape already work. Two consequences worth knowing:

- A command's `css-classes` does not reach the menu - the opener emits id, label, icon and disabled -
  so a red "Delete" entry is not available without an engine change.
- Outside an `<app-shell>` there is no opener. The list then renders **no trigger** (one DEBUG line,
  no exception): a trigger that opens nothing is worse than none.
- The opener dispatches a menu command with the **frame's** `ReactContext`, which is not a
  `ViewContext` and does not hold this list's row channel: under it `<execute-script><inputs>` throws
  a `ClassCastException` and `<write-channel>` silently does nothing (the same is true of every
  `<context-menu>` in the engine). `<data-list>` therefore wraps each row command so it runs in the
  context it was configured in. Do not remove that wrapper.
- A channel that already holds a value notifies nobody, so reopening the same row's menu would show
  what its `<executability>` decided the first time. The control re-evaluates the rules explicitly
  before every open.

`row-actions` has the same meaning and the same touch caveat as `<panel hover-actions>`: `hover`
reveals the trigger on `.rail-item:hover` and on `:focus-within` (so the keyboard reaches it), and
under `@media (hover: none)` it is permanently visible at a 44 px target, because a finger cannot
hover. The trigger sits in space the row already reserves and `.rail-open` keeps `flex: 1;
min-width: 0`, so the title shortens rather than the row reflowing.

When the row whose menu was open disappears - what a Delete in that menu does - focus moves to the
neighbouring row's trigger (previous, else next, else the list), not to `<body>`.

#### A list in a narrow host (#548)

A `<data-list>` stands in a row beside columns that do not give way - in a `<chat direction="row">`
the conversation with its 20 rem floor (#511) and the rail's strip, plus the stack's gaps. Below the
width they need together (688 px in the demo) the row overflowed into the engine's scrolling
`.tlSidebar__content`: the composer started off-screen and the chat scrolled in two dimensions.

So below `drawer-below` rem of **host** width - the list's parent element, measured by a
`ResizeObserver`, never the window - a collapsible list is a **drawer** (`.is-drawer`): its strip
stays in the flow, and the existing expand arrow opens the list over its neighbour with a scrim.

- A row pick, a header command ("Neuer Chat"), a click on the scrim or Escape closes it. Focus moves
  to the open row (else the first) on open, back to the arrow on close, and Tab wraps inside.
- The scrim covers the host, not the page: the host becomes the containing block (`:has(> .tl-data-list.is-drawer)`),
  so the app bar and the page around a dock stay undimmed, and `isolation` keeps the application's
  menus above an open drawer.
- The user's column choice (`collapsed`) is not touched while the list is a drawer; a host that
  grows wide again gets the column back as the user left it.
- The conversation's floor gives way by `--chat-conversation-floor-reserve` (two strips and two
  gaps, 10 rem) while a list beside it is a drawer, so a 320 px window reflows instead of
  scrolling. Beside a list that is a column the #511 floor holds unchanged.
- `collapsible="false"` (every list inside a rail section) and `drawer-below="0"` are never drawers.

The default 44 rem is the demo row: list 16.5 + floor 20 + rail strip 3.5 + two gaps 3 = 43 rem.
It is an attribute rather than a constant because only the view knows what else stands in the row;
a host with less beside the list sets less.

**Tokens.** Anything the element's CSS uses must be declared under **both** roots: `.tl-cockpit …`
and `.tl-data-list`. A token declared only on the first is silently missing in a standalone list.
The same holds for the button reset, the focus ring and `box-sizing`.

Writing a session: a row command calls `aiSessionRename(session, title)` / `aiSessionDelete(session)`
(tl-ai-core `AIScriptFunctions`), never the `title` attribute. Those go through
`AgentExecutionStore`, which commits on its writer thread, bumps `lastUpdated` (the list's own
grouping) and tells the other windows; `aiSessionDelete` also asks a live run to stop and drops the
chat out of every window that holds it. Both refuse a session the acting account neither owns nor
administers.

The browser store keeps its own copy of the chat list, and the conversation's header and the
`App` rail read titles from it. The model events of such a write therefore reach the store through
`SessionListWatch` on `<chat-conversation>`: it counts a created chat of the account, a chat whose
title is not the one it saw last, and any deletion into the `sessionsRevision` state entry, and the
element re-reads `GET /chat/sessions` whenever that count moves (`useSessionsRevision`). Not every
update counts - a run writes its chat's bookkeeping several times per send, and counting those made
one send cost four list reads instead of one. Opening a chat also takes the title its transcript load answers with,
so a window that missed the count shows the current title as soon as the chat is opened (#596).

This module registers the bundle on `ClientResources` itself (`tl-chat.conf.config.xml`, #642), so
an application that depends on `tl-chat` needs no entry of its own:

```xml
<module-script name="tl-react-agent-chat" resource="/script/tl-chat-ui.js" requires="tl-react-bridge"/>
<stylesheet name="tl-react-agent-chat-css" resource="/script/tl-chat-ui.css" requires="tl-react-controls-css"/>
```

The `resources` list is keyed by `name`, so an application that still declares one of these names
overrides the entry rather than loading the bundle twice; it can delete its copy. Only a host that
composes `App` itself (see "Extending the chat") adds the seams module:

```xml
<module-script name="tl-chat-seams" resource="/script/tl-chat-seams.js" specifier="tl-chat-seams" requires="tl-react-bridge"/>
```

### Styling

`ui/src/styles.css` uses only the TopLogic React design tokens (`--background`, `--layer`,
`--text-primary`, `--border-subtle`, `--corner-radius`, `--shadow-menu`, ...), so the chat follows
the theme the app-bar's `<set-theme>` buttons select. Tokens the shipped themes do not define yet
are aliased once at the top of the file with a fallback that is correct in both themes; add new ones
there rather than hard-coding a colour.

### An element measures itself, not the window

Every responsive rule in `ui/src/styles.css` that is about *how much room the chat has* is a
`@container` query, not a `@media` one (Issue #427). A chat placed by an application almost never
owns the window - a shell's navigation and the page gutters take their share first - so a viewport
query asks a box the chat does not have: it stayed wide while the conversation was 234 px, and the
narrow layout arrived hundreds of pixels late or never.

Two containers carry all of it:

| Container | On | What it decides |
|--|--|--|
| `chat-shell` | `.tl-cockpit` - `App`'s root, i.e. a host composing the whole chat | Session rail: column or drawer over the thread (≤ 1100 px). Artefact pane: column or sheet, and the icon strip (≤ 1279.98 px). |
| `chat-stage` | `.stage` - the transcript plus the composer, in App **and** in a composed `<chat-conversation/>` | Thread gutters (≤ 1080 px), Enter hint long/short (≤ 35 rem), the auto-approve label (≤ 19 rem), the model name on its picker (≤ 18 rem), and whether the composer's controls wrap (≤ 16 rem). |

Rules to know when touching this:

- **The stage, not the cockpit, is what the composer measures.** The cockpit stays as wide as ever
  while the rail and the artefact pane take their columns. Two class hacks used to stand in for that
  width - `.tl-cockpit.has-pane` (short Enter hint) and `.tl-cockpit.has-document` (no auto-approve
  label) - and neither ever reached a composed `<chat-conversation/>`, which has no cockpit at all.
  Both are gone; `has-document` survives only for the one rule that is about a document being open
  rather than about a width.
- **`.tl-chat-rail`, `.tl-data-list` and `.tl-chat-dock__body` are deliberately not containers.** The
  rail is as wide as its own strip plus column, so inline-size containment - which is what
  `container-type` applies - would leave its contents contributing nothing and collapse it to 0 px.
  The other two have nothing to ask: a list is as wide as the column it was placed in, and what the
  dock's card size has to reach is the conversation inside it, which measures itself through
  `.stage`.
- **One number, one measurement, for the sheet.** `SHEET_MAX` in `side-pane.tsx` (1279.98) and the
  `chat-shell` container query are the two halves of the same decision, because whether the pane is
  a sheet is partly React state (the scrim, the splitter, the pane's own width). `App` measures its
  root with a `ResizeObserver` and hands `narrow` down to `SidePane`; nothing in the bundle reads
  `window.matchMedia` any more. Keep it that way - a JS half that asks the window and a CSS half
  that asks the element disagree at every width where it matters.
- **What stays a media query, and why.** `@media (hover: none)` and
  `@media (prefers-reduced-motion: reduce)` are about the person's input device and their
  preference, not about a box. `@media (max-width: 1100px)` on `.plus-popover` / `.model-popover` /
  `.context-popover` / `.activity-panel` makes them full-bleed sheets on the bottom edge, which is a
  phone idiom and so genuinely the screen's business - note only that `position: fixed` there now
  resolves against `.stage`, because a query container is the containing block of its fixed
  descendants, which is what puts the popover over the chat rather than over the application.
- **Sizing inside a container query uses `cqi`, not `vw`** - the drawer's `min(20rem, 88cqi)` is 88 %
  of the chat, which is the width it is spilling out of.

**Browser support.** `container-type`, `@container` and the container query units (`cqi`) are
Baseline **Widely available**, across browsers since **February 2023** (MDN). They raise no floor
this product did not already have: the engine's React layer and this application's own shell are
written with `:has()` - `.tlStack:has(> .tlPanel--fill)`, `button:has(.bi-moon-stars)` - which is
Baseline Widely available only since **December 2023**, ten months later. The repo's own browser
tests run current Chromium (`tools/web`, Playwright); there is no listed minimum browser beyond
that.

### What the agent picker shows, and what it filters

Two fields of the agent definition are written for a person and are the only ones the UI shows:
`display-name` (what the agent is called - `TL_SingleAgent` is "Albert") and `summary` (one line
saying what it is for). Both are localizable, both live on the definition - in
`tl-ai-core.conf.config.xml` for the agents this product ships, or in the agent record for one an
installation authors - and both fall back to nothing: an agent with no display name is shown by its
technical name, one with no summary by its name alone. The agent's `description` is a delegation
instruction addressed to a model and never reaches the browser.

The summary is rendered **as authored** and **whole**: under the row's label, wrapping onto as many
lines as it needs (#635). The same holds for every hint of the "+" menu and the model picker. A hint
stands under the label and the value rather than beside the value, so how much room it has never
depends on the value - a longer model name used to cut the sentence off again. Each row is named by
its label and value and described by its hint (`aria-labelledby` / `aria-describedby`), so a screen
reader hears the whole sentence too. A summary is still best kept to one line where it is written,
because the menu is easier to scan that way; nothing breaks when it is not.

**Where an installation changes them (#403).** For the agents this product ships, both texts are
configuration, and the place to change them is *Administration → Development → Service Editor →
TopLogic-Agents and Skills*: each agent's entry now begins with **Display name** and **Summary**,
above the model-facing description, with a field per language and the shipped text already in it.
The technical `name` above them is not editable there, and that is the point of having a display
name at all: other configuration, the skills, the security policy and every persisted session name
the agent by its identifier, so renaming that is a migration while renaming the display name is a
setting. Two things about the mechanism are worth knowing before using it. A saved change reaches
users only when the service is restarted - *Administration → System → Services → Restart*, or the
next application start; saving alone changes nothing a running agent does. And the editor saves the
configuration of **every** agent as it currently reads, into
`WEB-INF/autoconf/com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.config.xml`, so from then
on the installation carries its own copy of the shipped prompts and tool lists too; *Reset
configuration* in the same editor deletes that file and gives the shipped texts back at the next
restart. An installation that wants to change two texts and keep receiving the rest of every release
writes that overlay file by hand instead, naming only the two properties it replaces - which is what
the configuration overlay merge is for, and what `TestShippedAgentOverride` pins.

`GET /chat/capabilities` is filtered by the acting account (#392): the agents and the MCP servers it
lists are exactly what a run of that account accepts. The **models** are the one thing a single
payload cannot make exact - it is fetched once and the agent changes without another round trip - so
it lists what the account may choose with *some* agent it may run and marks each with
`grantedToRole`; `modelsFor()` in `models.ts` narrows that to `granted ∪ {selected agent's model}`
before the picker sees it. A request that names something the account may not use is answered **403**
with a sentence in the user's language, which the composer shows in the notice strip in the neutral
tone.

`entryPointAgent` is resolved for the caller, not read from the configuration: a client that names no
agent runs the agent the payload reports. The prompt improver is exempt from the agent allowlist -
it rewrites a draft and never answers as a persona.

### Module map

| File | What it owns |
|--|--|
| `App.tsx` | The shipped chat as a composition over one `useChat` store, plus this element's own geometry |
| `chat-conversation.tsx` | `<chat-conversation/>` as `TLChatConversation`: transcript plus prompt field over the same store |
| `chat-dock.tsx` | `<chat-dock/>` as `TLChatDock`: the floating launcher and the panel it opens over the page |
| `chat-rail.tsx` / `rail-model.ts` | `<chat-rail/>` as `TLChatRail`: the side-rail shell - strip, section headers, pane chrome - and how it reads its state |
| `chat-sources.tsx` | `<chat-sources/>`: the one rail section the chat still contributes as an element of its own |
| `data-list.tsx` | `<data-list>` as `TLDataList`: the generic list - rows, lines, groups, search, selection, per-row state class |
| `chat-store.ts` | The chat's state, one store per `chat-id`: bootstrap, capabilities, sessions, turn settings, transcript, run, layout preferences and the one `EventSource` |
| `composer.tsx` | Input, plus menu, model picker, attachments, drag and drop, improve-prompt |
| `thread.tsx` | Transcript: user bubbles, answers, run meta line, trace panels |
| `subagent-card.tsx` | One card per background subagent at the tool call that started it, from the live events (`subagentLive`) or, after a reload, from the lead's recorded `SubagentPart`; `cardsForTurn` decides which cards a turn shows |
| `context-usage.tsx` | The context ring and its popover, plus how each number is derived |
| `prompts.tsx` | Approval and question cards above the composer, with their countdowns |
| `markdown.tsx` / `highlight.ts` | Markdown rendering, sanitizing, code blocks |
| `api.ts` | Every call to `/chat/*`, plus the persisted-transcript to message mapping |
| `stale-window.ts` | `chatFetch`, which every `/chat/*` call goes through: a `409 stale_window` (the server does not know this page's window, e.g. after a login in another tab) reloads the page once, under the engine's own reload guard, and the text of a refused send waits in `sessionStorage` for the reloaded composer (#574) |
| `reducer.ts` / `store.ts` / `types.ts` | Transcript reducer, the `runStore` snapshot a host may subscribe to, shared types |
| `extensions.ts` | Host composition contract (`ChatExtensions`) |
| `rail.tsx` / `side-pane.tsx` / `progress.tsx` | `App`'s own rail, the artefact pane, the Fortschritt list |
| `theme-icon.tsx` | A `ThemeImage` (`css:bi bi-…`) as an element, for configured icons |
| `working-indicator.tsx` | What stands in the answer's place while the agent works: the dots, or the element's `working-indicator` |
| `token-input.tsx` / `reference.ts` | The composer's inline reference tokens and how they travel |
| `sources.tsx` | The cited-source model, the `Quellen` section and the per-answer pill |

## Extending the chat

The shipped element is `TLAgentChat` (`ui/src/main.tsx`). A host that composes the chat registers its
own element and points `<chat-conversation react-element="MyChat"/>` at it. The control is still the
only binder of session, tracker and prompt-improver; the browser component still talks to `/chat/*`.

```js
import { register } from 'tl-react-bridge'
import { App } from 'tl-chat-seams'

register('MyChat', (props) => <App {...props} extensions={myExtensions} />)
```

`App` with no `extensions` is today's UI: session rail, thread, composer, send/stream/stop, inline
edit, regenerate, attachments, approval, pickers, and the framework's own two right-rail sections.
Fortschritt lists the agent's tasks (`createTask` / `updateTask`) and marks the running step as
waiting while an approval is open; Kontext names what the agent receives every turn. The rail never
opens by itself.

### Seams

Every field of `ChatExtensions` is optional.

| Seam | Default | Host |
|--|--|--|
| `railSections` | Fortschritt (this session's `createTask` / `updateTask` store, plus an open approval) and Kontext (language, agent, model, chat) | Extra sections between them, in array order. `accent` tints with `--button-primary` / `--layer-selected`; `icon` is what the 56 px strip shows. |
| `contextFacts` | language, agent, model, chat | Extra lines in the Kontext section. |
| `composerAddons` | none | `referenceCandidates(query)` is what typing **@** offers, `extraMenuItems` adds rows to the `+` menu (their `action` gets the `ComposerApi`), and `placeholder` is the host's own invitation for the empty field - see below. |
| `messageAnnotations.render` | web-link popover | May call `openInPane` (invoice kind in the demo). |
| `messageActions` | copy / rate / regenerate | Extra buttons under an answer. |
| `contextHook` | none | `header` in the thread bar; `contextTag` prepended as `<context>…</context>` on send and regenerate. |

The side pane is **not** a field of `ChatExtensions`: it is state the chat owns, and a host drives it
through the `RailRenderContext` every section's `render(ctx)` receives -

| `ctx` | |
|--|--|
| `sessionId` / `messages` / `busy` / `pendingApprovals` | The open chat, its trace, whether the agent is working, and what waits for this user. |
| `openInPane(node, options)` | Replaces the rail with the host's node in the same place; `options` carries the title, the back label, the host's tabs and actions, the section whose strip icon is active, and `mode`. |
| `closePane()` | Back to the rail. |
| `insertReference(token)` | Puts an atomic reference token at the composer's caret, e.g. from a passage the user marked in the pane. |

Pane geometry: `mode: 'card'` (the default) opens at 420 px with a 360 px floor; `mode: 'document'`
takes about two thirds of the workspace, as far as the thread's 240 px floor allows, under a slim
header the host fills (`title`, `version`, `actions`, close). The workspace is a grid, so opening the
pane narrows the thread rather than covering it; the splitter is draggable and arrow-key operable,
and a width dragged in one mode is not imposed on the other. **Below** 1280 px both modes are a
sheet over the thread.

### The composer's invitation

The empty field says "Frag mich etwas …", which is informal; an application whose every other
surface addresses the user with *Sie* then has one "du" on its most-read control (#420). Both paths
into the chat state their own sentence, and both feed one `Composer` prop:

| Path | How |
|--|--|
| `<chat-conversation>` | `<placeholder><en>…</en><de>…</de></placeholder>`, or the key of a text in the application's bundle. The control resolves it in the acting account's language (`ChatViewBinding.resolveText`) and pushes the words, never the key. |
| A host composing `App` | `extensions.composerAddons.placeholder`, already localised. |

A configured `<chat-conversation placeholder>` wins over the seam: it is the installation's
decision, the seam is the host code's default.

Two rules about it:

- **It is the idle invitation only.** While the agent works the field keeps saying "Nächste
  Nachricht schreiben …", because that sentence is about the run rather than about the application,
  and a host that replaced it would be inviting the user to start what is already under way.
- **It replaces both framework variants**, the one that offers `@` included. A host that wants its
  sentence to mention `@` writes that into its own text.

Before this the only way in was CSS - a host put its own text in front of
`.token-input.is-empty::before { content: attr(data-placeholder) }` with a custom property. That was
a host reaching into a framework pseudo-element; there is no reason to do it any more.

### The working indicator

While the agent works and has written nothing yet, the answer's place holds three pulsing dots
(`PendingAnswer` in `thread.tsx`, #429). An application puts its own animation there - a logo, say -
with one attribute:

```xml
<chat-conversation working-indicator="/images/app-working.svg"/>
```

It is a `ThemeImage`, named like every other icon of the chat, in one of three forms:

| Form | Rendered as | Light / dark | Reduced motion |
|--|--|--|--|
| `/images/….svg` | **Inline** SVG, fetched once per page (on mount, not on the first send) and passed through DOMPurify's SVG profile - scripts and event handlers go, `<style>` and SMIL stay | Draws in `currentColor` (the secondary text colour) and the theme tokens (`var(--button-primary)`), so one file fits both themes | The chat's reduced-motion rule reaches into it; SMIL is paused on its first frame |
| `css:app-working` | `<i class="app-working">` | The application's stylesheet draws it and may pick an image per `[data-theme]` | The chat's reduced-motion rule |
| any other image | `<img>` | As it is | Cannot be stopped from outside - prefer the other two |

- **Size**: the height of the first text line, `--chat-working-indicator-size` (1.5rem). The token is
  read with a fallback, not declared on the chat's roots, so `:root { --chat-working-indicator-size: … }`
  works.
- **An image travels with the context path** (`ChatConversationControl.workingIndicator`). Its encoded
  form is the path inside the application, which a browser resolves against the server root - wrong
  for every application not deployed there, which is also why `ThemeIcon`'s image branch is not used.
- **An inline SVG's `<style>` applies to the whole page**: prefix its classes. A file that fails to
  load, or is not one `<svg>` (a login page answering the request), keeps the dots with one console
  warning.
- **Decorative**, like the dots (`aria-hidden`): the "Arbeitet …" line and the thread's live region say
  what a screen reader needs, whatever is configured.
- **Only the answer's place.** The "Arbeitet …" / "… gearbeitet" line, the streaming caret and the
  dock launcher's busy mark are unchanged.
- The option is on `<chat-conversation>`, so a `<chat-dock>` gets it on the conversation nested in it.
  `App` (`TLAgentChat`) reads the same state.

`tl-ai-demo`'s Chat page shows `images/tl-ai-demo-working.svg`, a neutral example; its dock keeps the
dots. `working-indicator.test.tsx` and `TestChatWorkingIndicator` pin both.

While the latest step of a running run is thinking, the collapsed run line says what the model is
thinking about - "Nachdenken: " and the start of the newest paragraph of the thinking summary - in
place of "Arbeitet …" (#602). A model can think for minutes before its next step: Claude Opus 5.5
thought for almost four on an intake of five documents, and a line that only said "Arbeitet …" for
that long read as a hang. The summary exists only because `AnthropicRequestBuilder` asks for
`display: "summarized"`; the API default on the models since Opus 4.7 streams thinking blocks with no
text. `thread.test.ts` pins the line.

### The composer's reference tokens

A host's quoted passage is an **inline token inside the text**, not a chip above it: "shorten
[this] and expand [that]" needs two references at two positions in one message, and a chip cannot
say which is which. The token is `{kind, id, label, location, text}`; the caret steps over it,
Backspace takes it whole, and a screen reader reads it as one named thing.

On send it serialises **at its position** to `<quote label="…" ref="…" location="…" kind="…">the
quoted text</quote>`, so the model reads the reference inside the sentence that is about it.
`location` and `kind` are written only when the host set them. The thread renders a sent message
through the same parser, so the reference reads back as the token the user inserted, and the inline
editor edits it as one.

Two ways in: `ctx.insertReference(token)` from a host pane - which inserts at the caret, restoring
where the user last stood, because by then the editor has lost the selection - and typing **@** in
the composer, which offers what `composerAddons.referenceCandidates(query)` returns. There is no
prefix form and no chip list.

**/** types a slash and opens nothing. It is reserved for skills, the way it is in every chat
product; the agent picker lives in the **+** menu, which is where it was reachable all along
(#416). The skill picker itself comes with the skills work.

**Is `ref` resolvable?** For a model object - what `@` and the `reference`/`quote` channels insert
from an object - yes (#616): `ref` is the `TypeName:id` every tool reads (`ReadFilter.idOf`) and
`kind` the qualified type, so the agent answers from label and kind where they suffice and reads the
object with `getInstance(ref)`, a core tool of Albert, where they do not. The `<reference>` prompt
fragment (`tl-ai-core.conf.config.xml`) says so. A host that writes an id of its own as text is the
other case: tl-ai ships no tool that takes it, so **such a host must put everything the model needs
into the element's text**, or give its agent a tool that accepts that id.

### What an answer's citations look like

The answer cites with links in its text - `[Sommerfest](tl:GenericObject:11047)`,
`[laut Programm](https://...)` - and calls no tool for it (#617). The system writes the reading
list: every read tool registers what it read on the run's `ReadingList`, and the validator turns a
link to a source on that list into a footnote and drops the link of anything else. A sub-agent's
sources join the lead's list when its result is delivered.

A footnote is a small grey number after the link's text. Hovering (or focusing) it shows the
source's kind icon and label, where it comes from and which one it is (the page's path, or the
record's id), and what the source says: prose with the cited passage highlighted - the link's title,
kept only when what the tool read really contains it - or, for a kind declared `passage="false"`,
the record's fields as a field list. A web page also offers its **archived copy**
(`/archived-page/{handle}`), kept when the agent read it, so the citation still shows what the page
said after it changed. Clicking opens the source: `messageAnnotations.render` decides that for a
host kind (returning nothing leaves the web default, which opens the page).

**Kinds are declared, not invented.** The framework knows `web` and `object`; a host declares its
own kinds once through the `CitationKinds` service (name, `types` its objects belong to, label,
whether a passage applies). The model never names a kind - the reading tool decides it - and the
validator drops a citation of an undeclared kind at DEBUG. A host that cites another way than with
links registers a `CitationMarkerSyntax`. See `doc/citation-contract.md`.

Under an answer sits a collapsed `Quellen · n zitiert` pill with that answer's sources. The rail's
`Quellen` section is the session-wide list: every source cited in this chat, with the marker numbers
that point at it - a number scrolls to its answer, the row opens the source, and a filter appears
once the list is long. `sourceTabs` puts a host list beside it as further tabs.

Its **badge** is not the length of that list: it counts what the *newest answer* cited
(`latestAnswerSources`). A badge over a list that only grows sat on the Quellen icon for the rest of
the conversation and could not be dismissed (#495); an answer that cited nothing now carries none.

Layout preferences (`railOpen`, `paneWidth`, `badgeSeen`, the per-section collapse) are bootstrapped
from PersonalConfiguration via `ChatViewBinding.writeLayoutState` and written through
`PUT /chat/preferences`.

The Leiste badge is the sum of each section's badge minus the total last stored when the rail was
opened - "N new since you last looked", so a section that reports nothing contributes nothing. In
`App` the sections are `RailSection` objects and the sum is computed in place; with a composed
`<chat-rail>` the sections are separate view elements, so the rail publishes the sum into the store
(`badgeTotal`) and `<chat-conversation>`'s button reads it there.

### Where the chat's state lives

Everything the conversation *is* lives in `chat-store.ts`, not in a component: one `ChatStore` per
`chat-id`, held in a module-scoped map and subscribed with `useSyncExternalStore` through
`useChat(chatId, boot)`. The whole chat bundle is a single ES module instance in the browser
(`tl-chat-ui.js` and `tl-chat-seams.js` are shims over one `tl-chat-shared.js`), so that map is one
object for every chat element on the page - which is what lets several view elements show one
conversation.

| | |
|--|--|
| What the store owns | The bootstrap, the capabilities, the session list and the open session, the turn settings, the transcript (through `chatReducer`), the run, the layout preferences, and the chat's one `EventSource` |
| What a chat element keeps | Its own geometry and its host composition: for `App` the session drawer, the collapsed rail, the sheet breakpoint, the published `--composer-height`, and `ChatExtensions` |
| Lifetime | The first element to subscribe opens the stream, the last to leave closes it and drops the chat - a fresh mount starts as fresh as it did when `App` owned the state |
| `PUT /chat/preferences` | Written by the store and by nothing else, so several elements on one chat cannot race on the `tl-chat.layout` key |

#### What survives a reload, and who decides it

A browser reload does **not** rebuild the control tree: `ViewServlet` re-renders the tree the window
already holds, deliberately, so that a table keeps its selection. A control constructor therefore
runs once per window, not once per page load. Everything the chat wants a reload to restore has to
be read at **render** time, and every chat control reads the layout in `onBeforeWrite`
(`ChatViewBinding.writeLayoutState`).

| | |
|--|--|
| Who writes the layout | The browser store, through the one `PUT /chat/preferences?chatId=…`. Nothing else writes it - not a control, not an element. |
| Who reads it | `ChatViewBinding`, per render, into the control state the store boots from. There is no `GET`. |
| Where it is stored | `PersonalConfiguration`, key `tl-chat.layout`, **one layout per `chat-id`**: a `<chat-rail chat-id="grouped">` and a `<chat-rail chat-id="review">` are different places with different sections. The key is prefixed (`chat:`), a stored format that must not change. |
| What is stored | What the user *decided*: `railOpen` (absent while they never toggled it), the pane width they dragged to (whole pixels, written once when the pointer lets go - #588), `badgeSeen`, and a per-section-name open flag for each section they toggled. Never what a page happened to show. |
| Precedence | The configured defaults - `<chat-rail open>`, `<section open>`, `<chat-rail pane-width>` - apply **only** where the user has decided nothing. Once they toggled it, their choice wins on reload and in a new tab, and the configured flag no longer applies to them. Per section name, so a section added later still starts as configured. |

The store never writes back the layout it booted with (`sameLayout` against `storedLayout`). Before
#423 it did, and that is what destroyed the stored choice: a reloaded page booted from the first
render's control state and then persisted that stale value.

`applyRailDefault` / `applyCollapsedDefaults` are how `<chat-rail>` states its configuration. They
are idempotent - they only touch what the user has not decided - so a remount, or a second rail,
re-states the same defaults and changes nothing.

Run state is recovered the same way, but over REST rather than the control state: the SSE stream
replays nothing, so on restoring or selecting a session the store also reads `GET /chat/updates`
(the tasks of the open run, and whether it is still processing) and `GET /chat/interactions` (the
approvals and questions waiting for this user). A reload in mid-run therefore comes back with the
Fortschritt list, a busy composer and the open card - the approval, or the question the agent asked,
with the options and the single/multi shape it was asked in.

`chat-id` is how elements find each other, and **`<chat>` is where it is written**. A page puts its
chat elements inside one `<chat chat-id="...">`; each of them inherits that id through the view
context (`ChatScope`, `ChatScopeElement`) and carries no `chat-id` of its own. That is the normal
form, because the alternative - the same string repeated on every element - makes the strongest
coupling on the page the least visible thing about it: a typo in one attribute opens a second, empty
conversation and nothing in the product says so.

```xml
<chat chat-id="chat" direction="row">
    <data-list selection="chatSession" …/>          <!-- joins by channel, not by chat-id -->
    <panel fill="true"><chat-conversation input="chatSession"/></panel>
    <chat-rail input="paneObject">…</chat-rail>
</chat>
```

`<chat>` is a `<stack>` that carries an id: same `direction`, `gap` and `align`, no chrome of its
own, and any element may sit inside it. **Put it where the stack was** rather than adding a level:
the engine's `.tlStack:has(> .tlPanel--fill)` rule looks one level deep, so a `<panel fill="true">`
that stops being a direct child of the page's stack loses its height.

That panel is there for the height alone and carries no title. Its header, which the engine draws
anyway (padding, a tinted band and a border line), is hidden by a rule in `styles.css` for as long
as it holds no title, no title content and no command (#589). A host that gives the panel a title
or a command keeps its header.

`<chat-dock>` is the one element that **repeats** the id rather than inheriting it, because it
*contains* the `<chat>` instead of sitting inside one and a scope only reaches descendants:

```xml
<chat-dock chat-id="assistant" icon="css:bi bi-stars">
    <label><en>Assistant</en><de>Assistent</de></label>
    <chat chat-id="assistant" direction="row">…</chat>
</chat-dock>
```

The two must agree. A mismatch is not silent: `ChatJoinLog` warns about a window whose chat ids do
not line up, and a dock joining a chat nobody else is on is exactly that shape - the dock's launcher
would then count news from a conversation it does not contain.

`chat-id` on a child element is the **override**, for the rare page that holds two conversations;
it is nullable, and left empty an element outside every `<chat>` joins `default`. A host that binds
with no chat-id at all gets `App`'s fallback to `props.controlId`, so two chats in one window stay
two chats - Consulting's `CopilotRegistry` keeps a *list* of live panels, and a fixed key would
silently merge their transcripts (`chat-store.test.ts`,
`testTwoAppsInOneWindowDoNotShareATranscript`).

**The join is logged.** `ChatJoinLog` tallies, per render, which chat id each element joined and
writes one INFO line for it. When a window holds more than one chat id and one of them has a single
element, it adds a WARN naming that element - the shape a typo produces. A page with one chat id
never warns, however many elements are on it.

`<chat-conversation>` is the transcript and the prompt field as one element - the transcript and
the input almost always sit together, so they are one placeable box, while the session list and the
rail stay separate because those genuinely want independent placement. It honours its configuration
flags as render guards on the existing `Thread` and `Composer` props.

#### The nine channels of `<chat-conversation>`

| Channel | Direction | Value |
|--|--|--|
| `input` | two-way | The open `tl.ai.execution:AgentSession`, as the object. Empty means a fresh conversation, so "new chat" is the list clearing it. |
| `subject` | in | The object a conversation *created here* is about, written onto it as `AgentSession#subject` - see below. |
| `busy` | out | `Boolean`, `true` while the agent works. |
| `agent` | two-way | The agent's **name** (`String`). |
| `model` | two-way | The model's **name** (`String`). |
| `effort` | two-way | The effort level (`String`), one of the levels the selected model offers; empty for none. |
| `mcp-servers` | two-way | The attached servers' **names**, a `Collection<String>` (one name on its own is read as a set of one). |
| `draft` | two-way | The text standing unsent in the composer (`String`, or a `ResKey` on the way in). |
| `reference` | in | A model object, inserted into the draft as one atomic reference token. |
| `quote` | in | A marked passage *of* an object, as the token itself: `{id, text}` and optionally `{label, location, kind}`. |

`present` is not an eleventh: it is a *list* of channels this element opens to the agent rather than
a channel the element itself reads or writes - see "the agent writes a channel" below.

##### `subject` - what a conversation is about (#508)

A host whose page is about one object at a time - a project, an invoice, a contract - wants one
conversation per object, and before #508 it had to remember which conversation that was somewhere
of its own. `tl.ai.execution:AgentSession#subject` is where that now stands, and this channel is
what writes it:

```xml
<chat-conversation input="chatSession" subject="project"/>
```

| | |
|--|--|
| When | Once, when the user's first message brings a conversation into being. Which of the two a reported session is - created here, or opened - is the **browser's** answer (`sessionReport` in `chat-conversation.tsx`, carried as `created` on `publishSession`): the server sees an id arrive either way, and a reload re-opens what this window had. |
| Never | On the way in. Which conversation is shown stays `input`'s business, and which ones the user may pick stays the `<data-list>`'s. A conversation opened beside another object is not moved there, and one started while the channel was empty is not given a subject later - `AgentExecutionStore.setSubject` refuses a session that already has one, and no path backfills the rest. |
| What counts | One model object, or a one-row selection. Several objects name no single subject, and nothing on the channel means the page was showing nothing; both stamp nothing (`ChatConversationControl.subjectFor`). |
| Scoping the list | The `<data-list>`'s own `rows`, by navigating back - which is the point of a reference rather than a remembered id (see below). `referers` is not a security boundary, so the owner filter stays. |
| A deleted subject | Clears the reference (the default deletion policy): the conversation outlives the object it was about, and it drops out of that object's list. **But navigating back from the deleted object itself throws** - `TLObject.tReferers`, which TL-Script's `referers` calls, refuses a lookup from a deleted object, and TL-Script has no "is this still there" to guard with. So the expression below answers while the channel is empty and while it holds a live object, and fails in the window where it still holds one that was deleted meanwhile. That window is not this feature's: a `<form>` or a `<table>` over the same channel is in the same position, and a page that must survive it asks the sessions (`all(...).filter(s -> $s.get(...) == $x)`) instead of the object. `TestSessionSubject` pins both halves. |

```xml
<rows>project -> ifElse(isEmpty($project), list(),
    $project.referers(`tl.ai.execution:AgentSession#subject`)
        .filter(s -> $s.get(`tl.ai.execution:AgentSession#owner`) == currentUser())
        .sort((s -> $s.get(`tl.ai.execution:AgentSession#lastUpdated`)).comparator().desc()))</rows>
```

`tl-ai-demo/chat-grouped.view.xml` configures both halves: the conversation stamps what it creates
with the invoice the rail's pane is showing, and the session list on the left offers that invoice's
chats while one is open, and every chat of the account while none is.

The four turn settings carry **names**, not model objects, and that is a fact about this product
rather than a shortcut: only an agent an installation authored in the knowledge base is a
`tl.ai.core:Agent` at all - the agents tl-ai ships, every model and every MCP server are
configuration - while a name is what `/chat/send`, `AgentFactory` and the security policy are
written in. On the way *in* a `tl.ai.core:Agent` record is read for its `name`, so a `<data-list>`
of authored agents feeds the `agent` channel; on the way *out* the name is written, so such a list
is not highlighted back. `ChatTurnChannels` is where that translation and the refusal below live.

Two-way means the same thing for all five: the composer writes the user's choice and **follows a
write** from outside. It follows a write, not the value standing on the channel - the composer
changes these itself (restoring a session brings back its effort, picking an agent brings its model
and its servers) and the channel learns of that one round trip later, so acting on the standing
value would undo every such change. A host that pre-selects an agent for its page therefore writes
the channel once and the composer keeps it, including across the `GET /chat/capabilities` answer
that arrives afterwards.

A channel is a second way past the composer's own filtering, so a write is refused exactly where a
send naming the same value is: `ChatTurnChannels` asks `AgentFactory.runRefusal`, which is the
question `ChatServlet` pre-checks a send with, plus "does this application have such an agent at
all" (the servlet's 404). A refused write is logged at DEBUG, is *not* applied, and the composer's
own setting is written back onto the channel - so the channel never claims a setting the composer is
not showing, and the next send cannot be answered 403 for something the user never chose.
`TestChatTurnChannels` pins it against the shipped defaults, beside `TestChatRunAccess`.

#### The agent writes a channel: `present`

`present` is the only way an agent reaches the page it is on, and the page decides how much of
itself to offer:

```xml
<chat-conversation input="chatSession" present="paneObject"/>
```

| | |
|--|--|
| The tool | `present(query, ids, as, chart, title, channel)` - `PresentTool` in tl-ai-core holds the logic, `com.top_logic.ai.chat.tools.ChatPresentTool` delivers to the page; registered as `present.present` from `tl-chat.conf.config.xml`, so every host of this module has it. Classified READ / `interaction`: it reads business data and changes page state, not the knowledge base, so there is nothing to approve - and the approval card would sit in the very page it is filling. |
| What | A TL-Script `query` evaluating to the objects - "show me all events as a table" is `present(query: all(`demo.events:Event`), as: TABLE)`, one call, with no listing and fetching first (#616) - or the `ids` of objects the model already holds. `as` is `TABLE`, `CHART` (with `chart: {kind, label, value}`, two TL-Script functions of one object) or `FORM`; omitted, one object is a form and several are a table. The answer lists the objects as `label [id:TID]` (a chart's as `label: value [id:TID]`), so the model can talk about them without reading them again. |
| Data (#655) | A query may compute data instead of objects, in the format of a chart component's `data` script: a whole Chart.js configuration `{'type', 'data': {'labels', 'datasets'}, 'options'}` - stacked, a line over bars, two axes - or its short forms, `{label: number}` (what `groupBy(key, g -> $g.size())` returns), `{label: {series: number}}` and a list of records. `ChartData` (tl-ai-core) turns every form into the one configuration, checks it (a known type, numbers only, objects only in `metadata` and only readable ones) and cuts it to 200 points per series and 20 series, saying so in the answer. Shown as `CHART` (the default for data) or as `TABLE` - a label column and a column per series, `ComputedColumn`s over plain rows. A point of data has no object, so a click opens nothing. |
| The value | A form's object stands on the channel as itself - what a `<form>`, a subject or a context fact reading "the open object" understands. A table or a chart stands there as the `Presentation`, which a `<chat-rail>` renders and `ChatSelection` states to the agent. |
| The rights | A query is evaluated without being able to modify anything, and its objects are reduced to the ones the **acting user** may read (`ReadFilter`), the way `filterSecurity` would. Every id resolves through `TidResolver` with the same rights, all or nothing: an object they may not read is refused exactly like one that does not exist. A chart's functions are tried on every object before anything is shown, so a failing function is the model's error to correct, not an empty chart. |
| The allow list | **Deny by default.** `ChatConversationControl` resolves each `present="…"` entry and registers it on the `ChatViewBinding` of its (window, chat-id); the tool looks the name up across the bindings of that *window* - which is what a run knows about itself, the chat id being something the tool layer is never told. **So a window's allow list is the union of its chats' lists.** A window that offers exactly one channel is written without the model naming it; one that offers several makes the model choose and names them. **A channel counts while its element is displayed** (#649): the registration follows the control's attach and detach, so a page of the shell the user left, a classic tab that is not selected and the React page a browser tab showed before the classic UI offer nothing - before, a run presented into such a page and told the user they saw a table they could not see. |
| Offered or not | The run is offered `present` only while its window displays such a channel (`ChatPresentTool.noPlaceToShow`, through the generic `Tool.unavailableReason` that `AgentLoop` asks before every request). A run without one - a page that names none, no window at all - is not sent the tool, so it answers "show me" in text instead of with a call that is refused. Albert's prompt states the rule conditionally ("you have `present` only where the page has a place ... without it, `query` and a Markdown table"), so the page's `present` attribute stays the one switch: a second, page-dependent prompt section would be a second source of truth, and a second reason for the prompt to change between turns. A call that names the tool anyway is answered with the same sentence. |
| The window | The value is written in the window's sub-session, **within its interaction**, through `AgentViewRefreshService.inWindow` - a `ReactControl`'s state map is a plain `HashMap`. A run with no window (headless, TL-Script, the scheduler) says "no page is attached" and does nothing; that is a fact about the run, and is deliberately a different sentence from "this page allows nothing". |

**What a page needs, all three or none:** the channel, `present="…"` on the `<chat-conversation>`, and
an element that renders the channel - usually `<chat-rail input="…">`, whose pane shows any type.
Without `present` the agent is not offered the tool; without the rendering element it shows a table on
a channel nothing displays and says the user sees it. The classic Albert tab's default view
(`tl-chat-classic/chat.view.xml`) carries all three, plus `<selection channel="…">` so the agent knows
which object the pane shows; an application's own copy of that view has to keep them.

`TestPresentTool` drives the real path - a registered window, a registered channel, a listener
standing in for the page - and builds a real `ChatConversationControl` to pin that its channel counts
only while it is attached. The eval has the same tool without a page (`EvalPresentTool`), and grades
what a run showed (`<presented>`, tl-ai-eval).

#### The draft as a channel (#440)

A page button could not put text into the chat, so consulting built a view action that found the
chat control, reached the browser and typed into the composer with a DOM hack (`SendToCopilotAction`,
issue #414). One element changing another element's editable state is what a channel is for, and
`draft` is that channel:

```xml
<generic-command image="css:bi bi-chat-left-text" input="invoice">
    <label><en>Ask about this</en><de>Dazu fragen</de></label>
    <execute-script><![CDATA[i -> {"en": "Please explain …", "de": "Bitte erkläre …"}.internationalize()]]></execute-script>
    <write-channel name="chatDraft"/>
</generic-command>
```

Two existing engine actions, no new class. What to know about it:

- **Out** the composer reports its text on a typing pause (300 ms, `createDraftReporter` in
  `ui/src/draft.ts`), when the field loses the focus, and immediately before a send. A channel write
  is a round trip and a keystroke is not worth one; blur and send do not wait, because those are the
  moments a host acts on what it reads.
- **In** a write replaces the whole draft, focuses the field and puts the caret behind the text. The
  user still presses send - there is no `send` mode, deliberately: reading the sentence before it
  goes is worth one keystroke.
- A write may be a `ResKey`, because `{"en": …, "de": …}.internationalize()` is the obvious way to
  build a sentence in TL-Script; the control resolves it in the user's language
  (`ChatQuotes.text`). `null` - what an unwritten channel holds - says nothing and leaves the
  composer alone; the empty string clears the draft.
- It follows a **write**, not the standing value, the same way the turn settings do, and for the
  same reason. The echo is recognised in the browser: what this element last published is
  remembered, so a report of "Was" arriving back two keystrokes later does not put the composer back
  two keystrokes. The one place the standing value *is* applied is `onAttach` - a host may have
  written the draft while this element was on another page of the shell and its browser component
  was gone.
- `reference` is the "quote this" half: a model object written to it becomes one atomic reference
  token at the caret, the same token the `@` picker inserts, so it serialises to the same
  `<quote/>` on send. The token carries the object's label and its stable id, and the quoted text a
  nested `<mention>`'s `text` expression computes - without a `<mention>` the label is the quoted
  text too, because the element has nothing else to say about the object. It is read only: the
  tokens are part of the draft `draft` already carries.
- `quote` is the half `reference` cannot serve (#509): **a passage of an object**, which only the
  viewer that showed it can name. `reference` takes the object, whose quoted text is everything the
  element knows about it - the section, when what the reader marked was one paragraph of it. So
  `quote` takes the token itself, as a map:

  ```xml
  <chat-conversation quote="chatQuote"/>
  ```

  ```
  section -> {"id": $section, "location": "02 Kontext", "text": $marked}
  ```

  Only `id` and `text` have to be there - there is nothing to quote without the passage, and nothing
  for a host tool to resolve without the object. `id` is normally the **model object itself**, not a
  printed identifier: an application has no way of spelling the stable id the `@` menu uses, so the
  element fills the kind, the id and the label from the object (a `<mention>` having its say in the
  label) and the write only names the passage. Whatever the map does say wins. Fields may be
  `ResKey`s, resolved like a draft's text. A map with no `text` inserts nothing, and so does a bare
  object: that is `reference`'s job, and quoting the whole of a section back is the one thing the
  agent's instructions tell it not to do. The rule is `ChatQuotes.token`; the inserted token is the
  same one either way, so it serialises to the same `<quote/>`.
- A channel notifies nobody when it is written the value it already holds, so writing the **same**
  object to `reference` twice in a row inserts one token. (A, B, A does insert three: the control
  numbers its writes so the browser can tell the third from the first.) Two *passages* of one object
  are two different maps, so they are two writes and both insert - which is the case `quote` exists
  for.
- Two channels, two state slots (`referenceWrite`, `quoteWrite`) and two counters, deliberately: one
  slot fed by both would lose a token whenever a page cleared one channel in the same request in
  which it wrote the other. The browser parses both with the one `tokenWrite` in
  `chat-conversation.tsx` and hands both to the same `insertReference`.

`TestChatConversationConfig` holds the options and the `ChatQuotes` rule, `draft.test.ts` the
debounce, and the `chat-store.test.ts` "composer seam" block the two calls a write turns into.

#### Writing a channel across a view boundary

The dock case of #440 - a button on one page writing the draft of a chat that lives in another view -
comes down to two questions about the engine. Both are answered, with the code:

1. **Can a nested `<view-ref>` view write a channel of the enclosing view?** Yes, but only through
   an explicit `<bind>`. `ReferenceElement.createControl` calls `parentContext.withIsolatedChannels()`
   (`ReferenceElement.java:118`), which is a `DefaultViewContext` with a **fresh** channel map
   (`DefaultViewContext.java:207-211`), so `resolveChannel` in the child cannot find a parent channel
   by name and throws "Unknown channel" (`DefaultViewContext.java:228-236`). Each `<bind channel="x"
   to="y"/>` then registers the *parent's* channel instance under the child's name
   (`ReferenceElement.java:121-124`), and `ViewElement.createControl` skips creating a local one for
   a name that is already there (`ViewElement.java:139-142`) - so the child view may declare the
   channel for its standalone case and still get the parent's instance inside the shell. Both sides
   hold the same object, so the write travels in either direction.
2. **Can `<app-shell>`'s view declare channels that its pages see?** Directly, yes: `AppShellElement`
   never isolates - its header, content and footer contexts come from `withErrorSink` / `withScope` /
   `withContextMenuOpener` / `withChildSlotPath` (`AppShellElement.java:144-163`), each of which
   passes the **same** `_channels` map on, as does `childContext` (`DefaultViewContext.java:104-109`).
   `<sidebar>` is no different (`SidebarElement.java:441`). So an element written straight into the
   shell's own view - the `<chat-dock>` in the footer, for instance - shares the shell's channels with
   everything else in that file. What does *not* see them is a page, because a page is reached through
   `<view-ref>`, and that is question 1 again.

The demo shows all three positions on one channel. `app.view.xml` declares `chatDraft`; the dock's
`<chat-conversation draft="chatDraft"/>` names it with **no** `<bind>`, because the dock is in that
same file, while the chat page and the Events page each get it through one `<bind>` on their
`<view-ref>`. The Events page's "Dazu fragen" then reaches both: open the dock on that page and the
question is in its composer; switch to the Chat page instead and it is in that one's.

Two conversations on one draft channel work here only because the two are **never mounted at once** -
a dock unmounts its content while a `<chat-conversation/>` of its chat id is on the page (see
`chat-dock.tsx`). A composer holds its own text and reports it, so two that are mounted together
would overwrite each other's drafts; that is a property of the channel, not of the dock. The one that
mounts applies what stands on the channel (`onAttach`), which is what makes the draft follow the user
from the dock to the page and back.

So there is no engine gap for the dock case: a dock in `<app-shell><footer>` reaches the shell's
channels with no binding at all, and a page reaches them with one `<bind>` per `<view-ref>`. What the
engine has no answer for is *opening* the dock from the page - that is a second channel and a dock
element that follows one, not a channel-scope question.

#### Configuring `<chat-conversation>` - easy vs advanced

An application configures this element in the design view. Its options fall into three tiers:

- **Plain toggles / channels a designer sets** - `trace` (`none`/`summary`/`detailed`, one knob that
  replaces the former trace-mode + run-summary; the user can still change it live with the composer's
  trace picker), `citations` (markers *and* the per-answer sources pill - off removes the whole
  citation surface from the transcript), the nested `<actions>` (`copy`/`rate`/`regenerate`/
  `timestamp`), the nested `<controls>` pickers, `placeholder`, `working-indicator`, `prompt-improver-agent` (an agent
  dropdown), `chat-id`, the ten channels (`input`, `subject`, `busy`, `agent`, `model`, `effort`,
  `mcp-servers`, `draft`, `reference`, `quote`), and `present` - the channels the agent may write,
  deny by default. The transcript's rail badge button is *not* configured: it appears exactly when a `<chat-rail>` with the same `chat-id` is mounted, which the
  rail announces to the shared store (`railMounted`), so a rail that mounts after the conversation
  still makes the button appear.
- **Points at code (name a component registered in TypeScript; do nothing unless a developer set that
  name up)** - `react-element`, `citation-renderer` (the renamed `annotations`), `<actions host>`,
  `menu-items`, and `reference-source`. Each such tooltip says so, and each carries `@Hidden`, so the
  layout editor does not offer it at all - a name a designer can only get wrong is not a designer
  option. The XML still accepts them. `<chat-rail pane-width>` and the engine-inherited `class` and
  `personalization-key` are hidden for the same reason (`TestHiddenElementOptions` pins the set).
- **Needs data modelling (TL-Script)** - the nested `<mention>` binding: `rows` (the objects `@` may
  mention), optional `label`/`text` expressions, `observed-types`, `limit`. Same skill as
  `<data-list>`'s `rows`; the control compiles them with `QueryExecutor`, pushes a bounded candidate
  set shaped as the browser's `ReferenceCandidate`, and the existing `@` picker filters it client-side.
  A `<mention>` wins over `reference-source`. Each mention carries the object's `TypeName:id`, which
  `getInstance` resolves; a host that wants an id of its own to mean more gives its agent such a tool
  (see the `<reference>` prompt fragment). The nested `<context>` and `<prompts>` bindings are the
  same skill again - see below.

#### What a page states about the situation: `<context>`

```xml
<chat-conversation input="chatSession">
    <context>
        <inputs><input channel="project"/></inputs>
        <fact name="Project" stable="true"><value>p -> $p.label()</value></fact>
        <fact name="Selection" stable="false"><value>p -> ifElse(isEmpty($p), "nothing", $p.label())</value></fact>
    </context>
</chat-conversation>
```

Bound the way `<data-list>` binds its rows: the `<inputs>` are channels of this page and their
current values are the **leading arguments** of every fact expression. `ChatConversationControl`
evaluates them on the server, in the acting user's context, and re-evaluates them whenever an input
changes; the browser carries them back with `POST /chat/send` and `POST /chat/regenerate`, and
`ChatProcessor.sendMessage` binds them for the run (`RunFactsBinding`), which is what also reaches a
delegate and a subagent.

**This is the seam that replaces a context header on the message text** (#406, #428): the user's
text stays exactly what the user wrote, in the bubble, in the log and in the request.

**`stable` decides where a fact is rendered, and nothing else does.**

`stable="true"` (the default) means the value holds for the whole conversation: it goes into the
`{{context}}` section of the run's environment block, in the system prompt. That block sits in front
of every request of the conversation and has to stay byte-identical, or the provider's prompt cache
misses on every turn - which is why nothing that changes may be in it.

`stable="false"` means the value changes while the user talks - the open object, the selected row,
the section being read. Such a fact goes into the per-turn `<turn-context>` block, which travels in
the turn itself as its own content block of the user message, ahead of the user's text:

```
<turn-context>
Turn: 3
Time: 14:32 (Europe/Berlin)
Open invoice: RE-2026-0042
</turn-context>
```

The turn number and the time of day are added by the framework; they change per turn by nature,
which is exactly why the environment block states the date at day granularity and no time at all.
The block is composed per run (`TurnContext`, from the same facts), attached by `AgentLoop` where the
request is built - so a compaction cannot lose the newest one - and stored on the turn
(`Turn#turnContext`), apart from the user's text: the bubble shows what the user wrote, the
transcript and the run detail show both. A page that states no per-turn fact gets no block at all.
See `tl-ai-core/doc/environment-block.md`.

A fact that throws is sent empty with a WARN rather than taking the page down - the user is in the
middle of a sentence.

#### The one line for the selection: `<selection/>`

The question a user asks a chat beside a page is almost always about what they have open, and
expressing that as `<fact>`s means knowing how this product spells an object identifier. It is one
line instead (#494):

```xml
<chat-conversation input="chatSession">
    <selection channel="selectedEvent"/>
</chat-conversation>
```

which states, as a per-turn fact of the `<turn-context>` block:

```
Selected object: KI im Mittelstand 2026 [type:demo.events:Event] [id:GenericObject:12345]
```

| | |
|--|--|
| `channel` | Mandatory. The channel holding what the user has selected - one object, several, or nothing. A channel of *this* view; a page of a shell hands its selection to a dock in the footer with a `<bind>` on the `<view-ref>`, exactly the way it hands over the draft. |
| `name` | What the fact is called in front of the model. Default `Selected object`; name what the page actually holds where that says more. |
| `<label>` | TL-Script naming one object. Empty uses the application's own label - which is what the user sees on their screen, and therefore the right answer to a question about it. |

**Three things, not one**, and each is load-bearing (`ChatSelection`): the label answers "which one
do I have open?", the qualified type name says what kind of thing it is, and the identifier is the
same `label [id:TID]` form every tool result writes, so the model feeds it straight into the next
tool call and `TidResolver` reads it back. A label alone leaves the agent searching for the object
the user is looking at, which is the state #494 was filed about.

Never stable, and it cannot be configured to be: a selection is the value that changes most often in
a conversation. Nothing selected says `none` rather than nothing at all, and a selection of more than
`ChatSelection.MAX_NAMED` (20) objects names that many and counts the rest - a stray Ctrl+A on a
table must not put ten thousand identifiers in front of the model. The selection is stated **before**
the `<context>` facts, because the block is read top down.

**A dock states it too, and should.** `<chat-dock>` has no `<selection>` of its own - it frames a
`<chat-conversation>`, and that is where the line goes. The dock is where a user asks about the page
*behind* it, so the page has to hand its selection over: in `tl-ai-demo` the shell declares a
`pageSelection` channel, the Events, Venues and Invoices pages' `<view-ref>`s each bind their own
selection channel onto it, and the dock's conversation states that channel. Where a shell has no
channel naming the active page - which is where the engine stands today - the value survives the user
navigating away, so name the fact after what the pages hold together (`Selected object`) or, when
only one list writes it, after that list.

**State them from every place one conversation is reachable from.** A page that declares a
`<context>` and a dock over the same conversation that declares none flips the environment block
between the turns of one chat: the stable facts *and* the `{{turn_context}}` sentence are part of
the system prompt, and a prompt that changes mid-conversation misses every provider's prompt cache.
The demo's dock therefore repeats the page's `Application` fact word for word.

**What the agent remembers about the user** (#439) rides in the same block, under its own heading,
and is read **once per conversation** for the same prompt-cache reason: `ChatProcessor.sendMessage`
asks `ChatSessionManager.getActiveMemories` when the first message of a conversation is sent, keeps
the answer on the session state and binds it for every run of that conversation
(`UserMemoryBinding`). A memory the agent writes in turn 3 therefore shows up in the user's memory
list at once, and in the agent's block in the *next* conversation. The rail section that lists them
is an ordinary `<data-list>` over `tl.ai.execution:UserMemory` filtered by `owner == currentUser()`,
with a Forget command that calls `aiMemoryDelete`. See `tl-ai-core/doc/environment-block.md`.

#### The first questions a page offers: `<prompts>` (#529)

A new chat is the one screen with nothing to react to, and the page knows better than the chat what
is worth asking on it. Consulting's Pietro page carried three hand-built `<button>`s and three
derived channels for exactly that; this is the same thing as configuration:

```xml
<chat-conversation draft="chatDraft" input="chatSession">
    <prompts>
        <inputs><input channel="paneInvoice"/></inputs>
        <prompt icon="css:bi bi-receipt">
            <label><en>Summarise the newest invoices</en><de>Die neuesten Belege zusammenfassen</de></label>
            <value>open -> "Please summarise the three newest invoices."</value>
        </prompt>
        <prompt icon="css:bi bi-question-circle">
            <label><en>Explain the open invoice</en><de>Den geöffneten Beleg erklären</de></label>
            <value>open -> ifElse(isEmpty($open), "…", "Please explain " + $open.label() + ".")</value>
        </prompt>
    </prompts>
</chat-conversation>
```

Bound exactly the way `<context>` is: the `<inputs>` are channels of this page and their current
values are the **leading arguments** of every card's `value`, evaluated on the server in the acting
user's context and re-evaluated whenever an input changes. So a card names what the page is showing
rather than only a fixed sentence - and, because the text is already evaluated when it reaches the
browser, clicking one costs no round trip.

| | |
|--|--|
| `<label>` | Mandatory. What the card says, in the user's own language - the short form of the question. One line, elided where it does not fit; the whole sentence is the button's tooltip. |
| `icon` | A `ThemeImage`, named the way a `<chat-rail>` section names its icon. A card without one shows its label alone. |
| `<value>` | Mandatory. TL-Script producing the text the click puts into the composer. |

**Where they stand, and when.** Between the greeting and the composer - under "Womit kann ich
helfen?", over the field - which is the position the former classic chat gave its own example prompts and
the order the eye reads them in: what this is, what you could ask, where you type. In the empty
stage's grid they are a row of their own (`.stage.is-empty .prompt-suggestions`), zero-height for a
conversation that offers none.

**On a new chat, and nowhere else.** Not "whenever the conversation is empty": a chat the user
opened from the session list is not short of a first question, whatever it holds, so the rule is the
session channel - `promptCardsVisible(chat.sessionId, chat.messages.length)` in
`chat-conversation.tsx`. That is the element's rule and not a page's option; there is nothing to
switch off. (The `empty-only` flag of the first cut is gone.)

**A click fills the composer; it does not send.** The same rule `draft`, `reference` and `quote`
follow, and the reason a card may offer a longer and more precise question than anyone would type:
the user reads the sentence and presses send. It goes in through the same `setDraft` a host writing
the `draft` channel reaches, so the field takes the focus, the caret lands behind the text, and the
composer reports the draft back onto that channel the way a typed sentence does. A card therefore
**replaces** whatever stood in the composer - which on a new chat is a field the user has not typed
into yet.

A card whose expression throws is pushed with an empty text and the browser leaves it out, with a
WARN - the same trade a `<fact>` makes. One card fewer, not a broken page.

`TestChatPromptsConfig` holds the configuration shape; `readPromptCards` / `promptCardsVisible` in
`chat-conversation.test.ts` hold what the browser does with it.

Coupling to know: elements share one conversation only when they agree on a chat id - which is what
the enclosing `<chat>` is for - and a session list feeds the conversation over a **two-way** channel — `<data-list selection>` ↔
`<chat-conversation input>`. The list writes the picked session; the conversation follows it and
writes its own session back (so a chat created on first send gets highlighted). An **empty** selection
means a fresh, empty conversation, so "new chat" is just the list clearing the channel — a
`<data-list create-action="clear-selection">`, no separate binding element. (This replaced the former
`<chat-session-binding>`.)

**The channel is the selection; what the conversation writes back is a proposal** (#576). A host may
bind the channel to something else on its page - Consulting keeps one conversation per project - and
answer a write-back from its own channel listener by refusing the session and writing its own, or
none. `SessionSelection` therefore tells the browser what the channel holds *after* every listener
has answered, never what the browser reported, and counts each refusal in `selectedCorrections`:
a refusal that leaves the channel empty changes nothing in `selected`, and the count is what makes
the browser leave the refused chat (`followSelection` in `chat-conversation.tsx`). The browser does
not report an empty chat on mount either - it would clear the channel the host just set while the
chat it names is still loading. `TestSessionSelection` and `chat-conversation-input.test.tsx` pin
both halves.

Two rules keep the browser from arguing with the channel, and breaking either one makes the
transcript switch between two conversations every few milliseconds, for good. **The element follows
the channel when the channel moves, never when the store does**: the store opens chats itself (a
rail pick, a first send) and reports them, and until the channel answers, `selected` still names the
chat that was left. **Only the latest choice of chat takes effect**: `selectSession` ignores the chat
the store already shows or is opening, and an opening that another choice overtook is dropped when
its transcript arrives. The tab's remembered chat (`sessionStorage`) is never restored where an
element wires an `input` channel - there the channel is the whole selection, and empty means a fresh
conversation - and elsewhere only into a store nothing has chosen a chat for yet (#577).

`<chat-rail>` is a **shell**, not a set of sections: it renders the strip, the section headers and
the pane's chrome, and every section's body is a view element the application configured there.

```xml
<chat-rail input="paneObject">
    <section name="progress" icon="css:bi bi-list-check" open="true" badge="progressBadge">
        <label><en>Progress</en><de>Fortschritt</de></label>
        <data-list count="progressBadge" observed-types="tl.ai.execution:AgentTask" …/>
    </section>
    <section name="invoices" icon="css:bi bi-receipt">
        <label><en>Invoices</en><de>Rechnungen</de></label>
        <data-list selection="paneObject" …/>
    </section>
</chat-rail>
```

Any element is a section - `<chat-sources>`, a `<data-list>` of the agent's tasks or of the
application's own objects, an element a host wrote - so the rail can show a proposed plan, a document the agent
produced or a live view of its browser without this element learning about any of them. An empty
`<chat-rail>` renders an empty rail; there is no framework order and no name registry (the former
`registerRailSection` is gone, replaced by nesting). `@` sources, `+` menu items, citation renderers
and message actions are still registered from TypeScript and referenced by name from XML.

#### The rail's pane (#616)

The `input` channel is the rail's second face: while it holds something, the pane replaces the
section column. **Nothing about it is configured but the channel** - `PresentationPane` renders any
type:

| On the channel | The pane shows |
|--|--|
| one object | its form: every attribute not hidden from display, inherited ones first (so `name` leads), each composition as a `<composition-table>` below the fields - the engine's `<field>` prints a composition as text |
| several objects | a `<table>` typed to their most specific common type: the type's `<main-properties>`, or else up to six single-valued attributes that are not long text; the rest of the type is offered in the column selection. One personalization per type (`chat-pane:<type>`), so the columns a user picked for events are those of the next table of events |
| a `Presentation` (what `present` writes) | the table, chart (the engine's React `<chart>`, `tl-layout-react-chartjs`) or form it names, titled with the agent's `title`. Every chart is the one Chart.js configuration `Presentation.chartData()` computes; `PaneCharts` adds the pane's look only where the configuration says nothing itself |

Before #616 the application wired the pane per type - a `<pane><form>` and a `<list-pane><table>`
over its own main type - and every other type the agent presented showed empty cells. Those two
children are gone; a host that had them deletes them.

**History.** Every value that reaches the channel is a step on top of the ones before, and so is a
row clicked in a table or a bar in a chart, which opens that object's form. "Zurück" drops the top
step and writes the value of the one below back onto the channel, so whatever else reads it - a
scoped chat list, a context fact - sees what the user sees. A value equal to the top step's is not
a new step. At most 20 steps; the oldest goes first. Closing the pane (the cross) empties the
channel, and with it the history.

**Every step stays mounted.** The browser keeps all steps in the DOM, the top one shown and the ones
below `visibility: hidden` and `inert`, each its own scroller - so the table "Zurück" returns to
has its data, its sort, its filter and its scroll position, and no agent call is involved. A chart
is recomputed from its objects when the user returns to it.

**Folding.** The pane's `»` folds it away with its content kept; the strip's first icon (the step's
kind: table, chart or form) brings it back. A rail without a strip still shows that icon while the
pane holds something. Something new arriving - the agent presenting, a row picked in a section -
shows itself even while folded. In a drawer (narrow host), Escape and the scrim fold the pane rather
than empty it.

The composed rail has no workspace grid to spread a sheet over, so `App`'s 1280 px sheet does not
apply to it; below its own `drawer-below` host width it opens as a drawer over the conversation
instead - see "a rail in a narrow host". Two rails on one `chat-id` are not supported: the open
state and the collapsed set are one per conversation.

#### A rail in a narrow host (#565)

The same defect as #548, on the other side of the row: a section opened its 18.75 rem column, the
pane its 360-420 px, **in the flow**, beside a conversation that does not give way. From about 1100 px
down (the demo with the engine's navigation open) the row overflowed into `.tlSidebar__content` by
76-296 px, and at 420 px the column opened entirely off-screen - a click on the strip seemed to do
nothing.

So below `drawer-below` rem of **host** width (the rail's parent, measured by a `ResizeObserver`) the
rail is a **drawer** (`.is-drawer`): its strip stays in the flow, and a section or the pane opens
over the conversation with a scrim scoped to the host.

- **Where the open state lives.** In the store, not in the rail, because the conversation's rail
  button opens and closes it too: `railDrawer` (published per rail, so a rail that mounts and leaves
  cannot clear another's answer) and `railDrawerOpen` (never stored - every narrow page load starts
  closed). While `railDrawer` holds, `toggleSide`, `showSection`, `openPane` and `closeSide` move
  `railDrawerOpen` instead of `sideOpen`, so the user's column choice is untouched and comes back
  when the host is wide again.
- **Closing.** Escape (left to a search field that still has text), a click on the scrim, a second
  click on the active strip icon, and the conversation's rail button. Escape and the scrim also close
  an object pane the drawer shows (`closePane`), because the drawer is the only way to see it.
- **Focus.** Into the drawer on open, back to what opened it (strip icon or rail button) on close.
  Whenever the drawer's panel changes under the focus - a row pick opens the pane and hides the row,
  the pane's back link hides itself - focus moves to the first control of what is showing now, or
  Escape and Tab would have left the drawer with it. Tab wraps inside the rail.
- **Geometry.** The drawer is placed against the host, so the rail tells it where its strip begins
  (`--rail-drawer-end`), measured on resize **and** when the drawer opens: while a host narrows, the
  list turns into a drawer in the same frame, and a measurement taken before that is one of a row
  that still overflows. The drawer never takes the whole row - a strip's width of scrim stays beside
  it (`--rail-drawer-room`), so a pointer always has somewhere to tap out. A pane in the drawer has no
  splitter.
- The strip stays above the scrim, so another section is one click away while one is open.
- `drawer-below="0"` keeps a column at every width.

**How far a column pane widens (#586).** Above `drawer-below` the pane has its splitter, and the
splitter stops where the row would overflow. That end is not a constant: the row holds whatever
the host put beside the rail, and the conversation's panel has `min-width: auto`, so its floor is
reached only through its content. `railRoom` in `side-pane.tsx` therefore asks the row. For one
synchronous layout, the pane is set to the host's full width, so that every neighbour that can give
way shrinks to its floor. The widths are read, and the pane's width is restored before anything is
painted. The measurement runs when the pane is shown, when the host or a neighbour that does not
grow is resized (for example, the session list folding into its strip), and at the start of every
splitter key press or drag. A remembered width is granted only as far as that room allows. In the
demo at 1440 px, the pane's end is 495 px (host 1183, list 264, conversation floor 320, strip 56,
two gaps). The earlier fixed cap of host width less 296 px let Home widen the pane to 887 px, and
the row then overflowed by 392 px.

The default 70 rem is the demo row with the list as a column and the pane open at its default
420 px: list 16.5 + floor 20 + rail strip 3.5 + pane 26.25 + two gaps 3 = 69.25 rem. A section's
18.75 rem column alone would have needed 62, and between the two the pane overflowed the row by up
to 115 px (measured at 1250 px). With the list a drawer the row needs at most 56 rem, above the
list's own 44, so one threshold covers both. Consulting's `pietro.view.xml` has the same row and
fits the default.

#### `<chat-dock>` - the chat over the page

`<chat-dock>` is a **frame**, the way `<chat-rail>` is a shell: a fixed launcher in the corner of the
window, and a card over the page holding whatever the application nested inside it. It brings no
conversation of its own - normally one `<chat>` with a session list, a `<chat-conversation>` and a
`<chat-rail>`, which is what `tl-ai-demo/app.view.xml` puts in the shell's `<footer>`. The footer is
a mounting point, nothing more: the dock is `position: fixed`, so it takes no room in the layout.

| Surface | Kind | |
|--|--|--|
| *(content)* | nested elements | Write **one**. The engine's `.tlStack:has(> .tlPanel--fill)` rule looks one level deep, so a second child adds a wrapping stack and the conversation loses its bounded height. |
| `chat-id` | String, `@Nullable` | **Repeated**, not inherited - see above. Needed only so the launcher can read `badgeTotal` / `badgeSeen` / `busy` from the browser store. |
| `icon` | ThemeImage | The launcher's glyph, default `css:bi bi-chat-dots`. An open dock shows a close cross instead. |
| `<label>` | ResKey | The launcher's tooltip and accessible name; empty falls back to the chat's own wording. |
| `open` | boolean, default `false` | First-load state, and nothing more. Not remembered, no channel. |

**A window carries as many conversations as it shows (#502).** Every `/chat/*` call names its
window **and** its conversation (`windowId` + `conversation`, the browser's own chat id), and
`ChatSessionRegistry` holds a `TaskProgressTracker` - one active session, one processing state - for
each of them; `ChatEventHandlerRegistry` is keyed by tracker, so each conversation gets its own SSE
handler by construction. Until #502 the tracker was one per window: two chat ids that each mounted a
`<chat-conversation>` attached to the same run, each send switched the window's active session, the
second conversation to send took the first one's stream, and the first waited for an answer that
never arrived. `TestChatConversationRouting` pins the new shape, including that a request naming a
conversation the window does not show is answered `stale_window` rather than served from another
one.

Two keys travel, and they are **not** the same string: `conversation` is which chat of the window is
asking, and `chatId` on `PUT /chat/preferences` is the key a *layout* is stored under. They agree
for every element of a named chat; for `<agent-chat>` the layout key is empty (the one-element
chat's own place) while the conversation is the control's id, which is exactly how the browser keys
its store. Two `<agent-chat>` elements in one window are therefore two conversations on the server
as well as two stores in the browser.

**The dock still stands down while a page shows its conversation** - but for a different reason now.
`tl-ai-demo` writes `chat-id="chat"` on the dock *and* on the `<chat>` inside it, so the two are the
**same** conversation in two places; showing both at once would give one `draft` channel two
composers and one remembered layout two rails. `<chat-conversation>` registers itself with the store
(`registerConversation`, `insideDock` from `ChatDockScope`), and a dock whose
`conversationsOutsideDock > 0` renders nothing at all - no panel, no launcher - and *unmounts* its
content. A dock on a chat id of its own needs none of that.

Two guards sit beside it: `eventBelongsToChat` drops a run-scoped SSE frame that names a session
other than the open one - a run this conversation has since moved away from (inert while the server
stamps none, which is most of them today) - and both the remembered session
(`tl-chat-session:<chat-id>` in `sessionStorage`) and the run snapshot the context ring reads
(`ChatHandle.runs`) are per chat id. The module-level `runStore` export is still written, as the
single-chat host seam it has always been; nothing in this bundle renders from it.

Three things are load-bearing:

- **The panel is hidden when closed, never unmounted.** The store drops a chat when its last element
  leaves, so a dock that unmounted its panel would hand back a new, empty conversation on every open
  and would stop hearing about the run it started. `.tl-chat-dock__panel[hidden] { display: none }`
  is a rule of its own because the `display: flex` beside it would otherwise beat the attribute.
- **Opening it marks the badges seen, and only that.** `markBadgesSeen(badgeTotal)` moves the same
  watermark `toggleSide` moves - to the total as it stands, never to zero - without opening the
  rail, which is the other half of `toggleSide` and the wrong half here.
- **The breakpoints are the dock's, not the window's** (Gitea #427, #548). The conversation inside
  measures its own `.stage`, and the `<data-list>` session list measures its host row, so a dock
  sized for the desktop layout (`min(68.75rem, …)`) keeps a column list in a narrow window as long
  as the panel has room for it, and a panel below the list's `drawer-below` opens the list as a
  drawer over the conversation instead of squeezing it. The rail does the same below its own
  `drawer-below` (#565). Inside a dock the conversation's floor is measured on `--chat-dock-width`
  rather than on the window, because the panel stands its offset in from both edges.
- **The content mounts from the second commit on.** On the first one the dock cannot know yet
  whether the page shows its conversation - a `<chat-conversation/>` says so from an effect. Mounted
  meanwhile, the dock's rail stated its own `open="false"` over the page rail's `open="true"`, so a
  fresh account's rail on the chat page started closed (#552).
- **An Escape something inside already answered is not the dock's** (`defaultPrevented`): closing
  the rail's or the list's drawer must not close the dock with it.

Everything a theme would restyle is a token on `.tl-chat-dock` - `--chat-dock-width`, `-height`,
`-offset`, `-launcher`, `-gap`, `-radius`, `-shadow`, `-z`.

The store still issues the one `PUT /chat/preferences`; layout fields are mutated by the rail (and by
`App`, which contains one). `open` and `<section open>` are first-load defaults for a user who has
decided nothing - see "what survives a reload, and who decides it".

### Stability

Exported types and `App` / `extensions` are the contract. File splits inside `ui/src` are not.

Citations: see `doc/citation-contract.md`. Albert points at its sources with links; the server keeps a
link as a footnote only when a tool of the run read that source, and drops the link of any other id
or address.

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in `ChatRunExecutor`, which carries as many as arrive unless `<capacity chat-workers>` on `AgentRuntimeService` bounds it and queues behind a ceiling it has; a send with no room - only reachable with a bounded `chat-queue` or a per-account cap - is answered **429** with `{"status":"rejected","reason":"…"}`, which the React client throws as a `RefusedError` and the composer shows in the notice strip
