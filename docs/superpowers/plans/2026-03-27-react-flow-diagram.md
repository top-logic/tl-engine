# React Flow Diagram — Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the Graphic Blocks flow diagram library into a React-compatible module group (`com.top_logic.react.flow.*`) that uses React SSE/Commands instead of legacy AJAX for communication.

**Architecture:** Three new Maven modules mirror the existing `graphic.blocks` structure. The shared model and GWT rendering code are copied with minimal changes. The server control extends `ReactControl` instead of `AbstractControlBase`. The GWT client communicates via a JsInterop bridge to the React SSE/Command infrastructure. A minimal React component (`TLFlowDiagram.tsx`) handles only the GWT mount/unmount lifecycle.

**Tech Stack:** Java 17, GWT, msgbuf, Maven, TypeScript (minimal — one file)

**Reference files:**
- Design spec: `docs/superpowers/specs/2026-03-24-model-editor-react-design.md`
- Existing source: `com.top_logic.graphic.blocks[.client|.server]`
- React infrastructure: `com.top_logic.layout.react`

---

## File Structure

### Module: com.top_logic.react.flow.common

Shared model, layout, SVG rendering — GWT-compatible. Copied from `com.top_logic.graphic.blocks` with package rename.

```
com.top_logic.react.flow.common/
  pom.xml
  src/main/java/
    com/top_logic/react/flow/common/
      TLReactFlow.gwt.xml                          — GWT module (shared sources)
      version.properties
    com/top_logic/react/flow/data/
      data.proto                                    — msgbuf protocol (copied)
      *.java                                        — generated model classes
      impl/*.java                                   — generated implementations
    com/top_logic/react/flow/operations/
      *.java                                        — layout/rendering operations (copied)
      layout/*.java
      tree/*.java
      util/*.java
    com/top_logic/react/flow/callback/
      ClickHandler.java, DropHandler.java, etc.     — callback interfaces (copied)
    com/top_logic/react/flow/control/
      FlowControlCommon.java                        — shared command constants (NEW)
    com/top_logic/react/flow/svg/
      SvgWriter.java, RenderContext.java, etc.      — SVG infrastructure (copied)
  src/main/webapp/WEB-INF/conf/
    metaConf.txt                                    — references tl-react-flow-common.conf.xml
    tl-react-flow-common.conf.xml                   — module configuration (if needed)
```

### Module: com.top_logic.react.flow.server

Server-side ReactControl, command handlers.

```
com.top_logic.react.flow.server/
  pom.xml
  src/main/java/
    com/top_logic/react/flow/server/
      version.properties
    com/top_logic/react/flow/server/control/
      FlowDiagramControl.java                       — ReactControl subclass (NEW)
    com/top_logic/react/flow/server/ui/
      FlowDiagramElement.java                       — UIElement for <flow-diagram> (NEW)
      FlowChartBuilder.java                         — abstract builder (copied)
      ScriptFlowChartBuilder.java                   — script-based builder (copied)
      AWTContext.java                               — server-side text metrics (copied)
      DiagramChannel.java                           — channel for diagram model (copied)
      SelectableIndexCreator.java                   — selectable index (copied)
      ObservedIndexCreator.java                     — observed index (copied)
      DescendingBoxVisitor.java                     — box traversal (copied)
    com/top_logic/react/flow/server/handler/
      ContextMenuHandler.java                       — context menu (copied)
      ScriptedClickHandler.java                     — click handler (copied)
      ScriptedDropHandler.java                      — drop handler (copied)
      ServerDropHandler.java                        — drop base class (copied)
    com/top_logic/react/flow/server/svg/
      SvgTagWriter.java                             — server SVG rendering (copied)
  src/main/webapp/WEB-INF/conf/
    metaConf.txt                                    — references tl-react-flow-server.config.xml
    tl-react-flow-server.config.xml                 — module configuration
```

### Module: com.top_logic.react.flow.client

GWT client with adapted communication layer.

