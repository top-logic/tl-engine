# React Table Control - Phase 1 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement the core React table control with virtual scrolling, cell child controls, single-column sorting, single row selection, and a demo page for verification.

**Architecture:** Server-side `ReactTableControl` extends `ReactControl`, manages a row viewport with prefetch buffer, and creates child `ReactControl` instances per visible cell via a pluggable `ReactCellControlProvider`. The client-side `TLTable` React component renders the grid, handles scroll events, and hosts cell controls via `<TLChild>`. A demo component in `com.top_logic.demo` provides a standalone table with synthetic data for early verification.

**Tech Stack:** Java 17, React via tl-react-bridge (SSE + commands), Maven, Vite

---

## File Overview

### New Files

**Java (com.top_logic.layout.react):**
- `src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Main table control
- `src/main/java/com/top_logic/layout/react/control/table/ReactCellControlProvider.java` — Interface for cell control creation
- `src/main/java/com/top_logic/layout/react/control/table/ReactTextCellControl.java` — Simple text display cell control
- `src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java` — Server-side column definition helper

**TypeScript (com.top_logic.layout.react):**
- `react-src/controls/TLTableView.tsx` — Main table React component (replaces existing minimal `TLTable.tsx`)

**Java (com.top_logic.demo):**
- `src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java` — Demo page component

**Layout XML (com.top_logic.demo):**
- `src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/table.layout.xml` — Demo tab layout

### Modified Files

- `com.top_logic.layout.react/react-src/controls-entry.ts` — Register `TLTableView`
- `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/index.layout.xml` — Add table demo tab

---

## Task 1: ReactCellControlProvider Interface

**Purpose:** Define the pluggable interface that the table uses to create cell controls. This decouples the table from any specific data type.

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactCellControlProvider.java`

**Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactControl;

/**
 * Provider that creates {@link ReactControl}s for table cells.
 *
 * <p>
 * The table delegates all cell rendering to this provider. Implementations map column types to
 * appropriate React controls (e.g. text display, links, checkboxes, select fields).
 * </p>
 */
public interface ReactCellControlProvider {

	/**
	 * Creates a {@link ReactControl} for the given cell.
	 *
	 * @param rowObject
	 *        The application object for the row.
	 * @param columnName
	 *        The column identifier.
	 * @param cellValue
	 *        The cell value as extracted by the column's accessor.
	 * @return A {@link ReactControl} to render in the cell. Must not be {@code null}.
	 */
	ReactControl createCellControl(Object rowObject, String columnName, Object cellValue);
}
```

**Step 2: Commit**

```
Ticket #29109: Add ReactCellControlProvider interface for table cell control creation.
```

---

## Task 2: ReactTextCellControl

**Purpose:** A simple read-only text cell control that displays a string value. This is the default cell control used when no specialized control is configured.

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTextCellControl.java`

**Step 1: Create the control**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactControl;

/**
 * A simple read-only cell control that displays a text value.
 *
 * <p>
 * Renders as a {@code TLTextCell} React component showing the string representation of the cell
 * value.
 * </p>
 */
public class ReactTextCellControl extends ReactControl {

	/** State key for the displayed text. */
	private static final String TEXT = "text";

	/**
	 * Creates a new {@link ReactTextCellControl}.
	 *
	 * @param value
	 *        The value to display, or {@code null}.
	 */
	public ReactTextCellControl(Object value) {
		super(null, "TLTextCell");
		putState(TEXT, value != null ? value.toString() : "");
	}

	/**
	 * Updates the displayed text.
	 */
	public void setText(Object value) {
		putState(TEXT, value != null ? value.toString() : "");
	}
}
```

**Step 2: Create the React component**

Create `com.top_logic.layout.react/react-src/controls/TLTextCell.tsx`:

```typescript
import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Simple read-only text cell for table display.
 *
 * State:
 * - text: string - the text to display
 */
const TLTextCell: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const text = (state.text as string) ?? '';

  return <span className="tlTextCell">{text}</span>;
};

