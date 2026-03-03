# React Table Column Resize — Design

## Interaction

- Drag handle on the right border of each header cell (~5px invisible hit area, `col-resize` cursor).
- Mousedown + drag resizes the column live with local state override (no server round-trip during drag).
- Mouseup sends the final width to the server via `columnResize` command.
- Minimum column width: 50px.

## Command Protocol

### `columnResize` command

```
{ column: string, width: number }
```

Server updates the `ColumnDef` width and pushes updated column state.

## Server-Side Changes

- New `ColumnResizeCommand` inner class in `ReactTableControl`.
- Register in `COMMANDS` map.
- On execute: find the `ColumnDef` by name, call `setWidth(width)`, push updated columns state.

## Client-Side Changes

### TLTableView.tsx

- Resize handle `<div>` inside each header cell, positioned absolute on the right edge.
- `onMouseDown` on handle: record `startX` and `startWidth` in a ref, set `resizingColumn` state.
- Document-level `mousemove` listener: compute `newWidth = startWidth + (currentX - startX)`, clamp to 50, store in `columnWidthOverrides` local state map.
- Document-level `mouseup` listener: send `columnResize` command, clear overrides and resizing state.
- Column width reads from `columnWidthOverrides[col.name] ?? col.width` for live feedback.

## CSS

- `.tlTableView__resizeHandle` — absolute positioned, right 0, full height, 5px wide, transparent, `col-resize` cursor.
- `.tlTableView__resizeHandle:hover` — faint border to indicate affordance.
- `.tlTableView__resizeHandle--active` — highlight during drag.
