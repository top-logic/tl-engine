# React Table Control Design

## Overview

A React-based table display component for TopLogic, replacing the legacy server-rendered
table. The table is a **grid container** that hosts model-driven child `ReactControl`s per
cell. It has no knowledge of the data types it displays -- all cell rendering, editing, and
filtering is delegated to child controls determined by the backend model.

## Architecture

### Core Principle: Table as Grid Container

The table control is purely a layout/grid manager:

- Each **cell** is a full `ReactControl` (e.g. `TLTextDisplay`, `TLSelect`, `TLLink`),
  created by column-specific providers from the backend model.
- Each **filter** is a full `ReactControl` (e.g. text filter, date range filter, enum filter),
  also determined by the column type in the model.
- The **table itself** handles: grid layout, virtual scrolling, sorting, selection, column
  management, tree expand/collapse, keyboard navigation.
- The **toolbar** is NOT part of the table. A surrounding panel provides toolbar actions
  independently.

### Why Full ReactControls Per Cell

Each visible cell is a real `ReactControl` with its own `controlId` and SSE registration.
This allows reusing all existing React controls (inputs, selects, links, etc.) without
building table-specific variants.

Overhead per control is minimal:
- Server: ~200-300 bytes (HashMap + ConcurrentHashMap entry + ID string)
- Network: No extra SSE connections (all controls share one session-level SSE stream)
- Client: One lightweight state store + one listener entry per control

For 30 visible rows x 10 columns = 300 controls: ~90KB server memory, O(1) command dispatch.

## Server-Side Design

### Java Classes

```
ReactControl (existing base)
  +-- ReactTableControl
        - Owns the TableModel/TableData (existing TL model layer)
        - Manages column definitions derived from ColumnConfiguration
        - Manages the row viewport window with prefetch buffer
        - Creates/caches cell ReactControls via column providers
        - Creates filter ReactControls via filter providers
        - Handles commands: scroll, sort, select, expand/collapse,
          columnResize, columnReorder, toggleColumnVisibility,
          startEditRow, commitEditRow, cancelEditRow
```

### Cell Control Providers

The table delegates cell control creation to the model layer:

```java
interface ReactCellControlProvider {
    /** Creates a display or edit control for a cell, depending on edit state. */
    ReactControl createCellControl(Object rowObject, String columnName, boolean editing);
}

interface ReactFilterControlProvider {
    /** Creates a filter control for a column. */
    ReactControl createFilterControl(String columnName);
}
```

Column configuration determines which provider to use. A `String` attribute gets a
`ReactTextDisplay` / `ReactTextInput`, a reference gets a `ReactLink` / `ReactSelect`,
a boolean gets a `ReactCheckbox`, etc.

### Handler Callbacks

Application-specific behavior is injected via handler interfaces:

```java
interface TableEditHandler {
    /** Called before switching a row to edit mode. Can veto. */
    HandlerResult startEdit(DisplayContext context, Object rowObject);

    /** Called to commit edits. Performs validation and persistence. */
    HandlerResult commitEdit(DisplayContext context, Object rowObject);

    /** Called to cancel editing and discard changes. */
    HandlerResult cancelEdit(DisplayContext context, Object rowObject);
}

interface TableSelectionHandler {
    /** Called when selection changes (e.g. to navigate to detail view). */
    void selectionChanged(DisplayContext context, Set<Object> selectedRows);
}

interface TableDragHandler {
    /** Provides drag identification data for server-controlled D&D. */
    Object getDragData(Object rowObject);
}

interface TableDropHandler {
    /** Handles a drop on/near a row. */
    HandlerResult handleDrop(DisplayContext context, Object dragData, Object targetRow);
}
```

### Row Viewport Management

The server maintains a cache of cell controls for the current viewport + prefetch buffer:

```
Map<Object, Map<String, ReactControl>> _rowCellCache
```

