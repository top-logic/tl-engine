# Rendering Split: Old World / New World Design

## Problem

The view system (`tl-layout-view`) is forced to implement heavyweight old-world interfaces
to satisfy the `AbstractControlBase.write()` lifecycle. The result is adapter classes full of
dead methods:

- **ViewFrameScope** implements `FrameScope` (17 methods) -- only 6 are real (35%).
  The rest are stubs, no-ops, or throw `UnsupportedOperationException`.
- **ViewLayoutContext** implements `LayoutContext` (10 methods) -- only 1 is real (10%):
  `getWindowId()`. The other 9 come from `ValidationQueue`, `ActionQueue`, `MainLayout`,
  `RequestLock`.
- **ViewServlet** hacks into `TLContext.initLayoutContext()` and `DisplayContext.initScope()`
  to wire up a `LocalScope` just so `AbstractControlBase.write()` can call `attach()`.

The fundamental mismatch: the old world assumes a synchronous request/response cycle with
`LayoutComponent` trees, IFRAME scopes, `ClientAction` AJAX responses, and phase-based
command/rendering separation. The new world needs a one-shot HTML render, SSE for updates,
and a flat set of React controls registered on an `SSEUpdateQueue`.

## Solution

Introduce two lean interfaces in `tl-layout-react` that define the new-world rendering
contract. The old-world interfaces stay untouched.

### ViewDisplayContext

Replaces the combination of `DisplayContext` + `ControlScope` + `FrameScope` + `LayoutContext`
for view-system rendering:

```java
package com.top_logic.layout.react;

public interface ViewDisplayContext {
    /** Allocate a unique ID for a control's DOM element. */
    String allocateId();

    /** The window name sent to the client for command routing. */
    String getWindowName();

    /** The webapp context path for constructing URLs. */
    String getContextPath();

    /** The SSE queue for pushing state updates and registering controls. */
    SSEUpdateQueue getSSEQueue();
}
```

Four methods. No `ControlScope`, no `FrameScope`, no `LayoutContext`, no `ValidationQueue`,
no `ActionQueue`, no `MainLayout`, no `RequestLock`, no `ContentHandler`, no `WindowScope`.

A `DefaultViewDisplayContext` POJO implements this interface with an `AtomicInteger` for ID
allocation.

A static factory method `ViewDisplayContext.fromDisplayContext(DisplayContext)` creates an
adapter that extracts values from the richer old-world `DisplayContext` (context path from
request, window name from `LayoutContext`, ID allocation from `FrameScope`, SSE queue from
session). This bridges old-world callers into the new-world rendering path.

### ViewControl

Replaces `Control` as the return type for view-system element factories:

```java
package com.top_logic.layout.react;

public interface ViewControl {
    /** The control's unique ID (assigned during write). */
    String getID();

    /** Render this control into the given writer. */
    void write(ViewDisplayContext context, TagWriter out) throws IOException;
}
```

### Note on `getWindowName()`

The window name currently identifies the subsession for command routing. Its exact semantics
in the view system need further design work as the command dispatch and subsession model
evolves. For the PoC, a fixed value (`"view"`) is sufficient.

## ReactControl: Dual Interface

`ReactControl` keeps its old-world base class (`AbstractVisibleControl`) for backward
compatibility and additionally implements `ViewControl`:

```java
public class ReactControl extends AbstractVisibleControl implements ViewControl {

    @Override // ViewControl -- canonical rendering
    public void write(ViewDisplayContext context, TagWriter out) throws IOException {
        String id = context.allocateId();
        setID(id);

        SSEUpdateQueue queue = context.getSSEQueue();
        queue.registerControl(this);

        // Write mount-point div and TLReact.mount() bootstrap script
        // using context.getWindowName() and context.getContextPath()
        ...
    }

    @Override // AbstractControlBase -- old-world adapter
    protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
        write(ViewDisplayContext.fromDisplayContext(context), out);
    }
}
```

The canonical rendering logic lives in `write(ViewDisplayContext, ...)`. The old-world
`internalWrite(DisplayContext, ...)` is a thin adapter.

## ViewServlet Simplification

Before (current):
```java
DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
TLContext subSession = (TLContext) displayContext.getSubSessionContext();
subSession.initLayoutContext(new ViewLayoutContext(new WindowId("view")));
ControlScope rootScope = new LocalScope(frameScope, false);
displayContext.initScope(rootScope);
rootControl.write(displayContext, out);
```

After:
```java
ViewDisplayContext viewContext = new DefaultViewDisplayContext(
    request.getContextPath(),
    "view",
    SSEUpdateQueue.forSession(request.getSession())
);
rootControl.write(viewContext, out);
```

No `DisplayContext`, no `TLContext` cast, no `LocalScope`, no `ViewLayoutContext`, no
`ViewFrameScope`.

## Changes Summary

### Deleted from `tl-layout-view`

| File | Reason |
|---|---|
| `ViewFrameScope.java` | ID allocation moved to `ViewDisplayContext.allocateId()`. Command listener registry already on `SSEUpdateQueue`. |
| `ViewLayoutContext.java` | Window name moved to `ViewDisplayContext.getWindowName()`. |

### Added to `tl-layout-react`

| File | Purpose |
|---|---|
| `ViewDisplayContext.java` | Lean 4-method rendering context interface. |
| `ViewControl.java` | Lean 2-method control interface. |
| `DefaultViewDisplayContext.java` | Simple POJO implementation with `AtomicInteger` for IDs. |

### Changed in `tl-layout-react`

| File | Change |
|---|---|
| `ReactControl.java` | Implements `ViewControl`. Canonical rendering in `write(ViewDisplayContext, TagWriter)`. Old `internalWrite` becomes adapter via `ViewDisplayContext.fromDisplayContext()`. |

### Changed in `tl-layout-view`

| File | Change |
|---|---|
| `UIElement.java` | `createControl()` returns `ViewControl` instead of `Control`. |
| `ViewServlet.java` | `renderPage()` creates `DefaultViewDisplayContext`, calls `rootControl.write(viewCtx, out)`. No old-world scope setup. |
| `ViewContext.java` | Simplified -- no longer holds `FrameScope`. |
| `ContainerElement.java` | `createChildControls()` returns `List<ViewControl>`. |
| `ViewElement.java` | Returns `ViewControl` from `createControl()`. |
| Element classes (`AppShellElement`, `PanelElement`, `StackElement`) | Return `ViewControl` from `createControl()`. |

### Untouched

- All old-world interfaces (`FrameScope`, `ControlScope`, `LayoutContext`, `DisplayContext`, `Control`)
- `AbstractControlBase` lifecycle
- `TopLogicServlet` (ViewServlet still extends it for session management)