```
com.top_logic.react.flow.client/
  pom.xml
  src/main/java/
    com/top_logic/react/flow/client/
      TLReactFlowClient.gwt.xml                     — GWT module
      version.properties
    com/top_logic/react/flow/client/boot/
      ModuleEntry.java                              — GWT entry point (NEW)
    com/top_logic/react/flow/client/control/
      FlowDiagramClientControl.java                 — adapted JSDiagramControl (ADAPTED)
      SVGBuilder.java                               — SVG DOM builder (copied)
      JSRenderContext.java                          — client render context (copied)
      JSTextMetrics.java                            — client text metrics (copied)
      SubIdGenerator.java                           — ID generator (copied)
    com/top_logic/react/flow/client/bridge/
      ReactBridge.java                              — JsInterop to React SSE/Commands (NEW)
      StateListener.java                            — callback interface (NEW)
    com/top_logic/react/flow/client/dom/
      DOMUtil.java                                  — DOM helpers (copied)
  src/main/typescript/
    TLFlowDiagram.tsx                               — minimal React lifecycle wrapper (NEW)
    vite.config.ts                                  — Vite build config (NEW)
    package.json                                    — npm dependencies (NEW)
    tsconfig.json                                   — TypeScript config (NEW)
```

---

## Task 1: Create Maven Module Structure

**Files:**
- Create: `com.top_logic.react.flow.common/pom.xml`
- Create: `com.top_logic.react.flow.server/pom.xml`
- Create: `com.top_logic.react.flow.client/pom.xml`
- Modify: `tl-parent-engine/pom.xml` — register new modules

- [ ] **Step 1: Create com.top_logic.react.flow.common/pom.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.top-logic</groupId>
        <artifactId>tl-parent-core-internal</artifactId>
        <version>7.11.0-SNAPSHOT</version>
        <relativePath>../tl-parent-core-internal</relativePath>
    </parent>

    <artifactId>tl-react-flow-common</artifactId>
    <description>Shared model, layout, and SVG rendering for React flow diagrams (GWT-compatible).</description>

    <dependencies>
        <dependency>
            <groupId>com.top-logic</groupId>
            <artifactId>tl-common-json</artifactId>
        </dependency>
        <dependency>
            <groupId>de.haumacher.msgbuf</groupId>
            <artifactId>msgbuf-api</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>de.haumacher.msgbuf</groupId>
                <artifactId>msgbuf-generator-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create com.top_logic.react.flow.server/pom.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.top-logic</groupId>
        <artifactId>tl-parent-core-internal</artifactId>
        <version>7.11.0-SNAPSHOT</version>
        <relativePath>../tl-parent-core-internal</relativePath>
    </parent>

    <artifactId>tl-react-flow-server</artifactId>
    <description>Server-side ReactControl for React flow diagrams.</description>

    <dependencies>
        <dependency>
            <groupId>com.top-logic</groupId>
            <artifactId>tl-react-flow-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.top-logic</groupId>
            <artifactId>tl-layout-view</artifactId>
        </dependency>
        <dependency>
            <groupId>com.top-logic</groupId>
            <artifactId>tl-model-search</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3: Create com.top_logic.react.flow.client/pom.xml**

Use `tl-parent-gwt` as parent. Depend on `tl-react-flow-common` (sources for GWT),
`lib-gwt-svg`, `elemental2-dom`.

Reference `com.top_logic.graphic.blocks.client/pom.xml` for the exact GWT plugin
configuration (module name, output, etc.), adapting the GWT module name to
`com.top_logic.react.flow.client.TLReactFlowClient`.

- [ ] **Step 4: Register modules in tl-parent-engine/pom.xml**

Add to the `<modules>` section:
```xml
<module>../com.top_logic.react.flow.common</module>
<module>../com.top_logic.react.flow.server</module>
<module>../com.top_logic.react.flow.client</module>
```

- [ ] **Step 5: Create version.properties and metaConf.txt for each module**

Each module needs:
- `src/main/java/com/top_logic/react/flow/[common|server|client]/version.properties`

Modules with webapp configuration need:
- `src/main/webapp/WEB-INF/conf/metaConf.txt` — listing the `.conf.xml` files
- `src/main/webapp/WEB-INF/conf/tl-react-flow-<module>.conf.xml` — module configuration

No `web-fragment.xml` is needed — there are no new servlets. The React infrastructure
(`/react-api/command`, `/react-api/events`) already provides all endpoints.

