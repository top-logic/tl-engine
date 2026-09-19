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
   - `PUT /chat/preferences?chatId=…` – Per-user rail/pane layout of **one** chat. The React UI cannot call `PersonalConfiguration` itself, so this wraps `setJSONValue` **and** `storePersonalConfiguration()` (transient otherwise). Key `tl-chat.layout`, holding one layout per `chat-id`. The body is what the user *decided*, so a fact they never touched is absent. There is no `GET`: the bootstrap travels with the control state (`ChatViewBinding.writeLayoutState`, re-read on every render), so a second round trip would only repeat it.

2. **SSE API** (real-time push):
   - `GET /chat/events` – SSE connection. `ChatSSEHandler` receives `AgentEvent`s and emits structured SSE frames: `connected`, `run_started`, `turn_started`, `text_delta`, `tool_started`, `tool_completed`, `approval_required`, `message_complete`, `heartbeat`, etc.

`ChatSessionFilter` (mapped to `/chat/*`) requires a TopLogic session; `ChatServlet.doService`
additionally refuses anonymous ones with 401, because an application may hand a visitor a session
without a login. The chat has no anonymous mode: `AgentChatControl` passes `authenticated` to the
browser component, which shows a sign-in note rather than a composer.

### Processing Flow

1. User sends message via `POST /chat/send` or TopLogic AJAX command
2. `ChatProcessor.sendMessage()` adds message to `Chat`, creates a `ConversationLog` turn, submits `AIBackgroundTask`
3. Background task resolves entry point agent from `AgentRegistry.getEntryPointAgentName()` and invokes it with `ConversationLogListener` bound for structured event recording
4. Agent events (tool calls, thinking, text deltas) are delivered to the SSE handler in real-time and recorded in the `ConversationLog`
5. `ChatProcessor.applyPollUpdates()` flushes pending messages into Chat
6. On completion, agent response added as `ChatMessage` and `ConversationLog` persisted

### Key Classes

- **`ChatProcessor`** – Static utility: background thread pool, orchestrator execution, state sync
- **`TaskProgressTracker`** – Session-scoped chat state, manages processing states and session metadata
- **`ChatEventHandler`** – The contract a chat UI implements to observe a run: the `AgentEvent` stream plus the
  final-message and provider-error notifications `ChatProcessor` produces itself
- **`ChatEventHandlerRegistry`** – Interface-typed lookup of the chat UI observing a `TaskProgressTracker`;
  `ChatProcessor` resolves the handler here per event, so a UI other than the SSE servlet can register
