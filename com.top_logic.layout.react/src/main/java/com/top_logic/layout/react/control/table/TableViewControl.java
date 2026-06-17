/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.table.CellContent;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterState;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.util.Resources;

/**
 * Server-side React control rendering a green-field {@link TableView} with virtual
 * scrolling, driving the existing {@code TLTableView} client component.
 *
 * <p>
 * This is the React tier's binding to the green-field table model: it reads column
 * descriptors, the row count and row windows from a {@link TableView}, renders each cell
 * via {@link CellContentReactAdapter}, and maps the client commands (scroll, sort, select,
 * resize, reorder, expand, freeze) back onto {@link TableView} commands. It depends only on
 * the green-field model, not on the legacy {@code TableModel}.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public class TableViewControl<R> extends ReactControl implements TooltipProvider {

	/**
	 * Notified when the set of selected row keys changes.
	 */
	public interface SelectionListener {

		/**
		 * Called after the selection changed, with the current selected {@link Row#key()
		 * keys}.
		 */
		void selectionChanged(Set<Object> selectedKeys);
	}

	private static final String COLUMNS = "columns";

	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	private static final String VIEWPORT_START = "viewportStart";

	private static final String ROWS = "rows";

	private static final String ROW_HEIGHT = "rowHeight";

	private static final String SELECTION_MODE = "selectionMode";

	private static final String SELECTED_COUNT = "selectedCount";

	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	private static final String TREE_MODE = "treeMode";

	private static final String ROW_ID = "id";

	private static final String ROW_INDEX = "index";

	private static final String ROW_SELECTED = "selected";

	private static final String ROW_CELLS = "cells";

	private static final String TREE_DEPTH = "treeDepth";

	private static final String TREE_EXPANDABLE = "expandable";

	private static final String TREE_EXPANDED = "expanded";

	private static final String FILTER_POPUP = "filterPopup";

	private static final int PREFETCH_ROWS = 20;

	private static final int MIN_WIDTH = 50;

	private final TableView<R> _view;

	private final boolean _treeMode;

	private int _viewportStart;

	private int _viewportCount = 50;

	private final String _selectionMode;

	private final Set<Object> _selectedKeys = new LinkedHashSet<>();

	private int _selectionAnchor = -1;

	private SelectionListener _selectionListener;

	/** Cell controls for currently buffered rows, keyed by row key then column name. */
	private final Map<Object, Map<String, ReactControl>> _cellCache = new LinkedHashMap<>();

	/** The column whose filter popup is currently open, or {@code null}. */
	private String _filterColumn;

	/** The editor backing the open filter popup, or {@code null}. */
	private FilterEditor _filterEditor;

	/** Child controls of the open filter popup. */
	private final List<ReactControl> _filterControls = new ArrayList<>();

	/**
	 * Creates a {@link TableViewControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param view
	 *        The green-field table view to render.
	 * @param treeMode
	 *        Whether the client should render tree/group affordances (indent + expand
	 *        toggles).
	 */
	public TableViewControl(ReactContext context, TableView<R> view, boolean treeMode) {
		super(context, null, "TLTableView");
		_view = view;
		_treeMode = treeMode;
		_selectionMode = view.state().getSelection().mode() == SelectionMode.MULTI ? "multi" : "single";

		putState(ROW_HEIGHT, Integer.valueOf(36));
		putState(SELECTION_MODE, _selectionMode);
		putState(TREE_MODE, Boolean.valueOf(_treeMode));
		buildFullState();
	}

	/**
	 * Sets the listener notified on selection changes, or {@code null} to remove it.
	 */
	public void setSelectionListener(SelectionListener listener) {
		_selectionListener = listener;
	}

	// -- State building --

	private void buildFullState() {
		refreshColumns();
		putState(TOTAL_ROW_COUNT, Integer.valueOf(_view.rowCount()));
		updateViewport(_viewportStart, _viewportCount);
	}

	private void refreshColumns() {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> columns = new ArrayList<>();
		for (ColumnView column : _view.columns()) {
			ColumnDef def = new ColumnDef(column.name(), label(resources, column.label()));
			def.setWidth(column.width());
			def.setSortable(column.sortable());
			if (column.sortDirection() == SortDirection.ASC) {
				def.setSortDirection("asc");
			} else if (column.sortDirection() == SortDirection.DESC) {
				def.setSortDirection("desc");
			}
			def.setSortPriority(column.sortPriority());
			Map<String, Object> columnState = def.toStateMap();
			columnState.put("filterable", Boolean.valueOf(column.filterable()));
			columnState.put("filterActive", Boolean.valueOf(isFilterActive(column.name())));
			columns.add(columnState);
		}
		putState(COLUMNS, columns);
		putState(FROZEN_COLUMN_COUNT, Integer.valueOf(_view.frozenColumnCount()));
	}

	private boolean isFilterActive(String column) {
		FilterState state = _view.state().getFilters().get(column);
		return state != null && !state.isEmpty();
	}

	private static String label(Resources resources, ResKey key) {
		return key == null ? "" : resources.getString(key);
	}

	private void updateViewport(int start, int count) {
		int total = _view.rowCount();
		int bufferedStart = Math.max(0, start - PREFETCH_ROWS);
		int bufferedEnd = Math.min(total, start + count + PREFETCH_ROWS);

		List<Row<R>> rows = bufferedStart < bufferedEnd ? _view.rows(bufferedStart, bufferedEnd) : List.of();

		Set<Object> bufferedKeys = new HashSet<>();
		for (Row<R> row : rows) {
			bufferedKeys.add(row.key());
		}
		// Drop cell controls for rows that left the buffer.
		for (Object cached : new ArrayList<>(_cellCache.keySet())) {
			if (!bufferedKeys.contains(cached)) {
				Map<String, ReactControl> cells = _cellCache.remove(cached);
				if (cells != null) {
					cells.values().forEach(ReactControl::cleanupTree);
				}
			}
		}

		List<Map<String, Object>> rowStates = new ArrayList<>();
		int index = bufferedStart;
		for (Row<R> row : rows) {
			Map<String, ReactControl> cells = _cellCache.computeIfAbsent(row.key(), key -> createCells(row));

			Map<String, Object> rowState = new LinkedHashMap<>();
			rowState.put(ROW_ID, "row_" + index);
			rowState.put(ROW_INDEX, Integer.valueOf(index));
			rowState.put(ROW_SELECTED, Boolean.valueOf(_selectedKeys.contains(row.key())));
			if (_treeMode) {
				rowState.put(TREE_DEPTH, Integer.valueOf(row.depth()));
				rowState.put(TREE_EXPANDABLE, Boolean.valueOf(row.expandable()));
				if (row.expandable()) {
					rowState.put(TREE_EXPANDED, Boolean.valueOf(row.expanded()));
				}
			}
			rowState.put(ROW_CELLS, cells);
			rowStates.add(rowState);
			index++;
		}

		_viewportStart = start;
		_viewportCount = count;

		putState(VIEWPORT_START, Integer.valueOf(bufferedStart));
		putState(ROWS, rowStates);
		putState(SELECTED_COUNT, Integer.valueOf(_selectedKeys.size()));
	}

	private Map<String, ReactControl> createCells(Row<R> row) {
		Map<String, ReactControl> cells = new LinkedHashMap<>();
		for (ColumnView column : _view.columns()) {
			ReactControl cell = CellContentReactAdapter.toControl(getReactContext(), _view.cell(row, column.name()));
			registerChildControl(cell);
			cells.put(column.name(), cell);
		}
		return cells;
	}

	@Override
	protected void cleanupChildren() {
		for (Map<String, ReactControl> cells : _cellCache.values()) {
			cells.values().forEach(ReactControl::cleanupTree);
		}
		_cellCache.clear();
		cleanupFilterControls();
	}

	// -- Filter popup --

	/**
	 * Opens the filter popup for a column: builds the column's {@link FilterEditor}, hosts
	 * its fields as child form controls, and pushes the popup state.
	 */
	@ReactCommand("openFilter")
	void handleOpenFilter(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		ColumnFilter<?> filter = _view.columnFilter(column);
		if (filter == null) {
			return;
		}
		cleanupFilterControls();
		_filterColumn = column;
		_filterEditor = FilterEditors.create(filter, _view.state().getFilters().get(column),
			_view.columnMatchCounts(column));

		Resources resources = Resources.getInstance();
		List<Map<String, Object>> fieldStates = new ArrayList<>();
		for (FilterField field : _filterEditor.fields()) {
			ReactControl control = createFieldControl(field);
			registerChildControl(control);
			_filterControls.add(control);
			Map<String, Object> fieldState = new LinkedHashMap<>();
			fieldState.put("label", label(resources, field.label()));
			fieldState.put("control", control);
			fieldStates.add(fieldState);
		}
		Map<String, Object> popup = new LinkedHashMap<>();
		popup.put("column", column);
		popup.put("fields", fieldStates);
		putState(FILTER_POPUP, popup);
	}

	/**
	 * Applies the current filter popup's editor to the column.
	 */
	@ReactCommand("applyFilter")
	void handleApplyFilter(Map<String, Object> arguments) {
		if (_filterEditor == null) {
			return;
		}
		_view.filter(_filterColumn, _filterEditor.read());
		closeFilter();
		rebuildAfterRowChange();
	}

	/**
	 * Clears the filter of a column.
	 */
	@ReactCommand("clearFilter")
	void handleClearFilter(Map<String, Object> arguments) {
		String column = arguments.get("column") != null ? (String) arguments.get("column") : _filterColumn;
		if (column != null) {
			_view.filter(column, null);
		}
		closeFilter();
		rebuildAfterRowChange();
	}

	/**
	 * Closes the filter popup without applying.
	 */
	@ReactCommand("closeFilter")
	void handleCloseFilter(Map<String, Object> arguments) {
		closeFilter();
	}

	private ReactControl createFieldControl(FilterField field) {
		ReactContext context = getReactContext();
		switch (field.kind()) {
			case CHECKBOX:
				return new ReactCheckboxControl(context, field.model());
			case NUMBER:
				return new ReactNumberInputControl(context, field.model(), 0);
			case SELECT:
				return new ReactSelectFormFieldControl(context, (SelectFieldModel) field.model(),
					optionLabels(field.optionLabels()));
			case TEXT:
			default:
				return new ReactTextInputControl(context, field.model());
		}
	}

	private static LabelProvider optionLabels(LabelProvider provider) {
		return provider != null ? provider : String::valueOf;
	}

	private void closeFilter() {
		cleanupFilterControls();
		_filterColumn = null;
		_filterEditor = null;
		putState(FILTER_POPUP, null);
	}

	private void cleanupFilterControls() {
		_filterControls.forEach(ReactControl::cleanupTree);
		_filterControls.clear();
	}

	private void rebuildAfterRowChange() {
		clearCells();
		buildFullState();
	}

	private void clearCells() {
		for (Map<String, ReactControl> cells : _cellCache.values()) {
			cells.values().forEach(ReactControl::cleanupTree);
		}
		_cellCache.clear();
	}

	// -- Commands --

	/**
	 * Handles a viewport scroll request.
	 */
	@ReactCommand("scroll")
	void handleScroll(Map<String, Object> arguments) {
		int start = ((Number) arguments.get("start")).intValue();
		int count = ((Number) arguments.get("count")).intValue();
		updateViewport(start, count);
	}

	/**
	 * Handles a sort request (single click replaces, shift-click adds/toggles).
	 */
	@ReactCommand("sort")
	void handleSort(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		boolean ascending = !"desc".equals(arguments.get("direction"));
		boolean add = "add".equals(arguments.get("mode"));

		List<SortColumn> sort = new ArrayList<>(add ? _view.state().getSort() : List.of());
		boolean found = false;
		for (int n = 0; n < sort.size(); n++) {
			if (sort.get(n).column().equals(column)) {
				sort.set(n, new SortColumn(column, ascending));
				found = true;
				break;
			}
		}
		if (!found) {
			sort.add(new SortColumn(column, ascending));
		}
		_view.sort(new SortSpec(sort));
		rebuildAfterRowChange();
	}

	/**
	 * Handles a row selection (single / ctrl-toggle / shift-range).
	 */
	@ReactCommand("select")
	void handleSelect(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		int total = _view.rowCount();
		if (rowIndex < 0 || rowIndex >= total) {
			return;
		}
		boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
		boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));
		Object key = keyAt(rowIndex);

		if ("multi".equals(_selectionMode)) {
			if (shiftKey && _selectionAnchor >= 0) {
				int from = Math.min(_selectionAnchor, rowIndex);
				int to = Math.max(_selectionAnchor, rowIndex);
				for (Row<R> row : _view.rows(from, to + 1)) {
					_selectedKeys.add(row.key());
				}
			} else if (ctrlKey) {
				if (!_selectedKeys.remove(key)) {
					_selectedKeys.add(key);
				}
				_selectionAnchor = rowIndex;
			} else {
				_selectedKeys.clear();
				_selectedKeys.add(key);
				_selectionAnchor = rowIndex;
			}
		} else {
			boolean wasSelected = _selectedKeys.contains(key);
			_selectedKeys.clear();
			if (!(ctrlKey && wasSelected)) {
				_selectedKeys.add(key);
			}
		}

		pushSelection();
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Handles the header select-all / deselect-all checkbox.
	 */
	@ReactCommand("selectAll")
	void handleSelectAll(Map<String, Object> arguments) {
		boolean selected = Boolean.TRUE.equals(arguments.get("selected"));
		_selectedKeys.clear();
		if (selected) {
			for (Row<R> row : _view.rows(0, _view.rowCount())) {
				if (row.kind() == RowKind.DATA) {
					_selectedKeys.add(row.key());
				}
			}
		}
		pushSelection();
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Handles a column resize.
	 */
	@ReactCommand("columnResize")
	void handleColumnResize(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		int width = Math.max(MIN_WIDTH, ((Number) arguments.get("width")).intValue());
		_view.resizeColumn(column, width);
		refreshColumns();
	}

	/**
	 * Handles a column reorder.
	 */
	@ReactCommand("columnReorder")
	void handleColumnReorder(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		int targetIndex = ((Number) arguments.get("targetIndex")).intValue();
		_view.moveColumn(column, targetIndex);
		rebuildAfterRowChange();
	}

	/**
	 * Handles a tree/group expand or collapse.
	 */
	@ReactCommand("expand")
	void handleExpand(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		boolean expanded = Boolean.TRUE.equals(arguments.get("expanded"));
		if (rowIndex < 0 || rowIndex >= _view.rowCount()) {
			return;
		}
		_view.setExpanded(keyAt(rowIndex), expanded);
		rebuildAfterRowChange();
	}

	/**
	 * Handles a change of the frozen column count.
	 */
	@ReactCommand("setFrozenColumnCount")
	void handleSetFrozenColumnCount(Map<String, Object> arguments) {
		int count = ((Number) arguments.get("count")).intValue();
		_view.setFrozenColumnCount(Math.max(0, count));
		refreshColumns();
	}

	private Object keyAt(int rowIndex) {
		List<Row<R>> single = _view.rows(rowIndex, rowIndex + 1);
		return single.isEmpty() ? null : single.get(0).key();
	}

	@Override
	public TooltipContent getTooltipContent(String key) {
		if (key == null || !key.startsWith("row_")) {
			return null;
		}
		int separator = key.indexOf('|');
		if (separator < 0) {
			return null;
		}
		int rowIndex;
		try {
			rowIndex = Integer.parseInt(key.substring(4, separator));
		} catch (NumberFormatException ex) {
			return null;
		}
		if (rowIndex < 0 || rowIndex >= _view.rowCount()) {
			return null;
		}
		List<Row<R>> single = _view.rows(rowIndex, rowIndex + 1);
		if (single.isEmpty()) {
			return null;
		}
		CellContent content = _view.cell(single.get(0), key.substring(separator + 1));
		if (content instanceof CellContent.Labeled labeled
				&& labeled.tooltip() != null && !labeled.tooltip().isEmpty()) {
			return new TooltipContent(labeled.tooltip(), null);
		}
		return null;
	}

	private void pushSelection() {
		_view.select(new Selection(
			"multi".equals(_selectionMode) ? SelectionMode.MULTI : SelectionMode.SINGLE,
			new LinkedHashSet<>(_selectedKeys)));
		if (_selectionListener != null) {
			_selectionListener.selectionChanged(new LinkedHashSet<>(_selectedKeys));
		}
	}

}
