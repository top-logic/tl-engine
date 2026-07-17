/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.CellContentReactAdapter;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;
import com.top_logic.table.Sort;
import com.top_logic.table.TableId;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * The editable variant of the declarative {@code <table>}: a {@link RowSetTableControl} whose data
 * columns are derived from the model (sortable, filterable, personalized), rendered read-only
 * outside the enclosing form's edit mode and editable within it, according to a
 * {@link RowEditPolicy}.
 *
 * <p>
 * Keeps the table's standard capabilities across the edit lifecycle: column personalization via a
 * {@link ViewStateStore}, two-way selection channel binding, per-column filter UI, and automatic
 * row refresh on model or input changes. While an edit session is running, the row set is frozen
 * (no automatic re-query), so edited rows stay in place; after save or cancel the query is
 * re-evaluated.
 * </p>
 */
public class EditableTableControl extends RowSetTableControl {

	/**
	 * A data column of an editable table: its resolved {@link ColumnSetup} plus the read-only
	 * flag.
	 *
	 * @param setup
	 *        The resolved column descriptor.
	 * @param readonly
	 *        Whether the column stays read-only in edit mode.
	 */
	public record EditColumn(ColumnSetup setup, boolean readonly) {
		// Pure data carrier.
	}

	private final ViewContext _viewContext;

	private final List<EditColumn> _columns;

	private final RowEditPolicy _policy;

	private final ViewStateStore _store;

	private final TableId _tableId;

	private final ViewChannel _selectionChannel;

	private final Function<Object[], Collection<?>> _rowFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private final Set<Object> _selectedKeys = new LinkedHashSet<>();

	private RowSourceObserver<TLObject> _observer;

	/** Whether the first client write happened (the point from which observers attach directly). */
	private boolean _written;

	/** Guard breaking the notification cycle between selection channel and table selection. */
	private boolean _applyingFromChannel;

	private ViewChannel.ChannelListener _selectionChannelListener;

	/**
	 * Creates an {@link EditableTableControl}.
	 *
	 * @param context
	 *        The view context (channels, model scope, React wiring).
	 * @param formControl
	 *        The enclosing form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics.
	 * @param columns
	 *        The data columns with their read-only flags.
	 * @param policy
	 *        Which rows are editable in edit mode.
	 * @param store
	 *        Persists column personalization.
	 * @param tableId
	 *        The personalization key.
	 * @param selectionChannel
	 *        Optional channel carrying the selected row object(s), or {@code null}.
	 * @param rowFunction
	 *        Computes the row objects from the input channel values (for automatic refresh).
	 * @param observedTypes
	 *        Types whose object changes trigger a row refresh outside edit sessions.
	 * @param inputChannels
	 *        Channels whose changes trigger a row refresh outside edit sessions.
	 */
	public EditableTableControl(ViewContext context, FormControl formControl, RowSetBinding binding,
			List<EditColumn> columns, RowEditPolicy policy, ViewStateStore store, TableId tableId,
			ViewChannel selectionChannel, Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels) {
		super(context, formControl, binding, null, List.of(), null);
		_viewContext = context;
		_columns = columns;
		_policy = policy;
		_store = store;
		_tableId = tableId;
		_selectionChannel = selectionChannel;
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;

		if (_selectionChannel != null) {
			_selectionChannelListener = (sender, oldValue, newValue) -> reapplySelectionFromChannel();
			_selectionChannel.addListener(_selectionChannelListener);
		}

		addBeforeWriteAction(() -> {
			_written = true;
			attachObserver();
		});
	}

	@Override
	protected List<Column<TLObject, ?>> createDataColumns(boolean editMode) {
		List<Column<TLObject, ?>> columns = new ArrayList<>(_columns.size());
		for (EditColumn column : _columns) {
			Column<Object, ?> inner = column.setup().binding().createColumn(column.setup());
			columns.add(adapt(inner, editMode && !column.readonly()));
		}
		return columns;
	}

	private <V> Column<TLObject, V> adapt(Column<Object, V> inner, boolean editable) {
		return new EditAwareColumn<>(inner, editable);
	}

	@Override
	protected DefaultTableView<TLObject> createTableView(List<Column<TLObject, ?>> columns,
			ListRowSource<TLObject> source) {
		return DefaultTableView.create(columns, source, _store, _tableId);
	}

	@Override
	protected boolean isRowEditable(TLObject row) {
		if (_policy == RowEditPolicy.SELECTED) {
			// The row source keys rows by identity, so the selected keys are the row objects.
			return _selectedKeys.contains(row);
		}
		return true;
	}

	@Override
	protected void afterTableBuilt(TableViewControl<TLObject> table, boolean editMode) {
		table.setSelectionListener(this::handleSelectionChanged);
		reapplySelectionFromChannel();

		// Let each column contribute any per-session UI (e.g. a custom filter dialog).
		for (EditColumn column : _columns) {
			column.setup().binding().installUI(column.setup(), table);
		}

		// Refresh rows on model or input changes — but only outside edit sessions: while editing,
		// the row set is frozen so edited rows (overlays) stay in place until save or cancel.
		detachObserver();
		if (!editMode) {
			_observer = new RowSourceObserver<>(getRowSource(), _rowFunction, _observedTypes, _inputChannels,
				() -> {
					table.refreshData();
					reapplySelectionFromChannel();
				});
			if (_written) {
				attachObserver();
			}
		}
	}