- **`ChatSSEHandler`** – Push-based SSE implementation of `ChatEventHandler`
- **`Chat`** – Thread-safe in-memory container using `CopyOnWriteArrayList<ChatMessage>` (max 500)
- **`ChatSessionManager`** – Manages per-session in-memory state (Chat + ConversationMemory + ConversationLog) with LRU caching (max 5 sessions). Handles session switching with persist/load/rebuild cycle.
- **`ChatHistoryService`** – Static access to persistent `AgentSession` objects (kind CHAT) via `AgentExecutionStore` (tl-ai-core). Conversations are stored as one transcript per `AgentExecution` (written by `AgentRunner` at run end) and merged in run order on load.
- **`LegacyChatSessionMigration`** – Startup service migrating legacy `tl.ai.chat:ChatSession` blobs to `AgentSession`s eagerly. The legacy model has been removed (tl-ai-core migration `Issue_230_remove_module_tl_ai_chat`), so the service is a no-op now; it is kept until every deployment is known to have run it.
- **`ChatSessionRegistry`** – Bridges `WindowScope` (a chat control) and `HttpSession` (`ChatServlet`). Published API: the classic control in `tl-chat-classic` and `Consulting`'s own control register here from outside this module.
- **`ChatViewBinding`** – Once per (sub-session, `chat-id`): tracker, user, and the channels the agent may write there. `AgentChatControl` and the split elements (`<chat-conversation>`, `<chat-rail>`) delegate to it so several controls do not bind the window several times. The tracker stays one per window. The layout preference is the exception: it is read whenever `writeLayoutState` is asked for it, which every chat control does from `onBeforeWrite` - see "where the chat's state lives". It is also where every join is tallied for `ChatJoinLog`.
- **`AgentViewRefreshService`** – Makes a model-bound element beside the transcript follow a running agent. A run commits from a background thread, and a window only sees model changes when its `GlobalModelEventForwarder` is asked to synthesize them - on a browser command or on the 30 s SSE heartbeat. The service listens on `AgentExecutionStore.ChangeListener`, coalesces a burst of writes (200 ms) and synthesizes per window, in that window's sub-session and **under the window's request lock** (a `ReactControl`'s state map is a plain `HashMap`; the heartbeat path skips the lock, and this deliberately does not copy that). Windows are registered by `ChatViewBinding` while a chat renders, which is a fork: the engine offers no way to enumerate an account's live windows. The same register and the same lock serve the write direction: `inWindow` runs a caller's action in the window's sub-session under its lock (after synthesizing, so the window knows the run's objects), and `hasWindow` is what lets a tool tell "this run is headless" from "this page allows nothing". A write waits longer for the lock than a refresh does (`write-timeout-millis`, 5 s): a refresh that gives up loses nothing, a write has no second chance.
- **`PresentTool`** – The `present` tool (`com.top_logic.ai.chat.tools`): the agent puts model objects onto a channel of the page it is on, and the page renders them however it likes. Registered from this module's own configuration, so every application that depends on `tl-chat` has the tool; **which** channel it may write is the page's decision (`<chat-conversation present="…">`, deny by default). See "the agent writes a channel" below.
- **`ChatScope` / `ChatScopeElement`** – The `<chat>` container and the ambient value it puts into the child `ViewContext`. An element's conversation is its own `chat-id`, else the enclosing `<chat>`, else `default`.
- **`ChatJoinLog`** – Per render, which element joined which chat id; logged when the interaction ends, with a WARN for an element that joined nobody while another id has several.

## Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe message access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection and synchronized writer access

## Persistence Model

- **Module**: `tl.ai.execution` (tl-ai-core), **Types**: `AgentSession` (kind CHAT) + `AgentExecution` per run
- Session: `title`, `kind`, `owner`, `metadata` (approved tools, approval mode, tasks, skills), timestamps
- Execution: `runId`, `agentName`, `model`, `provider`, `status`, `stopReason`, token columns, `transcript` (CLOB)
- Transcript format: `ConversationLog` turns serialized via `ConversationLogSerializer` (tl-ai-core, `PersistedConversationV2`)
- Human-in-the-loop requests persist as `InteractionRequest` rows (approval/question/choice with resolution)
- The agent's task plan persists as `AgentTask` rows of the session. `TaskStore` (tl-ai-core) stays the truth of the running turn - it also carries `activeForm`, `owner` and the blocks/blocked-by sets, which the model does not - and `AgentTaskMirror` writes what it says onto the rows after every mutation, through `AgentExecutionStore.syncTasks` (one transaction, on the writer thread). The store is still serialized into the session metadata as well, because that is what restores it when a session is re-opened and what the SSE `task_update` frame and the App's own Fortschritt list read. So: the blob is the store's own persistence, the rows are the model a view binds to.
- Legacy `tl.ai.chat:ChatSession` single-blob sessions were migrated to this model eagerly at startup by `LegacyChatSessionMigration`; the legacy model has since been removed

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

The classic `chat-client.js` control for LayoutServlet applications is **not** in this module any
more: it is the separate, frozen `tl-chat-classic` (#398), which depends on this one. A host gets
the "Albert" tab by depending on `tl-chat-classic`; depending on `tl-chat` alone gives the API and
the React chat. Session state the classic control reads - `Chat`, `ChatProcessor`,
`TaskProgressTracker`, `ChatSessionRegistry`, `PromptImprovementProcessor` - stays here.

### Toolchain and build output

| | |
|--|--|
| Sources | `ui/src` (TypeScript, `.tsx`, `jsx: "react"` - the classic runtime, because React comes from the host) |
| Bundler | Vite library build, entries `ui/src/main.tsx` (`tl-chat-ui.js`) and `ui/src/index.ts` (`tl-chat-seams.js`) |
| Output | `src/main/webapp/script/tl-chat-ui.js`, `tl-chat-ui.css`, `tl-chat-seams.js` - **git-ignored build output, never commit it** |
| Node | installed by `frontend-maven-plugin` into `ui/node/` (v20.10.0); a local Node 20 works too |
| Dependencies | `marked` (MIT) + `dompurify` (Apache-2.0 or MPL-2.0) for markdown, `highlight.js` (BSD-3-Clause) for code blocks - core plus 13 registered grammars. All three are bundled into `tl-chat-ui.js`, so their licences travel with the WAR. |

`mvn -pl tl-chat install` runs, in `generate-resources`: `npm install`, then `npm run typecheck`
(`tsc --noEmit`), then `vitest run`, then `npm run build`. The type check is a build step of its own
because the Vite library build strips types without checking them.

`tl-react-bridge` is the host's module: the build keeps it external and the browser resolves it
through a `<module-script specifier=…>`, so there is nothing in `node_modules` for a test run to
import. `vite.config.ts` therefore aliases it, for the vitest run only, to `ui/test/tl-react-bridge.ts`.
The stub hands out the **real** React (a dev dependency), so a test may render a component with
`react-dom/server` and assert on the markup - `chat-rail.test.ts` does. Everything else in it is the
smallest stand-in that behaves: the control state a test sets, a command sink it reads back, and a
marker element in place of a mounted child control. React must still never be imported from `react`
in `ui/src` - it is the host's instance, and a second copy breaks every hook; `chat-rail.test.ts`
pins that, which is what the missing dependency used to do.

All four sit in `generate-resources` on purpose, so `-DskipTests` still runs the JS tests: that flag
is for the Java suites, whose KB setup is what makes a build slow, while the whole vitest run is a
second or two. Keeping it there means the bundle that goes into the WAR was never built from code
its own tests reject - including on a `-DskipTests` build, which is how this module is usually
built.

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

`view/AgentChatElement` is the `<agent-chat/>` view element (`trace-mode`, `prompt-improver-agent`);
`view/AgentChatControl` is the `ReactControl` behind it. It registers the per-window
`TaskProgressTracker` through `ChatViewBinding` and pushes the bootstrap state (`windowId`,
`userName`, `welcomeName`, `traceMode`, `promptImprovementEnabled`) the browser component reads.
The component itself is registered as `TLAgentChat` in `ui/src/main.tsx`.

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
| `css-class` | TL-Script of one row giving extra classes on the row element - its *state* (`is-done`, `is-running`, `is-pending` are the ones the stylesheet defines). |
| `selection` | Two-way channel holding the picked row object. |
| `count` | Channel receiving the number of rows, written with every serialisation - what a rail section's badge reads without asking the browser. |
| `search` / `collapsible` / `title` / `empty-text` | The list's own chrome. |
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

**Tokens.** Anything the element's CSS uses must be declared under **both** roots: `.tl-cockpit …`
and `.tl-data-list`. A token declared only on the first is silently missing in a standalone list.
The same holds for the button reset, the focus ring and `box-sizing`.

Writing a session: a row command calls `aiSessionRename(session, title)` / `aiSessionDelete(session)`
(tl-ai-core `AIScriptFunctions`), never the `title` attribute. Those go through
`AgentExecutionStore`, which commits on its writer thread, bumps `lastUpdated` (the list's own
grouping) and tells the other windows; `aiSessionDelete` also asks a live run to stop and drops the
chat out of every window that holds it. Both refuse a session the acting account neither owns nor
administers.

An application makes the bundle loadable by declaring it on `ClientResources` - see
`tl-ai-demo/src/main/webapp/WEB-INF/conf/tl-ai-demo.conf.config.xml`:

```xml
<module-script name="tl-react-agent-chat" resource="/script/tl-chat-ui.js" requires="tl-react-bridge"/>
<module-script name="tl-chat-seams" resource="/script/tl-chat-seams.js" specifier="tl-chat-seams" requires="tl-react-bridge"/>
<stylesheet name="tl-react-agent-chat-css" resource="/script/tl-chat-ui.css" requires="tl-react-controls-css"/>
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
| `chat-shell` | `.tl-cockpit` - App's root, i.e. `<agent-chat>` | Session rail: column or drawer over the thread (≤ 1100 px). Artefact pane: column or sheet, and the icon strip (≤ 1279.98 px). |
| `chat-stage` | `.stage` - the transcript plus the composer, in App **and** in a composed `<chat-conversation/>` | Thread gutters (≤ 1080 px), Enter hint long/short (≤ 26 rem), the auto-approve label (≤ 19 rem), the model name on its picker (≤ 18 rem), and whether the composer's controls wrap (≤ 16 rem). |

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

The summary is rendered **as authored**, on one line, cut off with an ellipsis where it does not fit.
A summary that has to fit is shortened where it is written, not here.

`GET /chat/capabilities` is filtered by the acting account (#392): the agents and the MCP servers it
lists are exactly what a run of that account accepts. The **models** are the one thing a single
payload cannot make exact - it is fetched once and the agent changes without another round trip - so
it lists what the account may choose with *some* agent it may run and marks each with
`grantedToRole`; `modelsFor()` in `models.ts` narrows that to `granted ∪ {selected agent's model}`
before the picker sees it. A request that names something the account may not use is answered **403**
with a sentence in the user's language, which the composer shows in the notice strip in the neutral
tone; the classic control in `tl-chat-classic` renders it as a system bubble.

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
| `context-usage.tsx` | The context ring and its popover, plus how each number is derived |
| `prompts.tsx` | Approval and question cards above the composer, with their countdowns |
| `markdown.tsx` / `highlight.ts` | Markdown rendering, sanitizing, code blocks |
| `api.ts` | Every call to `/chat/*`, plus the persisted-transcript to message mapping |
| `reducer.ts` / `store.ts` / `types.ts` | Transcript reducer, the `runStore` snapshot a host may subscribe to, shared types |
| `extensions.ts` | Host composition contract (`ChatExtensions`) |
| `rail.tsx` / `side-pane.tsx` / `progress.tsx` | `App`'s own rail, the artefact pane, the Fortschritt list |
| `theme-icon.tsx` | A `ThemeImage` (`css:bi bi-…`) as an element, for configured icons |
| `token-input.tsx` / `reference.ts` | The composer's inline reference tokens and how they travel |
| `sources.tsx` | The cited-source model, the `Quellen` section and the per-answer pill |

## Extending the chat

The shipped element is `TLAgentChat` (`ui/src/main.tsx`). A host that composes the chat registers its
own element and points `<agent-chat react-element="MyChat"/>` at it. `AgentChatControl` is still the
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
| `composerAddons` | none | `referenceCandidates(query)` is what typing **@** offers, and `extraMenuItems` adds rows to the `+` menu (their `action` gets the `ComposerApi`). |
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

**Is `ref` resolvable? No - not by the framework.** It is the host application's own id, carried so
that a host tool can recognise it; tl-ai ships no tool that takes one, and the agent is told so by
the `<reference>` prompt fragment (`tl-ai-core.conf.config.xml`). **A host must therefore put
everything the model needs into the element's text**, which is the quoted passage. A host that wants
the id to mean more gives its agent a tool that accepts it; then the same fragment's "unless a tool
you have takes such an id" applies.

### What an answer's citations look like

A marker is a small grey number after the sentence. Hovering (or focusing) it shows the source's
label, where it comes from, and the excerpt of the source with the cited passage highlighted -
`cite`'s `passage` argument, kept only when the turn's own tool results really contain it. Clicking
opens the source: `messageAnnotations.render` decides that for a host kind (returning nothing leaves
the web default, which opens the page).

**Kinds are declared, not invented.** The framework knows `web`; a host declares its own kinds once
through the `CitationKinds` service (name, one-line description, where the id comes from, whether a
passage applies). The framework appends the vocabulary to the system prompt of every agent that has
the cite tool, the validator drops a citation of an undeclared kind at DEBUG, and a kind marked
`passage="false"` is a structured object whose popover shows the tool result's fields instead of a
sentence. See `doc/citation-contract.md`.

Under an answer sits a collapsed `Quellen · n zitiert` pill with that answer's sources. The rail's
`Quellen` section is the session-wide list: every source cited in this chat, with the marker numbers
that point at it - a number scrolls to its answer, the row opens the source, and a filter appears
once the list is long. `sourceTabs` puts a host list beside it as further tabs.

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
| Where it is stored | `PersonalConfiguration`, key `tl-chat.layout`, **one layout per `chat-id`**: `<agent-chat>` (empty id) and a composed `<chat-rail chat-id="grouped">` are different places with different sections. |
| What is stored | What the user *decided*: `railOpen` (absent while they never toggled it), the pane width they dragged to, `badgeSeen`, and a per-section-name open flag for each section they toggled. Never what a page happened to show. |
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
approvals waiting for this user). A reload in mid-run therefore comes back with the Fortschritt
list, a busy composer and the open approval card.

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
it is nullable, and left empty an element outside every `<chat>` joins `default`. `<agent-chat>` is
the exception that joins nothing: it binds with `""` and `App` falls back to `props.controlId`, so
two chats in one window stay two chats - Consulting's `CopilotRegistry` keeps a *list* of live
panels, and a fixed key would silently merge their transcripts (`chat-store.test.ts`,
`testTwoAppsInOneWindowDoNotShareATranscript`).

**The join is logged.** `ChatJoinLog` tallies, per render, which chat id each element joined and
writes one INFO line for it. When a window holds more than one chat id and one of them has a single
element, it adds a WARN naming that element - the shape a typo produces. A page with one chat id
never warns, however many elements are on it.

`<chat-conversation>` is the transcript and the prompt field as one element - the transcript and
the input almost always sit together, so they are one placeable box, while the session list and the
rail stay separate because those genuinely want independent placement. It honours its configuration
flags as render guards on the existing `Thread` and `Composer` props.

#### The eight channels of `<chat-conversation>`

| Channel | Direction | Value |
|--|--|--|
| `input` | two-way | The open `tl.ai.execution:AgentSession`, as the object. Empty means a fresh conversation, so "new chat" is the list clearing it. |
| `busy` | out | `Boolean`, `true` while the agent works. |
| `agent` | two-way | The agent's **name** (`String`). |
| `model` | two-way | The model's **name** (`String`). |
| `effort` | two-way | The effort level (`String`), one of the levels the selected model offers; empty for none. |
| `mcp-servers` | two-way | The attached servers' **names**, a `Collection<String>` (one name on its own is read as a set of one). |
| `draft` | two-way | The text standing unsent in the composer (`String`, or a `ResKey` on the way in). |
| `reference` | in | A model object, inserted into the draft as one atomic reference token. |

`present` is not a seventh: it is a *list* of channels this element opens to the agent rather than
a channel the element itself reads or writes - see "the agent writes a channel" below.

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
| The tool | `present(ids, channel)` - `com.top_logic.ai.chat.tools.PresentTool`, registered as `present.present` from `tl-chat.conf.config.xml`, so every host of this module has it. Classified READ / `interaction`: it reads business data and changes page state, not the knowledge base, so there is nothing to approve - and the approval card would sit in the very page it is filling. |
| The ids | The identifiers tool results carry as `label [id:TID]` (`ReadFilter.withId`, read back by `TidResolver.objectId`). One id puts the object on the channel as itself; several put a `List`. All or nothing - one unreadable id refuses the whole call, because a page showing three of four objects is a page that lies about what the user is looking at. |
| The rights | Every id resolves through `TidResolver` with the **acting user's** `ModelSecurity`. An object they may not read is refused exactly like one that does not exist. A tool never exceeds its user. |
| The allow list | **Deny by default.** `ChatConversationControl` resolves each `present="…"` entry and registers it on the `ChatViewBinding` of its (window, chat-id); the tool looks the name up across the bindings of that *window* - which is what a run knows about itself, the chat id being something the tool layer is never told. **So a window's allow list is the union of its chats' lists**: a page and a `<chat-dock>` that each name a channel means a run in either may write both, and a host that needs them kept apart gives them separate windows. A name nobody registered is refused with the list of names that exist. The registration ends with the control, so a view that is gone is not writable. |
| The window | The value is written in the window's sub-session, **under its request lock**, through `AgentViewRefreshService.inWindow` - a `ReactControl`'s state map is a plain `HashMap`. A run with no window (headless, TL-Script, the scheduler) says "no page is attached" and does nothing; that is a fact about the run, and is deliberately a different sentence from "this page allows nothing". |

Which channel a host names is the whole configuration surface: the agent points at data, and what
that data *looks like* is whatever element reads that channel. `TestPresentTool` drives the real
path - a registered window, a registered channel, a listener standing in for the page.

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
  (`ChatConversationControl.draftText`). `null` - what an unwritten channel holds - says nothing and
  leaves the composer alone; the empty string clears the draft.
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
- A channel notifies nobody when it is written the value it already holds, so writing the **same**
  object to `reference` twice in a row inserts one token. (A, B, A does insert three: the control
  numbers its writes so the browser can tell the third from the first.)

`TestChatConversationConfig` holds the two options, `draft.test.ts` the debounce, and the
`chat-store.test.ts` "composer seam" block the two calls a write turns into.

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
same file, while the chat page and the Belege page each get it through one `<bind>` on their
`<view-ref>`. The Belege page's "Dazu fragen" then reaches both: open the dock on that page and the
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
  `timestamp`), the nested `<controls>` pickers, `placeholder`, `prompt-improver-agent` (an agent
  dropdown), `chat-id`, the eight channels (`input`, `busy`, `agent`, `model`, `effort`,
  `mcp-servers`, `draft`, `reference`), and `present` - the channels the agent may write, deny by
  default. The transcript's rail badge button is *not*
  configured: it appears exactly when a `<chat-rail>` with the same `chat-id` is mounted, which the
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
  A `<mention>` wins over `reference-source`. Each mention carries the object's stable id; the framework
  ships no tool that resolves it, so a host that wants the id to mean more gives its agent such a tool
  (see the `<reference>` prompt fragment). The nested `<context>` binding is the same skill again -
  see below.

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

`stable="true"` (the default) means the value holds for the whole conversation, and only such a fact
is rendered - into the `{{context}}` section of the run's environment block. The block sits in front
of every request of the conversation and has to stay byte-identical, or the provider's prompt cache
misses on every turn. A fact that is not stable travels and is dropped for now; rendering it per turn
is #438. See `tl-ai-core/doc/environment-block.md`.

A fact that throws is sent empty with a WARN rather than taking the page down - the user is in the
middle of a sentence.

Coupling to know: elements share one conversation only when they agree on a chat id - which is what
the enclosing `<chat>` is for - and a session list feeds the conversation over a **two-way** channel — `<data-list selection>` ↔
`<chat-conversation input>`. The list writes the picked session; the conversation follows it and
writes its own session back (so a chat created on first send gets highlighted). An **empty** selection
means a fresh, empty conversation, so "new chat" is just the list clearing the channel — a
`<data-list create-action="clear-selection">`, no separate binding element. (This replaced the former
`<chat-session-binding>`.)

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
    <pane><form input="paneInvoice"/></pane>
    <list-pane><data-list …/></list-pane>
</chat-rail>
```

Any element is a section - `<chat-sources>`, a `<data-list>` of the agent's tasks or of the
application's own objects, an element a host wrote - so the rail can show a proposed plan, a document the agent
produced or a live view of its browser without this element learning about any of them. An empty
`<chat-rail>` renders an empty rail; there is no framework order and no name registry (the former
`registerRailSection` is gone, replaced by nesting). `@` sources, `+` menu items, citation renderers
and message actions are still registered from TypeScript and referenced by name from XML.

The `input` channel is the rail's second face: while it holds something the pane replaces the
section column, with the value available to the element inside over the same channel; closing the
pane writes `null` back. **One** object shows the `<pane>`, **several** show the `<list-pane>` - a
record is a form and a set of records is a list, and no one element renders both (`<form>` resolves
its input to one object). A rail that configured only one of the two uses it for whatever stands
there. The pane's title is the object's label, or the count for a set. Every child stays mounted - a collapsed section and an unopened pane are
hidden, not dropped - so a section keeps reporting its badge while the rail is closed, which is
exactly when the badge matters. Below 1280 px the composed rail stays a column (it has no workspace
grid to spread a sheet over); `App`'s sheet is unaffected. Two rails on one `chat-id` are not
supported: the open state and the collapsed set are one per conversation.

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

**One conversation per window, and that is the transport talking.** Every `/chat/*` call carries a
`windowId` and nothing else: `ChatServlet` resolves one `TaskProgressTracker` per window, holding
one active session and one processing state, and `ChatEventHandlerRegistry` addresses one handler
per tracker. Two chat ids that each mount a `<chat-conversation>` in one window therefore attach to
the same run - each send switches the window's active session, and each transcript renders the
other's tokens. Three things follow, and all three are in this PR:

- **A dock joins the page's chat id, it does not invent one.** `tl-ai-demo` writes `chat-id="chat"`
  on the dock *and* on the `<chat>` inside it, so the dock is a second **place** for the one
  conversation rather than a second conversation.
- **The dock stands down while a page shows that conversation.** `<chat-conversation>` registers
  itself with the store (`registerConversation`, `insideDock` from `ChatDockScope`), and a dock
  whose `conversationsOutsideDock > 0` renders nothing at all - no panel, no launcher - and
  *unmounts* its content, so the rail inside it stops competing with the page's for the one
  remembered layout.
- **`ChatJoinLog` names the shape.** Two chat ids each mounting a `<chat-conversation>` in one
  window is a WARN, whatever the spelling: "the chat transport is one conversation per window".

Two guards sit behind that rule rather than in place of it: `eventBelongsToChat` drops a
run-scoped SSE frame that names a session other than the open one (inert while the server stamps
none, which is most of them today), and both the remembered session (`tl-chat-session:<chat-id>` in
`sessionStorage`) and the run snapshot the context ring reads (`ChatHandle.runs`) are per chat id.
The module-level `runStore` export is still written, as the single-chat host seam it has always
been; nothing in this bundle renders from it. **Making the transport itself per `(window, chat-id)`
is a ticket of its own** - until it lands, a window shows one conversation.

Three things are load-bearing:

- **The panel is hidden when closed, never unmounted.** The store drops a chat when its last element
  leaves, so a dock that unmounted its panel would hand back a new, empty conversation on every open
  and would stop hearing about the run it started. `.tl-chat-dock__panel[hidden] { display: none }`
  is a rule of its own because the `display: flex` beside it would otherwise beat the attribute.
- **Opening it marks the badges seen, and only that.** `markBadgesSeen(badgeTotal)` moves the same
  watermark `toggleSide` moves - to the total as it stands, never to zero - without opening the
  rail, which is the other half of `toggleSide` and the wrong half here.
- **The breakpoint is the window's, not the dock's** (Gitea #427). The chat's responsive rules are
  plain `@media (max-width: …)`, so a narrow *window* puts the elements inside the dock into their
  narrow layout however wide the panel is. Below 1100 px the `<data-list>` session list becomes
  App's off-canvas drawer and disappears from the dock as it does from the page. The panel is sized
  for the desktop layout (`min(68.75rem, …)`); the real fix is a container query and belongs to
  #427.

Everything a theme would restyle is a token on `.tl-chat-dock` - `--chat-dock-width`, `-height`,
`-offset`, `-launcher`, `-gap`, `-radius`, `-shadow`, `-z`.

The store still issues the one `PUT /chat/preferences`; layout fields are mutated by the rail (and by
`App`, which contains one). `open` and `<section open>` are first-load defaults for a user who has
decided nothing - see "what survives a reload, and who decides it".

### Stability

Exported types and `App` / `extensions` are the contract. File splits inside `ui/src` are not.

Citations: see `doc/citation-contract.md`. Albert records them by calling `cite`; the server drops
any id or URL that did not appear in this turn's other tool results, and leaves every other bracketed
number in the answer alone.

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
