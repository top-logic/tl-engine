# Per-Window SSEUpdateQueue Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor SSEUpdateQueue from one-per-session to one-per-window, owned by ReactWindowRegistry, so that each browser window gets its own isolated SSE channel, controls, and lifecycle.

**Architecture:** ReactWindowRegistry becomes the session-level authority owning a `Map<String, SSEUpdateQueue>` keyed by window ID. Each SSEUpdateQueue holds exactly one SSEConnection and only the controls for that window. A session-wide ReentrantLock serializes command processing across windows.

**Tech Stack:** Java 17, Jakarta Servlet, Maven, TypeScript (Vite build via frontend-maven-plugin)

**Spec:** `docs/superpowers/specs/2026-03-13-per-window-sse-queue-design.md`

---

## Chunk 1: Server-Side Refactoring

### Task 1: Refactor SSEUpdateQueue to single-connection model

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEUpdateQueue.java`

This is the core change. Transform SSEUpdateQueue from a session-scoped singleton with a list of connections to a window-scoped queue with a single connection.

- [ ] **Step 1: Remove session-attribute storage and HttpSessionBindingListener**

Remove these elements from `SSEUpdateQueue.java`:
- The `SESSION_ATTRIBUTE_KEY` constant (line 50)
- The `forSession(HttpSession)` static factory method (lines 82-94)
- The `implements HttpSessionBindingListener` from the class declaration (line 48)
- The `valueUnbound(HttpSessionBindingEvent)` method (lines 227-244)
- The `AtomicInteger _nextConnectionId` field (line 69)

- [ ] **Step 2: Replace connection list with single connection**

Replace:
```java
private final List<SSEConnection> _connections = new CopyOnWriteArrayList<>();
```

With:
```java
private volatile SSEConnection _connection;
private volatile boolean _shutdown;
```

Remove imports: `java.util.List`, `java.util.concurrent.CopyOnWriteArrayList`.

- [ ] **Step 3: Rewrite connection management methods**

Replace `addConnection(AsyncContext)` and `removeConnection(AsyncContext)` with:

```java
/**
 * Sets the SSE connection for this window's queue.
 *
 * <p>
 * Replaces any existing connection (closes the old one). Sends the full state of all
 * registered controls to the new connection. This handles both initial connection and
 * reconnects cleanly.
 * </p>
 */
public void setConnection(AsyncContext asyncContext) {
    SSEConnection old = _connection;
    SSEConnection newConn = new SSEConnection(asyncContext);
    _connection = newConn;
    Logger.info("SSE connection set for queue@" + System.identityHashCode(this)
        + ", replacing=" + (old != null), SSEUpdateQueue.class);
    if (old != null) {
        try {
            old.getContext().complete();
        } catch (Exception ex) {
            // Old connection already closed, ignore.
        }
    }
    sendFullState(newConn);
    ensureHeartbeat();
}

/**
 * Clears the SSE connection, but only if it matches the given context.
 *
 * <p>
 * This prevents a race where a reconnect has already replaced the connection:
 * the old connection's async listener fires, but the new connection is already set.
 * </p>
 */
public void clearConnection(AsyncContext asyncContext) {
    SSEConnection current = _connection;
    if (current != null && current.getContext() == asyncContext) {
        _connection = null;
        Logger.info("SSE connection cleared for queue@" + System.identityHashCode(this),
            SSEUpdateQueue.class);
    }
    cancelHeartbeatIfEmpty();
}
```

- [ ] **Step 4: Rewrite flush() and sendHeartbeat() for single connection**

Replace `flush()`:
```java
public void flush() {
    SSEConnection conn = _connection;
    if (conn == null) {
        return;
    }
    SSEEvent event;
    while ((event = _pendingEvents.poll()) != null) {
        String message = toDataMessage(toJson(event));
        if (message == null) {
            continue;
        }
        if (!writeOrDisconnect(conn, message)) {
            return;
        }
    }
}
```

Replace `sendHeartbeat()`:
```java
private void sendHeartbeat() {
    SSEConnection conn = _connection;
    if (conn != null) {
        writeOrDisconnect(conn, HEARTBEAT_MESSAGE);
    }
    cancelHeartbeatIfEmpty();
}
```

- [ ] **Step 5: Rewrite writeOrRemove to writeOrDisconnect**

Replace `writeOrRemove(SSEConnection, String)`:
```java
/**
 * Writes a message to the connection, clearing it if the write fails.
 *
 * @return {@code true} if the write succeeded.
 */
