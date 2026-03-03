# React Table Multi-Selection Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add multi-selection with shift-click range selection, ctrl-click toggle, automatic checkbox column, select-all, and selectionForced config to the React table control.

**Architecture:** Extend `ReactTableControl` with modifier-key-aware selection logic, an anchor row for range computation, and a `selectAll` command. The client sends `{rowIndex, ctrlKey, shiftKey}` and the server decides what to select. A checkbox column auto-renders when `selectionMode === 'multi'`.

**Tech Stack:** Java 17, React via tl-react-bridge, Maven, Vite

---

## File Overview

### Modified Files

**Java:**
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — New fields, new command, rewritten selection logic

**TypeScript:**
- `com.top_logic.layout.react/react-src/controls/TLTableView.tsx` — Checkbox column, modifier key forwarding

**CSS:**
- `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` — Checkbox cell styles

**Demo:**
- `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java` — Switch to multi-selection mode

---

## Task 1: Server-Side Selection Logic

**Purpose:** Extend `ReactTableControl` with modifier-key-aware selection, anchor tracking, `selectionForced` config, `selectedCount` state, and a `selectAll` command.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add new fields and state keys**

Add after the existing `SELECTION_MODE` constant (line 52):

```java
	private static final String SELECTION_FORCED = "selectionForced";

	private static final String SELECTED_COUNT = "selectedCount";
```

Add after `_selectionMode` field (line 86):

```java
	private boolean _selectionForced;

	/** Index into {@code _displayedRows} of the last plain-clicked row, or -1. */
	private int _selectionAnchor = -1;
```

**Step 2: Add `selectAll` to the command map**

Change the COMMANDS constant (line 61-64) to:

```java
	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand());
```

**Step 3: Add `setSelectionForced` method**

Add after `setSelectionMode` (after line 127):

```java
	/**
	 * Sets whether the selection is forced (at least one row must remain selected).
	 */
	public void setSelectionForced(boolean forced) {
		_selectionForced = forced;
		putState(SELECTION_FORCED, Boolean.valueOf(forced));
	}
```

**Step 4: Update `setSelectionMode` JavaDoc**

Replace the `setSelectionMode` method (lines 118-127):

```java
	/**
	 * Sets the selection mode.
	 *
	 * @param mode
	 *        One of {@code "single"}, {@code "multi"}.
	 */
	public void setSelectionMode(String mode) {
		_selectionMode = mode;
		putState(SELECTION_MODE, mode);
	}
```

**Step 5: Send `selectedCount` in `updateViewport`**

Add at the end of `updateViewport`, just before the closing brace (after line 222):

```java
		putState(SELECTED_COUNT, Integer.valueOf(_selectedRows.size()));
```

**Step 6: Also send `selectedCount` in `buildFullState`**

Add at the end of `buildFullState`, after `updateViewport` call (after line 163):

No change needed — `updateViewport` already sends it now.

**Step 7: Rewrite `SelectCommand.execute`**

Replace the entire `SelectCommand` inner class (lines 322-360):

```java
	/**
	 * Handles selection requests from the client.
	 */
	static class SelectCommand extends ControlCommand {

		SelectCommand() {
			super("select");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.select");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			int rowIndex = ((Number) arguments.get("rowIndex")).intValue();

			if (rowIndex < 0 || rowIndex >= table._displayedRows.size()) {
				return HandlerResult.DEFAULT_RESULT;
			}

			boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
			boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

			Object rowObject = table._displayedRows.get(rowIndex);

			if ("multi".equals(table._selectionMode)) {
				if (shiftKey && table._selectionAnchor >= 0) {
					// Range selection from anchor to clicked row.
					int from = Math.min(table._selectionAnchor, rowIndex);
					int to = Math.max(table._selectionAnchor, rowIndex);
					if (!ctrlKey) {
						// Plain shift: replace selection with range.
						table._selectedRows.clear();
					}
					// Ctrl+shift: add range to existing selection.
					for (int i = from; i <= to; i++) {
						table._selectedRows.add(table._displayedRows.get(i));
					}
				} else if (ctrlKey) {
					// Toggle individual row.
					if (table._selectedRows.contains(rowObject)) {
						if (!table._selectionForced || table._selectedRows.size() > 1) {
							table._selectedRows.remove(rowObject);
						}
					} else {
						table._selectedRows.add(rowObject);
					}
					table._selectionAnchor = rowIndex;
				} else {
					// Plain click: replace selection, set anchor.
					table._selectedRows.clear();
					table._selectedRows.add(rowObject);
					table._selectionAnchor = rowIndex;
				}
			} else {
				// Single mode.
				if (ctrlKey && table._selectedRows.contains(rowObject) && !table._selectionForced) {
					table._selectedRows.clear();
				} else {
					table._selectedRows.clear();
					table._selectedRows.add(rowObject);
				}
			}

			table.updateViewport(table._viewportStart, table._viewportCount);
			return HandlerResult.DEFAULT_RESULT;
		}
	}
```

