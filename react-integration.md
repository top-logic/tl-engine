# React Integration for TopLogic Applications

## Specification: `com.top_logic.layout.react`

**Status**: Draft
**Target**: TopLogic 7.10.x

---

## 1. Overview

This specification defines how React-based UI controls can replace or augment existing
TopLogic Controls. A `ReactControl` is a fine-grained, interactive UI element -- the same
abstraction level as `TextInputControl`, `SelectControl`, or `BPMNDisplay`. It renders a
DOM subtree managed by React, while participating fully in TopLogic's control lifecycle
(attachment, detachment, revalidation, command dispatch).

### Design Rationale

TopLogic separates concerns into two layers:

- **LayoutComponent** (coarse-grained): Manages persistent data access (model builders),
  navigation state (component channels), security, and page structure. A LayoutComponent
  does not directly produce HTML.
- **Control** (fine-grained): Handles display and user interaction. Controls render HTML,
  process user input via `ControlCommand`s, and translate between persistent objects and
  UI presentation through `LabelProvider`, `ResourceProvider`, etc.

A React-based UI element is fundamentally a **Control** -- it renders interactive HTML and
handles user events. It should not be a LayoutComponent, because it does not own data
access or navigation. This positioning allows React controls to be used anywhere a
traditional TopLogic Control is used today, enabling incremental migration: a
`TextInputControl` could be replaced by a React-based text input, a chart control by a
React chart, etc.

### Goals

- A `ReactControl` can be used wherever a TopLogic `Control` is used: inside form groups,
  component views, dialogs, table cell renderers, etc.
- React controls participate in the standard `Control` lifecycle: `attach()`, `detach()`,
  `requestRepaint()`, `revalidate()`.
- User interactions in React are dispatched to the server via `ControlCommand`s, using the
  same `dispatchControlCommand` mechanism as existing controls -- but with a **JSON-based
  protocol** instead of the XML/SOAP encoding.
- Server-to-client updates (model changes, value changes) flow through the standard
  revalidation cycle, delivered as React-compatible data (not raw HTML fragments).
- React assets are built with standard tooling (Vite/npm) and packaged into the Maven
  module for deployment.

### Non-Goals (for this iteration)

- Replacing the entire TopLogic UI with React.
- Server-side rendering (SSR) of React components.
- A complete React equivalent of every existing TopLogic form control.

---

## 2. Architecture

```
+----------------------------------------------------------------------+
|  Browser                                                              |
|                                                                       |
|  +------------------------------+  +------------------------------+   |
|  | TopLogic-rendered Controls   |  | React-rendered Controls      |   |
|  | (server HTML + AJAX updates) |  | (client-rendered via React)  |   |
|  |                              |  |                              |   |
|  | TextInputControl             |  | ReactControl                 |   |
|  | SelectControl                |  |  +-----------------------+   |   |
|  | TableControl                 |  |  | React Component Tree  |   |   |
|  | ...                          |  |  | (MyChart, MyEditor..) |   |   |
|  |                              |  |  +-----------------------+   |   |
|  +--------+---------------------+  +-------+----------------------+   |
|           |                                |                          |
|    dispatchControlCommand           dispatchControlCommand             |
|    (XML/SOAP, existing)             (JSON, new ReactServlet)          |
|           |                                |                          |
+-----------+--------------------------------+--------------------------+
            |                                |
            v                                v
+----------------------------------------------------------------------+
|  TopLogic Server (Servlet Container)                                  |
|                                                                       |
|  +------------------+           +--------------------------------+    |
|  | AJAXServlet      |           | ReactServlet                   |    |
|  | /ajax            |           | /react-api/*                   |    |
|  |                  |           |                                |    |
|  | Dispatches to    |           | Dispatches to                  |    |
|  | ControlCommand   |           | ControlCommand (same commands) |    |
|  | via XML args     |           | via JSON args                  |    |
|  +--------+---------+           +-------+------------------------+    |
|           |                             |                             |
|           +----------+------------------+                             |
|                      |                                                |
|           +----------+----------+                                     |
|           | AbstractControlBase |                                     |
|           | - command dispatch   |                                     |
|           | - model listeners   |                                     |
|           | - attach/detach     |                                     |
|           +---------------------+                                     |
|                      |                                                |
|     +----------------+----------------+                               |
|     |                                 |                               |
|  +--+---------------+   +------------+----------+                     |
|  | TextInputControl |   | ReactControl          |                     |
|  | (writes HTML)    |   | (writes mount div,    |                     |
|  |                  |   |  sends JSON state to   |                     |
|  |                  |   |  React on revalidate)  |                     |
|  +------------------+   +-------+---------------+                     |
|                                 |                                     |
|                        +--------+---------+                           |
|                        | FormField /      |                           |
|                        | arbitrary model  |                           |
|                        +------------------+                           |
+----------------------------------------------------------------------+
```

