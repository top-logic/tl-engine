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

A central part of this specification is the introduction of **Server-Sent Events (SSE)**
as a server-push mechanism for TopLogic. Today, all client updates are piggybacked on
command responses (request-response only). The SSE event stream introduced here serves
React controls initially, but is designed as a general-purpose update channel that can
deliver updates for *any* control -- React or traditional -- opening the door for future
migration of all server-to-client updates away from the XML/SOAP response piggybacking.

### Design Rationale

TopLogic separates concerns into two layers:

- **LayoutComponent** (coarse-grained): Manages persistent data access (model builders),
  navigation state (component channels), security, and page structure. A LayoutComponent
  does not directly produce HTML. Transactions are a LayoutComponent-level concern.
- **Control** (fine-grained): Handles display and user interaction. Controls render HTML,
  process user input via `ControlCommand`s, and translate between persistent objects and
  UI presentation through `LabelProvider`, `ResourceProvider`, etc. Controls do not manage
  transactions.

A React-based UI element is fundamentally a **Control** -- it renders interactive HTML and
handles user events. It should not be a LayoutComponent, because it does not own data
access, navigation, or transaction boundaries. This positioning allows React controls to
be used anywhere a traditional TopLogic Control is used today, enabling incremental
migration: a `TextInputControl` could be replaced by a React-based text input, a chart
control by a React chart, etc.

### Goals

- A `ReactControl` can be used wherever a TopLogic `Control` is used: inside form groups,
  component views, dialogs, table cell renderers, etc.
- React controls participate in the standard `Control` lifecycle: `attach()`, `detach()`,
  `requestRepaint()`, `revalidate()`.
- User interactions in React are dispatched to the server via `ControlCommand`s through
  a **JSON-based servlet** (`ReactServlet`).
- Server-to-client updates are delivered through a **dedicated SSE event stream**,
  separate from the existing XML/SOAP revalidation pipeline. Updates are
  **incremental** (partial state patches), not full state replacements.
- All labels and display text are **resolved server-side** and included in the control
  state, so React controls do not need their own I18N infrastructure.
- React controls can adopt the TopLogic theme's visual appearance through **CSS custom
  properties** exported by the theme system, while remaining compatible with third-party
  React component libraries that bring their own styling.
- React assets are built with standard tooling (Vite/npm) and packaged into the Maven
  module for deployment.

### Non-Goals (for this iteration)

- Replacing the entire TopLogic UI with React.
- Server-side rendering (SSR) of React components.
- A complete React equivalent of every existing TopLogic form control.
- Transaction management in controls (transactions are a LayoutComponent concern).

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
|  +--------+---------------------+  +------+-----------+-----------+   |
|           |                               |           |               |
|   dispatchControlCommand           POST command    SSE updates        |
|   (XML/SOAP, existing)            (JSON, new)     (JSON stream)      |
|           |                               |           ^               |
+-----------+-------------------------------+-----------+---------------+
            |                               |           |
            v                               v           |