export default TLTextCell;
```

**Step 3: Register in controls-entry.ts**

Add to `com.top_logic.layout.react/react-src/controls-entry.ts`:

```typescript
import TLTextCell from './controls/TLTextCell';
// ...
register('TLTextCell', TLTextCell);
```

**Step 4: Commit**

```
Ticket #29109: Add ReactTextCellControl and TLTextCell component for table cells.
```

---

## Task 3: ColumnDef Helper

**Purpose:** Server-side helper that holds column metadata sent to the client (name, label, width, sortable, sort direction).

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java`

**Step 1: Create the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server-side column definition that serializes to a JSON map for the React table state.
 *
 * <p>
 * Holds the column metadata needed by the client: name, label, width, sortability, and current sort
 * direction.
 * </p>
 */
public class ColumnDef {

	private final String _name;

	private final String _label;

	private int _width;

	private boolean _sortable;

	private String _sortDirection;

	/**
	 * Creates a new {@link ColumnDef}.
	 *
	 * @param name
	 *        The column identifier (unique within the table).
	 * @param label
	 *        The display label for the column header.
	 */
	public ColumnDef(String name, String label) {
		_name = name;
		_label = label;
		_width = 150;
		_sortable = true;
	}

	/**
	 * The column identifier.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the default width in pixels.
	 */
	public ColumnDef setWidth(int width) {
		_width = width;
		return this;
	}

	/**
	 * Sets whether this column is sortable.
	 */
	public ColumnDef setSortable(boolean sortable) {
		_sortable = sortable;
		return this;
	}

	/**
	 * Sets the current sort direction ({@code "asc"}, {@code "desc"}, or {@code null} for
	 * unsorted).
	 */
	public ColumnDef setSortDirection(String direction) {
		_sortDirection = direction;
		return this;
	}

