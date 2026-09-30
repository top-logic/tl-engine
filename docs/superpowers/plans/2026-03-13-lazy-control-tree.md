# Lazy Control Tree Construction — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace pre-built control trees in `openWindow()` with a factory pattern so controls are constructed with the child window's `ReactContext`.

**Architecture:** `openWindow()` accepts a `ReactControlProvider` + model instead of an `IReactControl`. `WindowEntry` stores the factory. `ViewServlet.doGet()` calls the factory with the child window's context to build the tree at render time.

**Tech Stack:** Java 17, JUnit 4, Maven

**Spec:** `docs/superpowers/specs/2026-03-13-child-window-queue-assignment-design.md`

---

## Chunk 1: WindowEntry and ReactWindowRegistry

### Task 1: Update WindowEntry to store a factory instead of a pre-built control

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowEntry.java`

- [ ] **Step 1: Replace the fields and constructor**

Replace the `IReactControl _rootControl` field with three fields: `ReactControlProvider _controlProvider`, `Object _model`, and `volatile ReactControl _rootControl`. Update the constructor and add a second constructor for no-control windows.

```java
// New imports
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;

// Remove import:
// import com.top_logic.layout.react.control.IReactControl;
```

Replace the fields (lines 15-25):
```java
private final String _windowId;
private final String _openerWindowId;
private final ReactControlProvider _controlProvider;
private final Object _model;
private final WindowOptions _options;
private final Runnable _closeCallback;
private volatile ReactControl _rootControl;
private boolean _connected;
```

Replace the constructor (lines 27-48) with two constructors:
```java
/**
 * Creates a new {@link WindowEntry} with a control provider.
 *
 * @param windowId
 *        The unique ID for this window.
 * @param openerWindowId
 *        The window ID of the opener.
 * @param controlProvider
 *        Factory that creates the control tree when the window connects.
 * @param model
 *        The model object passed to the control provider.
 * @param options
 *        The window display options.
 * @param closeCallback
 *        Optional callback invoked when the window is closed, or null.
 */
public WindowEntry(String windowId, String openerWindowId,
        ReactControlProvider controlProvider, Object model,
        WindowOptions options, Runnable closeCallback) {
    _windowId = windowId;
    _openerWindowId = openerWindowId;
    _controlProvider = controlProvider;
    _model = model;
    _options = options;
    _closeCallback = closeCallback;
}

/**
 * Creates a new {@link WindowEntry} without a control provider.
 *
 * @param windowId
 *        The unique ID for this window.
 * @param openerWindowId
 *        The window ID of the opener.
 * @param options
 *        The window display options.
 * @param closeCallback
 *        Optional callback invoked when the window is closed, or null.
 */
public WindowEntry(String windowId, String openerWindowId,
        WindowOptions options, Runnable closeCallback) {
    this(windowId, openerWindowId, null, null, options, closeCallback);
}
```

Replace the `getRootControl()` accessor (line 61-63) and add new accessors:
```java
/** The control tree for this window, or {@code null} if not yet built. */
public ReactControl getRootControl() {
    return _rootControl;
}

/** Sets the built control tree. Called by ViewServlet when the window connects. */
public void setRootControl(ReactControl rootControl) {
    _rootControl = rootControl;
}

/** The factory for creating the control tree, or {@code null}. */
public ReactControlProvider getControlProvider() {
    return _controlProvider;
}

/** The model object passed to the control provider. */
public Object getModel() {
    return _model;
}
```

- [ ] **Step 2: Verify the module compiles**

Run: `mvn compile -DskipTests=true -f com.top_logic.layout.react/pom.xml`

Expected: Compilation **fails** because `ReactWindowRegistry` still passes `IReactControl` to the old constructor. This confirms the old API is gone.

- [ ] **Step 3: Commit WindowEntry changes**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowEntry.java
git commit -m "Ticket #29108: Replace pre-built control with factory in WindowEntry."
```

---

### Task 2: Update ReactWindowRegistry to accept a factory

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java`

- [ ] **Step 1: Replace the openWindow overloads**

Add import:
```java
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
```

Remove the two pre-built control overloads (lines 102-116, 118-152) and replace with:

```java
/**
 * Opens a new window with a control provider.
 *
 * @param openerContext
 *        The opener window's {@link ReactContext}.
 * @param controlProvider
 *        Factory that creates the control tree when the child window connects.
 * @param model
 *        The model object passed to the control provider.
 * @param options
 *        Window display options.
 * @return The generated window ID.
 */
public String openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
        Object model, WindowOptions options) {
    return openWindow(openerContext, controlProvider, model, options, null);
}

