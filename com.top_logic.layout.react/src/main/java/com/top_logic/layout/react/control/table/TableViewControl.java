/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.table.CellContent;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterState;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.tool.boundsec.HandlerResult;
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
 * <p>
 * <b>Placement requirement:</b> this control virtualizes - it renders only the row window that
 * fits its scroll viewport and scrolls internally - so it must be given a container with a
 * <em>definite (bounded) height</em>. Its root fills its parent ({@code height: 100%}); if every
 * ancestor up to a bounded box is content-sized, there is no viewport to bound against, so the
 * control expands to its full natural height and the <em>surrounding</em> scroller (e.g. the tab
 * content) scrolls instead - showing a large scrollbar while the table never pages in new rows.
 * Inside the {@code com.top_logic.layout.view} layer, place it in a {@code <split-panel>}/
 * {@code <pane>} or a fill panel ({@code <panel fill="true">}); standalone (e.g. in a JSP-rendered
 * component) wrap it in a fixed- or flex-bounded container as the React-table demo does.
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

	/** State key for the keyboard focus/lead row index ({@code -1} when none). */
	private static final String CURSOR_INDEX = "cursorIndex";

	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	private static final String TREE_MODE = "treeMode";

	private static final String ROW_ID = "id";

	private static final String ROW_INDEX = "index";

	private static final String ROW_SELECTED = "selected";

	private static final String ROW_CELLS = "cells";

	private static final String TREE_DEPTH = "treeDepth";

	private static final String TREE_EXPANDABLE = "expandable";

	private static final String TREE_EXPANDED = "expanded";

	private static final int PREFETCH_ROWS = 20;

	private static final int MIN_WIDTH = 50;

	private final TableView<R> _view;

	private final boolean _treeMode;

	private int _viewportStart;

	private int _viewportCount = 50;

	private final String _selectionMode;

	private final Set<Object> _selectedKeys = new LinkedHashSet<>();

	private int _selectionAnchor = -1;

	/** The keyboard focus/lead row index; {@code -1} when no row has keyboard focus. */
	private int _cursorIndex = -1;

	private SelectionListener _selectionListener;

	/** Cell controls for currently buffered rows, keyed by row key then column name. */
	private final Map<Object, Map<String, ReactControl>> _cellCache = new LinkedHashMap<>();

	/** View-supplied custom filter UIs, keyed by column name. */
	private final Map<String, ColumnFilterUI> _filterUIs = new LinkedHashMap<>();

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

	/**
	 * Registers a view-supplied custom filter UI for a column, used instead of the built-in filter
	 * editor when the column's funnel is opened.
	 */
	public void setFilterUI(String column, ColumnFilterUI ui) {
		_filterUIs.put(column, ui);
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
		putState(CURSOR_INDEX, Integer.valueOf(_cursorIndex));
	}

	/**
	 * Adds an agent-facing {@code rows} projection to the headless state.
	 *
	 * <p>
	 * The regular {@code rows} state holds cell <em>controls</em> (so it is stripped from the agent
	 * projection, which omits control-bearing state). Here each visible row is projected as plain
	 * text — {@code rowIndex}, {@code selected} and the per-column cell text — so an agent can read
	 * the table and choose a {@code rowIndex} for {@code select}. Bounded to the current viewport
	 * (capped), with {@code totalRowCount} already in the scalar state indicating how many more exist
	 * (reachable via {@code scroll}).
	 * </p>
	 */
	@Override
	public Map<String, Object> agentScalarState() {
		Map<String, Object> result = super.agentScalarState();
		result.put("rows", agentRows());
		return result;
	}

	private List<Map<String, Object>> agentRows() {
		int maxRows = 100;
		int total = _view.rowCount();
		int start = Math.max(0, Math.min(_viewportStart, total));
		int count = _viewportCount > 0 ? _viewportCount : 50;
		int end = Math.min(total, start + Math.min(count, maxRows));
		List<Map<String, Object>> out = new ArrayList<>();
		if (start >= end) {
			return out;
		}
		int index = start;
		for (Row<R> row : _view.rows(start, end)) {
			Map<String, Object> rowState = new LinkedHashMap<>();
			rowState.put("rowIndex", Integer.valueOf(index));
			rowState.put("selected", Boolean.valueOf(_selectedKeys.contains(row.key())));
			Map<String, Object> cells = new LinkedHashMap<>();
			for (ColumnView column : _view.columns()) {
				cells.put(column.name(), cellText(_view.cell(row, column.name())));
			}
			rowState.put("cells", cells);
			out.add(rowState);
			index++;
		}
		return out;
	}

	private static String cellText(CellContent content) {
		if (content instanceof CellContent.Text text) {
			return text.text();
		}
		if (content instanceof CellContent.Labeled labeled) {
			return labeled.text();
		}
		if (content instanceof CellContent.Editable editable) {
			Object value = editable.field().getValue();
			return value == null ? "" : String.valueOf(value);
		}
		if (content instanceof CellContent.Raw raw) {
			Object payload = raw.payload();
			if (payload instanceof String || payload instanceof Number || payload instanceof Boolean) {
				return String.valueOf(payload);
			}
		}
		return "";
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
	}

	// -- Filter dialog --

	/**
	 * Opens the filter dialog for a column.
	 *
	 * <p>
	 * The dialog is composed entirely from standard React controls: the column's
	 * {@link FilterEditor} fields are laid out by {@link ReactFormBuilder} (labels + chrome),
	 * wrapped in a {@link ReactWindowControl} with reset / cancel / apply
	 * {@link MessageButtons}, and shown through the {@link DialogManager}. The input control
	 * per field is chosen from the field model itself ({@link #fieldControl}).
	 * </p>
	 */
	@ReactCommand("openFilter")
	void handleOpenFilter(Map<String, Object> arguments) {
		String column = (String) arguments.get("column");
		ColumnFilter<?> filter = _view.columnFilter(column);
		if (filter == null) {
			return;
		}
		ReactContext context = getReactContext();
		DialogManager dialogs = context.getDialogManager();
		if (dialogs == null) {
			return;
		}
		Resources resources = Resources.getInstance();

		// A view-registered custom filter UI (e.g. a model-form, script-evaluated filter) builds its
		// own dialog body; otherwise the built-in editor pipeline derives one from the filter input.
		ReactControl body;
		Supplier<FilterState> readState;
		ColumnFilterUI customUI = _filterUIs.get(column);
		if (customUI != null) {
			body = customUI.buildForm(context);
			readState = customUI::read;
		} else {
			// Facet counts are value-based, so only meaningful for filters that opt in (e.g. options);
			// a predicate-based options filter (regexp) declines them to avoid misleading "(0)" labels.
			MatchCounts counts = filter.countsMatches() ? _view.columnMatchCounts(column) : MatchCounts.NONE;
			FilterEditor editor = FilterEditors.create(filter, _view.state().getFilters().get(column), counts);
			// Build through the shared form pipeline with the same defaults as a model-bound form
			// (responsive columns + automatic label position), so the dialog form renders and reflows
			// exactly like every other form.
			ReactFormBuilder form = new ReactFormBuilder(context);
			for (FilterField field : editor.fields()) {
				form.addField(label(resources, field.label()), fieldControl(context, field));
			}
			body = form.build();
			readState = editor::read;
		}

		ReactWindowControl window = new ReactWindowControl(context,
			resources.getString(I18NConstants.JS_TABLE_FILTER), DisplayDimension.px(380),
			() -> dialogs.closeTopDialog(DialogResult.cancelled()));
		window.setChild(body);
		// Apply is the dialog's default action: primary-styled and Enter-bound, matching the legacy
		// filter popup (Enter applies from anywhere in the form).
		ReactButtonControl applyButton = MessageButtons.ok(context, ctx -> {
			_view.filter(column, readState.get());
			rebuildAfterRowChange();
			dialogs.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		applyButton.markAsDefault();
		window.setActions(List.of(
			new ReactButtonControl(context, resources.getString(I18NConstants.JS_TABLE_CLEAR), ctx -> {
				_view.filter(column, null);
				rebuildAfterRowChange();
				dialogs.closeTopDialog(DialogResult.ok(null));
				return HandlerResult.DEFAULT_RESULT;
			}),
			MessageButtons.cancel(context, ctx -> {
				dialogs.closeTopDialog(DialogResult.cancelled());
				return HandlerResult.DEFAULT_RESULT;
			}),
			applyButton));
		// A backdrop click dismisses the dialog, discarding edits just like Cancel / Escape.
		dialogs.openDialog(true, window, result -> {
			// Reset / Apply already acted; Cancel discards.
		});
	}

	/**
	 * Chooses the input control for a filter field from its model: a dropdown for a
	 * {@link SelectFieldModel}, a checkbox for a boolean value, a text input otherwise.
	 */
	private static ReactControl fieldControl(ReactContext context, FilterField field) {
		FieldModel model = field.model();
		if (model instanceof SelectFieldModel selectModel) {
			LabelProvider labels = field.optionLabels() != null ? field.optionLabels() : String::valueOf;
			return new ReactSelectFormFieldControl(context, selectModel, labels);
		}
		if (model.getValue() instanceof Boolean) {
			return new ReactCheckboxControl(context, model);
		}
		return new ReactTextInputControl(context, model);
	}

	/**
	 * Rebuilds the row count and viewport after the backing data changed externally (e.g. an
	 * object was created or deleted and the row source was refreshed). Stale selected keys that no
	 * longer match a row are dropped.
	 */
	public void refreshData() {
		_selectedKeys.retainAll(currentRowKeys());
		rebuildAfterRowChange();
	}

	private Set<Object> currentRowKeys() {
		Set<Object> keys = new LinkedHashSet<>();
		for (Row<R> row : _view.rows(0, _view.rowCount())) {
			keys.add(row.key());
		}
		return keys;
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
	@ReactCommand(value = "select", params = {
		@ReactParam(name = "rowIndex", type = "int", required = true,
			description = "Zero-based index of the row to select (see the projected rows)."),
		@ReactParam(name = "ctrlKey", type = "boolean", description = "Toggle selection (multi-select)."),
		@ReactParam(name = "shiftKey", type = "boolean", description = "Range selection (multi-select).") })
	void handleSelect(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		int total = _view.rowCount();
		if (rowIndex < 0 || rowIndex >= total) {
			return;
		}
		boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
		boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));
		Object key = keyAt(rowIndex);
		_cursorIndex = rowIndex;

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
	 * Handles keyboard row navigation (arrow keys, Home/End, PageUp/PageDown) with a unified
	 * cursor/lead model.
	 *
	 * <p>
	 * Arguments: {@code direction} ({@code "up"}, {@code "down"}, {@code "home"}, {@code "end"},
	 * {@code "pageUp"}, {@code "pageDown"}); {@code extend} (Shift: extend the range from the
	 * anchor); {@code move} (Ctrl: move the focus cursor without changing the selection, multi
	 * only). The target index is resolved against the full row list, so it is correct even when the
	 * target row is outside the rendered window; the window is then scrolled to include it.
	 * </p>
	 */
	@ReactCommand("moveSelection")
	void handleMoveSelection(Map<String, Object> arguments) {
		int total = _view.rowCount();
		if (total == 0) {
			return;
		}
		String direction = String.valueOf(arguments.get("direction"));
		boolean extend = Boolean.TRUE.equals(arguments.get("extend"));
		boolean move = Boolean.TRUE.equals(arguments.get("move"));
		int page = Math.max(1, _viewportCount);

		int from = _cursorIndex;
		int target;
		if (from < 0) {
			// First navigation lands on the edge rather than stepping from an implicit position.
			target = "end".equals(direction) ? total - 1 : 0;
		} else {
			switch (direction) {
				case "up": target = from - 1; break;
				case "down": target = from + 1; break;
				case "home": target = 0; break;
				case "end": target = total - 1; break;
				case "pageUp": target = from - page; break;
				case "pageDown": target = from + page; break;
				default: return;
			}
		}
		target = Math.max(0, Math.min(total - 1, target));
		_cursorIndex = target;
		Object key = keyAt(target);

		if ("multi".equals(_selectionMode) && move) {
			// Ctrl: move the focus cursor only; leave the selection untouched.
		} else if ("multi".equals(_selectionMode) && extend) {
			if (_selectionAnchor < 0) {
				_selectionAnchor = from < 0 ? target : from;
			}
			_selectedKeys.clear();
			int lo = Math.min(_selectionAnchor, target);
			int hi = Math.max(_selectionAnchor, target);
			for (Row<R> row : _view.rows(lo, hi + 1)) {
				if (row.kind() == RowKind.DATA) {
					_selectedKeys.add(row.key());
				}
			}
		} else {
			// Plain move (and the single-selection case): selection follows the cursor.
			_selectedKeys.clear();
			if (key != null) {
				_selectedKeys.add(key);
			}
			_selectionAnchor = target;
		}

		pushSelection();
		scrollIntoView(target);
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

	/**
	 * Re-buffers the viewport so the given row is within the rendered window, then re-renders.
	 */
	private void scrollIntoView(int target) {
		int start = _viewportStart;
		if (target < start) {
			start = target;
		} else if (target >= start + _viewportCount) {
			start = target - _viewportCount + 1;
		}
		updateViewport(Math.max(0, start), _viewportCount);
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
