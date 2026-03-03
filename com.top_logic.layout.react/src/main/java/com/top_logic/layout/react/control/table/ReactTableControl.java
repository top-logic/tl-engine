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

	private static final String SELECTION_FORCED = "selectionForced";

	private static final String SELECTED_COUNT = "selectedCount";

	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	// -- Configuration --

	/** Number of rows to prefetch above and below the visible area. */
	private static final int PREFETCH_ROWS = 20;

	// -- Commands --

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand(),
		new ColumnResizeCommand(),
		new ColumnReorderCommand());

	// -- Fields --

	private final List<ColumnDef> _columnDefs;

	private final ReactCellControlProvider _cellProvider;

	private List<Object> _allRows;

	private List<Object> _displayedRows;

	private int _viewportStart;

	private int _viewportCount;

	private final List<SortKey> _sortKeys = new ArrayList<>();

	private Set<Object> _selectedRows = new HashSet<>();

	private String _selectionMode = "single";

	private boolean _selectionForced;

	/** Index into {@code _displayedRows} of the last anchor-setting click, or -1. */
	private int _selectionAnchor = -1;

	/** Whether the last anchor-setting action was an add ({@code true}) or remove ({@code false}). */
	private boolean _anchorAdded = true;

	/** Number of columns frozen on the left side. */
	private int _frozenColumnCount;

	/**
	 * Cache of cell controls for currently visible rows. Keyed by row object, then column name.
	 */
	private final Map<Object, Map<String, ReactControl>> _rowCellCache = new LinkedHashMap<>();

	/**
	 * A single entry in the multi-column sort chain.
	 */
	static class SortKey {

		final String _columnName;

		boolean _ascending;

		SortKey(String columnName, boolean ascending) {
			_columnName = columnName;
			_ascending = ascending;
		}
	}

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
		putState(FROZEN_COLUMN_COUNT, Integer.valueOf(0));
		buildFullState();
	}

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

	/**
	 * Sets whether the selection is forced (at least one row must remain selected).
	 */
	public void setSelectionForced(boolean forced) {
		_selectionForced = forced;
		putState(SELECTION_FORCED, Boolean.valueOf(forced));
	}

	/**
	 * Sets the number of columns frozen on the left side.
	 *
	 * <p>
	 * The first {@code count} columns in the column list remain visible during horizontal
	 * scrolling. In multi-select mode, the checkbox column is automatically frozen when
	 * {@code count > 0}.
	 * </p>
	 *
	 * @param count
	 *        The number of frozen columns, or 0 for no frozen columns.
	 */
	public void setFrozenColumnCount(int count) {
		_frozenColumnCount = count;
		putState(FROZEN_COLUMN_COUNT, Integer.valueOf(count));
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

	/**
	 * Updates the sort direction and priority on all column definitions from the current sort key
	 * list.
	 */
	private void applySortKeysToColumnDefs() {
		// Build a lookup from column name to sort key index.
		Map<String, Integer> sortIndex = new LinkedHashMap<>();
		for (int i = 0; i < _sortKeys.size(); i++) {
			sortIndex.put(_sortKeys.get(i)._columnName, Integer.valueOf(i));
		}

		for (ColumnDef col : _columnDefs) {
			Integer idx = sortIndex.get(col.getName());
			if (idx != null) {
				SortKey key = _sortKeys.get(idx.intValue());
				col.setSortDirection(key._ascending ? "asc" : "desc");
				col.setSortPriority(idx.intValue() + 1);
			} else {
				col.setSortDirection(null);
				col.setSortPriority(0);
			}
		}
	}

	/**
	 * Creates a chained comparator from the current sort key list.
	 *
	 * @return A comparator, or {@code null} if no sort keys are set.
	 */
	private Comparator<Object> createChainedComparator() {
		Comparator<Object> result = null;
		for (SortKey key : _sortKeys) {
			Comparator<Object> comp = createSortComparator(key._columnName, key._ascending);
			if (comp != null) {
				result = result == null ? comp : result.thenComparing(comp);
			}
		}
		return result;
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
		putState(SELECTED_COUNT, Integer.valueOf(_selectedRows.size()));
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

			// Sort the displayed rows.
			Comparator<Object> comparator = table.createChainedComparator();
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

			boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
			boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

			Object rowObject = table._displayedRows.get(rowIndex);

			if ("multi".equals(table._selectionMode)) {
				if (shiftKey && table._selectionAnchor >= 0) {
					// Additive/subtractive range based on what the anchor action did.
					int from = Math.min(table._selectionAnchor, rowIndex);
					int to = Math.max(table._selectionAnchor, rowIndex);
					for (int i = from; i <= to; i++) {
						Object row = table._displayedRows.get(i);
						if (table._anchorAdded) {
							table._selectedRows.add(row);
						} else {
							if (!table._selectionForced || table._selectedRows.size() > 1) {
								table._selectedRows.remove(row);
							}
						}
					}
				} else if (ctrlKey) {
					// Toggle individual row.
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
					// Plain click: replace selection, set anchor.
					table._selectedRows.clear();
					table._selectedRows.add(rowObject);
					table._selectionAnchor = rowIndex;
					table._anchorAdded = true;
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

	/**
	 * Handles column reorder from the client.
	 */
	static class ColumnReorderCommand extends ControlCommand {

		ColumnReorderCommand() {
			super("columnReorder");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.columnReorder");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			String column = (String) arguments.get("column");
			int targetIndex = ((Number) arguments.get("targetIndex")).intValue();

			// Find and remove the column.
			ColumnDef moved = null;
			for (int i = 0; i < table._columnDefs.size(); i++) {
				if (table._columnDefs.get(i).getName().equals(column)) {
					moved = table._columnDefs.remove(i);
					break;
				}
			}

			if (moved == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			// Clamp target index and insert.
			int insertAt = Math.max(0, Math.min(targetIndex, table._columnDefs.size()));
			table._columnDefs.add(insertAt, moved);

			// Rebuild columns and viewport (cell order in rows changes).
			table.buildFullState();
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