/**
 * Opens a new window with a control provider and a close callback.
 *
 * @param openerContext
 *        The opener window's {@link ReactContext}.
 * @param controlProvider
 *        Factory that creates the control tree when the child window connects.
 * @param model
 *        The model object passed to the control provider.
 * @param options
 *        Window display options.
 * @param closeCallback
 *        Optional callback invoked when the window is closed, or null.
 * @return The generated window ID.
 */
public String openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
        Object model, WindowOptions options, Runnable closeCallback) {
    String windowId = generateWindowId();
    String openerWindowId = openerContext.getWindowName();

    WindowEntry entry = new WindowEntry(windowId, openerWindowId, controlProvider, model,
        options, closeCallback);
    _windows.put(windowId, entry);

    WindowOpenEvent event = WindowOpenEvent.create()
        .setTargetWindowId(openerWindowId)
        .setWindowId(windowId)
        .setWidth(options.getWidth())
        .setHeight(options.getHeight())
        .setTitle(options.getTitle())
        .setResizable(options.isResizable());
    SSEUpdateQueue openerQueue = getQueue(openerWindowId);
    if (openerQueue != null) {
        openerQueue.enqueue(event);
    }

    return windowId;
}
```

Update the no-control convenience overload (lines 98-100) to use the no-control `WindowEntry` constructor:

```java
public String openWindow(ReactContext openerContext, WindowOptions options) {
    String windowId = generateWindowId();
    String openerWindowId = openerContext.getWindowName();

    WindowEntry entry = new WindowEntry(windowId, openerWindowId, options, null);
    _windows.put(windowId, entry);

    WindowOpenEvent event = WindowOpenEvent.create()
        .setTargetWindowId(openerWindowId)
        .setWindowId(windowId)
        .setWidth(options.getWidth())
        .setHeight(options.getHeight())
        .setTitle(options.getTitle())
        .setResizable(options.isResizable());
    SSEUpdateQueue openerQueue = getQueue(openerWindowId);
    if (openerQueue != null) {
        openerQueue.enqueue(event);
    }

    return windowId;
}
```

- [ ] **Step 2: Update windowClosed() and valueUnbound()**

In `windowClosed()` (line 184), change the type from `IReactControl` to `ReactControl`:
```java
ReactControl rootControl = entry.getRootControl();
if (rootControl != null) {
    rootControl.cleanupTree();
}
```

In `valueUnbound()` (lines 206-211), same change:
```java
for (WindowEntry entry : _windows.values()) {
    ReactControl rootControl = entry.getRootControl();
    if (rootControl != null) {
        rootControl.cleanupTree();
    }
}
```

Remove the now-unused import:
```java
// Remove: import com.top_logic.layout.react.control.IReactControl;
```

- [ ] **Step 3: Verify the react module compiles**

Run: `mvn compile -DskipTests=true -f com.top_logic.layout.react/pom.xml`

Expected: PASS (react module compiles). The `com.top_logic.layout.view` and `com.top_logic.demo` modules will still fail because they reference the removed overloads.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java
git commit -m "Ticket #29108: Replace pre-built control overloads with factory pattern in ReactWindowRegistry."
```

---

## Chunk 2: ViewServlet and Caller Updates

### Task 3: Update ViewServlet to build the control tree from the factory

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

- [ ] **Step 1: Replace the pre-built control tree path**

Replace lines 94-107 (the `windowEntry` block) with:

```java
// Check if this is a programmatically opened window with a control provider.
ReactWindowRegistry windowRegistry = ReactWindowRegistry.forSession(session);
SSEUpdateQueue sseQueue = windowRegistry.getOrCreateQueue(windowName);
WindowEntry windowEntry = windowRegistry.getWindow(windowName);
if (windowEntry != null) {
    windowEntry.markConnected();
    ReactControlProvider controlProvider = windowEntry.getControlProvider();
    if (controlProvider != null) {
        ReactContext displayContext = new DefaultReactContext(
            request.getContextPath(), windowName, sseQueue, windowRegistry);
        ReactControl rootControl = controlProvider.createControl(
            displayContext, windowEntry.getModel());
        windowEntry.setRootControl(rootControl);
        renderPage(request, response, rootControl, displayContext);
        return;
    }
}
```

Add imports:
```java
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
```

Remove the now-unused import (if present):
```java
// Remove: import com.top_logic.layout.react.control.IReactControl;
```

Wait — `IReactControl` is still used by `renderPage()` signature and the view-based loading path (line 125). Keep the import.

- [ ] **Step 2: Verify the view module compiles**

Run: `mvn compile -DskipTests=true -f com.top_logic.layout.view/pom.xml`

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: Build control tree from factory in ViewServlet.doGet()."
```

---

### Task 4: Update OpenWindowDemoCommand to pass a factory

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/command/OpenWindowDemoCommand.java`

- [ ] **Step 1: Replace the pre-built control with a factory lambda**

