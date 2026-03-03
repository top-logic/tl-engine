# Multi-Column Sort Design for React Table

## Goal

Add multi-column sorting to the React table so users can sort by multiple columns with priority ordering.

## UX

- **Plain click** on a column header: replaces the entire sort with that single column.
- **Shift+click** on a column header: adds the column to the sort chain (or toggles its direction if already in the chain).
- Sort priority badges (1, 2, 3...) appear next to the sort arrow when multiple columns are sorted.
- No limit on the number of sort columns.

## Approach: Ordered Sort List on Server

The server maintains an ordered `List<SortKey>` instead of a single sort column. The `SortCommand` receives a `mode` parameter ("replace" or "add") to distinguish plain click from Shift+click. Comparators are chained in priority order.

## Server-Side Changes

### ReactTableControl.java

- Add inner class `SortKey` holding `columnName` (String) and `ascending` (boolean).
- Replace `_sortColumn` (String) and `_sortAscending` (boolean) with `_sortKeys` (`List<SortKey>`), initially empty.
- `SortCommand` receives `column`, `direction`, and `mode`:
  - **replace**: clear list, add column as sole sort key.
  - **add**: if column already in list, toggle direction; otherwise append to end.
- After updating `_sortKeys`, update each `ColumnDef`:
  - `sortDirection`: "asc"/"desc" if in sort list, null otherwise.
  - `sortPriority`: 1-based position in sort list, or 0 if unsorted.
- Chain comparators using `thenComparing()` in `_sortKeys` order.
- Sort `_displayedRows` and rebuild state.

### ColumnDef.java

- Add `_sortPriority` field (int, default 0).
- Add `setSortPriority(int priority)` setter.
- Include `"sortPriority"` in `toStateMap()` when priority > 0.

## Client-Side Changes

### TLTableView.tsx

- `handleSort` checks `event.shiftKey`:
  - No shift: `sendCommand('sort', { column, direction, mode: 'replace' })`.
  - Shift: `sendCommand('sort', { column, direction, mode: 'add' })`.
- Direction toggle logic unchanged (no direction/desc -> asc, asc -> desc).
- Read `sortPriority` from column state.
- When `sortPriority > 0` and multiple columns sorted, render priority badge next to sort arrow.

### tlReactControls.css

- `.tlTableView__sortPriority`: small badge for priority number.

## Interaction with Existing Features

- **Single-column sort (plain click):** Identical to current behavior.
- **Frozen columns:** No interaction.
- **Column reorder:** Sort keys use column names, not positions.
- **Virtual scrolling:** Sort triggers full rebuild, same as now.
