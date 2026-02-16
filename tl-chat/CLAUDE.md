# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in the `tl-chat` module.

## Module Overview

The **tl-chat** module provides a real-time agentic chat interface for TopLogic applications. It connects users to the multi-agent orchestrator (from `tl-ai-service`) and displays tool calls, agent handoffs, and AI responses as they happen.

**Key Technologies:**
- TopLogic 7.10.0-SNAPSHOT (enterprise application framework)
- Jackson (JSON serialization for the REST API)
- Server-Sent Events (SSE) for real-time push updates
- `tl-ai-service` for AI orchestration (`OrchestratorAgent`, `ToolCallTracker`)

**Parent:** `tl-parent-app` (TopLogic application parent)

**Dependencies:**
- `tl-core` - TopLogic core framework
- `tl-ai-service` - AI service module (orchestrator, tool call tracking, trace entries)
- `jackson-databind` - JSON serialization/deserialization

## Build Commands

### Build the module
```bash
mvn clean install -pl tl-chat
```

### Run tests
```bash
mvn test -pl tl-chat
```

### Skip tests during build
```bash
mvn clean install -pl tl-chat -DskipTests
```

## Project Structure

### Source Layout
```
tl-chat/
├── src/main/java/com/top_logic/ai/chat/
│   ├── api/                                    # JSON/SSE API layer
│   │   ├── ChatServlet.java                    # HTTP servlet: /chat/send, /chat/updates, /chat/events
│   │   ├── ChatSSEHandler.java                 # SSE handler for real-time push updates
│   │   ├── DtoMapper.java                      # Maps domain objects to DTOs
│   │   ├── ChatSendRequest.java                # Request DTO for POST /chat/send
│   │   ├── ChatSendResponse.java               # Response DTO (taskId + status)
│   │   ├── ChatUpdatesResponse.java            # Full chat state DTO
│   │   ├── MessageDto.java                     # Individual message DTO
│   │   ├── CurrentTaskDto.java                 # Current task with timeline events
│   │   └── TimelineEventDto.java               # Tool call or agent call event DTO
│   └── layout/                                 # TopLogic UI layer
│       ├── ChatProcessor.java                  # Core processing: orchestrator execution, polling, state sync
│       ├── TaskProgressTracker.java            # Session-scoped chat state and task management
│       ├── ChatSessionRegistry.java            # Bridges WindowScope and HttpSession
│       ├── ChatControl.java                    # TopLogic UI control (server-side rendering)
│       ├── ChatComponentBuilder.java           # Model builder for TopLogic forms
│       ├── Chat.java                           # In-memory chat container (thread-safe)
│       ├── ChatEntry.java                      # Sealed interface: MessageEntry, TaskEntry, TaskSnapshotEntry
│       ├── ChatMessage.java                    # Immutable message (user, text, timestamp)
│       ├── ChatTask.java                       # Immutable task with status and timeline events
│       ├── ChatTimelineEvent.java              # Unified timeline entry (TOOL_CALL or AGENT_CALL)
│       ├── ChatToolCall.java                   # Immutable tool invocation (name, args, result)
│       └── ChatNotifier.java                   # Lightweight notification messages
├── src/main/resources/META-INF/
│   └── web-fragment.xml                        # Servlet and filter registration
├── src/main/webapp/
│   ├── script/
│   │   └── chat-client.js                      # Client-side JavaScript (SSE, incremental DOM updates)
│   ├── style/
│   │   └── chatStyles.css                      # Chat UI styles (bubbles, timeline, collapsibles)
│   └── WEB-INF/
│       ├── conf/
│       │   ├── tl-chat.conf.xml                # Module configuration
│       │   └── tl-chat.conf.config.xml         # Service configuration
│       └── layouts/tl-chat/chat/
│           ├── chatDemo.layout.xml             # Demo layout
│           ├── demoChat.xml                    # Form definition using ChatComponentBuilder
│           └── tabLayout.layout.xml            # Tab layout template
└── src/test/java/                              # Test sources
```

## Architecture

### Dual API: REST + SSE

The module provides two complementary APIs, both served by `ChatServlet` at `/chat/*`:

1. **REST API** (JSON request/response):
   - `POST /chat/send` - Send a user message, starts background AI processing. Returns `{ taskId, status }`.
   - `GET /chat/updates` - Poll for full chat state (messages, current task, timeline events).

2. **SSE API** (real-time push):
   - `GET /chat/events` - Opens an SSE connection. The `ChatSSEHandler` polls `TaskProgressTracker` every 200ms and emits delta events:
     - `connected` - Initial connection with current sequence numbers
     - `message` - New chat message
     - `tool_call` - Tool call start or completion
     - `agent_call` - Agent handoff event
     - `task_status` - Task status change (completed/error)
     - `heartbeat` - Keep-alive ping (every 15s)

Authentication is enforced by TopLogic's `SessionCheckingFilter` mapped to `/chat/*` in the `web-fragment.xml`.

