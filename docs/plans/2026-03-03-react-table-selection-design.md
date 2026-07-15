# React Table Selection — Design

## Selection Modes

Two modes: `single` and `multi`.

### Single Mode

| Action | Behavior |
|--------|----------|
| Plain click | Select row, deselect previous |
| Ctrl-click | Deselect row (if `selectionForced=false`) |
| Shift-click | Same as plain click |

No checkbox column.

### Multi Mode

| Action | Behavior |
|--------|----------|
| Plain click | Replace selection with clicked row, set anchor |
| Ctrl-click | Toggle individual row |
| Shift-click | Select range from anchor to clicked row (replaces selection) |
| Ctrl+Shift-click | Add range from anchor to clicked row (extends selection) |

Automatic checkbox column prepended. Header checkbox toggles select-all / deselect-all.

## Configuration

- `selectionMode`: `"single"` or `"multi"` (default `"single"`)
- `selectionForced`: `boolean` (default `false`) — when true, the last selected row cannot be deselected

## Command Protocol

### `select` command

```
{ rowIndex: number, ctrlKey: boolean, shiftKey: boolean }
```

Server interprets modifier keys based on selection mode. Backward-compatible: missing modifier keys default to false (plain click).

### `selectAll` command (multi mode only)

```
{ selected: boolean }
```

Sent by header checkbox. `true` = select all rows, `false` = deselect all.

## Server-Side State

New fields on `ReactTableControl`:

- `_selectionAnchor` (int): index into `_displayedRows` of the last plain-clicked row. -1 means no anchor.
- `_selectionForced` (boolean): config flag.

New state keys sent to client:

- `selectedCount` (int): total selected rows across all data (not just viewport), so the client can render header checkbox state.

## Client-Side Changes

### TLTableView.tsx

- `handleRowClick(rowIndex, event)` extracts `event.ctrlKey || event.metaKey` and `event.shiftKey`, sends them with the `select` command.
- When `selectionMode === 'multi'`: render a 40px checkbox column before data columns.
  - Header cell: checkbox with three states (unchecked / indeterminate / checked) based on `selectedCount` vs `totalRowCount`.
  - Row cell: checkbox reflecting `row.selected`, click delegates to row click handler.

### CSS

- `.tlTableView__checkboxCell` — 40px fixed-width cell with centered content.
- `.tlTableView__checkbox` — native checkbox styling.