When the viewport changes (scroll command):
1. Determine which rows entered/left the viewport (including prefetch zone)
2. For new rows: ask column providers to create cell controls, register with SSE
3. For departed rows: unregister cell controls from SSE, remove from cache
4. Send PatchEvent with updated rows array

### Commands

| Command           | Arguments                        | Server Action                                    |
|-------------------|----------------------------------|--------------------------------------------------|
| `scroll`          | `{ start, count }`              | Update viewport, create/remove cell controls     |
| `sort`            | `{ column, direction, append }` | Re-sort model, reset viewport to top             |
| `select`          | `{ rowId, mode }`               | Update selection model, notify handler            |
| `expand`          | `{ rowId }`                     | Expand tree node, update row count               |
| `collapse`        | `{ rowId }`                     | Collapse tree node, update row count             |
| `columnResize`    | `{ column, width }`             | Update column width, persist personalization     |
| `columnReorder`   | `{ column, targetIndex }`       | Reorder columns, persist                         |
| `toggleColumn`    | `{ column, visible }`           | Show/hide column                                 |
| `startEditRow`    | `{ rowId }`                     | Delegate to TableEditHandler, switch to edit mode|
| `commitEditRow`   | `{ rowId }`                     | Delegate to TableEditHandler, commit + display   |
| `cancelEditRow`   | `{ rowId }`                     | Delegate to TableEditHandler, discard + display  |

## State Protocol

```json
{
  "columns": [
    {
      "name": "col1",
      "label": "Name",
      "width": 200,
      "frozen": true,
      "sortable": true,
      "filterable": true,
      "sortDirection": "asc"
    }
  ],
  "columnGroups": [{ "label": "Group", "span": 3 }],
  "totalRowCount": 5000,
  "frozenColumnCount": 2,
  "selectionMode": "multi",
  "editMode": "none",

  "viewportStart": 0,
  "rows": [
    {
      "id": "row_42",
      "selected": false,
      "cssClass": "even",
      "treeDepth": 0,
      "expanded": null,
      "cells": {
        "col1": "<ReactControl child descriptor>",
        "col2": "<ReactControl child descriptor>"
      }
    }
  ],

  "filters": {
    "col1": "<ReactControl child descriptor>",
    "col2": "<ReactControl child descriptor>"
  },
  "filterMode": "row"
}
```

## Client-Side Design

### Component Structure

```
TLTable (main component)
+-- TLTableHeader
|   +-- TLColumnGroupRow      (optional, grouped column headers)
|   +-- TLColumnHeaderRow     (column labels, sort indicators, resize handles)
|   +-- TLFilterRow           (optional, when filterMode="row")
+-- TLTableBody
|   +-- TLVirtualScroller     (scroll container, spacer, visible range)
|   |   +-- TLTableRow[]      (one per visible row)
|   |       +-- TLSelectionCell (optional checkbox)
|   |       +-- TLTreeIndent   (optional, indentation + expand/collapse)
|   |       +-- TLChild[]      (cell controls, mounted via bridge)
+-- TLFilterPopover            (optional, when filterMode="popover")
+-- TLColumnConfigDialog       (optional, column show/hide/reorder)
```

### Virtual Scrolling

1. Container with `overflow-y: auto`
2. Spacer element sized to `totalRowCount * rowHeight` for correct scrollbar
3. Actual rows positioned absolutely within the visible window
4. Server provides rows for visible range + prefetch buffer (1 page above and below)
5. Client requests new data only when scrolling beyond the prefetched buffer
6. Debounce scroll commands (~100ms) to avoid flooding during fast scrolling
7. While waiting for server response, keep existing rows visible (no flicker)

### Frozen Columns

Two synchronized scroll containers side by side:
- Left: frozen columns, no horizontal scroll
- Right: scrollable columns, horizontal scroll enabled
- Both share vertical scroll position (synchronized via scroll event)

### Selection

Three modes, configurable per table:
- **Single**: click to select one row, deselects previous
- **Multi**: checkbox column, ctrl-click to toggle individual rows
- **Range**: shift-click to select contiguous ranges, ctrl-click for disjoint