### Key Principle

A `ReactControl` **is a Control**. It extends `AbstractControl` (or
`AbstractFormFieldControl` for form-field-bound controls), registers `ControlCommand`s,
and lives within a `ControlScope`. The only difference from a traditional control is
*how it renders*: instead of writing HTML tags via `TagWriter`, it writes a mount `<div>`
and bootstraps a React component tree into it. Updates flow as JSON data rather than DOM
patches.

---

## 3. Module Structure

```
com.top_logic.layout.react/
+-- pom.xml
+-- package.json                          # npm project for React build tooling
+-- vite.config.ts                        # Vite bundler configuration
+-- tsconfig.json
+-- src/
|   +-- main/
|   |   +-- java/
|   |   |   +-- com/top_logic/layout/react/
|   |   |       +-- ReactControl.java             # Base control for React islands
|   |   |       +-- ReactFormFieldControl.java     # FormField-bound React control
|   |   |       +-- ReactServlet.java              # JSON API servlet for control commands
|   |   |       +-- ReactControlRenderer.java      # Renders the mount <div> + bootstrap
|   |   |       +-- ReactUpdate.java               # ClientAction: JSON state push
|   |   |       +-- ReactControlCommand.java       # Base for JSON-argument commands
|   |   |       +-- I18NConstants.java
|   |   |       +-- package-info.java
|   |   +-- webapp/
|   |   |   +-- WEB-INF/
|   |   |   |   +-- web-fragment.xml               # ReactServlet registration
|   |   |   |   +-- conf/react.conf.xml
|   |   |   |   +-- themes/core/theme.xml          # CSS registration
|   |   |   +-- react/                             # Built React assets (output of Vite)
|   |   |       +-- tl-react-bridge.js             # TL<->React bridge library
|   |   |       +-- vendor.js                      # React runtime (bundled)
|   |   +-- resources/
|   |       +-- META-INF/
|   |           +-- messages_en.properties
|   +-- test/
|       +-- java/
|           +-- test/com/top_logic/layout/react/
|               +-- ...
+-- react-src/                             # React application source (TypeScript)
|   +-- bridge/
|   |   +-- tl-react-bridge.ts             # Client-side bridge API
|   |   +-- types.ts                       # Shared type definitions
|   +-- controls/
|   |   +-- TLTextInput.tsx                # Example: React-based text input
|   |   +-- TLChart.tsx                    # Example: React chart control
|   +-- index.ts                           # Control registry
+-- README.md
```

### Maven Integration

The `pom.xml` uses `frontend-maven-plugin` to run npm/Vite during the Maven build:

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <configuration>
        <nodeVersion>v20.11.0</nodeVersion>
        <workingDirectory>${project.basedir}</workingDirectory>
    </configuration>
    <executions>
        <execution>
            <id>install-node-and-npm</id>
            <goals><goal>install-node-and-npm</goal></goals>
        </execution>
        <execution>
            <id>npm-install</id>
            <goals><goal>npm</goal></goals>
            <configuration>
                <arguments>ci</arguments>
            </configuration>
        </execution>
        <execution>
            <id>vite-build</id>
            <goals><goal>npm</goal></goals>
            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Vite outputs into `src/main/webapp/react/`, which is then packaged into the WAR overlay.

---

## 4. Server-Side: The `ReactControl` Hierarchy

### 4.1 `ReactControl` -- Base Class

Extends `AbstractControl` for controls that display an arbitrary model object via React.