- [ ] **Step 6: Verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server,com.top_logic.react.flow.client
```

Expected: BUILD SUCCESS (empty modules compile)

- [ ] **Step 7: Commit**

```
Ticket #29108: Create Maven module structure for React flow diagram.
```

---

## Task 2: Copy Shared Model and Operations

Copy the msgbuf model, operations, callbacks, and SVG infrastructure from
`com.top_logic.graphic.blocks` to `com.top_logic.react.flow.common` with package rename.

**Source:** `com.top_logic.graphic.blocks/src/main/java/com/top_logic/graphic/flow/`
**Target:** `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/`

- [ ] **Step 1: Copy data.proto**

Copy `com.top_logic.graphic.blocks/.../graphic/flow/data/data.proto`
to `com.top_logic.react.flow.common/.../react/flow/data/data.proto`.

Update the `package` declaration:
```protobuf
package com.top_logic.react.flow.data;
```

Update all `@Operations` annotations to reference new package paths:
`com.top_logic.react.flow.operations.*` instead of `com.top_logic.graphic.flow.operations.*`.

- [ ] **Step 2: Copy operations classes**

Copy entire `com/top_logic/graphic/flow/operations/` tree to
`com/top_logic/react/flow/operations/`. Update package declarations and imports.

- [ ] **Step 3: Copy callback interfaces**

Copy `com/top_logic/graphic/flow/callback/` to `com/top_logic/react/flow/callback/`.
Update packages.

- [ ] **Step 4: Copy SVG infrastructure**

Copy from `com/top_logic/graphic/blocks/svg/` to `com/top_logic/react/flow/svg/`.
Update packages.

- [ ] **Step 5: Copy math utilities**

Copy `com/top_logic/graphic/blocks/math/` to `com/top_logic/react/flow/math/`.

- [ ] **Step 6: Create FlowControlCommon.java**

```java
package com.top_logic.react.flow.control;

public interface FlowControlCommon {
    String DIAGRAM_UPDATE_COMMAND = "diagramUpdate";
    String CLICK_COMMAND = "click";
    String DROP_COMMAND = "drop";
    String CONTEXT_MENU_COMMAND = "contextMenu";
    String SELECTION_COMMAND = "selection";
    String VIEWBOX_COMMAND = "viewbox";
}
```

- [ ] **Step 7: Create GWT module TLReactFlow.gwt.xml**

```xml
<module>
    <inherits name="com.google.gwt.user.User" />
    <inherits name="com.top_logic.common.json.TLCommonJson" />
    <inherits name="de.haumacher.msgbuf.MsgBuf" />

    <source path="data" />
    <source path="operations" />
    <source path="callback" />
    <source path="control" />
    <source path="svg" />
    <source path="math" />
</module>
```

- [ ] **Step 8: Generate msgbuf classes and verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common
```

Expected: BUILD SUCCESS, generated Java classes in `data/` and `data/impl/`.

- [ ] **Step 9: Commit**

```
Ticket #29108: Copy shared model, operations, and SVG infrastructure to react.flow.common.
```

---

## Task 3: Copy Server-Side Components

Copy server-side code from `com.top_logic.graphic.blocks.server` with package rename.
The `DiagramControl` and `FlowChartComponent` are copied as-is first — they will be
adapted in Task 5.

**Source:** `com.top_logic.graphic.blocks.server/src/main/java/`
**Target:** `com.top_logic.react.flow.server/src/main/java/`

- [ ] **Step 1: Copy FlowChartBuilder and related classes**

Copy these files from `com/top_logic/graphic/flow/server/ui/` to
`com/top_logic/react/flow/server/ui/`:
- `FlowChartBuilder.java`
- `ScriptFlowChartBuilder.java`
- `AWTContext.java`
- `DiagramChannel.java`
- `SelectableIndexCreator.java`
- `ObservedIndexCreator.java`
- `DescendingBoxVisitor.java`

Update package declarations and imports to `com.top_logic.react.flow.*`.

- [ ] **Step 2: Copy handler classes**

Copy from `com/top_logic/graphic/flow/server/ui/handler/` to
`com/top_logic/react/flow/server/handler/`:
- `ContextMenuHandler.java`
- `ScriptedClickHandler.java`
- `ScriptedDropHandler.java`
- `ServerDropHandler.java`
- `DiagramContextMenuProviderSPI.java`

