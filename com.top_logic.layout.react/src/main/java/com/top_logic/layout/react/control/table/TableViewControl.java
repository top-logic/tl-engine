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

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.AgentModelKey;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.scripting.ReactActionContext;
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

	// Command names.
	private static final String CMD_OPEN_FILTER = "openFilter";

	private static final String CMD_SCROLL = "scroll";

	private static final String CMD_SORT = "sort";

	private static final String CMD_SELECT = "select";

	private static final String CMD_SELECT_BY_KEY = "selectByKey";

	private static final String CMD_MOVE_SELECTION = "moveSelection";

	private static final String CMD_SELECT_ALL = "selectAll";

	private static final String CMD_COLUMN_RESIZE = "columnResize";

	private static final String CMD_COLUMN_REORDER = "columnReorder";

	private static final String CMD_EXPAND = "expand";

	private static final String CMD_SET_FROZEN_COLUMN_COUNT = "setFrozenColumnCount";

	// Command argument names.
	private static final String ARG_ROW_INDEX = "rowIndex";

	private static final String ARG_CTRL_KEY = "ctrlKey";

	private static final String ARG_SHIFT_KEY = "shiftKey";

	private static final String ARG_KEY = "key";

	private static final String ARG_START = "start";

	private static final String ARG_COUNT = "count";

	private static final String ARG_COLUMN = "column";

	private static final String ARG_DIRECTION = "direction";

	private static final String ARG_MODE = "mode";

	private static final String ARG_EXTEND = "extend";

	private static final String ARG_MOVE = "move";

	private static final String ARG_WIDTH = "width";

	private static final String ARG_TARGET_INDEX = "targetIndex";

	private static final String ARG_EXPANDED = "expanded";

	private static final String ARG_SELECTED = "selected";

	// Navigation direction argument values.
	private static final String DIR_UP = "up";

	private static final String DIR_DOWN = "down";

	private static final String DIR_HOME = "home";

	private static final String DIR_END = "end";

	private static final String DIR_PAGE_UP = "pageUp";

	private static final String DIR_PAGE_DOWN = "pageDown";

	// Selection mode values.
	private static final String MODE_MULTI = "multi";

	private static final String MODE_SINGLE = "single";

	// Sort direction/accumulation argument values.
	private static final String SORT_ASC = "asc";

	private static final String SORT_DESC = "desc";

	private static final String SORT_MODE_ADD = "add";

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
		_selectionMode = view.state().getSelection().mode() == SelectionMode.MULTI ? MODE_MULTI : MODE_SINGLE;

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
				def.setSortDirection(SORT_ASC);
			} else if (column.sortDirection() == SortDirection.DESC) {
				def.setSortDirection(SORT_DESC);
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
	 * Adds an agent-facing {@link #ROWS} projection to the headless state.
	 *
	 * <p>
	 * The regular {@link #ROWS} state holds cell <em>controls</em> (so it is stripped from the agent
	 * projection, which omits control-bearing state). Here each visible row is projected as plain
	 * text — {@link #ARG_ROW_INDEX}, {@link #ROW_SELECTED} and the per-column cell text — so an agent
	 * can read the table and choose a {@link #ARG_ROW_INDEX} for {@link #CMD_SELECT}. Bounded to the
	 * current viewport (capped), with {@link #TOTAL_ROW_COUNT} already in the scalar state indicating
	 * how many more exist (reachable via {@link #CMD_SCROLL}).
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
			rowState.put(ARG_ROW_INDEX, Integer.valueOf(index));
			rowState.put("selected", Boolean.valueOf(_selectedKeys.contains(row.key())));
			String key = AgentModelKey.toJson(row.data());
			if (key != null) {
				rowState.put(ARG_KEY, key);
			}
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
	@ReactCommand(value = CMD_OPEN_FILTER, params = @ReactParam(name = ARG_COLUMN, required = true,
		description = "The name of the column whose filter dialog to open."))
	void handleOpenFilter(Map<String, Object> arguments) {
		String column = (String) arguments.get(ARG_COLUMN);
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
	@ReactCommand(value = CMD_SCROLL, params = {
		@ReactParam(name = ARG_START, type = "int", required = true, description = "Zero-based index of the first visible row."),
		@ReactParam(name = ARG_COUNT, type = "int", required = true, description = "Number of rows in the viewport.") })
	void handleScroll(Map<String, Object> arguments) {
		int start = ((Number) arguments.get(ARG_START)).intValue();
		int count = ((Number) arguments.get(ARG_COUNT)).intValue();
		updateViewport(start, count);
	}

	/**
	 * Handles a sort request (single click replaces, shift-click adds/toggles).
	 */
	@ReactCommand(value = CMD_SORT, params = {
		@ReactParam(name = ARG_COLUMN, required = true, description = "The name of the column to sort by."),
		@ReactParam(name = ARG_DIRECTION, description = "Sort direction; \"desc\" for descending, otherwise ascending."),
		@ReactParam(name = ARG_MODE, description = "\"add\" to append to the current multi-sort, otherwise replace.") })
	void handleSort(Map<String, Object> arguments) {
		String column = (String) arguments.get(ARG_COLUMN);
		boolean ascending = !SORT_DESC.equals(arguments.get(ARG_DIRECTION));
		boolean add = SORT_MODE_ADD.equals(arguments.get(ARG_MODE));

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
	@ReactCommand(value = CMD_SELECT, params = {
		@ReactParam(name = ARG_ROW_INDEX, type = "int", required = true,
			description = "Zero-based index of the row to select (see the projected rows)."),
		@ReactParam(name = ARG_CTRL_KEY, type = "boolean", description = "Toggle selection (multi-select)."),
		@ReactParam(name = ARG_SHIFT_KEY, type = "boolean", description = "Range selection (multi-select).") })
	void handleSelect(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get(ARG_ROW_INDEX)).intValue();
		int total = _view.rowCount();
		if (rowIndex < 0 || rowIndex >= total) {
			return;
		}
		boolean ctrlKey = Boolean.TRUE.equals(arguments.get(ARG_CTRL_KEY));
		boolean shiftKey = Boolean.TRUE.equals(arguments.get(ARG_SHIFT_KEY));
		Object key = keyAt(rowIndex);
		_cursorIndex = rowIndex;

		if (MODE_MULTI.equals(_selectionMode)) {
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
	 * Selects the single row whose business object is named by the given {@link AgentModelKey key} —
	 * the replay-stable counterpart of {@link #handleSelect} by {@link #ARG_ROW_INDEX}, which a
	 * recorded selection is captured as so it survives sorting, filtering and a fresh session.
	 *
	 * @param arguments
	 *        Must contain a {@link #ARG_KEY} with a row business key (the same key the agent projection
	 *        puts on each row).
	 */
	@ReactCommand(value = CMD_SELECT_BY_KEY, params = @ReactParam(name = ARG_KEY, required = true,
		description = "Business key of the row to select (the 'key' projected onto each row)."))
	void handleSelectByKey(Map<String, Object> arguments) {
		String key = (String) arguments.get(ARG_KEY);
		if (key == null) {
			return;
		}
		ModelName name = AgentModelKey.fromJson(key);
		if (name == null) {
			return;
		}
		Object target;
		try {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
			ActionContext actionContext =
				new ReactActionContext(displayContext, displayContext.asRequest().getSession());
			target = ModelResolver.locateModel(actionContext, null, name);
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve row for key: " + key, ex, this);
			return;
		}
		if (target == null) {
			return;
		}
		int total = _view.rowCount();
		List<Row<R>> rows = _view.rows(0, total);
		for (int i = 0; i < rows.size(); i++) {
			Row<R> row = rows.get(i);
			if (target.equals(row.data())) {
				_selectedKeys.clear();
				_selectedKeys.add(row.key());
				_cursorIndex = i;
				_selectionAnchor = i;
				pushSelection();
				updateViewport(_viewportStart, _viewportCount);
				return;
			}
		}
	}

	/**
	 * The table's purely view-affecting commands (viewport scrolling, column geometry) are not user
	 * intent and must not clutter a recording — added to the {@link #nonRecordableCommands() chrome
	 * default}.
	 */
	@Override
	public Set<String> nonRecordableCommands() {
		Set<String> result = new LinkedHashSet<>(super.nonRecordableCommands());
		result.add(CMD_SCROLL);
		result.add(CMD_COLUMN_RESIZE);
		result.add(CMD_COLUMN_REORDER);
		result.add(CMD_SET_FROZEN_COLUMN_COUNT);
		return result;
	}

	/**
	 * Records a plain (unmodified) row selection in replay-stable form: a {@link #CMD_SELECT} by
	 * {@link #ARG_ROW_INDEX} becomes a {@link #CMD_SELECT_BY_KEY} of the row's business key, so the
	 * recording survives sorting and a fresh session. Modifier selections (ctrl/shift range/toggle)
	 * are recorded verbatim — their semantics are index/anchor based.
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_SELECT.equals(command) && arguments != null
				&& arguments.get(ARG_ROW_INDEX) instanceof Number rowIndex
				&& !Boolean.TRUE.equals(arguments.get(ARG_CTRL_KEY))
				&& !Boolean.TRUE.equals(arguments.get(ARG_SHIFT_KEY))) {
			List<Row<R>> single = _view.rows(rowIndex.intValue(), rowIndex.intValue() + 1);
			if (!single.isEmpty()) {
				String key = AgentModelKey.toJson(single.get(0).data());
				if (key != null) {
					return new RecordedCommand(CMD_SELECT_BY_KEY, Map.of(ARG_KEY, key));
				}
			}
		}
		return super.recordCommand(command, arguments);
	}

	/**
	 * Handles keyboard row navigation (arrow keys, Home/End, PageUp/PageDown) with a unified
	 * cursor/lead model.
	 *
	 * <p>
	 * Arguments: {@link #ARG_DIRECTION} ({@link #DIR_UP}, {@link #DIR_DOWN}, {@link #DIR_HOME},
	 * {@link #DIR_END}, {@link #DIR_PAGE_UP}, {@link #DIR_PAGE_DOWN}); {@link #ARG_EXTEND} (Shift:
	 * extend the range from the anchor); {@link #ARG_MOVE} (Ctrl: move the focus cursor without
	 * changing the selection, multi only). The target index is resolved against the full row list, so
	 * it is correct even when the
	 * target row is outside the rendered window; the window is then scrolled to include it.
	 * </p>
	 */
	@ReactCommand(value = CMD_MOVE_SELECTION, params = {
		@ReactParam(name = ARG_DIRECTION, required = true,
			description = "Navigation direction: up, down, home, end, pageUp or pageDown."),
		@ReactParam(name = ARG_EXTEND, type = "boolean", description = "Extend the selection range from the anchor (Shift)."),
		@ReactParam(name = ARG_MOVE, type = "boolean", description = "Move the focus cursor without changing the selection (Ctrl, multi-select).") })
	void handleMoveSelection(Map<String, Object> arguments) {
		int total = _view.rowCount();
		if (total == 0) {
			return;
		}
		String direction = String.valueOf(arguments.get(ARG_DIRECTION));
		boolean extend = Boolean.TRUE.equals(arguments.get(ARG_EXTEND));
		boolean move = Boolean.TRUE.equals(arguments.get(ARG_MOVE));
		int page = Math.max(1, _viewportCount);

		int from = _cursorIndex;
		int target;
		if (from < 0) {
			// First navigation lands on the edge rather than stepping from an implicit position.
			target = DIR_END.equals(direction) ? total - 1 : 0;
		} else {
			switch (direction) {
				case DIR_UP: target = from - 1; break;
				case DIR_DOWN: target = from + 1; break;
				case DIR_HOME: target = 0; break;
				case DIR_END: target = total - 1; break;
				case DIR_PAGE_UP: target = from - page; break;
				case DIR_PAGE_DOWN: target = from + page; break;
				default: return;
			}
		}
		target = Math.max(0, Math.min(total - 1, target));
		_cursorIndex = target;
		Object key = keyAt(target);

		if (MODE_MULTI.equals(_selectionMode) && move) {
			// Ctrl: move the focus cursor only; leave the selection untouched.
		} else if (MODE_MULTI.equals(_selectionMode) && extend) {
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
	@ReactCommand(value = CMD_SELECT_ALL, params = @ReactParam(name = ARG_SELECTED, type = "boolean",
		description = "True to select all data rows, false to clear the selection."))
	void handleSelectAll(Map<String, Object> arguments) {
		boolean selected = Boolean.TRUE.equals(arguments.get(ARG_SELECTED));
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
	@ReactCommand(value = CMD_COLUMN_RESIZE, params = {
		@ReactParam(name = ARG_COLUMN, required = true, description = "The name of the column to resize."),
		@ReactParam(name = ARG_WIDTH, type = "int", required = true, description = "The new column width in pixels.") })
	void handleColumnResize(Map<String, Object> arguments) {
		String column = (String) arguments.get(ARG_COLUMN);
		int width = Math.max(MIN_WIDTH, ((Number) arguments.get(ARG_WIDTH)).intValue());
		_view.resizeColumn(column, width);
		refreshColumns();
	}

	/**
	 * Handles a column reorder.
	 */
	@ReactCommand(value = CMD_COLUMN_REORDER, params = {
		@ReactParam(name = ARG_COLUMN, required = true, description = "The name of the column to move."),
		@ReactParam(name = ARG_TARGET_INDEX, type = "int", required = true, description = "Zero-based index to move the column to.") })
	void handleColumnReorder(Map<String, Object> arguments) {
		String column = (String) arguments.get(ARG_COLUMN);
		int targetIndex = ((Number) arguments.get(ARG_TARGET_INDEX)).intValue();
		_view.moveColumn(column, targetIndex);
		rebuildAfterRowChange();
	}

	/**
	 * Handles a tree/group expand or collapse.
	 */
	@ReactCommand(value = CMD_EXPAND, params = {
		@ReactParam(name = ARG_ROW_INDEX, type = "int", required = true,
			description = "Zero-based index of the tree/group row to expand or collapse."),
		@ReactParam(name = ARG_EXPANDED, type = "boolean", required = true,
			description = "True to expand the row, false to collapse it.") })
	void handleExpand(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get(ARG_ROW_INDEX)).intValue();
		boolean expanded = Boolean.TRUE.equals(arguments.get(ARG_EXPANDED));
		if (rowIndex < 0 || rowIndex >= _view.rowCount()) {
			return;
		}
		_view.setExpanded(keyAt(rowIndex), expanded);
		rebuildAfterRowChange();
	}

	/**
	 * Handles a change of the frozen column count.
	 */
	@ReactCommand(value = CMD_SET_FROZEN_COLUMN_COUNT, params = @ReactParam(name = ARG_COUNT, type = "int", required = true,
		description = "Number of leading columns to keep frozen (clamped to at least zero)."))
	void handleSetFrozenColumnCount(Map<String, Object> arguments) {
		int count = ((Number) arguments.get(ARG_COUNT)).intValue();
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
			MODE_MULTI.equals(_selectionMode) ? SelectionMode.MULTI : SelectionMode.SINGLE,
			new LinkedHashSet<>(_selectedKeys)));
		if (_selectionListener != null) {
			_selectionListener.selectionChanged(new LinkedHashSet<>(_selectedKeys));
		}
	}

}
