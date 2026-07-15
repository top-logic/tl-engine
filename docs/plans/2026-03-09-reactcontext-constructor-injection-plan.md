# ReactContext Constructor Injection Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Pass ReactContext to the ReactControl constructor so controls are fully initialized at creation time, eliminating late-binding of context during write().

**Architecture:** Add ReactContext as the first parameter to ReactControl's constructor. Move ID allocation and SSE registration from write() to the constructor. Change IReactControl.write() to take only TagWriter. Move ErrorSink scoping from render-time mutation to ViewContext hierarchical scoping. Update all 42 subclasses and ~20 UIElement call sites.

**Tech Stack:** Java 17, Maven

**Important context:**
- Source encoding is ISO-8859-1.
- Member variables use `_` prefix.
- Commit messages: `Ticket #29108: <description>` — no Co-Authored-By lines.
- The module `com.top_logic.layout.react` cannot depend on `com.top_logic.layout.view`.
- `HTMLFragment` interface (in tl-core) cannot be changed.
- Pre-existing build failure in `PanelElement.java` (unrelated) — ignore it.

---

### Task 1: Update ReactControl base class — constructor takes ReactContext

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`

**What to change:**

1. **Constructor** — add `ReactContext context` as first parameter. Move initialization from write() to constructor:

```java
public ReactControl(ReactContext context, Object model, String reactModule) {
    _reactContext = context;
    _model = model;
    _reactModule = reactModule;
    _reactState = new HashMap<>();
    _id = context.allocateId();
    _sseQueue = context.getSSEQueue();
    _sseQueue.registerControl(this);
}
```

2. **Make `_reactContext` final**, `_id` final, `_sseQueue` non-final (still cleared in cleanupTree).

3. **IReactControl.write()** — remove ReactContext parameter. Use stored context:

```java
@Override
public void write(TagWriter out) throws IOException {
    onBeforeWrite();
    try {
        String stateJson = toJsonString(_reactContext, _reactState);

        out.beginBeginTag(HTMLConstants.DIV);
        writeIdAttribute(out);
        writeControlClasses(out);
        out.writeAttribute("data-react-module", _reactModule);
        out.writeAttribute("data-react-state", stateJson);
        out.writeAttribute("data-window-name", _reactContext.getWindowName());
        out.writeAttribute("data-context-path", _reactContext.getContextPath());
        out.endBeginTag();
        out.endTag(HTMLConstants.DIV);
    } finally {
        onAfterWrite();
    }
}
```

4. **HTMLFragment.write()** — delegate to the new parameterless write():

```java
@Override
public void write(DisplayContext context, TagWriter out) throws IOException {
    write(out);
}
```

5. **onBeforeWrite() / onAfterWrite()** — remove ReactContext parameter:

```java
protected void onBeforeWrite() {
    // Default: no-op.
}

protected void onAfterWrite() {
    // Default: no-op.
}
```

6. **writeAsChild()** — remove ReactContext parameter. Remove lazy-init block (controls already initialized). Keep the rest:

```java
protected void writeAsChild(JsonWriter writer) throws IOException {
    writer.beginObject();
    writer.name("controlId");
    writer.value(getID());
    writer.name("module");
    writer.value(_reactModule);
    writer.name("state");
    writeState(writer);
    writer.endObject();
}
```

7. **writeState()** — remove ReactContext parameter, use stored `_reactContext`:

```java
private void writeState(JsonWriter writer) throws IOException {
    writeJsonMap(_reactContext, writer, getReactState());
}
```

8. **writeJsonValue()** — where it calls `writeAsChild`, change to parameterless:

```java
} else if (value instanceof ReactControl) {
    ((ReactControl) value).writeAsChild(writer);
}
```

9. **getID()** — remove the null check / IllegalStateException. ID is always set:

```java
@Override
public String getID() {
    return _id;
}
```

10. **isSSEAttached()** — always true after construction. Can be simplified or removed, but for now just return true:

```java
protected boolean isSSEAttached() {
    return _sseQueue != null;
}
```

(Keep as-is since cleanupTree nulls _sseQueue.)

11. **registerChildControl()** — simplify. The child already has its own context from construction. Only register with SSE if not already registered:

```java
protected void registerChildControl(ReactControl child) {
    // Child already has context and ID from construction.
    // Just ensure it's on this control's SSE queue if not already.
    SSEUpdateQueue queue = _sseQueue;
    if (queue != null && child._sseQueue == null) {
        child._sseQueue = queue;
        queue.registerControl(child);
    }
}
```

12. **stateAsJSON()** — remove the assert (context is always set):

```java
public final String stateAsJSON() {
    StringW buffer = new StringW();
    try {
        writeState(new JsonWriter(buffer));
    } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
    return buffer.toString();
}
```

13. **patchReactState(SSEUpdateQueue, Map)** — the `toJsonString` call already uses `_reactContext`, no change needed.

**Step 1: Apply all changes above.**
**Step 2: Commit.**

```
Ticket #29108: Pass ReactContext to ReactControl constructor, remove context from write().
```

### Task 2: Update IReactControl interface

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/IReactControl.java`