- [ ] **Step 3: Copy SvgTagWriter**

Copy `com/top_logic/graphic/blocks/server/svg/SvgTagWriter.java` to
`com/top_logic/react/flow/server/svg/SvgTagWriter.java`.

- [ ] **Step 4: Copy DiagramControl as starting point**

Copy `com/top_logic/graphic/flow/server/control/DiagramControl.java` to
`com/top_logic/react/flow/server/control/FlowDiagramControl.java`.
Rename class. Update packages. This will be adapted in Task 5.

- [ ] **Step 5: Verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server
```

- [ ] **Step 6: Commit**

```
Ticket #29108: Copy server-side components to react.flow.server.
```

---

## Task 4: Create JsInterop Bridge (GWT ↔ React)

Create the JsInterop bridge that allows GWT-compiled code to subscribe to SSE events
and send commands via the React infrastructure.

**Files:**
- Create: `com.top_logic.react.flow.client/.../bridge/ReactBridge.java`
- Create: `com.top_logic.react.flow.client/.../bridge/StateListener.java`

- [ ] **Step 1: Create StateListener interface**

```java
package com.top_logic.react.flow.client.bridge;

import jsinterop.annotations.JsFunction;

/**
 * Callback for receiving SSE state updates from the React infrastructure.
 */
@JsFunction
public interface StateListener {
    void onState(Object state);
}
```

- [ ] **Step 2: Create ReactBridge**

```java
package com.top_logic.react.flow.client.bridge;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop bridge to the React SSE/Command infrastructure.
 *
 * <p>
 * Provides access to {@code window.TLReact.subscribe()} for receiving SSE events
 * and {@code fetch('/react-api/command')} for sending commands to the server.
 * </p>
 */
public class ReactBridge {

    /**
     * Subscribe to SSE state updates for a control.
     *
     * @param controlId the server-side control ID
     * @param listener  callback receiving state objects
     */
    public static native void subscribe(String controlId, StateListener listener) /*-{
        if ($wnd.TLReact && $wnd.TLReact.subscribe) {
            $wnd.TLReact.subscribe(controlId, listener);
        } else {
            $wnd.console.error('[ReactBridge] TLReact.subscribe not available');
        }
    }-*/;

    /**
     * Unsubscribe from SSE state updates.
     */
    public static native void unsubscribe(String controlId, StateListener listener) /*-{
        if ($wnd.TLReact && $wnd.TLReact.unsubscribe) {
            $wnd.TLReact.unsubscribe(controlId, listener);
        }
    }-*/;

    /**
     * Send a command to the server via the React command endpoint.
     *
     * @param contextPath base path (e.g. "/demo")
     * @param controlId   the control ID
     * @param command     command name
     * @param windowName  browser window identifier
     * @param arguments   JSON-serializable arguments object
     */
    public static native void sendCommand(
            String contextPath, String controlId, String command,
            String windowName, JavaScriptObject arguments) /*-{
        var url = contextPath + '/react-api/command';
        var body = {
            controlId: controlId,
            command: command,
            windowName: windowName,
            arguments: arguments || {}
        };
        $wnd.fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).catch(function(err) {
            $wnd.console.error('[ReactBridge] Command failed:', err);
        });
    }-*/;
}
```

- [ ] **Step 3: Verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.client
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Create JsInterop bridge for GWT-to-React SSE/Command communication.
```

---

## Task 5: Adapt Server Control to ReactControl

Transform the copied `FlowDiagramControl` from `AbstractControlBase` to `ReactControl`.
The key change: instead of writing HTML + content URL + JSFunctionCall, the control
serializes the diagram as React state and pushes updates via SSE.

**Files:**
- Modify: `com.top_logic.react.flow.server/...control/FlowDiagramControl.java`

- [ ] **Step 1: Study the existing DiagramControl communication pattern**

Read `com.top_logic.graphic.blocks.server/.../DiagramControl.java` (407 lines).
Key methods to understand before adapting:
- `internalWrite()` — renders `<div>` with content URL attribute
- `handleContent()` — serves diagram JSON via HTTP response
- `internalRevalidate()` — creates JSON patch, sends via `JSFunctionCall`
- `processUpdate()` — receives client patch, applies to scope
- `processClick()` / `processDrop()` — routes events to handlers

