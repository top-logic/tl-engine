/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.Group;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.RowSourceListener;
import com.top_logic.table.Selection;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;

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

	private final ViewStateStore _store;

	private final TableId _id;

	/** @see #DefaultTableView(List, RowSource, TableViewState, ViewStateStore, TableId, Collection) */
	private final Set<String> _hiddenByDefault;

	/**
	 * Creates a {@link DefaultTableView} without personalization persistence.
	 *
	 * @param columns
	 *        All column definitions (visibility/order is taken from {@code state}).
	 * @param source
	 *        The row source.
	 * @param state
	 *        The (mutable) view state.
	 */
	public DefaultTableView(List<Column<R, ?>> columns, RowSource<R> source, TableViewState state) {
		this(columns, source, state, null, null);
	}

	/**
	 * Creates a {@link DefaultTableView}, optionally restoring and persisting personalization.
	 *
	 * @param columns
	 *        All column definitions (visibility/order is taken from {@code state}).
	 * @param source
	 *        The row source.
	 * @param state
	 *        The (mutable) view state.
	 * @param store
	 *        Where personalization (column order/widths/frozen/sort/grouping) is persisted, or
	 *        {@code null} to disable persistence.
	 * @param id
	 *        The stable identity under which the personalization is stored (required when
	 *        {@code store} is given).
	 */
	public DefaultTableView(List<Column<R, ?>> columns, RowSource<R> source, TableViewState state,
			ViewStateStore store, TableId id) {
		this(columns, source, state, store, id, Set.of());
	}

	/**
	 * Creates a {@link DefaultTableView} that offers columns the user has to switch on first.
	 *
	 * @param hiddenByDefault
	 *        The columns not displayed until the user selects them - offered by the column
	 *        selection, and kept hidden when a personalization that predates them is restored.
	 * @see #DefaultTableView(List, RowSource, TableViewState, ViewStateStore, TableId)
	 */
	public DefaultTableView(List<Column<R, ?>> columns, RowSource<R> source, TableViewState state,
			ViewStateStore store, TableId id, Collection<String> hiddenByDefault) {
		for (Column<R, ?> column : columns) {
			_columns.put(column.name(), column);
		}
		_source = source;
		_state = state;
		_store = store;
		_id = id;
		_hiddenByDefault = new LinkedHashSet<>(hiddenByDefault);
		_hiddenByDefault.retainAll(_columns.keySet());
		_source.addListener(_sourceListener);
		if (_store != null && _id != null) {
			restore();
		}
		// Whatever the order ends up being - the initial default or the user's persisted choice -
		// the row source has to be told about it.
		if (!_state.getSort().isEmpty()) {
			_source.withOrder(new SortSpec(_state.getSort()));
		}
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
		return create(columns, source, null, null);
	}

	/**
	 * Creates a {@link DefaultTableView} with an initial state showing all columns in
	 * declaration order, restoring and persisting personalization through the given store.
	 *
	 * @param columns
	 *        All column definitions, in initial display order.
	 * @param source
	 *        The row source.
	 * @param store
	 *        Where personalization is persisted, or {@code null} to disable persistence.
	 * @param id
	 *        The stable identity under which the personalization is stored.
	 */
	public static <R> DefaultTableView<R> create(List<Column<R, ?>> columns, RowSource<R> source,
			ViewStateStore store, TableId id) {
		return create(columns, source, store, id, SortSpec.NONE);
	}

	/**
	 * Creates a {@link DefaultTableView} with an initial state showing all columns in declaration
	 * order and sorted by the given order, restoring and persisting personalization through the
	 * given store.
	 *
	 * @param columns
	 *        All column definitions, in initial display order.
	 * @param source
	 *        The row source.
	 * @param store
	 *        Where personalization is persisted, or {@code null} to disable persistence.
	 * @param id
	 *        The stable identity under which the personalization is stored.
	 * @param defaultSort
	 *        The order to display until the user sorts the table themselves,
	 *        {@link SortSpec#NONE} for none.
	 */
	public static <R> DefaultTableView<R> create(List<Column<R, ?>> columns, RowSource<R> source,
			ViewStateStore store, TableId id, SortSpec defaultSort) {
		return create(columns, source, store, id, defaultSort, Set.of());
	}

	/**
	 * Creates a {@link DefaultTableView} whose initial state displays all columns but the ones
	 * hidden by default, in declaration order, sorted by the given order.
	 *
	 * @param hiddenByDefault
	 *        The columns offered but not displayed until the user selects them - e.g. the further
	 *        attributes of the row type, next to the columns a table configures explicitly.
	 * @see #create(List, RowSource, ViewStateStore, TableId, SortSpec)
	 */
	public static <R> DefaultTableView<R> create(List<Column<R, ?>> columns, RowSource<R> source,
			ViewStateStore store, TableId id, SortSpec defaultSort, Collection<String> hiddenByDefault) {
		return new DefaultTableView<>(columns, source, initialState(columns, defaultSort, hiddenByDefault),
			store, id, hiddenByDefault);
	}

	/**
	 * The view state a table starts from: all columns but the ones hidden by default, in declaration
	 * order, with their default widths, sorted by the given order.
	 *
	 * <p>
	 * A caller that wants more of the initial state than the {@code create} methods offer - a number
	 * of frozen columns, for instance - fills it in here and passes the result to
	 * {@link #DefaultTableView(List, RowSource, TableViewState, ViewStateStore, TableId, Collection)}.
	 * A persisted personalization still wins over it, exactly as it does over the default sort.
	 * </p>
	 */
	public static <R> TableViewState initialState(List<Column<R, ?>> columns, SortSpec defaultSort,
			Collection<String> hiddenByDefault) {
		TableViewState state = new TableViewState();
		state.setSort(new ArrayList<>(defaultSort.columns()));
		List<String> order = new ArrayList<>(columns.size());
		Set<String> hidden = new LinkedHashSet<>();
		Map<String, Integer> widths = new LinkedHashMap<>();
		for (Column<R, ?> column : columns) {
			if (hiddenByDefault.contains(column.name())) {
				hidden.add(column.name());
			} else {
				order.add(column.name());
			}
			widths.put(column.name(), column.defaultWidth());
		}
		state.setColumnOrder(order);
		state.setHiddenColumns(hidden);
		state.setWidths(widths);
		return state;
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

	@Override
	public List<ColumnOption> columnOptions() {
		List<ColumnOption> result = new ArrayList<>(_columns.size());
		for (String name : _state.getColumnOrder()) {
			Column<R, ?> column = _columns.get(name);
			if (column != null && column.selectable()) {
				result.add(new ColumnOption(name, column.label(), true));
			}
		}
		for (Map.Entry<String, Column<R, ?>> entry : _columns.entrySet()) {
			if (entry.getValue().selectable() && !_state.getColumnOrder().contains(entry.getKey())) {
				result.add(new ColumnOption(entry.getKey(), entry.getValue().label(), false));
			}
		}
		return result;
	}

	@Override
	public List<String> defaultColumnOrder() {
		List<String> result = new ArrayList<>(_columns.size());
		for (String name : _columns.keySet()) {
			if (!_hiddenByDefault.contains(name)) {
				result.add(name);
			}
		}
		return result;
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
				return definition.renderCell(row.data());
			case GROUP_HEADER:
				// The header doubles as the subtotal row: label in the first column,
				// per-column aggregates in the rest.
				return isFirstColumn(column)
					? CellContent.label(groupLabel(row.group()))
					: aggregate(definition, row.group());
			case AGGREGATE:
				return aggregate(definition, row.group());
			default:
				return CellContent.empty();
		}
	}

	private <V> CellContent aggregate(Column<R, V> column, Group<R> group) {
		return column.aggregate().map(aggregator -> aggregator.over(group)).orElse(CellContent.empty());
	}

	private String groupLabel(Group<R> group) {
		List<Object> values = group.key().values();
		return values.isEmpty() ? "" : String.valueOf(values.get(values.size() - 1));
	}

	@Override
	public ColumnFilter<?> columnFilter(String column) {
		Column<R, ?> definition = _columns.get(column);
		return definition == null ? null : definition.filter().orElse(null);
	}

	@Override
	public MatchCounts columnMatchCounts(String column) {
		return _source.matchCounts(column);
	}

	private boolean isFirstColumn(String column) {
		List<String> order = _state.getColumnOrder();
		return !order.isEmpty() && order.get(0).equals(column);
	}

	// ---- commands ----

	@Override
	public void sort(SortSpec spec) {
		_state.setSort(new ArrayList<>(spec.columns()));
		_source.withOrder(spec);
		persist();
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
		persist();
		fireColumnsChanged();
	}

	@Override
	public void group(GroupSpec spec) {
		_state.setGrouping(spec);
		_source.withGrouping(spec);
		persist();
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
		persist();
		fireColumnsChanged();
	}

	@Override
	public void setColumnOrder(List<String> columns) {
		List<String> order = new ArrayList<>(columns.size());
		for (String column : columns) {
			Column<R, ?> definition = _columns.get(column);
			if (definition != null && definition.selectable() && !order.contains(column)) {
				order.add(column);
			}
		}
		// A column the user cannot decide about (an action column) keeps its place: it is not part
		// of the given arrangement, but dropping it would take its action away for good. Its place is
		// kept relative to the columns around it - a leading action column stays in front of them all,
		// a trailing one behind them all, however many the new arrangement holds.
		for (String name : unselectableColumns()) {
			order.add(insertPosition(name, order.size()), name);
		}
		_state.setColumnOrder(order);
		// The caller decided about every column they can decide about, so everything left out is
		// hidden on purpose - it must not come back as a "new" column on the next restore.
		Set<String> hidden = new LinkedHashSet<>();
		for (Map.Entry<String, Column<R, ?>> entry : _columns.entrySet()) {
			if (entry.getValue().selectable() && !order.contains(entry.getKey())) {
				hidden.add(entry.getKey());
			}
		}
		_state.setHiddenColumns(hidden);
		// A column that was frozen may have been hidden or moved out of the frozen range; the
		// frozen prefix can never reach beyond the columns that are left.
		_state.setFrozenCount(Math.min(_state.getFrozenCount(), order.size()));
		persist();
		fireColumnsChanged();
	}

	/** The currently displayed columns the user cannot decide about, in display order. */
	private List<String> unselectableColumns() {
		List<String> result = new ArrayList<>();
		for (String name : _state.getColumnOrder()) {
			Column<R, ?> definition = _columns.get(name);
			if (definition != null && !definition.selectable()) {
				result.add(name);
			}
		}
		return result;
	}

	/**
	 * Where to re-insert an unselectable column into a rearranged column order.
	 *
	 * @param name
	 *        The unselectable column, still at its old place in the current order.
	 * @param size
	 *        The number of columns the new order holds so far.
	 * @return The index to insert at: in front of everything if the column led the current order,
	 *         behind everything if it trailed it, otherwise after those of its previous predecessors
	 *         that are still displayed.
	 */
	private int insertPosition(String name, int size) {
		List<String> previous = _state.getColumnOrder();
		int index = previous.indexOf(name);
		int predecessors = 0;
		for (int n = 0; n < index; n++) {
			Column<R, ?> definition = _columns.get(previous.get(n));
			if (definition != null && definition.selectable()) {
				predecessors++;
			}
		}
		if (predecessors == 0) {
			return 0;
		}
		for (int n = index + 1; n < previous.size(); n++) {
			Column<R, ?> definition = _columns.get(previous.get(n));
			if (definition != null && definition.selectable()) {
				// Columns follow it, so keep it after its predecessors rather than at the very end.
				return Math.min(predecessors, size);
			}
		}
		return size;
	}

	@Override
	public void resizeColumn(String column, int width) {
		_state.getWidths().put(column, width);
		persist();
		fireColumnsChanged();
	}

	@Override
	public void setColumnVisible(String column, boolean visible) {
		List<String> order = _state.getColumnOrder();
		boolean present = order.contains(column);
		if (visible && !present) {
			order.add(column);
			_state.getHiddenColumns().remove(column);
		} else if (!visible && present) {
			order.remove(column);
			_state.getHiddenColumns().add(column);
		} else {
			return;
		}
		persist();
		fireColumnsChanged();
	}

	@Override
	public void setFrozenColumnCount(int count) {
		_state.setFrozenCount(frozenPrefix(count));
		persist();
		fireColumnsChanged();
	}

	/**
	 * The largest frozen prefix no longer than the requested one that ends behind a column the user
	 * may freeze.
	 *
	 * <p>
	 * A column declining to be {@link Column#frozenEligible() frozen} - an action column holding a
	 * button, say - must not be the one the frozen area ends with. Leading columns of that kind are
	 * carried along: they sit left of everything the user can decide about, so freezing anything at
	 * all includes them.
	 * </p>
	 */
	private int frozenPrefix(int count) {
		List<String> order = _state.getColumnOrder();
		int result = Math.max(0, Math.min(count, order.size()));
		while (result > 0) {
			Column<R, ?> last = _columns.get(order.get(result - 1));
			if (last == null || last.frozenEligible()) {
				return result;
			}
			result--;
		}
		return 0;
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

	/**
	 * Loads persisted personalization and merges it onto the current state: column order, widths
	 * and sort are reconciled against the columns that actually exist (stale columns dropped, new
	 * columns appended), and the persisted sort/grouping are re-applied to the row source.
	 */
	private void restore() {
		TableViewState persisted = _store.load(_id, filterCodec());
		if (persisted == null) {
			return;
		}

		List<String> order = new ArrayList<>();
		for (String name : persisted.getColumnOrder()) {
			if (_columns.containsKey(name) && !order.contains(name)) {
				order.add(name);
			}
		}
		Set<String> hidden = new LinkedHashSet<>();
		for (String name : persisted.getHiddenColumns()) {
			if (_columns.containsKey(name) && !order.contains(name)) {
				hidden.add(name);
			}
		}
		// A column the persisted state knows nothing about is one this table has gained since - it
		// appears, unless it is one of the columns that are offered but not displayed by default,
		// while a column the user hid stays hidden.
		for (String name : _columns.keySet()) {
			if (!order.contains(name) && !hidden.contains(name)) {
				if (_hiddenByDefault.contains(name)) {
					hidden.add(name);
				} else {
					order.add(name);
				}
			}
		}
		if (!order.isEmpty()) {
			_state.setColumnOrder(order);
			_state.setHiddenColumns(hidden);
		}

		for (Map.Entry<String, Integer> entry : persisted.getWidths().entrySet()) {
			if (_columns.containsKey(entry.getKey())) {
				_state.getWidths().put(entry.getKey(), entry.getValue());
			}
		}

		_state.setFrozenCount(Math.min(persisted.getFrozenCount(), _state.getColumnOrder().size()));

		List<SortColumn> sort = new ArrayList<>();
		for (SortColumn sortColumn : persisted.getSort()) {
			Column<R, ?> column = _columns.get(sortColumn.column());
			if (column != null && column.sort().isPresent()) {
				sort.add(sortColumn);
			}
		}
		// An empty persisted sort means the user never sorted this table, so the configured
		// default order stays in effect.
		if (!sort.isEmpty()) {
			_state.setSort(sort);
		}

		List<String> groupColumns = new ArrayList<>();
		for (String name : persisted.getGrouping().columns()) {
			if (_columns.containsKey(name)) {
				groupColumns.add(name);
			}
		}
		if (!groupColumns.isEmpty()) {
			GroupSpec grouping = new GroupSpec(groupColumns);
			_state.setGrouping(grouping);
			_source.withGrouping(grouping);
		}

		for (Map.Entry<String, FilterState> entry : persisted.getFilters().entrySet()) {
			Column<R, ?> column = _columns.get(entry.getKey());
			if (column != null && column.filter().isPresent()) {
				_state.getFilters().put(entry.getKey(), entry.getValue());
			}
		}
		if (!_state.getFilters().isEmpty()) {
			_source.withFilter(new FilterSpec(_state.getFilters()));
		}
	}

	/**
	 * Persists the current personalization, if a store is configured.
	 */
	private void persist() {
		if (_store != null && _id != null) {
			_store.save(_id, _state, filterCodec());
		}
	}

	/**
	 * A {@link FilterCodec} bridging this view's columns: it delegates a column's inner state to its
	 * {@link ColumnFilter#toJson(FilterState) filter} and handles {@link NegatedFilterState
	 * inversion} generically.
	 */
	private FilterCodec filterCodec() {
		return new FilterCodec() {
			@Override
			public Object toJson(String column, FilterState state) {
				ColumnFilter<?> filter = columnFilter(column);
				if (filter == null) {
					return null;
				}
				boolean inverted = state instanceof NegatedFilterState;
				FilterState inner = inverted ? ((NegatedFilterState) state).inner() : state;
				Object innerJson = filter.toJson(inner);
				if (innerJson == null) {
					return null;
				}
				Map<String, Object> result = new LinkedHashMap<>();
				result.put("state", innerJson);
				if (inverted) {
					result.put("inverted", Boolean.TRUE);
				}
				return result;
			}

			@Override
			public FilterState fromJson(String column, Object json) {
				ColumnFilter<?> filter = columnFilter(column);
				if (filter == null || !(json instanceof Map<?, ?> map)) {
					return null;
				}
				FilterState inner = filter.fromJson(map.get("state"));
				if (inner == null) {
					return null;
				}
				return Boolean.TRUE.equals(map.get("inverted")) ? new NegatedFilterState(inner) : inner;
			}
		};
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
