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
command responses (request-response only). The SSE event stream introduced here is the
**universal delivery channel for all control updates** -- both React controls (which
receive JSON state patches) and traditional controls (whose `ClientAction`s are
JSON-wrapped and delivered through the same stream). When a React command triggers a
server-side model change that affects both React and non-React controls, all updates are
collected via the standard `RevalidationVisitor` and delivered uniformly through SSE.

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
|  +--------+---------------------+  +------+----------------------+   |
|           |                               |                          |
|   dispatchControlCommand           POST command                      |
|   (XML/SOAP, existing)            (JSON, new)                       |
|           |                               |                          |
|           |    SSE event stream (JSON) -- delivers ALL updates       |
|           |    <----------------------------------------------------+|
|           |    DOM actions for traditional controls (JSON-wrapped)   ||
|           |    State patches for React controls                     ||
|           |                               |                          |
+-----------+-------------------------------+--------------------------+
            |                               |
            v                               v
+----------------------------------------------------------------------+
|  TopLogic Server (Servlet Container)                                  |
|                                                                       |
|  +-----------------+  +------------------+  +---------------------+   |
|  | AJAXServlet     |  | ReactServlet     |  | SSEServlet          |   |
|  | /ajax           |  | /react-api/*     |  | /react-api/events   |   |
|  |                 |  |                  |  |                     |   |
|  | XML/SOAP        |  | JSON commands +  |  | Persistent push     |   |
|  | DOM actions     |  | revalidation     |  | connection          |   |
|  +---------+-------+  +--------+---------+  +----------+----------+   |
|            |                   |                       ^              |
|            |                   |   +-------------------+              |
|            |                   |   |                                  |
|            |              +----+---+-------+                         |
|            |              | Revalidation   |                         |
|            |              | Visitor        |                         |
|            |              | (collects ALL  |                         |
|            |              |  control       |                         |
|            |              |  updates)      |                         |
|            |              +----+-----------+                         |
|            |                   |                                     |
|            |              +----+----------+                          |
|            |              | SSEUpdateQueue|                          |
|            |              | - per session |                          |
|            |              | - JSON-wraps  |                          |
|            |              |   all actions |                          |
|            |              +---------------+                          |
|            |                                                         |
|            +--------------------+                                    |
|                                 |                                    |
|         +----------+----------+ |                                    |
|         | AbstractControlBase +-+                                    |
|         +---------------------+                                      |
|                    |                                                  |
|     +--------------+--------------+                                  |
|     |                             |                                  |
|  +--+---------------+  +----------+---------+                        |
|  | TextInputControl |  | ReactControl       |                        |
|  | (HTML updates)   |  | (JSON state)       |                        |
|  +------------------+  +--------------------+                        |
+----------------------------------------------------------------------+
```

### Key Principles

1. **A `ReactControl` is a Control.** It extends `AbstractControl` (or
   `AbstractFormFieldControl`), registers `ControlCommand`s, and lives within a
   `ControlScope`.

2. **SSE is the universal update delivery channel.** After command execution, the
   `ReactServlet` runs the standard `RevalidationVisitor` to collect updates from *all*
   controls -- React and traditional. React control updates are delivered as JSON state
   patches. Traditional control updates (`ContentReplacement`, `PropertyUpdate`,
   `JSSnipplet`, etc.) are JSON-wrapped and delivered through the same SSE stream. The
   client-side SSE handler dispatches each event type to the appropriate processor.

3. **The ReactServlet is a full peer of the AJAXServlet.** Both servlets execute commands
   and trigger revalidation. The difference is the transport: the AJAXServlet delivers
   updates in the XML/SOAP response; the ReactServlet delegates delivery to the SSE
   stream. When a React command causes traditional controls to update, those updates
   reach the client immediately via SSE -- no secondary AJAX round-trip needed.

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

The event envelope. Serialized as an SSE `data:` frame containing JSON. There are two
categories of events: **React state updates** (patches/full state for React controls) and
**DOM actions** (JSON-wrapped traditional `ClientAction`s for non-React controls).

```java
public class SSEEvent {

    // --- React control events ---

    /** Full state replacement for a React control. */
    public static SSEEvent fullState(String controlId,
            Map<String, Object> state) { ... }

    /** Incremental patch for a React control. */
    public static SSEEvent patch(String controlId,
            Map<String, Object> patch) { ... }

    // --- Traditional control events (JSON-wrapped ClientActions) ---

    /** JSON-wrap a ContentReplacement action. */
    public static SSEEvent contentReplacement(String elementId,
            String htmlFragment) { ... }

    /** JSON-wrap an ElementReplacement action. */
    public static SSEEvent elementReplacement(String elementId,
            String htmlFragment) { ... }

    /** JSON-wrap a PropertyUpdate action. */
    public static SSEEvent propertyUpdate(String elementId,
            List<Map<String, String>> properties) { ... }

    /** JSON-wrap a CssClassUpdate action. */
    public static SSEEvent cssClassUpdate(String elementId,
            String cssClass) { ... }

    /** JSON-wrap a FragmentInsertion action. */
    public static SSEEvent fragmentInsertion(String elementId,
            String position, String htmlFragment) { ... }

    /** JSON-wrap a RangeReplacement action. */
    public static SSEEvent rangeReplacement(String startId,
            String stopId, String htmlFragment) { ... }

    /** JSON-wrap a JSSnipplet action. */
    public static SSEEvent jsSnipplet(String code) { ... }

    /** JSON-wrap a JSFunctionCall action. */
    public static SSEEvent jsFunctionCall(String elementId,
            String functionRef, String functionName,
            Object[] arguments) { ... }

    /**
     * Serialize as SSE text frame.
     */
    public void writeTo(Writer out) throws IOException { ... }
}
```

### 5.5 Converting Traditional ClientActions to SSEEvents

After the `ReactServlet` runs the `RevalidationVisitor`, it collects all `ClientAction`s
from the `UpdateQueue`. Each action is converted to an `SSEEvent`:

```java
/**
 * Converts traditional ClientActions (which would normally be serialized as
 * XML in the SOAP response) into JSON SSEEvents for delivery via the event
 * stream.
 */