- [ ] **Step 2: Rewrite FlowDiagramControl extending ReactControl**

The control must:
1. Extend `ReactControl` with module name `"TLFlowDiagram"`
2. Serialize the full diagram as initial React state (JSON string in `"diagram"` key)
3. Use `patchReactState()` or `FunctionCall` to push diagram patches via SSE
4. Handle commands via `@ReactCommand` annotations instead of `ControlCommand` inner classes

Key structure:

```java
package com.top_logic.react.flow.server.control;

import com.top_logic.layout.react.control.ReactControl;
// ... imports

public class FlowDiagramControl extends ReactControl {

    private Diagram _diagram;
    private DefaultScope _graphScope;

    public FlowDiagramControl(ReactContext context, Diagram diagram) {
        super(context, "TLFlowDiagram");
        _diagram = diagram;
        _graphScope = new ExternalScope(2, 0);
        // Serialize initial diagram state
        putState("diagram", serializeDiagram());
    }

    private String serializeDiagram() {
        StringW out = new StringW();
        try (JsonWriter writer = new JsonWriter(out)) {
            _diagram.writeTo(_graphScope, writer);
        }
        return out.toString();
    }

    /** Push diagram changes to client via SSE. */
    public void pushDiagramUpdate(SSEUpdateQueue queue) {
        if (_graphScope.hasChanges()) {
            String patch = _graphScope.createPatch();
            // Use FunctionCall to deliver patch to GWT code
            FunctionCall event = FunctionCall.create()
                .setElementId(getID())
                .setFunctionRef("GWT_FlowDiagram")
                .setFunctionName("applyPatch")
                .setArguments(patch);
            queue.enqueue(event);
        }
    }

    @ReactCommand("click")
    public HandlerResult handleClick(ReactContext context, Map<String, Object> args) {
        int nodeId = ((Number) args.get("nodeId")).intValue();
        // ... resolve node, dispatch to click handler
    }

    @ReactCommand("drop")
    public HandlerResult handleDrop(ReactContext context, Map<String, Object> args) {
        // ... resolve node, dispatch to drop handler
    }

    @ReactCommand("selection")
    public HandlerResult handleSelection(ReactContext context, Map<String, Object> args) {
        // ... update selection in diagram model
    }

    @ReactCommand("viewbox")
    public HandlerResult handleViewbox(ReactContext context, Map<String, Object> args) {
        // ... update viewbox (lazy, no response needed)
    }

    @ReactCommand("update")
    public HandlerResult handleUpdate(ReactContext context, Map<String, Object> args) {
        String patch = (String) args.get("patch");
        // ... apply client-side changes to _graphScope
    }
}
```

Note: The exact SSE mechanism for diagram patches needs to be decided based on what
works — `FunctionCall` is available immediately, `PatchEvent` with a custom key is
another option. Start with `FunctionCall` and refactor if needed.

- [ ] **Step 3: Verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Adapt FlowDiagramControl to extend ReactControl with SSE communication.
```

---

## Task 6: Adapt GWT Client Control

Adapt `JSDiagramControl` for the React integration. The key changes:
- Remove content URL fetching — initial diagram comes from React state attribute
- Replace AJAX `sendUpdate()` / `sendLazyUpdate()` with `ReactBridge.sendCommand()`
- Subscribe to SSE events via `ReactBridge.subscribe()` instead of receiving `invoke()`
- Mount/unmount lifecycle managed by React (not by the GWT control framework)

**Files:**
- Create: `com.top_logic.react.flow.client/.../control/FlowDiagramClientControl.java`
- Create: `com.top_logic.react.flow.client/.../boot/ModuleEntry.java`

- [ ] **Step 1: Study existing JSDiagramControl lifecycle**

Read `com.top_logic.graphic.blocks.client/.../JSDiagramControl.java` (827 lines).
Key flow:
1. `init()` — fetches JSON from content URL, parses diagram, layouts, draws SVG
2. `invoke("update", patch)` — receives server patch, applies, redraws dirty nodes
3. `sendUpdate(patch)` — sends client changes to server via AJAX
4. `onChange()` — debounced change handler, creates patch, calls sendUpdate

- [ ] **Step 2: Create FlowDiagramClientControl**

This is NOT a GWT `AbstractJSControl`. It is a plain Java class that:
1. Is instantiated from a global JS function `GWT_FlowDiagram.mount(div, controlId, windowName)`
2. Parses the initial diagram JSON from a parameter
3. Creates the SVG in the given div
4. Subscribes to SSE via `ReactBridge.subscribe(controlId, ...)`
5. Sends commands via `ReactBridge.sendCommand(...)`

Key structure (pseudo-code, adapt from JSDiagramControl):

```java
package com.top_logic.react.flow.client.control;

