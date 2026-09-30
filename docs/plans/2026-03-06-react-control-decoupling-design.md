# Design: Decouple ReactControl from AbstractControlBase

**Date:** 2026-03-06
**Ticket:** #29102
**Branch:** `investigate/react-displaycontext-npe`

## Problem

When a button in a ViewServlet-bootstrapped page is clicked, the command dispatch
chain crashes with a NullPointerException:

```
java.lang.NullPointerException: Cannot invoke "ControlScope.getFrameScope()"
  because the return value of "DisplayContext.getExecutionScope()" is null
    at DisplayContextAdapter.<init>(DisplayContextAdapter.java:28)
    at ViewDisplayContext.fromDisplayContext(ViewDisplayContext.java:53)
    at ButtonElement.lambda$1(ButtonElement.java:138)
    at ReactButtonControl$ClickCommand.execute(ReactButtonControl.java:89)
```

**Root cause:** ReactControl extends AbstractVisibleControl (old-world control
hierarchy). Its ControlCommand handlers receive an old-world DisplayContext. When
ButtonElement tries to convert this to a ViewDisplayContext via DisplayContextAdapter,
it calls `context.getExecutionScope().getFrameScope()`. In ViewServlet-bootstrapped
pages, `getExecutionScope()` returns null because `MainLayout.initScope()` was never
called.

**Deeper issue:** Two worlds are mixed in one class hierarchy. ReactControl inherits
ControlScope/FrameScope/attach/detach/revalidation machinery it never uses, and the
old-world DisplayContext leaks into new-world command dispatch.

## Solution

Decouple ReactControl from the old control hierarchy entirely. ReactControl implements
`HTMLFragment` (for legacy embedding) and `ViewControl` (for new-world rendering)
directly, with no inheritance from AbstractControlBase.

## Design

### 1. ReactControl Base

ReactControl no longer extends AbstractVisibleControl. It implements:

- `HTMLFragment` - for legacy embedding in old-world pages
- `ViewControl` - for new-world rendering via ViewServlet
- `ReactCommandTarget` - new lean interface for command dispatch

```java
public class ReactControl implements HTMLFragment, ViewControl, ReactCommandTarget {
    private String _id;
    private final Object _model;
    private final String _reactModule;
    private Map<String, Object> _reactState;
    private SSEUpdateQueue _sseQueue;
    private ViewDisplayContext _viewContext;
}
```

**Dropped from inheritance:**
- ControlScope integration (attach/detach/scope registration) - never used
- UpdateListener mechanism - ReactControl already no-ops this
- FrameScope-based ID allocation - replaced by SSEUpdateQueue counter
- ControlCommand map (commandsByName) - replaced by @ReactCommand annotations
- requestRepaint() / revalidation - uses SSE instead

**Reimplemented locally (trivial):**
- `getID()` - getter on `_id`
- `writeIdAttribute(TagWriter)` - `out.writeAttribute("id", _id)`
- `writeControlClasses(TagWriter)` - write CSS classes directly

**Legacy bridge:**
- `HTMLFragment.write(DisplayContext, TagWriter)` delegates to
  `write(ViewDisplayContext, TagWriter)` via DisplayContextAdapter. This path only runs
  inside old-world pages where `initScope()` was called, so `getExecutionScope()` is
  guaranteed non-null.

### 2. Annotation-Based Command Dispatch

#### @ReactCommand annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReactCommand {
    /** The command ID sent by the client (e.g., "click", "sort", "select"). */
    String value();
}
```

#### Method signatures

Methods annotated with `@ReactCommand` can declare any subset of these parameters
in any order. The framework inspects parameter types and injects matching values:

| Parameter type          | Injected value                         |
|-------------------------|----------------------------------------|
| `ViewDisplayContext`    | The control's stored `_viewContext`     |
| `Map<String, Object>`  | The raw arguments map from the client  |

No parameters is also valid (for simple commands like "click" or "dismiss").
Return type is always `HandlerResult`.

#### Example: before and after

Before (ReactButtonControl):
```java
static class ClickCommand extends ControlCommand {
    ClickCommand() { super("click"); }
    @Override public ResKey getI18NKey() { return I18NConstants.REACT_BUTTON_CLICK; }
    @Override
    protected HandlerResult execute(DisplayContext context, Control control,
            Map<String, Object> arguments) {
        return ((ReactButtonControl) control)._action.executeCommand(context);
    }
}
private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ClickCommand());
```

After:
```java
@ReactCommand("click")
HandlerResult handleClick(ViewDisplayContext context) {
    return _action.execute(context);
}
```

#### Resolution and caching

Resolution happens lazily per class on first instantiation via
`ConcurrentHashMap.computeIfAbsent(getClass(), ReactCommandMap::forClass)`.

```java
class ReactCommandMap {
    private final Map<String, ReactCommandInvoker> _invokers;

