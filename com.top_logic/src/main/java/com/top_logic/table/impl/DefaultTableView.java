/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.RowSource;
import com.top_logic.table.RowSourceListener;
import com.top_logic.table.Selection;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.TableViewState;

/**
 * Default {@link TableView}: composes a column model, a {@link RowSource} and a
 * {@link TableViewState}, turning UI commands into view-state mutations and re-derivations
 * of the row source, and translating row-source changes into {@link TableViewListener}
 * events.
 *
 * @param <R>
 *        The row business object type.
 */
public class DefaultTableView<R> implements TableView<R> {

	private final Map<String, Column<R, ?>> _columns = new LinkedHashMap<>();

	private final RowSource<R> _source;

	private final TableViewState _state;

	private final List<TableViewListener> _listeners = new ArrayList<>();

	private final RowSourceListener _sourceListener = this::onRowsInvalidated;

	/**
	 * Creates a {@link DefaultTableView}.
	 *
	 * @param columns
	 *        All column definitions (visibility/order is taken from {@code state}).
	 * @param source
	 *        The row source.
	 * @param state
	 *        The (mutable) view state.
	 */
	public DefaultTableView(List<Column<R, ?>> columns, RowSource<R> source, TableViewState state) {
		for (Column<R, ?> column : columns) {
			_columns.put(column.name(), column);
		}
		_source = source;
		_state = state;
		_source.addListener(_sourceListener);
	}

	/**
	 * Creates a {@link DefaultTableView} with an initial state showing all columns in
	 * declaration order.
	 *
	 * @param columns
	 *        All column definitions, in initial display order.
	 * @param source
	 *        The row source.
	 */
	public static <R> DefaultTableView<R> create(List<Column<R, ?>> columns, RowSource<R> source) {
		TableViewState state = new TableViewState();
		List<String> order = new ArrayList<>(columns.size());
		Map<String, Integer> widths = new LinkedHashMap<>();
		for (Column<R, ?> column : columns) {
			order.add(column.name());
			widths.put(column.name(), column.defaultWidth());
		}
		state.setColumnOrder(order);
		state.setWidths(widths);
		return new DefaultTableView<>(columns, source, state);
	}

	// ---- structure ----

	@Override
	public List<ColumnView> columns() {
		List<ColumnView> result = new ArrayList<>(_state.getColumnOrder().size());
		int index = 0;
		for (String name : _state.getColumnOrder()) {
			Column<R, ?> column = _columns.get(name);
			if (column == null) {
				continue;
			}
			Integer width = _state.getWidths().get(name);
			boolean frozen = index < _state.getFrozenCount();
			result.add(new ColumnView(
				name,
				column.label(),
				width != null ? width : column.defaultWidth(),
				column.sort().isPresent(),
				column.filter().isPresent(),
				column.editor().isPresent(),
				frozen,
				sortDirection(name),
				sortPriority(name)));
			index++;
		}
		return result;
	}

	private SortDirection sortDirection(String name) {
		for (SortColumn sortColumn : _state.getSort()) {
			if (sortColumn.column().equals(name)) {
				return sortColumn.ascending() ? SortDirection.ASC : SortDirection.DESC;
			}
		}
		return null;
	}

	private int sortPriority(String name) {
		List<SortColumn> sort = _state.getSort();
		for (int n = 0; n < sort.size(); n++) {
			if (sort.get(n).column().equals(name)) {
				return n + 1;
			}
		}
		return 0;
	}

	@Override
	public int frozenColumnCount() {
		return _state.getFrozenCount();
	}

	// ---- data window ----

	@Override
	public int rowCount() {
		return _source.size();
	}

	@Override
	public List<Row<R>> rows(int from, int to) {
		return _source.window(from, to);
	}

	@Override
	public CellContent cell(Row<R> row, String column) {
		Column<R, ?> definition = _columns.get(column);
		if (definition == null) {
			return CellContent.empty();
		}
		switch (row.kind()) {
			case DATA:
				return renderData(definition, row.data());
			case GROUP_HEADER:
				return isFirstColumn(column) ? CellContent.label(String.valueOf(row.group())) : CellContent.empty();
			case AGGREGATE:
				// Aggregate cell rendering requires the materialized Group; wired with grouping.
				return CellContent.empty();
			default:
				return CellContent.empty();
		}
	}