public class FlowDiagramClientControl implements DiagramContext {

    private Diagram _diagram;
    private DefaultScope _scope;
    private HTMLDivElement _container;
    private OMSVGSVGElement _svg;
    private String _controlId;
    private String _windowName;
    private String _contextPath;

    /** Called from TLFlowDiagram.tsx via global GWT_FlowDiagram.mount(). */
    public static FlowDiagramClientControl mount(
            HTMLDivElement container, String controlId,
            String windowName, String contextPath, String diagramJson) {
        FlowDiagramClientControl control = new FlowDiagramClientControl();
        control.init(container, controlId, windowName, contextPath, diagramJson);
        return control;
    }

    private void init(HTMLDivElement container, String controlId,
            String windowName, String contextPath, String diagramJson) {
        _container = container;
        _controlId = controlId;
        _windowName = windowName;
        _contextPath = contextPath;

        // Parse diagram from initial state JSON
        _scope = new DefaultScope(2, 1);
        _diagram = Diagram.readDiagram(
            new com.top_logic.common.json.gstream.JsonReader(diagramJson), _scope);

        // Layout and render
        JSRenderContext renderContext = new JSRenderContext();
        _diagram.layout(renderContext);
        SVGBuilder svgBuilder = new SVGBuilder(/* ... */);
        _diagram.draw(svgBuilder);

        // Insert SVG into container
        _container.appendChild(svgBuilder.getSvgElement());
        _svg = svgBuilder.getSvgElement();

        // Subscribe to SSE for server updates
        ReactBridge.subscribe(_controlId, this::onServerUpdate);

        // Attach interaction event handlers (pan, zoom, click, drop)
        attachEventHandlers();
    }

    /** Receives server-side diagram patches via SSE. */
    private void onServerUpdate(Object state) {
        // state contains the patch JSON string
        // Apply to _scope, track dirty nodes, redraw
    }

    /** Send client changes to server. */
    private void sendCommand(String command, JavaScriptObject args) {
        ReactBridge.sendCommand(_contextPath, _controlId, command, _windowName, args);
    }

    /** Cleanup when React unmounts the component. */
    public void destroy() {
        ReactBridge.unsubscribe(_controlId, this::onServerUpdate);
        // Remove event handlers, clear SVG
    }

    // ... adapt pan, zoom, click, drop, onChange from JSDiagramControl
}
```

- [ ] **Step 3: Create ModuleEntry (GWT entry point)**

```java
package com.top_logic.react.flow.client.boot;

import com.google.gwt.core.client.EntryPoint;

/**
 * GWT entry point that exports the FlowDiagramClientControl
 * mount function to the global scope.
 */
public class ModuleEntry implements EntryPoint {

    @Override
    public void onModuleLoad() {
        exportMountFunction();
    }

    private native void exportMountFunction() /*-{
        $wnd.GWT_FlowDiagram = {
            mount: function(container, controlId, windowName, contextPath, diagramJson) {
                return @com.top_logic.react.flow.client.control.FlowDiagramClientControl::mount(
                    Lelemental2/dom/HTMLDivElement;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;
                )(container, controlId, windowName, contextPath, diagramJson);
            }
        };
    }-*/;
}
```

- [ ] **Step 4: Create GWT module TLReactFlowClient.gwt.xml**

```xml
<module rename-to='TLReactFlowClient'>
    <inherits name="elemental2.dom.Dom" />
    <inherits name="com.google.gwt.user.User" />
    <inherits name="com.top_logic.react.flow.common.TLReactFlow" />
    <inherits name="de.haumacher.msgbuf.MsgBuf" />
    <inherits name="org.vectomatic.libgwtsvg" />

    <source path='boot' />
    <source path='control' />
    <source path='bridge' />
    <source path='dom' />

    <entry-point class='com.top_logic.react.flow.client.boot.ModuleEntry' />
    <add-linker name="xsiframe" />