### Drag and Drop

HTML5 drag-and-drop with server-controlled identification:
- Drag: rows get `draggable="true"`, `dragstart` sends command to server for drag data
- Drop: `dragover`/`drop` sends command to server with drop data and target row
- Table UI only handles visual feedback (drag ghost, drop indicator line)

### Keyboard Navigation

Configurable, details to be fine-tuned during implementation:
- Arrow up/down: navigate between rows
- Arrow left/right: navigate between cells, or collapse/expand in tree context
- Enter: context-dependent (select, start editing, expand -- configurable)
- Escape: cancel edit mode
- Tab: move focus into/out of cell controls
- Space: toggle selection

## Implementation Phases

### Phase 1: Core Grid
- `ReactTableControl` with column definitions and flat row viewport
- Cell controls per cell (display only, read-only)
- Virtual scrolling with server-fetched row windows and prefetch buffer
- Single-column sorting (click header)
- Single row selection

### Phase 2: Interaction
- Multi-selection, range selection (shift-click, ctrl-click)
- Multi-column sort (sort dialog or shift-click)
- Column resize (drag handle)
- Column reorder (drag header)
- Frozen columns

### Phase 3: Filtering
- Filter controls per column (delegated to model-driven providers)
- Header filter row mode
- Filter popover mode
- Active filter indicators

### Phase 4: Tree Table
- Tree depth, expand/collapse
- Lazy child loading
- Tree-aware keyboard navigation (arrow left/right for collapse/expand)

### Phase 5: Editing
- Row-wise edit mode (start/commit/cancel with handler callbacks)
- All-editable mode
- Cell editor controls (delegated to model-driven providers)

### Phase 6: Advanced
- Column configuration dialog (show/hide, bulk reorder)
- Drag-and-drop (source and target, server-controlled identification)
- Excel export (server-side, triggered by surrounding panel command)
- Personalization persistence (column widths, order, visibility, sort)
- Keyboard navigation refinements and configurability

## Feature Catalog

### Core Rendering
- Virtual scrolling with prefetch buffer (no paging)
- Frozen/fixed columns (configurable count, pinned left)
- Column groups (hierarchical headers spanning multiple columns)
- Custom cell renderers (model-driven ReactControl per cell)
- Row CSS classes (server-driven styling)
- Cell CSS classes (server-driven styling)

### Tree Table
- Unified flat/tree component (flat = depth 0)
- Expand/collapse (server-controlled, lazy child loading)
- Visual indentation
- Tree-aware keyboard navigation

### Sorting
- Single-column sort (click header)
- Multi-column sort (shift-click or dialog)
- Sort direction indicators in headers
- Server-side sorting

### Filtering
- Header filter row (always-visible filter inputs)
- Filter popover (per-column popup via icon)
- Both modes configurable per table
- Filter types determined by column model (text, comparable, date, boolean, enum, custom)
- Active filter indicators
- Server-side filtering

### Selection
- Single selection (click row)
- Multi-selection (checkboxes, ctrl-click)
- Range selection (shift-click)
- Configurable per table instance
- Server-notified via handler callback

### Inline Editing
- Row-wise editing (one row at a time, with start/commit/cancel lifecycle)
- All-editable mode (all rows show input controls)
- Configurable per table
- Cell editors determined by column model
- Handler callbacks for application-specific edit logic

### Column Configuration
- Drag-to-reorder (inline)
- Drag-to-resize (inline)
- Show/hide columns (configuration dialog)
- Column rename (configuration dialog)
- Personalization persistence

### Drag and Drop
- Table rows as drag source (server-controlled identification)
- Table as drop target
- Decoupled: UI provides drag data, server handles logic via handlers

### Export
- Excel export (server-side, triggered externally)

### Accessibility
- Keyboard navigation (arrow keys, tab, enter, escape)
- ARIA table semantics
- Screen reader announcements
