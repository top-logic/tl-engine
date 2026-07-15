# React Table: TableModel Integration — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor ReactTableControl to use TopLogic's `TableModel` as its data source, replacing the static row list with dynamic model access, delegating sorting to the model, and listening for change events.

**Architecture:** The constructor accepts a `TableModel` + `ReactCellControlProvider`. All row access goes through `model.getDisplayedRows()`, cell values through `model.getValueAt()`, and sorting through `model.setOrder()`. A `TableModelListener` triggers viewport rebuilds on model changes. `ColumnDef` remains as the serialization adapter, populated from `ColumnConfiguration`.

**Tech Stack:** Java 17, TopLogic TableModel API, Maven

---

## File Overview

### Modified Files

**Java (com.top_logic.layout.react):**
- `src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Replace constructor, row access, sort delegation, add listener
- `src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java` — Add factory method from ColumnConfiguration

**Java (com.top_logic.demo):**
- `src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java` — Use ObjectTableModel

---

## Task 1: Add ColumnDef Factory Method from ColumnConfiguration

**Purpose:** Add a static factory that builds a `ColumnDef` from a `ColumnConfiguration`, decoupling the React serialization from the model API.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java`

**Step 1: Add imports and factory method**

Add these imports at the top:

```java
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
```

After the `toStateMap()` method, add:

```java
/**
 * Creates a {@link ColumnDef} from a {@link ColumnConfiguration}.
 *
 * @param tableConfig
 *        The table configuration (used for label resolution).
 * @param colConfig
 *        The column configuration.
 * @param columnName
 *        The column name.
 * @return A new column definition populated from the configuration.
 */
public static ColumnDef fromColumnConfiguration(TableConfiguration tableConfig,
		ColumnConfiguration colConfig, String columnName) {
	String label = Column.getColumnLabel(tableConfig, colConfig, columnName);
	ColumnDef def = new ColumnDef(columnName, label);
	def.setSortable(colConfig.isSortable());
	String widthStr = colConfig.getDefaultColumnWidth();
	if (widthStr != null && !widthStr.isEmpty()) {
		try {
			def.setWidth(Integer.parseInt(widthStr.replaceAll("[^0-9]", "")));
		} catch (NumberFormatException ex) {
			// Keep default width.
		}
	}
	return def;
}
```

**Step 2: Commit**

```
Ticket #29109: Add ColumnDef factory method from ColumnConfiguration.
```

---

## Task 2: Refactor ReactTableControl to Use TableModel

**Purpose:** Replace the static row list with a `TableModel` data source. All row access, cell value extraction, and sorting go through the model. A `TableModelListener` rebuilds the viewport on model changes.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Update imports**

Replace the current import block with:

```java
import java.util.ArrayList;
import java.util.Comparator;
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
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.tool.boundsec.HandlerResult;
```

**Step 2: Replace fields**

Remove these fields:

```java
private final List<ColumnDef> _columnDefs;
private final ReactCellControlProvider _cellProvider;
private List<Object> _allRows;
private List<Object> _displayedRows;
```

Replace with:

```java
private final TableModel _tableModel;

private final ReactCellControlProvider _cellProvider;

private List<ColumnDef> _columnDefs;

/** Suppresses model events during command execution to prevent double rebuilds. */
private boolean _suppressModelEvents;

private final TableModelListener _modelListener = this::handleModelEvent;
```

**Step 3: Replace the constructor**

Remove the old constructor (lines 136-150) and replace with:

```java
/**
 * Creates a new {@link ReactTableControl}.
 *
 * @param model
 *        The table model providing rows, columns, and sorting.
 * @param cellProvider
 *        Provider for creating cell controls.
 */
public ReactTableControl(TableModel model, ReactCellControlProvider cellProvider) {
	super(null, "TLTableView", COMMANDS);
	_tableModel = model;
	_cellProvider = cellProvider;
	_viewportStart = 0;
	_viewportCount = 50;

	_columnDefs = buildColumnDefsFromModel();

	_tableModel.addTableModelListener(_modelListener);

	putState(ROW_HEIGHT, Integer.valueOf(36));
	putState(SELECTION_MODE, _selectionMode);
	putState(FROZEN_COLUMN_COUNT, Integer.valueOf(0));
	buildFullState();
}
```

