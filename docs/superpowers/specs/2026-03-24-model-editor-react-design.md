# Model Editor React Migration — Design

**Date**: 2026-03-24 (updated 2026-03-27)
**Status**: Design finalized — ready for implementation planning
**Ticket**: #29108

## Goal

Migrate the TopLogic Model Editor (UML graph visualization with auto-layout) from the
GWT-based architecture to the new View-based React UI framework (`com.top_logic.layout.view`).
The migrated editor must support the full interactive feature set of the current implementation.

## Current Architecture

### Existing Diagram Libraries

TopLogic currently has **two separate diagram libraries**, both GWT-based:

#### 1. DiagramJS Library (UML Model Editor)

```
com.top_logic.graph.diagramjs          — Shared model (SharedObject-based)
com.top_logic.graph.diagramjs.server   — Server logic (Sugiyama layout, commands, validation)
com.top_logic.graph.diagramjs.client   — GWT client (compiles Java to JS, diagram-js rendering)
```

Uses the external **diagram-js** library (MIT, bundled in `ext.io.bpmn.bpmn-js`) for SVG
rendering. Provides full interactive editing of UML class diagrams.

#### 2. Graphic Blocks Library (Flow/Tree Diagrams)

```
com.top_logic.graphic.blocks           — Core: msgbuf data model, SVG rendering, layout
com.top_logic.graphic.blocks.client    — GWT client: SVG DOM construction (SVGBuilder)
com.top_logic.graphic.blocks.server    — Server: FlowChartComponent, layout, event handling
```

A declarative diagram engine with **composable widget primitives** (Border, Fill, Padding,
Text, Image, layouts) and a **msgbuf-based data model** (`data.proto`). Renders to SVG
via native browser APIs (no GWT widgets), supports selection, click, drag-drop, and
context menus.

#### Key Differences

| Aspect | DiagramJS | Graphic Blocks |
|---|---|---|
| Data model | SharedObject (Java reflection) | msgbuf-generated (`data.proto`) |
| Rendering | External diagram-js library | Own SVG rendering (SVGBuilder, native APIs) |
| Layout | Sugiyama (hierarchical graph) | Tree, HBox, VBox, Grid, Floating |
| Interaction | Full editor (create, delete, move, resize, connect) | Selection, click, drop, context menu |
| Topology | Arbitrary graphs | Trees / hierarchies |
| Widget system | Fixed node types (ClassNode, Edge, Label) | Composable primitives (Border, Fill, Text, ...) |
| Protocol | ObjectScope/ChangeIO (JSON) | msgbuf SharedGraph (JSON) |

### DiagramJS Communication Flows (Current — Legacy)

#### Client → Server (3 variants)

| Variant | Used For | Mechanism |
|---|---|---|
| `dispatchControlCommand` | Create class/enum, create connection, delete, visibility, goto | AJAX with command name + typed arguments |
| `UPDATE_SERVER_GRAPH_COMMAND` | Move node, resize node | `ObjectScope.popChanges()` → `ChangeIO.writeChanges()` → AJAX (150ms timer batching) |
| No communication | Selection (click) | Local SharedObject update only |

#### Server → Client (1 mechanism)

All server responses use **`UPDATE_CLIENT_GRAPH_COMMAND`**:
1. Server modifies `SharedGraph` (ObjectScope-tracked)
2. `AbstractGraphControl.internalRevalidate()` detects pending changes
3. `ObjectScope.popChanges()` → `ChangeIO.writeChanges()` → JSON
4. `JSFunctionCall` sends JSON to client
5. Client: `ChangeIO.readChanges()` → `ObjectScope.update()` → `DefaultGraphScopeListener`
   → diagram-js canvas update

### Detailed Interaction Flows (DiagramJS — for reference)

#### 1. Click on Node (Selection)

```
diagram-js: ELEMENT_CLICKED_EVENT
  → ClickEventHandler: reads element, resolves SharedGraphPart
  → graphModel.setSelectedGraphParts([part])  (local SharedObject update)
  → No AJAX command sent
  → Server: DiagramJSGraphComponent.onSelectionChange() updates component selection
```