+----------------------------------------------------------------------+
|  TopLogic Server (Servlet Container)                                  |
|                                                                       |
|  +-----------------+  +------------------+  +---------------------+   |
|  | AJAXServlet     |  | ReactServlet     |  | SSE EventServlet    |   |
|  | /ajax           |  | /react-api/*     |  | /react-api/events   |   |
|  |                 |  |                  |  |                     |   |
|  | XML/SOAP        |  | JSON commands    |  | Server-push stream  |   |
|  | DOM actions     |  | JSON responses   |  | JSON event frames   |   |
|  +---------+-------+  +--------+---------+  +----------+----------+   |
|            |                   |                       |              |
|            +-------+-----------+-----------+-----------+              |
|                    |                       |                          |
|         +----------+----------+   +--------+---------+               |
|         | AbstractControlBase |   | SSEUpdateQueue   |               |
|         | - command dispatch   |   | - per-session     |               |
|         | - model listeners   |   | - any control can  |               |
|         | - attach/detach     |   |   enqueue updates  |               |
|         +---------------------+   +------------------+               |
|                    |                                                  |
|     +--------------+--------------+                                  |
|     |                             |                                  |
|  +--+---------------+  +----------+---------+                        |
|  | TextInputControl |  | ReactControl       |                        |
|  | (HTML + XML)     |  | (mount div + JSON) |                        |
|  +------------------+  +--------------------+                        |
+----------------------------------------------------------------------+
```

### Key Principles

1. **A `ReactControl` is a Control.** It extends `AbstractControl` (or
   `AbstractFormFieldControl`), registers `ControlCommand`s, and lives within a
   `ControlScope`.

2. **Updates flow through SSE, not SOAP.** When a `ReactControl`'s state changes (from
   a model listener, a channel update, or as a side effect of another command), the update
   is enqueued on the session's `SSEUpdateQueue` and delivered to the client via the SSE
   stream. This decouples update delivery from the request-response cycle.

3. **SSE is a general-purpose mechanism.** While this spec focuses on React controls, the
   SSE infrastructure can deliver JSON-encoded updates for traditional controls too. A
   traditional control could enqueue a JSON-wrapped `ContentReplacement` or
   `PropertyUpdate` for delivery via SSE, providing server-push for the entire UI
   in the future.

4. **Controls do not manage transactions.** Transaction boundaries are established by
   `LayoutComponent`-level `CommandHandler`s. `ControlCommand`s execute within whatever
   transaction context (if any) the calling `CommandHandler` has established.

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
|   |   |       +-- ReactServlet.java              # JSON API servlet for commands
|   |   |       +-- SSEServlet.java                # Server-Sent Events endpoint
|   |   |       +-- SSEUpdateQueue.java            # Per-session SSE event queue
|   |   |       +-- SSEEvent.java                  # Event envelope for SSE delivery
|   |   |       +-- ReactControlRenderer.java      # Renders the mount <div> + bootstrap
|   |   |       +-- I18NConstants.java
|   |   |       +-- package-info.java
|   |   +-- webapp/
|   |   |   +-- WEB-INF/
|   |   |   |   +-- web-fragment.xml               # Servlet registrations
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
|   |   +-- sse-client.ts                  # SSE connection manager
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
 * Model changes are delivered to the client as incremental JSON state patches
 * via the SSE event stream.
 * </p>
 */
public class ReactControl extends AbstractControl {

    private final String _reactModule;

    /** Full state -- the union of all patches applied so far. */
    private Map<String, Object> _reactState;

    public ReactControl(Object model, String reactModule,
            Map<String, ControlCommand> commands) {
        super(model, commands);
        _reactModule = reactModule;
        _reactState = new LinkedHashMap<>();
    }

    /**
     * Replace the entire React state.
     * Enqueues a full state event on the SSE stream.
     */
    protected void setReactState(Map<String, Object> state) {
        _reactState = new LinkedHashMap<>(state);
        enqueueSSE(SSEEvent.fullState(getID(), state));
    }

    /**
     * Apply an incremental patch to the React state.
     * Only the changed keys are sent to the client via SSE.
     *
     * <p>Keys present in {@code patch} overwrite existing keys.
     * Keys mapped to {@code null} are removed from the state.</p>
     */
    protected void patchReactState(Map<String, Object> patch) {
        for (Map.Entry<String, Object> entry : patch.entrySet()) {
            if (entry.getValue() == null) {
                _reactState.remove(entry.getKey());
            } else {
                _reactState.put(entry.getKey(), entry.getValue());
            }
        }
        enqueueSSE(SSEEvent.patch(getID(), patch));
    }

    /** Full current state (for initial mount and state-fetch requests). */
    public Map<String, Object> getReactState() {
        return Collections.unmodifiableMap(_reactState);
    }

    public String getReactModule() {
        return _reactModule;
    }

    @Override
    protected void internalWrite(DisplayContext context, TagWriter out)
            throws IOException {
        out.beginBeginTag(DIV);
        writeControlAttributes(context, out);
        out.writeAttribute("data-react-module", _reactModule);
        out.endBeginTag();
        out.endTag(DIV);

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
        // Updates are delivered via SSE, not via the SOAP UpdateQueue.
        // Nothing to add to the XML action stream.
    }