**Step 8: Add `SelectAllCommand` inner class**

Add after the `SelectCommand` class (before the closing brace of `ReactTableControl`):

```java
	/**
	 * Handles select-all / deselect-all from the header checkbox.
	 */
	static class SelectAllCommand extends ControlCommand {

		SelectAllCommand() {
			super("selectAll");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.selectAll");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			boolean selected = Boolean.TRUE.equals(arguments.get("selected"));

			if (selected) {
				table._selectedRows.addAll(table._displayedRows);
			} else {
				if (table._selectionForced && !table._displayedRows.isEmpty()) {
					table._selectedRows.clear();
					table._selectedRows.add(table._displayedRows.get(0));
				} else {
					table._selectedRows.clear();
				}
			}

			table.updateViewport(table._viewportStart, table._viewportCount);
			return HandlerResult.DEFAULT_RESULT;
		}
	}
```

**Step 9: Commit**

```
Ticket #29109: Add multi-selection with modifier keys, anchor, and selectAll command.
```

---

## Task 2: Client-Side Selection with Modifier Keys and Checkbox Column

**Purpose:** Update `TLTableView.tsx` to forward modifier keys with select commands, render a checkbox column in multi mode, and render a header checkbox with indeterminate state.

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`

**Step 1: Replace the full component**

Replace the entire file with:

```tsx
import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
}

interface RowState {
  id: string;
  index: number;
  selected: boolean;
  cells: Record<string, unknown>;
}

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * and multi-selection with checkbox column.
 *
 * State:
 * - columns: ColumnState[]
 * - totalRowCount: number
 * - viewportStart: number
 * - rows: RowState[]
 * - rowHeight: number
 * - selectionMode: 'single' | 'multi'
 * - selectionForced: boolean
 * - selectedCount: number
 */