#### 2. Create Class

```
diagram-js: CREATE_CLASS_EVENT
  → CreateClassEventHandler: extracts bounds (x, y, width, height)
  → AJAX: dispatchControlCommand("createClass", {x, y, width, height})
  → Server: CreateClassCommand.execute()
    → DiagramJSGraphComponent.createClass(bounds)
    → Opens modal dialog for type name entry
    → On confirm: creates TLClass in KB transaction
    → handleTLObjectCreations() → GraphModelUtil.createGraphPart()
    → ObjectScope records CREATE
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with new node
  → Client: ObjectScope.update() → DefaultGraphScopeListener.createDisplayGraphPart()
    → DisplayGraphPartCreator.createNode() → diagram-js Shape added to canvas
```

#### 3. Create Connection (Reference/Inheritance)

```
diagram-js: DRAG_CONNECT_END_EVENT
  → CreateConnectionEventHandler: extracts type, sourceID, targetID
  → AJAX: dispatchControlCommand("createConnection", {type, sourceID, targetID})
  → Server: CreateConnectionGraphCommand.execute()
    → If type="inheritance": validates no cyclic inheritance, adds to TLClass.generalizations
    → If type="reference": opens dialog for reference configuration
    → GraphModelUtil.createDiagramJSEdge() creates edge in SharedGraph
    → ObjectScope records CREATE
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with new edge
  → Client: DisplayGraphPartCreator.createEdge() → diagram-js Connection on canvas
```

#### 4. Delete Graph Part

```
diagram-js: DELETE_ELEMENT_EVENT
  → DeleteGraphPartEventHandler: extracts SharedObject ID
  → AJAX: dispatchControlCommand("deleteGraphPart", {id})
  → Server: DeleteGraphPartCommand.execute()
    → Shows confirmation dialog
    → On confirm: if TLInheritance → GraphModelUtil.deleteInheritance()
                   if TLModelPart → TLModelPartDeleteHandler (cascade delete)
    → GraphModelUtil.removeGraphParts() removes from SharedGraph
    → ObjectScope records DELETE
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with deletes
  → Client: DefaultGraphScopeListener.deleteDisplayGraphPart()
    → diagram-js Modeler.removeElement()
```

#### 5. Move Node (Drag)

```
diagram-js: ELEMENTS_MOVE_EVENT (via CommandStack POST_EXECUTED)
  → ElementsMoveEventHandler: reads new shape.x, shape.y
  → Updates DefaultDiagramJSClassNode.x/y in SharedObject (local)
  → ObjectScope marks x, y as changed
  → 150ms timer fires → sendChangesToServer()
    → ObjectScope.popChanges() → ChangeIO.writeChanges()
    → AJAX: UPDATE_SERVER_GRAPH_COMMAND({graphUpdate: changesJSON})
  → Server: UpdateGraphCommand.execute()
    → ChangeIO.readChanges() → ObjectScope.update() (applies x, y)
    → No response sent (client is authoritative for positions)
```

#### 6. Re-layout

```
Toolbar button click → RelayoutGraphCommand.handleCommand()
  → DiagramJSGraphComponent.resetGraphModel()
    → Saves current selection
    → createSharedGraphModel(module) → full Sugiyama layout recalculation
    → Restores selection on new graph
    → setGraphModel(newGraph) replaces entire graph
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with complete graph (all creates)
  → Client: Full canvas rebuild via DefaultGraphScopeListener
```

#### 7. Drop Type from Element Tree

```
DOM: drop event → DropEventListener → DiagramJSGraphControl.onDrop()
  → Extracts drop position (x, y) and dragged data
  → AJAX: GRAPH_DROP_COMMAND({x, y, data})
  → Server: processes drop
    → getOrCreateGraphPart(tlType) — creates node if not already in graph
    → Sets position to drop coordinates
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with new node
```

#### 8. Toggle Visibility