**What to change:**

1. Change `write()` signature and update JavaDoc:

```java
public interface IReactControl {

    /**
     * The control's unique ID (assigned at construction time).
     */
    String getID();

    /**
     * Renders this control into the given writer.
     *
     * @param out
     *        The writer to render into.
     */
    void write(TagWriter out) throws IOException;
}
```

2. Update JavaDoc on `getID()` — no longer "assigned during write".

**Step 1: Apply changes.**
**Step 2: Commit.**

```
Ticket #29108: Remove ReactContext parameter from IReactControl.write().
```

### Task 3: Update all ReactControl subclasses in com.top_logic.layout.react

**Files:** All 34 subclasses in `com.top_logic.layout.react`. Each needs:

1. Add `ReactContext context` as **first** constructor parameter.
2. Pass `context` to `super(context, model, reactModule)`.
3. For convenience constructors that delegate via `this(...)`, pass `context` through.
4. For subclasses that override `onBeforeWrite(ReactContext)` or `onAfterWrite(ReactContext)`, change to `onBeforeWrite()` / `onAfterWrite()`.
5. For subclasses that override `writeAsChild(ReactContext, JsonWriter)`, change to `writeAsChild(JsonWriter)`.

**The pattern for every subclass is mechanical.** Example:

```java
// Before:
public ReactButtonControl(String label, ButtonAction action) {
    super(null, "TLButton");

// After:
public ReactButtonControl(ReactContext context, String label, ButtonAction action) {
    super(context, null, "TLButton");
```

**Subclasses with writeAsChild overrides (4 files):**

- `ReactSidebarControl.writeAsChild(JsonWriter)` — remove `ReactContext context` param, call `super.writeAsChild(writer)`.
- `ReactTabBarControl.writeAsChild(JsonWriter)` — same.
- `ReactDeckPaneControl.writeAsChild(JsonWriter)` — same.
- `ReactFormFieldControl.writeAsChild(JsonWriter)` — same.

**Subclass with onBeforeWrite/onAfterWrite override (1 file):**

- `ReactAppShellControl` — **remove** the onBeforeWrite/onAfterWrite overrides entirely. ErrorSink scoping moves to Task 5.

**Complete list of files to modify (in com.top_logic.layout.react):**

- `control/button/ReactButtonControl.java`
- `control/form/ReactFormFieldControl.java`
- `control/layout/ReactCardControl.java`
- `control/layout/ReactDeckPaneControl.java`
- `control/layout/ReactFormFieldChromeControl.java`
- `control/layout/ReactFormGroupControl.java`
- `control/layout/ReactFormLayoutControl.java`
- `control/layout/ReactGridControl.java`
- `control/layout/ReactMaximizeRootControl.java`
- `control/layout/ReactPanelControl.java`
- `control/layout/ReactSplitPanelControl.java`
- `control/layout/ReactStackControl.java`
- `control/nav/ReactAppBarControl.java`
- `control/nav/ReactAppShellControl.java`
- `control/nav/ReactBottomBarControl.java`
- `control/nav/ReactBreadcrumbControl.java`
- `control/overlay/ReactDialogControl.java`
- `control/overlay/ReactDrawerControl.java`
- `control/overlay/ReactMenuControl.java`
- `control/overlay/ReactSnackbarControl.java`
- `control/photo/ReactPhotoCaptureControl.java`
- `control/photo/ReactPhotoViewerControl.java`
- `control/sidebar/ReactSidebarControl.java`
- `control/tabbar/ReactTabBarControl.java`
- `control/table/ReactResourceCellControl.java`
- `control/table/ReactTableControl.java`
- `control/table/ReactTextCellControl.java`
- `control/toggle/ReactToggleButtonControl.java`
- `control/tree/ReactTreeControl.java`
- `control/upload/ReactFileUploadControl.java`
- `control/audio/ReactAudioPlayerControl.java`
- `control/audio/ReactAudioRecorderControl.java`
- `control/common/ReactFieldListControl.java`
- `control/download/ReactDownloadControl.java`

**For ReactAppShellControl specifically:**

Remove `_previousErrorSink` field. Remove `onBeforeWrite()` and `onAfterWrite()` overrides. The `_errorSink` field stays — it will be passed to the ViewContext in Task 5.

Update the constructor to accept and pass `ReactContext`:

