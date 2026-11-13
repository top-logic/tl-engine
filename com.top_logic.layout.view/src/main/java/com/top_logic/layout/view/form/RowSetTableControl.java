/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.CommandTrigger;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.table.CellContentReactAdapter;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.model.DisplayedRowsBinding;
import com.top_logic.layout.view.model.TableFilterBinding;
import com.top_logic.layout.view.table.CellEditing;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnExistence;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.util.Pointer;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.Sort;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NamedFilterStore;
import com.top_logic.table.RowSource;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.util.Resources;

/**
 * Table presentation of a row set edited within a form.
 *
 * <p>
 * Renders as a {@code TLPanel} React component with a {@link TableViewControl} as content. The
 * editing (row overlays, per-cell field models, validation, participation in the form's save) is
 * the {@link RowSetEditSession} the table displays, see {@link AbstractCompositionControl}; the
 * membership semantics (row objects, add, remove, commit) come from the configured
 * {@link RowSetBinding}.
 * </p>
 *
 * <p>
 * Data columns come from the declarations the table is built with, and from the
 * {@link ColumnDeclarations#mainColumns(TLStructuredType) main columns} of the row type for a table
 * declaring none (sortable, filterable, cells displayed through the view-mode field display of the
 * column's values). While the form is in edit
 * mode, the cells of rows covered by the {@link RowEditPolicy} render the input the column's
 * {@link CellEditing} builds, where it offers one.
 * An action column for row removal is appended in edit mode when the binding supports removal; a
 * detail-open column is prepended when a {@link #setDetailDialog detail dialog} is configured.
 * </p>
 *
 * <p>
 * A {@link #setFramed framed} table shows the bound attribute's label as panel title, offers row
 * creation as an Add button in its own toolbar and is sized by its rows; a frameless table shows
 * neither, fills the height its container offers, and its creator offers row creation through the
 * enclosing command scope instead. Optional wiring - column
 * personalization, a two-way selection channel, and automatic row refresh on model or input changes
 * - is configured through setters before {@link #init()}. While an edit session is running, the row
 * set is frozen (no automatic re-query), so edited rows stay in place; after save or cancel the rows
 * are re-read.
 * </p>
 *
 * <p>
 * Where the rows come from, how they follow the model and how the selection is bound to them is
 * the {@link RowArrangement} of the table: a {@link ListRowArrangement} configured through
 * {@link #setRowRefresh(Function, Set, List, List, List, List)}, or the arrangement set by
 * {@link #setRowArrangement(RowArrangement)}.
 * </p>
 *
 * <p>
 * A table over a bound attribute follows the display mode of the attribute, see
 * {@link AbstractCompositionControl}: a hidden attribute hides the table with its title, an
 * attribute that may not be edited shows its rows view only while the form is edited, without
 * row commands, removal column and cell inputs.
 * </p>
 */
public class RowSetTableControl extends AbstractCompositionControl {

	/** The React module rendering this control. */
	private static final String REACT_MODULE = "TLPanel";

	/** Column name for the detail-open action column. */
	static final String COLUMN_DETAIL = "_detail";

	/** Column name for the row-removal action column. */
	public static final String COLUMN_DELETE = "_delete";

	/** Display width in pixels of the detail and the row-removal action columns. */
	public static final int ACTION_COLUMN_WIDTH = 48;

	/** Command name for {@link #handleAddRow(ReactContext)}. */
	private static final String CMD_ADD_ROW = "addRow";

	/** Name of the {@link #getAddRowCommand() add-row command}. */
	public static final String COMMAND_ADD_ROW = "tableAddRow";

	/** Name of the {@link #getRemoveSelectedRowsCommand() command removing the selected rows}. */
	public static final String COMMAND_REMOVE_SELECTED_ROWS = "tableRemoveSelectedRows";

	/** Panel state: the validation error of the bound attribute, or {@code null}. */
	private static final String ERROR_MESSAGE = "errorMessage";

	/** Panel state: encoded theme icon displayed in front of the error message. */
	private static final String ERROR_ICON = "errorIcon";

	private final ViewContext _context;

	/** The declarations of the data columns, in display order. */
	private final List<ColumnDeclaration> _columns;

	private final RowEditPolicy _policy;

	private String _fallbackTitle;

	private boolean _framed = true;

	private CompositionTableElement.DetailDialogConfig _detailDialogConfig;

	private ViewStateStore _store;

	private TableId _tableId;

	/** @see #setNamedFilters(BiFunction, NamedFilterStore, String) */
	private BiFunction<List<? extends Column<?, ?>>, Set<String>, List<NamedFilter>> _declaredFilters;

	/** @see #setNamedFilters(BiFunction, NamedFilterStore, String) */
	private NamedFilterStore _filterStore;

	/** @see #setNamedFilters(BiFunction, NamedFilterStore, String) */
	private String _initialFilter;

	/** @see #setFilterBar(boolean) */
	private boolean _filterBar;

	/** @see #setRowCount(boolean) */
	private boolean _rowCount = true;

	private SortSpec _defaultSort = SortSpec.NONE;

	/** The columns displayed only once the user selects them, taken from the resolved columns. */
	private Set<String> _hiddenByDefault = Set.of();

	/** @see #setFixedColumns(int) */
	private int _fixedColumns;

	/** @see #setSelectionChannel(ViewChannel) */
	private ViewChannel _selectionChannel;

	/** @see #setFilterChannels(ViewChannel, ViewChannel) */
	private ViewChannel _activePresetChannel;

	/** @see #setFilterChannels(ViewChannel, ViewChannel) */
	private ViewChannel _searchTermChannel;

	/** Binds the filter channels to the current {@link #_tableControl}. */
	private TableFilterBinding _filterBinding;

	/** @see #setDisplayedRowsChannel(ViewChannel) */
	private ViewChannel _displayedRowsChannel;

	/** Publishes the rows of the current {@link #_tableControl} on {@link #_displayedRowsChannel}. */
	private DisplayedRowsBinding _displayedRowsBinding;

	/**
	 * How the rows are arranged, followed and selected.
	 *
	 * @see #setRowRefresh(Function, Set, List, List, List, List)
	 * @see #setRowArrangement(RowArrangement)
	 */
	private RowArrangement _arrangement = new ListRowArrangement();

	private TableViewControl<TLObject> _tableControl;

	/**
	 * The setups of the data columns currently displayed.
	 *
	 * @see #getExportRenderers()
	 */
	private List<ColumnSetup> _dataSetups = List.of();

	/** What a row activation runs, {@code null} for a table whose rows cannot be opened. */
	private TableViewControl.ActivationHandler<TLObject> _activationHandler;

	/** The grouping the table starts with, until a personalization of its own exists. */
	private GroupSpec _grouping = GroupSpec.NONE;

	/** @see #setSelectionMode(SelectionMode) */
	private SelectionMode _selectionMode = SelectionMode.SINGLE;

