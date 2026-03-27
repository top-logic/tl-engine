# Model Editor React Migration — Design Discussion

**Date**: 2026-03-24 (updated 2026-03-27)
**Status**: In discussion — evaluating integration strategy
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
Text, Image, layouts) and a **msgbuf-based data model** (`data.proto`). Renders to SVG,
supports selection, click, drag-drop, and context menus. Currently used for flow charts,
tree hierarchies, and custom model visualizations.

### Key Differences Between the Two Libraries

| Aspect | DiagramJS | Graphic Blocks |
|---|---|---|
| Data model | SharedObject (Java reflection) | msgbuf-generated (`data.proto`) |
| Rendering | External diagram-js library | Own SVG rendering (SvgWriter) |
| Layout | Sugiyama (hierarchical graph) | Tree, HBox, VBox, Grid, Floating |
| Interaction | Full editor (create, delete, move, resize, connect) | Selection, click, drop, context menu |
| Topology | Arbitrary graphs | Trees / hierarchies |
| Widget system | Fixed node types (ClassNode, Edge, Label) | Composable primitives (Border, Fill, Text, ...) |
| Protocol | ObjectScope/ChangeIO (JSON) | msgbuf SharedGraph (JSON) |

### DiagramJS Communication Flows (Current)

The system uses **two distinct communication directions** with different mechanisms:

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
5. Client: `ChangeIO.readChanges()` → `ObjectScope.update()` → `DefaultGraphScopeListener` → diagram-js canvas update

### Detailed Interaction Flows (DiagramJS)

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

#### 6. Resize Node

```
diagram-js: SHAPE_RESIZE_EVENT (via CommandStack POST_EXECUTED)
  → ShapeResizeEventHandler: reads new bounds (x, y, width, height)
  → Updates SharedObject properties (local)
  → Same 150ms batched flow as Move
  → No response from server
```

#### 7. Re-layout

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

#### 8. Drop Type from Element Tree

```
DOM: drop event → DropEventListener → DiagramJSGraphControl.onDrop()
  → Extracts drop position (x, y) and dragged data
  → AJAX: GRAPH_DROP_COMMAND({x, y, data})
  → Server: processes drop
    → getOrCreateGraphPart(tlType) — creates node if not already in graph
    → Sets position to drop coordinates
  → Response: UPDATE_CLIENT_GRAPH_COMMAND with new node
```

#### 9. Toggle Visibility

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

### Key Architectural Properties

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

Both patterns use the **same technique**: a temporary SVG element follows the mouse. The
only difference is whether it is a copy of a shape (node drag) or a polyline (edge draw).

## Decisions Made

### 1. Full Interactive Editor (not read-only)
All current features must be supported: create/delete classes, enumerations, references,
inheritance; drag-drop from element tree; visibility toggles; re-layout; selection linking.

### 2. Server Holds Authoritative Graph State
- Changes come in as commands, server updates the model and pushes deltas
- **Exception**: Drag/move operations are client-local, batched to server on mouse-up
  (fire-and-forget, no confirmation needed for positions)

### 3. Auto-Layout (Sugiyama) Remains Server-Side
- Initial layout and explicit re-layout computed on server
- Client renders calculated positions

### 4. UIElement + Channel Integration
The diagram is a `<model-graph>` UIElement that communicates via View channels:

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

The complete model editor is composed from this building block as one or more `.view.xml` files.

## Integration Strategy — Options

### Option A: GWT Widget Embedded in React (Pragmatic)

Embed the existing GWT-compiled diagram widget inside a React component. The GWT code
handles diagram-js interaction and ObjectScope synchronization as before. The React wrapper
provides View framework integration (channels, commands).

```
React Wrapper (thin)
  ├─ Mounts GWT widget via useRef
  ├─ Bridges Selection events ↔ View channels
  ├─ Bridges Module changes ↔ GWT widget
  └─ GWT Widget (existing code, unchanged)
       ├─ diagram-js canvas (SVG)
       ├─ ObjectScope (client-side, GWT-compiled Java)
       ├─ ChangeIO (GWT-compiled Java)
       ├─ Event handlers → AJAX commands
       └─ DefaultGraphScopeListener → canvas updates
```

