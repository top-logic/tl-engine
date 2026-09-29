# ReactContext Constructor Injection Design

## Problem

ReactControls live in a "half-initialized" state between construction and `write()`. Fields
`_reactContext`, `_id`, `_sseQueue` are null until rendering. Scoped resources (ErrorSink) require
mutable context swapping during render traversal via `onBeforeWrite`/`onAfterWrite`.

## Solution

Pass `ReactContext` to the `ReactControl` constructor. Each control is fully initialized at
construction time. `write()` becomes pure rendering with no initialization logic.

## ReactControl Constructor

```java
// Before:
public ReactControl(Object model, String reactModule)

// After:
public ReactControl(ReactContext context, Object model, String reactModule)
```

At construction: `_reactContext = context`, `_id = context.allocateId()`,
`_sseQueue = context.getSSEQueue()`, `_sseQueue.registerControl(this)`.

## IReactControl.write()

```java
// Before:
void write(ReactContext context, TagWriter out) throws IOException;

// After:
void write(TagWriter out) throws IOException;
```

Controls use their stored `_reactContext`. Pure rendering.

## HTMLFragment Bridge

`ReactControl.write(DisplayContext, TagWriter)` delegates to `write(out)`, ignoring the
DisplayContext. The control already has its context from construction.

## writeAsChild()

```java
// Before:
protected void writeAsChild(ReactContext context, JsonWriter writer)

// After:
protected void writeAsChild(JsonWriter writer)
```

No lazy init needed. Controls are fully initialized at construction.

## onBeforeWrite / onAfterWrite

```java
// Before:
protected void onBeforeWrite(ReactContext context)
protected void onAfterWrite(ReactContext context)

// After:
protected void onBeforeWrite()
protected void onAfterWrite()
```

Subclasses use `getReactContext()` if they need the context.

## ErrorSink Scoping

Move from render-time mutation to construction-time scoping:

- Add `withErrorSink(ErrorSink)` to `ViewContext` (like `withCommandScope`)
- `DefaultViewContext` stores ErrorSink, delegates `getErrorSink()` from ReactContext
- `AppShellElement.createControl()` creates a derived ViewContext with ErrorSink before creating
  children
- Remove `setErrorSink()` from `DefaultReactContext`
- Remove `onBeforeWrite`/`onAfterWrite` ErrorSink swapping from `ReactAppShellControl`

## Scope

| Category | Count | Change |
|---|---|---|
| ReactControl base class | 1 | Constructor, write(), writeAsChild(), hooks |
| IReactControl interface | 1 | write() signature |
| ReactControl subclasses | 42 | Add ReactContext as first constructor param |
| UIElement implementations | ~20 | Pass ViewContext when constructing controls |
| ViewServlet | 1 | `rootControl.write(out)` |
| ViewContext / DefaultViewContext | 2 | Add ErrorSink scoping |
| DefaultReactContext | 1 | Remove setErrorSink() |
| ReactAppShellControl | 1 | Remove ErrorSink swapping from hooks |

## What Does NOT Change

- `ReactContext` interface (unchanged)
- `HTMLFragment` interface (in tl-core, untouched)
- `DefaultReactContext` (loses `setErrorSink()`, otherwise unchanged)