private boolean writeOrDisconnect(SSEConnection connection, String message) {
    try {
        writeToConnection(connection.getContext(), message);
        return true;
    } catch (IOException ex) {
        Logger.info("SSE connection lost for queue@" + System.identityHashCode(this),
            SSEUpdateQueue.class);
        _connection = null;
        return false;
    }
}
```

- [ ] **Step 6: Update sendFullState to use renamed method**

In `sendFullState(SSEConnection)`, replace the call to `writeOrRemove(connection, message)` with `writeOrDisconnect(connection, message)`. The method signature and logic are otherwise unchanged — it already works with a single connection parameter.

- [ ] **Step 7: Update cancelHeartbeatIfEmpty for single connection**

Replace:
```java
private synchronized void cancelHeartbeatIfEmpty() {
    if (_connections.isEmpty() && _heartbeatTask != null) {
```

With:
```java
private synchronized void cancelHeartbeatIfEmpty() {
    if (_connection == null && _heartbeatTask != null) {
```

- [ ] **Step 8: Add shutdown() method**

Add after `cancelHeartbeatIfEmpty()`:
```java
/**
 * Shuts down this queue permanently.
 *
 * <p>
 * Cancels the heartbeat, closes the SSE connection, and clears all controls and pending
 * events. After shutdown, {@link #enqueue(SSEEvent)} is a no-op.
 * </p>
 */
public void shutdown() {
    _shutdown = true;
    synchronized (this) {
        if (_heartbeatTask != null) {
            _heartbeatTask.cancel(false);
            _heartbeatTask = null;
        }
    }
    SSEConnection conn = _connection;
    _connection = null;
    if (conn != null) {
        try {
            conn.getContext().complete();
        } catch (Exception ex) {
            // Connection already closed, ignore.
        }
    }
    _pendingEvents.clear();
    _controls.clear();
}
```

- [ ] **Step 9: Guard enqueue() with shutdown check**

Update `enqueue()`:
```java
public void enqueue(SSEEvent event) {
    if (_shutdown) {
        return;
    }
    _pendingEvents.add(event);
    flush();
}
```

- [ ] **Step 10: Simplify SSEConnection inner class**

Remove the `_id` field and constructor parameter since there's only one connection:
```java
static final class SSEConnection {
    private final AsyncContext _context;

    SSEConnection(AsyncContext context) {
        _context = context;
    }

    AsyncContext getContext() {
        return _context;
    }
}
```

- [ ] **Step 11: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.react && mvn compile -DskipTests=true 2>&1 | tail -20`

Expected: Compilation errors in SSEServlet, ReactServlet, ReactWindowRegistry, DisplayContextAdapter (they still reference `SSEUpdateQueue.forSession()`). This is expected — we'll fix those in subsequent tasks.

- [ ] **Step 12: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEUpdateQueue.java
git commit -m "Ticket #29108: Refactor SSEUpdateQueue to single-connection per-window model."
```

---

### Task 2: Add queue ownership to ReactWindowRegistry

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java`

- [ ] **Step 1: Add window queue map, request lock, and new imports**

Add imports:
```java
import java.util.concurrent.ConcurrentHashMap;  // already imported
import java.util.concurrent.locks.ReentrantLock;
```

Add fields after `_windows`:
```java
private final ConcurrentHashMap<String, SSEUpdateQueue> _windowQueues = new ConcurrentHashMap<>();

private final ReentrantLock _requestLock = new ReentrantLock();
```

- [ ] **Step 2: Remove single-queue dependency**

Remove the `_sseQueue` field (line 38).

Remove the constructor that takes `SSEUpdateQueue` (lines 46-48). Replace with a no-arg constructor:
```java
/**
 * Creates a new {@link ReactWindowRegistry}.
 */
public ReactWindowRegistry() {
}
```

- [ ] **Step 3: Update forSession() to not depend on SSEUpdateQueue.forSession()**

Replace `forSession()` (lines 53-67):
```java
public static ReactWindowRegistry forSession(HttpSession session) {
    ReactWindowRegistry registry =
        (ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
    if (registry == null) {
        synchronized (session) {
            registry = (ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
            if (registry == null) {
                registry = new ReactWindowRegistry();
                session.setAttribute(SESSION_ATTRIBUTE_KEY, registry);
            }
        }
    }
    return registry;
}
```

- [ ] **Step 4: Add getOrCreateQueue() and getQueue()**

```java
/**
 * Returns the {@link SSEUpdateQueue} for the given window, creating it if necessary.
 */
public SSEUpdateQueue getOrCreateQueue(String windowId) {
    return _windowQueues.computeIfAbsent(windowId, id -> new SSEUpdateQueue());
}

/**
 * Returns the {@link SSEUpdateQueue} for the given window, or {@code null}.
 */
public SSEUpdateQueue getQueue(String windowId) {
    return _windowQueues.get(windowId);
}

/**
 * Returns the session-wide request lock for serializing command processing.
 */
public ReentrantLock getRequestLock() {
    return _requestLock;
}
```

- [ ] **Step 5: Update openWindow() to enqueue to opener's queue**

In `openWindow(ReactContext openerContext, IReactControl rootControl, WindowOptions options, Runnable closeCallback)` (lines 111-129), replace `_sseQueue.enqueue(event)` with:
```java
SSEUpdateQueue openerQueue = getQueue(openerContext.getWindowName());
if (openerQueue != null) {
    openerQueue.enqueue(event);
}
```

- [ ] **Step 6: Update windowClosed() to shut down the window's queue**

In `windowClosed(String windowId)` (lines 144-166), after the existing cleanup logic, add queue shutdown:
```java
// Shut down the window's SSE queue.
SSEUpdateQueue queue = _windowQueues.remove(windowId);
if (queue != null) {
    queue.shutdown();
}
```

- [ ] **Step 7: Update valueUnbound() to shut down all queues**

In `valueUnbound()` (lines 168-182), add queue shutdown before clearing windows:
```java
@Override
public void valueUnbound(HttpSessionBindingEvent event) {
    for (SSEUpdateQueue queue : _windowQueues.values()) {
        queue.shutdown();
    }
    _windowQueues.clear();
    for (WindowEntry entry : _windows.values()) {
        IReactControl rootControl = entry.getRootControl();
        if (rootControl instanceof ReactControl) {
            ((ReactControl) rootControl).cleanupTree();
        }
    }
    _windows.clear();
}
```

- [ ] **Step 8: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java
git commit -m "Ticket #29108: Add per-window queue ownership and request lock to ReactWindowRegistry."
```

---

### Task 3: Update SSEServlet to use per-window queue

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEServlet.java`

- [ ] **Step 1: Rewrite doGet() to use ReactWindowRegistry**

Replace the entire `doGet()` method:
```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = request.getSession(false);
    if (session == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
        return;
    }

    String windowName = request.getParameter("windowName");
    if (windowName == null || windowName.isEmpty()) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing windowName parameter.");
        return;
    }

    response.setContentType("text/event-stream");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    AsyncContext asyncContext = request.startAsync();
    asyncContext.setTimeout(0);

    ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
    SSEUpdateQueue queue = registry.getOrCreateQueue(windowName);
    queue.setConnection(asyncContext);

    asyncContext.addListener(new AsyncListener() {
        @Override
        public void onComplete(AsyncEvent event) {
            queue.clearConnection(asyncContext);
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            queue.clearConnection(asyncContext);
        }

        @Override
        public void onError(AsyncEvent event) {
            queue.clearConnection(asyncContext);
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            // Nothing to do.
        }
    });

    try {
        PrintWriter writer = response.getWriter();
        writer.write(": connected\n\n");
        writer.flush();
    } catch (IOException ex) {
        Logger.warn("Failed to write SSE connection comment.", ex, SSEServlet.class);
        queue.clearConnection(asyncContext);
    }
}
```

- [ ] **Step 2: Update imports**

Replace `import com.top_logic.layout.react.servlet.SSEUpdateQueue;` (if present as self-package import, it may not be needed) with:
```java
import com.top_logic.layout.react.window.ReactWindowRegistry;
```

Keep the `SSEUpdateQueue` import since it's still used (same package, so implicit).

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/SSEServlet.java
git commit -m "Ticket #29108: Update SSEServlet to use per-window queue from ReactWindowRegistry."
```

---

### Task 4: Update ReactServlet to use per-window queue and request lock

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java`

- [ ] **Step 1: Add import for ReactWindowRegistry and ReentrantLock**

Add:
```java
import java.util.concurrent.locks.ReentrantLock;
```

`ReactWindowRegistry` is already imported (line 62).

- [ ] **Step 2: Create helper method for queue lookup**

Add a private helper method:
```java
/**
 * Looks up the per-window SSE queue for the given window name.
 *
 * @return The queue, or {@code null} if the window name is missing or unknown.
 */
private SSEUpdateQueue getWindowQueue(HttpSession session, String windowName) {
    if (windowName == null || windowName.isEmpty()) {
        Logger.warn("Missing windowName in request.", ReactServlet.class);
        return null;
    }
    ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
    return registry.getQueue(windowName);
}
```

- [ ] **Step 3: Update handleCommand() to use per-window queue and request lock**

In `handleCommand()` (lines 215-292), replace the queue lookup and command dispatch:

Replace lines 261-291 (from `SSEUpdateQueue queue = ...` to the end of the method) with:
```java
SSEUpdateQueue queue = getWindowQueue(session, windowName);
if (queue == null) {
    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing windowName.");
    return;
}
ReactCommandTarget control = queue.getControl(controlId);
if (control == null) {
    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
    return;
}

// Obtain the real DisplayContext set up by TopLogicServlet.
DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

// Install subsession context and enable command phase.
SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
ReentrantLock requestLock = registry.getRequestLock();
requestLock.lock();
try {
    HandlerResult result;
    boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
    try {
        result = control.executeCommand(commandName, arguments);
    } finally {
        if (rootHandler != null) {
            rootHandler.enableUpdate(updateBefore);
        }
    }

    // Forward side effects: InfoService messages and legacy control repaints.
    forwardPendingUpdates(displayContext, rootHandler, queue, control);

    if (result.isSuccess()) {
        sendSuccess(response);
    } else {
        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Command failed.");
    }
} finally {
    requestLock.unlock();
}
```

- [ ] **Step 4: Update handleState() to use per-window queue**

Replace lines 303-304 in `handleState()`:
```java
SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
```

With:
```java
String windowName = request.getParameter("windowName");
SSEUpdateQueue queue = getWindowQueue(session, windowName);
if (queue == null) {
    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing windowName.");
    return;
}
```

- [ ] **Step 5: Update handleDataDownload() to use per-window queue**

Replace lines 117-118 in `handleDataDownload()`:
```java
SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
```

With:
```java
String windowName = request.getParameter("windowName");
SSEUpdateQueue queue = getWindowQueue(session, windowName);
if (queue == null) {
    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing windowName.");
    return;
}
```

- [ ] **Step 6: Update handleUpload() to use per-window queue and request lock**

Replace lines 325-326 in `handleUpload()`:
```java
SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
```

With:
```java
SSEUpdateQueue queue = getWindowQueue(session, windowName);
if (queue == null) {
    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing windowName.");
    return;
}
```

Wrap the command execution + forwardPendingUpdates in the request lock (same pattern as handleCommand):
```java
ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
ReentrantLock requestLock = registry.getRequestLock();
requestLock.lock();
try {
    // ... existing upload handling + forwardPendingUpdates ...
} finally {
    requestLock.unlock();
}
```

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java
git commit -m "Ticket #29108: Update ReactServlet to use per-window queue and session-wide request lock."
```

---

### Task 5: Update DisplayContextAdapter and ViewServlet

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

- [ ] **Step 1: Update DisplayContextAdapter constructor**

In `DisplayContextAdapter.java`, replace the `_sseQueue` initialization (line 37):
```java
_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession());
```

With:
```java
_sseQueue = ReactWindowRegistry.forSession(context.asRequest().getSession()).getOrCreateQueue(_windowName);
```

Remove the now-unused import of `SSEUpdateQueue` (line 9) if no other reference exists. Actually `SSEUpdateQueue` is still used as the field type, so keep it.

- [ ] **Step 2: Update ViewServlet to use registry for queue lookup**

In `ViewServlet.java`, replace lines 95-96:
```java
SSEUpdateQueue sseQueue = SSEUpdateQueue.forSession(session);
ReactWindowRegistry windowRegistry = ReactWindowRegistry.forSession(session);
```

With:
```java
ReactWindowRegistry windowRegistry = ReactWindowRegistry.forSession(session);
SSEUpdateQueue sseQueue = windowRegistry.getOrCreateQueue(windowName);
```

Remove the import of `SSEUpdateQueue` (line 34) since the variable type can use `var` or we keep the import since the type is still referenced.

Actually, `SSEUpdateQueue` is used as the variable type on the replacement line. Keep the import.

- [ ] **Step 3: Verify full compilation of both modules**

Run:
```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.react && mvn compile -DskipTests=true 2>&1 | tail -20
```

Then:
```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true 2>&1 | tail -20
```

Expected: Both compile successfully. All `SSEUpdateQueue.forSession()` references are now gone.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: Update DisplayContextAdapter and ViewServlet to use per-window queue from registry."
```

---

## Chunk 2: Client-Side Changes and Verification

### Task 6: Update client-side SSE URL to include windowName

**Files:**
- Modify: `com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts`

- [ ] **Step 1: Add windowName to SSE connect URL**

In `tl-react-bridge.ts`, replace lines 95 and 97:
```typescript
    connect(getApiBase() + 'react-api/events');
```

With (both occurrences):
```typescript
    connect(getApiBase() + 'react-api/events?windowName=' + encodeURIComponent(resolvedWindowName));
```

The `resolvedWindowName` variable is already defined on line 100 (`const resolvedWindowName = windowName ?? '';`). Move it before the `connect()` calls (before line 92) so it's available:

Move `const resolvedWindowName = windowName ?? '';` from line 100 to just before line 92 (before the `if (!_sseConnected)` block).

- [ ] **Step 2: Add windowName to useTLDataUrl**

In `tl-react-bridge.ts`, replace line 323:
```typescript
  return getApiBase() + 'react-api/data?controlId=' + encodeURIComponent(ctx.controlId);
```

With:
```typescript
  return getApiBase() + 'react-api/data?controlId=' + encodeURIComponent(ctx.controlId)
    + '&windowName=' + encodeURIComponent(ctx.windowName);
```

- [ ] **Step 3: Build the React module**

Run:
```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.react && mvn compile -DskipTests=true 2>&1 | tail -20
```

Expected: Build succeeds (frontend-maven-plugin runs Vite build).

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts
git commit -m "Ticket #29108: Add windowName parameter to SSE and data URLs in client bridge."
```

---

### Task 7: Remove redundant targetWindowId filtering from window-manager.ts

**Files:**
- Modify: `com.top_logic.layout.react/react-src/bridge/window-manager.ts`

With per-window queues, each window only receives events destined for it. The `targetWindowId` checks in `handleWindowOpen`, `handleWindowClose`, and `handleWindowFocus` are now dead code and must be removed.

- [ ] **Step 1: Remove targetWindowId guard from handleWindowOpen()**

In `window-manager.ts`, remove line 90:
```typescript
  if (event.targetWindowId !== getMyWindowId()) return;
```

- [ ] **Step 2: Remove targetWindowId guard from handleWindowClose()**

Remove line 107:
```typescript
  if (event.targetWindowId !== getMyWindowId()) return;
```

- [ ] **Step 3: Remove targetWindowId guard from handleWindowFocus()**

Remove line 117 (after previous removal shifts lines):
```typescript
  if (event.targetWindowId !== getMyWindowId()) return;
```

- [ ] **Step 4: Remove getMyWindowId() helper if no longer used**

Check if `getMyWindowId()` (lines 15-17) is still referenced elsewhere in the file. It is used in `notifyWindowClosed()` (line 53) and `notifyWindowBlocked()` (line 129) and `initSelfCloseNotification()` (line 151). So keep it — it's still needed for outgoing notifications.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/react-src/bridge/window-manager.ts
git commit -m "Ticket #29108: Remove redundant targetWindowId filtering from window-manager.ts."
```

---

### Task 8: Install react module WAR and verify with running app

**Files:** None (build + manual verification)

- [ ] **Step 1: Install the react module to update the WAR in Maven repo**

Run:
```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.react && mvn clean install -DskipTests=true 2>&1 | tail -20
```

- [ ] **Step 2: Install the view module**

Run:
```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn install -DskipTests=true 2>&1 | tail -20
```

- [ ] **Step 3: Start the demo app**

Use the `tl-app` skill to start the demo application.

- [ ] **Step 4: Verify basic functionality in the browser**

Open http://localhost:8080 and:
1. Log in with root / root1234
2. Verify the dashboard loads (SSE connection established for the main window)
3. Check browser console for `[TLReact]` messages — the SSE URL should now include `?windowName=...`
4. If popup windows are testable from the dashboard, open one and verify it gets its own SSE connection

- [ ] **Step 5: Commit any fixes if needed**

If issues are found during verification, fix and commit incrementally.
