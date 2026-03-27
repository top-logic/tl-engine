# Model Editor React Migration — Design Discussion

**Date**: 2026-03-24 (updated 2026-03-27)
**Status**: In discussion — evaluating integration strategy
**Ticket**: #29108

## Goal

Migrate the TopLogic Model Editor (UML graph visualization with auto-layout) from the
GWT-based architecture to the new View-based React UI framework (`com.top_logic.layout.view`).
The migrated editor must support the full interactive feature set of the current implementation.

## Current Architecture

### Module Structure

```
com.top_logic.graph.diagramjs          — Shared model (SharedObject-based)
com.top_logic.graph.diagramjs.server   — Server logic (Sugiyama layout, commands, validation)
com.top_logic.graph.diagramjs.client   — GWT client (compiles Java to JS, diagram-js rendering)
```

### Key Components

- **DiagramJSGraphComponent** — Main layout component, holds transient `SharedGraph` model
- **DiagramJSGraphControl** (server) — Server-side control, renders initial state, handles AJAX commands
- **DiagramJSGraphControl** (client) — GWT client control, manages diagram-js instance and ObjectScope
- **GraphModelUtil** — Graph construction, Sugiyama layout integration, model manipulation
- **DiagramJSGraphBuilder** — Creates layouted `SharedGraph` from `TLModule`
- **ObjectScope / SharedObject** — Client-server synchronization with automatic patch generation
- **ChangeIO** — JSON serialization of Changes (creates/updates/deletes)
- **DefaultGraphScopeListener** — Client-side scope listener, maps ObjectScope events to diagram-js operations
- **DisplayGraphPartCreator** — Converts SharedObjects to diagram-js visual elements (Shapes, Connections, Labels)
- **diagram-js (UmlJS)** — Client-side SVG rendering library (MIT licensed)

### Communication Flows (Current)

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

### Detailed Interaction Flows

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

## Decisions Made

### 1. Full Interactive Editor (not read-only)
All current features must be supported: create/delete classes, enumerations, references,
inheritance; drag-drop from element tree; visibility toggles; re-layout; selection linking.

### 2. Keep diagram-js as Rendering Library
- MIT licensed, no vendor lock-in risk (unlike React Flow Pro)
- Proven UX for UML diagrams in this exact use case
- The GWT wrapper is the problem, not diagram-js itself

### 3. Server Holds Authoritative Graph State
- Changes come in as commands, server updates the model and pushes deltas
- **Exception**: Drag/move operations are client-local, batched to server on mouse-up
  (fire-and-forget, no confirmation needed for positions)

### 4. Auto-Layout (Sugiyama) Remains Server-Side
- Initial layout and explicit re-layout computed on server
- Client renders calculated positions

### 5. UIElement + Channel Integration
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

### 6. Reuse SharedObject/ObjectScope for Patch Generation
The existing infrastructure provides exactly what is needed:
- `ObjectScope.popChanges()` generates `Changes` (creates/updates/deletes — deltas only)
- `ChangeIO.writeChanges()` serializes to JSON
- This works bidirectionally: client-to-server (e.g., node moved) and server-to-client
  (e.g., new class created)

## Integration Strategy — Open Discussion

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

**Module structure:**
```
com.top_logic.graph.diagramjs          — Shared model (unchanged)
com.top_logic.graph.diagramjs.server   — Server logic (extend with UIElement + ReactControl wrapper)
com.top_logic.graph.diagramjs.client   — GWT client (unchanged, embedded by React wrapper)
com.top_logic.graph.diagramjs.react    — React wrapper (NEW, thin integration layer)
```

### Option B: Pure React/TypeScript Rewrite (Clean, blocked)

Replace the GWT client entirely with TypeScript. Requires msgbuf features for clean
protocol transport.

**Advantages:**
- Clean, modern tech stack
- No GWT dependency
- Native View framework integration

**Disadvantages:**
- Requires rewriting ~2000 lines of client logic in TypeScript
- Loses single-source-of-truth (Java model on server, TS model on client)
- Blocked by msgbuf features (cross-file extension, TS codegen, native JSON)