```java
public ReactAppShellControl(ReactContext context, ReactControl header, ReactControl content, ReactControl footer) {
    super(context, null, REACT_MODULE);
    // ... rest unchanged ...
    // But: _snackbar must also receive context:
    _snackbar = new ReactSnackbarControl(context, "", "success", () -> { });
}
```

**Important:** Any subclass constructor that creates child controls (e.g. ReactAppShellControl creating ReactSnackbarControl) must pass the `context` to those children.

**Step 1: Apply all changes.**
**Step 2: Commit.**

```
Ticket #29108: Add ReactContext to all ReactControl subclass constructors.
```

### Task 4: Update ReactControl subclasses in com.top_logic.layout.view

**Files:** All 6 subclasses in `com.top_logic.layout.view`:

- `view/form/FormControl.java`
- `view/form/ViewCheckboxControl.java`
- `view/form/ViewDatePickerControl.java`
- `view/form/ViewNumberInputControl.java`
- `view/form/ViewSelectControl.java`
- `view/form/ViewTextInputControl.java`

Same mechanical pattern: add `ReactContext context` as first param, pass to super.

**Example:**

```java
// Before:
public FormControl(TLObject initialObject, String noModelMessage) {
    super(initialObject, "TLFormLayout");

// After:
public FormControl(ReactContext context, TLObject initialObject, String noModelMessage) {
    super(context, initialObject, "TLFormLayout");
```

**Step 1: Apply all changes.**
**Step 2: Commit.**

```
Ticket #29108: Add ReactContext to view-layer ReactControl subclass constructors.
```

### Task 5: Add ErrorSink scoping to ViewContext

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactContext.java`

**ViewContext interface — add `withErrorSink()`:**

```java
/**
 * Creates a derived context with the given {@link ErrorSink}.
 *
 * @param errorSink
 *        The error sink for the scope.
 * @return A new context with the given error sink.
 */
ViewContext withErrorSink(ErrorSink errorSink);
```

Add import for `com.top_logic.layout.react.control.ErrorSink`.

**DefaultViewContext — add ErrorSink field and implement withErrorSink():**

Add `_errorSink` field (final, nullable). Pass through private constructor. Implement:

```java
private final ErrorSink _errorSink;

// Update private constructor to accept errorSink:
private DefaultViewContext(ReactContext reactContext, String personalizationPath,
        Map<String, ViewChannel> channels, CommandScope commandScope,
        FormControl formControl, ErrorSink errorSink) {
    _reactContext = reactContext;
    _personalizationPath = personalizationPath;
    _channels = channels;
    _commandScope = commandScope;
    _formControl = formControl;
    _errorSink = errorSink;
}

// Public constructor passes null:
public DefaultViewContext(ReactContext reactContext) {
    this(reactContext, "view", new HashMap<>(), null, null, null);
}

@Override
public ViewContext withErrorSink(ErrorSink errorSink) {
    return new DefaultViewContext(_reactContext, _personalizationPath, _channels,
        _commandScope, _formControl, errorSink);
}

