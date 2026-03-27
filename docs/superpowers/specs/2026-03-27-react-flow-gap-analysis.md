# React Flow Diagram — Gap Analysis & Next Steps

**Date**: 2026-03-27
**Status**: Phase 1 PoC successful — gaps identified for production readiness
**Ticket**: #29108

## Current State

Phase 1 Proof of Concept is **successful**: A GWT-rendered SVG flow diagram (Parent →
Child A / Child B) displays correctly in the React View UI. The GWT code mounts via a
minimal React lifecycle wrapper (`TLFlowDiagram.tsx`), receives the diagram model as
serialized JSON initial state, and renders it to SVG using the existing SVGBuilder pipeline.

What works:
- GWT SVG rendering inside React-managed `<div>`
- Tree layout computation (client-side)
- Pan (drag) and zoom (Ctrl+wheel) interaction
- `<flow-diagram>` UIElement in View XML
- `TLReact.subscribe()` SSE bridge available
- `ReactBridge.sendCommand()` for client→server commands
- Viewbox update commands sent via ReactBridge

## Identified Gaps

### Gap 1: FlowChartBuilder API Incompatible with ViewContext

**Problem**: `FlowChartBuilder.getModel(Object businessModel, LayoutComponent component)`
requires a `LayoutComponent`, which does not exist in the View framework. The current
`FlowDiagramElement.createControl()` passes `null` for both parameters.

**Impact**: Builders that depend on `LayoutComponent` (for accessing channels, selection
models, or component hierarchy) will not work.

**Solution**: Create a new builder interface for the View framework:

```java
public interface ViewFlowChartBuilder {
    Diagram getModel(ViewContext context);
}
```

Or adapt `FlowChartBuilder` to accept a more generic context. The `FlowDiagramElement`
Config should support both interfaces for backward compatibility.

**Priority**: High — blocks real-world usage.

### Gap 2: Server→Client Push Updates Not Tested

**Problem**: `FlowDiagramControl.pushDiagramChanges()` serializes `ExternalScope` changes
as JSON and puts them in the React state as `"diagramPatch"`. The client's
`onServerState()` method extracts and applies this patch. However, this flow has never
been triggered — no server-side code currently modifies the diagram after initial render.

**Impact**: Server-initiated diagram changes (e.g., model observation triggers) would not
work without verification.

**Solution**: Create a test scenario where the server modifies the diagram (e.g., a command
that adds a node) and verify the patch arrives and is applied correctly on the client.

**Priority**: High — core architectural assumption untested.

### Gap 3: Drag-and-Drop Not Implemented

**Problem**: The `@ReactCommand("dispatchDrop")` handler on the server logs a message but
does not process drop data. The client-side `createDropArgs()` still references
`$wnd.services.ajax.mainLayout.tlDnD` for reading drop data — this legacy API may not be
available in the React UI.

**Impact**: Users cannot drop external elements onto the diagram.

**Solution**:
1. Define a React-native DnD data transfer mechanism (HTML5 drag-and-drop with custom data)
2. The GWT client reads drop data from the HTML5 `DataTransfer` API instead of `tlDnD`
3. The server handler deserializes the drop data from the command arguments

**Priority**: Medium — needed for element tree drops in Phase 2 (UML editor), but not
critical for basic flow diagram display.

### Gap 4: Context Menu Rendering Incomplete

**Problem**: The `@ReactCommand("contextMenu")` handler creates a `Menu` object via
`createContextMenu()`, but does not serialize it to the client. The TODO comment says
"Send context menu to client via SSE state update."

**Impact**: Right-click context menus do not appear.

**Solution**: Serialize the `Menu` as JSON and deliver via SSE. The React client needs a
context menu component that renders the menu items. This may require extending the View
framework's context menu infrastructure.

**Priority**: Medium — needed for node-specific actions.

### Gap 5: Selection Channel Integration End-to-End

**Problem**: The `@ReactCommand("selection")` handler on the server writes to the View
channel, but this has not been tested end-to-end. The client-side click handler dispatches
click events to the server, but it's unclear if selection changes in the diagram are
properly routed through the `selection` command.

**Impact**: Selecting a node in the diagram may not update other View components (forms,
tables) that listen to the selection channel.

**Solution**: Wire up a complete test scenario:
1. Click on a `SelectableBox` node → client sends `selection` command
2. Server resolves user object → writes to selection channel
3. Another View component (e.g., a panel showing the selected object) updates

