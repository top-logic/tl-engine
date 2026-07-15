# Child Window Queue Assignment Problem

## Problem Statement

When a child window is opened via `ReactWindowRegistry.openWindow()`, the pre-built control tree is constructed using the **opener's** `ReactContext`. This causes three mismatches when the child window's `ViewServlet` renders the page:

### 1. Control Registration (Server)

`ReactControl` constructor (line 94-95) does:
```java
_sseQueue = context.getSSEQueue();  // opener's queue
_sseQueue.registerControl(this);     // registered on opener's queue
```

Result: Controls are registered on the opener's `SSEUpdateQueue`. The child window's queue has zero controls. Commands from the child window fail with 404 because `getControl(id)` returns null.

### 2. Window Name on HTML Elements (Client)

`ReactControl.write()` (line 178) outputs:
```java
out.writeAttribute("data-window-name", _reactContext.getWindowName());  // opener's name
```

Result: React mount elements carry the opener's window name. The `tl-react-bridge.ts` `discoverAndMount()` reads this and connects SSE with the wrong window name. **Partially fixed** by preferring `document.body.dataset.windowName` in `discoverAndMount()`.

### 3. Command Routing (Server)

`ReactControl._sseQueue` points to the opener's queue. When the control enqueues events (e.g., state updates via `enqueue()`), they go to the opener's queue and are delivered to the opener's browser window, not the child's.

## Root Cause

The control tree is built at `openWindow()` time using the opener's context. But it is **rendered** at `ViewServlet.doGet()` time using the child window's context. The control tree was designed to be built and rendered in the same context (the same window), but `openWindow()` breaks this assumption.

## Observed Behavior

1. Main window opens child window - WindowOpenEvent delivered correctly (to opener's queue)
2. Child window loads, body gets correct `data-window-name`
3. Child window connects SSE with wrong windowName (opener's) - causes reconnect loop fighting with opener's connection
4. After client-side fix (prefer body windowName): SSE connects correctly, but commands fail with 404 because controls are on opener's queue

## Constraints

- `ReactControl._reactContext` is `private final` - can't be reassigned
- `ReactControl._sseQueue` is mutable (set to null in `cleanupTree()`) - can be reassigned
- Tree traversal uses virtual method pattern: `cleanupChildren()` is overridden by composite controls
- Many composite control subclasses exist (30+) - adding a new override requirement to all of them is expensive
- `SSEUpdateQueue._controls` is a `ConcurrentHashMap<String, ReactCommandTarget>` - controls can be moved between maps

## What Needs to Happen

When the child window's `ViewServlet.doGet()` renders a pre-built control tree:
1. All controls in the tree must be unregistered from the opener's queue
2. All controls in the tree must be registered on the child window's queue
3. Each control's `_sseQueue` field must point to the child window's queue
4. The `data-window-name` on rendered HTML elements must be the child window's name (not the opener's)