    /** Enqueue an event on the session's SSE stream. */
    private void enqueueSSE(SSEEvent event) {
        if (isAttached()) {
            SSEUpdateQueue.forCurrentSession().enqueue(event);
        }
    }
}
```

### 4.2 `ReactFormFieldControl` -- FormField-Bound Variant

Extends `AbstractFormFieldControl` for React controls bound to a `FormField`. Listens
to value, disabled, immutable, mandatory, and error state changes and pushes **incremental
patches** (only the changed properties) to the React component via SSE.

```java
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
        writeReactMount(context, out, true);
    }

    @Override
    protected void writeImmutable(DisplayContext context, TagWriter out)
            throws IOException {
        writeReactMount(context, out, false);
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
        writeJsonLiteral(out, buildFullFieldState(editable));
        out.append(");");
        HTMLUtil.endScriptAfterRendering(out);
    }

    // --- Full state (for initial mount) ---

    protected Map<String, Object> buildFullFieldState(boolean editable) {
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

    // --- Incremental change handlers ---

    @Override
    protected void internalHandleValueChanged(FormField field,
            Object oldValue, Object newValue) {
        pushPatch(Map.of("value", newValue));
    }

    @Override
    public Bubble handleDisabledChanged(FormMember sender,
            Boolean oldValue, Boolean newValue) {
        pushPatch(Map.of("disabled", newValue));
        return super.handleDisabledChanged(sender, oldValue, newValue);
    }

    @Override
    public Bubble handleMandatoryChanged(FormField sender,
            Boolean oldValue, Boolean newValue) {
        pushPatch(Map.of("mandatory", newValue));
        return super.handleMandatoryChanged(sender, oldValue, newValue);
    }

    @Override
    public Bubble handleImmutableChanged(FormMember sender,
            Boolean oldValue, Boolean newValue) {
        requestRepaint();  // Switches between writeEditable/writeImmutable
        return super.handleImmutableChanged(sender, oldValue, newValue);
    }

    /**
     * Push error state update. Called when validation state changes.
     */
    protected void pushErrorState() {
        FormField field = getFieldModel();
        if (field.hasError()) {
            pushPatch(Map.of(
                "hasError", true,
                "errorMessage", field.getError()
            ));
        } else {
            pushPatch(Map.of(
                "hasError", false,
                "errorMessage", null  // null = remove key
            ));
        }
    }

    private void pushPatch(Map<String, Object> patch) {
        if (isAttached()) {
            SSEUpdateQueue.forCurrentSession()
                .enqueue(SSEEvent.patch(getID(), patch));
        }
    }

    @Override
    protected void internalRevalidate(DisplayContext context,
            UpdateQueue actions) {
        // Updates are delivered via SSE, not the SOAP UpdateQueue.
    }

    // --- Commands ---

    protected static final Map<String, ControlCommand> COMMANDS =
        createCommandMap(AbstractFormFieldControl.COMMANDS,
            new ControlCommand[] {
                ValueChanged.INSTANCE
            });

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
                FormFieldInternals.updateFieldNoClientUpdate(
                    field, newValue);
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

---

## 5. Server-Sent Events (SSE) -- The Update Delivery Channel

### 5.1 Motivation

Today, TopLogic delivers all client updates by piggybacking them on AJAX command
responses. A model change caused by one user action is only delivered to the client when
*the next* user action triggers a request. This creates subtle staleness: a channel update
from navigation may not be visible until the user interacts again.

SSE solves this by providing a persistent, server-initiated push channel. When any control
enqueues an update, it is delivered immediately -- no user interaction required.

While this spec focuses on React controls, the SSE infrastructure is intentionally
**control-type-agnostic**. Any control can enqueue a `SSEEvent`. This opens a migration
path for traditional controls: a `PropertyUpdate` or `ContentReplacement` could be
JSON-wrapped and pushed via SSE instead of waiting for the next SOAP response.

### 5.2 `SSEServlet`

```xml
<!-- web-fragment.xml (alongside ReactServlet) -->
<servlet>
    <servlet-name>SSEServlet</servlet-name>
    <servlet-class>com.top_logic.layout.react.SSEServlet</servlet-class>
    <async-supported>true</async-supported>
</servlet>
<servlet-mapping>
    <servlet-name>SSEServlet</servlet-name>
    <url-pattern>/react-api/events</url-pattern>
</servlet-mapping>
```

The SSE servlet uses the Servlet 3.1 async API (`AsyncContext`) to hold the HTTP
connection open and stream events.

```java
public class SSEServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 2. Set SSE headers
        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");  // Disable proxy buffering

        // 3. Start async context
        AsyncContext async = req.startAsync();
        async.setTimeout(0);  // No timeout (reconnect handled by client)

        // 4. Register this connection with the session's SSEUpdateQueue
        SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
        queue.addConnection(async);

        // 5. Send initial keepalive
        resp.getWriter().write(": connected\n\n");
        resp.getWriter().flush();
    }
}
```

### 5.3 `SSEUpdateQueue`

A per-session queue that buffers events and writes them to all active SSE connections
for that session.

```java
public class SSEUpdateQueue {

    /** Retrieve the queue for the current thread's session. */
    public static SSEUpdateQueue forCurrentSession() { ... }

    /** Retrieve the queue for a specific HTTP session. */
    public static SSEUpdateQueue forSession(HttpSession session) { ... }

    /**
     * Enqueue an event for delivery to the client.
     * Thread-safe: can be called from any thread
     * (command processing, model listeners, etc.).
     */
    public void enqueue(SSEEvent event) {
        // Add to concurrent queue, notify writer thread / flush
    }

    /** Register a new SSE connection (from SSEServlet). */
    public void addConnection(AsyncContext async) { ... }

    /** Remove a closed connection. */
    public void removeConnection(AsyncContext async) { ... }
}
```

### 5.4 `SSEEvent`

The event envelope. Serialized as an SSE `data:` frame containing JSON.

```java
public class SSEEvent {

    /** Full state replacement for a control. */
    public static SSEEvent fullState(String controlId,
            Map<String, Object> state) { ... }

    /** Incremental patch for a control. */
    public static SSEEvent patch(String controlId,
            Map<String, Object> patch) { ... }

    /**
     * Serialize as SSE text frame.
     *
     * Output format:
     *   event: update
     *   data: {"controlId":"ctrl-123","type":"patch","patch":{"value":"x"}}
     *
     *   (blank line terminates event)
     */
    public void writeTo(Writer out) throws IOException { ... }
}
```

### 5.5 SSE Wire Format

Each SSE frame is a JSON object on a `data:` line:

**Incremental patch** (most common -- only changed keys):

```
event: update
data: {"controlId":"ctrl-123","type":"patch","patch":{"value":"new text","hasError":false}}

```

**Full state replacement** (on mount, reconnect, or explicit reset):

```
event: update
data: {"controlId":"ctrl-123","type":"state","state":{"value":"x","editable":true,"disabled":false,"mandatory":true,"hasError":false,"label":"Name","tooltip":"Enter name"}}

```

**Keepalive** (prevents connection timeout, sent periodically):

```
: keepalive

```

### 5.6 Reconnection

The `EventSource` API in the browser handles reconnection automatically. On reconnect:

1. The client sends a `Last-Event-ID` header (if the server set `id:` on events).
2. The server can replay missed events from a short buffer, or:
3. The client re-fetches full state for all mounted controls via
   `POST /react-api/state`.

For the initial implementation, option (3) is sufficient -- no server-side event buffering
is needed. The client simply re-mounts all active React controls on reconnect.

### 5.7 SSE for Non-React Controls (Future Path)

A traditional control (e.g., `TextInputControl`) could also enqueue SSE events:

```java
// Hypothetical future use in a traditional control:
SSEUpdateQueue.forCurrentSession().enqueue(
    SSEEvent.domAction("ctrl-456", new PropertyUpdate(
        inputId, "value", newDisplayValue)));
```

The client-side SSE handler would then apply the DOM action directly, without waiting for
the next AJAX response. This is not part of the initial implementation but the
architecture explicitly supports it.

---

## 6. The React Servlet (`ReactServlet`)

### 6.1 Purpose

The `ReactServlet` provides a **JSON-based command endpoint** for React controls. It
handles two operations:

- **Command dispatch**: Execute a `ControlCommand` on a `ReactControl`, return a JSON
  response.
- **State fetch**: Retrieve the current full state of a `ReactControl` (used on mount and
  reconnect).

Note: **Server-to-client updates are NOT part of command responses.** They flow
exclusively through the SSE channel. The command response only contains the direct result
of the command (success/error), not side-effect updates. Side effects (model changes
triggering other controls to update) are delivered asynchronously via SSE.

### 6.2 Registration

```xml
<!-- web-fragment.xml -->
<servlet>
    <servlet-name>ReactServlet</servlet-name>
    <servlet-class>com.top_logic.layout.react.ReactServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>ReactServlet</servlet-name>
    <url-pattern>/react-api/*</url-pattern>
</servlet-mapping>
```

### 6.3 URL Scheme

```
POST /react-api/command     Execute a ControlCommand on a ReactControl
POST /react-api/state       Fetch the current full state of a ReactControl
GET  /react-api/events      SSE event stream (handled by SSEServlet)
```

### 6.4 Protocol

**Content-Type**: `application/json; charset=utf-8`

#### Command Request/Response

```json
// Request
{
    "controlId": "ctrl-123",
    "command": "reactValueChanged",
    "arguments": {
        "value": "new text value"
    }
}

// Response (success)
{
    "success": true
}

// Response (error)
{
    "success": false,
    "error": {
        "code": "VALIDATION",
        "message": "Localized error text from ResKey"
    }
}
```

The response contains only the command result. Any state updates caused by the command
(including updates to the control itself and to other controls affected by side effects)
are delivered via the SSE stream.

#### State Request/Response

```json
// Request
{
    "controlId": "ctrl-123"
}

// Response
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

### 6.5 Request Lifecycle

```
1. Parse JSON request body
2. Extract HTTP session (same session cookie as main TopLogic app)
3. Validate session is active, user is authenticated
4. Resolve the FrameScope from the session's MainLayout
5. Look up the target control by controlId in the FrameScope's CommandListener registry
6. Verify the control is a ReactControl (reject otherwise)
7. Route:
   - "command" -> control.executeCommand(context, commandName, arguments)
                  (any state updates are enqueued on SSEUpdateQueue as side effects)
   - "state"   -> control.getReactState()
8. Return JSON response (success/error only for commands, state for fetches)
```

### 6.6 Session and Security

The `ReactServlet` participates in the same HTTP session as the main application. Since
the React control is rendered inside a TopLogic page, the browser already has the session
cookie. No separate authentication is needed.

Security is enforced at two levels:

- **Session**: The servlet rejects requests without a valid session (HTTP 401).
- **Control scope**: The control is only accessible if the hosting LayoutComponent is
  visible and the user has the appropriate `BoundCommandGroup` access.

### 6.7 Concurrency

- **State requests** are read-only and can execute concurrently.
- **Command requests** acquire the existing `RequestLock` on the session (same as the
  AJAXServlet), ensuring sequential processing with other commands from the same session.
  This prevents race conditions between React commands and traditional AJAX commands
  operating on the same component tree.

---

## 7. Client-Side Bridge (`TLReact`)

### 7.1 Overview

`TLReact` is a TypeScript library (bundled by Vite) that:

- Mounts/unmounts React component trees into TopLogic-rendered DOM elements.
- Manages a single SSE connection per page, dispatching incoming events to the
  appropriate React control instances.
- Provides React hooks for reading state and dispatching commands.

### 7.2 SSE Connection Manager

The bridge establishes a single `EventSource` connection on page load:

```typescript
class SSEClient {
    private _source: EventSource;
    private _listeners: Map<string, (event: SSEUpdateEvent) => void>;

    connect(url: string): void {
        this._source = new EventSource(url);

        this._source.addEventListener('update', (e: MessageEvent) => {
            const event: SSEUpdateEvent = JSON.parse(e.data);
            const listener = this._listeners.get(event.controlId);
            if (listener) {
                listener(event);
            }
        });

        this._source.onerror = () => {
            // EventSource reconnects automatically.
            // On reconnect, re-fetch state for all mounted controls.
            this.refetchAll();
        };
    }

    subscribe(controlId: string,
              callback: (event: SSEUpdateEvent) => void): void {
        this._listeners.set(controlId, callback);
    }

    unsubscribe(controlId: string): void {
        this._listeners.delete(controlId);
    }
}
```

### 7.3 Bridge API

```typescript
// --- Types ---

interface SSEUpdateEvent {
    controlId: string;
    type: 'patch' | 'state';
    patch?: Record<string, unknown>;   // Present when type = 'patch'
    state?: Record<string, unknown>;   // Present when type = 'state'
}

// --- Mount/Unmount ---

/**
 * Mount a React component into the control's DOM element.
 * Called from server-rendered inline script.
 */
function mount(
    controlId: string,
    reactModule: string,
    initialState: Record<string, unknown>
): void;

/**
 * Mount a form-field-bound React component.
 */
function mountField(
    controlId: string,
    reactModule: string,
    initialState: Record<string, unknown>
): void;

/**
 * Unmount a React component and clean up.
 */
function unmount(controlId: string): void;

// --- React Hooks ---

/**
 * Access the current server-provided state.
 * Re-renders when SSE delivers a patch or full state update.
 */
function useTLState<T = Record<string, unknown>>(): T;

/**
 * Execute a ControlCommand on the server via ReactServlet.
 */
function useTLCommand(): {
    execute: (command: string, args?: Record<string, unknown>) => Promise<void>;
    loading: boolean;
    error: string | null;
};

/**
 * Convenience hook for form-field controls.
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

### 7.4 State Management with Incremental Patches

Each mounted React control maintains a state store that applies patches incrementally:

```typescript
class ControlStateStore {
    private _state: Record<string, unknown>;
    private _subscribers: Set<() => void>;  // React setState triggers

    constructor(initialState: Record<string, unknown>) {
        this._state = { ...initialState };
        this._subscribers = new Set();
    }

    /** Apply an incremental patch (merge keys, remove null-valued keys). */
    applyPatch(patch: Record<string, unknown>): void {
        for (const [key, value] of Object.entries(patch)) {
            if (value === null) {
                delete this._state[key];
            } else {
                this._state[key] = value;
            }
        }
        this.notify();
    }

    /** Replace the entire state (on reconnect or full state event). */
    replaceState(state: Record<string, unknown>): void {
        this._state = { ...state };
        this.notify();
    }

    getState(): Record<string, unknown> {
        return this._state;
    }

    subscribe(callback: () => void): () => void {
        this._subscribers.add(callback);
        return () => this._subscribers.delete(callback);
    }

    private notify(): void {
        for (const cb of this._subscribers) cb();
    }
}
```

The `useTLState()` hook subscribes to the store via `useSyncExternalStore` (React 18+),
ensuring efficient re-renders only when state actually changes.

### 7.5 Mounting Lifecycle

```
 Server renders <div id="ctrl-123" .../>
 Server renders <script>TLReact.mount("ctrl-123", "controls/TLChart", {...})</script>
     |
     v
 TLReact.mount():
   1. Create ControlStateStore with initialState
   2. Subscribe to SSE events for "ctrl-123"
   3. Dynamic import: import("/react/controls/TLChart.js")
   4. ReactDOM.createRoot(document.getElementById("ctrl-123"))
   5. Render <TLControlContext.Provider><TLChart /></TLControlContext.Provider>
     |
     v
 SSE event arrives: {"controlId":"ctrl-123","type":"patch","patch":{"value":"x"}}
   1. SSEClient dispatches to ControlStateStore for "ctrl-123"
   2. Store applies patch, notifies subscribers
   3. React re-renders TLChart with updated state
     |
     v
 Control detached (navigation, tab switch):
   1. Server replaces/removes the <div> via ContentReplacement
   2. MutationObserver detects removal
   3. TLReact.unmount("ctrl-123"): root.unmount(), unsubscribe from SSE, dispose store