```java
/**
 * A {@link Control} that delegates rendering and interaction to a client-side
 * React component.
 *
 * <p>
 * Instead of writing HTML via {@link TagWriter}, this control writes a mount
 * point {@code <div>} and bootstraps a named React component into it.
 * Model changes are delivered to the client as JSON updates via
 * {@link ReactUpdate}, not as DOM patches.
 * </p>
 */
public class ReactControl extends AbstractControl {

    /** The React component module path (e.g. "controls/TLChart"). */
    private final String _reactModule;

    /** Current state to send to React on mount or repaint. */
    private Map<String, Object> _reactState;

    /**
     * @param model        the model object this control displays
     * @param reactModule  path to the React component (resolved under /react/)
     * @param commands     command map (from {@link #createCommandMap})
     */
    public ReactControl(Object model, String reactModule,
            Map<String, ControlCommand> commands) {
        super(model, commands);
        _reactModule = reactModule;
        _reactState = Collections.emptyMap();
    }

    /**
     * Update the state that will be delivered to the React component.
     * Queues an incremental JSON update (or a repaint if not yet attached).
     */
    protected void setReactState(Map<String, Object> state) {
        _reactState = state;
        if (isAttached()) {
            addUpdate(new ReactUpdate(getID(), state));
        }
    }

    /** Current state for the React component. */
    public Map<String, Object> getReactState() {
        return _reactState;
    }

    /** The React module path. */
    public String getReactModule() {
        return _reactModule;
    }

    @Override
    protected void internalWrite(DisplayContext context, TagWriter out)
            throws IOException {
        // Render mount point
        out.beginBeginTag(DIV);
        writeControlAttributes(context, out);
        out.writeAttribute("data-react-module", _reactModule);
        out.endBeginTag();
        out.endTag(DIV);

        // Bootstrap React component with initial state
        HTMLUtil.beginScriptAfterRendering(out);
        out.append("TLReact.mount(");
        out.writeJsString(getID());
        out.append(", ");
        out.writeJsString(_reactModule);
        out.append(", ");
        writeJsonLiteral(out, _reactState);
        out.append(");");
        HTMLUtil.endScriptAfterRendering(out);
    }

    @Override
    protected void internalRevalidate(DisplayContext context,
            UpdateQueue actions) {
        // If there are queued ReactUpdates, they are already in the queue.
        // Nothing extra needed -- AbstractControl handles the update queue.
    }

    @Override
    protected void detachInvalidated() {
        super.detachInvalidated();
        // React unmount is handled client-side when the DOM element is removed.
    }
}
```

### 4.2 `ReactFormFieldControl` -- FormField-Bound Variant

Extends `AbstractFormFieldControl` for React controls that are bound to a `FormField`,
participating in the form validation and value lifecycle.