	/**
	 * Converts this column definition to a map suitable for JSON serialization in the React state.
	 */
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", _name);
		map.put("label", _label);
		map.put("width", Integer.valueOf(_width));
		map.put("sortable", Boolean.valueOf(_sortable));
		if (_sortDirection != null) {
			map.put("sortDirection", _sortDirection);
		}
		return map;
	}
}
```

**Step 2: Commit**

```
Ticket #29109: Add ColumnDef helper for React table column metadata.
```

---

## Task 4: ReactTableControl — Core with Virtual Scrolling

**Purpose:** The main server-side table control. Manages columns, rows, viewport with prefetch, cell control lifecycle, scroll command, and sort command.

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Create the control**

This is the largest piece. Key responsibilities:
- Hold a list of row objects and column definitions
- Maintain a viewport window (start index + count) with a prefetch buffer
- Create/cache/unregister `ReactControl` instances for visible cells
- Handle `scroll` and `sort` commands

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Server-side React control that renders a table with virtual scrolling.
 *
 * <p>
 * The table is a grid container: each visible cell hosts a child {@link ReactControl} created by a
 * {@link ReactCellControlProvider}. The table itself has no knowledge of the data types it
 * displays.
 * </p>
 *
 * <p>
 * Virtual scrolling is implemented by maintaining a viewport window. The server sends only the
 * visible rows plus a prefetch buffer to the client. On scroll, the client requests a new viewport,
 * and the server creates/removes cell controls accordingly.
 * </p>
 */
public class ReactTableControl extends ReactControl {

	// -- State keys --

	private static final String COLUMNS = "columns";

	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	private static final String VIEWPORT_START = "viewportStart";

	private static final String ROWS = "rows";

	private static final String ROW_HEIGHT = "rowHeight";

	private static final String SELECTION_MODE = "selectionMode";

	// -- Configuration --

	/** Number of rows to prefetch above and below the visible area. */
	private static final int PREFETCH_ROWS = 20;

	// -- Commands --

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand());

	// -- Fields --

	private final List<ColumnDef> _columnDefs;

	private final ReactCellControlProvider _cellProvider;

	private List<Object> _allRows;

	private List<Object> _displayedRows;

	private int _viewportStart;

	private int _viewportCount;

	private String _sortColumn;

	private boolean _sortAscending = true;

	private Comparator<Object> _sortComparator;

	private Set<Object> _selectedRows = new HashSet<>();

	private String _selectionMode = "single";

	/**
	 * Cache of cell controls for currently visible rows. Keyed by row object, then column name.
	 */
	private final Map<Object, Map<String, ReactControl>> _rowCellCache = new LinkedHashMap<>();

	/**
	 * Creates a new {@link ReactTableControl}.
	 *
	 * @param rows
	 *        The full list of row objects.
	 * @param columnDefs
	 *        The column definitions.
	 * @param cellProvider
	 *        Provider for creating cell controls.
	 */
	public ReactTableControl(List<?> rows, List<ColumnDef> columnDefs,
			ReactCellControlProvider cellProvider) {
		super(null, "TLTableView", COMMANDS);
		_allRows = new ArrayList<>(rows);
		_displayedRows = new ArrayList<>(_allRows);
		_columnDefs = columnDefs;
		_cellProvider = cellProvider;
		_viewportStart = 0;
		_viewportCount = 50;

		putState(ROW_HEIGHT, Integer.valueOf(36));
		putState(SELECTION_MODE, _selectionMode);
		buildFullState();
	}

	/**
	 * Sets the selection mode.
	 *
	 * @param mode
	 *        One of {@code "single"}, {@code "multi"}, {@code "range"}.
	 */
	public void setSelectionMode(String mode) {
		_selectionMode = mode;
		putState(SELECTION_MODE, mode);
	}

	/**
	 * Provides a comparator factory for sorting. Subclasses or configuration can override this.
	 *
	 * @param columnName
	 *        The column to sort by.
	 * @param ascending
	 *        Whether to sort ascending.
	 * @return A comparator, or {@code null} if the column is not sortable.
	 */
	protected Comparator<Object> createSortComparator(String columnName, boolean ascending) {
		// Default: sort by toString() of the cell value via the cell provider.
		// Real implementations would use the model's accessor and comparator.
		return (a, b) -> {
			String va = String.valueOf(getCellValue(a, columnName));
			String vb = String.valueOf(getCellValue(b, columnName));
			int result = va.compareToIgnoreCase(vb);
			return ascending ? result : -result;
		};
	}

	/**
	 * Extracts the cell value for a row and column. Override to use model accessors.
	 */
	@SuppressWarnings("unchecked")
	protected Object getCellValue(Object rowObject, String columnName) {
		if (rowObject instanceof Map) {
			return ((Map<String, Object>) rowObject).get(columnName);
		}
		return null;
	}

	// -- State building --

	private void buildFullState() {
		putState(COLUMNS, buildColumnsState());
		putState(TOTAL_ROW_COUNT, Integer.valueOf(_displayedRows.size()));
		updateViewport(_viewportStart, _viewportCount);
	}

	private List<Map<String, Object>> buildColumnsState() {
		List<Map<String, Object>> columns = new ArrayList<>();
		for (ColumnDef col : _columnDefs) {
			columns.add(col.toStateMap());
		}
		return columns;
	}

	private void updateViewport(int start, int count) {
		int totalRows = _displayedRows.size();

		// Apply prefetch buffer.
		int bufferedStart = Math.max(0, start - PREFETCH_ROWS);
		int bufferedEnd = Math.min(totalRows, start + count + PREFETCH_ROWS);

		// Determine rows leaving and entering the viewport.
		Set<Object> newRowObjects = new HashSet<>();
		for (int i = bufferedStart; i < bufferedEnd; i++) {
			newRowObjects.add(_displayedRows.get(i));
		}

		// Remove cell controls for rows that left the viewport.
		Set<Object> oldRowObjects = new HashSet<>(_rowCellCache.keySet());
		for (Object oldRow : oldRowObjects) {
			if (!newRowObjects.contains(oldRow)) {
				Map<String, ReactControl> cells = _rowCellCache.remove(oldRow);
				if (cells != null) {
					for (ReactControl cell : cells.values()) {
						unregisterChildControl(cell);
					}
				}
			}
		}

		// Build row state for the new viewport.
		List<Map<String, Object>> rowStates = new ArrayList<>();
		for (int i = bufferedStart; i < bufferedEnd; i++) {
			Object rowObject = _displayedRows.get(i);
			Map<String, ReactControl> cells = _rowCellCache.get(rowObject);
			if (cells == null) {
				cells = createCellControls(rowObject);
				_rowCellCache.put(rowObject, cells);
			}

			Map<String, Object> rowState = new LinkedHashMap<>();
			rowState.put("id", "row_" + i);
			rowState.put("index", Integer.valueOf(i));
			rowState.put("selected", Boolean.valueOf(_selectedRows.contains(rowObject)));
			rowState.put("cells", cells);
			rowStates.add(rowState);
		}

		_viewportStart = start;
		_viewportCount = count;

		putState(VIEWPORT_START, Integer.valueOf(bufferedStart));
		putState(ROWS, rowStates);
	}

	private Map<String, ReactControl> createCellControls(Object rowObject) {
		Map<String, ReactControl> cells = new LinkedHashMap<>();
		for (ColumnDef col : _columnDefs) {
			Object cellValue = getCellValue(rowObject, col.getName());
			ReactControl cellControl = _cellProvider.createCellControl(rowObject, col.getName(), cellValue);
			registerChildControl(cellControl);
			cells.put(col.getName(), cellControl);
		}
		return cells;
	}

	@Override
	protected void cleanupChildren() {
		for (Map<String, ReactControl> cells : _rowCellCache.values()) {
			for (ReactControl cell : cells.values()) {
				cell.cleanupTree();
			}
		}
		_rowCellCache.clear();
	}

	// -- Commands --

	/**
	 * Handles scroll requests from the client.
	 */
	static class ScrollCommand extends ControlCommand {

		ScrollCommand() {
			super("scroll");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.scroll");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			int start = ((Number) arguments.get("start")).intValue();
			int count = ((Number) arguments.get("count")).intValue();
			table.updateViewport(start, count);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Handles sort requests from the client.
	 */
	static class SortCommand extends ControlCommand {

		SortCommand() {
			super("sort");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.sort");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			String column = (String) arguments.get("column");
			String direction = (String) arguments.get("direction");
			boolean ascending = !"desc".equals(direction);

			table._sortColumn = column;
			table._sortAscending = ascending;

			// Update sort direction in column defs.
			for (ColumnDef col : table._columnDefs) {
				if (col.getName().equals(column)) {
					col.setSortDirection(ascending ? "asc" : "desc");
				} else {
					col.setSortDirection(null);
				}
			}

			// Sort the displayed rows.
			Comparator<Object> comparator = table.createSortComparator(column, ascending);
			if (comparator != null) {
				table._displayedRows.sort(comparator);
			}

			// Rebuild: reset viewport to top, send new columns + rows.
			table.buildFullState();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

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

			Object rowObject = table._displayedRows.get(rowIndex);

			if ("single".equals(table._selectionMode)) {
				table._selectedRows.clear();
				table._selectedRows.add(rowObject);
			} else {
				if (table._selectedRows.contains(rowObject)) {
					table._selectedRows.remove(rowObject);
				} else {
					table._selectedRows.add(rowObject);
				}
			}

			// Update selection state in the current viewport rows.
			table.updateViewport(table._viewportStart, table._viewportCount);
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
```