	@Override
	protected void onRowAdded(TLObject newRow) {
		// Select the created row so it is immediately editable under the SELECTED policy (and
		// scrolled into view under any policy).
		TableViewControl<TLObject> table = getTableControl();
		if (table != null) {
			table.selectRow(newRow);
		}
	}

	private void handleSelectionChanged(Set<Object> selectedKeys) {
		// Rows whose selection state flips change their editability under the SELECTED policy, so
		// their cells must re-render.
		Set<Object> flipped = new LinkedHashSet<>(_selectedKeys);
		Set<Object> unchanged = new HashSet<>(flipped);
		unchanged.retainAll(selectedKeys);
		flipped.addAll(selectedKeys);
		flipped.removeAll(unchanged);

		_selectedKeys.clear();
		_selectedKeys.addAll(selectedKeys);

		TableViewControl<TLObject> table = getTableControl();
		if (_policy == RowEditPolicy.SELECTED && getEditor().isEditing() && table != null && !flipped.isEmpty()) {
			table.invalidateRowCells(flipped);
		}

		if (_selectionChannel != null && !_applyingFromChannel) {
			if (selectedKeys.size() == 1) {
				_selectionChannel.set(selectedKeys.iterator().next());
			} else if (selectedKeys.isEmpty()) {
				_selectionChannel.set(null);
			} else {
				_selectionChannel.set(selectedKeys);
			}
		}
	}

	private void reapplySelectionFromChannel() {
		TableViewControl<TLObject> table = getTableControl();
		if (_selectionChannel == null || table == null) {
			return;
		}
		Object value = _selectionChannel.get();
		_applyingFromChannel = true;
		try {
			table.selectRow(value instanceof Collection ? null : value);
		} finally {
			_applyingFromChannel = false;
		}
	}

	private void attachObserver() {
		if (_observer != null) {
			_observer.attach(_viewContext.getModelScope());
		}
	}

	private void detachObserver() {
		if (_observer != null) {
			_observer.detach();
			_observer = null;
		}
	}

	@Override
	protected void cleanupChildren() {
		detachObserver();
		if (_selectionChannel != null && _selectionChannelListener != null) {
			_selectionChannel.removeListener(_selectionChannelListener);
			_selectionChannelListener = null;
		}
		super.cleanupChildren();
	}

	/**
	 * Adapts a type-derived {@link Column} (rows typed {@code Object}) to the {@link TLObject} row
	 * type of the editable table, rendering cells editable when the enclosing form edits and the
	 * {@link RowEditPolicy} covers the row.
	 */
	private final class EditAwareColumn<V> implements Column<TLObject, V> {

		private final Column<Object, V> _inner;

		private final boolean _editable;

		EditAwareColumn(Column<Object, V> inner, boolean editable) {
			_inner = inner;
			_editable = editable;
		}

		@Override
		public String name() {
			return _inner.name();
		}

		@Override
		public ResKey label() {
			return _inner.label();
		}

		@Override
		public V value(TLObject row) {
			return _inner.value(row);
		}

		@Override
		public CellRenderer<V> renderer() {
			return _inner.renderer();
		}

		@Override
		public CellContent renderCell(TLObject row) {
			if (_editable && row != null && isRowEditable(row)) {
				return new CellContent.Raw((CellControlFactory) context -> {
					ReactControl editControl = getEditor().createEditCellControl(context, row, name());
					return editControl != null
						? editControl
						: CellContentReactAdapter.toControl(context, _inner.renderCell(row));
				});
			}
			return _inner.renderCell(row);
		}

		@Override
		public Optional<Sort<V>> sort() {
			return _inner.sort();
		}

		@Override
		public Optional<ColumnFilter<V>> filter() {
			return _inner.filter();
		}

		@Override
		public Optional<Aggregator<TLObject, V>> aggregate() {
			return _inner.aggregate().map(aggregator -> group -> aggregator.over(asObjectGroup(group)));
		}

		@Override
		public int defaultWidth() {
			return _inner.defaultWidth();
		}

		@Override
		public boolean frozenEligible() {
			return _inner.frozenEligible();
		}

		@Override
		public String cssClass(TLObject row) {
			return _inner.cssClass(row);
		}

		@Override
		public Optional<CellExistence<TLObject>> existence() {
			return _inner.existence().map(existence -> existence::cellExists);
		}

	}

	private static Group<Object> asObjectGroup(Group<TLObject> group) {
		return new Group<>() {

			@Override
			public GroupKey key() {
				return group.key();
			}

			@Override
			public int size() {
				return group.size();
			}

			@Override
			public List<Object> members() {
				return List.copyOf(group.members());
			}
		};
	}

}
