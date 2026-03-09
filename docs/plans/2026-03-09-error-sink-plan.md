# ErrorSink Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Route InfoService error/warning/info messages to the React snackbar when running in the native ViewServlet context, transparently and without changing command handler code.

**Architecture:** An `ErrorSink` interface abstracts error reporting. The app shell creates an implementation backed by its `ReactSnackbarControl` and propagates it through `ViewContext`. Each `ReactControl` stores its sink. At command time, `ReactServlet` checks the target control for a sink and routes InfoService entries there instead of generating legacy `showInfoArea()` JavaScript.

**Tech Stack:** Java 17, TopLogic view system, React/SSE infrastructure

---

### Task 1: Add accessors to DefaultInfoServiceItem

Expose the `_kindOfClass` and `_headerText` fields so that `ReactServlet` can extract severity and message text without rendering HTML.

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/event/infoservice/DefaultInfoServiceItem.java`

**Step 1: Add getter methods**

Add after line 62 (end of constructor):

```java
/**
 * The CSS class identifying the message kind ({@link InfoService#ERROR_CSS},
 * {@link InfoService#WARNING_CSS}, or {@link InfoService#INFO_CSS}).
 */
public String getKindOfClass() {
    return _kindOfClass;
}

/**
 * The header text key for this info service item.
 */
public ResKey getHeaderText() {
    return _headerText;
}
```

**Step 2: Commit**

```
Ticket #29108: Add getters for kind and header text on DefaultInfoServiceItem.
```

---

### Task 2: Create the ErrorSink interface

Define a simple interface for reporting errors at three severity levels.

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ErrorSink.java`

**Step 1: Write the interface**

```java
package com.top_logic.layout.react.control;

/**
 * Sink for user-visible error, warning, and info messages.
 *
 * <p>
 * Implementations typically forward messages to a UI notification component such as a snackbar. The
 * sink is propagated through the view context so that controls can report errors to whatever
 * notification component is in scope.
 * </p>
 */
public interface ErrorSink {

    /**
     * Shows an error message to the user.
     *
     * @param message
     *        The error message text.
     */
    void showError(String message);

    /**
     * Shows a warning message to the user.
     *
     * @param message
     *        The warning message text.
     */
    void showWarning(String message);

    /**
     * Shows an informational message to the user.
     *
     * @param message
     *        The info message text.
     */
    void showInfo(String message);
}
```

**Step 2: Commit**

```
Ticket #29108: Add ErrorSink interface for forwarding server errors to UI.
```

---

### Task 3: Store ErrorSink on ReactControl

Each `ReactControl` stores an `ErrorSink` reference, set during rendering from the `ReactDisplayContext`.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactDisplayContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactDisplayContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java`

**Step 1: Add ErrorSink to ReactDisplayContext interface**

In `ReactDisplayContext.java`, add:

```java
/**
 * The {@link ErrorSink} for reporting user-visible errors in the current scope, or {@code null}
 * if no error sink is installed (legacy mode).
 */
default ErrorSink getErrorSink() {
    return null;
}
```

Note: Use a `default` method so that `DisplayContextAdapter` (the legacy bridge) returns `null` without changes, and only `DefaultReactDisplayContext` needs to implement storage.

**Step 2: Add ErrorSink storage to DefaultReactDisplayContext**

In `DefaultReactDisplayContext.java`, add a mutable field and setter/getter:

```java
private ErrorSink _errorSink;

@Override
public ErrorSink getErrorSink() {
    return _errorSink;
}

/**
 * Sets the {@link ErrorSink} for the current rendering scope.
 *
 * <p>
 * Called by the app shell control during rendering to make its snackbar available to all
 * descendant controls.
 * </p>
 *
 * @param errorSink
 *        The error sink.
 */
public void setErrorSink(ErrorSink errorSink) {
    _errorSink = errorSink;
}
```

Add import for `ErrorSink`:

```java
import com.top_logic.layout.react.control.ErrorSink;
```

**Step 3: Add ErrorSink field to ReactControl**

In `ReactControl.java`, add a field after `_viewContext` (line 67):

```java
private ErrorSink _errorSink;
```

In `write(ReactDisplayContext context, TagWriter out)` (line 155), after `_viewContext = context;` (line 156), add:

```java
_errorSink = context.getErrorSink();
```

In `writeAsChild(JsonWriter writer, ReactDisplayContext viewContext)` (line 339), after `_viewContext = viewContext;` (line 342), add:

```java
_errorSink = viewContext.getErrorSink();
```

Add a public getter:

```java
/**
 * The {@link ErrorSink} for reporting user-visible errors, or {@code null} in legacy mode.
 */
public ErrorSink getErrorSink() {
    return _errorSink;
}
```

**Step 4: Commit**

```
Ticket #29108: Propagate ErrorSink through ReactDisplayContext to ReactControl.
```

---

### Task 4: Create ErrorSink implementation in ReactAppShellControl

The app shell creates an `ErrorSink` backed by its `ReactSnackbarControl` and installs it on the `ReactDisplayContext` before children are rendered.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/nav/ReactAppShellControl.java`