public class ClientActionConverter {

    public SSEEvent convert(ClientAction action) {
        if (action instanceof ContentReplacement cr) {
            return SSEEvent.contentReplacement(
                cr.getElementId(), renderFragment(cr));
        } else if (action instanceof ElementReplacement er) {
            return SSEEvent.elementReplacement(
                er.getElementId(), renderFragment(er));
        } else if (action instanceof PropertyUpdate pu) {
            return SSEEvent.propertyUpdate(
                pu.getElementId(), pu.getProperties());
        } else if (action instanceof JSSnipplet js) {
            return SSEEvent.jsSnipplet(js.getCode());
        } else if (action instanceof ReactUpdate ru) {
            // Already a React event -- pass through
            return ru.toSSEEvent();
        }
        // ... other action types
    }

    /** Render an HTMLFragment to an HTML string. */
    private String renderFragment(DOMModification mod) {
        StringWriter sw = new StringWriter();
        TagWriter tw = new TagWriter(sw);
        mod.getFragment().write(displayContext, tw);
        return sw.toString();
    }
}
```

This conversion is the key bridge: traditional controls continue to produce their normal
`ClientAction`s (they don't know about SSE). The `ReactServlet` converts these to JSON
and delivers them. The traditional controls require **zero changes**.

### 5.6 SSE Wire Format

Each SSE frame is a JSON object on a `data:` line. The `event:` field distinguishes
React state updates from DOM actions:

**React state patch** (incremental, only changed keys):

```
event: state
data: {"controlId":"ctrl-123","type":"patch","patch":{"value":"new text","hasError":false}}

```

**React full state** (on mount, reconnect, or explicit reset):

```
event: state
data: {"controlId":"ctrl-123","type":"full","state":{"value":"x","editable":true,"disabled":false}}

