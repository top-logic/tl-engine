# React Table Column Resize Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add interactive column resize via drag handles on header cell borders, with live feedback and server persistence.

**Architecture:** Client-side drag tracking with local width overrides for smooth feedback. On mouseup, the final width is sent to the server via a `columnResize` command. The server updates the `ColumnDef` and pushes updated column state.

**Tech Stack:** Java 17, React via tl-react-bridge, Maven, Vite

---

## File Overview

### Modified Files

- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Add `ColumnResizeCommand`
- `com.top_logic.layout.react/react-src/controls/TLTableView.tsx` — Add resize handles and drag logic
- `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` — Add resize handle styles

---

## Task 1: Server-Side ColumnResizeCommand

**Purpose:** Add a command that updates a column's width on the server when the user finishes dragging.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add `ColumnResizeCommand` to the command map**

Change the COMMANDS constant from:
```java
	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand());
```

To:
```java
	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand(),
		new ColumnResizeCommand());
```

**Step 2: Add the `ColumnResizeCommand` inner class**

Add before the closing brace of `ReactTableControl`, after `SelectAllCommand`:

```java
	/**
	 * Handles column resize from the client.
	 */
	static class ColumnResizeCommand extends ControlCommand {

		/** Minimum column width in pixels. */
		private static final int MIN_WIDTH = 50;

		ColumnResizeCommand() {
			super("columnResize");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.columnResize");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			String column = (String) arguments.get("column");
			int width = Math.max(MIN_WIDTH, ((Number) arguments.get("width")).intValue());

			for (ColumnDef col : table._columnDefs) {
				if (col.getName().equals(column)) {
					col.setWidth(width);
					break;
				}
			}

			// Push updated column definitions to client.
			table.putState(COLUMNS, table.buildColumnsState());
			return HandlerResult.DEFAULT_RESULT;
		}
	}
```

**Step 3: Make `buildColumnsState` package-private**

Change from:
```java
	private List<Map<String, Object>> buildColumnsState() {
```

To:
```java
	List<Map<String, Object>> buildColumnsState() {
```

This allows `ColumnResizeCommand` (same package, static inner class) to call it. Actually, since it's a static inner class of `ReactTableControl`, it already has access to private members via `table.buildColumnsState()`. So **no change needed** — keep it private.

**Step 4: Commit**

```
Ticket #29109: Add ColumnResizeCommand for server-side column width updates.
```

---

## Task 2: Client-Side Resize Handles and Drag Logic

**Purpose:** Add resize handles to header cells and drag-to-resize with live width feedback.

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