**Step 1: Create ErrorSink implementation and install it**

Add an `ErrorSink` field and create it in the constructor:

```java
private final ErrorSink _errorSink;
```

In the constructor, after the `_snackbar` initialization (after line 73), create the sink:

```java
_errorSink = new ErrorSink() {
    @Override
    public void showError(String message) {
        showSnackbar(message, "error");
    }

    @Override
    public void showWarning(String message) {
        showSnackbar(message, "warning");
    }

    @Override
    public void showInfo(String message) {
        showSnackbar(message, "info");
    }
};
```

**Step 2: Install ErrorSink on the ReactDisplayContext during rendering**

Override `onBeforeWrite` to set the sink on the context before children are rendered:

```java
@Override
protected void onBeforeWrite(ReactDisplayContext context) {
    if (context instanceof DefaultReactDisplayContext defaultContext) {
        defaultContext.setErrorSink(_errorSink);
    }
}
```

Add imports:

```java
import com.top_logic.layout.react.DefaultReactDisplayContext;
import com.top_logic.layout.react.control.ErrorSink;
```

**Step 3: Commit**

```
Ticket #29108: Install ErrorSink from ReactAppShellControl during rendering.
```

---

### Task 5: Route InfoService entries through ErrorSink in ReactServlet

Modify `forwardPendingUpdates()` to check the target control for an `ErrorSink` and route messages there instead of generating legacy JavaScript.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java`

**Step 1: Pass the target control to forwardPendingUpdates**

Change the `handleCommand` method to pass the control to `forwardPendingUpdates`. At line 266, change:

```java
forwardPendingUpdates(displayContext, rootHandler, queue);
```

to:

```java
forwardPendingUpdates(displayContext, rootHandler, queue, control);
```

**Step 2: Update forwardPendingUpdates signature and add ErrorSink routing**

Change the method signature and add the ErrorSink path. Replace the existing method (lines 369-383) with:

```java
@SuppressWarnings("unchecked")
private void forwardPendingUpdates(DisplayContext displayContext, SubsessionHandler rootHandler,
        SSEUpdateQueue queue, ReactCommandTarget control) {
    // Forward InfoService messages.
    if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
        List<HTMLFragment> entries = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
        if (!entries.isEmpty()) {
            ErrorSink errorSink = control instanceof ReactControl rc ? rc.getErrorSink() : null;
            if (errorSink != null) {
                forwardToErrorSink(entries, errorSink);
            } else {
                String jsCode = InfoServiceXMLStringConverter.getJSInvocation(displayContext, entries);
                queue.enqueue(JSSnipplet.create().setCode(jsCode));
            }
        }
    }

    // Collect and forward pending legacy control repaints.
    forwardLegacyControlUpdates(displayContext, rootHandler, queue);
}
```

**Step 3: Add the forwardToErrorSink helper method**

Add after `forwardPendingUpdates`:

```java
private void forwardToErrorSink(List<HTMLFragment> entries, ErrorSink errorSink) {
    for (HTMLFragment entry : entries) {
        if (entry instanceof DefaultInfoServiceItem item) {
            String message = Resources.getInstance().getString(item.getHeaderText());
            String kindOfClass = item.getKindOfClass();

            if (InfoService.ERROR_CSS.equals(kindOfClass)) {
                errorSink.showError(message);
            } else if (InfoService.WARNING_CSS.equals(kindOfClass)) {
                errorSink.showWarning(message);
            } else {
                errorSink.showInfo(message);
            }
        }
    }
}
```

**Step 4: Fix other callers of forwardPendingUpdates**

Search for other calls to `forwardPendingUpdates` in `ReactServlet.java` (e.g. `handleUpload`). Update them to pass the control parameter. If a caller doesn't have a control reference, pass `null` (the method handles `null` gracefully via the `instanceof` check).

Check these methods:
- `handleUpload` (search for `forwardPendingUpdates` in the file)

**Step 5: Add imports**

```java
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.event.infoservice.DefaultInfoServiceItem;
import com.top_logic.basic.util.ResourcesModule;
```

Note: `Resources` is already likely imported. Check and add only missing imports.

**Step 6: Commit**

```
Ticket #29108: Route InfoService messages through ErrorSink in ReactServlet.
```

---

### Task 6: Build and verify

**Step 1: Build the modified modules**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic
mvn install -DskipTests=true

cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.react
mvn install -DskipTests=true

cd /home/bhu/devel/tl-engine/.worktrees/agent-c/com.top_logic.layout.view
mvn install -DskipTests=true
```

**Step 2: Fix any compilation errors**

Address import issues, missing dependencies, or signature mismatches.

**Step 3: Commit any fixes**

```
Ticket #29108: Fix compilation issues in ErrorSink integration.
```