```

**DOM action -- ContentReplacement:**

```
event: domAction
data: {"type":"ContentReplacement","elementId":"ctrl-456","fragment":"<span>new content</span>"}

```

**DOM action -- ElementReplacement:**

```
event: domAction
data: {"type":"ElementReplacement","elementId":"ctrl-456","fragment":"<div id='ctrl-456'>replaced</div>"}

```

**DOM action -- PropertyUpdate:**

```
event: domAction
data: {"type":"PropertyUpdate","elementId":"input-789","properties":[{"name":"value","value":"hello"},{"name":"disabled","value":"false"}]}

```

**DOM action -- CssClassUpdate:**

```
event: domAction
data: {"type":"CssClassUpdate","elementId":"ctrl-456","cssClass":"tlControl active selected"}

```

**DOM action -- FragmentInsertion:**

```
event: domAction
data: {"type":"FragmentInsertion","elementId":"list-1","position":"beforeend","fragment":"<li>new item</li>"}

```

**DOM action -- RangeReplacement:**

```
event: domAction
data: {"type":"RangeReplacement","startId":"row-1","stopId":"row-5","fragment":"<tr>...</tr>"}

```

**DOM action -- JSSnipplet:**

```
event: domAction
data: {"type":"JSSnipplet","code":"services.form.TableControl.init('tbl-1');"}

```

**DOM action -- JSFunctionCall:**

```
event: domAction
data: {"type":"FunctionCall","elementId":"ctrl-1","functionRef":"BAL","functionName":"setStyle","arguments":["color: red;"]}

```

**Keepalive** (prevents proxy/connection timeout):

```
: keepalive

```

### 5.7 Reconnection

The `EventSource` API in the browser handles reconnection automatically. On reconnect:

1. The client sends a `Last-Event-ID` header (if the server set `id:` on events).
2. The server can replay missed events from a short buffer, or:
3. The client re-fetches full state for all mounted React controls via
   `POST /react-api/state`.

For the initial implementation, option (3) is sufficient -- no server-side event buffering
is needed. The client re-fetches state for mounted React controls on reconnect. For
traditional controls, the server can trigger a full repaint of the visible component
tree on reconnect to ensure consistency.

---

## 6. The React Servlet (`ReactServlet`)

### 6.1 Purpose

The `ReactServlet` provides a **JSON-based command endpoint** for React controls. It
handles two operations:

- **Command dispatch**: Execute a `ControlCommand` on a `ReactControl`, then run the
  `RevalidationVisitor` to collect updates from *all* controls (React and traditional),
  convert them to `SSEEvent`s, and enqueue them on the SSE stream.
- **State fetch**: Retrieve the current full state of a `ReactControl` (used on mount and
  reconnect).

**Server-to-client updates are NOT part of command responses.** The command response only
indicates success or error. All side effects -- including updates to the command's own
control, other React controls, and traditional controls -- are delivered via the SSE
stream. This clean separation means the React client never needs to parse update payloads
from command responses.

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
For /react-api/command:

  1. Parse JSON request body
  2. Extract HTTP session (same session cookie as main TopLogic app)
  3. Validate session is active, user is authenticated
  4. Acquire the session's RequestLock (writer lock)
  5. Resolve the FrameScope from the session's MainLayout
  6. Look up the target control by controlId in the FrameScope's CommandListener registry
  7. Verify the control is a ReactControl (reject otherwise)
  8. Execute: control.executeCommand(context, commandName, arguments)
  9. Run RevalidationVisitor on the MainLayout
     - Visits ALL components/controls, not just the targeted one
     - Collects ClientActions from all invalid controls
     - React controls: produce ReactUpdate events (already on SSE queue)
     - Traditional controls: produce ContentReplacement, PropertyUpdate, etc.
 10. Convert traditional ClientActions to SSEEvents via ClientActionConverter
 11. Enqueue all SSEEvents on the session's SSEUpdateQueue
 12. Release the RequestLock
 13. Return JSON response: {"success": true} or {"success": false, "error": {...}}
     (updates are delivered asynchronously via SSE, not in this response)

For /react-api/state:

  1. Parse JSON request body
  2. Validate session
  3. Resolve control by controlId
  4. Return: {"success": true, "state": control.getReactState()}
```

