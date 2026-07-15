# Error Reporting from React Commands via ErrorSink

## Problem

`ReactServlet` uses `InfoService` + `JSSnipplet` (`showInfoArea()`) to forward server-side errors to the client. This works in the legacy case (React controls embedded in traditional UI) but fails in the native React UI (`ViewServlet`) where no legacy info area exists -- only a `TLSnackbar` in the `ReactAppShellControl`.

## Solution

Introduce an `ErrorSink` interface that abstracts error reporting. The app shell creates an `ErrorSink` implementation backed by its `ReactSnackbarControl` and propagates it through `ViewContext` during rendering. Each `ReactControl` stores its sink reference. At command time, `ReactServlet` asks the target control for its sink and forwards `InfoService` entries there instead of generating `showInfoArea()` JS.

## Components

### 1. ErrorSink interface

New interface (in `com.top_logic.layout.react` or `com.top_logic.layout.view`):

```java
public interface ErrorSink {
    void showError(String message);
    void showWarning(String message);
    void showInfo(String message);
}
```

### 2. ViewContext changes

`ViewContext` gets an `ErrorSink` property. Set by the app shell during rendering, inherited by all child controls.

### 3. ReactControl changes

Each `ReactControl` stores the `ErrorSink` from its `ViewContext` during `write()`. Exposes it via `getErrorSink()`.

### 4. ReactAppShellControl changes

Creates an `ErrorSink` implementation that delegates to its `ReactSnackbarControl`, mapping severity levels to snackbar variants (`"error"`, `"warning"`, `"info"`). Sets this sink on the `ViewContext` before rendering children.

### 5. ReactServlet changes

In `forwardPendingUpdates()`: if the target control has an `ErrorSink`, iterate the `InfoService` entries, `instanceof`-check for `DefaultInfoServiceItem`, extract the header text (`_headerText`) and map `_kindOfClass` to the appropriate `showError`/`showWarning`/`showInfo` call. Fall back to the legacy `showInfoArea()` JS path if no sink is available.

## Data Flow (new path)

```
Command handler calls InfoService.showError(resKey)
  -> DisplayContext.INFO_SERVICE_ENTRIES populated
  -> ReactServlet.forwardPendingUpdates()
  -> Target control has ErrorSink? Yes ->
    -> Cast entries to DefaultInfoServiceItem
    -> Extract headerText, resolve to String
    -> Map _kindOfClass -> showError/showWarning/showInfo
    -> ErrorSink implementation patches ReactSnackbarControl state
    -> PatchEvent sent via SSE
    -> TLSnackbar renders the notification
```

## Backward Compatibility

- If a control has no `ErrorSink` (legacy embedded case), the existing `showInfoArea()` JS path is used unchanged.
- `InfoService` API is untouched -- existing command handlers need zero changes.
- The `ErrorSink` is only populated when rendering through `ViewServlet` with an app shell.

## Multi-window

Each browser window gets its own view tree with its own `ReactAppShellControl`, which creates its own `ErrorSink`. Commands in window A report to window A's snackbar. No singleton problem.

## Key Files

| File | Role |
|------|------|
| `ReactServlet.java` | Captures InfoService entries, routes to ErrorSink or legacy path |
| `InfoService.java` | Unchanged -- existing API for queuing errors |
| `DefaultInfoServiceItem.java` | Carries `_kindOfClass` (severity) and `_headerText` (message) |
| `ReactControl.java` | Stores ErrorSink reference from ViewContext during rendering |
| `ViewContext.java` | Propagates ErrorSink through the view tree |
| `ReactAppShellControl.java` | Creates ErrorSink backed by ReactSnackbarControl |
| `ReactSnackbarControl.java` | Receives show calls, patches React state via SSE |
| `TLSnackbar.tsx` | Client-side React component that renders notifications |
