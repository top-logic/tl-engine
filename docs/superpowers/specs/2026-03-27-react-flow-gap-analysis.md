# React Flow Diagram — Gap Analysis & Next Steps

**Date**: 2026-03-27 (updated 2026-03-30)
**Status**: Phase 1 production-ready for basic usage — high-priority gaps closed
**Ticket**: #29108

## Current State

Phase 1 is **functional with real business data**: The construction plan flowchart demo
renders in the React View UI with product selection, full diagram rendering, and
node selection feeding a detail form.

What works:
- GWT SVG rendering inside React-managed `<div>`
- Tree layout computation (client-side)
- Pan (drag) and zoom (Ctrl+wheel) interaction
- `<flow-diagram>` UIElement in View XML with TL-Script `createChart` expression
- Input channels feeding positional arguments to the TL-Script expression
- Channel listener rebuilds diagram when input changes (e.g. product selection)
- TLFlowDiagram.tsx remounts GWT control when diagram JSON changes
- `reactFlowSelection()` nodes with `userObject` for business object binding
- Selection E2E: click node → scope patch → server updates selection channel → detail form
- `<form>` element with `<field>` children bound to selection channel
- Full construction plan demo ported from legacy layout framework

## Closed Gaps

### Gap 1: FlowChartBuilder API — CLOSED (2026-03-30)

**Solution**: Replaced `FlowChartBuilder` with direct TL-Script + input channels in
`FlowDiagramElement`. The element now follows the `TableElement` pattern (channels) and
`ScriptFlowChartBuilder` pattern (TL-Script + handler injection). No separate builder
interface needed.

### Gap 2: Server→Client Push — CLOSED (2026-03-30)

**Solution**: `FlowDiagramElement` adds a `ChannelListener` on all input channels. When a
channel value changes, the diagram is rebuilt via `_createChart.executeWith(args)` and
`control.setModel(newDiagram)` is called. `TLFlowDiagram.tsx` uses `diagramJson` as a
`useEffect` dependency, causing React to destroy and remount the GWT control with the new
diagram data.

### Gap 5: Selection Channel E2E — CLOSED (2026-03-30)

**Solution**: `FlowDiagramControl.processUpdate()` now calls `updateSelectionChannel()`
after applying a client-side scope patch. This method reads the current selection from
`Diagram.getSelection()`, filters for `isSelected()`, extracts `userObject`, and writes
to the configured `ViewChannel`. The complete chain: click SelectableBox → GWT client sends
scope update → server applies patch → server writes to selection channel → detail form
updates.

## Open Gaps

### Gap 3: Drag-and-Drop Not Implemented

**Problem**: The `@ReactCommand("dispatchDrop")` handler on the server logs a message but
does not process drop data. The client-side `createDropArgs()` still references
`$wnd.services.ajax.mainLayout.tlDnD` for reading drop data — this legacy API may not be
available in the React UI.

**Impact**: Users cannot drop external elements onto the diagram.

**Priority**: Medium — needed for element tree drops in Phase 2 (UML editor), but not
critical for basic flow diagram display.

### Gap 4: Context Menu Rendering Incomplete

**Problem**: The `@ReactCommand("contextMenu")` handler creates a `Menu` object via
`createContextMenu()`, but does not serialize it to the client. The TODO comment says
"Send context menu to client via SSE state update."

**Impact**: Right-click context menus do not appear.

**Priority**: Medium — needed for node-specific actions.

### Gap 6: Legacy JSNI Dependencies Remain

**Problem**: Several JSNI methods in `FlowDiagramClientControl` still reference
`$wnd.services.ajax`:

- `createDropArgs()` — reads `$wnd.services.ajax.mainLayout.tlDnD.data`
- `hasWindowTlDnD()` — checks `$wnd.services.ajax.mainLayout.tlDnD`
- `getWheelScrollFactor()` — calls `$wnd.BAL.getWheelScrollFactor(event)`

**Impact**: DnD references will fail in the React UI. Wheel scroll likely works (BAL is
globally loaded).

**Priority**: Medium — DnD is broken, wheel scroll likely works.

### Gap 7: Lazy Update Optimization Lost

**Problem**: The old `JSDiagramControl` distinguished between viewbox-only changes
(pan/zoom) and content changes. The new implementation uses a simple 150ms debounce.

**Impact**: Pan/zoom may generate more network traffic than necessary. Not a functional
issue but a performance concern for large diagrams.

**Priority**: Low — works correctly, just suboptimal.

### Gap 8: Model Observation Not Available in View Framework

**Problem**: `FlowDiagramElement` compiles the `observed` expression via
`QueryExecutor.compileOptional()` but does not wire it into any observation mechanism.
Diagrams in the React UI do not react to external model changes.

**Impact**: Diagrams do not update when the underlying business objects change.

**Priority**: Medium — needed for live diagrams but not for static visualizations.

## Next Steps

The high-priority gaps (1, 2, 5) are closed. The remaining gaps (3, 4, 6, 7, 8) are
medium/low priority and can be addressed incrementally.

**Phase 2: UML Model Editor** is the next major milestone. The plan is to implement
the model editor using the flow diagram infrastructure (replacing diagram.js). See
`docs/superpowers/specs/2026-03-24-model-editor-react-design.md` for the design spec.