	/** Columns appended behind the data and action columns, see {@link #setTrailingColumns(List)}. */
	private List<? extends Column<TLObject, ?>> _trailingColumns = List.of();

	/** @see #setRowCssClass(Function) */
	private Function<? super TLObject, String> _rowCssClass;

	/** @see #setRowColor(Function) */
	private Function<? super TLObject, ValueColor> _rowColor;

	/**
	 * The observation of the conditions deciding which of the data columns of the current
	 * {@link #_tableControl} exist, {@link Registration#NONE} while there is none.
	 */
	private Registration _existence = Registration.NONE;

	/**
	 * Asks again which of the data columns of the current {@link #_tableControl} exist and which
	 * declared filters are offered, see
	 * {@link #refreshColumnsAndDeclaredFilters(ColumnExistence, DefaultTableView, List)}; does
	 * nothing while there is no table.
	 */
	private Runnable _columnRefresh = () -> {
		// No table.
	};

	private final Set<Object> _selectedKeys = new java.util.LinkedHashSet<>();

	/**
	 * For each row, the attribute values its read-only cells were built from while the table is
	 * edited, see {@link #renderAsEdited(TLObject, Supplier)}.
	 *
	 * <p>
	 * The objects of the values are the objects that are edited, never their buffers, so that a
	 * change of a row is found whichever of the two was read.
	 * </p>
	 */
	private final Map<TLObject, Set<Pointer>> _cellDependencies = new IdentityHashMap<>();

	/**
	 * The rows whose read-only cells are built again when the current interaction has completed,
	 * see {@link #rowValueChanged(TLObject, TLStructuredTypePart)}.
	 */
	private final Set<TLObject> _staleRows = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * The model types of the rows the columns are resolved against, empty for the type of the
	 * binding, see {@link #setRowTypes(List)}.
	 */
	private List<TLStructuredType> _rowTypes = List.of();

	/** How the {@link #getAddRowCommand() add command} creates a row, see {@link #setRowCreation(RowCreation)}. */
	private RowCreation _creation = RowCreation.BINDING;

	/** The command adding a row, see {@link #getAddRowCommand()}. */
	private SimpleCommandModel _addRowCommand;

	/** The command removing the selected rows, see {@link #getRemoveSelectedRowsCommand()}. */
	private SimpleCommandModel _removeSelectedRowsCommand;

	/** The panel toolbar holding the row commands, or {@code null} if it shows none. */
	private ReactToolbarControl _toolbar;

	/**
	 * Creates a new {@link RowSetTableControl}.
	 *
	 * @param context
	 *        The view context (channels, model scope, React wiring).
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics (row objects, add, remove, commit).
	 * @param columns
	 *        The declarations of the data columns to display and edit.
	 * @param policy
	 *        Which rows are editable while the form is in edit mode.
	 */
	public RowSetTableControl(ViewContext context, FormControl formControl, RowSetBinding binding,
			List<ColumnDeclaration> columns, RowEditPolicy policy) {
		super(context, formControl, binding, REACT_MODULE);
		_context = context;
		_columns = columns;
		_policy = policy;
		initState();
	}

	/**
	 * Creates a {@link RowSetTableControl} displaying the given running session.
	 *
	 * <p>
	 * The table displays and edits the rows of the session, which outlives the table.
	 * </p>
	 *
	 * @param context
	 *        The view context (channels, model scope, React wiring).
	 * @param session
	 *        The session editing the rows.
	 * @param columns
	 *        The declarations of the data columns to display and edit.
	 * @param policy
	 *        Which rows are editable while the session runs.
	 */
	public RowSetTableControl(ViewContext context, RowSetEditSession session, List<ColumnDeclaration> columns,
			RowEditPolicy policy) {
		super(context, session, REACT_MODULE);
		_context = context;
		_columns = columns;
		_policy = policy;
		initState();
	}

	/**
	 * Sets the model types of the rows, which the columns are resolved against.
	 *
	 * <p>
	 * A table displaying rows of several types resolves an attribute column at the first of these
	 * types holding the attribute, see {@link ColumnResolution}. Without types, the columns are
	 * resolved against the {@link RowSetBinding#getRowType() row type of the binding}.
	 * </p>
	 *
	 * @param rowTypes
	 *        The model types of the rows, in their order, or an empty list.
	 */
	public void setRowTypes(List<TLStructuredType> rowTypes) {
		_rowTypes = List.copyOf(rowTypes);
	}

	private void initState() {
		_addRowCommand = SimpleCommandModel.create(COMMAND_ADD_ROW,
			Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_ADD), this::addRowOfChosenType)
			.setImage(Icons.COMPOSITION_TABLE_ADD)
			.setPlacement(CommandPlacement.TOOLBAR)
			.setVisible(this::offersRowCreation)
			.setExecutable(() -> !createTypes().isEmpty());
		_removeSelectedRowsCommand = SimpleCommandModel.create(COMMAND_REMOVE_SELECTED_ROWS,
			Resources.getInstance().getString(I18NConstants.TABLE_REMOVE_SELECTED_ROWS),
			context -> removeSelectedRows())
			.setImage(Icons.COMPOSITION_TABLE_DELETE)
			.setPlacement(CommandPlacement.TOOLBAR)
			.setVisible(this::offersSelectedRowsRemoval)
			.setExecutable(() -> offersSelectedRowsRemoval() && !_selectedKeys.isEmpty());

		// Row-set tables should span the full form row.
		putState("fullLine", Boolean.TRUE);

		putState(ERROR_ICON,
			ReactImages.encode(_context, com.top_logic.layout.react.control.layout.Icons.VALIDATION_ERROR));

		updateFill();
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		_arrangement.attach(_context.getModelScope());
	}

	@Override
	protected void onDetach() {
		// Pause the observation rather than dropping it: the table keeps its rows while it is not
		// displayed, and becoming displayed again resumes observing. Dropping it is reserved for a
		// table rebuild and for disposal, where a new observation begins (or none at all).
		_arrangement.detach();
		super.onDetach();
	}

	/**
	 * Pushes the bound attribute's validation error to the panel display.
	 *
	 * <p>
	 * Like regular fields, the error only becomes visible once the field model is revealed - on
	 * user interaction or on a save attempt.
	 * </p>
	 */
	@Override
	protected void updateCompositionErrorDisplay() {
		CompositionFieldModel fieldModel = fieldModel();
		if (fieldModel != null && fieldModel.hasError()) {
			putState(ERROR_MESSAGE, Resources.getInstance().getString(fieldModel.getError()));
		} else {
			putState(ERROR_MESSAGE, null);
		}
	}

	/**
	 * Sets the panel title used when the binding has no bound attribute to derive a label from.
	 */
	public void setFallbackTitle(String fallbackTitle) {
		_fallbackTitle = fallbackTitle;
	}