The critical step is **9**: by running the `RevalidationVisitor`, the `ReactServlet`
ensures that all controls affected by the command -- regardless of type -- have their
updates collected and delivered. This is the same visitor that the `AJAXServlet` runs
after command execution (see `AJAXServlet.validate()`), ensuring identical semantics.

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

The bridge establishes a single `EventSource` connection on page load. It handles two
event types: `state` events (for React controls) and `domAction` events (for traditional
controls whose updates were triggered by a React command).

```typescript
class SSEClient {
    private _source: EventSource;
    private _stateListeners: Map<string, (event: SSEStateEvent) => void>;

    connect(url: string): void {
        this._source = new EventSource(url);

        // React control state updates (patches and full state)
        this._source.addEventListener('state', (e: MessageEvent) => {
            const event: SSEStateEvent = JSON.parse(e.data);
            const listener = this._stateListeners.get(event.controlId);
            if (listener) {
                listener(event);
            }
        });

        // Traditional control DOM actions (JSON-wrapped ClientActions)
        this._source.addEventListener('domAction', (e: MessageEvent) => {
            const action: DOMActionEvent = JSON.parse(e.data);
            DOMActionProcessor.apply(action);
        });

        this._source.onerror = () => {
            // EventSource reconnects automatically.
            // On reconnect, re-fetch state for all mounted React controls.
            this.refetchAll();
        };
    }

    subscribe(controlId: string,
              callback: (event: SSEStateEvent) => void): void {
        this._stateListeners.set(controlId, callback);
    }

    unsubscribe(controlId: string): void {
        this._stateListeners.delete(controlId);
    }
}

/**
 * Applies JSON-wrapped DOM actions to the page.
 * This is the JSON equivalent of what simpleajax.js does for XML actions.
 */
class DOMActionProcessor {
    static apply(action: DOMActionEvent): void {
        switch (action.type) {
            case 'ContentReplacement': {
                const el = document.getElementById(action.elementId);
                if (el) el.innerHTML = action.fragment;
                break;
            }
            case 'ElementReplacement': {
                const el = document.getElementById(action.elementId);
                if (el) el.outerHTML = action.fragment;
                break;
            }
            case 'PropertyUpdate': {
                const el = document.getElementById(action.elementId);
                if (el) {
                    for (const prop of action.properties) {
                        (el as any)[prop.name] = coerceValue(prop.value);
                    }
                }
                break;
            }
            case 'CssClassUpdate': {
                const el = document.getElementById(action.elementId);
                if (el) el.className = action.cssClass;
                break;
            }
            case 'FragmentInsertion': {
                const el = document.getElementById(action.elementId);
                if (el) el.insertAdjacentHTML(action.position, action.fragment);
                break;
            }
            case 'RangeReplacement': {
                // Replace all siblings from startId to stopId
                const start = document.getElementById(action.startId);
                const stop = document.getElementById(action.stopId);
                if (start && stop) {
                    start.insertAdjacentHTML('beforebegin', action.fragment);
                    // Remove nodes from start to stop (inclusive)
                    let node = start;
                    while (node && node !== stop) {
                        const next = node.nextSibling;
                        node.remove();
                        node = next as Element;
                    }
                    stop.remove();
                }
                break;
            }
            case 'JSSnipplet': {
                eval(action.code);
                break;
            }
            case 'FunctionCall': {
                const el = document.getElementById(action.elementId);
                const obj = eval(action.functionRef);
                obj[action.functionName].apply(obj, [el, ...action.arguments]);
                break;
            }
        }
    }
}
```