    static ReactCommandMap forClass(Class<?> controlClass) {
        // Walk class hierarchy, find all @ReactCommand methods
        // For each: build MethodHandle, determine parameter injection flags
        // Cache result per class
    }
}

class ReactCommandInvoker {
    private final MethodHandle _handle;
    private final boolean _needsArgs;
    private final boolean _needsContext;

    HandlerResult invoke(ReactControl control, ViewDisplayContext context,
            Map<String, Object> arguments) {
        // Build argument array based on flags, invoke handle
    }
}
```

After resolution, dispatch is `Map.get()` + `MethodHandle.invoke()` - no reflection on
the hot path.

### 3. ReactCommandTarget Interface

Replaces `CommandListener` in SSEUpdateQueue:

```java
public interface ReactCommandTarget {
    String getID();
    HandlerResult executeCommand(String commandName, Map<String, Object> arguments);
}
```

No DisplayContext anywhere in the contract.

### 4. SSEUpdateQueue Changes

```java
public class SSEUpdateQueue {
    private final Map<String, ReactCommandTarget> _controls = new ConcurrentHashMap<>();

    // Session-scoped ID counter (replaces request-scoped counter)
    private final AtomicInteger _nextId = new AtomicInteger(1);

    public String allocateId() {
        return "v" + _nextId.getAndIncrement();
    }

    public void registerControl(ReactCommandTarget control) { ... }
    public ReactCommandTarget getControl(String controlId) { ... }
}
```

ID allocation moves from the request-scoped DefaultViewDisplayContext to the
session-scoped SSEUpdateQueue, preventing ID collisions when controls are created
dynamically during command execution.

DefaultViewDisplayContext.allocateId() delegates to the SSE queue:

```java
public class DefaultViewDisplayContext implements ViewDisplayContext {
    @Override
    public String allocateId() {
        return _sseQueue.allocateId();
    }
}
```

### 5. ReactServlet Dispatch Changes

```java
// Before:
DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
CommandListener control = queue.getControl(controlId);
result = control.executeCommand(displayContext, commandName, arguments);

// After:
ReactCommandTarget control = queue.getControl(controlId);
result = control.executeCommand(commandName, arguments);
```

The DisplayContext from TopLogicServlet remains available for legacy side-effects
(InfoService messages, forwardLegacyControlUpdates) but is no longer passed into the
command dispatch chain.

### 6. ViewCommandAction Interface

Replaces the old-world `Command` interface for button actions:

```java
@FunctionalInterface
public interface ViewCommandAction {
    HandlerResult execute(ViewDisplayContext context);
}
```

ButtonElement:
```java
// Before:
ReactButtonControl control = new ReactButtonControl(label,
    displayContext -> model.executeCommand(
        ViewDisplayContext.fromDisplayContext(displayContext)));  // NPE

// After:
ReactButtonControl control = new ReactButtonControl(label,
    context -> model.executeCommand(context));  // ViewDisplayContext flows through
```

### 7. DisplayContextAdapter

Stays in the codebase but is no longer on the command dispatch path. Only used for
the `HTMLFragment.write(DisplayContext, TagWriter)` legacy rendering bridge, where
`getExecutionScope()` is guaranteed non-null.

## Migration Scope

| Category                  | Count | Change                                                    |
|---------------------------|-------|-----------------------------------------------------------|
| ReactControl base class   | 1     | Drop extends, implement interfaces, add dispatch          |
| ReactControl subclasses   | ~36   | Remove ControlCommand classes, add @ReactCommand methods  |
| SSEUpdateQueue            | 1     | CommandListener -> ReactCommandTarget, add allocateId()   |
| ReactServlet              | 1     | Stop passing DisplayContext to command dispatch            |
| DefaultViewDisplayContext  | 1     | Delegate allocateId() to SSEUpdateQueue                   |
| ButtonElement             | 1     | Use ViewCommandAction instead of Command                  |
| New files                 | ~5    | @ReactCommand, ReactCommandMap, ReactCommandInvoker,      |
|                           |       | ReactCommandTarget, ViewCommandAction                     |

### Per-subclass migration pattern

Each subclass follows the same mechanical transform:

1. Delete `static class XxxCommand extends ControlCommand { ... }`
2. Delete `private static final Map<String, ControlCommand> COMMANDS = createCommandMap(...)`
3. Remove commands from constructor: `super(model, module, COMMANDS)` -> `super(model, module)`
4. Add `@ReactCommand("xxx") HandlerResult handleXxx(...)` method with the body from
   the old `execute()` method
5. Remove DisplayContext parameter usage (replace with ViewDisplayContext if needed)

### What does NOT change

- ViewServlet - already in the new world
- ViewCommand / ViewCommandModel - already uses ViewDisplayContext
- Old-world controls (AbstractControlBase hierarchy) - untouched
- AbstractControlBase / AbstractVisibleControl - untouched
