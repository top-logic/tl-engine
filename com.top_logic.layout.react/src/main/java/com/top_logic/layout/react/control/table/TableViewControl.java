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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMarker;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropPlace;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropRequest;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropTreeNavigation;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.dnd.TreeDropPlace;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.selection.SelectByKeysArguments;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.table.CellContent;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowHierarchy;
import com.top_logic.table.RowKind;
import com.top_logic.table.RowSource;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

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
 * {@link com.top_logic.layout.react.control.dnd}: {@link #setDragSource(String, Predicate)} makes
 * the rows draggable as drags of a kind, {@link #setDropTarget(DropTarget)} accepts a drop of the
 * kinds its target accepts and applies it. A drag names client-side row keys only, and each control
 * resolves the keys it owns, so the two ends of a drag between two tables need know nothing of each
 * other. While a drag hovers the table, the client probes each row and {@link DropZone zone} it
 * passes for the {@link DropTarget#check(DropRequest) verdict} of a drop there, draws the
 * {@link DropMarker marker} of an accepted drop and shows a refusal with its reason.
 * </p>
 *
 * <p>
 * The table resolves a row and a zone into the {@link DropLocation} of each {@link DropMode} as a
 * flat list: a drop {@link DropMode#ONTO onto} a row in any of its zones is made onto that row; an
 * {@link DropMode#ORDERED insertion} in the upper part of a row inserts before it, in the lower part
 * before the next data row (at the end below the last one), and beside the rows at the end; a drop
 * on the {@link DropMode#CONTROL table as a whole} is one wherever it is made.
 * </p>
 *
 * <p>
 * A table whose rows form a tree - a {@link TableView#hierarchy() hierarchy} - resolves them by the
 * rules of a {@link TreeDropPlace} instead, as a tree does: an insertion is made under a parent row
 * before one of its children, and the client splits a row as a tree node (see
 * {@link #DROP_TREE_ZONES}).
 * </p>
 *
 * <p>
 * <b>Placement:</b> this control virtualizes - it renders only the row window that fits its
 * scroll viewport and scrolls internally - when it is given a container with a <em>definite
 * (bounded) height</em>. Its root fills its parent ({@code height: 100%}); if every ancestor up to
 * a bounded box is content-sized, the control expands to the full height of its rows and the
 * <em>surrounding</em> scroller (e.g. the tab content) scrolls them instead: the client then reports
 * the part of the rows visible in that scroller as its viewport, so the rows still page in as it
 * scrolls. Each window is limited to {@link #MAX_VIEWPORT_ROWS} rows plus the prefetched rows
 * around them. Inside the {@code com.top_logic.layout.view} layer, place it in a {@code <split-panel>}/
 * {@code <pane>} or a fill panel ({@code <panel fill="true">}) - the containers between such a
 * filling control and the next bounded box grow with it, so the chain holds however deeply it is
 * nested; standalone (e.g. in a JSP-rendered component) wrap it in a fixed- or flex-bounded
 * container as the React-table demo does.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 */
public class TableViewControl<R> extends ReactControl implements DragSourceControl {

	/**
	 * Notified when the set of selected row keys changes.
	 */
	public interface SelectionListener {

		/**
		 * Called after the selection changed, with the current selected {@link Row#key()
		 * keys}.
		 *
		 * <p>
		 * A listener refuses the change by throwing a {@link ChannelVetoException} - the unsaved
		 * changes of a form the selection would replace block it. The table then restores the
		 * selection it displayed before the refused change, both in its own state and in the
		 * {@link TableView}, and rethrows, so whoever resolves the unsaved changes retries the
		 * whole selection change. A listener notified before the refusing one keeps what it was
		 * told; the retry tells it the same value again.
		 * </p>
		 */
		void selectionChanged(Set<Object> selectedKeys);
	}

	/**
	 * Notified when a row is activated: opened by a double-click, by {@code Enter} while it carries
	 * the keyboard cursor, or by a control acting on behalf of the row through
	 * {@link TableViewControl#activate(Object)}.
	 *
	 * @param <T>
	 *        The row business object type.
	 */
	@FunctionalInterface
	public interface ActivationHandler<T> {

		/**
		 * Called after the activated row became the table's selection - whether the row was
		 * activated by the gesture or through {@link TableViewControl#activate(Object)}.
		 *
		 * @param row
		 *        The business object of the activated row.
		 * @throws TopLogicException
		 *         If the reaction fails in a way the user can act on; reported to the client (and
		 *         to a scripted replay).
		 */
		void rowActivated(T row);
	}

	private static final String COLUMNS = "columns";

	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	private static final String VIEWPORT_START = "viewportStart";

	/**
	 * The first row of the window the client body showed last, so that a body mounted again (after a
	 * reload of the page) scrolls back to it. {@link #VIEWPORT_START} is the start of the rows sent,
	 * which lies before it by the prefetched rows.
	 */
	private static final String SCROLL_START = "scrollStart";

	/**
	 * State key of the rows the client renders: the window of the rows around the displayed ones,
	 * starting at {@link #VIEWPORT_START}, each with its cell controls.
	 *
	 * <p>
	 * Pushed when the rows themselves change - the window is scrolled, the data, the filter, the
	 * sorting or the grouping changes, or a row's cells are built again. The selection is carried
	 * by {@link #SELECTED_ROWS}, so a selection change pushes no rows.
	 * </p>
	 */
	private static final String ROWS = "rows";

	/**
	 * State key of the selected rows among the {@link #ROWS rows sent}: the list of their
	 * {@link #ROW_INDEX indexes}, in the order of the rows.
	 *
	 * <p>
	 * A selected row outside the window of the rows sent is not listed; the key is pushed again
	 * whenever the selection or that window changes. Pushed together with {@link #SELECTED_COUNT}
	 * and {@link #CURSOR_INDEX}, each only when its value changed.
	 * </p>
	 */
	private static final String SELECTED_ROWS = "selectedRows";

	private static final String SELECTION_MODE = "selectionMode";

	private static final String SELECTED_COUNT = "selectedCount";

	/**
	 * State key of what the select-all checkbox of the header shows: {@link #SELECT_ALL_NONE},
	 * {@link #SELECT_ALL_SOME} or {@link #SELECT_ALL_ALL}.
	 *
	 * <p>
	 * Decided by how many of the rows {@link #CMD_SELECT_ALL} acts on are selected - the
	 * {@link TableView#matchingKeys() matching rows}, the members of a collapsed group included -
	 * not by the size of the selection: a selected row the filter hides takes no part in it, and a
	 * group header is no row.
	 * </p>
	 */
	private static final String SELECT_ALL_STATE = "selectAllState";

	/** {@link #SELECT_ALL_STATE} while none of the matching rows is selected. */
	private static final String SELECT_ALL_NONE = "none";

	/** {@link #SELECT_ALL_STATE} while some, but not all of the matching rows are selected. */
	private static final String SELECT_ALL_SOME = "some";

	/** {@link #SELECT_ALL_STATE} while all of the matching rows, and at least one, are selected. */
	private static final String SELECT_ALL_ALL = "all";

	/**
	 * State key of the text telling how many rows the table has, and how many of them its filter
	 * lets pass; empty while the table shows no row count.
	 */
	private static final String ROW_COUNT = "rowCount";

	/**
	 * State key of the text telling how many rows are selected; empty while the table shows no row
	 * count, selects a single row only, or has nothing selected.
	 */
	private static final String ROW_COUNT_SELECTED = "rowCountSelected";

	/** State key for the keyboard focus/lead row index ({@code -1} when none). */
	private static final String CURSOR_INDEX = "cursorIndex";

	private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";

	private static final String TREE_MODE = "treeMode";

	/**
	 * State key telling the client to split a row into the {@link DropZone zones} of a tree node
	 * rather than those of an item of a flat list: a row whose drops are resolved by the rules of a
	 * {@link TreeDropPlace} has a middle third for an insertion into it.
	 */
	public static final String DROP_TREE_ZONES = "dropTreeZones";

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
	 * This is the identity the client refers to a row by - the key of a dragged or dropped-on row.
	 * It designates a row for as long as the client's row window is the one the server sent, which
	 * is what a gesture on a displayed row rests on anyway.
	 * </p>
	 *
	 * @see #rowIndex(String)
	 */
	private static final String ROW_ID_PREFIX = "row_";

	private static final String ROW_INDEX = "index";

	/**
	 * Entry of a row in the agent projection of the rows (see {@link #scriptingScalarState()})
	 * telling whether the row is selected.
	 */
	private static final String ROW_SELECTED = "selected";

	/**
	 * Row entry telling whether the row may be dragged, present while the rows are
	 * {@link #setDragSource(String, Predicate) draggable} at all.
	 */
	private static final String ROW_DRAGGABLE = "draggable";

	private static final String ROW_CELLS = "cells";

	/**
	 * Per-row state key holding the tooltip of the row's cells, by column name.
	 *
	 * <p>
	 * Holds an entry for the cells whose {@link CellContent#tooltip()} says something the cell does
	 * not display; the client shows that text where the cell is. A row whose cells all display what
	 * they have to say is sent without the key - the client then offers the cell's own text where it
	 * does not fit.
	 * </p>
	 */
	private static final String ROW_TOOLTIPS = "tooltips";

	/**
	 * Per-row state key holding the CSS class the client puts on the row's element.
	 *
	 * <p>
	 * Holds the {@link TableView#rowCssClass(Row) row class} of the table view; a row without one
	 * is sent without the key. The class is evaluated with every push of the rows, so it follows
	 * the row object's state.
	 * </p>
	 *
	 * @see #ROW_CELL_CLASSES
	 */
	private static final String ROW_CSS_CLASS = "cssClass";

	/**
	 * Per-row state key holding the CSS class of the row's cells, by column name.
	 *
	 * <p>
	 * Holds an entry for the cells that have a {@link TableView#cellCssClass(Row, String) cell
	 * class}; the client puts it on the cell's element next to the column's own class. A row none
	 * of whose cells has one is sent without the key. The classes are computed together with the
	 * row's {@link #ROW_CELLS cells}, so they follow the row's data whenever its cells are built
	 * again.
	 * </p>
	 *
	 * @see #ROW_CSS_CLASS
	 */
	private static final String ROW_CELL_CLASSES = "cellClasses";

	private static final String TREE_DEPTH = "treeDepth";

	private static final String TREE_EXPANDABLE = "expandable";

	private static final String TREE_EXPANDED = "expanded";

	/**
	 * Per-row state key holding the number of rows in a group, present exactly on the group header
	 * rows a {@link #GROUPING} introduces.
	 */
	private static final String ROW_GROUP_COUNT = "groupCount";

	/**
	 * The type class of the design system the text of a group header row is drawn in: the header
	 * is strong where its text is set, since the text brings its own type class, which a class on
	 * the row could not override.
	 */
	private static final String GROUP_HEADER_TYPE_CLASS = "tl-type-body-strong";

	private static final int PREFETCH_ROWS = 20;

	/** Separates what the row count tells from the filter it names. */
	private static final String FOOTER_SEPARATOR = " \u00b7 ";

	/**
	 * The largest number of rows a viewport shows, whatever the client reports.
	 *
	 * <p>
	 * The client reports how many rows it shows, and every later update of the rows sends that many
	 * again. A client measuring a table body that is as high as all its rows would otherwise make
	 * every update send thousands of rows. No viewport on a screen shows more rows than this.
	 * </p>
	 */
	public static final int MAX_VIEWPORT_ROWS = 200;

	private static final int MIN_WIDTH = 50;

	/** State key telling the client whether to offer the column selection. */
	private static final String COLUMN_SELECT = "columnSelect";

	/**
	 * State key of the column selection the view popover shows on its page "Columns", a
	 * {@link ReactColumnSelectControl}; {@code null} while the page is not open.
	 */
	private static final String COLUMN_SELECTION = "columnSelection";

	/** State key of the number of the columns the user can choose that the table displays. */
	private static final String COLUMNS_SHOWN = "columnsShown";

	/** State key of the number of the columns the user can choose, displayed or not. */
	private static final String COLUMNS_TOTAL = "columnsTotal";

	/**
	 * State key telling the client whether the table {@link TableView#isPersonalized() keeps its
	 * state}.
	 */
	private static final String VIEW_OPTIONS = "viewOptions";

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
	 * Per-column state key telling whether the user may move the column in the table header: not the one pinned to
	 * the end, nor one the column selection does not offer.
	 */
	private static final String COLUMN_REORDERABLE = "reorderable";

	/**
	 * Per-column state key holding the {@link ColumnView#cssClass() CSS class} the client puts on
	 * every cell of that column, its heading included. Absent for a column declaring none.
	 */
	private static final String COLUMN_CSS_CLASS = "cssClass";

	/**
	 * Per-column state key holding the description of the column's {@link ColumnView#label() label},
	 * which the client offers on the heading. Absent for a column whose label has none.
	 */
	private static final String COLUMN_TOOLTIP = "tooltip";

	/** State key telling the client whether to display the filter bar. */
	private static final String FILTER_BAR = "filterBar";

	/**
	 * State key of the filter popover open at the funnel of a column, {@code null} while none is:
	 * the name of the column ({@link #FILTER_POPOVER_COLUMN}) and the content of the popover
	 * ({@link #FILTER_POPOVER_CONTENT}). One popover at a time.
	 */
	private static final String FILTER_POPOVER = "filterPopover";

	/** Entry key of {@link #FILTER_POPOVER}: the name of the column whose filter the popover edits. */
	private static final String FILTER_POPOVER_COLUMN = "column";

	/** Entry key of {@link #FILTER_POPOVER}: the content of the popover, a control of its own. */
	private static final String FILTER_POPOVER_CONTENT = "content";

	/**
	 * The pause after the last keystroke in a field of a filter popover before what is typed filters
	 * the table, in milliseconds. Enter filters at once.
	 *
	 * <p>
	 * Longer than the pause of a plain field: every value that reaches the server filters all rows
	 * of the table, so the table waits until the user stops typing rather than following each
	 * keystroke.
	 * </p>
	 */
	public static final long FILTER_TYPING_PAUSE_MS = 400;

	/** State key holding the filters the table offers under a name, one map per filter. */
	private static final String NAMED_FILTERS = "namedFilters";

	/** Entry key of a {@link #NAMED_FILTERS} filter's {@link NamedFilter#id() identifier}. */
	private static final String NAMED_FILTER_ID = "id";

	/** Entry key of a {@link #NAMED_FILTERS} filter's displayed name. */
	private static final String NAMED_FILTER_LABEL = "label";

	/** Entry key telling whether a {@link #NAMED_FILTERS} filter may be deleted. */
	private static final String NAMED_FILTER_DELETABLE = "deletable";

	/**
	 * Entry key of a {@link #NAMED_FILTERS} filter's {@link NamedFilter#cssClass() CSS class}, put
	 * on its chip in either state. Present only for a filter that has one.
	 */
	private static final String NAMED_FILTER_CSS_CLASS = "cssClass";

	/**
	 * Entry key of the number of data rows a {@link NamedFilter#counted() counted}
	 * {@link #NAMED_FILTERS} filter selects on its own, see {@link TableView#matchCount(NamedFilter)}.
	 * Present only for a counted filter of a table whose rows can be counted.
	 */
	private static final String NAMED_FILTER_COUNT = "count";

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
	private static final String DRAG_ENABLED = DropSupport.DRAG_ENABLED;

	/** @see DropSupport#DRAG_KIND */
	private static final String DRAG_KIND = DropSupport.DRAG_KIND;

	/** @see DropSupport#DROP_VERDICTS */
	private static final String DROP_VERDICTS = DropSupport.DROP_VERDICTS;

	// Command names.
	private static final String CMD_OPEN_FILTER = "openFilter";

	/** The command the client sends when the filter popover closes: on Escape, on a press outside. */
	private static final String CMD_CLOSE_FILTER = "closeFilter";

	/** The command the client sends when the view popover shows its page of the column selection. */
	private static final String CMD_OPEN_COLUMNS = "openColumns";

	/** The command the client sends when that page is left, and when the popover closes while it shows it. */
	private static final String CMD_CLOSE_COLUMNS = "closeColumns";

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

	/** @see #handleSelectByKey(SelectByKeyArguments) */
	public static final String CMD_SELECT_BY_KEY = "selectByKey";

	/** @see #handleSelectByKeys(SelectByKeysArguments) */
	public static final String CMD_SELECT_BY_KEYS = "selectByKeys";

	/**
	 * The command the client sends when the user opens a row: a double-click, or {@code Enter} on
	 * the row carrying the keyboard cursor.
	 *
	 * @see ActivateRowArguments
	 */
	public static final String CMD_ACTIVATE = "activate";

	/** @see #handleActivateByKey(ActivateByKeyArguments) */
	public static final String CMD_ACTIVATE_BY_KEY = "activateByKey";

	/** The command the client sends for the keyboard navigation of the rows. */
	public static final String CMD_MOVE_SELECTION = "moveSelection";

	/** The command the client sends for the header select-all checkbox and {@code Ctrl+A}. */
	public static final String CMD_SELECT_ALL = "selectAll";

	/**
	 * The command the client sends to change the width of a column.
	 *
	 * @see ColumnResizeArguments
	 */
	public static final String CMD_COLUMN_RESIZE = "columnResize";

	/** The command the client sends to expand or collapse a tree node or a group header. */
	public static final String CMD_EXPAND = "expand";

	/** @see #handleExpandByKey(ExpandByKeyArguments) */
	public static final String CMD_EXPAND_BY_KEY = "expandByKey";

	/**
	 * The command the client sends to fix a number of leading columns while the table scrolls
	 * horizontally.
	 *
	 * @see SetFrozenColumnCountArguments
	 */
	public static final String CMD_SET_FROZEN_COLUMN_COUNT = "setFrozenColumnCount";

	/**
	 * The command the client sends to change the view options of the table.
	 *
	 * @see SetViewOptionsArguments
	 */
	public static final String CMD_SET_VIEW_OPTIONS = "setViewOptions";

	/**
	 * The command the client sends to return the table to the arrangement it is defined with: its
	 * columns, their widths, the frozen columns, the sort, the grouping and the view options. The
	 * filters stay.
	 */
	public static final String CMD_RESET_VIEW = "resetView";

	private static final String CMD_APPLY_NAMED_FILTER = "applyNamedFilter";

	private static final String CMD_CLEAR_FILTER = "clearFilter";

	/** The command the client sends to search the displayed columns for a text. */
	public static final String CMD_SEARCH = "search";

	private static final String CMD_SAVE_NAMED_FILTER = "saveNamedFilter";

	private static final String CMD_DELETE_NAMED_FILTER = "deleteNamedFilter";

	private static final String CMD_DROP = DropSupport.CMD_DROP;

	private static final String CMD_DROP_OBJECTS = DropSupport.CMD_DROP_OBJECTS;

	/** @see DropSupport#CMD_DROP_PROBE */
	private static final String CMD_DROP_PROBE = DropSupport.CMD_DROP_PROBE;

	// Command argument names (shared with the typed SelectRowArguments so dispatch, recording and
	// projection agree on the wire keys).
	private static final String ARG_ROW_INDEX = SelectRowArguments.ROW_INDEX;

	private static final String ARG_CTRL_KEY = SelectRowArguments.CTRL_KEY;

	private static final String ARG_SHIFT_KEY = SelectRowArguments.SHIFT_KEY;

	private static final String ARG_KEY = ModelKeyArguments.KEY;

	private static final String ARG_SELECTED = SelectAllArguments.SELECTED;

	private static final String ARG_DIRECTION = MoveSelectionArguments.DIRECTION;

	private static final String ARG_EXTEND = MoveSelectionArguments.EXTEND;

	private static final String ARG_MOVE = MoveSelectionArguments.MOVE;

	private static final String ARG_EXPANDED = ExpandArguments.EXPANDED;

	// Navigation direction argument values.
	private static final String DIR_UP = "up";

	private static final String DIR_DOWN = "down";

	private static final String DIR_HOME = "home";

	private static final String DIR_END = "end";

	private static final String DIR_PAGE_UP = "pageUp";

	private static final String DIR_PAGE_DOWN = "pageDown";

	// Sort direction/accumulation argument values.
	private static final String SORT_ASC = "asc";

	private static final String SORT_DESC = "desc";

	private static final String SORT_MODE_ADD = "add";

	private final TableView<R> _view;

	private final boolean _treeMode;

	private int _viewportStart;

	private int _viewportCount = 50;

	/** The {@link #ROW_INDEX index} of the first of the {@link #ROWS rows sent}. */
	private int _bufferedStart;

	/** The {@link Row#key() keys} of the {@link #ROWS rows sent}, starting at {@link #_bufferedStart}. */
	private List<Object> _bufferedKeys = List.of();

	private final SelectionMode _selectionMode;

	private final Set<Object> _selectedKeys = new LinkedHashSet<>();

	private int _selectionAnchor = -1;

	/** The keyboard focus/lead row index; {@code -1} when no row has keyboard focus. */
	private int _cursorIndex = -1;

	private final List<SelectionListener> _selectionListeners = new CopyOnWriteArrayList<>();

	/**
	 * The selected keys the {@link #addSelectionListener(SelectionListener) selection listeners}
	 * have accepted, restored when one of them refuses a change.
	 */
	private Set<Object> _committedKeys = new LinkedHashSet<>();

	/** The {@link #_cursorIndex} belonging to {@link #_committedKeys}. */
	private int _committedCursor = -1;

	/** The {@link #_selectionAnchor} belonging to {@link #_committedKeys}. */
	private int _committedAnchor = -1;

	/** What a row activation runs, {@code null} for a table whose rows cannot be opened. */
	private ActivationHandler<R> _activationHandler;

	/** What is rendered in the cells of the currently buffered rows, keyed by row key. */
	private final Map<Object, RowCells> _cellCache = new LinkedHashMap<>();

	/** View-supplied custom filter UIs, keyed by column name. */
	private final Map<String, ColumnFilterUI> _filterUIs = new LinkedHashMap<>();

	/** Whether the user may choose which columns are displayed, and in which order. */
	private boolean _columnSelect = true;

	/** Whether the named filters, the search field and saving a filter are displayed. */
	private boolean _filterBar;

	/** Whether the table tells below its rows how many it has. */
	private boolean _rowCount = true;

	/** Whether rows may be dragged at all. */
	private boolean _dragEnabled;

	/** The kind of a drag of rows, {@code null} for a drag without a kind. */
	private String _dragKind;

	/**
	 * Which rows may be dragged while {@link #_dragEnabled} is set, {@code null} when every data row
	 * may be.
	 */
	private Predicate<? super R> _draggable;

	/** The drop protocol shared with every control accepting drops. */
	private final DropSupport _dropSupport = new DropSupport(this);

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
		_selectionMode = view.state().getSelection().mode();

		putState(SELECTION_MODE, _selectionMode.getExternalName());
		putState(COLUMN_SELECT, Boolean.valueOf(_columnSelect));
		putState(FILTER_BAR, Boolean.valueOf(_filterBar));
		putState(VIEW_OPTIONS, Boolean.valueOf(view.isPersonalized()));
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
	 * Makes all data rows draggable as drags of the given kind.
	 *
	 * @param dragKind
	 *        The {@link #dragKind() kind} of a drag, {@code null} for a drag without a kind.
	 *
	 * @see #setDragSource(String, Predicate)
	 */
	public void setDragSource(String dragKind) {
		setDragSource(dragKind, null);
	}

	/**
	 * Makes the rows draggable as drags of the given kind.
	 *
	 * <p>
	 * Dragging a selected row drags the whole {@link #getSelectedKeys() selection}, an unselected row
	 * drags itself. What a receiving {@link DropTarget} gets are the row business objects; the kind
	 * is what it accepts the drop by.
	 * </p>
	 *
	 * <p>
	 * A row the given predicate refuses offers no drag, and a drag of a selection including such a
	 * row is refused as a whole (see {@link DragSourceControl#isDraggable(Object)}). Group headers are
	 * never draggable.
	 * </p>
	 *
	 * @param dragKind
	 *        The {@link #dragKind() kind} of a drag, {@code null} for a drag without a kind.
	 * @param draggable
	 *        Which row business objects may be dragged, {@code null} for all of them. Asked whenever
	 *        rows are rendered; when its answer changes for other reasons, call
	 *        {@link #refreshDragSource()}.
	 *
	 * @see #setDragEnabled(boolean)
	 */
	public void setDragSource(String dragKind, Predicate<? super R> draggable) {
		Object update = beginUpdate();
		try {
			_dragKind = dragKind;
			_draggable = draggable;
			putState(DRAG_KIND, dragKind);
			setDragEnabled(true);
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Switches dragging of rows on or off, keeping the kind and the predicate given to
	 * {@link #setDragSource(String, Predicate)}.
	 */
	public void setDragEnabled(boolean enabled) {
		Object update = beginUpdate();
		try {
			_dragEnabled = enabled;
			putState(DRAG_ENABLED, Boolean.valueOf(enabled));
			updateViewport(_viewportStart, _viewportCount);
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Asks the {@link #setDragSource(String, Predicate) draggable predicate} again for the rendered
	 * rows, after its answer may have changed.
	 */
	public void refreshDragSource() {
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Makes the table accept a drop of the objects the given target accepts, and applies such a drop
	 * through it.
	 *
	 * @param dropTarget
	 *        What dropped objects are done with, or {@code null} to accept no drop again.
	 */
	public void setDropTarget(DropTarget dropTarget) {
		_dropSupport.setTarget(dropTarget);
		refreshDropTarget();
	}

	/**
	 * Announces the {@link DropTarget#acceptedKinds() accepted kinds} and the
	 * {@link DropTarget#dropModes() modes} of the drop operations to the client again, after the
	 * {@link #setDropTarget(DropTarget) drop target's} answers changed.
	 */
	public void refreshDropTarget() {
		Object update = beginUpdate();
		try {
			_dropSupport.targetState().forEach(this::putState);
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
	 * Whether this table lets the user select one row at a time, or any number of them.
	 *
	 * <p>
	 * The mode is what the {@link TableView#state() view state} was seeded with when this control
	 * was created; it does not change over the life of the control.
	 * </p>
	 */
	public SelectionMode getSelectionMode() {
		return _selectionMode;
	}

	/**
	 * Whether more than one row may be selected at a time.
	 */
	private boolean multiSelection() {
		return _selectionMode == SelectionMode.MULTI;
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
			RowCells cells = _cellCache.remove(key);
			if (cells != null) {
				cells.dispose();
				changed = true;
			}
		}
		if (changed) {
			updateViewport(_viewportStart, _viewportCount);
		}
	}

	/**
	 * Pushes the table again after the {@link TableView#namedFilters() named filters} were replaced
	 * on the {@link #getView() view} directly, the rows and their content staying as they are.
	 *
	 * <p>
	 * The filter bar displays the replaced filters - with the numbers of rows they select -, and
	 * an active declared filter is applied again with its replaced criteria, so the displayed rows
	 * are pushed again as well. No cell control is built again, in contrast to
	 * {@link #refreshData()}.
	 * </p>
	 */
	public void refreshFilters() {
		buildFullState();
	}

	/**
	 * Pushes the rows of the current window again, with the cell controls built for them.
	 *
	 * <p>
	 * For a change that leaves the rows and their cells as they are but changes what is evaluated
	 * with every push of the rows, such as the {@link TableView#rowCssClass(Row) row class}: no cell
	 * control is built again, in contrast to {@link #refreshData()}.
	 * </p>
	 */
	public void repaintRows() {
		updateViewport(_viewportStart, _viewportCount);
	}

	/**
	 * Selects exactly the row with the given {@link Row#key() row key} (clearing any other selection),
	 * or clears the selection when {@code key} is {@code null} or names no row of the table's data.
	 * Pushes the change to the client, scrolls the row into view and notifies the
	 * {@link #addSelectionListener(SelectionListener) selection listeners}.
	 *
	 * @param key
	 *        The row key to select, or {@code null} to clear.
	 *
	 * @see #selectRows(Collection)
	 */
	public void selectRow(Object key) {
		selectRows(key == null ? Set.of() : Collections.singleton(key));
	}

	/**
	 * Selects exactly the rows whose {@link Row#key() row key} is among the given ones (clearing any
	 * other selection), leaving out the keys the table's data has no row for. Pushes the change to
	 * the client, scrolls the first displayed selected row into view and notifies the
	 * {@link #addSelectionListener(SelectionListener) selection listeners}.
	 *
	 * <p>
	 * A row of the data that is not displayed - hidden by a filter, or inside a collapsed group or
	 * tree node - is selected as well (see {@link TableView#containedKeys(Collection)}); it shows as
	 * selected once it is displayed again. The keyboard cursor and the range anchor go to the first
	 * displayed selected row, and to no row when none of the selected rows is displayed.
	 * </p>
	 *
	 * <p>
	 * A selection of more than one row is what {@link SelectionMode#MULTI} allows; in
	 * {@link SelectionMode#SINGLE} the caller is responsible for passing at most one key.
	 * </p>
	 *
	 * @param keys
	 *        The row keys to select, empty to clear the selection.
	 */
	public void selectRows(Collection<?> keys) {
		_selectedKeys.clear();
		if (!keys.isEmpty()) {
			_selectedKeys.addAll(_view.containedKeys(keys));
		}
		// The first displayed selected row carries the cursor and is the range anchor, so a
		// keyboard range extension continues from where the selection starts.
		_cursorIndex = firstSelectedRowIndex();
		_selectionAnchor = _cursorIndex;
		pushSelection();
		// Scroll the selected row into view when it lies outside the current viewport (e.g. a row
		// selected programmatically after a create), centering it; keep the viewport otherwise.
		if (_cursorIndex >= 0 && (_cursorIndex < _viewportStart || _cursorIndex >= _viewportStart + _viewportCount)) {
			updateViewport(Math.max(0, _cursorIndex - _viewportCount / 2), _viewportCount);
		}
	}

	/**
	 * Expands the rows with the given {@link Row#key() row keys}, as the user does with their
	 * toggles, and re-renders the rows.
	 *
	 * <p>
	 * A row is expanded in the order the keys are given, so a row below another one becomes
	 * displayed when the keys name the row above it first. A key of a row that is expanded already
	 * changes nothing.
	 * </p>
	 *
	 * @param keys
	 *        The keys of the rows to expand.
	 */
	public void expandRows(Collection<?> keys) {
		if (keys.isEmpty()) {
			return;
		}
		for (Object key : keys) {
			_view.setExpanded(key, true);
		}
		rebuildAfterRowChange();
	}

	/**
	 * Registers a view-supplied custom filter UI for a column, used instead of the built-in filter
	 * editor when the column's funnel is opened.
	 */
	public void setFilterUI(String column, ColumnFilterUI ui) {
		_filterUIs.put(column, ui);
	}

	/**
	 * Whether the table tells below its rows how many rows it has: how many the filter lets pass
	 * of how many there are while it is filtered, and how many are selected in a table selecting
	 * several rows.
	 *
	 * <p>
	 * The rows counted are the business objects, not the displayed lines: the header of a group is
	 * not counted, a row of a collapsed group is. A table whose rows cannot be counted without
	 * loading them shows no count.
	 * </p>
	 */
	public void setRowCount(boolean rowCount) {
		_rowCount = rowCount;
		refreshSelectionCounts();
	}

	/**
	 * The table this control displays.
	 *
	 * <p>
	 * What the table is - its columns, its rows, what it is filtered and sorted by - is the view's
	 * business; this control renders it and turns the user's gestures into calls on it. A caller
	 * reads the view to learn the displayed state and observes it to follow a change of that state,
	 * but changes it through this control, so that what the client displays follows.
	 * </p>
	 */
	public TableView<R> getView() {
		return _view;
	}

	// -- State building --

	private void buildFullState() {
		refreshColumns();
		refreshFilterBar();
		refreshFilterPopover();
		putState(TOTAL_ROW_COUNT, Integer.valueOf(_view.rowCount()));
		updateViewport(_viewportStart, _viewportCount);
		refreshSelectionCounts();
	}

	private void refreshColumns() {
		Resources resources = Resources.getInstance();
		// Grouping buckets rows by a column's value, which is meaningful for the columns the user
		// may choose at all - an action column carries the row itself, and would yield one group
		// per row. A tree is structured by its nodes already and is not grouped at all.
		List<ColumnOption> options = _view.columnOptions();
		Set<String> groupable = new HashSet<>();
		Set<String> arrangeable = new HashSet<>();
		int shown = 0;
		for (ColumnOption option : options) {
			arrangeable.add(option.name());
			if (!_treeMode) {
				groupable.add(option.name());
			}
			if (option.visible()) {
				shown++;
			}
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
			def.setAlignment(column.alignment());
			def.setAggregates(column.aggregates());
			Map<String, Object> columnState = def.toStateMap();
			columnState.put(COLUMN_FILTERABLE, Boolean.valueOf(column.filterable()));
			columnState.put(COLUMN_FILTER_ACTIVE, Boolean.valueOf(isFilterActive(column.name())));
			columnState.put(COLUMN_GROUPABLE, Boolean.valueOf(groupable.contains(column.name())));
			columnState.put(COLUMN_PINNED_END, Boolean.valueOf(column.pinnedEnd()));
			columnState.put(COLUMN_REORDERABLE, Boolean.valueOf(arrangeable.contains(column.name())));
			if (column.cssClass() != null) {
				columnState.put(COLUMN_CSS_CLASS, column.cssClass());
			}
			String tooltip = description(resources, column.label());
			if (!StringServices.isEmpty(tooltip)) {
				columnState.put(COLUMN_TOOLTIP, tooltip);
			}
			columns.add(columnState);
		}
		putState(COLUMNS, columns);
		// What the entry "Columns" of the view popover tells: "n of m".
		putState(COLUMNS_SHOWN, Integer.valueOf(shown));
		putState(COLUMNS_TOTAL, Integer.valueOf(options.size()));
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
	 * filter set in the filter popover of a column ends the match just like applying another named
	 * filter does.
	 * </p>
	 *
	 * <p>
	 * The number of rows a {@link NamedFilter#counted() counted} filter selects is computed here
	 * as well, so it follows every change of the rows - a {@link #refreshData()} after the
	 * displayed objects changed, a {@link #refreshFilters()} after the declared filters were
	 * resolved anew.
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
				if (filter.cssClass() != null) {
					filterState.put(NAMED_FILTER_CSS_CLASS, filter.cssClass());
				}
				if (filter.counted()) {
					int count = _view.matchCount(filter);
					if (count != RowSource.UNKNOWN_COUNT) {
						filterState.put(NAMED_FILTER_COUNT, Integer.valueOf(count));
					}
				}
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

	/**
	 * The description belonging to the given label, {@code null} when the label has none.
	 *
	 * <p>
	 * What a label says about itself over and above its own text, resolved from
	 * {@link ResKey#tooltipOptional()} - the same description a form field offers on its label.
	 * </p>
	 */
	private static String description(Resources resources, ResKey key) {
		return key == null ? null : resources.getString(key.tooltipOptional());
	}

	/**
	 * Pushes the {@link #ROWS rows} of the window around the given viewport, the viewport itself
	 * and the {@link #SELECTED_ROWS selection} within the window.
	 *
	 * <p>
	 * Called where the rows change; the selection is carried by {@link #SELECTED_ROWS}, so a
	 * selection change pushes no rows.
	 * </p>
	 *
	 * @param start
	 *        The index of the first displayed row, a negative one is taken as the first row.
	 * @param count
	 *        The number of displayed rows, limited to the range from one row to
	 *        {@link #MAX_VIEWPORT_ROWS}.
	 */
	private void updateViewport(int start, int count) {
		start = Math.max(0, start);
		count = Math.max(1, Math.min(MAX_VIEWPORT_ROWS, count));
		int total = _view.rowCount();
		int bufferedStart = Math.max(0, start - PREFETCH_ROWS);
		int bufferedEnd = Math.min(total, start + count + PREFETCH_ROWS);

		List<Row<R>> rows = bufferedStart < bufferedEnd ? _view.rows(bufferedStart, bufferedEnd) : List.of();

		List<Object> bufferedKeyList = new ArrayList<>(rows.size());
		for (Row<R> row : rows) {
			bufferedKeyList.add(row.key());
		}
		Set<Object> bufferedKeys = new HashSet<>(bufferedKeyList);
		// Drop cell controls for rows that left the buffer.
		for (Object cached : new ArrayList<>(_cellCache.keySet())) {
			if (!bufferedKeys.contains(cached)) {
				RowCells cells = _cellCache.remove(cached);
				if (cells != null) {
					cells.dispose();
				}
			}
		}

		List<Map<String, Object>> rowStates = new ArrayList<>();
		int index = bufferedStart;
		for (Row<R> row : rows) {
			RowCells cells = _cellCache.computeIfAbsent(row.key(), key -> createCells(row));

			Map<String, Object> rowState = new LinkedHashMap<>();
			rowState.put(ROW_ID, ROW_ID_PREFIX + index);
			rowState.put(ROW_INDEX, Integer.valueOf(index));
			if (_dragEnabled) {
				rowState.put(ROW_DRAGGABLE, Boolean.valueOf(isDraggableRow(row)));
			}
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
			rowState.put(ROW_CELLS, cells.controls());
			if (!cells.tooltips().isEmpty()) {
				rowState.put(ROW_TOOLTIPS, cells.tooltips());
			}
			if (!cells.cssClasses().isEmpty()) {
				rowState.put(ROW_CELL_CLASSES, cells.cssClasses());
			}
			String rowCssClass = _view.rowCssClass(row);
			if (!StringServices.isEmpty(rowCssClass)) {
				rowState.put(ROW_CSS_CLASS, rowCssClass);
			}
			rowStates.add(rowState);
			index++;
		}

		_viewportStart = start;
		_viewportCount = count;
		_bufferedStart = bufferedStart;
		_bufferedKeys = bufferedKeyList;

		putState(VIEWPORT_START, Integer.valueOf(bufferedStart));
		putState(SCROLL_START, Integer.valueOf(start));
		putState(ROWS, rowStates);
		pushSelectionState();
	}

	/**
	 * Pushes the selection as the client displays it - {@link #SELECTED_ROWS},
	 * {@link #SELECTED_COUNT} and {@link #CURSOR_INDEX} - each only when it changed.
	 *
	 * <p>
	 * Called when the selection changes and when the window of the {@link #ROWS rows sent} does.
	 * </p>
	 */
	private void pushSelectionState() {
		List<Integer> selectedRows = new ArrayList<>();
		if (!_selectedKeys.isEmpty()) {
			for (int n = 0, cnt = _bufferedKeys.size(); n < cnt; n++) {
				if (_selectedKeys.contains(_bufferedKeys.get(n))) {
					selectedRows.add(Integer.valueOf(_bufferedStart + n));
				}
			}
		}
		putChangedState(SELECTED_ROWS, selectedRows);
		putChangedState(SELECTED_COUNT, Integer.valueOf(_selectedKeys.size()));
		putChangedState(CURSOR_INDEX, Integer.valueOf(_cursorIndex));
	}

	/**
	 * Pushes what the select-all checkbox shows and the texts of the row count.
	 *
	 * <p>
	 * Called where what it reports on changes, and nowhere else: from {@link #buildFullState()},
	 * which every change of the rows ends in - the data, the filter, the search, a named filter,
	 * the grouping - and from {@link #commitSelection()}, which every accepted change of the
	 * selection ends in. Scrolling changes neither, so it computes nothing here. A value is only
	 * pushed when it changed.
	 * </p>
	 *
	 * @implNote Checking the selection against the filter costs a lookup per selected key; an empty
	 *           selection costs nothing.
	 */
	private void refreshSelectionCounts() {
		int selectedCount = _selectedKeys.size();
		// Only the selection needs checking against the filter, so an empty selection - the common
		// case while scrolling - costs nothing.
		int selectedMatching = selectedCount == 0 ? 0 : _view.matchingKeys(_selectedKeys).size();

		String selectAll;
		if (selectedMatching == 0) {
			selectAll = SELECT_ALL_NONE;
		} else {
			int matching = _view.matchCount();
			if (matching == RowSource.UNKNOWN_COUNT) {
				matching = _view.matchingKeys().size();
			}
			selectAll = selectedMatching == matching ? SELECT_ALL_ALL : SELECT_ALL_SOME;
		}
		putChangedState(SELECT_ALL_STATE, selectAll);

		String rows = NOTHING;
		String selected = NOTHING;
		int matching = _view.matchCount();
		int total = _view.dataCount();
		if (_rowCount && matching != RowSource.UNKNOWN_COUNT && total != RowSource.UNKNOWN_COUNT) {
			Resources resources = Resources.getInstance();
			int activeFilters = activeFilterCount();
			rows = resources.getString(activeFilters > 0
				? I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(matching, total)
				: I18NConstants.TABLE_ROW_COUNT__COUNT.fill(total));
			if (activeFilters > 0) {
				rows += FOOTER_SEPARATOR + activeFilterText(resources, activeFilters);
			}
			if (_selectionMode == SelectionMode.MULTI && selectedCount > 0) {
				int filteredOut = selectedCount - selectedMatching;
				selected = resources.getString(filteredOut == 0
					? I18NConstants.TABLE_ROW_COUNT_SELECTED__COUNT.fill(selectedCount)
					: I18NConstants.TABLE_ROW_COUNT_SELECTED_FILTERED__COUNT_FILTERED.fill(selectedCount,
						filteredOut));
			}
		}
		putChangedState(ROW_COUNT, rows);
		putChangedState(ROW_COUNT_SELECTED, selected);
	}

	/**
	 * The number of criteria narrowing the table's rows: the column filters, and the search when it
	 * has a term - which is what a named filter applies as well.
	 */
	private int activeFilterCount() {
		TableViewState state = _view.state();
		int result = state.getSearch() != null ? 1 : 0;
		for (FilterState filter : state.getFilters().values()) {
			if (filter != null && !filter.isEmpty()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Names what narrows the rows: the {@link TableView#activeNamedFilter() named filter} the
	 * criteria are, or else how many criteria there are.
	 */
	private String activeFilterText(Resources resources, int activeFilters) {
		NamedFilter preset = _view.activeNamedFilter();
		if (preset != null) {
			return resources.getString(
				I18NConstants.TABLE_FILTER_PRESET__NAME.fill(label(resources, preset.label())));
		}
		return resources.getString(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(activeFilters));
	}

	/** Pushes the given state value unless the client already has it. */
	private void putChangedState(String key, Object value) {
		if (!Objects.equals(getState(key), value)) {
			putState(key, value);
		}
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
			rowState.put(ROW_SELECTED, Boolean.valueOf(_selectedKeys.contains(row.key())));
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

	private RowCells createCells(Row<R> row) {
		Map<String, ReactControl> controls = new LinkedHashMap<>();
		Map<String, String> tooltips = new LinkedHashMap<>();
		Map<String, String> cssClasses = new LinkedHashMap<>();
		String typeClass = row.kind() == RowKind.GROUP_HEADER ? GROUP_HEADER_TYPE_CLASS : null;
		for (ColumnView column : _view.columns()) {
			CellContent content = _view.cell(row, column.name());
			ReactControl cell = CellContentReactAdapter.toControl(getReactContext(), content, typeClass);
			controls.put(column.name(), cell);
			String tooltip = content.tooltip();
			if (!StringServices.isEmpty(tooltip)) {
				tooltips.put(column.name(), tooltip);
			}
			String cssClass = _view.cellCssClass(row, column.name());
			if (!StringServices.isEmpty(cssClass)) {
				cssClasses.put(column.name(), cssClass);
			}
		}
		return new RowCells(controls, tooltips, cssClasses);
	}

	/**
	 * What is rendered in the cells of one row: the cell controls, for the cells that say more than
	 * they display their tooltips, and for the cells that are marked their CSS classes.
	 *
	 * <p>
	 * All are built once per row, so the row is rendered once however often its state is pushed.
	 * They are built again when the row's cells are invalidated - by
	 * {@link #invalidateRowCells(Collection)}, {@link #refreshData()}, or the row leaving the
	 * buffered window. The {@link TableView#rowCssClass(Row) row class}, in contrast, is evaluated
	 * with every push.
	 * </p>
	 *
	 * @param controls
	 *        The cell control of each column, by column name.
	 * @param tooltips
	 *        The {@link CellContent#tooltip() tooltip} of a cell that has one, by column name;
	 *        empty when no cell of the row has one.
	 * @param cssClasses
	 *        The {@link TableView#cellCssClass(Row, String) CSS class} of a cell that has one, by
	 *        column name; empty when no cell of the row has one.
	 */
	private record RowCells(Map<String, ReactControl> controls, Map<String, String> tooltips,
			Map<String, String> cssClasses) {

		/** Disposes the cell controls. */
		void dispose() {
			controls().values().forEach(ReactControl::cleanupTree);
		}

	}

	/**
	 * Also disposes the cells built for rows outside the current viewport: only the rendered rows are
	 * part of the state, the others are only reachable through the cache.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		for (RowCells cells : _cellCache.values()) {
			cells.dispose();
		}
		_cellCache.clear();
	}

	// -- Filter popover --

	/**
	 * Opens the filter of a column in the popover at its funnel, in place of the popover of another
	 * column.
	 *
	 * <p>
	 * The popover is not modal, and every change in it filters the table at once:
	 * </p>
	 * <ul>
	 * <li>A filter offering a choice among values is a value list ({@link ReactFilterListControl}):
	 * a click on a value shows or hides its rows.</li>
	 * <li>A filter UI the view registered for the column ({@link #setFilterUI(String, ColumnFilterUI)})
	 * is a form that keeps an explicit apply, since evaluating it may be expensive
	 * ({@link ReactFilterFormControl}).</li>
	 * <li>Any other filter is the form of its {@link FilterEditor}, whose fields filter as their
	 * values change - a text field after the {@link #FILTER_TYPING_PAUSE_MS typing pause} or on
	 * Enter, a choice or a check box at once.</li>
	 * </ul>
	 *
	 * <p>
	 * The client draws the popover at the funnel of the column and shows in it the content it finds
	 * in {@link #FILTER_POPOVER}.
	 * </p>
	 */
	@ReactCommandHandler(CMD_OPEN_FILTER)
	void handleOpenFilter(OpenFilterArguments args) {
		String column = args.getColumn();
		ColumnFilter<?> filter = _view.columnFilter(column);
		if (filter == null) {
			return;
		}
		Map<String, Object> popover = new LinkedHashMap<>();
		popover.put(FILTER_POPOVER_COLUMN, column);
		popover.put(FILTER_POPOVER_CONTENT, filterContent(column, filter));
		showFilterPopover(popover);
	}

	/**
	 * Closes the filter popover. The filter stays as it is: every change has taken effect already.
	 */
	@ReactCommandHandler(CMD_CLOSE_FILTER)
	void handleCloseFilter() {
		showFilterPopover(null);
	}

	/**
	 * Shows the given filter popover, or none, and disposes the content of the one shown before.
	 *
	 * <p>
	 * Replacing the state only forgets the previous content; disposing it is the job of whoever
	 * replaces it, or its fields would keep observing their models.
	 * </p>
	 */
	private void showFilterPopover(Map<String, Object> popover) {
		ReactControl previous = filterPopoverContent();
		putState(FILTER_POPOVER, popover);
		if (previous != null) {
			previous.cleanupTree();
		}
	}

	/** The content of the open filter popover, {@code null} while none is open. */
	private ReactControl filterPopoverContent() {
		return getState(FILTER_POPOVER) instanceof Map<?, ?> popover
			? (ReactControl) popover.get(FILTER_POPOVER_CONTENT)
			: null;
	}

	/** The name of the column whose filter popover is open, {@code null} while none is open. */
	private String filterPopoverColumn() {
		return getState(FILTER_POPOVER) instanceof Map<?, ?> popover
			? (String) popover.get(FILTER_POPOVER_COLUMN)
			: null;
	}

	/**
	 * The content of the filter popover of the given column, see {@link #handleOpenFilter(OpenFilterArguments)}.
	 */
	private ReactControl filterContent(String column, ColumnFilter<?> filter) {
		ReactContext context = getReactContext();
		Consumer<FilterState> apply = state -> applyColumnFilter(column, state);
		ColumnFilterUI customUI = _filterUIs.get(column);
		if (customUI != null) {
			return new ReactFilterFormControl(context, formApply -> customUI.buildForm(context), customUI::read, apply,
				_view.state().getFilters().get(column));
		}
		if (filter.input() instanceof FilterInput.Options options) {
			// Facet counts are value-based, so only meaningful for filters that opt in; a
			// predicate-based options filter (regexp) declines them to avoid misleading zeros.
			MatchCounts counts = filter.countsMatches() ? _view.columnMatchCounts(column) : MatchCounts.NONE;
			ReactFilterListControl list = new ReactFilterListControl(context, options.values(), counts,
				_view.state().getFilters().get(column), apply);
			list.setLabel(columnLabel(column));
			return list;
		}
		return new ReactFilterFormControl(context, formApply -> editorForm(column, filter, formApply), null, apply,
			_view.state().getFilters().get(column));
	}

	/** The displayed label of the column of the given name, the name itself for a column not displayed. */
	private String columnLabel(String column) {
		for (ColumnView view : _view.columns()) {
			if (view.name().equals(column)) {
				return label(Resources.getInstance(), view.label());
			}
		}
		return column;
	}

	/**
	 * The form of the {@link FilterEditor} of a column, seeded from the filter of the column now,
	 * whose every field filters the table as its value changes - through the given function, which
	 * the content of the popover passes in (see {@link ReactFilterFormControl}).
	 *
	 * <p>
	 * Built through the shared form pipeline with the same defaults as a model-bound form
	 * (responsive columns, automatic label position), so the form renders and reflows like every
	 * other form. The input control per field is chosen from the field model itself
	 * ({@link #fieldControl}).
	 * </p>
	 */
	private ReactControl editorForm(String column, ColumnFilter<?> filter, Consumer<FilterState> apply) {
		ReactContext context = getReactContext();
		Resources resources = Resources.getInstance();
		// A value list is no form (see filterContent), so the editor needs no facet counts.
		FilterEditor editor = FilterEditors.create(filter, _view.state().getFilters().get(column), MatchCounts.NONE);
		FieldModelListener onChange = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				apply.accept(editor.read());
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The filter depends on the values alone.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// The filter depends on the values alone.
			}
		};
		ReactFormBuilder form = new ReactFormBuilder(context);
		for (FilterField field : editor.fields()) {
			field.model().addListener(onChange);
			ReactControl input = fieldControl(context, field);
			ReactFormFieldChromeControl chrome = form.addField(label(resources, field.label()), input);
			if (input instanceof ReactCheckboxControl) {
				// A checkbox reads as its own statement ("Yes", "case sensitive"), so the label
				// belongs next to the box - above it, the two look like unrelated lines.
				chrome.setLabelPosition(LabelPosition.AFTER);
			}
		}
		return form.build();
	}

	/**
	 * Filters a column by the given state and shows the result: the rows, the funnel of the column
	 * and the open filter popover (see {@link #refreshFilterPopover()}).
	 *
	 * @param column
	 *        The name of the column.
	 * @param state
	 *        The filter of the column, {@code null} or empty for none.
	 */
	private void applyColumnFilter(String column, FilterState state) {
		Object update = beginUpdate();
		try {
			_view.filter(column, state);
			rebuildAfterRowChange();
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * Shows in the open filter popover what is filtered now: the rows were rebuilt, so the filter of
	 * its column may have changed elsewhere - the filter bar clears every filter, a named filter
	 * replaces them -, and the other criteria or the data may have changed the counts of a value
	 * list.
	 */
	private void refreshFilterPopover() {
		String column = filterPopoverColumn();
		if (column == null) {
			return;
		}
		ReactControl content = filterPopoverContent();
		if (content instanceof ReactFilterListControl list) {
			ColumnFilter<?> filter = _view.columnFilter(column);
			MatchCounts counts =
				filter != null && filter.countsMatches() ? _view.columnMatchCounts(column) : MatchCounts.NONE;
			list.refresh(counts, _view.state().getFilters().get(column));
		} else if (content instanceof ReactFilterFormControl form) {
			form.refresh(_view.state().getFilters().get(column));
		}
	}

	/**
	 * Closes the filter popover and the page of the column selection when the table leaves the
	 * display: displayed again, the table shows no popover nobody opened, and the content is
	 * released with it.
	 */
	@Override
	protected void onDetach() {
		super.onDetach();
		if (filterPopoverContent() != null) {
			showFilterPopover(null);
		}
		if (columnSelection() != null) {
			showColumnSelection(null);
		}
	}

	// -- Column selection --

	/**
	 * Opens the column selection: which columns the table displays, in which order, which of them
	 * it freezes, and which column the rows are grouped by.
	 *
	 * <p>
	 * The selection is the page "Columns" of the view popover. It is held in the state, as a child
	 * of the table, while the page is open, and every gesture in it changes the table at once - the
	 * selection tells {@link #applyColumns(List, List, String)} the arrangement it now has -, so
	 * there is nothing to confirm and nothing to cancel.
	 * </p>
	 */
	@ReactCommandHandler(CMD_OPEN_COLUMNS)
	void handleOpenColumns() {
		if (!_columnSelect) {
			return;
		}
		ReactColumnSelectControl selection =
			new ReactColumnSelectControl(getReactContext(), _view.columnOptions(), lockedColumnNames(),
				getGroupedColumn(), !_treeMode);
		selection.setOnChange(
			() -> applyColumns(selection.visibleColumns(), selection.lockedColumns(), selection.groupedColumn()));
		showColumnSelection(selection);
	}

	/**
	 * Closes the page of the column selection. The table stays as it is: every change has taken
	 * effect already.
	 */
	@ReactCommandHandler(CMD_CLOSE_COLUMNS)
	void handleCloseColumns() {
		showColumnSelection(null);
	}

	/**
	 * Shows the given column selection, or none, and disposes the one shown before.
	 *
	 * <p>
	 * Replacing the state only forgets the previous selection; disposing it is the job of whoever
	 * replaces it, as for the {@link #showFilterPopover(Map) filter popover}.
	 * </p>
	 */
	private void showColumnSelection(ReactColumnSelectControl selection) {
		ReactColumnSelectControl previous = columnSelection();
		putState(COLUMN_SELECTION, selection);
		if (previous != null) {
			previous.cleanupTree();
		}
	}

	/** The column selection of the open page, {@code null} while it is not open. */
	private ReactColumnSelectControl columnSelection() {
		return getState(COLUMN_SELECTION) instanceof ReactColumnSelectControl selection ? selection : null;
	}

	/**
	 * Applies a new set of displayed columns, the columns to freeze and the grouping, and re-renders.
	 *
	 * @param columns
	 *        The columns to display, in display order.
	 * @param lockedColumns
	 *        The displayed columns to freeze; the table freezes up to the last of them in the new order.
	 * @param groupedColumn
	 *        The column to group the rows by, displayed or not; {@code null} or empty for none.
	 */
	protected void applyColumns(List<String> columns, List<String> lockedColumns, String groupedColumn) {
		GroupSpec grouping = groupSpec(groupedColumn);
		rearrange(() -> {
			_view.setColumnOrder(columns);
			_view.setFrozenColumnCount(frozenPrefixOf(lockedColumns));
			if (!grouping.columns().equals(_view.state().getGrouping().columns())) {
				_view.group(grouping);
			}
		});
	}

	/**
	 * Performs a change of the table's arrangement - its columns, its frozen columns, its sort, its
	 * grouping - and re-renders: the cell controls of the buffered rows belong to the previous
	 * arrangement, so they are rebuilt for the new one.
	 *
	 * <p>
	 * Columns, frozen count and rows must reach the client as one patch: a client that already knows a newly shown
	 * column but still holds the rows of the previous column set would render a cell that has no control yet.
	 * </p>
	 *
	 * <p>
	 * A change of the grouping rearranges the rows, it does not replace them: the selected rows are
	 * the same objects under the same keys, at other positions. Only the cursor and the range
	 * anchor, which are positions, have to be found again.
	 * </p>
	 *
	 * @param change
	 *        The change, performed through the {@link TableView}.
	 */
	private void rearrange(Runnable change) {
		GroupSpec grouping = _view.state().getGrouping();
		Object update = beginUpdate();
		try {
			change.run();
			if (!_view.state().getGrouping().equals(grouping)) {
				relocateSelection();
				pushGrouping();
				pushSelection();
			}
			rebuildAfterRowChange();
		} finally {
			commitUpdate(update);
		}
	}

	/**
	 * The number of leading displayed columns up to and including the last of the given ones.
	 */
	private int frozenPrefixOf(List<String> lockedColumns) {
		int result = 0;
		List<ColumnView> displayed = _view.columns();
		for (int n = 0; n < displayed.size(); n++) {
			if (lockedColumns.contains(displayed.get(n).name())) {
				result = n + 1;
			}
		}
		return result;
	}

	/**
	 * The names of the columns the table freezes now - the locks the column selection opens with.
	 */
	protected Set<String> lockedColumnNames() {
		Set<String> result = new HashSet<>();
		List<ColumnView> displayed = _view.columns();
		for (int n = 0; n < Math.min(_view.frozenColumnCount(), displayed.size()); n++) {
			result.add(displayed.get(n).name());
		}
		return result;
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
		ReactTextInputControl text = new ReactTextInputControl(context, model);
		// Every value that reaches the server filters the table: a typed one after the pause, and at
		// once on Enter, which sends the value held back and finishes the edit - reported only to a
		// field with a commit listener, which has nothing more to do here.
		text.setDebounce(Long.valueOf(FILTER_TYPING_PAUSE_MS));
		text.setCommitListener(value -> {
			// The value has filtered the table already.
		});
		return text;
	}

	/**
	 * Rebuilds the row count and viewport after the backing data changed externally (e.g. an
	 * object was created or deleted and the row source was refreshed).
	 *
	 * <p>
	 * A selected key whose object is gone from the data is dropped from the selection. A selected
	 * row the table merely does not display - hidden by a filter, or inside a collapsed group or
	 * tree node - is still part of the data and stays selected (see
	 * {@link TableView#containedKeys(Collection)}).
	 * </p>
	 */
	public void refreshData() {
		_selectedKeys.retainAll(_view.containedKeys(_selectedKeys));
		commitSelection();
		rebuildAfterRowChange();
	}

	private void rebuildAfterRowChange() {
		clearCells();
		buildFullState();
	}

	private void clearCells() {
		for (RowCells cells : _cellCache.values()) {
			cells.dispose();
		}
		_cellCache.clear();
	}

	// -- Commands --

	/**
	 * Handles a viewport scroll request.
	 *
	 * <p>
	 * The viewport is limited to {@link #MAX_VIEWPORT_ROWS} rows.
	 * </p>
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
		GroupSpec grouping = groupSpec(column);
		if (grouping.columns().equals(_view.state().getGrouping().columns())) {
			return;
		}
		rearrange(() -> _view.group(grouping));
	}

	/**
	 * The grouping by the given column, {@link GroupSpec#NONE} for a {@code null} or empty one.
	 */
	private static GroupSpec groupSpec(String column) {
		return StringServices.isEmpty(column) ? GroupSpec.NONE : new GroupSpec(List.of(column));
	}

	/**
	 * Re-derives the cursor and the range anchor from the selected rows after the rows were
	 * rearranged.
	 *
	 * <p>
	 * A rearrangement (a change of the grouping) does not change the data, so the selection itself
	 * stays as it is - also a selected row that is not displayed, because a filter hides it or its
	 * group is collapsed. The cursor goes to the first displayed selected row, and to no row when
	 * none of the selected rows is displayed.
	 * </p>
	 */
	private void relocateSelection() {
		_cursorIndex = firstSelectedRowIndex();
		_selectionAnchor = _cursorIndex;
		commitSelection();
	}

	/**
	 * The index of the first displayed row that is selected, {@code -1} when none of the selected
	 * rows is displayed.
	 */
	private int firstSelectedRowIndex() {
		if (_selectedKeys.isEmpty()) {
			return -1;
		}
		List<Row<R>> rows = _view.rows(0, _view.rowCount());
		for (int n = 0; n < rows.size(); n++) {
			Row<R> row = rows.get(n);
			if (row.kind() == RowKind.DATA && _selectedKeys.contains(row.key())) {
				return n;
			}
		}
		return -1;
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
		putState(DROP_TREE_ZONES, Boolean.valueOf(rowTree() != null));
	}

	/**
	 * The tree the rows form, {@code null} while they form none - a flat table, and a grouped one,
	 * whose group headers stand for no object.
	 */
	private RowHierarchy<R> rowTree() {
		return getGroupedColumn() != null ? null : _view.hierarchy();
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
			commitSelection();
			toggleExpansion(clicked);
			return;
		}
		Set<Object> selection = clickSelection(rowIndex, ctrlKey, shiftKey);
		_cursorIndex = rowIndex;
		if (multiSelection() && !(shiftKey && _selectionAnchor >= 0)) {
			_selectionAnchor = rowIndex;
		}
		_selectedKeys.clear();
		_selectedKeys.addAll(selection);

		pushSelection();
	}

	/**
	 * The {@link Row#key() keys} of the rows a click on the data row at the given index selects,
	 * without changing the selection.
	 *
	 * <p>
	 * In {@link SelectionMode#MULTI}, a click with {@code Shift} adds the rows from the range
	 * anchor up to the clicked one, a click with {@code Ctrl} toggles the clicked row, and a plain
	 * click selects the clicked row only. In {@link SelectionMode#SINGLE}, a click selects the
	 * clicked row, and a click with {@code Ctrl} on the selected row clears the selection.
	 * </p>
	 */
	private Set<Object> clickSelection(int rowIndex, boolean ctrlKey, boolean shiftKey) {
		Object key = keyAt(rowIndex);
		Set<Object> result = new LinkedHashSet<>();
		if (multiSelection()) {
			if (shiftKey && _selectionAnchor >= 0) {
				result.addAll(_selectedKeys);
				int from = Math.min(_selectionAnchor, rowIndex);
				int to = Math.max(_selectionAnchor, rowIndex);
				for (Row<R> row : _view.rows(from, to + 1)) {
					if (row.kind() == RowKind.DATA) {
						result.add(row.key());
					}
				}
			} else if (ctrlKey) {
				result.addAll(_selectedKeys);
				if (!result.remove(key)) {
					result.add(key);
				}
			} else {
				result.add(key);
			}
		} else {
			if (!(ctrlKey && _selectedKeys.contains(key))) {
				result.add(key);
			}
		}
		return result;
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
	void handleSelectByKey(SelectByKeyArguments args) {
		ModelName name = args.getKey();
		int rowIndex = rowIndexOf(name);
		if (rowIndex < 0) {
			// Drift contract: a recorded row key that no longer designates a present row is an
			// explicit failure (replay reports success:false), never a silent no-op selection.
			throw new TopLogicException(I18NConstants.ERROR_ROW_KEY_UNRESOLVED__KEY.fill(name));
		}
		selectOnly(rowIndex);
	}

	/**
	 * Sets the selection among the rows of the table to the rows whose business objects are named
	 * by the given {@link ScriptingModelKey keys} - the replay-stable form a {@code Ctrl} or
	 * {@code Shift} click and {@link #CMD_SELECT_ALL} are recorded in, as the selection they result
	 * in.
	 *
	 * <p>
	 * The rows of the table are those the filter lets pass, the members of a collapsed group or
	 * tree node included. A selected row the filter hides is beyond the command, as it is beyond the
	 * gestures recorded this way: it stays selected.
	 * </p>
	 *
	 * @param args
	 *        Carries the {@link SelectByKeysArguments#getKeys() keys} of the rows to select (the
	 *        same keys the agent projection puts on each row).
	 */
	@ReactCommandHandler(CMD_SELECT_BY_KEYS)
	void handleSelectByKeys(SelectByKeysArguments args) {
		Map<Object, Object> keyByData = rowKeysByData();
		List<Object> named = new ArrayList<>();
		List<ModelName> unresolved = new ArrayList<>();
		for (ModelName name : args.getKeys()) {
			Object target = ScriptingModelKey.locate(null, name);
			Object key = target == null ? null : keyByData.get(target);
			if (key == null) {
				unresolved.add(name);
			} else {
				named.add(key);
			}
		}
		if (!unresolved.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_ROW_KEYS_UNRESOLVED__KEYS.fill(unresolved));
		}
		Set<Object> rowKeys = new HashSet<>(keyByData.values());
		Set<Object> selection = new LinkedHashSet<>();
		for (Object key : _selectedKeys) {
			if (!rowKeys.contains(key)) {
				selection.add(key);
			}
		}
		selection.addAll(named);
		selectRows(selection);
	}

	/**
	 * The {@link Row#key() keys} of the data rows the filter lets pass, by the business object of
	 * the row - the rows {@link #CMD_SELECT_BY_KEYS} selects among.
	 */
	private Map<Object, Object> rowKeysByData() {
		Map<Object, Object> result = new LinkedHashMap<>();
		for (Row<R> row : _view.allRows()) {
			if (row.kind() == RowKind.DATA && row.data() != null) {
				result.put(row.data(), row.key());
			}
		}
		return result;
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
	void handleActivate(ActivateRowArguments args) {
		activateRow(args.getRowIndex());
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
	void handleActivateByKey(ActivateByKeyArguments args) {
		ModelName name = args.getKey();
		int rowIndex = rowIndexOf(name);
		if (rowIndex < 0) {
			throw new TopLogicException(I18NConstants.ERROR_ROW_KEY_UNRESOLVED__KEY.fill(name));
		}
		activateRow(rowIndex);
	}

	/**
	 * Activates the row displaying the given business object, as a double-click on that row does:
	 * the row becomes the sole selection, then what {@link #setActivationHandler(ActivationHandler)}
	 * registered runs with the object.
	 *
	 * <p>
	 * This is the entry for a control acting on behalf of a row - a button the row carries, say -
	 * so that whatever reads the table's selection while the activation runs sees that row. An
	 * object the table currently displays in no row activates nothing.
	 * </p>
	 *
	 * @param row
	 *        The business object of the row to activate.
	 * @throws TopLogicException
	 *         The failure of the {@link ActivationHandler}.
	 */
	public void activate(R row) {
		if (row == null) {
			return;
		}
		activateRow(rowIndexOfData(row));
	}

	private void activateRow(int rowIndex) {
		Row<R> row = rowAt(rowIndex);
		if (row == null) {
			return;
		}
		if (row.kind() != RowKind.DATA) {
			// A group header has nothing to open: activating it collapses or expands the group.
			_cursorIndex = rowIndex;
			commitSelection();
			toggleExpansion(row);
			return;
		}
		selectOnly(rowIndex);
		ActivationHandler<R> handler = _activationHandler;
		if (handler == null) {
			return;
		}
		handler.rowActivated(row.data());
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
		Object target = ScriptingModelKey.locate(null, name);
		if (target == null) {
			return -1;
		}
		return rowIndexOfData(target);
	}

	/**
	 * The index of the row whose business object is the given one, or {@code -1} when no row
	 * displays it.
	 */
	private int rowIndexOfData(Object target) {
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
		result.add(ReactColumnSelectControl.CMD_COLUMN_MOVE);
		result.add(CMD_SET_FROZEN_COLUMN_COUNT);
		return result;
	}

	/**
	 * Records a gesture whose live arguments are session-bound in replay-stable form.
	 *
	 * <p>
	 * A plain (unmodified) row selection - a {@link #CMD_SELECT} by {@link #ARG_ROW_INDEX} - becomes
	 * a {@link #CMD_SELECT_BY_KEY} of the row's business identity, a {@link #CMD_ACTIVATE} becomes a
	 * {@link #CMD_ACTIVATE_BY_KEY} and a {@link #CMD_EXPAND} a {@link #CMD_EXPAND_BY_KEY} of the
	 * same, and a {@link #CMD_DROP} becomes a {@link #CMD_DROP_OBJECTS} naming the dragged objects
	 * and the location of the drop. A selection with {@code Ctrl} or {@code Shift}, a
	 * {@link #CMD_SELECT_ALL} and a keyboard navigation {@link #CMD_MOVE_SELECTION} depend on the
	 * cursor, the range anchor and the order of the rows, so they are
	 * recorded as the selection they result in, a {@link #CMD_SELECT_BY_KEYS} of the identities of
	 * the selected rows; consecutive ones coalesce into one step. So the recording survives sorting
	 * and a fresh session. A gesture on a row whose business object has no identity is recorded
	 * verbatim.
	 * </p>
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (arguments != null) {
			RecordedCommand recorded = recordByKey(command, arguments);
			if (recorded != null) {
				return recorded;
			}
		}
		return super.recordCommand(command, arguments);
	}

	private RecordedCommand recordByKey(String command, Map<String, Object> arguments) {
		switch (command) {
			case CMD_DROP:
				return recordDrop(arguments);
			case CMD_SELECT: {
				if (!(arguments.get(ARG_ROW_INDEX) instanceof Number index)) {
					return null;
				}
				int rowIndex = index.intValue();
				boolean ctrlKey = Boolean.TRUE.equals(arguments.get(ARG_CTRL_KEY));
				boolean shiftKey = Boolean.TRUE.equals(arguments.get(ARG_SHIFT_KEY));
				if (!ctrlKey && !shiftKey) {
					return recordByKey(rowIndex, SelectByKeyArguments.class, CMD_SELECT_BY_KEY);
				}
				Row<R> clicked = rowAt(rowIndex);
				if (clicked == null || clicked.kind() != RowKind.DATA) {
					return null;
				}
				return recordSelection(clickSelection(rowIndex, ctrlKey, shiftKey));
			}
			case CMD_SELECT_ALL:
				return recordSelection(selectAllSelection(Boolean.TRUE.equals(arguments.get(ARG_SELECTED))));
			case CMD_MOVE_SELECTION: {
				if (!(arguments.get(ARG_DIRECTION) instanceof String direction)) {
					return null;
				}
				KeyboardMove move = keyboardMove(direction, Boolean.TRUE.equals(arguments.get(ARG_EXTEND)),
					Boolean.TRUE.equals(arguments.get(ARG_MOVE)));
				return move == null ? null : recordSelection(move.selection());
			}
			case CMD_ACTIVATE:
				if (arguments.get(ARG_ROW_INDEX) instanceof Number rowIndex) {
					return recordByKey(rowIndex.intValue(), ActivateByKeyArguments.class, CMD_ACTIVATE_BY_KEY);
				}
				return null;
			case CMD_EXPAND:
				if (arguments.get(ARG_ROW_INDEX) instanceof Number rowIndex) {
					RecordedCommand recorded =
						recordByKey(rowIndex.intValue(), ExpandByKeyArguments.class, CMD_EXPAND_BY_KEY);
					if (recorded != null) {
						((ExpandByKeyArguments) recorded.command())
							.setExpanded(Boolean.TRUE.equals(arguments.get(ARG_EXPANDED)));
					}
					return recorded;
				}
				return null;
			default:
				return null;
		}
	}

	/**
	 * The given command addressing the row at the given index by the business identity of the row,
	 * {@code null} when the row has none.
	 */
	private RecordedCommand recordByKey(int rowIndex, Class<? extends ModelKeyArguments> type, String command) {
		ModelName key = rowKeyAt(rowIndex);
		if (key == null) {
			return null;
		}
		ModelKeyArguments recorded = TypedConfiguration.newConfigItem(type);
		recorded.setName(command);
		recorded.setKey(key);
		return new RecordedCommand(recorded);
	}

	/**
	 * A {@link #CMD_SELECT_BY_KEYS} setting the selection to the rows with the given
	 * {@link Row#key() keys}, {@code null} when one of them has no business identity.
	 *
	 * <p>
	 * A selected row the filter hides is no part of it: {@link #CMD_SELECT_BY_KEYS} leaves it
	 * selected.
	 * </p>
	 */
	private RecordedCommand recordSelection(Set<Object> selection) {
		SelectByKeysArguments recorded = TypedConfiguration.newConfigItem(SelectByKeysArguments.class);
		recorded.setName(CMD_SELECT_BY_KEYS);
		for (Map.Entry<Object, Object> entry : rowKeysByData().entrySet()) {
			if (selection.contains(entry.getValue())) {
				ModelName name = ScriptingModelKey.name(null, entry.getKey());
				if (name == null) {
					return null;
				}
				recorded.getKeys().add(name);
			}
		}
		return new RecordedCommand(recorded, true);
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
		KeyboardMove move = keyboardMove(args.getDirection(), args.isExtend(), args.isMove());
		if (move == null) {
			return;
		}
		_cursorIndex = move.cursor();
		_selectionAnchor = move.anchor();
		_selectedKeys.clear();
		_selectedKeys.addAll(move.selection());

		pushSelection();
		scrollIntoView(move.cursor());
	}

	/**
	 * The outcome of a keyboard navigation step: where the cursor and the range anchor go, and which
	 * rows are selected afterwards.
	 *
	 * @param cursor
	 *        The row index of the keyboard cursor.
	 * @param anchor
	 *        The row index the next range extension starts from.
	 * @param selection
	 *        The {@link Row#key() keys} of the selected rows.
	 */
	private record KeyboardMove(int cursor, int anchor, Set<Object> selection) {
		// Pure data.
	}

	/**
	 * What a {@link #CMD_MOVE_SELECTION} with the given arguments does, without doing it;
	 * {@code null} when it does nothing - the table has no rows, or the direction is unknown.
	 *
	 * @see MoveSelectionArguments
	 */
	private KeyboardMove keyboardMove(String direction, boolean extend, boolean move) {
		int total = _view.rowCount();
		if (total == 0) {
			return null;
		}
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
				default: return null;
			}
		}
		target = Math.max(0, Math.min(total - 1, target));
		Object key = keyAt(target);

		int anchor = _selectionAnchor;
		Set<Object> selection = new LinkedHashSet<>();
		if (multiSelection() && move) {
			// Ctrl: move the focus cursor only; leave the selection untouched.
			selection.addAll(_selectedKeys);
		} else if (multiSelection() && extend) {
			if (anchor < 0) {
				anchor = from < 0 ? target : from;
			}
			int lo = Math.min(anchor, target);
			int hi = Math.max(anchor, target);
			for (Row<R> row : _view.rows(lo, hi + 1)) {
				if (row.kind() == RowKind.DATA) {
					selection.add(row.key());
				}
			}
		} else {
			// Plain move (and the single-selection case): selection follows the cursor - onto a
			// group header, which stands for no object, it follows as an empty selection.
			Row<R> row = rowAt(target);
			if (key != null && row != null && row.kind() == RowKind.DATA) {
				selection.add(key);
			}
			anchor = target;
		}
		return new KeyboardMove(target, anchor, selection);
	}

	/**
	 * Handles the header select-all / deselect-all checkbox and {@code Ctrl+A}.
	 *
	 * <p>
	 * Acts on the rows the filter lets pass, the members of a collapsed group included - the rows
	 * {@link #SELECT_ALL_STATE} reports on. A selected row the filter hides is beyond the gesture:
	 * it stays selected either way, exactly as it stays selected while the filter hides it.
	 * </p>
	 */
	@ReactCommandHandler(CMD_SELECT_ALL)
	void handleSelectAll(SelectAllArguments args) {
		Set<Object> selection = selectAllSelection(args.isSelected());
		_selectedKeys.clear();
		_selectedKeys.addAll(selection);
		pushSelection();
	}

	/**
	 * The {@link Row#key() keys} of the rows {@link #CMD_SELECT_ALL} selects, without changing the
	 * selection.
	 *
	 * @param selected
	 *        Whether the rows the filter lets pass are added to the selection or taken out of it.
	 */
	private Set<Object> selectAllSelection(boolean selected) {
		Set<Object> result = new LinkedHashSet<>(_selectedKeys);
		List<Object> matching = _view.matchingKeys();
		if (selected) {
			result.addAll(matching);
		} else {
			matching.forEach(result::remove);
		}
		return result;
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
	 * Handles the move of a column in the table header: it changes its position and whether it is frozen in one
	 * step.
	 *
	 * <p>
	 * The target index is a place in the displayed columns after the moved column was taken out of them. A frozen
	 * column ends up at the end of the frozen block at the latest, a movable one at the start of the rest at the
	 * earliest; the number of frozen columns follows the column across the border. The columns the user cannot
	 * decide about (a leading detail column) keep their place in front of the movable ones. A column pinned to the
	 * end, one the user cannot arrange and an unknown one are not moved, and neither is a column to be hidden:
	 * the header has no hidden zone.
	 * </p>
	 */
	@ReactCommandHandler(ReactColumnSelectControl.CMD_COLUMN_MOVE)
	void handleColumnMove(ColumnMoveArguments args) {
		String column = args.getColumn();
		if (!args.isVisible()) {
			return;
		}
		List<String> displayed = new ArrayList<>();
		int unpinned = 0;
		for (ColumnView view : _view.columns()) {
			displayed.add(view.name());
			if (!view.pinnedEnd()) {
				unpinned++;
			}
		}
		int from = displayed.indexOf(column);
		if (from < 0 || !isArrangeable(column)) {
			return;
		}
		// Leading columns the user cannot arrange stay in front of every movable column.
		int leading = 0;
		while (leading < unpinned && !isArrangeable(displayed.get(leading))) {
			leading++;
		}
		int frozen = Math.min(_view.frozenColumnCount(), unpinned);
		int remaining = frozen - (from < frozen ? 1 : 0);
		int block = Math.max(remaining, leading);
		int target = Math.min(Math.max(args.getTargetIndex(), leading), unpinned - 1);
		int count;
		if (args.isLocked()) {
			target = Math.min(target, block);
			count = block + 1;
		} else {
			target = Math.max(target, block);
			count = remaining;
		}
		int movedTo = target;
		int frozenCount = count;
		rearrange(() -> {
			_view.moveColumn(column, movedTo);
			_view.setFrozenColumnCount(frozenCount);
		});
	}

	/**
	 * Whether the user may move the column with the given name in the table header: it is neither pinned to the end
	 * nor one of the columns the column selection does not offer.
	 */
	private boolean isArrangeable(String column) {
		for (ColumnOption option : _view.columnOptions()) {
			if (option.name().equals(column)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles a tree/group expand or collapse.
	 */
	@ReactCommandHandler(CMD_EXPAND)
	void handleExpand(ExpandArguments args) {
		setExpanded(args.getRowIndex(), args.isExpanded());
	}

	/**
	 * Expands or collapses the tree row whose business object is named by the given
	 * {@link ScriptingModelKey key} - the replay-stable counterpart of {@link #handleExpand} by
	 * {@link #ARG_ROW_INDEX}, which a recorded expansion is captured as.
	 *
	 * @param args
	 *        Carries a {@link ExpandByKeyArguments#getKey() key} with a row business identity (the
	 *        same key the agent projection puts on each row).
	 */
	@ReactCommandHandler(CMD_EXPAND_BY_KEY)
	void handleExpandByKey(ExpandByKeyArguments args) {
		ModelName name = args.getKey();
		int rowIndex = rowIndexOf(name);
		if (rowIndex < 0) {
			throw new TopLogicException(I18NConstants.ERROR_ROW_KEY_UNRESOLVED__KEY.fill(name));
		}
		setExpanded(rowIndex, args.isExpanded());
	}

	private void setExpanded(int rowIndex, boolean expanded) {
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
	 * The named filter replaces the whole filter, the search term included, so what the bar displays
	 * as active is what the table is filtered by - a column the filter does not mention ends up
	 * unfiltered, and a filter that names no term of its own leaves the table searching for nothing.
	 * </p>
	 */
	@ReactCommandHandler(CMD_APPLY_NAMED_FILTER)
	void handleApplyNamedFilter(ApplyNamedFilterArguments args) {
		applyNamedFilter(args.getId());
	}

	/**
	 * Filters the table by the criteria of one of the {@link TableView#namedFilters() named filters}
	 * it offers, and re-renders it.
	 *
	 * <p>
	 * The named filter replaces the whole filter, so what the bar displays as active is what the
	 * table is filtered by - a column the filter does not mention ends up unfiltered.
	 * </p>
	 *
	 * <p>
	 * No filter carries the given identifier - it is empty, or it names one this table does not (or
	 * no longer) offer - leaves the table {@link #clearFilter() unfiltered}: a name nothing carries
	 * selects nothing, and the table shows every row instead of keeping criteria that belong to no
	 * name.
	 * </p>
	 *
	 * @param id
	 *        The {@link NamedFilter#id() identifier} of the filter to apply, {@code null} to
	 *        unfilter the table.
	 */
	public void applyNamedFilter(String id) {
		if (id == null || id.isEmpty() || namedFilter(id) == null) {
			clearFilter();
			return;
		}
		_view.applyNamedFilter(id);
		rebuildAfterRowChange();
	}

	/**
	 * The offered {@link NamedFilter} carrying the given {@link NamedFilter#id() identifier},
	 * {@code null} when none does.
	 */
	private NamedFilter namedFilter(String id) {
		for (NamedFilter filter : _view.namedFilters()) {
			if (id.equals(filter.id())) {
				return filter;
			}
		}
		return null;
	}

	@ReactCommandHandler(CMD_CLEAR_FILTER)
	void handleClearFilter() {
		clearFilter();
	}

	/**
	 * Unfilters the table - clears every column filter and the search term - and re-renders it.
	 *
	 * <p>
	 * This is what clicking the active chip in the filter bar does: the chip acts as a toggle, and a
	 * second click leaves the table showing every row again. It withdraws the criteria of the chip
	 * together with a text searched for on top of them, which is what the user sees the chip select.
	 * </p>
	 */
	public void clearFilter() {
		for (String column : new ArrayList<>(_view.state().getFilters().keySet())) {
			_view.filter(column, null);
		}
		_view.search(null);
		rebuildAfterRowChange();
	}

	@ReactCommandHandler(CMD_SEARCH)
	void handleSearch(SearchArguments args) {
		search(args.getTerm());
	}

	/**
	 * Searches the table's displayed columns for a text - or clears the search when the term is
	 * empty - and re-renders it.
	 *
	 * <p>
	 * The bar searches for a plain {@link TextFilterState#contains(String) case-insensitive
	 * substring}; the matching flags of a column's own text filter stay that column's business.
	 * </p>
	 *
	 * <p>
	 * The search narrows the rows within whatever the table is filtered by, and leaves that
	 * filtering alone: a {@link NamedFilter} the table matches goes on being the
	 * {@link TableView#activeNamedFilter() active} one while the text is searched for, so the bar
	 * keeps its chip marked and the table shows the rows of that filter holding the text.
	 * </p>
	 *
	 * @param term
	 *        The text to search for, {@code null} or empty to search for nothing.
	 */
	public void search(String term) {
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
	 * Handles a change of the view options: density, stripes and column lines.
	 *
	 * <p>
	 * They are stored for the person, not for this table: they apply to every table, and the
	 * state of this table does not change.
	 * </p>
	 *
	 * @see TableViewPreferences#store(String, boolean, boolean)
	 */
	@ReactCommandHandler(CMD_SET_VIEW_OPTIONS)
	void handleSetViewOptions(SetViewOptionsArguments args) {
		TableViewPreferences.store(args.getDensity(), args.isStriped(), args.isColumnLines());
	}

	/**
	 * Handles the view reset: restores the arrangement the table is defined with - its columns,
	 * their widths, the frozen columns, the sort and the grouping - and keeps the filters. The view
	 * options of the person are not part of it.
	 */
	@ReactCommandHandler(CMD_RESET_VIEW)
	void handleResetView() {
		rearrange(_view::resetToDefaults);
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
	public boolean isDragEnabled() {
		return _dragEnabled;
	}

	@Override
	public String dragKind() {
		return _dragKind;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isDraggable(Object object) {
		return _dragEnabled && (_draggable == null || _draggable.test((R) object));
	}

	/** Whether the given row offers a drag, see {@link #setDragSource(String, Predicate)}. */
	private boolean isDraggableRow(Row<R> row) {
		return row.kind() == RowKind.DATA && row.data() != null && isDraggable(row.data());
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
	 * The row the drop names is resolved into the place the drop was made at (see
	 * {@link #dropPlace(String, DropZone)}), everything else is
	 * {@link DropSupport#drop(DropArguments, DropPlace.Resolver) shared} with every control accepting
	 * drops.
	 * </p>
	 */
	@ReactCommandHandler(CMD_DROP)
	void handleDrop(DropArguments args) {
		_dropSupport.drop(args, this::dropPlace);
	}

	/**
	 * Answers whether a drop right where a drag hovers would be accepted, without applying it.
	 *
	 * <p>
	 * The verdict is added to {@link #DROP_VERDICTS} under the {@link DropProbeArguments#getProbe()
	 * probe's identifier}, see {@link DropSupport#probe(DropProbeArguments, DropPlace.Resolver)}. The
	 * probe is technical: it is neither recorded nor offered as an action, and it never fails, since
	 * a refusal is its answer.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_DROP_PROBE, technical = true)
	void handleDropProbe(DropProbeArguments args) {
		putState(DROP_VERDICTS, _dropSupport.probe(args, this::dropPlace));
	}

	/**
	 * The place of a drop made in the given zone of the row with the given client-side key, or
	 * beside the rows.
	 *
	 * @return The place, {@code null} if the key names a row this table no longer displays.
	 *
	 * @see DropPlace.Resolver#place(String, DropZone)
	 */
	private DropPlace dropPlace(String rowKey, DropZone zone) {
		RowHierarchy<R> tree = rowTree();
		if (tree != null) {
			return treePlace(tree, rowKey, zone);
		}
		if (rowKey == null) {
			return new RowPlace(-1, DropZone.NONE);
		}
		int rowIndex = rowIndex(rowKey);
		if (rowAt(rowIndex) == null) {
			return null;
		}
		return new RowPlace(rowIndex, zone);
	}

	/**
	 * The place of a drop on the rows of a tree, see {@link #dropPlace(String, DropZone)}.
	 */
	private DropPlace treePlace(RowHierarchy<R> tree, String rowKey, DropZone zone) {
		RowNavigation navigation = new RowNavigation(tree);
		if (rowKey == null) {
			return TreeDropPlace.beside(navigation);
		}
		Row<R> row = rowById(rowKey);
		if (!isDataRow(row)) {
			return null;
		}
		return new TreeDropPlace<>(navigation, row, rowKey, zone);
	}

	/**
	 * The rows of a tree as the {@link TreeDropPlace drop rules} see them.
	 */
	private static final class RowNavigation implements DropTreeNavigation<Row<?>> {

		private final RowHierarchy<?> _tree;

		RowNavigation(RowHierarchy<?> tree) {
			_tree = tree;
		}

		@Override
		public List<? extends Row<?>> topLevel() {
			return _tree.roots();
		}

		@Override
		public Object topLevelParent() {
			return _tree.rootParent();
		}

		@Override
		public Row<?> parent(Row<?> row) {
			return parentOf(_tree, row);
		}

		@Override
		public List<? extends Row<?>> children(Row<?> row) {
			return childrenOf(_tree, row);
		}

		@Override
		public boolean isExpanded(Row<?> row) {
			return row.expanded();
		}

		@Override
		public Object businessObject(Row<?> row) {
			return row.data();
		}

		@SuppressWarnings("unchecked")
		private static <T> Row<?> parentOf(RowHierarchy<T> tree, Row<?> row) {
			return tree.parent((Row<T>) row);
		}

		@SuppressWarnings("unchecked")
		private static <T> List<Row<T>> childrenOf(RowHierarchy<T> tree, Row<?> row) {
			return tree.children((Row<T>) row);
		}

	}

	/**
	 * A place of a drop in this table, resolved as in a flat list.
	 *
	 * <p>
	 * An insertion from the upper part of a row is marked before that row, one from its lower part
	 * after it, one beside the rows after the last data row - or on the table as a whole, if it has
	 * none.
	 * </p>
	 */
	private final class RowPlace implements DropPlace {

		/** The index of the row the drop was made on, {@code -1} for a drop beside the rows. */
		private final int _rowIndex;

		/** The zone of the row the drop was made in, {@link DropZone#NONE} without a row. */
		private final DropZone _zone;

		RowPlace(int rowIndex, DropZone zone) {
			_rowIndex = rowIndex;
			_zone = rowIndex < 0 ? DropZone.NONE : zone;
		}

		@Override
		public DropLocation location(DropMode mode) {
			return locationOf(mode, _rowIndex, _zone);
		}

		@Override
		public DropMarker marker(DropLocation location) {
			return markerOf(location, _rowIndex, _zone, markerRow());
		}

		@Override
		public String markerKey(DropMarker marker) {
			return marker == DropMarker.CONTROL ? null : ROW_ID_PREFIX + markerRow();
		}

		/** The index of the row a marker of an item is drawn at, {@code -1} if there is none. */
		private int markerRow() {
			return _rowIndex >= 0 ? _rowIndex : lastDataRowIndex();
		}

	}

	/**
	 * The location of a drop of the given mode made in the given zone of the given row, as in a flat
	 * list.
	 *
	 * @param rowIndex
	 *        The index of the row the drop was made on, {@code -1} for a drop beside the rows.
	 * @param zone
	 *        The zone of the row the drop was made in.
	 * @return The location, {@code null} if the place yields none for the mode.
	 */
	private DropLocation locationOf(DropMode mode, int rowIndex, DropZone zone) {
		switch (mode) {
			case CONTROL:
				return new DropLocation.Control();
			case ONTO: {
				Row<R> row = rowAt(rowIndex);
				if (row == null || zone == DropZone.NONE || row.kind() != RowKind.DATA || row.data() == null) {
					return null;
				}
				return new DropLocation.Onto(row.data());
			}
			case ORDERED:
				if (rowIndex < 0 || zone == DropZone.NONE) {
					return new DropLocation.Insert(null, null);
				}
				switch (zone) {
					case UPPER:
						return new DropLocation.Insert(null, dataAtOrAfter(rowIndex));
					case LOWER:
						return new DropLocation.Insert(null, dataAtOrAfter(rowIndex + 1));
					default:
						return null;
				}
		}
		throw new IllegalArgumentException("Unknown drop mode: " + mode);
	}

	/**
	 * The business object of the first data row at or after the given index, {@code null} if there
	 * is none.
	 */
	private R dataAtOrAfter(int index) {
		int count = _view.rowCount();
		for (int n = Math.max(0, index); n < count; n++) {
			Row<R> row = rowAt(n);
			if (isDataRow(row)) {
				return row.data();
			}
		}
		return null;
	}

	/** The index of the last data row, {@code -1} if there is none. */
	private int lastDataRowIndex() {
		for (int n = _view.rowCount() - 1; n >= 0; n--) {
			if (isDataRow(rowAt(n))) {
				return n;
			}
		}
		return -1;
	}

	private static boolean isDataRow(Row<?> row) {
		return row != null && row.kind() == RowKind.DATA && row.data() != null;
	}

	/**
	 * The marker the client draws for a drop accepted at the given location, made in the given zone
	 * of the row with the given index.
	 *
	 * @param rowIndex
	 *        The index of the row the drop was made on, {@code -1} for a drop beside the rows.
	 * @param markerRow
	 *        The index of the row a marker of an item is drawn at, {@code -1} if there is none.
	 */
	private static DropMarker markerOf(DropLocation location, int rowIndex, DropZone zone, int markerRow) {
		if (location instanceof DropLocation.Onto) {
			return DropMarker.INTO;
		}
		if (location instanceof DropLocation.Insert) {
			if (rowIndex >= 0 && zone == DropZone.UPPER) {
				return DropMarker.BEFORE;
			}
			if (markerRow >= 0) {
				return DropMarker.AFTER;
			}
		}
		return DropMarker.CONTROL;
	}

	/**
	 * Applies a drop of the objects named by their {@link ScriptingModelKey business identity} - the
	 * replay-stable counterpart of {@link #handleDrop} by client-side keys, which a recorded drop is
	 * captured as so it survives sorting, filtering and a fresh session.
	 *
	 * @see DropSupport#dropObjects(DropObjectsArguments, java.util.function.Predicate)
	 */
	@ReactCommandHandler(CMD_DROP_OBJECTS)
	void handleDropObjects(DropObjectsArguments args) {
		_dropSupport.dropObjects(args, this::displaysLocation);
	}

	/**
	 * Whether the reference objects of the given location are rows of this table; a flat table has
	 * no parent to insert under, the rows of a tree are checked as a tree's nodes are (see
	 * {@link TreeDropPlace#displays(DropTreeNavigation, java.util.function.Function, DropLocation)}).
	 */
	private boolean displaysLocation(DropLocation location) {
		RowHierarchy<R> tree = rowTree();
		if (tree != null) {
			return TreeDropPlace.displays(new RowNavigation(tree), this::rowFor, location);
		}
		if (location instanceof DropLocation.Onto onto) {
			return onto.target() == null || rowFor(onto.target()) != null;
		}
		if (location instanceof DropLocation.Insert insert) {
			return insert.parent() == null && (insert.before() == null || rowFor(insert.before()) != null);
		}
		return true;
	}

	/**
	 * Rewrites a client drop into the replay-stable {@link #CMD_DROP_OBJECTS} form, naming the
	 * location the drop target accepts the drop at.
	 *
	 * @see DropSupport#recordDrop(Map, DropPlace.Resolver)
	 */
	private RecordedCommand recordDrop(Map<String, Object> arguments) {
		return _dropSupport.recordDrop(arguments, this::dropPlace);
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
	 * Re-buffers the viewport so the given row is within the rendered window, and re-renders the
	 * rows when that moved the viewport.
	 */
	private void scrollIntoView(int target) {
		int start = _viewportStart;
		if (target < start) {
			start = target;
		} else if (target >= start + _viewportCount) {
			start = target - _viewportCount + 1;
		}
		start = Math.max(0, start);
		if (start != _viewportStart) {
			updateViewport(start, _viewportCount);
		}
	}

	/**
	 * Publishes the current selection to the {@link TableView} and the
	 * {@link #addSelectionListener(SelectionListener) selection listeners}, as one change the
	 * listeners can refuse.
	 *
	 * <p>
	 * A {@link ChannelVetoException} from a listener restores the selection state of the last
	 * accepted change - the keys, the cursor, the range anchor and the {@link TableView}'s
	 * selection - and is rethrown, so the table holds what it displays until the refused change is
	 * retried.
	 * </p>
	 */
	private void pushSelection() {
		selectInView(_selectedKeys);
		try {
			for (SelectionListener listener : _selectionListeners) {
				listener.selectionChanged(new LinkedHashSet<>(_selectedKeys));
			}
		} catch (ChannelVetoException ex) {
			_selectedKeys.clear();
			_selectedKeys.addAll(_committedKeys);
			_cursorIndex = _committedCursor;
			_selectionAnchor = _committedAnchor;
			selectInView(_selectedKeys);
			pushSelectionState();
			throw ex;
		}
		commitSelection();
	}

	/** Writes the given keys as the {@link TableView}'s selection. */
	private void selectInView(Set<Object> keys) {
		_view.select(new Selection(_selectionMode, new LinkedHashSet<>(keys)));
	}

	/**
	 * Makes the current selection state the one a refused change is restored to.
	 *
	 * <p>
	 * Called when the selection listeners have accepted a change, and wherever the table changes
	 * what it displays without asking them: it drops a key it has no row for any more, or it moves
	 * the cursor onto a group header. What the table gave up must not come back through a restore.
	 * </p>
	 */
	private void commitSelection() {
		_committedKeys = new LinkedHashSet<>(_selectedKeys);
		_committedCursor = _cursorIndex;
		_committedAnchor = _selectionAnchor;
		// A refused change restores exactly this state, so the counts of the committed selection are
		// the ones the client keeps showing then.
		refreshSelectionCounts();
		pushSelectionState();
	}

	/**
	 * The answer to a drop probe names a row by its client-side id and is only of use to the client
	 * rendering the drag, and the row carrying the keyboard focus is where the browser continues a
	 * keyboard navigation - a gesture recorded as the selection it results in -, so both are left
	 * out of the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DROP_VERDICTS, CURSOR_INDEX);
	}

}
