# tl-chat

Real-time agentic chat interface for TopLogic applications. Connects users to the multi-agent orchestrator (from `tl-ai-service`) and displays tool calls, agent handoffs, and AI responses as they happen.

**Parent:** `tl-parent-app` | **Dependencies:** `tl-core`, `tl-ai-service`, `jackson-databind`

## Architecture

### Dual API: REST + SSE

Both served by `ChatServlet` at `/chat/*`:

1. **REST API** (JSON):
   - `POST /chat/send` – Send user message, starts background AI processing. Returns `{ taskId, status }`.
   - `GET /chat/updates` – Poll for full chat state (messages, current task, timeline events).
   - `POST /chat/sessions` – Create new chat session.
   - `GET /chat/sessions` – List all sessions for current user.
   - `GET /chat/sessions/{id}` – Load session and activate server-side.
   - `PUT /chat/sessions/{id}/title` – Update session title.
   - `PUT /chat/sessions/{id}/messages` – Persist messages to session.
   - `DELETE /chat/sessions/{id}` – Delete session.

2. **SSE API** (real-time push):
   - `GET /chat/events` – SSE connection. `ChatSSEHandler` polls every 200ms and emits delta events: `connected`, `message`, `tool_call`, `agent_call`, `task_status`, `heartbeat`.

Authentication enforced by TopLogic's `ChatSessionFilter` mapped to `/chat/*`.

### Processing Flow

1. User sends message via `POST /chat/send` or TopLogic AJAX command
2. `ChatProcessor.sendMessage()` adds message to `Chat`, creates `ChatTask`, submits `AIBackgroundTask`
3. Background task calls `OrchestratorAgent.chat(userMessage)` with `ToolCallTracker` bound via `ToolCallTracking.setTracker()`
4. Tool calls and agent handoffs appear in tracker's timeline as they happen
5. `ChatProcessor.applyPollUpdates()` syncs `TraceEntry` objects into `ChatTimelineEvent` objects
6. SSE handler (or polling endpoint) delivers updates to client in real-time
7. On completion, agent response added as `ChatMessage`

### Key Classes

- **`ChatProcessor`** – Static utility: background thread pool, orchestrator execution, state sync
- **`TaskProgressTracker`** – Session-scoped chat state, manages `ChatTask` objects (LRU, max 50)
- **`ChatSSEHandler`** – SSE handler polling for deltas via `ScheduledExecutorService`
- **`Chat`** – Thread-safe in-memory container using `CopyOnWriteArrayList<ChatEntry>` (max 500)
- **`ChatSessionManager`** – Manages per-session in-memory state (Chat + ChatMemory) with LRU caching (max 5 sessions, 20-message memory window). Handles session switching with persist/load/rebuild cycle.
- **`ChatHistoryService`** – Static CRUD service for persistent `ChatSession` objects in KnowledgeBase. Messages stored as JSON blob in CLOB attribute. All methods use `ThreadContextManager.inSystemInteraction()`.
- **`ChatSessionRegistry`** – Bridges `WindowScope` (ChatControl) and `HttpSession` (ChatServlet)

## Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe entry access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection and synchronized writer access
- `TaskProgressTracker` methods that modify task state are synchronized

## Domain Model Immutability

- `ChatMessage`, `ChatTask`, `ChatToolCall`, and `ChatTimelineEvent` are immutable
- `ChatTask` uses builder pattern (`withStatus()`, `withTimelineEvent()`) returning new instances
- `ChatEntry` is a sealed interface with record implementations

## Persistence Model

- **Module**: `tl.ai.chat`, **Type**: `ChatSession`
- **Attributes**: `title`, `messages` (JSON CLOB), `createdAt`, `lastUpdated`, `owner` (via `WithOwner`)
- Chat messages serialized/deserialized between in-memory `ChatEntry` objects and JSON via msgbuf DTOs

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