	/**
	 * Sets whether the table renders framed: with the bound attribute's label as panel title and
	 * row creation as an Add button in its own toolbar. A frameless table shows neither; its
	 * creator offers row creation through the enclosing command scope.
	 *
	 * <p>
	 * A framed table is a field among others: its panel is sized by its content and ends the fill
	 * chain, so the table shows all its rows and the enclosing form or dialog scrolls. A frameless
	 * table is a region of its own: it fills the height its container offers, so the table bounds
	 * its scroll viewport and scrolls internally.
	 * </p>
	 */
	public void setFramed(boolean framed) {
		_framed = framed;
		updateFill();
	}

	/**
	 * Pushes the panel's {@link ReactPanelControl#FILL} state for the current {@link #setFramed
	 * framed} setting: a framed table is content-sized, a frameless one fills.
	 */
	private void updateFill() {
		putState(ReactPanelControl.FILL, Boolean.valueOf(!_framed));
	}

	/**
	 * Configures the detail dialog opened from each row's detail button. Without it, no detail
	 * column is shown.
	 */
	public void setDetailDialog(CompositionTableElement.DetailDialogConfig detailDialogConfig) {
		_detailDialogConfig = detailDialogConfig;
	}

	/**
	 * Enables column personalization (order, widths) under the given key.
	 */
	public void setPersonalization(ViewStateStore store, TableId tableId) {
		_store = store;
		_tableId = tableId;
	}

	/**
	 * Offers named filters: the criteria the table declares, materialized over its columns by the
	 * given function, plus the ones the user saves themselves.
	 *
	 * @param declaredFilters
	 *        Materializes the declared filters over the table's columns, called whenever the columns
	 *        are rebuilt and whenever the rows are refreshed - the criteria a table declares can
	 *        depend on the inputs it is refreshed for. It is given the names of the columns the
	 *        access rights of the user withhold now, see {@link ColumnExistence#withheldColumns()},
	 *        on which no declared filter may filter.
	 * @param filterStore
	 *        Where the filters the user saves under a name are persisted, or {@code null} to offer
	 *        only the declared ones.
	 * @param initialFilter
	 *        The {@link NamedFilter#id() identifier} of the filter the table is filtered by until a
	 *        personalization of its own exists, or {@code null} to start out unfiltered.
	 */
	public void setNamedFilters(
			BiFunction<List<? extends Column<?, ?>>, Set<String>, List<NamedFilter>> declaredFilters,
			NamedFilterStore filterStore, String initialFilter) {
		_declaredFilters = declaredFilters;
		_filterStore = filterStore;
		_initialFilter = initialFilter;
	}

	/**
	 * Binds the table's filtering two-way to the given channels through a
	 * {@link TableFilterBinding}: the named filter it matches, and the text it searches for.
	 *
	 * <p>
	 * To be called before {@link #init()}: the binding is established for each
	 * {@link TableViewControl} this control builds.
	 * </p>
	 *
	 * @param activePreset
	 *        The channel holding the name of the matched filter, {@code null} to leave it
	 *        unpublished.
	 * @param searchTerm
	 *        The channel holding the searched text, {@code null} to leave it unpublished.
	 */
	public void setFilterChannels(ViewChannel activePreset, ViewChannel searchTerm) {
		_activePresetChannel = activePreset;
		_searchTermChannel = searchTerm;
	}

	/**
	 * Publishes the row objects the table displays, in display order, on the given channel
	 * through a {@link DisplayedRowsBinding}.
	 *
	 * <p>
	 * To be called before {@link #init()}: the binding is established for each
	 * {@link TableViewControl} this control builds.
	 * </p>
	 *
	 * @param displayedRows
	 *        The channel receiving the displayed row objects, {@code null} to leave them
	 *        unpublished.
	 */
	public void setDisplayedRowsChannel(ViewChannel displayedRows) {
		_displayedRowsChannel = displayedRows;
	}

	/**
	 * Whether the table displays its filter bar.
	 *
	 * @see TableViewControl#setFilterBar(boolean)
	 */
	public void setFilterBar(boolean filterBar) {
		_filterBar = filterBar;
	}

	/**
	 * Whether the table tells below its rows how many it has.
	 *
	 * @see TableViewControl#setRowCount(boolean)
	 */
	public void setRowCount(boolean rowCount) {
		_rowCount = rowCount;
	}

	/**
	 * The order the rows are displayed in until the user sorts the table themselves.
	 */
	public void setDefaultSort(SortSpec defaultSort) {
		_defaultSort = defaultSort;
	}

	/**
	 * How many of the leading data columns stay in place while the table is scrolled horizontally.
	 *
	 * <p>
	 * Counted among the data columns: a leading action column (the detail button) is carried along,
	 * because it sits left of them all.
	 * </p>
	 */
	public void setFixedColumns(int fixedColumns) {
		_fixedColumns = fixedColumns;
	}

	/**
	 * Binds the table's selection two-way to the given channel, see
	 * {@link RowArrangement#bindSelection(TableViewControl, ViewChannel)}.
	 *
	 * <p>
	 * To be called before {@link #init()}: the binding is established for each
	 * {@link TableViewControl} this control builds.
	 * </p>
	 */
	public void setSelectionChannel(ViewChannel selectionChannel) {
		_selectionChannel = selectionChannel;
	}

	/**
	 * Sets whether the user may select one row at a time, or any number of them.
	 *
	 * <p>
	 * To be called before {@link #init()}: the mode is part of the initial state of each
	 * {@link TableViewControl} this control builds, and it decides how the selection reaches the
	 * {@link #setSelectionChannel(ViewChannel) selection channel} - one selected row as that row's
	 * object, several as the set of them.
	 * </p>
	 */
	public void setSelectionMode(SelectionMode selectionMode) {
		_selectionMode = selectionMode;
	}

	/**
	 * Enables automatic row refresh outside edit sessions: the row function is re-evaluated when an
	 * observed object or input channel changes.
	 *
	 * <p>
	 * An input change that leaves the rows as they are - a change of a row-independent input, or
	 * of a cell-independent input yielding the same rows - keeps the cells: only the criteria of
	 * the declared filters are resolved again, and the rows are pushed again for their
	 * {@link #setRowCssClass(Function) CSS class} and their {@link #setRowColor(Function) color
	 * role}, where one is set. A change of any other input builds the cells again.
	 * </p>
	 *
	 * @param rowFunction
	 *        Computes the row objects from the values of all input channels, in their order.
	 * @param observedTypes
	 *        Types whose object changes trigger a refresh.
	 * @param inputChannels
	 *        Channels whose values the row function is called with.
	 * @param rowIndependentInputs
	 *        Those of the input channels whose changes cannot change the rows, e.g. the table's
	 *        own selection channel; their changes do not re-evaluate the row function.
	 * @param cellIndependentInputs
	 *        Those of the input channels no cell reads; a change of one of them that yields the
	 *        same rows keeps the cells.
	 * @param cellInputs
	 *        Channels the cells read that are no input channels; a change of one of them builds
	 *        the cells again and filters the rows anew, without computing the rows again.
	 *
	 * @see ListRowArrangement#ListRowArrangement(Function, Set, List, List, List, List)
	 */
	public void setRowRefresh(Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels,
			List<ViewChannel> rowIndependentInputs, List<ViewChannel> cellIndependentInputs,
			List<ViewChannel> cellInputs) {
		_arrangement = new ListRowArrangement(rowFunction, observedTypes, inputChannels, rowIndependentInputs,
			cellIndependentInputs, cellInputs);
	}