**Advantages:**
- Single-source-of-truth preserved (same Java code on client and server)
- No rewrite of client logic (~28 GWT Java files, ~2000 lines)
- ObjectScope/ChangeIO synchronization unchanged
- Minimal risk — proven code stays intact
- No msgbuf dependency for the initial integration

**Disadvantages:**
- GWT build toolchain remains (slow, aging)
- Two JS runtimes in one page (React + GWT)
- Bridge layer between React and GWT adds complexity
- Not fully "native" in the View framework
- diagram-js remains as external dependency

### Option B: Pure React/TypeScript Rewrite (Clean, blocked)

Replace the GWT client entirely with TypeScript, keeping diagram-js for rendering.
Requires msgbuf features for clean protocol transport.

**Advantages:**
- Clean, modern tech stack
- No GWT dependency
- Native View framework integration

**Disadvantages:**
- Requires rewriting ~2000 lines of client logic in TypeScript
- Loses single-source-of-truth (Java model on server, TS model on client)
- Blocked by msgbuf features (cross-file extension, TS codegen, native JSON)
- diagram-js remains as external dependency

### Option C: GWT for Model/Protocol Only (Hybrid)

Compile only the SharedObject/ObjectScope/ChangeIO/Model classes via GWT as a small JS
library. The diagram-js interaction and display logic are rewritten in TypeScript and use
this library for synchronization.

**Advantages:**
- Single-source-of-truth for model and protocol
- diagram-js interaction is native TypeScript (cleaner than GWT JsInterop)
- Smaller GWT footprint (data logic only, no UI)

**Disadvantages:**
- GWT build still required (though smaller)
- Display logic rewrite (~640 lines DisplayGraphPartCreator + event handlers)
- Hybrid approach may be confusing
- diagram-js remains as external dependency

### Option D: Extend Graphic Blocks for UML (Replace diagram-js)

Extend the existing Graphic Blocks library to support UML class diagrams, replacing
diagram-js entirely. Add Sugiyama layout and interactive edge-drawing.

**Advantages:**
- One diagram technology instead of two
- msgbuf already handles the protocol
- Sugiyama layout becomes generally available

**Disadvantages:**
- Significant extension work needed
- Still GWT-based on the client side
- Proven diagram-js UX must be rebuilt

### Option E: Unified React Diagram Library (Recommended)

Create a **new, unified diagram library for the React UI** that cherry-picks from both
existing libraries. When the React UI replaces the old GWT-based UIs, both old libraries
can be removed.

#### What to take from Graphic Blocks

- **Composable widget primitives**: Border, Fill, Padding, Text, Image, HBox, VBox, Grid
- **msgbuf-based data model**: `.proto` definitions for type-safe serialization
- **SVG rendering pipeline**: SvgWriter abstraction

UML class nodes are composed from these primitives — no special UML widget type needed:

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

#### What to take from DiagramJS

- **Sugiyama layout algorithm** (from `tl-graph-layouter`)
- **Edge types and rendering**: inheritance arrows, composition diamonds, association lines
- **Edge labels**: cardinality, role names
- **Interactive edge-drawing**: SVG polyline following mouse with target highlighting

#### What to build new (React-native)

- **React SVG component** rendering the diagram from msgbuf model
- **Edge-drawing interaction** directly in React/SVG events:

  ```typescript
  function DiagramSVG({ model }) {
      const [drawing, setDrawing] = useState<EdgeDraft | null>(null);

      return (
          <svg onMouseMove={e => drawing && updateDraft(e)}>
              {model.nodes.map(n => <ClassNode key={n.id} onConnectStart={...} />)}
              {model.edges.map(e => <UmlEdge key={e.id} ... />)}

              {/* Temporary line during edge drawing */}
              {drawing && (
                  <line x1={drawing.from.x} y1={drawing.from.y}
                        x2={drawing.to.x} y2={drawing.to.y}
                        strokeDasharray="4" stroke="#333" pointerEvents="none" />
              )}
          </svg>
      );
  }
  ```

- **Context-menu-based node creation**: right-click on canvas → "New Class" / "New Enum"
  → node appears at click position
- **Selection, click, drop handlers** via React event system
- **SSE integration** via msgbuf protocol for server push

#### What to leave out (v1)

- **Node move/resize**: Positions are not persisted, auto-layout determines placement.
  Re-layout on demand via toolbar button.
- **Waypoint editing**: Edge routing is computed by the layout algorithm.