**Step 2: Commit**

```
Ticket #29109: Add ReactTableControl with virtual scrolling, sort, and selection.
```

---

## Task 5: TLTableView React Component

**Purpose:** The client-side React component that renders the table grid with virtual scrolling, column headers with sort indicators, and hosts cell child controls via `<TLChild>`.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts` — Add registration

**Step 1: Create the component**

```typescript
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
 * React table component with virtual scrolling and server-driven cell controls.
 *
 * State:
 * - columns: ColumnState[]
 * - totalRowCount: number
 * - viewportStart: number
 * - rows: RowState[]
 * - rowHeight: number
 * - selectionMode: 'single' | 'multi' | 'range'
 */
const TLTableView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const viewportStart = (state.viewportStart as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;

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

  const handleRowClick = React.useCallback((rowIndex: number) => {
    sendCommand('select', { rowIndex });
  }, [sendCommand]);

  const tableWidth = columns.reduce((sum, col) => sum + col.width, 0);

  return (
    <div className="tlTableView">
      {/* Header */}
      <div className="tlTableView__header" style={{ width: tableWidth }}>
        <div className="tlTableView__headerRow">
          {columns.map((col) => (
            <div
              key={col.name}
              className="tlTableView__headerCell"
              style={{ width: col.width, minWidth: col.width }}
              onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
            >
              <span className="tlTableView__headerLabel">{col.label}</span>
              {col.sortDirection && (
                <span className="tlTableView__sortIndicator">
                  {col.sortDirection === 'asc' ? ' \u25B2' : ' \u25BC'}
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
              onClick={() => handleRowClick(row.index)}
            >
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

**Step 2: Register in controls-entry.ts**

Add to `com.top_logic.layout.react/react-src/controls-entry.ts`:

```typescript
import TLTableView from './controls/TLTableView';
// ... at the end:
register('TLTableView', TLTableView);
```

Also register the `TLTextCell` from Task 2 if not yet done.

**Step 3: Commit**

```
Ticket #29109: Add TLTableView React component with virtual scrolling.
```

---

## Task 6: CSS Styles for TLTableView

**Purpose:** Basic CSS for the table layout, header, virtual scrolling body, row selection, and sort indicators.

**Files:**
- Check existing CSS location. Likely `com.top_logic.layout.react/src/main/webapp/style/` or inline in the component. Look for where other control styles live (e.g. `TLSidebar`, `TLGrid` styles).
- If styles are typically in a central CSS file, add there. If no central file exists, add as a `<style>` block or a new CSS file imported by the controls bundle.

Find where existing React control CSS lives by checking:
```bash
find com.top_logic.layout.react/src/main/webapp -name "*.css" -o -name "*.scss"
```
or look for style imports in `controls-entry.ts`.

**Step 1: Add styles**

The styles should cover:
- `.tlTableView` — outer container with `display: flex; flex-direction: column; overflow: hidden;`
- `.tlTableView__header` — fixed header row, `overflow: hidden;`
- `.tlTableView__headerRow` — `display: flex;`
- `.tlTableView__headerCell` — flex cell with cursor pointer for sortable columns, border-bottom
- `.tlTableView__body` — `flex: 1; overflow-y: auto; overflow-x: auto;`
- `.tlTableView__row` — `display: flex;`
- `.tlTableView__row--selected` — highlight background
- `.tlTableView__cell` — `display: flex; align-items: center; padding: 0 8px; overflow: hidden;`
- `.tlTableView__sortIndicator` — small triangle indicators

**Step 2: Commit**

```
Ticket #29109: Add CSS styles for TLTableView component.
```

---

## Task 7: Demo Component and Layout

**Purpose:** A demo page in `com.top_logic.demo` that creates a `ReactTableControl` with synthetic data (e.g. 1000 rows, 5 columns) so the table can be tested visually early on.

**Files:**
- Create: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java`
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/table.layout.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/index.layout.xml`

**Step 1: Create the demo component**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.table.ColumnDef;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React table control.
 *
 * <p>
 * Creates a {@link ReactTableControl} with 1000 synthetic rows and 5 columns to demonstrate virtual
 * scrolling, sorting, and row selection.
 * </p>
 */
public class DemoReactTableComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactTableComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactTableControl _tableControl;

	/**
	 * Creates a new {@link DemoReactTableComponent}.
	 */
	public DemoReactTableComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_tableControl == null) {
			_tableControl = createDemoTable();
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("React Table Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("A table with 1000 rows and virtual scrolling. "
			+ "Click column headers to sort. Click rows to select.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 500px; border: 1px solid #ccc;");
		out.endBeginTag();
		_tableControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);
	}

	private ReactTableControl createDemoTable() {
		List<ColumnDef> columns = new ArrayList<>();
		columns.add(new ColumnDef("id", "ID").setWidth(80));
		columns.add(new ColumnDef("name", "Name").setWidth(200));
		columns.add(new ColumnDef("department", "Department").setWidth(150));
		columns.add(new ColumnDef("email", "Email").setWidth(250));
		columns.add(new ColumnDef("status", "Status").setWidth(120));

		String[] departments = { "Engineering", "Marketing", "Sales", "HR", "Finance", "Support" };
		String[] statuses = { "Active", "Inactive", "On Leave" };

		List<Map<String, Object>> rows = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", String.valueOf(i + 1));
			row.put("name", "Employee " + (i + 1));
			row.put("department", departments[i % departments.length]);
			row.put("email", "employee" + (i + 1) + "@example.com");
			row.put("status", statuses[i % statuses.length]);
			rows.add(row);
		}

		ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(cellValue);
		};

		return new ReactTableControl(rows, columns, cellProvider);
	}
}
```

**Step 2: Create the layout file**

Create `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/table.layout.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<config:template-call
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	template="com.top_logic/tab.template.xml"
>
	<arguments>
		<tabLabel key="dynamic.reactDemo.table.tab">
			<en>Table</en>
			<de>Tabelle</de>
		</tabLabel>
		<components>
			<component
				class="com.top_logic.demo.react.DemoReactTableComponent"
				name="reactTableDemo"
			/>
		</components>
	</arguments>
</config:template-call>
```

**Step 3: Add to index.layout.xml**

Add a `<layout-reference>` to the tabbar in `index.layout.xml`:

```xml
<layout-reference resource="com.top_logic.demo/technical/reactDemo/table.layout.xml"/>
```

Insert it after the `sidebar.layout.xml` reference (before `app.layout.xml`).

**Step 4: Commit**

```
Ticket #29109: Add React table demo component with 1000 synthetic rows.
```

---

## Task 8: Build, Verify, and Fix

**Purpose:** Build both modules, run the Vite build for the React bundle, and verify everything compiles.

**Step 1: Build the React bundle**

```bash
cd com.top_logic.layout.react
npx vite build --config vite.config.controls.ts
```

Check that `src/main/webapp/script/tl-react-controls.js` is updated and includes `TLTableView` and `TLTextCell`.

**Step 2: Build com.top_logic.layout.react**

```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

**Step 3: Build com.top_logic.demo (incremental, NO clean)**

```bash
cd com.top_logic.demo
mvn install -DskipTests=true
```

**IMPORTANT:** Never use `mvn clean` on `com.top_logic.demo` — it causes `PluginContainerException` errors.

**Step 4: Fix any compilation errors**

Address any issues found during the build. Common things to check:
- Missing imports
- Encoding issues (files must be ISO-8859-1)
- React imports must use `'tl-react-bridge'` not `'react'`

**Step 5: Commit fixes if needed**

```
Ticket #29109: Fix build issues for React table control.
```

---

## Task 9: Manual Testing and Iteration

**Purpose:** Start the demo application and verify the table works in the browser.

**Step 1: Start the demo application**

Follow the standard procedure for running `com.top_logic.demo`.

**Step 2: Navigate to the React Table demo tab**

Open the React demo section and click the "Table" tab.

**Step 3: Verify these behaviors:**

- [ ] Table renders with 5 columns and a scrollbar
- [ ] Scrolling loads new rows (check browser DevTools Network tab for SSE events)
- [ ] Scrolling is smooth with no visible gaps (prefetch buffer works)
- [ ] Clicking a column header sorts the table
- [ ] Sort indicator (triangle) appears in the sorted column
- [ ] Clicking the same column again reverses sort direction
- [ ] Clicking a row highlights it (selection)
- [ ] Scrolling to the bottom shows row 1000

**Step 4: Fix any issues found and commit**

```
Ticket #29109: Fix issues found during React table manual testing.
```

---

## Summary of Deliverables

| # | Task | Files | Key Artifact |
|---|------|-------|-------------|
| 1 | ReactCellControlProvider | 1 Java | Interface |
| 2 | ReactTextCellControl + TLTextCell | 1 Java + 1 TSX | Default cell control |
| 3 | ColumnDef | 1 Java | Column metadata helper |
| 4 | ReactTableControl | 1 Java | Main table control (scroll, sort, select) |
| 5 | TLTableView | 1 TSX + modify entry | React component |
| 6 | CSS styles | 1 CSS/style | Visual styling |
| 7 | Demo component | 1 Java + 1 XML + modify index | Demo with 1000 rows |
| 8 | Build verification | — | Compilation check |
| 9 | Manual testing | — | Visual verification |

## What Phase 1 Does NOT Include (Deferred to Later Phases)

- Multi-selection / range selection (Phase 2)
- Multi-column sort (Phase 2)
- Column resize / reorder (Phase 2)
- Frozen columns (Phase 2)
- Filtering (Phase 3)
- Tree tables (Phase 4)
- Inline editing (Phase 5)
- Drag-and-drop, export, personalization (Phase 6)