```java
/**
 * A {@link ReactControl} variant bound to a {@link FormField}.
 *
 * <p>
 * Listens to value, disabled, immutable, mandatory, and error state changes
 * on the field and pushes them to the React component as JSON state updates.
 * </p>
 *
 * <p>
 * This is the React equivalent of {@link TextInputControl},
 * {@link SelectControl}, etc. -- it can be used as a drop-in replacement
 * for any form field control.
 * </p>
 */
public class ReactFormFieldControl extends AbstractFormFieldControl {

    private final String _reactModule;

    public ReactFormFieldControl(FormField model, String reactModule) {
        this(model, reactModule, COMMANDS);
    }

    protected ReactFormFieldControl(FormField model, String reactModule,
            Map<String, ControlCommand> commands) {
        super(model, commands);
        _reactModule = reactModule;
    }

    // --- Rendering ---

    @Override
    protected void writeEditable(DisplayContext context, TagWriter out)
            throws IOException {
        writeReactMount(context, out, /* editable */ true);
    }

    @Override
    protected void writeImmutable(DisplayContext context, TagWriter out)
            throws IOException {
        writeReactMount(context, out, /* editable */ false);
    }

    private void writeReactMount(DisplayContext context, TagWriter out,
            boolean editable) throws IOException {
        out.beginBeginTag(DIV);
        writeControlAttributes(context, out);
        out.writeAttribute("data-react-module", _reactModule);
        out.endBeginTag();
        out.endTag(DIV);

        HTMLUtil.beginScriptAfterRendering(out);
        out.append("TLReact.mountField(");
        out.writeJsString(getID());
        out.append(", ");
        out.writeJsString(_reactModule);
        out.append(", ");
        writeJsonLiteral(out, buildFieldState(editable));
        out.append(");");
        HTMLUtil.endScriptAfterRendering(out);
    }

    // --- State projection ---

    /**
     * Build the JSON state object representing the current field state.
     * Subclasses can override to add custom properties.
     */
    protected Map<String, Object> buildFieldState(boolean editable) {
        FormField field = getFieldModel();
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("value", field.getRawValue());
        state.put("editable", editable);
        state.put("disabled", field.isDisabled());
        state.put("mandatory", field.isMandatory());
        state.put("hasError", field.hasError());
        if (field.hasError()) {
            state.put("errorMessage", field.getError());
        }
        state.put("label", getLabel(field));
        state.put("tooltip", getTooltip(field));
        return state;
    }

    // --- Model change handling ---

    @Override
    protected void internalHandleValueChanged(FormField field,
            Object oldValue, Object newValue) {
        pushStateUpdate();
    }

    /**
     * Push the full field state to the React component.
     * Called after any relevant model change.
     */
    protected void pushStateUpdate() {
        boolean editable = !getFieldModel().isImmutable();
        addUpdate(new ReactUpdate(getID(), buildFieldState(editable)));
    }

    @Override
    public Bubble handleDisabledChanged(FormMember sender,
            Boolean oldValue, Boolean newValue) {
        pushStateUpdate();
        return super.handleDisabledChanged(sender, oldValue, newValue);
    }

    @Override
    public Bubble handleMandatoryChanged(FormField sender,
            Boolean oldValue, Boolean newValue) {
        pushStateUpdate();
        return super.handleMandatoryChanged(sender, oldValue, newValue);
    }

    @Override
    public Bubble handleImmutableChanged(FormMember sender,
            Boolean oldValue, Boolean newValue) {
        requestRepaint();  // Switches between writeEditable/writeImmutable
        return super.handleImmutableChanged(sender, oldValue, newValue);
    }

    // --- Commands ---

    protected static final Map<String, ControlCommand> COMMANDS =
        createCommandMap(AbstractFormFieldControl.COMMANDS,
            new ControlCommand[] {
                ValueChanged.INSTANCE
            });

    /**
     * Handles value-changed events from the React component.
     * Mirrors {@link AbstractFormFieldControlBase.ValueChanged} but
     * receives JSON-encoded values.
     */
    protected static class ValueChanged extends ControlCommand {
        public static final ValueChanged INSTANCE = new ValueChanged();

        protected ValueChanged() {
            super("reactValueChanged");
        }

        @Override
        protected HandlerResult execute(DisplayContext context,
                Control control, Map<String, Object> arguments) {
            ReactFormFieldControl reactControl =
                (ReactFormFieldControl) control;
            Object newValue = arguments.get("value");
            FormField field = reactControl.getFieldModel();
            try {
                FormFieldInternals.updateFieldNoClientUpdate(field, newValue);
            } catch (VetoException ex) {
                ex.process(reactControl.getWindowScope());
            }
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
        public ResKey getI18NKey() {
            return I18NConstants.REACT_VALUE_CHANGED;
        }
    }
}
```

### 4.3 `ReactUpdate` -- JSON State ClientAction

A new `ClientAction` subtype that delivers JSON data to a React control on the client,
instead of patching the DOM.

```java
/**
 * A {@link ClientAction} that delivers a JSON state update to a mounted
 * React control.
 *
 * <p>
 * On the client, this translates to a call to
 * {@code TLReact.updateState(controlId, jsonState)}, which triggers a
 * React re-render with the new props.
 * </p>
 */
public class ReactUpdate extends ClientAction {

    private final String _controlId;
    private final Map<String, Object> _state;

    public ReactUpdate(String controlId, Map<String, Object> state) {
        _controlId = controlId;
        _state = state;
    }

    @Override
    protected String getXSIType() {
        return "reactUpdate";
    }

    @Override
    protected void writeChildrenAsXML(DisplayContext context,
            TagWriter writer) throws IOException {
        writer.beginBeginTag("controlId");
        writer.endBeginTag();
        writer.writeText(_controlId);
        writer.endTag("controlId");

        writer.beginBeginTag("state");
        writer.endBeginTag();
        // Embed JSON as CDATA within the XML SOAP response
        writer.writeCDATAContent(JsonUtilities.toJsonString(_state));
        writer.endTag("state");
    }
}
```

On the client side, the existing `simpleajax.js` action processor is extended to
recognize the `reactUpdate` XSI type and delegate to `TLReact.updateState()`.

---

## 5. The React Servlet (`ReactServlet`)

### 5.1 Purpose

The `ReactServlet` provides a **JSON-based alternative transport** for
`dispatchControlCommand`. While existing controls send commands through the AJAXServlet
using XML/SOAP encoding, React controls send commands through the ReactServlet using JSON.
The result is the same: a `ControlCommand.execute()` call on the server.

The servlet also supports a **state fetch** endpoint, so a React control can request its
current server-side state on demand (e.g., after mount, or to re-sync after an error).

### 5.2 Registration

