# Frozen Columns Design for React Table

## Goal

Add left-side frozen (sticky) columns to the React table control so that key columns remain visible while the user scrolls horizontally.

## Approach: CSS `position: sticky`

Use `position: sticky; left: <offset>` on frozen column cells. The frozen columns stay in place as the scrollable area scrolls horizontally, without duplicating DOM elements or splitting the table into separate panels.

## Configuration

- **Count-based:** `frozenColumnCount = N` on the table. The first N columns in the column list are frozen.
- **Checkbox column:** When `frozenColumnCount > 0` and multi-select mode is active, the checkbox column is automatically frozen with `left: 0`.
- **Left-side only.** No right-side frozen columns.

## Server-Side Changes

### ReactTableControl.java

- Add `_frozenColumnCount` field (int, default 0).
- Add `setFrozenColumnCount(int count)` public setter.
- Send `"frozenColumnCount"` as a state key to the client.

### ColumnDef.java

No changes. Frozen status is determined by column position + count.

## Client-Side Changes

### TLTableView.tsx

- Read `frozenColumnCount` from state (default 0).
- For each column at index `colIdx`, determine `isFrozen = colIdx < frozenColumnCount`.
- Compute cumulative `left` offset for each frozen column (sum of preceding frozen column widths, plus checkbox width if multi-select).
- Apply to frozen header cells and body cells:
  - `position: 'sticky'`
  - `left: <computed offset>px`
  - `zIndex: 2`
- Checkbox column (multi-select): when `frozenColumnCount > 0`, apply `position: 'sticky'; left: 0; zIndex: 2`.
- Last frozen column gets a right box-shadow for visual separation.

### tlReactControls.css

- `.tlTableView__headerCell--frozen`, `.tlTableView__cell--frozen`: `position: sticky; z-index: 2;`
- `.tlTableView__headerCell--frozenLast`, `.tlTableView__cell--frozenLast`: right box-shadow separator.
- Frozen cells need opaque background so scrolling content doesn't show through.

## Interaction with Existing Features

- **Column reorder:** Works naturally. Reordering changes positions; the first N columns become frozen.
- **Column resize:** Works naturally. Sticky `left` values recomputed from current widths.
- **Virtual scrolling:** No impact. Rows are absolutely positioned; cells use flex.
- **Selection checkbox:** Automatically frozen when `frozenColumnCount > 0`.

## Demo

Update `DemoReactTableComponent` to set `frozenColumnCount = 2` (freezes "ID" and "Name").