Replace lines 55-67 (the counter creation + openWindow call) with:

```java
registry.openWindow(context,
    (ctx, model) -> new DemoCounterControl(ctx, (String) model),
    windowLabel, options, () -> {
        if (errorSink != null) {
            errorSink.showInfo(Fragments.text(windowLabel + " closed."));
        }
    });
```

- [ ] **Step 2: Verify the demo module compiles**

Run: `mvn compile -DskipTests=true -f com.top_logic.demo/pom.xml`

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.demo/src/main/java/com/top_logic/demo/view/command/OpenWindowDemoCommand.java
git commit -m "Ticket #29108: Pass control factory instead of pre-built control in OpenWindowDemoCommand."
```

---

## Chunk 3: Tests and Verification

### Task 5: Run existing tests to verify no regressions

**Files:**
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestReactWindowRegistry.java`

- [ ] **Step 1: Run the existing tests**

Run: `mvn test -DskipTests=false -Dtest=TestReactWindowRegistry -f com.top_logic.layout.view/pom.xml`

Expected: All 4 tests PASS. They use the no-control `openWindow(ctx, options)` overload which was not changed.

- [ ] **Step 2: Add a test for the factory-based openWindow**

Add this test method to `TestReactWindowRegistry`:

```java
public void testOpenWindowWithProvider() {
    ReactWindowRegistry registry = new ReactWindowRegistry();
    SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
    ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);

    WindowOptions options = new WindowOptions().setWidth(800).setTitle("Provider Test");

    String windowId = registry.openWindow(openerCtx,
        (ctx, model) -> new ReactControl(ctx, model, "test-module"),
        "testModel", options);

    assertNotNull(windowId);
    WindowEntry entry = registry.getWindow(windowId);
    assertNotNull(entry);
    assertNotNull("Provider must be stored", entry.getControlProvider());
    assertEquals("testModel", entry.getModel());
    assertNull("Root control must be null before ViewServlet builds it",
        entry.getRootControl());

    // Simulate what ViewServlet does: build the tree with the child's context.
    SSEUpdateQueue childQueue = registry.getOrCreateQueue(windowId);
    ReactContext childCtx = new DefaultReactContext("", windowId, childQueue, registry);
    ReactControl builtControl = entry.getControlProvider().createControl(
        childCtx, entry.getModel());
    entry.setRootControl(builtControl);

    assertNotNull("Root control must be set after building", entry.getRootControl());
    // Verify the control is registered on the child's queue, not the opener's.
    assertNotNull("Control must be findable on child queue",
        childQueue.getControl(builtControl.getID()));
    assertNull("Control must NOT be on opener queue",
        openerQueue.getControl(builtControl.getID()));
}
```

Add imports:
```java
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
```

- [ ] **Step 3: Run the new test to verify it passes**

Run: `mvn test -DskipTests=false -Dtest=TestReactWindowRegistry#testOpenWindowWithProvider -f com.top_logic.layout.view/pom.xml`

Expected: PASS

- [ ] **Step 4: Run all tests together**

Run: `mvn test -DskipTests=false -Dtest=TestReactWindowRegistry -f com.top_logic.layout.view/pom.xml`

Expected: All 5 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestReactWindowRegistry.java
git commit -m "Ticket #29108: Add test verifying factory-based openWindow registers controls on child queue."
```

---

### Task 6: Install the react module to update the WAR

**Files:** None (build step only)

- [ ] **Step 1: Install the react module**

Per MEMORY.md: static webapp resources are served from web-fragment WARs in `~/.m2/repository`. The react module must be installed so that the demo app picks up the changes.

Run: `mvn clean install -DskipTests=true -f com.top_logic.layout.react/pom.xml`

Expected: BUILD SUCCESS

- [ ] **Step 2: Install the view module**

Run: `mvn install -DskipTests=true -f com.top_logic.layout.view/pom.xml`

Expected: BUILD SUCCESS

- [ ] **Step 3: Compile the demo module**

Per CLAUDE.md: NEVER use `mvn clean` on app modules. Use incremental compilation.

Run: `mvn compile -DskipTests=true -f com.top_logic.demo/pom.xml`

Expected: BUILD SUCCESS

---

### Task 7: Manual runtime verification

- [ ] **Step 1: Start the demo app**

Start the demo app with: `mvn jetty:start -Dtl_initial_password=root1234 -f com.top_logic.demo/pom.xml`

- [ ] **Step 2: Open a child window and verify the fix**

1. Log in at `http://localhost:8080` (root / root1234)
2. Navigate to the dashboard with the "Open Window" demo command
3. Click to open a child window
4. Verify the child window renders correctly (counter control visible)
5. Interact with the counter in the child window — verify no 404 errors in the server log
6. Close the child window — verify the close callback fires (snackbar appears in main window)