```xml
<!-- web-fragment.xml -->
<web-fragment>
    <servlet>
        <servlet-name>ReactServlet</servlet-name>
        <servlet-class>com.top_logic.layout.react.ReactServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>ReactServlet</servlet-name>
        <url-pattern>/react-api/*</url-pattern>
    </servlet-mapping>
</web-fragment>
```

### 5.3 URL Scheme

```
POST /react-api/command     Execute a ControlCommand on a ReactControl
POST /react-api/state       Fetch the current state of a ReactControl
```

The target control is identified by its control ID (the same ID used in
`dispatchControlCommand` today).

### 5.4 Protocol

**Content-Type**: `application/json; charset=utf-8` (both request and response)

#### Command Request

```json
{
    "controlId": "ctrl-1234",
    "command": "reactValueChanged",
    "arguments": {
        "value": "new text value"
    }
}
```

#### Command Response

```json
{
    "success": true,
    "actions": [
        {
            "type": "reactUpdate",
            "controlId": "ctrl-1234",
            "state": {
                "value": "new text value",
                "hasError": false,
                "disabled": false
            }
        }
    ]
}
```

The `actions` array contains any `ClientAction`s that were generated during command
processing -- but serialized as JSON instead of XML. This allows the React bridge to
process server responses immediately without going through the SOAP action pipeline.

#### State Request

```json
{
    "controlId": "ctrl-1234"
}
```

#### State Response

```json
{
    "success": true,
    "state": {
        "value": "current value",
        "editable": true,
        "disabled": false,
        "mandatory": true,
        "hasError": false,
        "label": "Name",
        "tooltip": "Enter the item name"
    }
}
```

#### Error Response

```json
{
    "success": false,
    "error": {
        "code": "UNAUTHORIZED | NOT_FOUND | VALIDATION | INTERNAL",
        "message": "Human-readable error description"
    }
}
```

### 5.5 Request Lifecycle

```
1. Parse JSON request body
2. Extract HTTP session (same session cookie as main TopLogic app)
3. Validate session is active
4. Resolve the ControlScope (via FrameScope from the session's MainLayout)
5. Look up the target control by controlId in the FrameScope's CommandListener registry
6. Verify the control is a ReactControl (reject otherwise)
7. Route:
   - "command" -> control.executeCommand(context, commandName, arguments)
   - "state"   -> control.getReactState() or buildFieldState()
8. Collect any resulting ClientActions
9. Serialize response as JSON
10. Return with appropriate HTTP status
```

### 5.6 Session and Security

The `ReactServlet` participates in the same HTTP session as the main application. Since
the React control is rendered inside a TopLogic page, the browser already has the session
cookie. No separate authentication is needed.

Security is enforced at two levels:

- **Session**: The servlet rejects requests without a valid session (HTTP 401).
- **Control scope**: The control is only accessible if the hosting LayoutComponent is
  visible and the user has the appropriate `BoundCommandGroup` access. This is the same
  security model as `dispatchControlCommand` via the AJAXServlet.

### 5.7 Concurrency

- **State requests** are read-only and can execute concurrently.
- **Command requests** acquire the existing `RequestLock` on the session (same as
  AJAXServlet), ensuring sequential processing with other commands from the same session.
  This prevents race conditions between React commands and traditional AJAX commands
  operating on the same component tree.

### 5.8 Relationship to AJAXServlet

The `ReactServlet` does **not** replace the AJAXServlet. Both coexist:

- Traditional controls continue to use the AJAXServlet with XML/SOAP.
- React controls use the ReactServlet with JSON.
- Both dispatch to the same `ControlCommand` infrastructure on the server.
- During the revalidation cycle, a `ReactControl` can generate `ReactUpdate` actions
  alongside traditional `ContentReplacement` actions from other controls. The
  `simpleajax.js` client dispatches each action type to the appropriate handler.

---

## 6. Client-Side Bridge (`TLReact`)

### 6.1 Overview

`TLReact` is a small JavaScript library (written in TypeScript, bundled by Vite) that:

- Mounts/unmounts React component trees into TopLogic-rendered DOM elements.
- Provides React hooks for fetching state and dispatching commands via the ReactServlet.
- Receives `reactUpdate` actions from the AJAX revalidation pipeline and forwards them as
  React prop updates.

### 6.2 Bridge API

