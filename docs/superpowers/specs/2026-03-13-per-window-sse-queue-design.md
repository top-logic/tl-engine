# Per-Window SSEUpdateQueue Design

## Problem

`SSEUpdateQueue` exists once per session, while each browser window has its own SSE connection. This creates three classes of issues:

1. **Correctness**: Controls from a closed window remain registered. Control IDs are globally unique but could collide if made window-local. State events reach windows that don't own the controls.
2. **Efficiency**: Every event is broadcast to every connection. Each window processes and discards events for other windows' controls.
3. **Lifecycle management**: No way to bulk-cleanup controls when a window closes. The queue doesn't know which controls belong to which window.

## Solution

Move from one `SSEUpdateQueue` per session to one per window. `ReactWindowRegistry` becomes the owner of all per-window queues.

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

**Change:**
- Single `volatile SSEConnection _connection` field
- `setConnection(AsyncContext)` replaces any existing connection (closes old, sends full state to new)
- `clearConnection(AsyncContext)` nulls out the connection only if it matches (prevents race on reconnect)
- `flush()` writes to the single connection (no-op if null)
- `sendFullState()` sends only this window's controls
- Keep `AtomicInteger _nextId` — IDs are window-local

**Add:**
- `shutdown()` — cancels heartbeat, closes connection, clears controls and pending events. Called by registry on window close or session invalidation.

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
- `openWindow()` — enqueues `WindowOpenEvent` to the **opener's** queue (via `getQueue(openerWindowId)`), not a shared queue
- `windowClosed(windowId)` — additionally calls `queue.shutdown()` and removes the queue from the map
- `valueUnbound()` — iterates all queues and calls `shutdown()` on each, then clears the map

### SSEServlet

**Current:** `SSEUpdateQueue.forSession(session)` then `queue.addConnection(asyncContext)`.

**New:**
1. Extract `windowName` from request parameter (`request.getParameter("windowName")`)
2. `ReactWindowRegistry.forSession(session).getOrCreateQueue(windowName)`
3. `queue.setConnection(asyncContext)`
4. AsyncListener calls `queue.clearConnection(asyncContext)` on complete/timeout/error

### ReactServlet

**Command dispatch:**
- Use `ReactWindowRegistry.forSession(session)` instead of `SSEUpdateQueue.forSession(session)`
- Look up controls via `registry.getQueue(windowName).getControl(controlId)` — `windowName` from command payload is mandatory
- Acquire `registry.getRequestLock()` before dispatching commands/uploads, release in finally block
- Lock scope: command execution + `forwardPendingUpdates()` + implicit flush

**Data download / state queries:**
- Add `windowName` parameter to requests
- Look up control via the window's queue

**Window lifecycle commands (`windowClosed`, `windowBlocked`):**
- Continue using `registry.windowClosed()` — it now also shuts down the queue

**`forwardPendingUpdates()`:**
- Pass the window-specific queue instead of the session-level queue
- Legacy control updates are enqueued to the correct window's queue

### ReactContext / DefaultReactContext

- `getSSEQueue()` returns the per-window queue (already correct — DefaultReactContext is constructed with a specific queue)
- `allocateId()` delegates to the queue's `allocateId()` (unchanged, IDs are window-local)

### DisplayContextAdapter

**Change line 37:**
- From: `_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession())`
- To: Look up the queue from `ReactWindowRegistry.forSession(session).getOrCreateQueue(windowName)`
- Uses `_windowName` (extracted on line 35) for the lookup

### ViewServlet

**Change lines 95-96:**
- From: `SSEUpdateQueue sseQueue = SSEUpdateQueue.forSession(session)`
- To: Get queue from `windowRegistry.getOrCreateQueue(windowName)`
- Pass this per-window queue to `DefaultReactContext` constructor

### Client-Side (sse-client.ts / tl-react-bridge.ts)

**tl-react-bridge.ts:**
- Pass `windowName` as query parameter in SSE URL: `connect(getApiBase() + 'react-api/events?windowName=' + encodeURIComponent(windowName))`
- The `windowName` is already available from the `mount()` call parameters

**sse-client.ts:**
- No structural change. Each window already has its own `EventSource` instance (each window is a separate JS context).

## Window Lifecycle Events

- `WindowOpenEvent` goes to the opener's queue only (no broadcast)
- `WindowCloseEvent` / `WindowFocusEvent` go to the target window's queue directly
- The `targetWindowId` field becomes informational (routing is implicit from queue ownership)

## Concurrency

- **Session-wide `ReentrantLock`** in `ReactWindowRegistry` serializes all command processing across windows
- Prevents concurrent modifications to shared model state (e.g., knowledge base objects)
- Acquired by `ReactServlet` before command/upload dispatch, released in finally block
- SSE writes are outside the lock (heartbeat, flush happen independently)

## Files Changed

| File | Module | Change |
|------|--------|--------|
| `SSEUpdateQueue.java` | `com.top_logic.layout.react` | Single connection, remove session storage, add `shutdown()` |
| `ReactWindowRegistry.java` | `com.top_logic.layout.react` | Own per-window queues, add request lock |
| `SSEServlet.java` | `com.top_logic.layout.react` | Use registry + windowName for queue lookup |
| `ReactServlet.java` | `com.top_logic.layout.react` | Use registry for control lookup, acquire request lock |
| `DisplayContextAdapter.java` | `com.top_logic.layout.react` | Get queue from registry by windowName |
| `ViewServlet.java` | `com.top_logic.layout.view` | Get queue from registry by windowName |
| `tl-react-bridge.ts` | `com.top_logic.layout.react` | Add windowName to SSE URL |

No new classes. No new event types.
