# React Table Column Reorder — Design

## Interaction

- Drag a header cell to reorder columns.
- Drop indicator (vertical blue line) shows between columns where the column will land.
- On drop, column moves to new position. Server notified via `columnReorder` command.

## Approach: HTML5 Drag and Drop

- Header cells get `draggable={true}`.
- `onDragStart` stores dragged column name in a ref.
- `onDragOver` determines drop position (left/right half of target cell), shows CSS indicator.
- `onDrop` sends `columnReorder: { column, targetIndex }` to server.
- Resize handle mousedown already calls `stopPropagation` — no conflict with drag.

## Command Protocol

### `columnReorder` command

```
{ column: string, targetIndex: number }
```

Server removes column from `_columnDefs` and re-inserts at `targetIndex`, then pushes updated columns state.

## Server-Side Changes

- New `ColumnReorderCommand` inner class in `ReactTableControl`.
- Register in `COMMANDS` map.
- On execute: find column by name, remove from list, insert at target index, push updated columns + rebuild viewport (cell order in rows changes).

## Client-Side Changes

### TLTableView.tsx

- `dragColumnRef` — ref storing the name of the column being dragged.
- `dragOverState` — `{ column: string, side: 'left' | 'right' }` local state for drop indicator.
- Header cells: `draggable={true}`, `onDragStart`, `onDragEnd`, `onDragOver`, `onDrop`, `onDragLeave`.
- `onDragOver`: compute left/right half from `event.clientX` vs cell bounding rect center.
- `onDrop`: compute target index from drop side and send command.
- Checkbox header cell is NOT draggable.

## CSS

- `.tlTableView__headerCell[draggable]` — `cursor: grab`.
- `.tlTableView__headerCell--dragging` — `opacity: 0.4`.
- `.tlTableView__headerCell--dragOver-left` — `border-left: 2px solid var(--interactive, #0f62fe)`.
- `.tlTableView__headerCell--dragOver-right` — `border-right: 2px solid var(--interactive, #0f62fe)`.