const TLTableView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const selectedCount = (state.selectedCount as number) ?? 0;

  const isMulti = selectionMode === 'multi';
  const checkboxWidth = 40;

  const scrollContainerRef = React.useRef<HTMLDivElement>(null);
  const scrollTimeoutRef = React.useRef<number | null>(null);

  const totalHeight = totalRowCount * rowHeight;

  const handleScroll = React.useCallback(() => {
    if (scrollTimeoutRef.current !== null) {
      clearTimeout(scrollTimeoutRef.current);
    }
    scrollTimeoutRef.current = window.setTimeout(() => {
      const container = scrollContainerRef.current;
      if (!container) return;
      const scrollTop = container.scrollTop;
      const visibleCount = Math.ceil(container.clientHeight / rowHeight);
      const start = Math.floor(scrollTop / rowHeight);
      sendCommand('scroll', { start, count: visibleCount });
    }, 80);
  }, [sendCommand, rowHeight]);

  const handleSort = React.useCallback((columnName: string, currentDirection?: string) => {
    let newDirection: string;
    if (!currentDirection || currentDirection === 'desc') {
      newDirection = 'asc';
    } else {
      newDirection = 'desc';
    }
    sendCommand('sort', { column: columnName, direction: newDirection });
  }, [sendCommand]);

  const handleRowClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand]);

  const handleSelectAll = React.useCallback(() => {
    const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
    sendCommand('selectAll', { selected: !allSelected });
  }, [sendCommand, selectedCount, totalRowCount]);

  const tableWidth = columns.reduce((sum, col) => sum + col.width, 0)
    + (isMulti ? checkboxWidth : 0);

  // Header checkbox state.
  const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
  const someSelected = selectedCount > 0 && selectedCount < totalRowCount;

  // Ref for indeterminate state (cannot set via JSX attribute).
  const headerCheckboxRef = React.useCallback((el: HTMLInputElement | null) => {
    if (el) {
      el.indeterminate = someSelected;
    }
  }, [someSelected]);

  return (
    <div className="tlTableView">
      {/* Header */}
      <div className="tlTableView__header" style={{ width: tableWidth }}>
        <div className="tlTableView__headerRow">
          {isMulti && (
            <div className="tlTableView__headerCell tlTableView__checkboxCell"
              style={{ width: checkboxWidth, minWidth: checkboxWidth }}>
              <input
                type="checkbox"
                ref={headerCheckboxRef}
                className="tlTableView__checkbox"
                checked={allSelected}
                onChange={handleSelectAll}
              />
            </div>
          )}
          {columns.map((col) => (
            <div
              key={col.name}
              className={'tlTableView__headerCell' + (col.sortable ? ' tlTableView__headerCell--sortable' : '')}
              style={{ width: col.width, minWidth: col.width }}
              onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
            >
              <span className="tlTableView__headerLabel">{col.label}</span>
              {col.sortDirection && (
                <span className="tlTableView__sortIndicator">
                  {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                </span>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Scrollable body */}
      <div
        ref={scrollContainerRef}
        className="tlTableView__body"
        onScroll={handleScroll}
      >
        {/* Spacer for virtual scrolling */}
        <div style={{ height: totalHeight, position: 'relative' }}>
          {rows.map((row) => (
            <div
              key={row.id}
              className={
                'tlTableView__row' +
                (row.selected ? ' tlTableView__row--selected' : '')
              }
              style={{
                position: 'absolute',
                top: row.index * rowHeight,
                height: rowHeight,
                width: tableWidth,
              }}
              onClick={(e) => handleRowClick(row.index, e)}
            >
              {isMulti && (
                <div className="tlTableView__cell tlTableView__checkboxCell"
                  style={{ width: checkboxWidth, minWidth: checkboxWidth }}>
                  <input
                    type="checkbox"
                    className="tlTableView__checkbox"
                    checked={row.selected}
                    readOnly
                    tabIndex={-1}
                  />
                </div>
              )}
              {columns.map((col) => (
                <div
                  key={col.name}
                  className="tlTableView__cell"
                  style={{ width: col.width, minWidth: col.width }}
                >
                  <TLChild control={row.cells[col.name]} />
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TLTableView;
```

**Step 2: Commit**

```
Ticket #29109: Add checkbox column and modifier key support to TLTableView.
```

---

## Task 3: CSS for Checkbox Column

**Purpose:** Add styles for the checkbox cells.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Append checkbox styles**

Add after the `.tlTextCell` block (after line 1940):

```css

.tlTableView__checkboxCell {
	justify-content: center;
	flex-shrink: 0;
}

.tlTableView__checkbox {
	cursor: pointer;
	width: 1rem;
	height: 1rem;
	accent-color: var(--interactive, #0f62fe);
}
```

**Step 2: Commit**

```
Ticket #29109: Add checkbox column styles for multi-selection.
```

---

## Task 4: Demo Component — Switch to Multi-Selection

**Purpose:** Update the demo to use multi-selection mode so the new behavior can be tested.

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java`

**Step 1: Set multi-selection on the demo table**

Replace line 108:
```java
		return new ReactTableControl(rows, columns, cellProvider);
```

With:
```java
		ReactTableControl table = new ReactTableControl(rows, columns, cellProvider);
		table.setSelectionMode("multi");
		return table;
```

**Step 2: Commit**

```
Ticket #29109: Switch demo table to multi-selection mode.
```

---

## Task 5: Build and Verify

**Purpose:** Build the Vite bundle and both Maven modules.

**Step 1: Build React bundle**

```bash
cd com.top_logic.layout.react
node_modules/.bin/vite build --config vite.config.controls.ts
```

Verify `tl-react-controls.js` is updated.

**Step 2: Build com.top_logic.layout.react**

```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Expected: BUILD SUCCESS

**Step 3: Build com.top_logic.demo (incremental, NO clean)**

```bash
cd com.top_logic.demo
mvn install -DskipTests=true
```

Expected: BUILD SUCCESS

**Step 4: Commit build artifacts**

```
Ticket #29109: Rebuild React controls bundle.
```

---

## Task 6: Manual Testing

Test in the demo app (React > Table tab):

1. **Checkbox column visible** — leftmost column with checkboxes
2. **Plain click** — selects single row, deselects all others, sets anchor
3. **Ctrl-click** — toggles individual row selection
4. **Shift-click** — selects range from anchor to clicked row
5. **Ctrl+Shift-click** — adds range to existing selection
6. **Header checkbox** — toggles select-all / deselect-all
7. **Header checkbox indeterminate** — shows when some but not all rows are selected
8. **Virtual scrolling** — selection survives scroll (rows re-entering viewport show correct state)
9. **Sort** — selection persists across sort (row objects, not indices)
