# Per-Window SSEUpdateQueue Design

## Problem

`SSEUpdateQueue` exists once per session, while each browser window has its own SSE connection. This creates three classes of issues:

1. **Correctness**: Controls from a closed window remain registered. Control IDs are globally unique but could collide if made window-local. State events reach windows that don't own the controls.
2. **Efficiency**: Every event is broadcast to every connection. Each window processes and discards events for other windows' controls.
3. **Lifecycle management**: No way to bulk-cleanup controls when a window closes. The queue doesn't know which controls belong to which window.

## Solution

Move from one `SSEUpdateQueue` per session to one per window. `ReactWindowRegistry` becomes the owner of all per-window queues.

## Key Assumption

Each browser window is a **separate browser document** opened via `window.open()`. There is no overlay/iframe pattern where multiple windows share the same DOM. This means window-local control IDs (e.g., both windows having `v1`) are safe because they exist in different documents with independent DOM trees and independent JS contexts.

## Architecture

```
Session (HTTP)
└── ReactWindowRegistry (1 per session, session attribute)
    ├── windowId "v1a2b3c" -> SSEUpdateQueue (1 connection, controls for this window)
    ├── windowId "v4d5e6f" -> SSEUpdateQueue (1 connection, controls for this window)
    └── session-wide ReentrantLock (serializes command processing)
```

## Detailed Changes

### SSEUpdateQueue

**Remove:**
- `forSession(HttpSession)` static factory — no longer stored as session attribute
- `List<SSEConnection> _connections` — replaced by single connection
- `HttpSessionBindingListener` — lifecycle managed by registry
- `AtomicInteger _nextConnectionId` — single connection, no need for connection IDs

**Change:**
- Single `volatile SSEConnection _connection` field (keep `SSEConnection` inner class for debug ID)
- `setConnection(AsyncContext)` replaces any existing connection (closes old, sends full state to new)
- `clearConnection(AsyncContext)` nulls out the connection only if it matches (prevents race on reconnect)
- `flush()` writes to the single connection (no-op if null)
- `sendFullState()` sends only this window's controls
- Keep `AtomicInteger _nextId` — IDs are window-local (safe because each window is a separate browser document)

**Add:**
- `shutdown()` — cancels heartbeat, closes connection, clears controls and pending events. Called by registry on window close or session invalidation. After shutdown, `enqueue()` is a no-op.

### ReactWindowRegistry

**Change:**
- Remove `SSEUpdateQueue _sseQueue` constructor parameter — no longer holds a single session-level queue
- `forSession(HttpSession)` no longer creates/depends on `SSEUpdateQueue.forSession()`

**Add:**
- `Map<String, SSEUpdateQueue> _windowQueues` — keyed by window ID
- `getOrCreateQueue(String windowId)` — lazily creates a queue for any window (main or popup). Thread-safe via `computeIfAbsent`.
- `getQueue(String windowId)` — returns existing queue or null
- `ReentrantLock _requestLock` — session-wide lock for command serialization
- `getRequestLock()` — returns the lock

**Update:**
- `openWindow()` — enqueues `WindowOpenEvent` to the **opener's** queue via `getQueue(openerContext.getWindowName())`, not a shared queue
- `windowClosed(windowId)` — additionally calls `queue.shutdown()` and removes the queue from the map
- `valueUnbound()` — iterates all queues and calls `shutdown()` on each, then clears the map

**Note:** All 5 call sites of `SSEUpdateQueue.forSession()` (SSEServlet, ReactServlet x2, ViewServlet, DisplayContextAdapter, ReactWindowRegistry) must be updated atomically with the removal of `forSession()`.

### SSEServlet

**Current:** `SSEUpdateQueue.forSession(session)` then `queue.addConnection(asyncContext)`.

**New:**
1. Extract `windowName` from request parameter (`request.getParameter("windowName")`)
2. `ReactWindowRegistry.forSession(session).getOrCreateQueue(windowName)`
3. `queue.setConnection(asyncContext)`
4. AsyncListener calls `queue.clearConnection(asyncContext)` on complete/timeout/error

### ReactServlet

**Command dispatch (`/command`):**
- Use `ReactWindowRegistry.forSession(session)` instead of `SSEUpdateQueue.forSession(session)`
- Look up controls via `registry.getQueue(windowName).getControl(controlId)` — `windowName` from command payload is mandatory
- Acquire `registry.getRequestLock()` before dispatching commands/uploads, release in finally block
- Lock scope: command execution + `forwardPendingUpdates()` + implicit flush

**State query (`/state`):**
- Add `windowName` request parameter
- Look up control via `registry.getQueue(windowName).getControl(controlId)` instead of `SSEUpdateQueue.forSession(session).getControl(controlId)`