	/**
	 * Sets how the rows are arranged: where the {@link RowSource} of each table this control builds
	 * takes them from, how they follow the model outside an edit session, and how the selection is
	 * bound to them.
	 *
	 * <p>
	 * To be called before {@link #init()}, instead of
	 * {@link #setRowRefresh(Function, Set, List, List, List, List)}, which sets a
	 * {@link ListRowArrangement}.
	 * </p>
	 */
	public void setRowArrangement(RowArrangement arrangement) {
		_arrangement = arrangement;
	}

	/**
	 * Sets how the {@link #getAddRowCommand() add command} creates a row, {@link RowCreation#BINDING}
	 * by default.
	 *
	 * <p>
	 * To be called before {@link #init()}.
	 * </p>
	 */
	public void setRowCreation(RowCreation creation) {
		_creation = creation;
	}

	/**
	 * Sets the grouping the table starts with: one collapsible header row per value of the grouped
	 * column, aggregating the other columns over its rows.
	 *
	 * <p>
	 * To be called before {@link #init()}: the grouping is part of the initial state of each
	 * {@link TableViewControl} this control builds, so a grouping the user chose (and which is
	 * persisted under the table's identity) wins over it.
	 * </p>
	 */
	public void setGrouping(GroupSpec grouping) {
		_grouping = grouping;
	}

	/**
	 * Sets what a row activation runs - a double-click on the row, or {@code Enter} while the row
	 * carries the keyboard cursor.
	 *
	 * <p>
	 * To be called before {@link #init()}: the handler is installed on each
	 * {@link TableViewControl} this control builds.
	 * </p>
	 *
	 * @see TableViewControl#setActivationHandler(TableViewControl.ActivationHandler)
	 */
	public void setActivationHandler(TableViewControl.ActivationHandler<TLObject> handler) {
		_activationHandler = handler;
	}

	/**
	 * Sets columns to append behind the ones this control builds itself.
	 *
	 * <p>
	 * The hook for what a caller offers on every row - the per-row buttons of a
	 * {@link com.top_logic.layout.view.table.RowCommandColumn}, say. The columns are appended
	 * whenever the table is (re-)built, so they survive the switch between view and edit mode.
	 * </p>
	 *
	 * <p>
	 * To be called before {@link #init()}.
	 * </p>
	 *
	 * @param columns
	 *        The columns to append, in display order.
	 */
	public void setTrailingColumns(List<? extends Column<TLObject, ?>> columns) {
		_trailingColumns = columns;
	}

	/**
	 * The renderers writing the values of the data columns into an Excel export, by column name; a
	 * column without an entry is written by the default renderer.
	 *
	 * <p>
	 * Follows the columns the table is currently built with, which differ between view and edit
	 * mode.
	 * </p>
	 */
	public Map<String, ExcelCellRenderer> getExportRenderers() {
		return ColumnDeclarations.exportRenderers(_dataSetups);
	}

	/**
	 * Sets the function computing the CSS class of a data row from its row object.
	 *
	 * <p>
	 * To be called before {@link #init()}: the function is installed on each
	 * {@link DefaultTableView} this control builds, so it survives the switch between view and
	 * edit mode.
	 * </p>
	 *
	 * @param rowCssClass
	 *        The function answering the class of a row object, or {@code null} for none.
	 *
	 * @see DefaultTableView#setRowCssClass(Function)
	 */
	public void setRowCssClass(Function<? super TLObject, String> rowCssClass) {
		_rowCssClass = rowCssClass;
	}

	/**
	 * Sets the function computing the color role of a data row from its row object.
	 *
	 * <p>
	 * To be called before {@link #init()}: the function is installed on each
	 * {@link DefaultTableView} this control builds, so it survives the switch between view and
	 * edit mode.
	 * </p>
	 *
	 * @param rowColor
	 *        The function answering the role of a row object, or {@code null} for none.
	 *
	 * @see DefaultTableView#setRowColor(Function)
	 */
	public void setRowColor(Function<? super TLObject, ValueColor> rowColor) {
		_rowColor = rowColor;
	}

	/**
	 * The inner {@link TableViewControl}, or {@code null} if not yet initialized.
	 */
	public TableViewControl<TLObject> getTableControl() {
		return _tableControl;
	}

	/**
	 * Activates the row displaying the given object in the current inner table, as a double-click
	 * on that row does: the row becomes the selection, then the
	 * {@link #setActivationHandler(TableViewControl.ActivationHandler) activation handler} runs.
	 *
	 * @param row
	 *        The object of the row to activate.
	 * <p>
	 * Before {@link #init()} or when no row displays the object, nothing happens.
	 * </p>
	 *
	 * @see TableViewControl#activate(Object)
	 */
	public void activate(TLObject row) {
		TableViewControl<TLObject> table = _tableControl;
		if (table == null) {
			return;
		}
		table.activate(row);
	}

	/**
	 * Adds a new row to the table, see {@link #getAddRowCommand()}.
	 */
	@ReactCommandHandler(CMD_ADD_ROW)
	void handleAddRow(ReactContext context) {
		_addRowCommand.executeCommand(context);
	}

	/**
	 * The command adding a row to the table while it is edited.
	 *
	 * <p>
	 * The command is offered while the table creates rows in the running session (see
	 * {@link RowCreation#offersCreation(RowSetEditSession)}), and executable while the
	 * {@link #createTypes() types offered for new rows} are not empty. With a single type, it adds
	 * a row of that type. With several, it opens a menu listing the types by their labels, placed
	 * at the button the command was triggered from (see {@link CommandTrigger}), and adds a row of
	 * the type the user chooses. The new row is selected and displayed: the rows holding it are
	 * expanded, see {@link RowArrangement#ancestorKeys(TLObject)}.
	 * </p>
	 *
	 * <p>
	 * A {@link #setFramed framed} table shows the command as Add button in its own toolbar; the
	 * creator of a frameless table offers it through the enclosing command scope.
	 * </p>
	 */
	public CommandModel getAddRowCommand() {
		return _addRowCommand;
	}