const MIN_COL_WIDTH = 50;

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * multi-selection with checkbox column, and column resize.
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

  // -- Resize state --
  const [columnWidthOverrides, setColumnWidthOverrides] = React.useState<Record<string, number>>({});
  const resizeRef = React.useRef<{ column: string; startX: number; startWidth: number } | null>(null);

  const getColWidth = (col: ColumnState): number => {
    return columnWidthOverrides[col.name] ?? col.width;
  };

  const totalHeight = totalRowCount * rowHeight;

  // -- Resize handlers --
  const handleResizeStart = React.useCallback((columnName: string, colWidth: number, event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    resizeRef.current = { column: columnName, startX: event.clientX, startWidth: colWidth };

    const onMouseMove = (e: MouseEvent) => {
      const info = resizeRef.current;
      if (!info) return;
      const newWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (e.clientX - info.startX));
      setColumnWidthOverrides((prev) => ({ ...prev, [info.column]: newWidth }));
    };

    const onMouseUp = (e: MouseEvent) => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
      const info = resizeRef.current;
      if (info) {
        const finalWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (e.clientX - info.startX));
        sendCommand('columnResize', { column: info.column, width: finalWidth });
        setColumnWidthOverrides((prev) => {
          const next = { ...prev };
          delete next[info.column];
          return next;
        });
        resizeRef.current = null;
      }
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }, [sendCommand]);

  // -- Scroll handler --
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

  // -- Sort handler --
  const handleSort = React.useCallback((columnName: string, currentDirection?: string) => {
    let newDirection: string;
    if (!currentDirection || currentDirection === 'desc') {
      newDirection = 'asc';
    } else {
      newDirection = 'desc';
    }
    sendCommand('sort', { column: columnName, direction: newDirection });
  }, [sendCommand]);

  // -- Selection handlers --
  const handleRowClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    if (event.shiftKey) {
      event.preventDefault();
    }
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand]);

  const handleCheckboxClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    event.stopPropagation();
    sendCommand('select', { rowIndex, ctrlKey: true, shiftKey: false });
  }, [sendCommand]);

  const handleSelectAll = React.useCallback(() => {
    const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
    sendCommand('selectAll', { selected: !allSelected });
  }, [sendCommand, selectedCount, totalRowCount]);

  // -- Computed values --
  const tableWidth = columns.reduce((sum, col) => sum + getColWidth(col), 0)
    + (isMulti ? checkboxWidth : 0);

  const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
  const someSelected = selectedCount > 0 && selectedCount < totalRowCount;

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
          {columns.map((col) => {
            const w = getColWidth(col);
            return (
              <div
                key={col.name}
                className={'tlTableView__headerCell' + (col.sortable ? ' tlTableView__headerCell--sortable' : '')}
                style={{ width: w, minWidth: w, position: 'relative' }}
                onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
              >
                <span className="tlTableView__headerLabel">{col.label}</span>
                {col.sortDirection && (
                  <span className="tlTableView__sortIndicator">
                    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                  </span>
                )}
                <div
                  className="tlTableView__resizeHandle"
                  onMouseDown={(e) => handleResizeStart(col.name, w, e)}
                />
              </div>
            );
          })}
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
                  style={{ width: checkboxWidth, minWidth: checkboxWidth }}
                  onClick={(e) => e.stopPropagation()}>
                  <input
                    type="checkbox"
                    className="tlTableView__checkbox"
                    checked={row.selected}
                    onChange={() => {/* handled by onClick */}}
                    onClick={(e) => handleCheckboxClick(row.index, e)}
                    tabIndex={-1}
                  />
                </div>
              )}
              {columns.map((col) => {
                const w = getColWidth(col);
                return (
                  <div
                    key={col.name}
                    className="tlTableView__cell"
                    style={{ width: w, minWidth: w }}
                  >
                    <TLChild control={row.cells[col.name]} />
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TLTableView;
```

Key changes from previous version:
- `columnWidthOverrides` state and `resizeRef` for live drag tracking
- `getColWidth()` helper reads override or server width
- `handleResizeStart()` sets up document-level mousemove/mouseup listeners
- Resize handle `<div>` in each header cell
- Header cells get `position: relative` for the absolute-positioned handle
- Both header and body cells use `getColWidth(col)` for widths

**Step 2: Commit**

```
Ticket #29109: Add column resize drag handles to TLTableView.
```

---

## Task 3: CSS for Resize Handles

**Purpose:** Style the resize handle affordance.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Append resize handle styles**

Add at the end of the file, after the `.tlTableView__checkbox` block:

```css

.tlTableView__resizeHandle {
	position: absolute;
	top: 0;
	right: -2px;
	width: 5px;
	height: 100%;
	cursor: col-resize;
	z-index: 1;
}

.tlTableView__resizeHandle:hover,
.tlTableView__resizeHandle:active {
	background: var(--interactive, #0f62fe);
	opacity: 0.3;
}
```

**Step 2: Commit**

```
Ticket #29109: Add resize handle CSS styles.
```

---

## Task 4: Build and Verify

**Step 1: Build com.top_logic.layout.react**

```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Expected: BUILD SUCCESS (includes Vite build via frontend-maven-plugin)

**Step 2: Commit build artifacts**

```
Ticket #29109: Rebuild React controls bundle.
```

---

## Task 5: Manual Testing

Test in the demo app (React > Table tab):

1. **Resize handle visible** — hover right edge of a header cell, cursor changes to `col-resize`, faint blue line appears
2. **Drag to resize** — mousedown on handle, drag right/left, column width updates live
3. **Minimum width** — cannot resize below 50px
4. **Body columns follow** — body cell widths match header during and after resize
5. **Sort still works** — clicking header label (not handle) still sorts
6. **Selection unaffected** — resizing doesn't trigger selection