```

---

## 8. Theme Integration

### 8.1 CSS Custom Properties

The TopLogic theme system exports its design tokens as CSS custom properties on the
`:root` element. React controls can use these for a consistent look:

```css
/* Generated by TopLogic theme system (theme.xml + buildStyles) */
:root {
    --tl-color-primary: #1a73e8;
    --tl-color-error: #d93025;
    --tl-color-bg: #ffffff;
    --tl-color-text: #202124;
    --tl-color-border: #dadce0;
    --tl-font-family: 'Roboto', sans-serif;
    --tl-font-size-base: 14px;
    --tl-spacing-sm: 4px;
    --tl-spacing-md: 8px;
    --tl-border-radius: 4px;
    /* ... */
}
```

React controls use these in their CSS:

```css
.tl-react-field input {
    font-family: var(--tl-font-family);
    font-size: var(--tl-font-size-base);
    border: 1px solid var(--tl-color-border);
    border-radius: var(--tl-border-radius);
    padding: var(--tl-spacing-sm) var(--tl-spacing-md);
}

.tl-react-field input:focus {
    border-color: var(--tl-color-primary);
}

.tl-react-field.has-error input {
    border-color: var(--tl-color-error);
}
```

### 8.2 Third-Party React Libraries

Third-party React components (e.g., Recharts, React Select, MUI) bring their own
styling. This is explicitly supported -- the theme CSS custom properties are available
but not mandatory. Developers can:

- **Use them directly** in custom CSS for hand-built React controls.
- **Map them to library-specific themes** (e.g., pass `--tl-color-primary` as MUI's
  primary color via a theme provider).
- **Ignore them** and use the library's default styling, accepting a visual mismatch.

The bridge provides a helper to read theme tokens programmatically:

```typescript
/**
 * Read TopLogic theme tokens as a JavaScript object.
 * Useful for configuring third-party component library themes.
 */