**Priority**: High — core integration feature.

### Gap 6: Legacy JSNI Dependencies Remain

**Problem**: Several JSNI methods in `FlowDiagramClientControl` still reference
`$wnd.services.ajax`:

- `createDropArgs()` — reads `$wnd.services.ajax.mainLayout.tlDnD.data`
- `hasWindowTlDnD()` — checks `$wnd.services.ajax.mainLayout.tlDnD`
- `getWheelScrollFactor()` — calls `$wnd.BAL.getWheelScrollFactor(event)`

**Impact**: These may fail silently in the React UI if the legacy TopLogic AJAX framework
is not loaded. The `getWheelScrollFactor` call likely works (BAL is globally loaded), but
the DnD references will fail.

**Solution**:
- DnD: Replace with HTML5 DataTransfer API (see Gap 3)
- `getWheelScrollFactor`: Add a fallback for when `BAL` is not available

**Priority**: Medium — DnD is broken, wheel scroll likely works.

### Gap 7: Lazy Update Optimization Lost

**Problem**: The old `JSDiagramControl` distinguished between viewbox-only changes
(pan/zoom) and content changes. Viewbox changes used a "lazy request" mechanism that
coalesced multiple rapid updates into a single request. The new implementation uses a
simple 150ms debounce for all changes.

**Impact**: Pan/zoom may generate more network traffic than necessary. Not a functional
issue but a performance concern for large diagrams.

**Solution**: Differentiate viewbox-only changes in the debounce handler. Viewbox changes
can use a longer debounce interval or be coalesced more aggressively. This is an
optimization, not a correctness issue.

**Priority**: Low — works correctly, just suboptimal.

### Gap 8: Model Observation Not Available in View Framework

**Problem**: `FlowChartComponent` (legacy layout framework) has rich model observation:
it monitors business objects for changes and re-renders the diagram when they are modified
or deleted. `FlowDiagramElement` (new View framework) has no equivalent.

**Impact**: Diagrams in the React UI do not react to external model changes.

**Solution**: The `FlowDiagramControl` (ReactControl) should observe the model objects
and push diagram updates when changes occur. This requires:
1. The builder reports observed objects
2. The control registers listeners on those objects
3. On change: re-build diagram or apply incremental update, push via SSE

**Priority**: Medium — needed for live diagrams but not for static visualizations.

### Gap 9: constraint-test View Disabled

**Problem**: The `constraint-test.view.xml` was temporarily disabled in `app.view.xml`
because it uses an unregistered `generic-command` tag name. This is a pre-existing issue
unrelated to the flow diagram work.

**Solution**: Fix the `constraint-test.view.xml` to use a registered command tag, or
register the `generic-command` tag. Re-enable in sidebar.

**Priority**: Low — unrelated to flow diagram.

## Recommended Next Steps (Ordered)

### Step 1: Server→Client Push Verification

Create a `@ReactCommand("addNode")` test handler that adds a new node to the diagram
on the server and pushes the change via SSE. Verify the client applies the patch and
the new node appears. This validates the core bidirectional communication.

### Step 2: ViewFlowChartBuilder API

Design a `ViewFlowChartBuilder` interface that works with `ViewContext` instead of
`LayoutComponent`. Adapt `FlowDiagramElement` to use it. This unblocks real-world usage.

### Step 3: Selection End-to-End

Wire up selection: click on SelectableBox → server resolves user object → writes to
channel → second View component shows selected object. Create a test view that
demonstrates this.

### Step 4: Context Menu Delivery

Serialize `Menu` objects to JSON and deliver via SSE state update. Implement a React-side
context menu renderer, or use the existing View framework's context menu infrastructure
if available.

### Step 5: DnD Migration

Replace `$wnd.services.ajax.mainLayout.tlDnD` references with HTML5 DataTransfer API.
Implement server-side drop data deserialization.

### Step 6: Model Observation

Add business object observation to `FlowDiagramControl` so diagrams update when the
underlying model changes.

### Step 7: Phase 2 — UML Model Editor

With the flow diagram fully operational in the React framework, begin Phase 2:
- Integrate Sugiyama layout algorithm
- Add UML class node composition from widget primitives
- Implement interactive edge-drawing
- Build the complete model editor as View XML compositions