	private boolean offersRowCreation() {
		RowSetEditSession session = session();
		return isEditing() && session.isEditable() && _creation.offersCreation(session);
	}

	/**
	 * The types the user may choose from for a new row, for the rows selected now, see
	 * {@link RowCreation#createTypes(RowSetEditSession, List)}; empty while no edit session is
	 * running.
	 */
	@Override
	public List<TLClass> createTypes() {
		if (!isEditing()) {
			return List.of();
		}
		return _creation.createTypes(session(), selectedRows());
	}

	/**
	 * The row objects of the selected rows, in the order of their selection.
	 */
	private List<TLObject> selectedRows() {
		List<TLObject> rows = new ArrayList<>(_selectedKeys.size());
		for (Object selected : _selectedKeys) {
			TLObject row = _arrangement.row(selected);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	/**
	 * The command removing all selected rows from the table while it is edited, with the
	 * {@link RowSetBinding#getRemoveMode() remove semantics} the per-row delete buttons apply.
	 *
	 * <p>
	 * The command is offered in edit mode of a table that removes rows and lets the user
	 * {@link SelectionMode#MULTI select several rows} (the header checkbox selects all of them);
	 * it is executable while a row is selected. Like the {@link #getAddRowCommand() add-row
	 * command}, a {@link #setFramed framed} table shows it in its own toolbar, the creator of a
	 * frameless table offers it through the enclosing command scope.
	 * </p>
	 */
	public CommandModel getRemoveSelectedRowsCommand() {
		return _removeSelectedRowsCommand;
	}

	/**
	 * Whether the table offers the {@link #getRemoveSelectedRowsCommand() removal of the selected
	 * rows}.
	 */
	public boolean offersSelectedRowsRemoval() {
		return isEditable() && _selectionMode == SelectionMode.MULTI
			&& binding().getRemoveMode() != RowSetBinding.RemoveMode.NONE;
	}

	private void removeSelectedRows() {
		if (!offersSelectedRowsRemoval()) {
			return;
		}
		removeRows(selectedRows());
	}

	private void addRowOfChosenType(ReactContext context) {
		List<TLClass> types = createTypes();
		if (types.size() <= 1) {
			if (!types.isEmpty()) {
				addRowOfType(types.get(0));
			}
			return;
		}

		ContextMenuOpener opener = context != null ? context.getContextMenuOpener() : null;
		if (opener == null) {
			opener = _context.getContextMenuOpener();
		}
		if (opener == null) {
			Logger.warn("No context menu overlay available to choose the type of a new row.",
				RowSetTableControl.class);
			return;
		}

		List<CommandModel> choices = new ArrayList<>(types.size());
		for (TLClass type : types) {
			choices.add(SimpleCommandModel.create(null, MetaLabelProvider.INSTANCE.getLabel(type), ctx -> {
				addRowOfType(type);
			}));
		}
		// Without a triggering button, the menu hangs off the table.
		String trigger = CommandTrigger.currentId();
		String anchorId = trigger != null ? trigger : getID();
		opener.open(anchorId, List.of(new Targeted(new ContextMenuContribution(target -> {
			// The entries carry their type, nothing to publish.
		}, choices), null)), null);
	}

	/**
	 * Adds a new transient row of the only type offered for new rows to the table and selects it,
	 * so it is immediately editable under the {@link RowEditPolicy#SELECTED} policy.
	 */
	@Override
	public TLObject addRow() {
		List<TLClass> types = createTypes();
		return types.size() == 1 ? addRowOfType(types.get(0)) : null;
	}

	/**
	 * Adds a new transient row of the given type to the table, as the {@link RowCreation} of the
	 * table does for the selected rows, and selects it, so it is immediately editable under the
	 * {@link RowEditPolicy#SELECTED} policy.
	 *
	 * @return The new row, {@code null} if the type is none of the {@link #createTypes() types
	 *         offered} for the selected rows.
	 */
	@Override
	public TLObject addRowOfType(TLClass type) {
		if (!createTypes().contains(type)) {
			return null;
		}
		return selected(_creation.addRow(session(), type, selectedRows()));
	}

	private TLObject selected(TLObject newRow) {
		if (newRow != null && _tableControl != null) {
			_tableControl.expandRows(_arrangement.ancestorKeys(newRow));
			_tableControl.selectRow(_arrangement.key(newRow));
		}
		return newRow;
	}

	@Override
	public void onFormStateChanged(FormModel source) {
		super.onFormStateChanged(source);
		// The types offered for new rows and the removal of rows follow the edit session.
		_addRowCommand.fireStateChanged();
		_removeSelectedRowsCommand.fireStateChanged();
	}

	/**
	 * Sets the panel title: the bound attribute's display label if available, else the fallback
	 * title. A frameless table shows no title.
	 */
	private void updateTitle() {
		if (!_framed) {
			putState("title", null);
			return;
		}
		String title = _fallbackTitle;
		TLStructuredTypePart boundPart = binding().getBoundPart();
		if (boundPart != null) {
			String label = MetaLabelProvider.INSTANCE.getLabel(boundPart);
			if (label != null) {
				title = label;
			}
		}
		putState("title", title);
	}

	// -- Table Building --

	@Override
	protected void buildContent(List<? extends TLObject> rowObjects, boolean editMode) {
		updateTitle();

		List<Column<TLObject, ?>> columns = new ArrayList<>();

		// Detail action column (first, when a detail dialog is configured). Action columns carry no
		// header label - it would not fit their narrow width; the buttons in the cells label the
		// action themselves.
		if (_detailDialogConfig != null) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DETAIL, row -> row)
				.label(ResKey.text(""))
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDetailButton(ctx, row))))
				.width(ACTION_COLUMN_WIDTH)
				.frozenEligible(false)
				// Holds a per-row button, not data: nothing to offer in the column selection.
				.selectable(false)
				.build());
		}

		List<ColumnSetup> setups = new ArrayList<>(_columns.size());
		columns.addAll(createDataColumns(editMode, isEditing(), setups));
		_hiddenByDefault = ColumnDeclarations.hiddenByDefault(setups);
		_dataSetups = setups;

		// Removal action column (edit mode only, when the binding supports removal, no header label -
		// see detail column). Like every per-row command column it is pinned to the end of the
		// table: it keeps its width, and the last data column takes the room the columns leave.
		if (editMode && binding().getRemoveMode() != RowSetBinding.RemoveMode.NONE) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DELETE, row -> row)
				.label(ResKey.text(""))
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDeleteButton(ctx, row))))
				.width(ACTION_COLUMN_WIDTH)
				.pinnedEnd(true)
				.build());
		}

		columns.addAll(_trailingColumns);

		// Create or replace the row source and table control (column set may change between
		// edit/view mode).
		_cellDependencies.clear();
		RowSource<TLObject> source =
			_arrangement.createSource(rowObjects, isEditing() ? session() : null, columns);
		TableViewState initialState =
			DefaultTableView.initialState(columns, _defaultSort, _hiddenByDefault);
		initialState.setGrouping(_grouping);
		initialState.setSelection(Selection.none(_selectionMode));
		if (_fixedColumns > 0) {
			// The configured number counts data columns; a leading action column has to be added on
			// top of it, or freezing "the first two columns" would freeze the detail button and one
			// data column.
			int leadingActions = 0;
			for (Column<TLObject, ?> column : columns) {
				if (column.selectable()) {
					break;
				}
				leadingActions++;
			}
			initialState.setFrozenCount(Math.min(_fixedColumns + leadingActions, columns.size()));
		}
		// The columns that do not exist must not be displayed from the first state on, and no
		// declared filter may refer to a column withheld from the user.
		ColumnExistence existence = ColumnExistence.of(setups);
		List<NamedFilter> declaredFilters =
			_declaredFilters == null ? List.of() : _declaredFilters.apply(columns, existence.withheldColumns());
		DefaultTableView<TLObject> view = new DefaultTableView<>(columns, source, initialState, _store,
			_tableId, _hiddenByDefault, declaredFilters, _filterStore, _initialFilter);
		view.setRowCssClass(_rowCssClass);
		view.setRowColor(_rowColor);
		view.setAbsentColumns(existence.absentColumns());

		disposeExistence();
		_arrangement.release();
		disposeFilterBinding();
		disposeDisplayedRowsBinding();
		if (_tableControl != null) {
			_tableControl.cleanupTree();
		}
		_tableControl = new TableViewControl<>(_context, view, view.hierarchy() != null);
		TableViewControl<TLObject> shown = _tableControl;
		_columnRefresh = () -> refreshColumnsAndDeclaredFilters(existence, view, columns);
		_existence = existence.observe(absent -> {
			// A notification in progress may still reach the observation of a replaced table.
			if (_tableControl == shown) {
				shown.setAbsentColumns(absent);
			}
		});
		_tableControl.setFilterBar(_filterBar);
		_tableControl.setRowCount(_rowCount);
		_tableControl.setActivationHandler(_activationHandler);

		// Set panel child to the table.
		putState("child", _tableControl);

		_tableControl.addSelectionListener(this::handleSelectionChanged);
		// The new table selects what the table before selected as far as it has these rows - the
		// rows of a tree are keyed alike in view and edit mode -, so that the rows editable under
		// the SELECTED policy are the ones displayed as selected. The selection channel bound next
		// has the last word.
		_tableControl.selectRows(new ArrayList<>(_selectedKeys));
		_arrangement.bindSelection(_tableControl, _selectionChannel);
		if (_activePresetChannel != null || _searchTermChannel != null) {
			_filterBinding =
				new TableFilterBinding(_tableControl, _activePresetChannel, _searchTermChannel);
		}
		if (_displayedRowsChannel != null) {
			_displayedRowsBinding = new DisplayedRowsBinding(view, _displayedRowsChannel);
		}

		// Let each column contribute any per-session UI (e.g. a custom filter dialog).
		for (ColumnSetup setup : setups) {
			setup.binding().installUI(setup, _tableControl);
		}

		// Refresh rows on model or input changes - but only outside edit sessions: while editing,
		// the row set is frozen so edited rows (overlays) stay in place until save or cancel, and
		// changes only as the arrangement computes it from the edited values.
		TableViewControl<TLObject> table = _tableControl;
		RowArrangement.Display display = new RowArrangement.Display() {
			@Override
			public void rowsReplaced() {
				// The criteria a table declares can depend on the inputs it is refreshed for, so
				// they are resolved again for their new values; whether a column exists may depend
				// on the access rights of the user, which change with the model.
				refreshColumnsAndDeclaredFilters(existence, view, columns);
				_cellDependencies.clear();
				table.refreshData();
			}

			@Override
			public void inputsChanged() {
				// The rows and their cells stay; what is computed from the inputs apart from the
				// rows is computed again.
				if (refreshColumnsAndDeclaredFilters(existence, view, columns)) {
					// Pushes the rows again as well, for their row class and role.
					table.refreshFilters();
				} else if (_rowCssClass != null || _rowColor != null) {
					table.repaintRows();
				}
			}
		};
		if (isEditing()) {
			_arrangement.followSession(display);
		} else {
			_arrangement.observe(display);
			if (isAttached()) {
				_arrangement.attach(_context.getModelScope());
			}
		}

		// Update toolbar: a framed table offers its row commands in edit mode in its own toolbar.
		updateToolbar(_framed && editMode);
		_addRowCommand.fireStateChanged();
		_removeSelectedRowsCommand.fireStateChanged();
	}

	/**
	 * Asks again which columns exist, see {@link ColumnExistence#recheck()}, and resolves the
	 * criteria of the declared filters again for the current input values and the columns withheld
	 * from the user now.
	 *
	 * @return Whether the table declares filters, so that they were replaced on the given view.
	 */
	private boolean refreshColumnsAndDeclaredFilters(ColumnExistence existence, DefaultTableView<TLObject> view,
			List<Column<TLObject, ?>> columns) {
		existence.recheck();
		if (_declaredFilters == null) {
			return false;
		}
		view.setDeclaredFilters(_declaredFilters.apply(columns, existence.withheldColumns()));
		return true;
	}

	/**
	 * Builds the data columns for the current mode, resolving the declarations against the
	 * {@link #setRowTypes(List) row types}, or the binding's current row type (the bound attribute
	 * may resolve only once the form has an object).
	 *
	 * @param editMode
	 *        Whether the rows may be changed, so that editable cells render their inputs.
	 * @param edited
	 *        Whether the rows are edited, so that read-only cells show the pending changes.
	 * @param setups
	 *        Collects the resolved {@link ColumnSetup}s, for per-session UI installation.
	 */
	private List<Column<TLObject, ?>> createDataColumns(boolean editMode, boolean edited, List<ColumnSetup> setups) {
		List<Column<TLObject, ?>> columns = new ArrayList<>(_columns.size());
		ColumnResolution scope = _rowTypes.isEmpty() ? new ColumnResolution(binding().getRowType(), _context)
			: new ColumnResolution(_rowTypes, _context);
		List<ColumnDeclaration> declarations =
			_columns.isEmpty() ? ColumnDeclarations.mainColumns(scope.rowType()) : _columns;
		for (ColumnSetup setup : ColumnDeclarations.resolve(declarations, scope)) {
			setups.add(setup);
			columns.add(adapt(setup.buildColumn(), setup, editMode, edited));
		}
		return columns;
	}

	private <V> Column<TLObject, V> adapt(Column<Object, V> inner, ColumnSetup setup, boolean editable,
			boolean edited) {
		return new EditAwareColumn<>(inner, setup, editable, edited);
	}

	@Override
	protected void refreshRows() {
		if (!isEditing()) {
			return;
		}
		_arrangement.setRows(fieldModel().getCurrentList());
		if (_tableControl != null) {
			_columnRefresh.run();
			_cellDependencies.clear();
			_tableControl.refreshData();
		}
	}

	/**
	 * Whether the given row is currently editable: in edit mode, all rows under
	 * {@link RowEditPolicy#ALL}, exactly the selected row under {@link RowEditPolicy#SELECTED}.
	 */
	private boolean isRowEditable(TLObject row) {
		if (_policy == RowEditPolicy.SELECTED) {
			return _selectedKeys.contains(_arrangement.key(row));
		}
		return true;
	}

	private void handleSelectionChanged(Set<Object> selectedKeys) {
		// Rows whose selection state flips change their editability under the SELECTED policy, so
		// their cells must re-render.
		Set<Object> flipped = new java.util.LinkedHashSet<>(_selectedKeys);
		Set<Object> unchanged = new HashSet<>(flipped);
		unchanged.retainAll(selectedKeys);
		flipped.addAll(selectedKeys);
		flipped.removeAll(unchanged);

		_selectedKeys.clear();
		_selectedKeys.addAll(selectedKeys);
		_removeSelectedRowsCommand.fireStateChanged();
		// The types offered for a new row may depend on the selected rows.
		_addRowCommand.fireStateChanged();

		if (_policy == RowEditPolicy.SELECTED && isEditing() && _tableControl != null
			&& !flipped.isEmpty()) {
			_tableControl.invalidateRowCells(flipped);
		}
	}

	/**
	 * Stops following the conditions deciding which columns of the current table exist.
	 */
	private void disposeExistence() {
		_existence.dispose();
		_existence = Registration.NONE;
		_columnRefresh = () -> {
			// No table.
		};
	}

	/**
	 * Detaches the {@link TableFilterBinding} from the table it was created for.
	 */
	private void disposeFilterBinding() {
		if (_filterBinding != null) {
			_filterBinding.dispose();
			_filterBinding = null;
		}
	}

	/**
	 * Detaches the {@link DisplayedRowsBinding} from the table it was created for.
	 */
	private void disposeDisplayedRowsBinding() {
		if (_displayedRowsBinding != null) {
			_displayedRowsBinding.dispose();
			_displayedRowsBinding = null;
		}
	}

	/**
	 * Shows the commands changing the rows of a framed table in its toolbar: the
	 * {@link #getAddRowCommand() add-row command} and the
	 * {@link #getRemoveSelectedRowsCommand() removal of the selected rows}, as far as the table
	 * offers them.
	 */
	private void updateToolbar(boolean showRowCommands) {
		if (_toolbar != null) {
			_toolbar.cleanupTree();
			_toolbar = null;
		}

		List<ReactControl> buttons = new ArrayList<>();
		if (showRowCommands) {
			if (offersRowCreation()) {
				buttons.add(_addRowCommand.createButton(_context));
			}
			if (offersSelectedRowsRemoval()) {
				buttons.add(_removeSelectedRowsCommand.createButton(_context));
			}
		}
		if (buttons.isEmpty()) {
			putState("toolbar", null);
		} else {
			// TLPanel renders a single `toolbar` child control (a TLToolbar), so wrap the buttons in
			// a toolbar rather than pushing a bare button list.
			_toolbar = new ReactToolbarControl(_context);
			_toolbar.addGroup("rows", ToolbarGroupDisplay.INLINE, null, null, buttons);
			putState("toolbar", _toolbar);
		}
	}

	private ReactButtonControl createDetailButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DETAIL);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			openDetailDialog(rowObject);
		});
		button.setImage(Icons.COMPOSITION_TABLE_DETAIL);
		button.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		return button;
	}

	/**
	 * Opens the detail dialog for the given row object.
	 *
	 * <p>
	 * Follows the {@link com.top_logic.layout.view.command.OpenDialogAction} pattern: loads the
	 * configured view XML, creates a fresh {@link ViewContext}, injects channels for the row object
	 * and edit mode, and opens the dialog via {@link DialogManager}.
	 * </p>
	 *
	 * @param rowObject
	 *        The row object (the overlay it is edited in) to display in the dialog.
	 */
	private void openDetailDialog(Object rowObject) {
		if (_detailDialogConfig == null) {
			Logger.info("No detail dialog configured for row-set table.", RowSetTableControl.class);
			return;
		}

		DialogManager mgr = _context.getDialogManager();
		if (mgr == null) {
			Logger.warn("No DialogManager available, cannot open detail dialog.", RowSetTableControl.class);
			return;
		}

		// Load the dialog view from the configured layout path.
		String viewPath = _detailDialogConfig.getLayout().fullPath();
		ViewElement dialogView;
		try {
			dialogView = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load detail dialog view: " + viewPath, ex);
		}

		// Create a fresh ViewContext for the dialog scope.
		ViewContext dialogContext = new DefaultViewContext(_context);
		List<ViewChannel> dialogChannels = new ArrayList<>();

		// Inject the row object into the configured input channel.
		String inputChannelName = _detailDialogConfig.getInputChannel();
		if (inputChannelName != null && rowObject != null) {
			DefaultViewChannel inputChannel = new DefaultViewChannel(inputChannelName);
			inputChannel.set(rowObject);
			dialogContext.registerChannel(inputChannelName, inputChannel);
			dialogChannels.add(inputChannel);
		}

		// Inject the edit mode state into the configured edit-mode channel.
		String editModeChannelName = _detailDialogConfig.getEditModeChannel();
		if (editModeChannelName != null && !editModeChannelName.isEmpty()) {
			DefaultViewChannel editModeChannel = new DefaultViewChannel(editModeChannelName);
			// A table that is displayed view only while the form is edited opens its rows view only.
			editModeChannel.set(Boolean.valueOf(isEditable()));
			dialogContext.registerChannel(editModeChannelName, editModeChannel);
			dialogChannels.add(editModeChannel);
		}

		// Create the dialog control tree from the view definition.
		ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);
		ObservingChannels.observeWhileDisplayed(dialogControl, dialogChannels, dialogContext);

		// Open the dialog.
		mgr.openDialog(false, dialogControl, result -> {
			// After dialog close, refresh the row's cell models.
			// The dialog's store-form-state may have applied changes to the row overlay
			// without going through the cell's AttributeFieldModel.setValue(), so the
			// cached values in the cell models may be stale.
			if (rowObject instanceof TLObject row) {
				CompositionRowModel rowModel = findRowModel(row);
				if (rowModel != null) {
					rowModel.refreshColumnModels();
				}
			}
		});
	}

	/**
	 * Renders a read-only cell of the given row of the edited table as the objects it shows are
	 * currently edited.
	 *
	 * <p>
	 * A cell shows stored objects, also when they are edited in the same form as rows of this
	 * table: the cell of a container showing its parts, say. The cell is built in a
	 * {@link TracingScope} over the overlays of the form, which answers the read of an edited
	 * object from its buffer, so the cell shows the pending changes. The scope records the
	 * attribute values read, so a later change of one of them through the cell of another row
	 * builds the cell again, see {@link #rowValueChanged(TLObject, TLStructuredTypePart)}. A cell
	 * control built later on, when the cell is displayed, is built in such a scope as well.
	 * </p>
	 */
	private CellContent renderAsEdited(TLObject row, Supplier<CellContent> render) {
		OverlayLookup overlays = formControl().getValidationModel();
		if (overlays == null) {
			return render.get();
		}
		CellContent content = traced(row, overlays, render);
		if (content instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
			return new CellContent.Raw((CellControlFactory) context -> traced(row, overlays, () -> factory.create(context)),
				raw.tooltip());
		}
		return content;
	}

	private <T> T traced(TLObject row, OverlayLookup overlays, Supplier<T> render) {
		Set<Pointer> dependencies = _cellDependencies.computeIfAbsent(row, x -> new HashSet<>());
		try (TracingScope scope = TracingScope.open(
			pointer -> dependencies.add(Pointer.create(editedObject(pointer.object()), pointer.attribute())),
			overlays)) {
			return render.get();
		}
	}

	/**
	 * The object the given object edits: the base of a buffer, the object itself otherwise.
	 */
	private static TLObject editedObject(TLObject object) {
		return object instanceof TLObjectOverlay overlay ? overlay.getBase() : object;
	}

	/**
	 * Builds the read-only cells of the rows again that were built from the changed value, so they
	 * show it; the cells of rows being edited are kept, so their inputs stay as they are.
	 *
	 * <p>
	 * The cells are built again when the interaction has completed
	 * ({@link DeliveryScope#afterInteraction(Runnable)}): the change is reported while the value
	 * entered is still being applied, and the input it was entered in takes it into its state only
	 * afterwards. Pushing the rows earlier would send that input with the value it had before.
	 * </p>
	 */
	@Override
	protected void rowValueChanged(TLObject row, TLStructuredTypePart part) {
		if (_tableControl == null) {
			return;
		}
		boolean scheduled = !_staleRows.isEmpty();
		addDependentRows(row, part);
		scheduleRebuild(scheduled);
	}

	/**
	 * Builds the read-only cells of the changed row again, and those of the rows that were built
	 * from the changed values, see {@link #rowValueChanged(TLObject, TLStructuredTypePart)}.
	 */
	@Override
	protected void rowChanged(TLObject row, Collection<TLStructuredTypePart> parts) {
		if (_tableControl == null) {
			return;
		}
		boolean scheduled = !_staleRows.isEmpty();
		_staleRows.add(row);
		for (TLStructuredTypePart part : parts) {
			addDependentRows(row, part);
		}
		scheduleRebuild(scheduled);
	}

	private void addDependentRows(TLObject row, TLStructuredTypePart part) {
		Pointer changed = Pointer.create(editedObject(row), part);
		for (Map.Entry<TLObject, Set<Pointer>> entry : _cellDependencies.entrySet()) {
			TLObject dependent = entry.getKey();
			if (dependent != row && entry.getValue().contains(changed)) {
				_staleRows.add(dependent);
			}
		}
	}

	private void scheduleRebuild(boolean scheduled) {
		if (!scheduled && !_staleRows.isEmpty()) {
			DeliveryScope.afterInteraction(this::rebuildStaleRows);
		}
	}

	private void rebuildStaleRows() {
		List<Object> stale = new ArrayList<>();
		for (TLObject row : _staleRows) {
			if (!isRowEditable(row)) {
				// Recorded anew when the cells are built again.
				_cellDependencies.remove(row);
				stale.add(_arrangement.key(row));
			}
		}
		_staleRows.clear();
		if (_tableControl != null && !stale.isEmpty()) {
			_tableControl.invalidateRowCells(stale);
		}
	}

	private ReactButtonControl createDeleteButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DELETE);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			if (rowObject instanceof TLObject row) {
				removeRow(row);
			}
		});
		button.setImage(Icons.COMPOSITION_TABLE_DELETE);
		button.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		return button;
	}

	@Override
	protected void onCleanup() {
		_arrangement.release();
		disposeExistence();
		disposeFilterBinding();
		disposeDisplayedRowsBinding();
		// The toolbar and the table itself are part of the state and are disposed with it; only the
		// references are dropped here, so a trailing event cannot reach a torn-down control.
		_toolbar = null;
		_tableControl = null;
		_cellDependencies.clear();
		_staleRows.clear();
		super.onCleanup();
	}

	/**
	 * Adapts a type-derived {@link Column} (rows typed {@code Object}) to the {@link TLObject} row
	 * type of this table, rendering cells editable when the enclosing form edits and the
	 * {@link RowEditPolicy} covers the row, and read-only cells exactly as the column built from its
	 * {@link ColumnSetup} renders them - with the display, the tooltip, the CSS class and the color
	 * role it declares, and the view-mode field display of the values' type otherwise (so value
	 * types keep their interactive display, e.g. a download link).
	 */
	private final class EditAwareColumn<V> implements Column<TLObject, V> {

		private final Column<Object, V> _inner;

		private final ColumnSetup _setup;

		/** Whether the rows may be changed, so that an editable cell renders its input. */
		private final boolean _editable;

		/**
		 * Whether the rows are edited, so that a read-only cell shows the pending changes, also
		 * while the rows may not be changed.
		 */
		private final boolean _edited;

		EditAwareColumn(Column<Object, V> inner, ColumnSetup setup, boolean editable, boolean edited) {
			_inner = inner;
			_setup = setup;
			_editable = editable;
			_edited = edited;
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
			if (_editable && row != null && isRowEditable(row) && offersEdit(row)) {
				return new CellContent.Raw((CellControlFactory) context -> {
					ReactControl editControl = buildEditCellControl(context, row, _setup);
					return editControl != null
						? editControl
						: CellContentReactAdapter.toControl(context, _inner.renderCell(row));
				});
			}
			if (_edited && row != null) {
				return renderAsEdited(row, () -> _inner.renderCell(row));
			}
			return _inner.renderCell(row);
		}

		@Override
		public String searchText(TLObject row) {
			return _inner.searchText(row);
		}

		/**
		 * Whether the column offers an edit of the given row's cell.
		 */
		private boolean offersEdit(TLObject row) {
			CellEditing editing = _setup.editing();
			return editing != null && editing.canEdit(row);
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
		public boolean exportable() {
			return _inner.exportable();
		}

		@Override
		public Object exportValue(TLObject row) {
			return _inner.exportValue(row);
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
		public ValueColor color(TLObject row) {
			return _inner.color(row);
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