function useTLTheme(): {
    colorPrimary: string;
    colorError: string;
    colorBg: string;
    colorText: string;
    fontFamily: string;
    fontSizeBase: string;
    /* ... */
};
```

---

## 9. Usage Patterns

### 9.1 Replacing a Form Field Control

```java
// Java: ControlProvider returns a ReactFormFieldControl
public class ReactTextInputProvider implements ControlProvider {
    @Override
    public Control createControl(Object model, String style) {
        return new ReactFormFieldControl((FormField) model,
            "controls/TLTextInput");
    }
}
```

```tsx
// React: TLTextInput.tsx
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

### 9.2 Custom Visualization with Third-Party Library

```java
// Java: ControlProvider for a chart
public class ChartControlProvider implements LayoutControlProvider {
    @Override
    public LayoutControl createLayoutControl(Strategy strategy,
            LayoutComponent component) {
        ReactControl chart = new ReactControl(
            component.getModel(), "controls/TLChart", ReactControl.COMMANDS);
        chart.setReactState(buildChartData(component.getModel()));
        return chart;
    }
}
```

```tsx
// React: TLChart.tsx (using Recharts)
import { useTLState } from '../bridge/tl-react-bridge';
import { BarChart, Bar, XAxis, YAxis } from 'recharts';

export default function TLChart() {
    const { labels, series } = useTLState<ChartData>();
    // ... render chart
}
```