**Module structure:**
```
com.top_logic.graph.diagramjs          — Shared model (keep or replace)
com.top_logic.graph.diagramjs.server   — Server logic (new ReactControl)
com.top_logic.graph.diagramjs.client   — GWT client (removed)
com.top_logic.graph.diagramjs.react    — React client (NEW, full implementation)
```

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

## Blocking Issues for Option B

### msgbuf Feature Requests

Three feature requests have been prepared (see `msgbuf-feature-requests.md`):

1. **Cross-file protocol extension** ([msgbuf/msgbuf](https://github.com/msgbuf/msgbuf))
   — `extends` across `.proto` file boundaries with polymorphic dispatch
2. **TypeScript/JavaScript code generation** — generate TS classes with serialization
   and type-tag dispatch from `.proto` files
3. **Native JSON value type** — `json` field type that embeds structured data without
   string-wrapping

### Why Workarounds Are Insufficient

| Workaround | Problem |
|---|---|
| Add `GraphPatchEvent` to `sse.proto` | Creates dependency from base module to graph module |
| Use `PatchEvent` with graph data as state | Semantically wrong — patches are commands, not state |
| Use `FunctionCall` with string-based function names | Unstructured, fragile, not type-safe |
| Full state replacement via `StateEvent` | Works but wastes bandwidth, no delta support |

The core issue: every interaction (node move, edge creation, property change) needs a
corresponding patch to flow in both directions. Hand-coding commands for each operation
defeats the purpose of the ObjectScope/ChangeIO infrastructure, which already solves this
generically.

## Architecture Diagram (Target — Option B)

```
                        View XML (.view.xml)
                              │
                    ┌─────────┴─────────┐
                    │   <model-graph>    │
                    │   UIElement        │
                    └─────────┬─────────┘
                              │ createControl()
                    ┌─────────┴─────────┐
                    │ DiagramGraphControl│
                    │ (ReactControl)     │
                    │                    │
                    │ SharedGraph model  │
                    │ ObjectScope        │
                    │ Sugiyama layout    │
                    └────┬─────────┬────┘
                   SSE   │         │  HTTP Commands
              (Changes)  │         │  (Changes)
                         ▼         ▲
                    ┌─────────────────┐
                    │  React Client   │
                    │  (TypeScript)   │
                    │                 │
                    │  diagram-js     │
                    │  UML rendering  │
                    └─────────────────┘
```

## Architecture Diagram (Target — Option A)

```
                        View XML (.view.xml)
                              │
                    ┌─────────┴─────────┐
                    │   <model-graph>    │
                    │   UIElement        │
                    └─────────┬─────────┘
                              │ createControl()
                    ┌─────────┴──────────────────┐
                    │ React Wrapper (ReactControl)│
                    │  - Channel bridging         │
                    │  - Module/Selection events   │
                    └─────────┬──────────────────┘
                              │ mounts
                    ┌─────────┴──────────────────┐
                    │ GWT Widget (existing)       │
                    │  - DiagramJSGraphControl    │
                    │  - ObjectScope + ChangeIO   │
                    │  - Event handlers           │
                    │  - diagram-js canvas        │
                    │  - AJAX ↔ Server            │
                    └────┬─────────┬─────────────┘
                  AJAX   │         │  AJAX
              (Changes)  │         │  (Commands)
                         ▼         ▲
                    ┌─────────────────┐
                    │ Server          │
                    │ (unchanged)     │
                    │ SharedGraph     │
                    │ ObjectScope     │
                    │ Sugiyama layout │
                    └─────────────────┘
```

## Open Items

- [ ] Decide on integration strategy (Option A, B, or C)
- [ ] If Option A: design the React ↔ GWT bridge API
- [ ] If Option B: implement msgbuf features first
- [ ] Detail design of UIElement and ReactControl
- [ ] Channel bindings (module, selection, editMode)
- [ ] Toolbar and context menu integration in View framework
- [ ] Detail editor views (TypeEditor, AttributeEditor, etc.) as `.view.xml`
- [ ] Test strategy