### Processing Flow

1. **User sends message** via `POST /chat/send` or TopLogic AJAX command
2. `ChatProcessor.sendMessage()` adds the message to `Chat`, creates a `ChatTask`, and submits an `AIBackgroundTask` to the background executor
3. The background task calls `OrchestratorAgent.chat(userMessage)` with a `ToolCallTracker` bound via `ToolCallTracking.setTracker()`
4. As the orchestrator works, tool calls and agent handoffs appear in the tracker's timeline
5. `ChatProcessor.applyPollUpdates()` syncs new `TraceEntry` objects from the tracker into `TaskProgressTracker` as `ChatTimelineEvent` objects
6. The SSE handler (or polling endpoint) delivers updates to the client in real-time
7. On completion, the agent response is added as a `ChatMessage`

### Key Classes

- **`ChatProcessor`** - Static utility class with all processing logic. Manages background thread pool, orchestrator execution, and state synchronization between background threads and UI/SSE handlers.
- **`TaskProgressTracker`** - Session-scoped container for chat state. Stores the `Chat` instance, manages `ChatTask` objects (with LRU eviction, max 50 tasks), and tracks the current `AIProcessingState`.
- **`ChatSSEHandler`** - SSE handler using a `ScheduledExecutorService` to poll for deltas. Tracks sequence numbers for messages and timeline events to emit only new items.
- **`Chat`** - Thread-safe in-memory chat container using `CopyOnWriteArrayList<ChatEntry>`. Max 500 entries with automatic eviction.
- **`ChatSessionRegistry`** - Bridges TopLogic's `WindowScope` (used by `ChatControl`) and `HttpSession` (used by `ChatServlet`) so both share the same `TaskProgressTracker`.

### Client-Side

- **`chat-client.js`** - JavaScript client that manages SSE connections with auto-reconnect, performs incremental DOM updates (no full repaint), and handles optimistic UI updates.
- **`chatStyles.css`** - Modern monochrome chat UI with bubble layout, inline task progress, collapsible tool/agent calls, and CSS custom properties for theming.

## Servlet Registration

The `web-fragment.xml` registers both the servlet and session-checking filter:

```xml
<web-fragment xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="3.1">
    <name>tl-chat</name>

    <filter>
        <filter-name>chatSessionCheckingFilter</filter-name>
        <filter-class>com.top_logic.base.accesscontrol.SessionCheckingFilter</filter-class>
        <async-supported>true</async-supported>
    </filter>
    <filter-mapping>
        <filter-name>chatSessionCheckingFilter</filter-name>
        <url-pattern>/chat/*</url-pattern>
    </filter-mapping>

    <servlet>
        <servlet-name>ChatServlet</servlet-name>
        <servlet-class>com.top_logic.ai.chat.api.ChatServlet</servlet-class>
        <async-supported>true</async-supported>
    </servlet>
    <servlet-mapping>
        <servlet-name>ChatServlet</servlet-name>
        <url-pattern>/chat/*</url-pattern>
    </servlet-mapping>
</web-fragment>
```

**Location:** `tl-chat/src/main/resources/META-INF/web-fragment.xml`

## Module Relationships

```
tl-chat  ──depends on──>  tl-ai-service  (OrchestratorAgent, ToolCallTracker, TraceEntry)
tl-chat  ──depends on──>  tl-core        (TopLogic framework: layouts, controls, sessions)

tl-chat does NOT depend on tl-ai-mcp-server (both depend on tl-ai-service independently)
```

## Coding Conventions

### JavaDoc Standards

**DO NOT** add `@author` tags to JavaDoc. Author information is tracked through git.

### Internationalization (I18N)

**DO NOT** manually edit internationalization message files (`messages_en.properties`, `messages_de.properties`). These files are automatically generated by the build process.

### Thread Safety

- `Chat` uses `CopyOnWriteArrayList` for thread-safe entry access
- `AIProcessingState` uses `synchronized` blocks and `volatile` fields
- `ChatSSEHandler` uses `AtomicBoolean` for close detection and synchronized writer access
- `TaskProgressTracker` methods that modify task state are synchronized

### Domain Model Immutability

- `ChatMessage`, `ChatTask`, `ChatToolCall`, and `ChatTimelineEvent` are immutable
- `ChatTask` uses a builder pattern (`withStatus()`, `withTimelineEvent()`) that returns new instances
- `ChatEntry` is a sealed interface with record implementations

## Notes

- This module uses `tl-parent-app` as parent, making it a deployable TopLogic application
- The chat state lives in session memory (not persisted to database)
- `TaskProgressTracker` uses LRU eviction: max 50 tasks, max 500 chat entries
- SSE connections have no timeout; they stay open until the client disconnects
- Background AI tasks run in a cached thread pool (`ChatProcessor.BACKGROUND_EXECUTOR`)
- SSE polling uses a fixed-size scheduled thread pool (`ChatSSEHandler.SSE_EXECUTOR`)
