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
   - `PUT /chat/sessions/{id}/messages` – Persist messages to session.
   - `DELETE /chat/sessions/{id}` – Delete session.

2. **SSE API** (real-time push):
   - `GET /chat/events` – SSE connection. `ChatSSEHandler` receives `AgentEvent`s and emits structured SSE frames: `connected`, `run_started`, `turn_started`, `text_delta`, `tool_started`, `tool_completed`, `approval_required`, `message_complete`, `heartbeat`, etc.

Authentication enforced by TopLogic's `ChatSessionFilter` mapped to `/chat/*`.

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
- **`ChatSSEHandler`** – Push-based SSE handler implementing `AgentEventListener`
- **`Chat`** – Thread-safe in-memory container using `CopyOnWriteArrayList<ChatMessage>` (max 500)
- **`ChatSessionManager`** – Manages per-session in-memory state (Chat + ConversationMemory + ConversationLog) with LRU caching (max 5 sessions). Handles session switching with persist/load/rebuild cycle.
- **`ChatHistoryService`** – Static CRUD service for persistent `ChatSession` objects in KnowledgeBase. Messages stored as JSON blob in CLOB attribute. All methods use `ThreadContextManager.inSystemInteraction()`.
- **`ChatSessionRegistry`** – Bridges `WindowScope` (ChatControl) and `HttpSession` (ChatServlet)

## Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe message access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection and synchronized writer access

## Persistence Model

- **Module**: `tl.ai.chat`, **Type**: `ChatSession`
- **Attributes**: `title`, `messages` (JSON CLOB), `createdAt`, `lastUpdated`, `owner` (via `WithOwner`)
- V2 format: `ConversationLog` serialized via `ConversationLogSerializer` (structured turns with parts)
- V1 format (legacy): `ChatMessage` list serialized via `DtoMapper` (flat message array)

## Notes

- Active chat state lives in session memory; completed sessions persisted to KnowledgeBase
- SSE connections have no timeout; stay open until client disconnects
- Background AI tasks run in cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