@Override
public ErrorSink getErrorSink() {
    return _errorSink;
}
```

Update `childContext()` and `withCommandScope()` to propagate `_errorSink` through the new constructor.

**DefaultReactContext — remove setErrorSink():**

Remove the `setErrorSink()` method and the `_errorSink` field. Remove `getErrorSink()` override. The `ReactContext` interface already has a `default getErrorSink()` returning null, which is correct for the base context.

**Step 1: Apply all changes.**
**Step 2: Commit.**

```
Ticket #29108: Add ErrorSink scoping to ViewContext, remove mutable ErrorSink from DefaultReactContext.
```

### Task 6: Update UIElement implementations to pass ViewContext to control constructors

**Files:** All UIElement createControl() methods in `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/`:

- `AppShellElement.java` — pass `context` to `new ReactAppShellControl(context, ...)`. Also install ErrorSink: create `context.withErrorSink(appShellControl.getErrorSink())` before creating children, or better: the AppShellElement creates the ErrorSink, calls `context.withErrorSink(errorSink)`, then passes that derived context to the AppShellControl and its children.
- `AppBarElement.java` — pass `context` to control constructors
- `BottomBarElement.java` — same
- `ButtonElement.java` — pass `context` to `new ReactButtonControl(context, ...)`
- `CardElement.java` — same
- `FieldElement.java` — pass `context` to field control constructors
- `FormElement.java` — pass `context` to `new FormControl(context, ...)`
- `GridElement.java` — same
- `PanelElement.java` — same
- `SidebarElement.java` — pass `context` to `new ReactSidebarControl(context, ...)`
- `SplitPanelElement.java` — same
- `StackElement.java` — same
- `TabBarElement.java` — same
- `TableElement.java` — same
- `TreeElement.java` — same
- `ReferenceElement.java` — context already available

**AppShellElement ErrorSink integration:**

The key change in `AppShellElement.createControl()`:

```java
@Override
public IReactControl createControl(ViewContext context) {
    ReactAppShellControl shell = new ReactAppShellControl(context, header, content, footer);

    // Install the shell's error sink into the context for child elements.
    ViewContext errorSinkContext = context.withErrorSink(shell.getErrorSink());

    // Create slot controls in the error-sink-scoped context.
    ReactControl header = createSlotControl(errorSinkContext, _header);
    ReactControl content = createSlotControl(errorSinkContext, _content);
    ReactControl footer = createSlotControl(errorSinkContext, _footer);

    return shell;
}
```

Wait — this creates a chicken-and-egg problem: the shell needs the slot controls, but the slot controls need the error-sink context from the shell. The solution: create the shell first without slots, then create slots in the scoped context, then set them on the shell. Or: create the ErrorSink independently (not owned by shell), pass it to both the shell and the context.

**Simpler approach:** The ErrorSink is created by AppShellElement independently, passed to both ViewContext and the AppShellControl:

```java
@Override
public IReactControl createControl(ViewContext context) {
    // Create snackbar control and error sink.
    ReactSnackbarControl snackbar = new ReactSnackbarControl(context, ...);
    ErrorSink errorSink = createErrorSink(snackbar);

    // Derive context with error sink for children.
    ViewContext scopedContext = context.withErrorSink(errorSink);

    ReactControl header = createSlotControl(scopedContext, _header);
    ReactControl content = createSlotControl(scopedContext, _content);
    ReactControl footer = createSlotControl(scopedContext, _footer);

    return new ReactAppShellControl(context, header, content, footer, snackbar);
}
```

This means ReactAppShellControl's constructor takes snackbar from outside instead of creating it internally. Add a `getErrorSink()` accessor to make the ErrorSink available.

**Step 1: Apply all changes.**
**Step 2: Commit.**

```
Ticket #29108: Pass ViewContext to control constructors in all UIElement implementations.
```

### Task 7: Update ViewServlet to use parameterless write()

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

**Change line 254:**

```java
// Before:
rootControl.write(context, out);

// After:
rootControl.write(out);
```

Also update the `renderPage()` method signature — it no longer needs the `ReactContext` parameter since the control has it:

```java
// Before:
private void renderPage(HttpServletRequest request, HttpServletResponse response,
        IReactControl rootControl, ReactContext context) throws IOException {

// After:
private void renderPage(HttpServletRequest request, HttpServletResponse response,
        IReactControl rootControl, ReactContext context) throws IOException {
```

Actually, `renderPage()` still needs `context` for `context.getWindowName()` and `context.getContextPath()` on lines 247-248 (body attributes). Keep the parameter.

Just change line 254:

```java
rootControl.write(out);
```

**Step 1: Apply change.**
**Step 2: Commit.**

```
Ticket #29108: Use parameterless write() in ViewServlet.
```

### Task 8: Update demo module

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoFieldTogglesControl.java`
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactCounterComponent.java`
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/DemoCounterElement.java` (if it creates controls)

Same mechanical pattern: add `ReactContext context` to constructors, pass to super.

**Step 1: Apply changes.**
**Step 2: Commit.**

```
Ticket #29108: Add ReactContext to demo ReactControl subclass constructors.
```

### Task 9: Update test code

**Files:**
- Modify: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestChannelDeclaration.java`
- Any other test files that construct ReactControl or its subclasses.

Tests that construct `DefaultViewContext(null)` will still work because DefaultViewContext delegates ReactContext methods to the wrapped context. But `createControl()` calls that create ReactControls now need a non-null ReactContext (for ID allocation and SSE registration).

**Step 1: Check which tests need updates.**
**Step 2: Apply changes.**
**Step 3: Commit.**

```
Ticket #29108: Update tests for ReactContext constructor injection.
```

### Task 10: Build and verify

**Step 1: Build com.top_logic.layout.react:**

```bash
cd com.top_logic.layout.react && mvn clean compile -DskipTests=true
```

Expected: BUILD SUCCESS (or only pre-existing PanelElement errors if that's in this module).

**Step 2: Build com.top_logic.layout.view:**

```bash
cd com.top_logic.layout.view && mvn clean compile -DskipTests=true -Dmaven.compiler.failOnError=false
```

Expected: Only pre-existing PanelElement errors.

**Step 3: Build com.top_logic.demo:**

```bash
cd com.top_logic.demo && mvn clean compile -DskipTests=true
```

Expected: BUILD SUCCESS.

**Step 4: Run tests:**

```bash
cd com.top_logic.layout.view && mvn test -DskipTests=false -Dtest=TestChannelDeclaration
```