**Step 4: Add helper methods**

After `setFrozenColumnCount()`, add:

```java
/**
 * The underlying table model.
 */
protected TableModel getTableModel() {
	return _tableModel;
}

/**
 * Convenience accessor for the model's displayed (filtered and sorted) row list.
 */
private List<?> getDisplayedRows() {
	return _tableModel.getDisplayedRows();
}

/**
 * Builds {@link ColumnDef} instances from the model's column configuration.
 */
private List<ColumnDef> buildColumnDefsFromModel() {
	TableConfiguration tableConfig = _tableModel.getTableConfiguration();
	List<ColumnDef> defs = new ArrayList<>();
	for (String name : _tableModel.getColumnNames()) {
		ColumnConfiguration colConfig = _tableModel.getColumnDescription(name);
		defs.add(ColumnDef.fromColumnConfiguration(tableConfig, colConfig, name));
	}
	return defs;
}

/**
 * Handles events from the underlying {@link TableModel}.
 */
private void handleModelEvent(TableModelEvent event) {
	if (_suppressModelEvents) {
		return;
	}
	switch (event.getType()) {
		case TableModelEvent.INSERT:
		case TableModelEvent.DELETE:
		case TableModelEvent.INVALIDATE:
			buildFullState();
			break;
		case TableModelEvent.UPDATE:
			updateViewport(_viewportStart, _viewportCount);
			break;
		default:
			break;
	}
}
```

**Step 5: Update getCellValue()**

Replace the current `getCellValue()` method with:

```java
/**
 * Extracts the cell value for a row and column from the underlying model.
 */
protected Object getCellValue(Object rowObject, String columnName) {
	return _tableModel.getValueAt(rowObject, columnName);
}
```

**Step 6: Update createSortComparator()**

Replace the current `createSortComparator()` method with:

```java
/**
 * Creates a row-level comparator for sorting by a single column.
 *
 * <p>
 * Uses the column's {@link ColumnConfiguration#getComparator()} to compare cell values
 * extracted from the model.
 * </p>
 *
 * @param columnName
 *        The column to sort by.
 * @param ascending
 *        Whether to sort ascending.
 * @return A comparator, or {@code null} if the column is not sortable.
 */
@SuppressWarnings("unchecked")
protected Comparator<Object> createSortComparator(String columnName, boolean ascending) {
	ColumnConfiguration colConfig = _tableModel.getColumnDescription(columnName);
	if (colConfig == null || !colConfig.isSortable()) {
		return null;
	}
	Comparator<Object> cellComparator =
		ascending ? colConfig.getComparator() : colConfig.getDescendingComparator();
	if (cellComparator == null) {
		return null;
	}
	return (a, b) -> {
		Object va = _tableModel.getValueAt(a, columnName);
		Object vb = _tableModel.getValueAt(b, columnName);
		return cellComparator.compare(va, vb);
	};
}
```

**Step 7: Update buildFullState() and updateViewport()**

Replace `buildFullState()`:

```java
private void buildFullState() {
	putState(COLUMNS, buildColumnsState());
	putState(TOTAL_ROW_COUNT, Integer.valueOf(getDisplayedRows().size()));
	updateViewport(_viewportStart, _viewportCount);
}
```

In `updateViewport()`, replace all four occurrences of `_displayedRows` with `getDisplayedRows()`. Specifically, change the method to:

```java
private void updateViewport(int start, int count) {
	List<?> displayedRows = getDisplayedRows();
	int totalRows = displayedRows.size();

	// Apply prefetch buffer.
	int bufferedStart = Math.max(0, start - PREFETCH_ROWS);
	int bufferedEnd = Math.min(totalRows, start + count + PREFETCH_ROWS);

	// Determine rows leaving and entering the viewport.
	Set<Object> newRowObjects = new HashSet<>();
	for (int i = bufferedStart; i < bufferedEnd; i++) {
		newRowObjects.add(displayedRows.get(i));
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
		Object rowObject = displayedRows.get(i);
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
	putState(SELECTED_COUNT, Integer.valueOf(_selectedRows.size()));
}
```

