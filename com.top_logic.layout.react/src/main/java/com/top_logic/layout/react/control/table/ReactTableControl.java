/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

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