**Data download (`/data`):**
- Add `windowName` request parameter
- Look up control via `registry.getQueue(windowName).getControl(controlId)`

**File upload (`/upload`):**
- Same pattern: use `windowName` parameter + registry queue lookup

**Window lifecycle commands (`windowClosed`, `windowBlocked`):**
- Continue using `registry.windowClosed()` — it now also shuts down the queue

**`forwardPendingUpdates()`:**
- Pass the window-specific queue instead of the session-level queue
- Legacy control updates are enqueued to the correct window's queue

### ReactContext / DefaultReactContext

- `getSSEQueue()` returns the per-window queue (already correct — `DefaultReactContext` is a pure value holder constructed with a specific queue by its callers)
- `allocateId()` delegates to the queue's `allocateId()` (unchanged, IDs are window-local)
- **No code changes to `DefaultReactContext.java`** — callers (`ViewServlet`, `DisplayContextAdapter`) are responsible for passing the correct per-window queue

### DisplayContextAdapter

**Change `_sseQueue` initialization in the constructor:**
- From: `_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession())`
- To: `_sseQueue = ReactWindowRegistry.forSession(session).getOrCreateQueue(_windowName)`
- Uses `_windowName` (already extracted from `context.getLayoutContext().getWindowId().getWindowName()`) for the lookup

### ViewServlet

**Change queue acquisition in `doGet()`:**
- From: `SSEUpdateQueue sseQueue = SSEUpdateQueue.forSession(session)`
- To: Get queue from `windowRegistry.getOrCreateQueue(windowName)`
- Pass this per-window queue to `DefaultReactContext` constructor

### Client-Side (sse-client.ts / tl-react-bridge.ts)

**tl-react-bridge.ts `mount()` function:**
- The `windowName` parameter is already received by `mount()`. Change the `connect()` calls (lines 95, 97) to include it in the URL:
  ```typescript
  connect(getApiBase() + 'react-api/events?windowName=' + encodeURIComponent(resolvedWindowName));
  ```
- Both the initial connect and the reconnect-on-reload path must include the `windowName`.

**`useTLDataUrl` hook:**
- Append `&windowName=<windowName>` to the data download URL so the server can look up the control from the correct window queue.

**`useTLCommand` / `useTLUpload` hooks:**
- Already include `windowName` in their POST payloads. No change needed.

**sse-client.ts:**
- No structural change. Each window is a separate browser document with its own JS context, so each has its own `EventSource` instance and `_listeners` map independently.

## Window Lifecycle Events

**`WindowOpenEvent`:** Enqueued to the **opener's** queue only (the opener's browser window receives it and calls `window.open()`). No broadcast.

**`WindowCloseEvent` (server-initiated):** Enqueued to the **opener's** queue, telling the opener window to tear down the popup UI. The popup's own queue is then shut down by `registry.windowClosed()`.

**`WindowCloseEvent` (client-initiated):** The client sends a POST command (`windowClosed`) to `ReactServlet`. The server calls `registry.windowClosed()` which shuts down the popup's queue. No SSE event is needed — the client already knows it closed.

**`WindowFocusEvent`:** Enqueued to the **target window's** queue directly (looked up by window ID from the registry).

The `targetWindowId` field on these events becomes informational (routing is implicit from queue ownership).

## Concurrency

- **Session-wide `ReentrantLock`** in `ReactWindowRegistry` serializes all command processing across windows
- Prevents concurrent modifications to shared model state (e.g., knowledge base objects)
- Acquired by `ReactServlet` before command/upload dispatch, released in finally block
- SSE writes are outside the lock (heartbeat, flush happen independently)

## Heartbeat

Each per-window queue manages its own heartbeat timer. With N open windows, there are N timers (each firing every 30 seconds via `SchedulerService`). This is acceptable for typical window counts (1-5 per session). The timer starts when a connection is set and cancels when the connection is cleared or the queue is shut down.

## Files Changed

| File | Module | Change |
|------|--------|--------|
| `SSEUpdateQueue.java` | `com.top_logic.layout.react` | Single connection, remove session storage, add `shutdown()` |
| `ReactWindowRegistry.java` | `com.top_logic.layout.react` | Own per-window queues, add request lock |
| `SSEServlet.java` | `com.top_logic.layout.react` | Use registry + windowName for queue lookup |
| `ReactServlet.java` | `com.top_logic.layout.react` | Use registry for all control lookups, acquire request lock |
| `DisplayContextAdapter.java` | `com.top_logic.layout.react` | Get queue from registry by windowName |
| `ViewServlet.java` | `com.top_logic.layout.view` | Get queue from registry by windowName |
| `tl-react-bridge.ts` | `com.top_logic.layout.react` | Add windowName to SSE URL and data URL |

No new classes. No new event types. `DefaultReactContext.java` and `ReactControl.java` are unchanged.
