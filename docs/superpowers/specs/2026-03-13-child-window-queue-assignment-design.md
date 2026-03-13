# Design: Lazy Control Tree Construction for Child Windows

## Problem

When a child window is opened via `ReactWindowRegistry.openWindow()`, the control tree is pre-built using the opener's `ReactContext`. This causes three mismatches when the child window's `ViewServlet` renders the page:

1. **Control registration**: Controls register on the opener's `SSEUpdateQueue`. Commands from the child window fail with 404.
2. **Window name on HTML elements**: `data-window-name` attributes carry the opener's window name.
3. **Command routing**: Events enqueued by controls go to the opener's queue, not the child's.

Root cause: the control tree is built at `openWindow()` time with the opener's context, but rendered at `ViewServlet.doGet()` time with the child's context.

## Solution: Factory Pattern

Don't build the control tree too early. Pass a factory (`ReactControlProvider`) instead of a pre-built `IReactControl`. The tree is constructed in `ViewServlet.doGet()` when the child window's `ReactContext` exists, so every control is born with the correct queue.

The existing `ReactControlProvider` interface already has the right signature:

```java
@FunctionalInterface
public interface ReactControlProvider {
    ReactControl createControl(ReactContext context, Object model);
}
```

## Changes

### 1. `ReactWindowRegistry`

Remove all pre-built control overloads:

```java
// REMOVED (both):
openWindow(ReactContext, IReactControl, WindowOptions)
openWindow(ReactContext, IReactControl, WindowOptions, Runnable)

// NEW:
openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
           Object model, WindowOptions options, Runnable closeCallback)

// Convenience (no callback):
openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
           Object model, WindowOptions options)
```

The no-control convenience overload stays:

```java
openWindow(ReactContext openerContext, WindowOptions options)
```

### 2. `WindowEntry`

Replace `IReactControl _rootControl` (final) with:

- `ReactControlProvider _controlProvider` (final) — the factory
- `Object _model` (final) — the model passed to the factory
- `volatile ReactControl _rootControl` (mutable) — set once when the tree is built in `ViewServlet`

Constructor changes:

```java
// For provider-based windows:
WindowEntry(String windowId, String openerWindowId,
            ReactControlProvider controlProvider, Object model,
            WindowOptions options, Runnable closeCallback)

// For no-control windows (provider and model are null):
WindowEntry(String windowId, String openerWindowId,
            WindowOptions options, Runnable closeCallback)
```

Add `setRootControl(ReactControl)` and `getControlProvider()` / `getModel()` accessors.

Note: `_rootControl` is `volatile` because it is written in `ViewServlet.doGet()` and read in `windowClosed()` on potentially different threads.

### 3. `ViewServlet.doGet()`

When rendering a child window, build the control tree from the provider:

```java
ReactControlProvider provider = windowEntry.getControlProvider();
if (provider != null) {
    ReactContext childContext = new DefaultReactContext(
        request.getContextPath(), windowName, sseQueue, windowRegistry);
    ReactControl rootControl = provider.createControl(childContext, windowEntry.getModel());
    windowEntry.setRootControl(rootControl);
    renderPage(request, response, rootControl, childContext);
    return;
}
// Falls through to existing view-based loading logic if no provider.
```

If the browser sends a duplicate GET (e.g. network retry), the second call would build a new tree, overwriting the first. The first tree's controls become unreachable and are GC'd. The SSE queue only has the second tree's controls registered (first tree's controls were overwritten in `registerControl()` by ID). This is acceptable — duplicate page loads are inherently last-writer-wins.

### 4. `OpenWindowDemoCommand`

From:
```java
DemoCounterControl counter = new DemoCounterControl(context, windowLabel);
registry.openWindow(context, counter, options, closeCallback);
```

To:
```java
registry.openWindow(context,
    (ctx, model) -> new DemoCounterControl(ctx, (String) model),
    windowLabel, options, closeCallback);
```

### 5. `windowClosed()` / `valueUnbound()`

No change needed. These already null-check the root control via `instanceof ReactControl`. If the browser never connected (tree never built), `_rootControl` is null and cleanup is skipped.

### 6. Tests (`TestReactWindowRegistry`)

Unaffected. All tests use the no-control overload `openWindow(ctx, options)`.

## What This Eliminates

- No queue reassignment or tree walking
- No new virtual methods on 30+ composite control subclasses
- No mutable `ReactContext`
- All three problems vanish because controls are built with the correct context from the start
