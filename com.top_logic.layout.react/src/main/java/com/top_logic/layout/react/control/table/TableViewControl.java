/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
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
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.scripting.ReactActionContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.table.CellContent;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.table.filter.TextFilterState;
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
 * activate, resize, reorder, expand, freeze) back onto {@link TableView} commands. It depends only on
 * the green-field model, not on the legacy {@code TableModel}.
 * </p>
 *
 * <p>
 * Rows are dragged and dropped through the seam of
 * {@link com.top_logic.layout.react.control.dnd}: {@link #setDragSource(String)} makes the rows
 * draggable under a type tag, {@link #setDropTarget(DropTarget)} accepts a drop of such objects and
 * applies it. A drag names client-side row keys only, and each control resolves the keys it owns, so
 * the two ends of a drag between two tables need know nothing of each other.
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
 * {@code <pane>} or a fill panel ({@code <panel fill="true">}) - the containers between such a
 * filling control and the next bounded box grow with it, so the chain holds however deeply it is
 * nested; standalone (e.g. in a JSP-rendered component) wrap it in a fixed- or flex-bounded
 * container as the React-table demo does.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public class TableViewControl<R> extends ReactControl implements TooltipProvider, DragSourceControl {

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

	/**
	 * Notified when a row is activated: opened by a double-click, or by {@code Enter} while it
	 * carries the keyboard cursor.
	 *
	 * @param <T>
	 *        The row business object type.
	 */
	@FunctionalInterface
	public interface ActivationHandler<T> {

		/**
		 * Called after the activated row became the table's selection.
		 *
		 * @param row
		 *        The business object of the activated row.
		 * @return The outcome reported to the client (and to a scripted replay).
		 */
		HandlerResult rowActivated(T row);
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

	/**
	 * State key holding the name of the column the rows are grouped by, {@link #NOTHING} when they
	 * are not grouped.
	 */
	private static final String GROUPING = "grouping";

	private static final String ROW_ID = "id";

	/**
	 * Prefix of a row's {@link #ROW_ID id}, followed by the row's {@link #ROW_INDEX index}.
	 *
	 * <p>
	 * This is the identity the client refers to a row by - in a tooltip request, and as the key of a
	 * dragged or dropped-on row. It designates a row for as long as the client's row window is the
	 * one the server sent, which is what a gesture on a displayed row rests on anyway.
	 * </p>
	 *
	 * @see #rowIndex(String)
	 */
	private static final String ROW_ID_PREFIX = "row_";

	private static final String ROW_INDEX = "index";

	private static final String ROW_SELECTED = "selected";

	private static final String ROW_CELLS = "cells";

	private static final String TREE_DEPTH = "treeDepth";

	private static final String TREE_EXPANDABLE = "expandable";

	private static final String TREE_EXPANDED = "expanded";

	/**
	 * Per-row state key holding the number of rows in a group, present exactly on the group header
	 * rows a {@link #GROUPING} introduces.
	 */
	private static final String ROW_GROUP_COUNT = "groupCount";

	private static final int PREFETCH_ROWS = 20;

	private static final int MIN_WIDTH = 50;

	/** State key telling the client whether to offer the column selection. */
	private static final String COLUMN_SELECT = "columnSelect";

	/** Per-column state key telling whether the rows can be grouped by that column. */
	private static final String COLUMN_GROUPABLE = "groupable";

	/** Per-column state key telling whether the column can be filtered. */
	private static final String COLUMN_FILTERABLE = "filterable";

	/** Per-column state key telling whether a {@link #COLUMN_FILTERABLE} column filters right now. */
	private static final String COLUMN_FILTER_ACTIVE = "filterActive";

	/**
	 * Per-column state key telling whether the column {@link ColumnView#pinnedEnd() keeps its place}
	 * at the end of the table, where the client renders it fixed to the right edge.
	 */
	private static final String COLUMN_PINNED_END = "pinnedEnd";

	/**
	 * Per-column state key holding the {@link ColumnView#cssClass() CSS class} the client puts on
	 * every cell of that column, its heading included. Absent for a column declaring none.
	 */
	private static final String COLUMN_CSS_CLASS = "cssClass";

	/** State key telling the client whether to display the filter bar. */
	private static final String FILTER_BAR = "filterBar";

	/** State key holding the filters the table offers under a name, one map per filter. */
	private static final String NAMED_FILTERS = "namedFilters";

	/** Entry key of a {@link #NAMED_FILTERS} filter's {@link NamedFilter#id() identifier}. */
	private static final String NAMED_FILTER_ID = "id";

	/** Entry key of a {@link #NAMED_FILTERS} filter's displayed name. */
	private static final String NAMED_FILTER_LABEL = "label";

	/** Entry key telling whether a {@link #NAMED_FILTERS} filter may be deleted. */
	private static final String NAMED_FILTER_DELETABLE = "deletable";

	/**
	 * State key holding the {@link NamedFilter#id() identifier} of the filter the table currently
	 * matches, empty when it matches none of them.
	 */
	private static final String ACTIVE_NAMED_FILTER = "activeNamedFilter";

	/** State key holding the text the table searches its displayed columns for. */
	private static final String SEARCH = "search";

	/** State key telling the client whether the table keeps filters the user saves. */
	private static final String FILTER_SAVING = "filterSaving";

	/** Value of {@link #ACTIVE_NAMED_FILTER} and {@link #SEARCH} for "none". */
	private static final String NOTHING = "";

	/** State key telling the client whether the rows may be dragged. */
	private static final String DRAG_ENABLED = "dragEnabled";

	/** State key holding the {@link #dragType() type tag} the client tags a drag payload with. */
	private static final String DRAG_TYPE = "dragType";

	/** State key holding the {@link DropTarget#acceptedTypes() type tags} a drop is accepted of. */
	private static final String DROP_ACCEPTS = "dropAccepts";

	/** State key telling the client whether a single row is a drop target of its own. */
	private static final String DROP_ON_ROWS = "dropOnRows";

	// Command names.
	private static final String CMD_OPEN_FILTER = "openFilter";

	private static final String CMD_OPEN_COLUMN_SELECT = "openColumnSelect";

	private static final String CMD_SCROLL = "scroll";

	/** The command the client sends to sort by a column. */
	public static final String CMD_SORT = "sort";

	/**
	 * The command the client sends to group the rows by a column, or to show them ungrouped.
	 *
	 * @see GroupArguments
	 */
	public static final String CMD_GROUP = "group";

	/** The command the client sends when the user clicks a row. */
	public static final String CMD_SELECT = "select";

	private static final String CMD_SELECT_BY_KEY = "selectByKey";

	/**
	 * The command the client sends when the user opens a row: a double-click, or {@code Enter} on
	 * the row carrying the keyboard cursor.
	 *
	 * @see ActivateRowArguments
	 */
	public static final String CMD_ACTIVATE = "activate";

	private static final String CMD_ACTIVATE_BY_KEY = "activateByKey";

	private static final String CMD_MOVE_SELECTION = "moveSelection";

	private static final String CMD_SELECT_ALL = "selectAll";

	/**
	 * The command the client sends to change the width of a column.
	 *
	 * @see ColumnResizeArguments
	 */
	public static final String CMD_COLUMN_RESIZE = "columnResize";

	private static final String CMD_COLUMN_REORDER = "columnReorder";

	/** The command the client sends to expand or collapse a tree node or a group header. */
	public static final String CMD_EXPAND = "expand";

	/**
	 * The command the client sends to fix a number of leading columns while the table scrolls
	 * horizontally.
	 *
	 * @see SetFrozenColumnCountArguments
	 */
	public static final String CMD_SET_FROZEN_COLUMN_COUNT = "setFrozenColumnCount";

	private static final String CMD_APPLY_NAMED_FILTER = "applyNamedFilter";

	private static final String CMD_CLEAR_FILTER = "clearFilter";

	/** The command the client sends to search the displayed columns for a text. */
	public static final String CMD_SEARCH = "search";

	private static final String CMD_SAVE_NAMED_FILTER = "saveNamedFilter";

	private static final String CMD_DELETE_NAMED_FILTER = "deleteNamedFilter";

	private static final String CMD_DROP = "drop";

	private static final String CMD_DROP_OBJECTS = "dropObjects";

	// Command argument names (shared with the typed SelectRowArguments so dispatch, recording and
	// projection agree on the wire keys).
	private static final String ARG_ROW_INDEX = SelectRowArguments.ROW_INDEX;

	private static final String ARG_CTRL_KEY = SelectRowArguments.CTRL_KEY;

	private static final String ARG_SHIFT_KEY = SelectRowArguments.SHIFT_KEY;

	private static final String ARG_KEY = SelectByKeyArguments.KEY;

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

	private final List<SelectionListener> _selectionListeners = new CopyOnWriteArrayList<>();

	/** What a row activation runs, {@code null} for a table whose rows cannot be opened. */
	private ActivationHandler<R> _activationHandler;

	/** Cell controls for currently buffered rows, keyed by row key then column name. */
	private final Map<Object, Map<String, ReactControl>> _cellCache = new LinkedHashMap<>();

	/** View-supplied custom filter UIs, keyed by column name. */
	private final Map<String, ColumnFilterUI> _filterUIs = new LinkedHashMap<>();

	/** Whether the user may choose which columns are displayed, and in which order. */
	private boolean _columnSelect = true;

	/** Whether the named filters, the search field and saving a filter are displayed. */
	private boolean _filterBar;

	/** The type tag dragged rows are announced under, or {@code null} while rows are not draggable. */
	private String _dragType;

	/** What dropped objects are done with, or {@code null} while the table accepts no drop. */
	private DropTarget _dropTarget;

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
		putState(COLUMN_SELECT, Boolean.valueOf(_columnSelect));
		putState(FILTER_BAR, Boolean.valueOf(_filterBar));
		pushGrouping();
		buildFullState();
	}

	/**
	 * Registers a listener notified on selection changes.
	 *
	 * <p>
	 * All registered listeners are notified in registration order. A listener may register or
	 * unregister listeners while being notified; such a change takes effect for subsequent
	 * notifications.
	 * </p>
	 *
	 * @param listener
	 *        The listener to notify.
	 *
	 * @see #removeSelectionListener(SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		_selectionListeners.add(listener);
	}

	/**
	 * Unregisters a listener added through {@link #addSelectionListener(SelectionListener)}.
	 *
	 * @param listener
	 *        The listener to stop notifying.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		_selectionListeners.remove(listener);
	}

	/**
	 * Sets what a row activation runs, replacing any handler set before.
	 *
	 * <p>
	 * The handler is called with the activated row's business object, after that row became the
	 * table's selection. Without one, a double-click and {@code Enter} select the row and do
	 * nothing further.
	 * </p>
	 *
	 * @param handler
	 *        The handler to call, {@code null} to make the rows unopenable again.
	 */
	public void setActivationHandler(ActivationHandler<R> handler) {
		_activationHandler = handler;
	}

	/**
	 * Whether the table offers the column selection (which columns are displayed, in which order).
	 *
	 * <p>
	 * Switch this off for a table whose columns are part of what it means - a matrix over a fixed
	 * set of columns, or a table whose rows are only meaningful next to a specific neighbour.
	 * </p>
	 */
	public void setColumnSelect(boolean columnSelect) {
		_columnSelect = columnSelect;
		putState(COLUMN_SELECT, Boolean.valueOf(columnSelect));
	}

	/**
	 * Whether the table displays its filter bar: the {@link TableView#namedFilters() named filters}
	 * it offers, the free-text search over its displayed columns, and saving the current filter
	 * under a name.
	 */
	public void setFilterBar(boolean filterBar) {
		Object update = beginUpdate();
		try {
			_filterBar = filterBar;
			putState(FILTER_BAR, Boolean.valueOf(filterBar));
			refreshFilterBar();
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Makes the rows draggable, announcing them under the given type tag.
	 *
	 * <p>
	 * Dragging a selected row drags the whole {@link #getSelectedKeys() selection}, an unselected row
	 * drags itself. What a receiving {@link DropTarget} gets are the row business objects; the tag is
	 * what it accepts the drop by.
	 * </p>
	 *
	 * @param dragType
	 *        The {@link #dragType() type tag}, or {@code null} to make the rows undraggable again.
	 */
	public void setDragSource(String dragType) {
		Object update = beginUpdate();
		try {
			_dragType = dragType;
			putState(DRAG_ENABLED, Boolean.valueOf(dragType != null));
			putState(DRAG_TYPE, dragType == null ? NOTHING : dragType);
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Makes the table accept a drop of the objects the given target accepts, and applies such a drop
	 * through it.
	 *
	 * @param dropTarget
	 *        What dropped objects are done with, or {@code null} to accept no drop again.
	 */
	public void setDropTarget(DropTarget dropTarget) {
		Object update = beginUpdate();
		try {
			_dropTarget = dropTarget;
			putState(DROP_ACCEPTS,
				dropTarget == null ? List.of() : List.copyOf(dropTarget.acceptedTypes()));
			putState(DROP_ON_ROWS, Boolean.valueOf(dropTarget != null && dropTarget.dropOnRows()));
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * The currently selected {@link Row#key() row keys}, in selection order.
	 */
	public Set<Object> getSelectedKeys() {
		return Collections.unmodifiableSet(_selectedKeys);
	}

	/**
	 * Drops the cached cell controls of the given rows and re-renders the current viewport, so
	 * those rows' cells are rebuilt on the next write (e.g. after a row's editability changed).
	 *
	 * @param rowKeys
	 *        The {@link Row#key() keys} of the rows whose cells are stale.
	 */
	public void invalidateRowCells(Collection<Object> rowKeys) {
		boolean changed = false;
		for (Object key : rowKeys) {
			Map<String, ReactControl> cells = _cellCache.remove(key);
			if (cells != null) {
				cells.values().forEach(ReactControl::cleanupTree);
				changed = true;
			}
		}
		if (changed) {
			updateViewport(_viewportStart, _viewportCount);
		}
	}

	/**
	 * Selects exactly the row with the given {@link Row#key() row key} (clearing any other selection),
	 * or clears the selection when {@code key} is {@code null} or matches no current row. Pushes the
	 * change to the client, scrolls the row into view and notifies the
	 * {@link #addSelectionListener(SelectionListener) selection listeners}.
	 *
	 * @param key
	 *        The row key to select, or {@code null} to clear.
	 */
	public void selectRow(Object key) {
		_selectedKeys.clear();
		_cursorIndex = -1;
		_selectionAnchor = -1;
		if (key != null) {
			List<Row<R>> rows = _view.rows(0, _view.rowCount());
			for (int i = 0; i < rows.size(); i++) {
				if (rows.get(i).key().equals(key)) {
					_selectedKeys.add(rows.get(i).key());
					_cursorIndex = i;
					_selectionAnchor = i;
					break;
				}
			}
		}
		pushSelection();
		// Scroll the selected row into view when it lies outside the current viewport (e.g. a row
		// selected programmatically after a create), centering it; keep the viewport otherwise.
		int start = _viewportStart;
		if (_cursorIndex >= 0 && (_cursorIndex < _viewportStart || _cursorIndex >= _viewportStart + _viewportCount)) {
			start = Math.max(0, _cursorIndex - _viewportCount / 2);
		}
		updateViewport(start, _viewportCount);
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
		refreshFilterBar();
		putState(TOTAL_ROW_COUNT, Integer.valueOf(_view.rowCount()));
		updateViewport(_viewportStart, _viewportCount);
	}

	private void refreshColumns() {
		Resources resources = Resources.getInstance();
		// Grouping buckets rows by a column's value, which is meaningful for the columns the user
		// may choose at all - an action column carries the row itself, and would yield one group
		// per row.
		Set<String> groupable = new HashSet<>();
		for (ColumnOption option : _view.columnOptions()) {
			groupable.add(option.name());
		}
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
			columnState.put(COLUMN_FILTERABLE, Boolean.valueOf(column.filterable()));
			columnState.put(COLUMN_FILTER_ACTIVE, Boolean.valueOf(isFilterActive(column.name())));
			columnState.put(COLUMN_GROUPABLE, Boolean.valueOf(groupable.contains(column.name())));
			columnState.put(COLUMN_PINNED_END, Boolean.valueOf(column.pinnedEnd()));
			if (column.cssClass() != null) {
				columnState.put(COLUMN_CSS_CLASS, column.cssClass());
			}
			columns.add(columnState);
		}
		putState(COLUMNS, columns);
		putState(FROZEN_COLUMN_COUNT, Integer.valueOf(_view.frozenColumnCount()));
	}

	private boolean isFilterActive(String column) {
		FilterState state = _view.state().getFilters().get(column);
		return state != null && !state.isEmpty();
	}

	/**
	 * Pushes what the filter bar displays: the offered {@link NamedFilter}s, which of them the
	 * table's criteria currently are, the search term, and whether saving a filter is offered.
	 *
	 * <p>
	 * Called from {@link #buildFullState()}, so every command that changes the rows refreshes the
	 * bar as well: which named filter is active is derived from the live criteria, hence a column
	 * filter set in the per-column dialog ends the match just like applying another named filter
	 * does.
	 * </p>
	 */
	private void refreshFilterBar() {
		if (!_filterBar) {
			return;
		}
		Object update = beginUpdate();
		try {
			Resources resources = Resources.getInstance();
			List<Map<String, Object>> filters = new ArrayList<>();
			for (NamedFilter filter : _view.namedFilters()) {
				Map<String, Object> filterState = new LinkedHashMap<>();
				filterState.put(NAMED_FILTER_ID, filter.id());
				filterState.put(NAMED_FILTER_LABEL, label(resources, filter.label()));
				filterState.put(NAMED_FILTER_DELETABLE,
					Boolean.valueOf(filter.origin() == NamedFilter.Origin.SAVED));
				filters.add(filterState);
			}
			putState(NAMED_FILTERS, filters);

			NamedFilter active = _view.activeNamedFilter();
			putState(ACTIVE_NAMED_FILTER, active == null ? NOTHING : active.id());

			TextFilterState search = _view.state().getSearch();
			putState(SEARCH, search == null ? NOTHING : search.pattern());
			putState(FILTER_SAVING, Boolean.valueOf(_view.savesNamedFilters()));
		} finally {
			commitUpdate(update);
		}
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
			rowState.put(ROW_ID, ROW_ID_PREFIX + index);
			rowState.put(ROW_INDEX, Integer.valueOf(index));
			rowState.put(ROW_SELECTED, Boolean.valueOf(_selectedKeys.contains(row.key())));
			if (treeMode()) {
				rowState.put(TREE_DEPTH, Integer.valueOf(row.depth()));
				rowState.put(TREE_EXPANDABLE, Boolean.valueOf(row.expandable()));
				if (row.expandable()) {
					rowState.put(TREE_EXPANDED, Boolean.valueOf(row.expanded()));
				}
			}
			if (row.kind() == RowKind.GROUP_HEADER) {
				rowState.put(ROW_GROUP_COUNT, Integer.valueOf(row.group().size()));
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
	public Map<String, Object> scriptingScalarState() {
		Map<String, Object> result = super.scriptingScalarState();
		result.put("rows", scriptingRows());
		return result;
	}

	private List<Map<String, Object>> scriptingRows() {
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
			if (row.kind() == RowKind.GROUP_HEADER) {
				rowState.put(ROW_GROUP_COUNT, Integer.valueOf(row.group().size()));
			} else {
				Object key = ScriptingModelKey.toKey(null, row.data());
				if (key != null) {
					rowState.put(ARG_KEY, key);
				}
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

	/**
	 * Also disposes the cells built for rows outside the current viewport: only the rendered rows are
	 * part of the state, the others are only reachable through the cache.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
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
	@ReactCommandHandler(CMD_OPEN_FILTER)
	void handleOpenFilter(OpenFilterArguments args) {
		String column = args.getColumn();
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
				ReactControl input = fieldControl(context, field);
				ReactFormFieldChromeControl chrome = form.addField(label(resources, field.label()), input);
				if (input instanceof ReactCheckboxControl) {
					// A checkbox reads as its own statement ("Yes", "case sensitive"), so the label
					// belongs next to the box - above it, the two look like unrelated lines.
					chrome.setLabelPosition(LabelPosition.AFTER);
				}
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

	// -- Column selection dialog --

	/**
	 * Opens the column selection: which columns the table displays, in which order, and which of
	 * them the rows are grouped by.
	 *
	 * <p>
	 * The dialog edits a working copy in a {@link ReactColumnSelectControl} and applies it in one
	 * step on {@code apply}, so a cancelled dialog leaves the table untouched and an accepted one
	 * persists a single arrangement rather than every intermediate one.
	 * </p>
	 */
	@ReactCommandHandler(CMD_OPEN_COLUMN_SELECT)
	void handleOpenColumnSelect() {
		if (!_columnSelect) {
			return;
		}
		ReactContext context = getReactContext();
		DialogManager dialogs = context.getDialogManager();
		if (dialogs == null) {
			return;
		}
		Resources resources = Resources.getInstance();

		ReactColumnSelectControl selection =
			new ReactColumnSelectControl(context, _view.columnOptions(), getGroupedColumn());
		// Wider than the filter dialog: three actions, one of them a spelled-out "show all columns".
		ReactWindowControl window = new ReactWindowControl(context,
			resources.getString(I18NConstants.JS_TABLE_COLUMNS), DisplayDimension.px(460),
			() -> dialogs.closeTopDialog(DialogResult.cancelled()));
		window.setChild(selection);
		ReactButtonControl applyButton = MessageButtons.ok(context, ctx -> {
			setGroupedColumn(selection.groupedColumn());
			applyColumns(selection.visibleColumns());
			dialogs.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		applyButton.markAsDefault();
		window.setActions(List.of(
			new ReactButtonControl(context, resources.getString(I18NConstants.TABLE_COLUMNS_RESET), ctx -> {
				// Resetting restores the table as it is defined, which includes showing its rows
				// ungrouped again.
				setGroupedColumn(null);
				applyColumns(_view.defaultColumnOrder());
				dialogs.closeTopDialog(DialogResult.ok(null));
				return HandlerResult.DEFAULT_RESULT;
			}),
			MessageButtons.cancel(context, ctx -> {
				dialogs.closeTopDialog(DialogResult.cancelled());
				return HandlerResult.DEFAULT_RESULT;
			}),
			applyButton));
		dialogs.openDialog(true, window, result -> {
			// Reset / Apply already acted; Cancel discards.
		});
	}

	/**
	 * Applies a new set of displayed columns and re-renders: the cell controls of the buffered rows
	 * belong to the previous columns, so they are rebuilt for the new ones.
	 *
	 * <p>
	 * Columns and rows must reach the client as one patch: a client that already knows a newly shown
	 * column but still holds the rows of the previous column set would render a cell that has no
	 * control yet.
	 * </p>
	 */
	private void applyColumns(List<String> columns) {
		Object update = beginUpdate();
		try {
			_view.setColumnOrder(columns);
			clearCells();
			buildFullState();
		} finally {
			commitUpdate(update);
		}
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
	@ReactCommandHandler(CMD_SCROLL)
	void handleScroll(ScrollArguments args) {
		updateViewport(args.getStart(), args.getCount());
	}

	/**
	 * Handles a sort request (single click replaces, shift-click adds/toggles).
	 */
	@ReactCommandHandler(CMD_SORT)
	void handleSort(SortArguments args) {
		String column = args.getColumn();
		boolean ascending = !SORT_DESC.equals(args.getDirection());
		boolean add = SORT_MODE_ADD.equals(args.getMode());

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
	 * Groups the rows by one column, or shows them ungrouped again.
	 *
	 * <p>
	 * The grouping is part of what the user personalizes about the table, so it is persisted under
	 * the table's identity exactly like the sort order is.
	 * </p>
	 *
	 * @param args
	 *        The {@link GroupArguments#getColumn() column} to group by, empty for no grouping.
	 */
	@ReactCommandHandler(CMD_GROUP)
	void handleGroup(GroupArguments args) {
		setGroupedColumn(args.getColumn());
	}

	/**
	 * Groups the rows by the given column, or shows them ungrouped when it is {@code null} or
	 * empty.
	 *
	 * @param column
	 *        The name of the column to group by.
	 */
	public void setGroupedColumn(String column) {
		GroupSpec grouping = StringServices.isEmpty(column) ? GroupSpec.NONE : new GroupSpec(List.of(column));
		if (grouping.columns().equals(_view.state().getGrouping().columns())) {
			return;
		}
		_view.group(grouping);
		Object update = beginUpdate();
		try {
			// A grouping rearranges the rows, it does not replace them: the selected rows are the
			// same objects under the same keys, at other positions. Only the cursor and the range
			// anchor, which are positions, have to be found again.
			relocateSelection();
			pushGrouping();
			pushSelection();
			rebuildAfterRowChange();
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Re-derives the cursor and the range anchor from the selected rows after the rows were
	 * rearranged, and gives up the selection of a row that is no longer among them.
	 *
	 * <p>
	 * A value the table can still display stays selected - it is the selection whoever wrote it
	 * made, and a rearrangement is no reason to drop it. To be called after a rearrangement that
	 * leaves every row displayed (a change of the grouping), where a key that is not among the rows
	 * is one the table cannot display any more.
	 * </p>
	 */
	private void relocateSelection() {
		if (_selectedKeys.isEmpty()) {
			_cursorIndex = -1;
			_selectionAnchor = -1;
			return;
		}
		Set<Object> displayed = new LinkedHashSet<>();
		int cursor = -1;
		List<Row<R>> rows = _view.rows(0, _view.rowCount());
		for (int n = 0; n < rows.size(); n++) {
			Row<R> row = rows.get(n);
			if (row.kind() == RowKind.DATA && _selectedKeys.contains(row.key())) {
				displayed.add(row.key());
				if (cursor < 0) {
					cursor = n;
				}
			}
		}
		_selectedKeys.retainAll(displayed);
		_cursorIndex = cursor;
		_selectionAnchor = cursor;
	}

	/** The name of the column the rows are grouped by, {@code null} when they are not grouped. */
	public String getGroupedColumn() {
		List<String> columns = _view.state().getGrouping().columns();
		return columns.isEmpty() ? null : columns.get(0);
	}

	/**
	 * Whether the client renders the tree/group affordances: the indent of the first column and the
	 * expansion toggles. A grouped table needs them for its group headers, whatever it was built
	 * as.
	 */
	private boolean treeMode() {
		return _treeMode || getGroupedColumn() != null;
	}

	private void pushGrouping() {
		String grouped = getGroupedColumn();
		putState(GROUPING, grouped == null ? NOTHING : grouped);
		putState(TREE_MODE, Boolean.valueOf(treeMode()));
	}

	/**
	 * Handles a row selection (single / ctrl-toggle / shift-range).
	 */
	@ReactCommandHandler(CMD_SELECT)
	void handleSelect(SelectRowArguments args) {
		int rowIndex = args.getRowIndex();
		int total = _view.rowCount();
		if (rowIndex < 0 || rowIndex >= total) {
			return;
		}
		boolean ctrlKey = args.isCtrlKey();
		boolean shiftKey = args.isShiftKey();
		Row<R> clicked = rowAt(rowIndex);
		if (clicked != null && clicked.kind() != RowKind.DATA) {
			// A group header stands for no object: the gesture that would select it collapses or
			// expands the group instead, and the selection stays what it was.
			_cursorIndex = rowIndex;
			toggleExpansion(clicked);
			return;
		}
		Object key = keyAt(rowIndex);
		_cursorIndex = rowIndex;

		if (MODE_MULTI.equals(_selectionMode)) {
			if (shiftKey && _selectionAnchor >= 0) {
				int from = Math.min(_selectionAnchor, rowIndex);
				int to = Math.max(_selectionAnchor, rowIndex);
				for (Row<R> row : _view.rows(from, to + 1)) {
					if (row.kind() == RowKind.DATA) {
						_selectedKeys.add(row.key());
					}
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
	 * Selects the single row whose business object is named by the given {@link ScriptingModelKey key} —
	 * the replay-stable counterpart of {@link #handleSelect} by {@link #ARG_ROW_INDEX}, which a
	 * recorded selection is captured as so it survives sorting, filtering and a fresh session.
	 *
	 * @param args
	 *        Carries a {@link SelectByKeyArguments#getKey() key} with a row business identity (the
	 *        same key the agent projection puts on each row).
	 */
	@ReactCommandHandler(CMD_SELECT_BY_KEY)
	HandlerResult handleSelectByKey(SelectByKeyArguments args) {
		ModelName name = args.getKey();
		int rowIndex = rowIndexOf(name);
		if (rowIndex < 0) {
			// Drift contract: a recorded row key that no longer designates a present row is an
			// explicit failure (replay reports success:false), never a silent no-op selection.
			return HandlerResult.error(I18NConstants.ERROR_ROW_KEY_UNRESOLVED__KEY.fill(name));
		}
		selectOnly(rowIndex);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Activates a row: the row becomes the selection, and what
	 * {@link #setActivationHandler(ActivationHandler)} registered runs with the row's business
	 * object.
	 *
	 * <p>
	 * This is what a double-click on the row and {@code Enter} on the cursor row send. An index
	 * outside the current rows activates nothing.
	 * </p>
	 */
	@ReactCommandHandler(CMD_ACTIVATE)
	HandlerResult handleActivate(ActivateRowArguments args) {
		return activateRow(args.getRowIndex());
	}

	/**
	 * Activates the row whose business object is named by the given {@link ScriptingModelKey key} —
	 * the replay-stable counterpart of {@link #handleActivate} by {@link #ARG_ROW_INDEX}, which a
	 * recorded activation is captured as so it survives sorting, filtering and a fresh session.
	 *
	 * @param args
	 *        Carries a {@link ActivateByKeyArguments#getKey() key} with a row business identity (the
	 *        same key the agent projection puts on each row).
	 */
	@ReactCommandHandler(CMD_ACTIVATE_BY_KEY)
	HandlerResult handleActivateByKey(ActivateByKeyArguments args) {
		ModelName name = args.getKey();
		int rowIndex = rowIndexOf(name);
		if (rowIndex < 0) {
			return HandlerResult.error(I18NConstants.ERROR_ROW_KEY_UNRESOLVED__KEY.fill(name));
		}
		return activateRow(rowIndex);
	}

	private HandlerResult activateRow(int rowIndex) {
		Row<R> row = rowAt(rowIndex);
		if (row == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		if (row.kind() != RowKind.DATA) {
			// A group header has nothing to open: activating it collapses or expands the group.
			_cursorIndex = rowIndex;
			toggleExpansion(row);
			return HandlerResult.DEFAULT_RESULT;
		}
		selectOnly(rowIndex);
		ActivationHandler<R> handler = _activationHandler;
		if (handler == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return handler.rowActivated(row.data());
	}

	/**
	 * Makes the row at the given index the sole selection, the cursor and the range anchor, and
	 * pushes the change to the client.
	 */
	private void selectOnly(int rowIndex) {
		Row<R> row = rowAt(rowIndex);
		if (row == null || row.kind() != RowKind.DATA) {
			return;
		}
		Object key = row.key();
		_selectedKeys.clear();
		_selectedKeys.add(key);
		_cursorIndex = rowIndex;
		_selectionAnchor = rowIndex;
		pushSelection();
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Collapses an expanded row and expands a collapsed one, and re-renders what that changed.
	 */
	private void toggleExpansion(Row<R> row) {
		if (!row.expandable()) {
			return;
		}
		_view.setExpanded(row.key(), !row.expanded());
		rebuildAfterRowChange();
	}

	/**
	 * The index of the row whose business object the given {@link ScriptingModelKey key} names, or
	 * {@code -1} when the key resolves to no object or no row displays it.
	 */
	private int rowIndexOf(ModelName name) {
		Object target = name == null ? null : locate(newActionContext(), name);
		if (target == null) {
			return -1;
		}
		List<Row<R>> rows = _view.rows(0, _view.rowCount());
		for (int i = 0; i < rows.size(); i++) {
			if (target.equals(rows.get(i).data())) {
				return i;
			}
		}
		return -1;
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
	 * Records a gesture whose live arguments are session-bound in replay-stable form.
	 *
	 * <p>
	 * A plain (unmodified) row selection — a {@link #CMD_SELECT} by {@link #ARG_ROW_INDEX} — becomes
	 * a {@link #CMD_SELECT_BY_KEY} of the row's business identity, a {@link #CMD_ACTIVATE} becomes a
	 * {@link #CMD_ACTIVATE_BY_KEY} of the same, and a {@link #CMD_DROP} becomes a
	 * {@link #CMD_DROP_OBJECTS} naming the dragged objects and the target row, so the recording
	 * survives sorting and a fresh session. Modifier selections (ctrl/shift range/toggle) are recorded
	 * verbatim — their semantics are index/anchor based.
	 * </p>
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_DROP.equals(command) && arguments != null) {
			RecordedCommand recorded = recordDrop(arguments);
			if (recorded != null) {
				return recorded;
			}
		}
		if (CMD_SELECT.equals(command) && arguments != null
				&& arguments.get(ARG_ROW_INDEX) instanceof Number rowIndex
				&& !Boolean.TRUE.equals(arguments.get(ARG_CTRL_KEY))
				&& !Boolean.TRUE.equals(arguments.get(ARG_SHIFT_KEY))) {
			ModelName key = rowKeyAt(rowIndex.intValue());
			if (key != null) {
				SelectByKeyArguments recorded = TypedConfiguration.newConfigItem(SelectByKeyArguments.class);
				recorded.setName(CMD_SELECT_BY_KEY);
				recorded.setKey(key);
				return new RecordedCommand(recorded);
			}
		}
		if (CMD_ACTIVATE.equals(command) && arguments != null
				&& arguments.get(ARG_ROW_INDEX) instanceof Number rowIndex) {
			ModelName key = rowKeyAt(rowIndex.intValue());
			if (key != null) {
				ActivateByKeyArguments recorded = TypedConfiguration.newConfigItem(ActivateByKeyArguments.class);
				recorded.setName(CMD_ACTIVATE_BY_KEY);
				recorded.setKey(key);
				return new RecordedCommand(recorded);
			}
		}
		return super.recordCommand(command, arguments);
	}

	/** The business identity of the row at the given index, {@code null} when there is none. */
	private ModelName rowKeyAt(int rowIndex) {
		Row<R> row = rowAt(rowIndex);
		return row == null ? null : ScriptingModelKey.name(null, row.data());
	}

	/**
	 * Handles keyboard row navigation (arrow keys, Home/End, PageUp/PageDown) with a unified
	 * cursor/lead model.
	 *
	 * <p>
	 * Arguments: {@link MoveSelectionArguments#getDirection()} ({@link #DIR_UP}, {@link #DIR_DOWN},
	 * {@link #DIR_HOME}, {@link #DIR_END}, {@link #DIR_PAGE_UP}, {@link #DIR_PAGE_DOWN});
	 * {@link MoveSelectionArguments#isExtend()} (Shift: extend the range from the anchor);
	 * {@link MoveSelectionArguments#isMove()} (Ctrl: move the focus cursor without changing the
	 * selection, multi only). The target index is resolved against the full row list, so it is correct
	 * even when the target row is outside the rendered window; the window is then scrolled to include
	 * it.
	 * </p>
	 */
	@ReactCommandHandler(CMD_MOVE_SELECTION)
	void handleMoveSelection(MoveSelectionArguments args) {
		int total = _view.rowCount();
		if (total == 0) {
			return;
		}
		String direction = args.getDirection();
		boolean extend = args.isExtend();
		boolean move = args.isMove();
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
			// Plain move (and the single-selection case): selection follows the cursor - onto a
			// group header, which stands for no object, it follows as an empty selection.
			_selectedKeys.clear();
			Row<R> row = rowAt(target);
			if (key != null && row != null && row.kind() == RowKind.DATA) {
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
	@ReactCommandHandler(CMD_SELECT_ALL)
	void handleSelectAll(SelectAllArguments args) {
		boolean selected = args.isSelected();
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
	@ReactCommandHandler(CMD_COLUMN_RESIZE)
	void handleColumnResize(ColumnResizeArguments args) {
		String column = args.getColumn();
		int width = Math.max(MIN_WIDTH, args.getWidth());
		_view.resizeColumn(column, width);
		refreshColumns();
	}

	/**
	 * Handles a column reorder.
	 */
	@ReactCommandHandler(CMD_COLUMN_REORDER)
	void handleColumnReorder(ColumnReorderArguments args) {
		String column = args.getColumn();
		int targetIndex = args.getTargetIndex();
		_view.moveColumn(column, targetIndex);
		rebuildAfterRowChange();
	}

	/**
	 * Handles a tree/group expand or collapse.
	 */
	@ReactCommandHandler(CMD_EXPAND)
	void handleExpand(ExpandArguments args) {
		int rowIndex = args.getRowIndex();
		boolean expanded = args.isExpanded();
		if (rowIndex < 0 || rowIndex >= _view.rowCount()) {
			return;
		}
		_view.setExpanded(keyAt(rowIndex), expanded);
		rebuildAfterRowChange();
	}

	/**
	 * Filters the table by the criteria of one of the {@link TableView#namedFilters() named
	 * filters} the bar offers.
	 *
	 * <p>
	 * The named filter replaces the whole filter, so what the bar displays as active is what the
	 * table is filtered by - a column the filter does not mention ends up unfiltered.
	 * </p>
	 */
	@ReactCommandHandler(CMD_APPLY_NAMED_FILTER)
	void handleApplyNamedFilter(ApplyNamedFilterArguments args) {
		_view.applyNamedFilter(args.getId());
		rebuildAfterRowChange();
	}

	/**
	 * Unfilters the table: clears every column filter and the search term.
	 *
	 * <p>
	 * This is what clicking the active chip in the filter bar does. While a chip is active, the
	 * table's criteria are exactly that chip's own, so clearing them all clears exactly what the
	 * chip applied - the chip acts as a toggle, and a second click leaves the table showing every
	 * row again.
	 * </p>
	 */
	@ReactCommandHandler(CMD_CLEAR_FILTER)
	void handleClearFilter() {
		for (String column : new ArrayList<>(_view.state().getFilters().keySet())) {
			_view.filter(column, null);
		}
		_view.search(null);
		rebuildAfterRowChange();
	}

	/**
	 * Searches the table's displayed columns for a text, or clears the search when the term is
	 * empty.
	 *
	 * <p>
	 * The bar searches for a plain {@link TextFilterState#contains(String) case-insensitive
	 * substring}; the matching flags of a column's own text filter stay that column's business.
	 * </p>
	 */
	@ReactCommandHandler(CMD_SEARCH)
	void handleSearch(SearchArguments args) {
		String term = args.getTerm();
		_view.search(term == null || term.isEmpty() ? null : TextFilterState.contains(term));
		rebuildAfterRowChange();
	}

	/**
	 * Keeps the table's current criteria as a {@link NamedFilter} of the user's own, under the name
	 * they typed.
	 */
	@ReactCommandHandler(CMD_SAVE_NAMED_FILTER)
	void handleSaveNamedFilter(SaveNamedFilterArguments args) {
		String name = args.getFilterName().trim();
		if (name.isEmpty()) {
			return;
		}
		_view.saveNamedFilter(name);
		refreshFilterBar();
	}

	/**
	 * Deletes one of the filters the user saved. The rows stay as they are: the deleted filter's
	 * criteria are not withdrawn, only its name.
	 */
	@ReactCommandHandler(CMD_DELETE_NAMED_FILTER)
	void handleDeleteNamedFilter(DeleteNamedFilterArguments args) {
		_view.deleteNamedFilter(args.getId());
		refreshFilterBar();
	}

	/**
	 * Handles a change of the frozen column count.
	 */
	@ReactCommandHandler(CMD_SET_FROZEN_COLUMN_COUNT)
	void handleSetFrozenColumnCount(SetFrozenColumnCountArguments args) {
		int count = args.getCount();
		_view.setFrozenColumnCount(Math.max(0, count));
		refreshColumns();
	}

	// -- Drag and drop --

	@Override
	public String dragType() {
		return _dragType;
	}

	@Override
	public List<?> dragObjects(List<String> keys) {
		if (keys == null) {
			return List.of();
		}
		List<Object> result = new ArrayList<>(keys.size());
		for (String key : keys) {
			Row<R> row = rowById(key);
			if (row != null && row.kind() == RowKind.DATA && row.data() != null) {
				result.add(row.data());
			}
		}
		return result;
	}

	@Override
	public List<?> dragSelection() {
		List<Object> result = new ArrayList<>(_selectedKeys.size());
		for (Row<R> row : _view.rows(0, _view.rowCount())) {
			if (row.kind() == RowKind.DATA && row.data() != null && _selectedKeys.contains(row.key())) {
				result.add(row.data());
			}
		}
		return result;
	}

	/**
	 * Applies a drop the client made on this table.
	 *
	 * <p>
	 * The arguments name client-side identities only, so both ends of the gesture are resolved by the
	 * control that owns them: the dragged objects by the {@link DragSourceControl} the
	 * {@link DropArguments#getSource() source id} designates, the target by this table. A drop of a
	 * type the {@link #setDropTarget(DropTarget) drop target} does not accept, from a control that is
	 * no drag source, or naming a row this table no longer displays is refused - the client-side
	 * acceptance check that precedes it narrows the gesture for the user, it does not decide it.
	 * </p>
	 */
	@ReactCommandHandler(CMD_DROP)
	HandlerResult handleDrop(DropArguments args) {
		DropTarget dropTarget = _dropTarget;
		if (dropTarget == null) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		ReactCommandTarget registered = registeredControl(args.getSource());
		if (!(registered instanceof DragSourceControl source) || !(registered instanceof ReactControl sourceControl)) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		String dragType = source.dragType();
		if (dragType == null || !dropTarget.acceptedTypes().contains(dragType)) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}

		Row<R> targetRow = null;
		DropPosition position = DropPosition.NONE;
		if (dropTarget.dropOnRows()) {
			position = DropPosition.fromWire(args.getPosition());
			if (position == null) {
				return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
			}
			String targetKey = args.getTargetKey();
			if (targetKey != null && !targetKey.isEmpty()) {
				targetRow = rowById(targetKey);
				if (targetRow == null) {
					return HandlerResult.error(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(targetKey));
				}
			}
		}

		List<?> objects = args.isSelection() ? source.dragSelection() : source.dragObjects(args.getKeys());
		if (objects.isEmpty()) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(args.getKeys()));
		}

		dropTarget.onDrop(
			new DropEvent(sourceControl, objects, targetRow == null ? null : targetRow.data(), position));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Applies a drop of the objects named by their {@link ScriptingModelKey business identity} - the
	 * replay-stable counterpart of {@link #handleDrop} by client-side keys, which a recorded drop is
	 * captured as so it survives sorting, filtering and a fresh session.
	 *
	 * @param args
	 *        Carries the identities of the dropped objects and of the target row.
	 */
	@ReactCommandHandler(CMD_DROP_OBJECTS)
	HandlerResult handleDropObjects(DropObjectsArguments args) {
		DropTarget dropTarget = _dropTarget;
		if (dropTarget == null) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		DropPosition position = DropPosition.fromWire(args.getPosition());
		if (position == null) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		ActionContext actionContext = newActionContext();

		List<ModelName> unresolved = new ArrayList<>();
		List<Object> objects = new ArrayList<>();
		for (ModelName name : args.getObjects()) {
			Object object = locate(actionContext, name);
			if (object == null) {
				unresolved.add(name);
			} else {
				objects.add(object);
			}
		}
		Object target = null;
		ModelName targetName = args.getTargetObject();
		if (targetName != null) {
			Object object = locate(actionContext, targetName);
			// The target must be a row of this table: a recorded drop that lands somewhere else is a
			// drift, not a drop.
			Row<R> targetRow = object == null ? null : rowFor(object);
			if (targetRow == null) {
				unresolved.add(targetName);
			} else {
				target = targetRow.data();
			}
		}
		// Drift contract: a recorded identity that no longer designates a present object is an
		// explicit failure (replay reports success:false), never a partially applied drop.
		if (!unresolved.isEmpty() || objects.isEmpty()) {
			return HandlerResult.error(I18NConstants.ERROR_DROP_UNRESOLVED__OBJECTS.fill(unresolved));
		}

		dropTarget.onDrop(new DropEvent(null, objects, target,
			dropTarget.dropOnRows() ? position : DropPosition.NONE));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Rewrites a client drop into the replay-stable {@link #CMD_DROP_OBJECTS} form: the live
	 * {@link #CMD_DROP} names the dragged rows by session-bound client keys, the recorded step names
	 * the business objects themselves. {@code null} when an object cannot be named, so the drop is
	 * recorded verbatim rather than as an incomplete set.
	 */
	private RecordedCommand recordDrop(Map<String, Object> arguments) {
		if (!(commandItem(CMD_DROP, arguments) instanceof DropArguments args)) {
			return null;
		}
		if (!(registeredControl(args.getSource()) instanceof DragSourceControl source)) {
			return null;
		}
		List<?> objects = args.isSelection() ? source.dragSelection() : source.dragObjects(args.getKeys());
		if (objects.isEmpty()) {
			return null;
		}
		DropObjectsArguments recorded = TypedConfiguration.newConfigItem(DropObjectsArguments.class);
		recorded.setName(CMD_DROP_OBJECTS);
		for (Object object : objects) {
			ModelName name = ScriptingModelKey.name(null, object);
			if (name == null) {
				return null;
			}
			recorded.getObjects().add(name);
		}
		Row<R> targetRow = rowById(args.getTargetKey());
		if (targetRow != null) {
			ModelName targetName = ScriptingModelKey.name(null, targetRow.data());
			if (targetName == null) {
				return null;
			}
			recorded.setTargetObject(targetName);
		}
		recorded.setPosition(args.getPosition());
		return new RecordedCommand(recorded);
	}

	/**
	 * The control registered in this window under the given id, or {@code null} if none is (or the
	 * id is missing).
	 */
	private ReactCommandTarget registeredControl(String controlId) {
		SSEUpdateQueue queue = getReactContext().getSSEQueue();
		if (controlId == null || queue == null) {
			return null;
		}
		return queue.getControl(controlId);
	}

	/**
	 * An {@link ActionContext} for resolving a {@link ModelName}, or {@code null} if the running
	 * interaction offers no display context to build one from.
	 */
	private ActionContext newActionContext() {
		try {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
			return new ReactActionContext(displayContext, displayContext.asRequest().getSession());
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve a business identity outside an interaction.", ex, this);
			return null;
		}
	}

	/**
	 * The object the given {@link ModelName} designates, or {@code null} if it designates none (or
	 * there is no {@code context} to resolve it in).
	 */
	private Object locate(ActionContext context, ModelName name) {
		if (context == null || name == null) {
			return null;
		}
		try {
			return ModelResolver.locateModel(context, null, name);
		} catch (RuntimeException ex) {
			Logger.warn("Cannot resolve object for key: " + name, ex, this);
			return null;
		}
	}

	private Object keyAt(int rowIndex) {
		Row<R> row = rowAt(rowIndex);
		return row == null ? null : row.key();
	}

	/** The row at the given index, or {@code null} when the index is outside the current rows. */
	private Row<R> rowAt(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= _view.rowCount()) {
			return null;
		}
		List<Row<R>> single = _view.rows(rowIndex, rowIndex + 1);
		return single.isEmpty() ? null : single.get(0);
	}

	/**
	 * The row the given client-side {@link #ROW_ID_PREFIX row id} designates, or {@code null} if it
	 * designates none.
	 */
	private Row<R> rowById(String rowId) {
		return rowAt(rowIndex(rowId));
	}

	/**
	 * The row whose business object is the given one, or {@code null} if the table displays none.
	 */
	private Row<R> rowFor(Object data) {
		for (Row<R> row : _view.rows(0, _view.rowCount())) {
			if (row.kind() == RowKind.DATA && data.equals(row.data())) {
				return row;
			}
		}
		return null;
	}

	/**
	 * The {@link #ROW_INDEX index} the given client-side {@link #ROW_ID_PREFIX row id} carries, or
	 * {@code -1} if the id is malformed.
	 */
	private static int rowIndex(String rowId) {
		if (rowId == null || !rowId.startsWith(ROW_ID_PREFIX)) {
			return -1;
		}
		try {
			return Integer.parseInt(rowId.substring(ROW_ID_PREFIX.length()));
		} catch (NumberFormatException ex) {
			return -1;
		}
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
		if (key == null) {
			return null;
		}
		int separator = key.indexOf('|');
		if (separator < 0) {
			return null;
		}
		Row<R> row = rowById(key.substring(0, separator));
		if (row == null) {
			return null;
		}
		CellContent content = _view.cell(row, key.substring(separator + 1));
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
		for (SelectionListener listener : _selectionListeners) {
			listener.selectionChanged(new LinkedHashSet<>(_selectedKeys));
		}
	}

}