This means that when a React button click triggers a server command that causes a
traditional `SelectControl` to update its options, the `PropertyUpdate` or
`ContentReplacement` for that `SelectControl` arrives via SSE and is applied to the DOM
immediately -- the user sees both the React control and the traditional control update
together, without any additional user interaction.

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

                                        3. User clicks React button
                                           -> useTLCommand().execute("save", {...})

 4. ReactServlet receives               <-- POST /react-api/command
    -> acquires RequestLock
    -> resolves control by ID
    -> dispatches ControlCommand
    -> command modifies model
       (affects React AND traditional
        controls)

 5. ReactServlet runs
    RevalidationVisitor
    -> visits entire component tree
    -> ReactControl: patchReactState()
       -> SSEEvent.patch("ctrl-123",...)
    -> TextInputControl: addUpdate(
         PropertyUpdate("input-456",...))
       -> ClientActionConverter converts
          to SSEEvent.propertyUpdate(...)
    -> SelectControl: requestRepaint()
       -> renders HTML, creates
          ContentReplacement
       -> ClientActionConverter converts
          to SSEEvent.contentReplacement()
    -> all events enqueued on
       SSEUpdateQueue

 6. ReactServlet returns                 --> {"success": true}
    (updates NOT in response)

 7. SSEUpdateQueue flushes               --> SSE: event: state
    (immediate, all events)                  data: {"controlId":"ctrl-123",
                                                    "type":"patch",
                                                    "patch":{"name":"New"}}

                                             SSE: event: domAction
                                             data: {"type":"PropertyUpdate",
                                                    "elementId":"input-456",
                                                    "properties":[...]}

                                             SSE: event: domAction
                                             data: {"type":"ContentReplacement",
                                                    "elementId":"select-789",
                                                    "fragment":"<select>...</select>"}

                                        8. SSEClient dispatches:
                                           - "state" -> ControlStateStore.applyPatch()
                                             -> React re-renders ctrl-123
                                           - "domAction" -> DOMActionProcessor.apply()
                                             -> DOM updates input-456, select-789

                                        Result: ALL controls update together,
                                        both React and traditional, from a
                                        single React button click.
```

---

## 13. Future Extensions

Considered in the architecture but out of scope for the initial implementation:

- **SSE as delivery channel for AJAXServlet commands**: Currently, only commands routed
  through the `ReactServlet` deliver updates via SSE. A future step could have the
  `AJAXServlet` also enqueue updates on the SSE stream, unifying the delivery path
  entirely and eliminating the XML/SOAP response for updates.
- **TypeScript type generation** from TopLogic model definitions (`*.model.xml`).
- **React form control library**: Systematic React replacements for all standard form
  field controls (text, select, date, checkbox, etc.).
- **Hot module replacement**: Vite dev server proxied through TopLogic for instant React
  reload during development.
- **Nested patch depth**: Support for deep merging in patches (e.g., patching
  `{"items[3].status": "done"}` within a table state) if the flat key-value patch model
  proves insufficient.
- **SSE event buffering and replay**: Server-side event log enabling replay on reconnect
  instead of full state refetch.

---

## 14. Resolved Design Decisions

| Decision | Resolution | Rationale |
|---|---|---|
| Update delivery path | SSE stream delivers ALL updates (React + traditional) | ReactServlet runs RevalidationVisitor, JSON-wraps traditional ClientActions; no stranded updates |
| Control ID stability | Use generated IDs (from `FrameScope.createNewID()`) | Stable while control is displayed; on detach, control is not reused |
| Transaction scope | Controls do not manage transactions | Transactions are a LayoutComponent concern, out of scope for Controls |
| I18N | Server resolves all labels, included in state | Simplest approach; no client-side resource bundles needed |
| Theme integration | CSS custom properties + programmatic access via `useTLTheme()` | Consistent look when desired, third-party libs work unchanged |
| State update granularity | Incremental patches (only changed keys) | Required for large state (tables); `patchReactState()` + `setReactState()` |
| SSE scope | Universal: delivers React patches AND JSON-wrapped DOM actions | Mixed React/traditional views work correctly from day one |
| Revalidation in ReactServlet | Full RevalidationVisitor run after each command | Same semantics as AJAXServlet; traditional controls get updates immediately |