### 9.3 Interactive Control with Custom Commands

```java
// Java: custom ControlCommands for save/delete
public class ItemEditorControl extends ReactControl {

    protected static final Map<String, ControlCommand> COMMANDS =
        createCommandMap(ReactControl.COMMANDS, new ControlCommand[] {
            SaveCommand.INSTANCE
        });

    public ItemEditorControl(TLObject item) {
        super(item, "controls/ItemEditor", COMMANDS);
        setReactState(Map.of(
            "id", item.tId().toString(),
            "name", item.tValueByName("name"),
            "status", item.tValueByName("status")
        ));
    }

    protected static class SaveCommand extends ControlCommand {
        public static final SaveCommand INSTANCE = new SaveCommand();
        protected SaveCommand() { super("save"); }

        @Override
        protected HandlerResult execute(DisplayContext context,
                Control control, Map<String, Object> arguments) {
            ItemEditorControl editor = (ItemEditorControl) control;
            TLObject item = (TLObject) editor.getModel();
            // Note: transaction is managed by the LayoutComponent's
            // CommandHandler, not here.
            item.tUpdateByName("name", arguments.get("name"));
            editor.patchReactState(Map.of(
                "name", arguments.get("name")
            ));
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
        public ResKey getI18NKey() {
            return I18NConstants.SAVE_ITEM;
        }
    }
}
```

