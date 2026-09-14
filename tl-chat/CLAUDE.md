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
- **`ChatSessionRegistry`** – Bridges `WindowScope` (ChatControl) and `HttpSession` (ChatServlet)

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

### Toolchain and build output

| | |
|--|--|
| Sources | `ui/src` (TypeScript, `.tsx`, `jsx: "react"` - the classic runtime, because React comes from the host) |
| Bundler | Vite library build, entry `ui/src/main.tsx` |
| Output | `src/main/webapp/script/tl-chat-ui.js` and `tl-chat-ui.css` - **git-ignored build output, never commit it** |
| Node | installed by `frontend-maven-plugin` into `ui/node/` (v20.10.0); a local Node 20 works too |
| Dependencies | `marked` (MIT) + `dompurify` (Apache-2.0 or MPL-2.0) for markdown, `highlight.js` (BSD-3-Clause) for code blocks - core plus 13 registered grammars. All three are bundled into `tl-chat-ui.js`, so their licences travel with the WAR. |

`mvn -pl tl-chat install` runs, in `generate-resources`: `npm install`, then `npm run typecheck`
(`tsc --noEmit`), then `vitest run`, then `npm run build`. The type check is a build step of its own
because the Vite library build strips types without checking them.

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

`GET /chat/capabilities` is filtered by the acting account (#392): it lists the agents that account
may run, the models it may choose and the MCP servers it may attach, so the menu cannot offer a turn
that `/chat/send` would refuse. A request that names something else anyway is answered **403** with a
sentence in the user's language, which the composer shows in the notice strip in the neutral tone.

### Module map

| File | What it owns |
|--|--|
| `App.tsx` | SSE wiring, session lifecycle, run state, connection health |
| `composer.tsx` | Input, plus menu, model picker, attachments, drag and drop, improve-prompt |
| `thread.tsx` | Transcript: user bubbles, answers, run meta line, trace panels |
| `context-usage.tsx` | The context ring and its popover, plus how each number is derived |
| `prompts.tsx` | Approval and question cards above the composer, with their countdowns |
| `markdown.tsx` / `highlight.ts` | Markdown rendering, sanitizing, code blocks |
| `api.ts` | Every call to `/chat/*`, plus the persisted-transcript to message mapping |
| `reducer.ts` / `store.ts` / `types.ts` | Transcript reducer, run snapshot store, shared types |

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