**Step 8: Update SortCommand to delegate to model**

Replace the `SortCommand.execute()` body with:

```java
@Override
protected HandlerResult execute(DisplayContext context, Control control,
		Map<String, Object> arguments) {
	ReactTableControl table = (ReactTableControl) control;
	String column = (String) arguments.get("column");
	String direction = (String) arguments.get("direction");
	boolean ascending = !"desc".equals(direction);
	String mode = (String) arguments.get("mode");

	if ("add".equals(mode)) {
		// Shift+click: add to chain or toggle existing.
		boolean found = false;
		for (SortKey key : table._sortKeys) {
			if (key._columnName.equals(column)) {
				key._ascending = ascending;
				found = true;
				break;
			}
		}
		if (!found) {
			table._sortKeys.add(new SortKey(column, ascending));
		}
	} else {
		// Plain click: replace entire sort with single column.
		table._sortKeys.clear();
		table._sortKeys.add(new SortKey(column, ascending));
	}

	// Update column defs with sort metadata.
	table.applySortKeysToColumnDefs();

	// Delegate sorting to the model.
	table._suppressModelEvents = true;
	try {
		Comparator<Object> comparator = table.createChainedComparator();
		if (comparator != null) {
			table._tableModel.setOrder(comparator);
		}
	} finally {
		table._suppressModelEvents = false;
	}

	// Rebuild viewport with new sort order.
	table.buildFullState();
	return HandlerResult.DEFAULT_RESULT;
}
```

**Step 9: Update SelectCommand**

In `SelectCommand.execute()`, replace all occurrences of `table._displayedRows` with `table.getDisplayedRows()`:

```java
@Override
protected HandlerResult execute(DisplayContext context, Control control,
		Map<String, Object> arguments) {
	ReactTableControl table = (ReactTableControl) control;
	int rowIndex = ((Number) arguments.get("rowIndex")).intValue();

	List<?> displayedRows = table.getDisplayedRows();
	if (rowIndex < 0 || rowIndex >= displayedRows.size()) {
		return HandlerResult.DEFAULT_RESULT;
	}

	boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
	boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

	Object rowObject = displayedRows.get(rowIndex);

	if ("multi".equals(table._selectionMode)) {
		if (shiftKey && table._selectionAnchor >= 0) {
			int from = Math.min(table._selectionAnchor, rowIndex);
			int to = Math.max(table._selectionAnchor, rowIndex);
			for (int i = from; i <= to; i++) {
				Object row = displayedRows.get(i);
				if (table._anchorAdded) {
					table._selectedRows.add(row);
				} else {
					if (!table._selectionForced || table._selectedRows.size() > 1) {
						table._selectedRows.remove(row);
					}
				}
			}
		} else if (ctrlKey) {
			if (table._selectedRows.contains(rowObject)) {
				if (!table._selectionForced || table._selectedRows.size() > 1) {
					table._selectedRows.remove(rowObject);
					table._anchorAdded = false;
				}
			} else {
				table._selectedRows.add(rowObject);
				table._anchorAdded = true;
			}
			table._selectionAnchor = rowIndex;
		} else {
			table._selectedRows.clear();
			table._selectedRows.add(rowObject);
			table._selectionAnchor = rowIndex;
			table._anchorAdded = true;
		}
	} else {
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
```

**Step 10: Update SelectAllCommand**

In `SelectAllCommand.execute()`, replace `table._displayedRows` with `table.getDisplayedRows()`:

```java
@Override
protected HandlerResult execute(DisplayContext context, Control control,
		Map<String, Object> arguments) {
	ReactTableControl table = (ReactTableControl) control;
	boolean selected = Boolean.TRUE.equals(arguments.get("selected"));

	List<?> displayedRows = table.getDisplayedRows();
	if (selected) {
		table._selectedRows.addAll(displayedRows);
	} else {
		if (table._selectionForced && !displayedRows.isEmpty()) {
			table._selectedRows.clear();
			table._selectedRows.add(displayedRows.get(0));
		} else {
			table._selectedRows.clear();
		}
	}

	table.updateViewport(table._viewportStart, table._viewportCount);
	return HandlerResult.DEFAULT_RESULT;
}
```