```
diagram-js: ELEMENTS_VISIBILITY_EVENT
  → ElementVisibilityEventHandler: extracts IDs and visibility flag
  → AJAX: dispatchControlCommand("elementsVisibility", {ids, visibility})
  → Server: ElementsVisibilityCommand.execute()
    → DiagramJSGraphComponent.setElementsVisibility()
    → If hiding and showHiddenElements=false: GraphModelUtil.removeGraphParts()
    → ObjectScope records DELETE or UPDATE
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with deletes or updates
```

### Key Architectural Properties (Current)

1. **ObjectScope is bidirectional** — the same mechanism (`popChanges`/`ChangeIO`) works in
   both directions, providing a single-source-of-truth for the graph model in Java
2. **Move/Resize are fire-and-forget** — client is authoritative for positions, no server echo
3. **Create/Delete go through dialogs** — the AJAX command opens a dialog; the actual object
   is created only on confirmation
4. **The response is always a Changes object** — creates, updates, deletes as a batch
5. **150ms timer batching** — move/resize changes are coalesced to reduce network traffic
6. **Replay mechanism** — `ObjectScope.startReplay()` prevents infinite loops when applying
   received updates

### How diagram-js Renders (Pure SVG)

diagram-js is **entirely SVG-based** — no Canvas overlay. Key rendering layers within a
single `<svg>` element:

- `.djs-visual` — visible representation (rects, paths, text)
- `.djs-hit` — invisible hit areas for pointer events (larger than visual)
- `.djs-outline` — dashed outline on selection/hover
- `.djs-dragger` — temporary element copy during drag operations

**Edge-Drawing Pattern:**
1. User starts drag from a node
2. A temporary SVG `<polyline>` is created (dashed, `pointer-events: none`)
3. On `mousemove`: polyline coordinates update (line follows mouse)
4. On hover over potential targets: CSS markers `.connect-ok` / `.connect-not-ok`
5. On `mouseup` on target: polyline removed, real connection created

**Node-Dragging Pattern:**
1. A "dragger" SVG group is created with a copy of the visual element
2. Original becomes semi-transparent (`.djs-dragging`, opacity 0.3)
3. Dragger group follows mouse via SVG transform
4. On `mouseup`: original moves to new position, dragger removed

Both patterns use the **same technique**: a temporary SVG element follows the mouse.

## Design Decision: Unified GWT Diagram Library with React Integration

### Approach

Create a **new, unified diagram library** that combines the best of both existing libraries.
The library is written entirely in **Java**, compiled to JavaScript via **GWT** for
client-side execution. It integrates into the React View framework via a **minimal React
lifecycle wrapper** that provides the communication channel (SSE + Commands).

**Core principle**: GWT handles all substantive work (model, layout, SVG rendering,
interaction). React provides only the communication infrastructure and lifecycle management.
No diagram logic in TypeScript.

### Why GWT for Everything (Not TypeScript)

1. **Single source of truth** — same Java code runs on server (JVM) and client (GWT→JS).
   No duplicate implementations in two languages.
2. **Layout must run on both sides** — server needs layout for PDF export, client needs
   layout for smooth animations and interactive edge routing. GWT is the only way to
   share the implementation.
3. **Existing infrastructure** — Graphic Blocks already has a complete SVG rendering
   pipeline via native browser APIs (`SVGBuilder`), not GWT widgets. This is proven code.
4. **Sugiyama only once** — the layout algorithm exists in Java (`tl-graph-layouter`).
   Rewriting it in TypeScript would be wasteful and error-prone.

### What to Take from Each Library

**From Graphic Blocks:**
- Composable widget primitives (Border, Fill, Padding, Text, Image, layouts)
- msgbuf-based data model (`.proto` definitions)
- SVG rendering pipeline (`SVGBuilder`, native browser APIs)
- Layout engine (intrinsic size computation, size distribution)
- Event handling infrastructure (click, drop, selection, context menu)
- Text metrics (client-side: `JSTextMetrics`, server-side: `AWTContext`)

**From DiagramJS:**
- Sugiyama layout algorithm (`tl-graph-layouter`)
- Edge types and rendering (inheritance arrows, composition diamonds, associations)
- Edge labels (cardinality, role names)
- Interactive edge-drawing (SVG polyline + mouse tracking + target highlighting)
- UML business logic (TLType operations, validation, cyclic inheritance check)