	private <V> CellContent renderData(Column<R, V> column, R data) {
		return column.renderer().render(column.value(data));
	}

	private boolean isFirstColumn(String column) {
		List<String> order = _state.getColumnOrder();
		return !order.isEmpty() && order.get(0).equals(column);
	}

	@Override
	public FieldModel editor(Object rowKey, String column) {
		Column<R, ?> definition = _columns.get(column);
		if (definition == null || definition.editor().isEmpty()) {
			return null;
		}
		R data = dataByKey(rowKey);
		if (data == null) {
			return null;
		}
		return newField(definition, data);
	}

	private <V> FieldModel newField(Column<R, V> column, R data) {
		return column.editor().orElseThrow().newField(data, column.value(data));
	}

	private R dataByKey(Object rowKey) {
		for (Row<R> row : _source.window(0, _source.size())) {
			if (row.kind() == RowKind.DATA && row.key().equals(rowKey)) {
				return row.data();
			}
		}
		return null;
	}

	// ---- commands ----

	@Override
	public void sort(SortSpec spec) {
		_state.setSort(new ArrayList<>(spec.columns()));
		_source.withOrder(spec);
		fireColumnsChanged();
	}

	@Override
	public void filter(String column, FilterState state) {
		if (state == null || state.isEmpty()) {
			_state.getFilters().remove(column);
		} else {
			_state.getFilters().put(column, state);
		}
		_source.withFilter(new FilterSpec(_state.getFilters()));
		fireColumnsChanged();
	}

	@Override
	public void group(GroupSpec spec) {
		_state.setGrouping(spec);
		_source.withGrouping(spec);
		fireColumnsChanged();
	}

	@Override
	public void moveColumn(String column, int toIndex) {
		List<String> order = _state.getColumnOrder();
		int from = order.indexOf(column);
		if (from < 0) {
			return;
		}
		order.remove(from);
		order.add(Math.min(toIndex, order.size()), column);
		fireColumnsChanged();
	}

	@Override
	public void resizeColumn(String column, int width) {
		_state.getWidths().put(column, width);
		fireColumnsChanged();
	}

	@Override
	public void setColumnVisible(String column, boolean visible) {
		List<String> order = _state.getColumnOrder();
		boolean present = order.contains(column);
		if (visible && !present) {
			order.add(column);
		} else if (!visible && present) {
			order.remove(column);
		} else {
			return;
		}
		fireColumnsChanged();
	}

	@Override
	public void setFrozenColumnCount(int count) {
		_state.setFrozenCount(count);
		fireColumnsChanged();
	}

	@Override
	public void setExpanded(Object rowKey, boolean expanded) {
		if (expanded) {
			_state.getExpanded().add(rowKey);
		} else {
			_state.getExpanded().remove(rowKey);
		}
		_source.setExpanded(rowKey, expanded);
	}

	@Override
	public void select(Selection selection) {
		_state.setSelection(selection);
		fireSelectionChanged();
	}

	@Override
	public void commitEdit(Object rowKey, String column, Object value) {
		Column<R, ?> definition = _columns.get(column);
		if (definition == null) {
			return;
		}
		R data = dataByKey(rowKey);
		if (data == null) {
			return;
		}
		commit(definition, data, value);
		fireRowsChanged(0, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	private <V> void commit(Column<R, V> column, R data, Object value) {
		column.editor().orElseThrow().commit(data, (V) value);
	}

	@Override
	public void window(int page, int pageSize) {
		_state.setPage(page);
		_state.setPageSize(pageSize);
		fireRowsChanged(0, Integer.MAX_VALUE);
	}

	// ---- listeners ----

	@Override
	public void addListener(TableViewListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(TableViewListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public TableViewState state() {
		return _state;
	}

	private void onRowsInvalidated(int from, int to) {
		fireRowsChanged(from, to);
	}

	private void fireColumnsChanged() {
		for (TableViewListener listener : List.copyOf(_listeners)) {
			listener.columnsChanged();
		}
	}

	private void fireRowsChanged(int from, int to) {
		for (TableViewListener listener : List.copyOf(_listeners)) {
			listener.rowCountChanged();
			listener.rowsChanged(from, to);
		}
	}

	private void fireSelectionChanged() {
		for (TableViewListener listener : List.copyOf(_listeners)) {
			listener.selectionChanged();
		}
	}

}