</module>
```

- [ ] **Step 5: Verify GWT compilation**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.client
```

- [ ] **Step 6: Commit**

```
Ticket #29108: Create GWT client control with React SSE/Command bridge.
```

---

## Task 7: Create React Lifecycle Wrapper

Create the minimal React component and build configuration.

**Files:**
- Create: `com.top_logic.react.flow.client/src/main/typescript/TLFlowDiagram.tsx`
- Create: `com.top_logic.react.flow.client/src/main/typescript/package.json`
- Create: `com.top_logic.react.flow.client/src/main/typescript/tsconfig.json`
- Create: `com.top_logic.react.flow.client/src/main/typescript/vite.config.ts`

- [ ] **Step 1: Create TLFlowDiagram.tsx**

```typescript
import { React, useTLState, useTLCommand } from 'tl-react-bridge';

interface TLFlowDiagramProps {
    controlId: string;
    state: Record<string, unknown>;
}

/**
 * Minimal React lifecycle wrapper for the GWT-rendered flow diagram.
 *
 * This component renders a <div>, mounts the GWT FlowDiagramClientControl
 * into it on first render, and destroys it on unmount. All diagram logic
 * (model, layout, SVG rendering, interaction) runs in GWT-compiled Java.
 */
function TLFlowDiagram({ controlId, state }: TLFlowDiagramProps) {
    const ref = React.useRef<HTMLDivElement>(null);
    const controlRef = React.useRef<any>(null);

    React.useEffect(() => {
        const div = ref.current;
        if (!div) return;

        const gwtApi = (window as any).GWT_FlowDiagram;
        if (!gwtApi) {
            console.error('[TLFlowDiagram] GWT_FlowDiagram not loaded');
            return;
        }

        const windowName = document.body.dataset.windowName || 'main';
        const contextPath = document.body.dataset.contextPath || '';
        const diagramJson = typeof state.diagram === 'string'
            ? state.diagram
            : JSON.stringify(state.diagram);

        controlRef.current = gwtApi.mount(
            div, controlId, windowName, contextPath, diagramJson
        );

        return () => {
            if (controlRef.current) {
                controlRef.current.destroy();
                controlRef.current = null;
            }
        };
    }, [controlId]);

    return <div ref={ref} style={{ width: '100%', height: '100%' }} />;
}

export default TLFlowDiagram;
```

- [ ] **Step 2: Create package.json, tsconfig.json, vite.config.ts**

Follow the pattern from `com.top_logic.layout.react/src/main/typescript/` for the
build configuration. The Vite build should produce a single JS file that registers
`TLFlowDiagram` as a React module.

Reference `com.top_logic.layout.react/react-src/bridge/registry.ts` for how modules
are registered — the component needs to call `register('TLFlowDiagram', TLFlowDiagram)`.

- [ ] **Step 3: Configure frontend-maven-plugin in client pom.xml**

Add the `frontend-maven-plugin` configuration to build TypeScript during `mvn compile`.
Reference the existing configuration in `com.top_logic.layout.react/pom.xml`.

- [ ] **Step 4: Verify full build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server,com.top_logic.react.flow.client
```

- [ ] **Step 5: Commit**

```
Ticket #29108: Create TLFlowDiagram React lifecycle wrapper with Vite build.
```

---

## Task 8: Create UIElement for View Integration

Create the `<flow-diagram>` UIElement that plugs into the View framework.

**Files:**
- Create: `com.top_logic.react.flow.server/.../ui/FlowDiagramElement.java`

- [ ] **Step 1: Create FlowDiagramElement**

```java
package com.top_logic.react.flow.server.ui;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.UIElement;
import com.top_logic.react.flow.server.control.FlowDiagramControl;

/**
 * UIElement for embedding a flow diagram in the React View framework.
 *
 * <p>
 * Usage in view.xml:
 * </p>
 * <pre>
 * &lt;flow-diagram builder="com.example.MyFlowChartBuilder" /&gt;
 * </pre>
 */
@TagName("flow-diagram")
public class FlowDiagramElement implements UIElement {