UML class nodes are composed from widget primitives — no special UML widget needed:

```
ClassNode =
  Border(stroke: black,
    VerticalLayout(
      Fill(bg: header-color,
        Padding(
          VerticalLayout(
            Text(stereotype, italic),     // «enumeration»
            Text(className, bold),        // MyClass
            Text(modifiers, small)        // {abstract}
          )
        )
      ),
      Separator(line),
      Padding(
        VerticalLayout(                   // Properties compartment
          Text("name : String"),
          Text("age : Integer"),
        )
      )
    )
  )
```

### Architecture

```
Server (Java / JVM):
  ┌──────────────────────────────┐
  │ DiagramControl (ReactControl)│
  │                              │
  │ TLModule → Diagram model     │
  │ Sugiyama layout              │
  │ Widget tree (msgbuf)         │
  │ @ReactCommand handlers      │
  │ PDF export (same layout code)│
  └──────┬───────────────┬──────┘
    SSE  │               │  HTTP POST /react-api/command
  (msgbuf│events)        │  (msgbuf commands)
         ▼               ▲
  ┌──────────────────────────────┐
  │ GWT-compiled Java → JS      │
  │                              │
  │ msgbuf model (same code)     │
  │ Layout engine (same code)    │
  │ SVGBuilder → SVG DOM         │
  │ Edge-drawing interaction     │
  │ Selection, Click, Drop       │
  │ Context menus                │
  │                              │
  │ subscribe() ← SSE events    │
  │ fetch() → Commands           │
  └──────────────────────────────┘
         mounted by
  ┌──────────────────────────────┐
  │ TLDiagram.tsx (minimal)      │
  │                              │
  │ <div ref={ref} />            │
  │ useEffect → GWT.mount()      │
  │ cleanup → GWT.destroy()      │
  └──────────────────────────────┘
```

### Communication via React Infrastructure

The GWT-compiled code communicates through the React framework's SSE/Command infrastructure
instead of legacy AJAX servlets:

#### Server → Client (SSE)

```java
// Server: DiagramControl pushes updates via SSE
SSEUpdateQueue queue = getSSEQueue();
queue.enqueue(myEvent);  // msgbuf-serialized event
```

```java
// Client (GWT): subscribes to SSE events via global API
// (GWT JsInterop calling into tl-react-bridge)
TLReact.subscribe(controlId, new StateListener() {
    void onState(JsonObject state) {
        // Apply changes to local model, re-render SVG
    }
});
```

#### Client → Server (Commands)

```java
// Client (GWT): sends commands via fetch()
// (GWT JsInterop calling fetch API)
JsonObject args = new JsonObject();
args.put("sourceId", sourceId);
args.put("targetId", targetId);
args.put("type", "inheritance");

CommandBridge.send(controlId, "createConnection", args);
// → POST /react-api/command
```

```java
// Server: handles command via @ReactCommand
@ReactCommand("createConnection")
HandlerResult handleCreateConnection(ReactContext ctx, Map<String, Object> args) {
    String sourceId = (String) args.get("sourceId");
    String targetId = (String) args.get("targetId");
    // ... validate, create in KB, update model, push via SSE
}
```

#### No Legacy AJAX Required

The existing `dispatchControlCommand` AJAX mechanism is **not needed**. All communication
flows through the React infrastructure:

| Legacy (AJAX) | New (React SSE/Commands) |
|---|---|
| `dispatchControlCommand` | `fetch('/react-api/command', ...)` |
| `UPDATE_SERVER_GRAPH_COMMAND` | `fetch('/react-api/command', ...)` |
| `JSFunctionCall` → `UPDATE_CLIENT_GRAPH_COMMAND` | `SSEUpdateQueue.enqueue(event)` |

### React Component (Minimal Lifecycle Wrapper)