**Step 11: Update cleanupChildren()**

Add listener unregistration:

```java
@Override
protected void cleanupChildren() {
	_tableModel.removeTableModelListener(_modelListener);
	for (Map<String, ReactControl> cells : _rowCellCache.values()) {
		for (ReactControl cell : cells.values()) {
			cell.cleanupTree();
		}
	}
	_rowCellCache.clear();
}
```

**Step 12: Commit**

```
Ticket #29109: Refactor ReactTableControl to use TableModel as data source.
```

---

## Task 3: Update DemoReactTableComponent to Use ObjectTableModel

**Purpose:** Update the demo to create an `ObjectTableModel` with `TableConfiguration` and pass it to the refactored `ReactTableControl`.

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java`

**Step 1: Replace the entire createDemoTable() method and imports**

Replace the import block with:

```java
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
```

Replace `createDemoTable()` with:

```java
@SuppressWarnings("deprecation")
private ReactTableControl createDemoTable() {
	// Column definitions.
	List<String> columnNames = List.of("id", "name", "department", "email", "status");

	TableConfiguration tableConfig = TableConfiguration.table();
	tableConfig.getDefaultColumn().setAccessor(MapAccessor.INSTANCE);
	tableConfig.getDefaultColumn().setComparator(
		Comparator.comparing(o -> String.valueOf(o), String.CASE_INSENSITIVE_ORDER));

	ColumnConfiguration idCol = tableConfig.declareColumn("id");
	idCol.setColumnLabel("ID");
	idCol.setDefaultColumnWidth("80px");

	ColumnConfiguration nameCol = tableConfig.declareColumn("name");
	nameCol.setColumnLabel("Name");
	nameCol.setDefaultColumnWidth("200px");

	ColumnConfiguration deptCol = tableConfig.declareColumn("department");
	deptCol.setColumnLabel("Department");
	deptCol.setDefaultColumnWidth("150px");

	ColumnConfiguration emailCol = tableConfig.declareColumn("email");
	emailCol.setColumnLabel("Email");
	emailCol.setDefaultColumnWidth("250px");

	ColumnConfiguration statusCol = tableConfig.declareColumn("status");
	statusCol.setColumnLabel("Status");
	statusCol.setDefaultColumnWidth("120px");

	// Row data.
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

	ObjectTableModel model = new ObjectTableModel(columnNames, tableConfig, rows);

	ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
		return new ReactTextCellControl(cellValue);
	};

	ReactTableControl table = new ReactTableControl(model, cellProvider);
	table.setSelectionMode("multi");
	table.setFrozenColumnCount(2);
	return table;
}
```

Remove the now-unused import of `com.top_logic.layout.react.control.table.ColumnDef`.

**Step 2: Commit**

```
Ticket #29109: Update DemoReactTableComponent to use ObjectTableModel.
```

---

## Task 4: Build and Verify

**Purpose:** Build both modules and verify everything compiles.

**Step 1: Build com.top_logic.layout.react**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Expected: BUILD SUCCESS

**Step 2: Build com.top_logic.demo (incremental, NO clean)**

```bash
touch com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java
mvn compile -DskipTests=true -pl com.top_logic.demo
```

**IMPORTANT:** Never use `mvn clean` on `com.top_logic.demo`.

Expected: BUILD SUCCESS

**Step 3: Fix build issues if needed and commit**

```
Ticket #29109: Fix build issues for TableModel integration.
```

---

## Summary

| # | Task | Files | Change |
|---|------|-------|--------|
| 1 | ColumnDef factory | ColumnDef.java | Add `fromColumnConfiguration()` static factory |
| 2 | ReactTableControl refactor | ReactTableControl.java | TableModel constructor, model-based row/value/sort access, listener |
| 3 | Demo update | DemoReactTableComponent.java | ObjectTableModel with TableConfiguration |
| 4 | Build & verify | — | Compile check |