    public interface Config extends UIElement.Config {

        @Name("builder")
        @Mandatory
        PolymorphicConfiguration<FlowChartBuilder> getBuilder();
    }

    private final FlowChartBuilder _builder;

    public FlowDiagramElement(InstantiationContext context, Config config) {
        _builder = context.getInstance(config.getBuilder());
    }

    @Override
    public IReactControl createControl(ViewContext context) {
        Diagram diagram = _builder.createDiagram(context);
        return new FlowDiagramControl(context, diagram);
    }
}
```

Note: The exact `FlowChartBuilder` API for the View context may need adaptation.
The existing `FlowChartBuilder.getModel(Object, LayoutComponent)` takes a
`LayoutComponent` which does not exist in the View framework. This will need a
new builder interface or adapter — keep it simple for now and iterate.

- [ ] **Step 2: Verify build**

```bash
mvn install -DskipTests=true -pl com.top_logic.react.flow.server
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Create FlowDiagramElement UIElement for View framework integration.
```

---

## Task 9: Integration Test with Demo View

Create a simple test view that renders a flow diagram to validate the entire stack.

**Files:**
- Create: test view.xml in `com.top_logic.react.flow.server`
- Create: a simple `TestFlowChartBuilder` for testing

- [ ] **Step 1: Create a simple test builder**

```java
package com.top_logic.react.flow.server.ui;

/**
 * Simple flow chart builder for integration testing.
 * Creates a small tree diagram with three nodes.
 */
public class TestFlowChartBuilder implements FlowChartBuilder {
    // Create a Diagram with:
    // - Root TreeLayout
    // - Three nodes: "Parent" → "Child A", "Child B"
    // - Each node: Border(Fill(Padding(Text)))
    // - TreeConnections between parent and children
}
```

- [ ] **Step 2: Create test view.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <panel title="Flow Diagram Test">
    <flow-diagram builder="com.top_logic.react.flow.server.ui.TestFlowChartBuilder" />
  </panel>
</view>
```

- [ ] **Step 3: Test manually**

Start the demo app with the test view and verify:
1. The `<div>` is rendered by React
2. The GWT code mounts and renders SVG
3. The tree diagram appears with three nodes
4. Click on a node triggers selection
5. Pan (drag) and zoom (Ctrl+wheel) work
6. Browser console shows no errors

- [ ] **Step 4: Commit**

```
Ticket #29108: Add integration test view for flow diagram validation.
```

---

## Task 10: Wire Selection Channel

Connect the diagram's selection to a View channel so other components
(forms, tables) can react to diagram selection.

**Files:**
- Modify: `FlowDiagramElement.java` — add selection channel config
- Modify: `FlowDiagramControl.java` — push selection changes to channel

- [ ] **Step 1: Add selection channel to FlowDiagramElement Config**

Add a `selection` channel reference to the Config interface.
When the GWT client sends a `selection` command, the `FlowDiagramControl`
updates the channel value.

- [ ] **Step 2: Handle selection command in FlowDiagramControl**

The `@ReactCommand("selection")` handler resolves the selected user object
and writes it to the selection channel.

- [ ] **Step 3: Test with a view that shows selection**

```xml
<view>
  <channels>
    <channel name="selected" />
  </channels>
  <split-panel>
    <start>
      <flow-diagram builder="..." selection="selected" />
    </start>
    <end>
      <panel title="Selected">
        <!-- Show selected object -->
      </panel>
    </end>
  </split-panel>
</view>
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Wire diagram selection to View channel.
```

---

## Summary

| Task | Description | Estimated Complexity |
|------|-------------|---------------------|
| 1 | Maven module structure | Low — boilerplate |
| 2 | Copy shared model + operations | Medium — bulk copy with package rename |
| 3 | Copy server-side components | Medium — bulk copy with package rename |
| 4 | JsInterop bridge | Low — two small classes |
| 5 | Adapt server control to ReactControl | High — key architectural change |
| 6 | Adapt GWT client control | High — communication layer rewrite |
| 7 | React lifecycle wrapper | Low — one small TSX file |
| 8 | UIElement for View integration | Medium — new integration point |
| 9 | Integration test | Medium — end-to-end validation |
| 10 | Selection channel wiring | Medium — channel integration |
