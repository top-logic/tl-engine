/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;

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

	/**
	 * Listener notified when the set of selected rows changes.
	 */
	public interface SelectionListener {

		/**
		 * Called after the selection has changed.
		 *
		 * @param selectedRows
		 *        The current set of selected row objects (unmodifiable).
		 */
		void selectionChanged(Set<Object> selectedRows);
	}

	// -- State keys --

	/** @see #buildColumnsState() */
	private static final String COLUMNS = "columns";

	/** @see #buildFullState() */
	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	/** @see #updateViewport(int, int) */
	private static final String VIEWPORT_START = "viewportStart";

	/** @see #updateViewport(int, int) */
	private static final String ROWS = "rows";

	/** @see #ReactTableControl(ReactContext, TableModel, ReactCellControlProvider) */
	private static final String ROW_HEIGHT = "rowHeight";

	/** @see #setSelectionMode(String) */
	private static final String SELECTION_MODE = "selectionMode";

	/** @see #setSelectionForced(boolean) */
	private static final String SELECTION_FORCED = "selectionForced";

	/** @see #updateViewport(int, int) */
	private static final String SELECTED_COUNT = "selectedCount";

	/** @see #setFrozenColumnCount(int) */
	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	/** @see #ReactTableControl(ReactContext, TableModel, ReactCellControlProvider) */
	private static final String TREE_MODE = "treeMode";

	// -- Row state keys (used in {@link #updateViewport(int, int)}) --

	/** Synthetic row identifier. */
	private static final String ROW_ID = "id";

	/** Index into the displayed row list. */
	private static final String ROW_INDEX = "index";

	/** Whether the row is selected. */
	private static final String ROW_SELECTED = "selected";

	/** Map of column name to cell {@link ReactControl}. */
	private static final String ROW_CELLS = "cells";

	/** Tree depth of the node (0 for top-level visible items). */
	private static final String TREE_DEPTH = "treeDepth";

	/** Whether the tree node can be expanded. */
	private static final String TREE_EXPANDABLE = "expandable";

	/** Whether the tree node is currently expanded. */
	private static final String TREE_EXPANDED = "expanded";

	// -- Configuration --

	/** Number of rows to prefetch above and below the visible area. */
	private static final int PREFETCH_ROWS = 20;

	/** Minimum column width in pixels. */
	private static final int MIN_WIDTH = 50;

	// -- Fields --

	private final TableModel _tableModel;

	private final ReactCellControlProvider _cellProvider;

	private List<ColumnDef> _columnDefs;

	/** Suppresses model events during command execution to prevent double rebuilds. */
	private boolean _suppressModelEvents;

	private final TableModelListener _modelListener = this::handleModelEvent;

	/** The tree UI model, or {@code null} if the model is not a tree table. */
	private final TreeUIModel<?> _treeModel;

	private int _viewportStart;

	private int _viewportCount;

	private final List<SortKey> _sortKeys = new ArrayList<>();

	private Set<Object> _selectedRows = new HashSet<>();

	private String _selectionMode = "single";

	private boolean _selectionForced;

	/** Index into the displayed row list of the last anchor-setting click, or -1. */
	private int _selectionAnchor = -1;

	/** Whether the last anchor-setting action was an add ({@code true}) or remove ({@code false}). */
	private boolean _anchorAdded = true;

	/** Number of columns frozen on the left side. */
	private int _frozenColumnCount;

	/** Optional listener notified when the row selection changes. */
	private SelectionListener _selectionListener;

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
	 * @param model
	 *        The table model providing rows, columns, and sorting.
	 * @param cellProvider
	 *        Provider for creating cell controls.
	 */
	public ReactTableControl(ReactContext context, TableModel model, ReactCellControlProvider cellProvider) {
		super(context, null, "TLTableView");
		_tableModel = model;
		_cellProvider = cellProvider;
		_viewportStart = 0;
		_viewportCount = 50;

		_columnDefs = buildColumnDefsFromModel();

		_tableModel.addTableModelListener(_modelListener);
		_treeModel = (_tableModel instanceof TreeTableModel)
			? ((TreeTableModel) _tableModel).getTreeModel()
			: null;

		putState(ROW_HEIGHT, Integer.valueOf(36));
		putState(SELECTION_MODE, _selectionMode);
		putState(FROZEN_COLUMN_COUNT, Integer.valueOf(0));
		putState(TREE_MODE, Boolean.valueOf(_treeModel != null));
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
	 * The current number of frozen columns.
	 *
	 * @see #setFrozenColumnCount(int)
	 */
	public int getFrozenColumnCount() {
		return _frozenColumnCount;
	}

	/**
	 * Sets the listener to be notified on selection changes.
	 *
	 * @param listener
	 *        The listener, or {@code null} to remove.
	 */
	public void setSelectionListener(SelectionListener listener) {
		_selectionListener = listener;
	}

	/**
	 * The current set of selected row objects (unmodifiable).
	 */
	public Set<Object> getSelectedRows() {
		return Collections.unmodifiableSet(_selectedRows);
	}

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
	 * Casts a row object to a node type compatible with the tree UI model.
	 */
	@SuppressWarnings("unchecked")
	private static <N> N castNode(Object node) {
		return (N) node;
	}

	/**
	 * Builds {@link ColumnDef} instances from the model's column configuration.
	 */
	private List<ColumnDef> buildColumnDefsFromModel() {
		TableConfiguration tableConfig = _tableModel.getTableConfiguration();
		List<ColumnDef> defs = new ArrayList<>();
		for (String name : _tableModel.getColumnNames()) {
			if (TableControl.SELECT_COLUMN_NAME.equals(name)) {
				continue;
			}
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
				clearCellCache();
				buildFullState();
				break;
			case TableModelEvent.UPDATE:
				clearCellCacheForRows(event.getFirstRow(), event.getLastRow());
				updateViewport(_viewportStart, _viewportCount);
				break;
			default:
				break;
		}
	}

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
		Comparator<Object> cellComparator;
		if (ascending) {
			cellComparator = colConfig.getComparator();
		} else {
			cellComparator = colConfig.getDescendingComparator();
			if (cellComparator == null) {
				// Fall back to reversing the ascending comparator, consistent with TableViewModel.
				Comparator<Object> ascComparator = colConfig.getComparator();
				if (ascComparator != null) {
					cellComparator = ascComparator.reversed();
				}
			}
		}
		if (cellComparator == null) {
			return null;
		}
		Comparator<Object> comparator = cellComparator;
		return (a, b) -> {
			Object va = getCellValue(a, columnName);
			Object vb = getCellValue(b, columnName);
			return comparator.compare(va, vb);
		};
	}

	/**
	 * Extracts the cell value for a row and column from the underlying model.
	 *
	 * <p>
	 * For tree tables, the business object is extracted from the tree node before calling the
	 * model's accessor, because accessors like {@link com.top_logic.layout.MapAccessor} operate on
	 * the data object (e.g. a {@link java.util.Map}), not on the tree node wrapper.
	 * </p>
	 */
	protected Object getCellValue(Object rowObject, String columnName) {
		if (_treeModel != null && rowObject instanceof TLTreeNode) {
			return _tableModel.getValueAt(((TLTreeNode<?>) rowObject).getBusinessObject(), columnName);
		}
		return _tableModel.getValueAt(rowObject, columnName);
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
		putState(TOTAL_ROW_COUNT, Integer.valueOf(getDisplayedRows().size()));
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
						cell.cleanupTree();
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
			rowState.put(ROW_ID, "row_" + i);
			rowState.put(ROW_INDEX, Integer.valueOf(i));
			rowState.put(ROW_SELECTED, Boolean.valueOf(_selectedRows.contains(rowObject)));
			if (_treeModel != null && rowObject instanceof TLTreeNode) {
				TLTreeNode<?> node = (TLTreeNode<?>) rowObject;
				int depth = node.getDepth();
				if (!_treeModel.isRootVisible()) {
					depth--;
				}
				rowState.put(TREE_DEPTH, Integer.valueOf(depth));
				boolean leaf = _treeModel.isLeaf(castNode(rowObject));
				rowState.put(TREE_EXPANDABLE, Boolean.valueOf(!leaf));
				if (!leaf) {
					rowState.put(TREE_EXPANDED, Boolean.valueOf(_treeModel.isExpanded(castNode(rowObject))));
				}
			}
			rowState.put(ROW_CELLS, cells);
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
			ReactControl cellControl = _cellProvider.createCellControl(getReactContext(), rowObject, col.getName(), cellValue);
			registerChildControl(cellControl);
			cells.put(col.getName(), cellControl);
		}
		return cells;
	}

	private void clearCellCache() {
		for (Map<String, ReactControl> cells : _rowCellCache.values()) {
			for (ReactControl cell : cells.values()) {
				cell.cleanupTree();
			}
		}
		_rowCellCache.clear();
	}

	private void clearCellCacheForRows(int firstRow, int lastRow) {
		List<?> displayedRows = getDisplayedRows();
		for (int i = firstRow; i <= lastRow && i < displayedRows.size(); i++) {
			Object rowObject = displayedRows.get(i);
			Map<String, ReactControl> cells = _rowCellCache.remove(rowObject);
			if (cells != null) {
				for (ReactControl cell : cells.values()) {
					cell.cleanupTree();
				}
			}
		}
	}

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

	// -- Commands --

	/**
	 * Handles scroll requests from the client.
	 */
	@ReactCommand("scroll")
	void handleScroll(Map<String, Object> arguments) {
		int start = ((Number) arguments.get("start")).intValue();
		int count = ((Number) arguments.get("count")).intValue();
		updateViewport(start, count);
	}

	/**
	 * Handles sort requests from the client.
	 */
	@ReactCommand("sort")
	void handleSort(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		String direction = (String) arguments.get("direction");
		boolean ascending = !"desc".equals(direction);
		String mode = (String) arguments.get("mode");

		if ("add".equals(mode)) {
			// Shift+click: add to chain or toggle existing.
			boolean found = false;
			for (SortKey key : _sortKeys) {
				if (key._columnName.equals(column)) {
					key._ascending = ascending;
					found = true;
					break;
				}
			}
			if (!found) {
				_sortKeys.add(new SortKey(column, ascending));
			}
		} else {
			// Plain click: replace entire sort with single column.
			_sortKeys.clear();
			_sortKeys.add(new SortKey(column, ascending));
		}

		// Update column defs with sort metadata.
		applySortKeysToColumnDefs();

		// Delegate sorting to the model.
		_suppressModelEvents = true;
		try {
			Comparator<Object> comparator = createChainedComparator();
			if (comparator != null) {
				_tableModel.setOrder(comparator);
			}
		} finally {
			_suppressModelEvents = false;
		}

		// Rebuild viewport with new sort order.
		buildFullState();
	}

	/**
	 * Handles selection requests from the client.
	 */
	@ReactCommand("select")
	void handleSelect(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		List<?> displayedRows = getDisplayedRows();

		if (rowIndex < 0 || rowIndex >= displayedRows.size()) {
			return;
		}

		boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
		boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

		Object rowObject = displayedRows.get(rowIndex);

		// Save previous selection for rollback on veto.
		Set<Object> previousSelection = new LinkedHashSet<>(_selectedRows);

		if ("multi".equals(_selectionMode)) {
			if (shiftKey && _selectionAnchor >= 0) {
				// Additive/subtractive range based on what the anchor action did.
				int from = Math.min(_selectionAnchor, rowIndex);
				int to = Math.max(_selectionAnchor, rowIndex);
				for (int i = from; i <= to; i++) {
					Object row = displayedRows.get(i);
					if (_anchorAdded) {
						_selectedRows.add(row);
					} else {
						if (!_selectionForced || _selectedRows.size() > 1) {
							_selectedRows.remove(row);
						}
					}
				}
			} else if (ctrlKey) {
				// Toggle individual row.
				if (_selectedRows.contains(rowObject)) {
					if (!_selectionForced || _selectedRows.size() > 1) {
						_selectedRows.remove(rowObject);
						_anchorAdded = false;
					}
				} else {
					_selectedRows.add(rowObject);
					_anchorAdded = true;
				}
				_selectionAnchor = rowIndex;
			} else {
				// Plain click: replace selection, set anchor.
				_selectedRows.clear();
				_selectedRows.add(rowObject);
				_selectionAnchor = rowIndex;
				_anchorAdded = true;
			}
		} else {
			// Single mode.
			if (ctrlKey && _selectedRows.contains(rowObject) && !_selectionForced) {
				_selectedRows.clear();
			} else {
				_selectedRows.clear();
				_selectedRows.add(rowObject);
			}
		}

		// Notify listener before updating the viewport. If a ChannelVetoException is thrown
		// (e.g. because a dependent form has unsaved changes), roll back the selection and
		// push the old state to the client so the UI stays consistent.
		if (_selectionListener != null) {
			try {
				_selectionListener.selectionChanged(Collections.unmodifiableSet(_selectedRows));
			} catch (RuntimeException ex) {
				_selectedRows.clear();
				_selectedRows.addAll(previousSelection);
				updateViewport(_viewportStart, _viewportCount);
				throw ex;
			}
		}
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Handles select-all / deselect-all from the header checkbox.
	 */
	@ReactCommand("selectAll")
	void handleSelectAll(Map<String, Object> arguments) {
		boolean selected = Boolean.TRUE.equals(arguments.get("selected"));

		List<?> displayedRows = getDisplayedRows();

		if (selected) {
			_selectedRows.addAll(displayedRows);
		} else {
			if (_selectionForced && !displayedRows.isEmpty()) {
				_selectedRows.clear();
				_selectedRows.add(displayedRows.get(0));
			} else {
				_selectedRows.clear();
			}
		}

		updateViewport(_viewportStart, _viewportCount);
		if (_selectionListener != null) {
			_selectionListener.selectionChanged(Collections.unmodifiableSet(_selectedRows));
		}
	}

	/**
	 * Handles column resize from the client.
	 */
	@ReactCommand("columnResize")
	void handleColumnResize(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		int width = Math.max(MIN_WIDTH, ((Number) arguments.get("width")).intValue());

		for (ColumnDef col : _columnDefs) {
			if (col.getName().equals(column)) {
				col.setWidth(width);
				break;
			}
		}

		// Push updated column definitions to client.
		putState(COLUMNS, buildColumnsState());
	}

	/**
	 * Handles column reorder from the client.
	 */
	@ReactCommand("columnReorder")
	void handleColumnReorder(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		int targetIndex = ((Number) arguments.get("targetIndex")).intValue();

		// Find and remove the column.
		ColumnDef moved = null;
		for (int i = 0; i < _columnDefs.size(); i++) {
			if (_columnDefs.get(i).getName().equals(column)) {
				moved = _columnDefs.remove(i);
				break;
			}
		}

		if (moved == null) {
			return;
		}

		// Clamp target index and insert.
		int insertAt = Math.max(0, Math.min(targetIndex, _columnDefs.size()));
		_columnDefs.add(insertAt, moved);

		// Rebuild columns and viewport (cell order in rows changes).
		buildFullState();
	}

	/**
	 * Handles expand/collapse requests from the client.
	 */
	@ReactCommand("expand")
	void handleExpand(Map<String, Object> arguments) {
		if (_treeModel == null) {
			return;
		}

		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		boolean expanded = Boolean.TRUE.equals(arguments.get("expanded"));

		List<?> displayedRows = getDisplayedRows();
		if (rowIndex < 0 || rowIndex >= displayedRows.size()) {
			return;
		}

		Object rowObject = displayedRows.get(rowIndex);
		_suppressModelEvents = true;
		try {
			_treeModel.setExpanded(castNode(rowObject), expanded);
		} finally {
			_suppressModelEvents = false;
		}

		buildFullState();
	}

	/**
	 * Handles freeze column count changes from the client.
	 */
	@ReactCommand("setFrozenColumnCount")
	void handleSetFrozenColumnCount(Map<String, Object> arguments) {
		int count = ((Number) arguments.get("count")).intValue();
		count = Math.max(0, Math.min(count, _columnDefs.size()));
		setFrozenColumnCount(count);
	}
}