#### Interaction design (v1)

| Interaction | Mechanism |
|---|---|
| Select node/edge | Click → React onClick → update selection channel |
| Create class/enum | Context menu on canvas → server command → re-layout at position |
| Create connection | Drag from node edge → SVG polyline follows mouse → drop on target |
| Delete element | Context menu or keyboard → server command |
| Re-layout | Toolbar button → server re-computes Sugiyama → full state push |
| Toggle visibility | Toolbar toggle → server filters model → state push |
| Drop from tree | HTML5 drag-drop → server command → node at drop position |

#### Architecture

```
                        View XML (.view.xml)
                              │
                    ┌─────────┴─────────┐
                    │   <model-graph>    │
                    │   UIElement        │
                    └─────────┬─────────┘
                              │ createControl()
                    ┌─────────┴──────────────┐
                    │ DiagramControl          │
                    │ (ReactControl)          │
                    │                         │
                    │ Diagram model (msgbuf)  │
                    │ Sugiyama layout         │
                    │ TLModule → Graph logic  │
                    └────┬──────────────┬────┘
                   SSE   │              │  HTTP Commands
             (msgbuf)    │              │  (msgbuf)
                         ▼              ▲
                    ┌────────────────────────┐
                    │  React Component (TS)  │
                    │                        │
                    │  Own SVG rendering     │
                    │  Composed from         │
                    │  widget primitives     │
                    │                        │
                    │  Edge-drawing (React)  │
                    │  Selection (React)     │
                    │  Context menus (React) │
                    └────────────────────────┘
```

#### Module structure

```
com.top_logic.layout.diagram              — NEW: unified diagram library
  src/main/java/                          — Server: msgbuf model, Sugiyama, ReactControl
  src/main/java/.../protocol/diagram.proto — msgbuf protocol definition
  src/main/typescript/                    — React SVG component, edge-drawing, interactions
```

Both old libraries (`graphic.blocks.*` and `graph.diagramjs.*`) remain operational for
the legacy GWT UI and are removed when the React UI fully replaces it.

#### Lifecycle

```
Phase 1 (now):
  graphic.blocks.* + graph.diagramjs.*  →  old GWT UI (unchanged)
  com.top_logic.layout.diagram          →  new React UI (new development)

Phase 2 (React UI complete):
  Remove graphic.blocks.client, graph.diagramjs.client  (GWT modules)
  Remove ext.io.bpmn.bpmn-js dependency (diagram-js)

Phase 3 (cleanup):
  Evaluate if graphic.blocks server-side code can be consolidated
```

#### Dependencies on msgbuf

Option E still benefits from the msgbuf feature requests, but is **less blocked** than
Option B:

- **Cross-file protocol extension**: Needed for clean SSE event types. Can be worked around
  initially by defining diagram events in the base `sse.proto` (temporary coupling).
- **TypeScript code generation**: Would eliminate hand-written TS serialization. Can be
  hand-written initially (the `data.proto` model is well-defined).
- **Native JSON value type**: Nice-to-have, not blocking.

The msgbuf data model (`data.proto` from Graphic Blocks) already demonstrates that msgbuf
works well for diagram data. The new library extends this pattern.

## msgbuf Feature Requests

Three feature requests have been prepared (see `msgbuf-feature-requests.md`):

1. **Cross-file protocol extension** ([msgbuf/msgbuf](https://github.com/msgbuf/msgbuf))
   — `extends` across `.proto` file boundaries with polymorphic dispatch
2. **TypeScript/JavaScript code generation** — generate TS classes with serialization
   and type-tag dispatch from `.proto` files
3. **Native JSON value type** — `json` field type that embeds structured data without
   string-wrapping

## Open Items

- [ ] Decide on integration strategy (Option A through E)
- [ ] If Option E: define the `.proto` model for UML diagrams (extend `data.proto` patterns)
- [ ] If Option E: prototype SVG rendering of a UML class node from widget primitives
- [ ] If Option E: prototype interactive edge-drawing in React/SVG
- [ ] Design the Sugiyama integration for the new module
- [ ] Channel bindings (module, selection, editMode)
- [ ] Toolbar and context menu integration in View framework
- [ ] Detail editor views (TypeEditor, AttributeEditor, etc.) as `.view.xml`
- [ ] Test strategy
- [ ] msgbuf feature request timeline
