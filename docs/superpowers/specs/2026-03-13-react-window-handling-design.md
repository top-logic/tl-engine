# React Multi-Window Handling Design

## Context

TopLogic's legacy system supports multiple browser windows within a single sub-session via `WindowManager`, `WindowComponent`, and `window.js`. The React layer (served by `ViewServlet`) currently has no equivalent. Each browser tab independently generates a window name, loads a view, and establishes its own SSE connection, but there is no mechanism for one window to programmatically open another or for windows to communicate.

This design adds multi-window support to the React layer, coexisting with the legacy window system.

## Requirements

- **Coexist with legacy**: Legacy `WindowManager`/`window.js` continues to work. React windows use a parallel mechanism.
- **Server-driven**: Server defines what renders in each window (from `.view.xml` or programmatic `ReactControl` trees).
- **Server-only communication**: All cross-window communication routes through the server via commands and SSE. No client-side messaging (BroadcastChannel/postMessage).
- **Independent SSE per window**: Each browser window establishes its own SSE connection.
- **Child windows survive opener closing**: Child windows are fully independent once opened.
- **Shared request lock**: Single request lock per sub-session, same as legacy.
- **CSP-compliant**: No dynamic JavaScript execution. All client-side behavior comes from pre-loaded static code reacting to structured SSE events.

## Architecture

### New Components

**Server-side:**

1. **`ReactWindowRegistry`** — Per-session (stored as HTTP session attribute alongside `SSEUpdateQueue`). Manages open windows, pre-creates control trees, enqueues lifecycle SSE events.

2. **Three new SSE event types** — `WindowOpenEvent`, `WindowCloseEvent`, `WindowFocusEvent`. Structured msgbuf messages, not JavaScript.

**Client-side:**

3. **`window-manager.ts`** — New module in `tl-react-bridge`. Listens for window lifecycle SSE events, calls `window.open()`/`.close()`/`.focus()`, detects user-initiated closes.

**Modified:**

4. **`ViewServlet`** — One added check: before loading a `.view.xml` by path, check `ReactWindowRegistry` for a pre-created `WindowEntry`. If found, use the pre-built control tree. Also: `extractWindowName()` updated to accept registry-generated window IDs.

5. **`sse-client.ts`** — Extended with three new cases in the event dispatch switch to handle `WindowOpenEvent`, `WindowCloseEvent`, `WindowFocusEvent` by delegating to `window-manager.ts`.

### What We Are NOT Building

- No client-side cross-window messaging (BroadcastChannel/postMessage)
- No replacement of the legacy WindowManager
- No per-window request locks
- No inline/dynamic JavaScript

## Server-Side Window Creation

When server-side code decides to open a window, it does not send the view path to the client. Instead:

1. Command handler calls `windowRegistry.openWindow(...)`.
2. Registry generates a server-side window ID (using the `'v'` prefix to match ViewServlet's `extractWindowName()` expectations), creates the sub-session, and builds the control tree (from `.view.xml` or programmatically).
3. Registry enqueues a `WindowOpenEvent` to the opener's SSE connection.
4. Client opens the browser window at `/view/<windowId>`.
5. ViewServlet finds the pre-created `WindowEntry` and renders it.

This keeps view resolution server-side and allows both declarative (`.view.xml`) and programmatic window content.

### ReactWindowRegistry API

```java
public class ReactWindowRegistry implements HttpSessionBindingListener {
    // Window ID -> WindowEntry
    Map<String, WindowEntry> _windows;

    static ReactWindowRegistry forSession(HttpSession session);

    // Open from view.xml
    String openWindow(ViewContext openerContext, String viewPath, WindowOptions options);

    // Open with programmatic control tree
    String openWindow(ViewContext openerContext, IReactControl rootControl, WindowOptions options);

    // Called when child window connects via ViewServlet
    WindowEntry getWindow(String windowId);

    // Called when window close notification arrives from client
    void windowClosed(String windowId);

    // HttpSessionBindingListener: clean up all windows on session invalidation
    void valueUnbound(HttpSessionBindingEvent event);
}
```

### WindowEntry

```java
class WindowEntry {
    String _windowId;
    String _openerWindowId;     // which window opened this one
    IReactControl _rootControl; // pre-built control tree
    WindowOptions _options;     // dimensions, title, features
    boolean _connected;         // has the browser window connected yet?
}
```

### WindowOptions

```java
class WindowOptions {
    int _width;                 // window width in pixels
    int _height;                // window height in pixels
    String _title;              // window title
    boolean _resizable;         // whether user can resize
    boolean _menubar;           // whether to show browser menubar
}
```

## SSE Event Types

Three new msgbuf event types added to the existing SSE protocol.

**Important**: `SSEUpdateQueue` is per-session and broadcasts all events to all connected SSE clients (all windows). Therefore, each window lifecycle event includes a `targetWindowId` field. The client-side `window-manager.ts` compares this against the current page's window name and ignores events not targeted at it.

### WindowOpenEvent

```
{
  "targetWindowId": "v1a2b3c4d5e6f7a8",
  "windowId": "v9f8e7d6c5b4a3a2",
  "width": 800,
  "height": 600,
  "title": "Help",
  "resizable": true
}
```

`targetWindowId` is the opener's window ID (so only the opener acts on it). `windowId` is the new window's ID.

### WindowCloseEvent

```
{
  "targetWindowId": "v1a2b3c4d5e6f7a8",
  "windowId": "v9f8e7d6c5b4a3a2"
}
```

### WindowFocusEvent

```
{
  "targetWindowId": "v1a2b3c4d5e6f7a8",
  "windowId": "v9f8e7d6c5b4a3a2"
}
```

## Client-Side Window Manager

`window-manager.ts` in `tl-react-bridge`:

- **Filters by `targetWindowId`**: Compares against the current page's window name. Ignores events meant for other windows.
- **Opens windows**: Calls `window.open(contextPath + "/view/" + windowId, windowId, features)`. Stores the returned `Window` reference in a `Map<string, Window>`.
- **Closes/focuses windows**: Looks up the stored reference, calls `.close()` or `.focus()`.
- **Detects user-initiated closes**: Polls `openWindows` every 2 seconds, checks `.closed` property. Sends a close notification command to the server for cleanup.
- **Self-close notification**: On `beforeunload`, sends a close notification for the current window to the server. This handles the case where the opener has already closed and no external polling is detecting this window's closure.
- **Popup blocker handling**: If `window.open()` returns `null`, notifies the server which can display a snackbar suggesting the user allow popups.

```typescript
const openWindows: Map<string, Window> = new Map();
const myWindowId: string = /* read from document.body dataset or URL */;

// SSE event handlers (new cases in sse-client.ts dispatch switch)
case 'WindowOpenEvent':
    if (event.targetWindowId !== myWindowId) break;
    const ref = window.open(
        `${contextPath}/view/${event.windowId}`,
        event.windowId,
        buildFeatureString(event)
    );
    if (ref) {
        openWindows.set(event.windowId, ref);
    } else {
        sendCommand('windowBlocked', { windowId: event.windowId });
    }
    break;

case 'WindowCloseEvent':
    if (event.targetWindowId !== myWindowId) break;
    openWindows.get(event.windowId)?.close();
    openWindows.delete(event.windowId);
    break;

case 'WindowFocusEvent':
    if (event.targetWindowId !== myWindowId) break;
    openWindows.get(event.windowId)?.focus();
    break;
```

## ViewServlet Changes

One added check before view resolution:

```
/view/<windowId>/  -> check ReactWindowRegistry first
  -> if WindowEntry exists: use pre-built control tree, skip ViewLoader
  -> if not: fall through to existing behavior (load .view.xml)
```

The rendering pipeline (`renderPage()`) is unchanged. It receives an `IReactControl` root regardless of origin.

Sub-session handling: `ensureSubSession()` finds the sub-session already created by `ReactWindowRegistry.openWindow()`. No change needed.

**Stale window IDs**: If a user bookmarks or reloads a URL with a window ID that no longer exists in the registry (e.g., after session expiry or server restart), the registry lookup returns `null` and ViewServlet falls through to `.view.xml` resolution. Since the window ID is not a valid view path, this results in a 404. This is acceptable — programmatically opened windows are transient and not bookmarkable.

## Cross-Window Communication

No new protocol needed. Cross-window communication works through the existing SSE/command infrastructure.

**Important**: `SSEUpdateQueue` broadcasts all events to all SSE connections. Events are tagged with `controlId`. Each window's `sse-client.ts` only has listeners for its own controls, so events for other windows' controls are silently ignored. This means cross-window state updates work naturally: updating a control's state enqueues an event that reaches all windows, but only the window owning that control acts on it.

### Via Direct Control Access

```java
// In a command handler executing in Window A's context:
ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
WindowEntry helpWindow = registry.getWindow(helpWindowId);
IReactControl helpRoot = helpWindow.getRootControl();

// Update state on a control in the other window.
// SSEUpdateQueue broadcasts to all connections; Window B's client
// has the listener for this controlId and applies the update.
helpRoot.patchReactState(Map.of("topic", "newTopic"));
```

### Via Shared Channels

Both windows can share channel references wired at window-open time:

```java
// When opening the child window, pass a channel from the opener
ViewChannel selectedItem = openerViewContext.resolveChannel(channelRef);
childViewContext.registerChannel("parentSelection", selectedItem);
```

Channel listeners trigger `patchReactState()` which enqueues SSE events. Since events are tagged with `controlId` (not `windowId`), only the window owning the affected control applies the update.

## Cleanup and Edge Cases

### Window Close (Normal)

1. User closes child window -> browser window becomes `.closed`.
2. Opener's `window-manager.ts` detects closure via polling, sends close notification command.
3. Server's `ReactWindowRegistry.windowClosed()`:
   - Calls `cleanupTree()` on root control (unregisters from `SSEUpdateQueue`).
   - Removes the `WindowEntry`.
   - Destroys the sub-session.

### Opener Closes, Child Survives

- Child has its own SSE connection and sub-session. Continues working independently.
- Opener's `window-manager.ts` dies with the opener page, so no more polling of the child's `.closed` state.
- Server detects opener's SSE disconnect (heartbeat timeout) and cleans up opener's controls.
- Child's `WindowEntry` remains with `_openerWindowId` pointing to a dead window. Harmless.
- When the child eventually closes, its own `beforeunload` handler sends a self-close notification to the server, triggering cleanup. If `beforeunload` fails to fire (browser crash), the server detects the SSE heartbeat timeout for the child's connection and cleans up the `WindowEntry` and control tree.

### SSE Disconnect Cleanup

When all SSE connections for a given window ID are lost (detected via heartbeat timeout), the server should clean up the corresponding `WindowEntry` if one exists. This ensures orphaned windows are eventually cleaned up even if no close notification arrives.

### Session Expiry

`ReactWindowRegistry` implements `HttpSessionBindingListener`. On session invalidation: clean up all control trees, remove all entries.

### Race Condition Prevention

The primary race (client connecting before control tree exists) cannot occur. `openWindow()` creates the control tree *before* enqueuing the `WindowOpenEvent`. The child window can only be opened after the event arrives, so the control tree is always ready.

### Duplicate Window Prevention

`window.open()` uses `windowId` as the window name (second argument). If a window with that name already exists, the browser reuses it.

### Popup Blockers

`window.open()` triggered asynchronously (from SSE event) may be blocked. `window-manager.ts` detects `null` return and notifies the server, which can show a snackbar.

### Stale Window IDs

Bookmarked or manually entered URLs with expired window IDs result in a 404. Programmatically opened windows are transient and not bookmarkable.

## Data Flow Diagram

```
User clicks button in Window A
  -> command POST to /react-api/command
  -> server command handler calls windowRegistry.openWindow(viewPath, options)
     -> generates windowId (v-prefixed)
     -> creates sub-session for windowId
     -> creates control tree (from .view.xml or programmatically)
     -> stores WindowEntry
     -> enqueues WindowOpenEvent (targetWindowId=opener) to SSE queue
  -> SSE broadcasts to all connections
  -> Window A's window-manager.ts matches targetWindowId, receives event
  -> Window B (if open) ignores event (targetWindowId mismatch)
  -> Window A calls window.open("/view/" + windowId, windowId, features)
  -> child browser window loads /view/<windowId>
  -> ViewServlet finds WindowEntry, renders pre-built control tree
  -> child window establishes own SSE connection
  -> server pushes StateEvent with initial control state
  -> child React app renders

Cross-window update:
  Window B command -> server updates Window A's controls
    -> SSE broadcast to all connections
    -> Window A's sse-client has listener for that controlId, applies update
    -> Window B's sse-client has no listener, ignores
```