```typescript
// TLDiagram.tsx — registered as React module "TLDiagram"
import { React } from 'tl-react-bridge';

function TLDiagram({ controlId, windowName }: TLDiagramProps) {
    const ref = React.useRef<HTMLDivElement>(null);

    React.useEffect(() => {
        // GWT entry point — mounts SVG, subscribes to SSE, registers event handlers
        const control = (window as any).GWT_Diagram.mount(
            ref.current, controlId, windowName
        );
        return () => control.destroy();  // Unsubscribe, cleanup SVG
    }, [controlId, windowName]);

    return <div ref={ref} style={{ width: '100%', height: '100%' }} />;
}
```

This is the **only TypeScript file** in the diagram module. Everything else is Java.

### View Integration

The diagram is a `<model-graph>` UIElement communicating via View channels:

```xml
<view>
  <channels>
    <channel name="module" />
    <channel name="selection" />
  </channels>
  <split-panel>
    <start>
      <model-graph module="module" selection="selection" />
    </start>
    <end>
      <form input="selection">
        <field attribute="name" />
      </form>
    </end>
  </split-panel>
</view>
```

### Module Structure

```
com.top_logic.layout.diagram                — NEW: unified diagram library
  src/main/java/
    com/top_logic/layout/diagram/
      model/                                — msgbuf widget model (from graphic.blocks)
      layout/                               — Layout engines (Sugiyama + box layouts)
      render/                               — SVG rendering (from graphic.blocks SVGBuilder)
      interaction/                          — Edge-drawing, selection, drop, context menu
      uml/                                  — UML-specific: class node builder, edge types
      server/                               — ReactControl, command handlers
      bridge/                               — JsInterop bridge to React SSE/Commands
      protocol/diagram.proto                — msgbuf protocol definition
  src/main/java/.../TLDiagram.gwt.xml       — GWT module (model + layout + render + interaction)
  src/main/typescript/
    TLDiagram.tsx                           — Minimal React lifecycle wrapper (only file)
```

### Interaction Design (v1)

| Interaction | Mechanism |
|---|---|
| Select node/edge | Click on SVG element → GWT handler → update selection → command to server |
| Create class/enum | Context menu on canvas → command to server → dialog → model update → SSE push |
| Create connection | Drag from node → SVG polyline follows mouse → drop on target → command |
| Delete element | Context menu or keyboard → command to server → confirmation → SSE push |
| Re-layout | Toolbar button → command to server → Sugiyama recalculation → SSE push |
| Toggle visibility | Toolbar toggle → command → server filters model → SSE push |
| Drop from tree | HTML5 drag-drop → command → server creates node → SSE push |

### v1 Scope

**Included:**
- Full UML class diagram visualization with Sugiyama auto-layout
- Interactive edge-drawing (reference, inheritance)
- Node creation/deletion via context menu
- Selection with channel binding to detail editors
- Visibility toggles
- Re-layout on demand
- Drop from element tree

**Excluded (can be added later):**
- Node move/resize (positions not persisted, auto-layout determines placement)
- Waypoint editing (edge routing computed by layout algorithm)
- Smooth re-layout animations (layout runs on server initially; client-side layout
  enables animations in a future version)

### Lifecycle

```
Phase 1 (now):
  graphic.blocks.* + graph.diagramjs.*  →  old GWT UI (unchanged)
  com.top_logic.layout.diagram          →  new React UI (new development)

Phase 2 (React UI complete):
  Remove graph.diagramjs.client (GWT diagram-js wrapper)
  Remove ext.io.bpmn.bpmn-js dependency (diagram-js library)

Phase 3 (cleanup):
  Consolidate graphic.blocks into layout.diagram where applicable
  Remove graphic.blocks.client
```

### Dependencies on msgbuf

The new library uses msgbuf for the data model (like Graphic Blocks), which works today.

For SSE event transport, the msgbuf feature requests would help but are **not blocking**:

- **Cross-file protocol extension**: Diagram-specific SSE events would ideally be defined
  in the diagram module's own `.proto`. Until then, use `FunctionCall` or add events
  temporarily to `sse.proto`.
- **TypeScript code generation**: Not needed — client-side code is GWT (Java), not TypeScript.
  msgbuf already generates Java. Only relevant for a future non-GWT client.