```tsx
// React: ItemEditor.tsx
import { useState, useEffect } from 'react';
import { useTLState, useTLCommand } from '../bridge/tl-react-bridge';

interface ItemState { id: string; name: string; status: string; }

export default function ItemEditor() {
    const state = useTLState<ItemState>();
    const { execute, loading } = useTLCommand();
    const [name, setName] = useState(state.name);

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

## 10. JSON Serialization

The `ReactServlet` and `SSEEvent` use TopLogic's JSON utilities
(`com.top_logic.common.json.gstream.JsonWriter`) for serialization.

| Java Type | JSON Type |
|---|---|
| `String` | string |
| `Number` (int, long, double) | number |
| `Boolean` | boolean |
| `null` | null (or key removal in patches) |
| `Map<String, ?>` | object |
| `List<?>` / `Collection<?>` | array |
| `ConfigurationItem` with `@JsonBinding` | per binding |

Controls project their model into JSON-serializable maps via `buildState()` /
`buildFullFieldState()`. There is no automatic serialization of persistent objects --
controls explicitly decide what data to expose.

---

## 11. Error Handling

### Server-Side

- `ControlCommand` implementations can throw `TopLogicException` for user-facing errors.
  The `ReactServlet` catches these and returns:

```json
{
    "success": false,
    "error": { "code": "VALIDATION", "message": "Localized error text" }
}
```

- Unexpected exceptions: `"code": "INTERNAL"` (details logged server-side only).
- For `ReactFormFieldControl`, field validation errors are part of the state (`hasError`,
  `errorMessage`) and delivered via SSE patches. The command response itself only indicates
  success/failure of the command, not the resulting validation state.

### Client-Side

- `useTLCommand` exposes `error` state for React components to render.
- HTTP 401 from `ReactServlet` triggers a redirect to the login page.
- SSE disconnection: `EventSource` auto-reconnects; on reconnect, the bridge refetches
  state for all mounted controls.

---

## 12. Lifecycle Summary

```
 Server                                Client
 ------                                ------

 1. LayoutComponent renders
    -> ControlProvider creates
       ReactControl
    -> control.write() produces:        <div id="ctrl-123"
                                             data-react-module="..."/>
                                        <script>
                                          TLReact.mount("ctrl-123",
                                            "controls/TLChart",
                                            {initial state JSON});
                                        </script>

                                        2. TLReact.mount():
                                           - Create ControlStateStore
                                           - Subscribe to SSE for "ctrl-123"
                                           - import("/react/controls/TLChart.js")
                                           - ReactDOM.createRoot(div).render(...)

                                        3. User interacts
                                           -> useTLCommand().execute("save", {...})

 4. ReactServlet receives               <-- POST /react-api/command
    -> resolves control by ID
    -> dispatches ControlCommand
    -> command calls patchReactState()
       -> enqueued on SSEUpdateQueue
    -> returns success                   --> {"success": true}

 5. SSEUpdateQueue flushes               --> SSE: event: update
    (asynchronous, immediate)                data: {"controlId":"ctrl-123",
                                                    "type":"patch",
                                                    "patch":{"name":"New"}}

                                        6. SSEClient receives event
                                           -> ControlStateStore.applyPatch()
                                           -> React re-renders

 --- Cross-control updates ---

 7. Another command changes a model
    that a ReactControl listens to
    -> listener fires
    -> control.patchReactState(...)
    -> enqueued on SSEUpdateQueue
    -> delivered via SSE immediately      --> SSE: event: update
                                              data: {"controlId":"ctrl-456",...}

                                        8. React re-renders affected control
                                           (no user interaction required!)