```typescript
// --- Context ---

interface TLControlContext {
    /** The TopLogic control ID. */
    controlId: string;

    /** Base URL for the ReactServlet (e.g., "/app/react-api"). */
    apiBase: string;

    /** Current state from the server. Updated on revalidation. */
    state: Record<string, unknown>;
}

// --- Mount/Unmount (called from server-rendered inline scripts) ---

/**
 * Mount a React component into the control's DOM element.
 * Called from the inline script generated by ReactControl.internalWrite().
 */
function mount(
    controlId: string,
    reactModule: string,
    initialState: Record<string, unknown>
): void;

/**
 * Mount a form-field-bound React component.
 * Same as mount() but provides form field context (value, validation, etc.).
 */
function mountField(
    controlId: string,
    reactModule: string,
    initialState: Record<string, unknown>
): void;

/**
 * Unmount a React component and clean up.
 * Called when the control is detached or the DOM element is removed.
 */
function unmount(controlId: string): void;

/**
 * Update the state of a mounted React control.
 * Called when a ReactUpdate action arrives via the AJAX revalidation pipeline.
 */
function updateState(
    controlId: string,
    newState: Record<string, unknown>
): void;

// --- React Hooks (used inside React components) ---

/**
 * Access the current server-provided state.
 * Re-renders when the server pushes new state via ReactUpdate.
 */
function useTLState<T = Record<string, unknown>>(): T;

/**
 * Execute a ControlCommand on the server.
 * Returns a promise with the command response.
 */
function useTLCommand(): {
    execute: (command: string, args?: Record<string, unknown>) => Promise<unknown>;
    loading: boolean;
    error: string | null;
};

/**
 * Convenience hook for form-field controls: read/write the field value.
 */
function useTLFieldValue<T>(): {
    value: T;
    setValue: (newValue: T) => void;
    editable: boolean;
    disabled: boolean;
    mandatory: boolean;
    hasError: boolean;
    errorMessage: string | null;
};
```

### 6.3 Mounting and Lifecycle

**Mount** (triggered by the inline script after server rendering):

1. `TLReact.mount(controlId, reactModule, initialState)` is called.
2. The bridge looks up the DOM element by `controlId`.
3. It dynamically imports the React module: `import("/react/" + reactModule + ".js")`.
4. It wraps the React component in a `<TLControlContext.Provider>` with the control ID,
   API base URL, and initial state.
5. It calls `ReactDOM.createRoot(element).render(...)`.
6. The bridge stores a reference to the React root keyed by `controlId`.

**Update** (triggered by `ReactUpdate` client action during revalidation):

1. The AJAX action processor calls `TLReact.updateState(controlId, newState)`.
2. The bridge updates the state in the context provider, causing React to re-render.

**Unmount** (triggered when the control is detached or DOM is replaced):

1. `TLReact.unmount(controlId)` is called (or detected via MutationObserver).
2. The bridge calls `root.unmount()` and cleans up the stored reference.

**Repaint** (triggered by `requestRepaint()` on the server):

1. A standard `ControlRepaint` or `ContentReplacement` action replaces the mount `<div>`.
2. The AJAX action processor detects the old React root is gone (via MutationObserver or
   explicit check) and unmounts it.
3. The new inline script calls `TLReact.mount()` again with fresh initial state.

### 6.4 Integration with `simpleajax.js`

The existing `processActions()` function in `simpleajax.js` is extended with one
additional action type handler:

```javascript
// In simpleajax.js processActions():
case "reactUpdate":
    var controlId = getChildText(action, "controlId");
    var stateJson = getCDATAContent(action, "state");
    TLReact.updateState(controlId, JSON.parse(stateJson));
    break;
```

This is a minimal, non-invasive addition to the existing AJAX framework.

---

## 7. Usage Patterns

### 7.1 Replacing a Form Field Control

A `ReactFormFieldControl` can be used as a drop-in replacement for any existing form
field control. The `ControlProvider` mechanism determines which control renders a field.

**Java -- Custom ControlProvider:**

```java
public class ReactTextInputProvider implements ControlProvider {

    @Override
    public Control createControl(Object model, String style) {
        FormField field = (FormField) model;
        return new ReactFormFieldControl(field, "controls/TLTextInput");
    }
}
```

**React -- TLTextInput.tsx:**

```tsx
import { useTLFieldValue } from '../bridge/tl-react-bridge';

export default function TLTextInput() {
    const { value, setValue, editable, disabled, mandatory,
            hasError, errorMessage } = useTLFieldValue<string>();

    return (
        <div className={`tl-react-field ${hasError ? 'has-error' : ''}`}>
            <input
                type="text"
                value={value ?? ''}
                onChange={e => setValue(e.target.value)}
                disabled={disabled || !editable}
                required={mandatory}
            />
            {hasError && <span className="error">{errorMessage}</span>}
        </div>
    );
}
```

