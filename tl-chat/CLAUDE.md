# tl-chat

Real-time agentic chat interface for TopLogic applications. Connects users to the multi-agent orchestrator (from `tl-ai-core`) and displays tool calls, agent handoffs, and AI responses as they happen.

**Parent:** `tl-parent-core` | **Dependencies:** `tl-core`, `tl-ai-core`

## Architecture

### Dual API: REST + SSE

Both served by `ChatServlet` at `/chat/*`:

1. **REST API** (JSON):
   - `POST /chat/send` – Send user message, starts background AI processing. Returns `{ taskId, status }`.
   - `GET /chat/updates` – Poll for full chat state (messages).
   - `POST /chat/sessions` – Create new chat session.
   - `GET /chat/sessions` – List all sessions for current user.
   - `GET /chat/sessions/{id}` – Load session and activate server-side.
   - `PUT /chat/sessions/{id}/title` – Update session title.
   - `DELETE /chat/sessions/{id}` – Delete session (cascades over recorded executions, tasks, interactions).
   - `PUT /chat/preferences` – Per-user rail/pane layout. The React UI cannot call `PersonalConfiguration` itself, so this wraps `setJSONValue` **and** `storePersonalConfiguration()` (transient otherwise). Key `tl-chat.layout`. There is no `GET`: the bootstrap travels with the control state (`AgentChatControl.putState`), so a second round trip would only repeat it.

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
- Legacy `tl.ai.chat:ChatSession` single-blob sessions were migrated to this model eagerly at startup by `LegacyChatSessionMigration`; the legacy model has since been removed

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
import. `vite.config.ts` therefore aliases it, for the vitest run only, to `ui/test/tl-react-bridge.ts`
- a stub of just what importing the chat's modules touches. A test that needs a *rendered* component
needs a real React, which this is not.

All four sit in `generate-resources` on purpose, so `-DskipTests` still runs the JS tests: that flag
is for the Java suites, whose KB setup is what makes a build slow, while the whole vitest run is a
second or two. Keeping it there means the bundle that goes into the WAR was never built from code
its own tests reject - including on a `-DskipTests` build, which is how this module is usually
built.

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
`TaskProgressTracker` and pushes the bootstrap state (`windowId`, `userName`, `welcomeName`,
`traceMode`, `promptImprovementEnabled`) the browser component reads. The component itself is
registered as `TLAgentChat` in `ui/src/main.tsx`.

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
| `chat-store.ts` | The chat's state, one store per `chat-id`: bootstrap, capabilities, sessions, turn settings, transcript, run, layout preferences and the one `EventSource` |
| `composer.tsx` | Input, plus menu, model picker, attachments, drag and drop, improve-prompt |
| `thread.tsx` | Transcript: user bubbles, answers, run meta line, trace panels |
| `context-usage.tsx` | The context ring and its popover, plus how each number is derived |
| `prompts.tsx` | Approval and question cards above the composer, with their countdowns |
| `markdown.tsx` / `highlight.ts` | Markdown rendering, sanitizing, code blocks |
| `api.ts` | Every call to `/chat/*`, plus the persisted-transcript to message mapping |
| `reducer.ts` / `store.ts` / `types.ts` | Transcript reducer, the `runStore` snapshot a host may subscribe to, shared types |
| `extensions.ts` | Host composition contract (`ChatExtensions`) |
| `rail.tsx` / `side-pane.tsx` / `progress.tsx` | Right rail, artefact pane, Fortschritt |
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

Layout preferences (`railOpen`, `paneWidth`, `badgeSeen`) are bootstrapped from PersonalConfiguration
via `AgentChatControl.putState` and written through `PUT /chat/preferences`.

The Leiste badge is the sum of each section's `badge()` minus the total last stored when the rail was
opened - "N new since you last looked", so a section whose rows simply exist contributes none and
uses `count()` for the number beside its own title instead.

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

`chat-id` is how elements find each other. `<agent-chat>` leaves it empty and `App` falls back to
`props.controlId`, so two chats in one window stay two chats - Consulting's `CopilotRegistry` keeps a
*list* of live panels, and a fixed key would silently merge their transcripts
(`chat-store.test.ts`, `testTwoAppsInOneWindowDoNotShareATranscript`).

### Stability

Exported types and `App` / `extensions` are the contract. File splits inside `ui/src` are not.

Citations: see `doc/citation-contract.md`. Albert records them by calling `cite`; the server drops
any id or URL that did not appear in this turn's other tool results, and leaves every other bracketed
number in the answer alone.

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