```

---

## 13. Future Extensions

Considered in the architecture but out of scope for the initial implementation:

- **SSE for traditional controls**: Deliver `ContentReplacement`, `PropertyUpdate`, and
  `JSFunctionCall` actions via SSE (JSON-wrapped), eliminating the need to piggyback
  updates on command responses for the entire UI.
- **TypeScript type generation** from TopLogic model definitions (`*.model.xml`).
- **React form control library**: Systematic React replacements for all standard form
  field controls (text, select, date, checkbox, etc.).
- **Hot module replacement**: Vite dev server proxied through TopLogic for instant React
  reload during development.
- **Nested patch depth**: Support for deep merging in patches (e.g., patching
  `{"items[3].status": "done"}` within a table state) if the flat key-value patch model
  proves insufficient.

---

## 14. Resolved Design Decisions

| Decision | Resolution | Rationale |
|---|---|---|
| Update delivery path | Dedicated SSE stream, separate from XML/SOAP | Clean separation; enables server-push for the first time |
| Control ID stability | Use generated IDs (from `FrameScope.createNewID()`) | Stable while control is displayed; on detach, control is not reused |
| Transaction scope | Controls do not manage transactions | Transactions are a LayoutComponent concern, out of scope for Controls |
| I18N | Server resolves all labels, included in state | Simplest approach; no client-side resource bundles needed |
| Theme integration | CSS custom properties + programmatic access via `useTLTheme()` | Consistent look when desired, third-party libs work unchanged |
| State update granularity | Incremental patches (only changed keys) | Required for large state (tables); `patchReactState()` + `setReactState()` |
| SSE scope | General-purpose (any control can enqueue) | Enables future migration of all controls to server-push |
