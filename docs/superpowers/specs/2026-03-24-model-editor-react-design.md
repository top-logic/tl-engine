# Model Editor React Migration — Design Discussion

**Date**: 2026-03-24
**Status**: Blocked — waiting for msgbuf feature requests
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
- **DiagramJSGraphControl** — Server-side control, renders initial state, handles AJAX commands
- **GraphModelUtil** — Graph construction, Sugiyama layout integration, model manipulation
- **DiagramJSGraphBuilder** — Creates layouted `SharedGraph` from `TLModule`
- **ObjectScope / SharedObject** — Client-server synchronization with automatic patch generation
- **ChangeIO** — JSON serialization of Changes (creates/updates/deletes)
- **diagram-js (UmlJS)** — Client-side SVG rendering library (MIT licensed)

### Communication Pattern (Current)

```
Server                              GWT Client
  SharedGraph (ObjectScope)  ←→  ObjectScope (GWT-compiled)
  ObjectScope.popChanges()   →   ChangeIO → scope.update(changes)
  ControlCommand handlers   ←   AJAX dispatchControlCommand
```

## Decisions Made

### 1. Full Interactive Editor (not read-only)
All current features must be supported: create/delete classes, enumerations, references,
inheritance; drag-drop from element tree; visibility toggles; re-layout; selection linking.

### 2. Keep diagram-js as Rendering Library
- MIT licensed, no vendor lock-in risk (unlike React Flow Pro)
- Proven UX for UML diagrams in this exact use case
- Wrap as React component via `useRef` + `useEffect` pattern
- The GWT wrapper is the problem, not diagram-js itself

### 3. SSE + Commands Communication (not AJAX/SharedObject sync)
- Consistent with the View framework pattern
- Server sends state updates via SSE
- Client sends commands via HTTP
- SharedObject patch mechanism to be reused for generating deltas

### 4. Server Holds Authoritative Graph State
- A `ReactControl` subclass holds the graph model
- Changes come in as commands, server updates the model and pushes deltas via SSE
- **Exception**: Drag/move operations are client-local, batched to server on mouse-up
  (fire-and-forget, no SSE confirmation needed for positions)

### 5. Auto-Layout (Sugiyama) Remains Server-Side
- Initial layout and explicit re-layout computed on server
- Client renders calculated positions
- No client-side layout approximation (can be added later if needed)

### 6. UIElement + Channel Integration
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

### 7. Module Structure (New)

```
com.top_logic.graph.diagramjs          — Shared model (keep as-is)
com.top_logic.graph.diagramjs.server   — Server logic (extend with ReactControl)
com.top_logic.graph.diagramjs.client   — GWT client (becomes obsolete)
com.top_logic.graph.diagramjs.react    — React client (NEW)
```

- **Server module** gets a new `ReactControl`-based control alongside the existing GWT control
- **GWT client module** remains until React version is feature-complete, then removed
- **New React module** contains TypeScript/diagram-js integration with own Vite build
- **Shared module** is retained — `SharedObject` classes are already JSON-serializable
  and support automatic patch generation via `ObjectScope`

### 8. Reuse SharedObject/ObjectScope for Patch Generation
The existing infrastructure provides exactly what is needed:
- `ObjectScope.popChanges()` generates `Changes` (creates/updates/deletes — deltas only)
- `ChangeIO.writeChanges()` serializes to JSON
- This works bidirectionally: client-to-server (e.g., node moved) and server-to-client
  (e.g., new class created)

## Blocking Issue: msgbuf Protocol Extensibility

### The Problem

The SSE protocol is defined in `com.top_logic.layout.react/protocol/sse.proto`. All event
types (`PatchEvent`, `StateEvent`, etc.) must be in this single file. msgbuf does not support:

1. **Cross-file protocol extension** — defining `GraphPatchEvent extends SSEEvent` in a
   separate `.proto` file in the graph module
2. **TypeScript code generation** — the client-side deserialization must be hand-written
3. **Native JSON value fields** — structured JSON data must be string-wrapped, causing
   double serialization

### Why Workarounds Are Insufficient

Considered workarounds and why they were rejected:

| Workaround | Problem |
|---|---|
| Add `GraphPatchEvent` to `sse.proto` | Creates dependency from base module to graph module |
| Use `PatchEvent` with graph data as state | Semantically wrong — patches are commands, not state |
| Use `FunctionCall` with string-based function names | Unstructured, fragile, not type-safe |
| Use `JSSnipplet` | Completely unstructured, not debuggable |
| Full state replacement via `StateEvent` | Works but wastes bandwidth, no delta support |

The core issue: every interaction (node move, edge creation, property change) needs a
corresponding patch to flow in both directions. The SharedObject/ObjectScope system already
generates these patches. What is missing is a clean, type-safe way to transport them over
the SSE channel without polluting the base protocol or resorting to string-encoded hacks.

### Required msgbuf Features

Three feature requests have been filed (text in `msgbuf-feature-requests.md`):

1. **Cross-file protocol extension** ([msgbuf/msgbuf](https://github.com/msgbuf/msgbuf))
   — `extends` across `.proto` file boundaries with polymorphic dispatch
2. **TypeScript/JavaScript code generation** — generate TS classes with serialization
   and type-tag dispatch from `.proto` files
3. **Native JSON value type** — `json` field type that embeds structured data without
   string-wrapping

### Path Forward

Once msgbuf supports these features, the architecture becomes:

```protobuf
// In com.top_logic.graph.diagramjs.react: graph-sse.proto
import "com.top_logic.layout.react.protocol.sse.proto";

message GraphChangesEvent extends SSEEvent {
    string controlId;
    json changes;    // ObjectScope Changes (creates/updates/deletes)
}
```

Generated TypeScript handles deserialization and dispatch automatically. The React component
receives typed `GraphChangesEvent` objects and applies them to the diagram-js canvas.

The same mechanism works client-to-server: when the user moves a node, a `GraphChangesEvent`
is sent as a command, the server applies it to the ObjectScope, and any resulting model
changes (e.g., validation, cascading updates) are pushed back.

## Architecture Diagram

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

## Open Items (After msgbuf Unblock)

- [ ] Detail design of `DiagramGraphControl` (ReactControl subclass)
- [ ] TypeScript component structure in `com.top_logic.graph.diagramjs.react`
- [ ] Mapping of SharedObject Changes to diagram-js canvas operations
- [ ] Channel bindings (module, selection, editMode)
- [ ] Command definitions (createClass, createConnection, delete, relayout, etc.)
- [ ] Toolbar and context menu integration in View framework
- [ ] Detail editor views (TypeEditor, AttributeEditor, etc.) as `.view.xml`
- [ ] Migration path: running GWT and React editors in parallel
- [ ] Test strategy