### 7.2 Custom Visualization Control

A `ReactControl` (not form-field-bound) can display any model object.

**Java -- Chart control in a LayoutComponent:**

```java
public class MyChartControlProvider implements LayoutControlProvider {

    @Override
    public LayoutControl createLayoutControl(Strategy strategy,
            LayoutComponent component) {
        Object model = component.getModel();
        ReactControl chart = new ReactControl(model, "controls/TLChart",
            ReactControl.COMMANDS);
        chart.setReactState(buildChartData(model));
        return chart;
    }

    private Map<String, Object> buildChartData(Object model) {
        // Transform model into chart-compatible JSON structure
        return Map.of(
            "labels", List.of("Jan", "Feb", "Mar"),
            "series", List.of(
                Map.of("name", "Revenue", "data", List.of(100, 150, 130))
            )
        );
    }
}
```

**React -- TLChart.tsx:**

```tsx
import { useTLState } from '../bridge/tl-react-bridge';
import { BarChart, Bar, XAxis, YAxis } from 'recharts';

interface ChartData {
    labels: string[];
    series: Array<{ name: string; data: number[] }>;
}

export default function TLChart() {
    const state = useTLState<ChartData>();

    const chartData = state.labels.map((label, i) => ({
        name: label,
        ...Object.fromEntries(
            state.series.map(s => [s.name, s.data[i]])
        )
    }));

    return (
        <BarChart width={600} height={300} data={chartData}>
            <XAxis dataKey="name" />
            <YAxis />
            {state.series.map(s => (
                <Bar key={s.name} dataKey={s.name} fill="#8884d8" />
            ))}
        </BarChart>
    );
}
```

### 7.3 Interactive Control with Custom Commands

**Java -- Control with save/delete commands:**

```java
public class ItemEditorControl extends ReactControl {

    protected static final Map<String, ControlCommand> COMMANDS =
        createCommandMap(ReactControl.COMMANDS, new ControlCommand[] {
            SaveCommand.INSTANCE,
            DeleteCommand.INSTANCE
        });

    public ItemEditorControl(TLObject item) {
        super(item, "controls/ItemEditor", COMMANDS);
        setReactState(buildState(item));
    }

    private Map<String, Object> buildState(TLObject item) {
        return Map.of(
            "id", item.tId().toString(),
            "name", item.tValueByName("name"),
            "status", item.tValueByName("status")
        );
    }

    protected static class SaveCommand extends ControlCommand {
        public static final SaveCommand INSTANCE = new SaveCommand();

        protected SaveCommand() { super("save"); }

        @Override
        protected HandlerResult execute(DisplayContext context,
                Control control, Map<String, Object> arguments) {
            ItemEditorControl editor = (ItemEditorControl) control;
            TLObject item = (TLObject) editor.getModel();
            try (Transaction tx = PersistencyLayer.getKnowledgeBase()
                    .beginTransaction()) {
                item.tUpdateByName("name", arguments.get("name"));
                tx.commit();
            }
            editor.setReactState(editor.buildState(item));
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
        public ResKey getI18NKey() {
            return I18NConstants.SAVE_ITEM;
        }
    }
}
```

**React -- ItemEditor.tsx:**

```tsx
import { useTLState, useTLCommand } from '../bridge/tl-react-bridge';

interface ItemState { id: string; name: string; status: string; }

export default function ItemEditor() {
    const state = useTLState<ItemState>();
    const { execute, loading } = useTLCommand();
    const [name, setName] = useState(state.name);

    // Sync local state when server pushes update
    useEffect(() => setName(state.name), [state.name]);

    return (
        <form onSubmit={async e => {
            e.preventDefault();
            await execute('save', { name });
        }}>
            <input value={name} onChange={e => setName(e.target.value)} />
            <button type="submit" disabled={loading}>Save</button>
        </form>
    );
}
```

---

## 8. JSON Serialization

The `ReactServlet` and `ReactUpdate` use TopLogic's existing JSON utilities
(`com.top_logic.common.json.gstream.JsonWriter`) for serialization.

| Java Type | JSON Type |
|---|---|
| `String` | string |
| `Number` (int, long, double) | number |
| `Boolean` | boolean |
| `null` | null |
| `Map<String, ?>` | object |
| `List<?>` / `Collection<?>` | array |
| `ConfigurationItem` with `@JsonBinding` | per binding |