- **Native JSON value type**: Nice-to-have for cleaner wire format.

## msgbuf Feature Requests

Three feature requests have been prepared (see `msgbuf-feature-requests.md`). These are
valuable for the broader platform but do not block the diagram library development:

1. **Cross-file protocol extension** ([msgbuf/msgbuf](https://github.com/msgbuf/msgbuf))
   — `extends` across `.proto` file boundaries with polymorphic dispatch
2. **TypeScript/JavaScript code generation** — generate TS classes with serialization
   and type-tag dispatch from `.proto` files
3. **Native JSON value type** — `json` field type that embeds structured data without
   string-wrapping

## Implementation Phases

### Phase 1: Flow Diagram in React (Validation)

Port the existing Graphic Blocks flow diagram into a React-compatible new module group.
This is a **1:1 copy with minimal changes** to validate the GWT ↔ React communication
layer. No new features, no UML — just the existing flow/tree diagram functionality
working in the React View framework.

**Goal**: Prove that GWT-rendered SVG diagrams work within the React UI, communicating
via SSE + Commands instead of legacy AJAX.

**New modules:**

```
com.top_logic.react.flow.common    — msgbuf model, layout, SVG rendering (GWT-compatible)
com.top_logic.react.flow.server    — ReactControl, @ReactCommand handlers
com.top_logic.react.flow.client    — GWT client: SVGBuilder, event handling, SSE/Command bridge
```

**What this validates:**
- [ ] GWT code mounting into React via `TLFlowDiagram.tsx` lifecycle wrapper
- [ ] Server → Client: SSE events received by GWT `subscribe()` callback
- [ ] Client → Server: GWT `fetch()` to `/react-api/command` endpoint
- [ ] `@ReactCommand` handlers dispatching to diagram operations
- [ ] SVG rendering via `SVGBuilder` in the React-managed `<div>`
- [ ] Existing interactions working: selection, click, drop, context menu
- [ ] `<flow-diagram>` UIElement with channel bindings (input, selection)
- [ ] FlowChartBuilder API preserved for application-defined visualizations

**What changes from `graphic.blocks`:**
- Communication layer: AJAX → React SSE/Commands (JsInterop bridge)
- Server control: `FlowChartComponent` → `FlowDiagramControl` (extends `ReactControl`)
- Client control: `JSDiagramControl` → adapted for React mount/unmount lifecycle
- UIElement integration: new `FlowDiagramElement` with `@TagName("flow-diagram")`

**What stays identical:**
- msgbuf data model (`data.proto` — copied or shared)
- Widget primitives (Border, Fill, Padding, Text, Image, layouts)
- SVGBuilder rendering pipeline
- Layout algorithms (Tree, HBox, VBox, Grid, Floating)
- Text metrics (JSTextMetrics client-side, AWTContext server-side)
- Event handling logic (click, drop, selection, context menu)

### Phase 2: UML Model Editor

Extend the validated flow diagram module with UML-specific functionality. This adds
the Sugiyama layout, edge-drawing interaction, UML node composition, and the full
model editor experience.

**Additions:**
- Sugiyama layout algorithm (from `tl-graph-layouter`)
- UML class node composition from widget primitives
- Edge types: inheritance, composition, aggregation, association
- Edge labels: cardinality, role names
- Interactive edge-drawing (SVG polyline + mouse tracking)
- Context-menu-based node creation (class, enumeration)
- TLModule → diagram model builder (from `DiagramJSGraphBuilder`)
- Business logic: cyclic inheritance validation, cascade delete
- `<model-graph>` UIElement with module/selection channels
- Complete model editor as `.view.xml` compositions
- Detail editor views (TypeEditor, AttributeEditor, etc.)

## Open Items

- [ ] Phase 1: Define exact scope of `data.proto` sharing vs. copying
- [ ] Phase 1: Design JsInterop bridge API for SSE subscribe + Command fetch
- [ ] Phase 1: Identify minimal changes needed in GWT client code
- [ ] Phase 1: Create demo view.xml with `<flow-diagram>` element
- [ ] Phase 2: detailed planning (after Phase 1 validation)