For knowledge base objects (`TLObject`), controls are responsible for projecting them into
JSON-serializable maps via `buildState()` / `buildFieldState()`. There is no automatic
deep serialization of persistent objects -- this is intentional, as it forces explicit
control over what data leaves the server.

---

## 9. Error Handling

### Server-Side

- `ControlCommand` implementations can throw `TopLogicException` for user-facing errors.
  The servlet serializes these as:

```json
{
    "success": false,
    "error": { "code": "VALIDATION", "message": "Localized error text" }
}
```

- Unexpected exceptions result in `"code": "INTERNAL"` (details logged server-side only).
- For `ReactFormFieldControl`, field validation errors are part of the state (`hasError`,
  `errorMessage`) and flow through the normal revalidation cycle.

### Client-Side

- The `useTLCommand` hook exposes `error` state that React components can render.
- HTTP 401 triggers a redirect to the login page (handled by the bridge).
- Network errors are retried once, then surfaced to the component.

---

## 10. Lifecycle Summary

```
 Server                              Client
 ------                              ------

 1. LayoutComponent renders
    -> ControlProvider creates
       ReactControl (or ReactFormFieldControl)
    -> control.write() produces:       <div id="ctrl-123"
                                            data-react-module="..."/>
                                       <script>
                                         TLReact.mount("ctrl-123",
                                           "controls/TLChart",
                                           {initial state JSON});
                                       </script>

                                       2. TLReact.mount():
                                          - import("/react/controls/TLChart.js")
                                          - ReactDOM.createRoot(div)
                                          - render(<TLChart />) with state

                                       3. User interacts (types, clicks, etc.)
                                          React calls useTLCommand().execute()

 4. ReactServlet receives POST        <-- POST /react-api/command
    -> resolves control by ID              {"controlId":"ctrl-123",
    -> dispatches ControlCommand            "command":"save",
    -> command modifies model               "arguments":{...}}
    -> control.setReactState(newState)
    -> returns JSON response           --> {"success":true, "actions":[...]}

                                       5. Bridge applies response:
                                          TLReact.updateState("ctrl-123", newState)
                                          -> React re-renders with new props

 --- OR, via standard revalidation ---

 6. Another component changes model
    -> ReactControl listener fires
    -> control.setReactState(newState)
    -> addUpdate(new ReactUpdate(...))

 7. AJAXServlet revalidation cycle     --> <ajax:action xsi:type="reactUpdate">
    includes ReactUpdate action             <controlId>ctrl-123</controlId>
                                            <state><![CDATA[{...}]]></state>
                                           </ajax:action>

                                       8. simpleajax.js processActions()
                                          -> TLReact.updateState("ctrl-123", ...)
                                          -> React re-renders
```

---

## 11. Future Extensions

Out of scope for the initial implementation, but considered in the architecture:

- **Server-Sent Events (SSE)** for real-time state push without polling.
- **TypeScript type generation** from TopLogic model definitions (`*.model.xml`).
- **React form control library**: Systematic React replacements for all standard TopLogic
  form field controls (text, select, date, checkbox, etc.).
- **Hot module replacement**: Vite dev server proxied through TopLogic for instant React
  reload during development.
- **Composite React views**: A `ReactControl` that hosts a complex React SPA (multiple
  routes, local state management) while still being a single Control from TopLogic's
  perspective.

---

## 12. Open Questions

1. **ReactUpdate delivery path**: Should `ReactUpdate` go through the existing XML/SOAP
   revalidation (as specified in Section 4.3), or should the ReactServlet also serve as
   the delivery mechanism for server-initiated state pushes (via long-polling or SSE)?

2. **Control ID stability**: Control IDs are generated during `attach()` and change on
   repaint. Should React controls use a more stable identifier (e.g., derived from the
   component name + field name) to allow the client to maintain React state across
   repaints?

3. **Transaction scope**: Should each command execute in its own transaction, or should the
   ReactServlet support batching multiple commands in a single transaction?

4. **I18N for React views**: Options:
   - Server resolves all labels and includes them in the state (simplest for now).
   - A separate endpoint serves the resource bundle for the current locale.
   - React views manage their own i18n independently.

5. **Theme integration**: Should React controls receive TopLogic theme CSS
   variables/design tokens, enabling them to visually blend with the rest of the UI?

6. **Granularity of state updates**: Should `pushStateUpdate()` always send the full
   field state, or should there be a mechanism for fine-grained partial updates
   (e.g., only `{"hasError": true, "errorMessage": "..."}` when validation changes)?
