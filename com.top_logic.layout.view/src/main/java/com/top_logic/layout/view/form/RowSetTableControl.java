/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.table.CellContentReactAdapter;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
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
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Table presentation of a row set edited within a form.
 *
 * <p>
 * Renders as a {@code TLPanel} React component with a {@link TableViewControl} as content. The
 * editing lifecycle (row overlays, per-cell field models, validation, participation in the form's
 * save) is inherited from {@link AbstractCompositionControl}; the membership semantics (row
 * objects, add, remove, commit) come from the configured {@link RowSetBinding}.
 * </p>
 *
 * <p>
 * Data columns are derived from the model per attribute (sortable, filterable, cells displayed
 * through the attribute's view-mode field display). While the form is in edit mode, the cells of
 * rows covered by the {@link RowEditPolicy} render the attribute's editable field control instead.
 * An action column for row removal is appended in edit mode when the binding supports removal; a
 * detail-open column is prepended when a {@link #setDetailDialog detail dialog} is configured.
 * </p>
 *
 * <p>
 * A {@link #setFramed framed} table shows the bound attribute's label as panel title and offers row
 * creation as an Add button in its own toolbar; a frameless table shows neither, and its creator
 * offers row creation through the enclosing command scope instead. Optional wiring - column
 * personalization, a two-way selection channel, and automatic row refresh on model or input changes
 * - is configured through setters before {@link #init()}. While an edit session is running, the row
 * set is frozen (no automatic re-query), so edited rows stay in place; after save or cancel the rows
 * are re-read.
 * </p>
 */
public class RowSetTableControl extends AbstractCompositionControl {

	/** Column name for the detail-open action column. */
	static final String COLUMN_DETAIL = "_detail";

	/** Column name for the row-removal action column. */
	static final String COLUMN_DELETE = "_delete";

	/** Command name for {@link #handleAddRow()}. */
	private static final String CMD_ADD_ROW = "addRow";

	/**
	 * A data column of a row-set table.
	 *
	 * @param attribute
	 *        The model attribute name.
	 * @param readonly
	 *        Whether the column stays read-only in edit mode.
	 * @param binding
	 *        The strategy turning the attribute into a runtime column (sort, filter, display).
	 */
	public record TableColumn(String attribute, boolean readonly, ColumnBinding binding) {
		// Pure data carrier.
	}

	private final ViewContext _context;

	private final List<TableColumn> _columns;

	private final RowEditPolicy _policy;

	private String _fallbackTitle;

	private boolean _framed = true;

	private CompositionTableElement.DetailDialogConfig _detailDialogConfig;

	private ViewStateStore _store;

	private TableId _tableId;

	private ViewChannel _selectionChannel;

	private ViewChannel.ChannelListener _selectionChannelListener;

	private Function<Object[], Collection<?>> _rowFunction;

	private Set<TLStructuredType> _observedTypes = Set.of();

	private List<ViewChannel> _inputChannels = List.of();

	private TableViewControl<TLObject> _tableControl;

	private ListRowSource<TLObject> _rowSource;

	private RowSourceObserver<TLObject> _observer;

	/** Whether the first client write happened (the point from which observers attach directly). */
	private boolean _written;

	/** Guard breaking the notification cycle between selection channel and table selection. */
	private boolean _applyingFromChannel;

	private final Set<Object> _selectedKeys = new java.util.LinkedHashSet<>();

	/** The Add button in the toolbar, or {@code null} if not in edit mode. */
	private ReactButtonControl _addButton;

	/** The panel toolbar holding the Add button, or {@code null} if not in edit mode. */
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
	 *        The data columns to display and edit.
	 * @param policy
	 *        Which rows are editable while the form is in edit mode.
	 */
	public RowSetTableControl(ViewContext context, FormControl formControl, RowSetBinding binding,
			List<TableColumn> columns, RowEditPolicy policy) {
		super(context, formControl, binding, "TLPanel");
		_context = context;
		_columns = columns;
		_policy = policy;

		// Row-set tables should span the full form row.
		putState("fullLine", Boolean.TRUE);

		addBeforeWriteAction(() -> {
			_written = true;
			attachObserver();
		});
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
	 */
	public void setFramed(boolean framed) {
		_framed = framed;
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
	 * Binds the table's selection two-way to the given channel.
	 */
	public void setSelectionChannel(ViewChannel selectionChannel) {
		if (_selectionChannel != null && _selectionChannelListener != null) {
			_selectionChannel.removeListener(_selectionChannelListener);
		}
		_selectionChannel = selectionChannel;
		if (_selectionChannel != null) {
			_selectionChannelListener = (sender, oldValue, newValue) -> reapplySelectionFromChannel();
			_selectionChannel.addListener(_selectionChannelListener);
		}
	}

	/**
	 * Enables automatic row refresh outside edit sessions: the row function is re-evaluated when an
	 * observed object or input channel changes.
	 *
	 * @param rowFunction
	 *        Computes the row objects from the input channel values.
	 * @param observedTypes
	 *        Types whose object changes trigger a refresh.
	 * @param inputChannels
	 *        Channels whose changes trigger a refresh.
	 */
	public void setRowRefresh(Function<Object[], Collection<?>> rowFunction,
			Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels) {
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
	}

	/**
	 * The inner {@link TableViewControl}, or {@code null} if not yet initialized.
	 */
	public TableViewControl<TLObject> getTableControl() {
		return _tableControl;
	}

	/**
	 * Adds a new row to the table.
	 */
	@ReactCommandHandler(CMD_ADD_ROW)
	void handleAddRow() {
		addRow();
	}

	/**
	 * Adds a new transient row of the binding's create type to the table and selects it, so it is
	 * immediately editable under the {@link RowEditPolicy#SELECTED} policy.
	 */
	@Override
	public TLObject addRow() {
		TLObject newRow = super.addRow();
		if (newRow != null && _tableControl != null) {
			_tableControl.selectRow(newRow);
		}
		return newRow;
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
				.width(48)
				.frozenEligible(false)
				.build());
		}

		List<ColumnSetup> setups = new ArrayList<>(_columns.size());
		columns.addAll(createDataColumns(editMode, setups));

		// Removal action column (edit mode only, when the binding supports removal, last, no header
		// label - see detail column).
		if (editMode && binding().getRemoveMode() != RowSetBinding.RemoveMode.NONE) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DELETE, row -> row)
				.label(ResKey.text(""))
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDeleteButton(ctx, row))))
				.width(48)
				.frozenEligible(false)
				.build());
		}

		// Create or replace the row source and table control (column set may change between
		// edit/view mode).
		_rowSource = new ListRowSource<>(new ArrayList<>(rowObjects), columns);
		DefaultTableView<TLObject> view = _store != null
			? DefaultTableView.create(columns, _rowSource, _store, _tableId)
			: DefaultTableView.create(columns, _rowSource);

		if (_tableControl != null) {
			_tableControl.cleanupTree();
		}
		_tableControl = new TableViewControl<>(_context, view, false);
		registerChildControl(_tableControl);

		// Set panel child to the table.
		putState("child", _tableControl);

		_tableControl.setSelectionListener(this::handleSelectionChanged);
		reapplySelectionFromChannel();

		// Let each column contribute any per-session UI (e.g. a custom filter dialog).
		for (ColumnSetup setup : setups) {
			setup.binding().installUI(setup, _tableControl);
		}

		// Refresh rows on model or input changes - but only outside edit sessions: while editing,
		// the row set is frozen so edited rows (overlays) stay in place until save or cancel.
		detachObserver();
		if (_rowFunction != null && !editMode) {
			TableViewControl<TLObject> table = _tableControl;
			_observer = new RowSourceObserver<>(_rowSource, _rowFunction, _observedTypes, _inputChannels,
				() -> {
					table.refreshData();
					reapplySelectionFromChannel();
				});
			if (_written) {
				attachObserver();
			}
		}

		// Update toolbar: a framed table offers an own Add button in edit mode, when the binding
		// offers row creation.
		updateToolbar(_framed && editMode && !binding().getCreateTypes().isEmpty());
	}

	/**
	 * Builds the data columns for the current mode, resolving each attribute against the binding's
	 * current row type (the bound attribute may resolve only once the form has an object).
	 *
	 * @param setups
	 *        Collects the resolved {@link ColumnSetup}s, for per-session UI installation.
	 */
	private List<Column<TLObject, ?>> createDataColumns(boolean editMode, List<ColumnSetup> setups) {
		List<Column<TLObject, ?>> columns = new ArrayList<>(_columns.size());
		TLClass rowType = binding().getRowType();
		for (TableColumn column : _columns) {
			String attribute = column.attribute();
			TLStructuredTypePart part = rowType == null ? null : rowType.getPart(attribute);
			ResKey label = part != null ? TLModelNamingConvention.resourceKey(part) : ResKey.text(attribute);
			ColumnSetup setup = new ColumnSetup(attribute, label, part, _context, column.binding());
			setups.add(setup);
			Column<Object, ?> inner = column.binding().createColumn(setup);
			columns.add(adapt(inner, part, editMode && !column.readonly()));
		}
		return columns;
	}

	private <V> Column<TLObject, V> adapt(Column<Object, V> inner, TLStructuredTypePart part, boolean editable) {
		return new EditAwareColumn<>(inner, part, editable);
	}

	@Override
	protected void refreshRows() {
		if (_rowSource == null || !isEditing()) {
			return;
		}
		_rowSource.setElements(new ArrayList<>(fieldModel().getCurrentList()));
		if (_tableControl != null) {
			_tableControl.refreshData();
		}
	}

	/**
	 * Whether the given row is currently editable: in edit mode, all rows under
	 * {@link RowEditPolicy#ALL}, exactly the selected row under {@link RowEditPolicy#SELECTED}.
	 */
	private boolean isRowEditable(TLObject row) {
		if (_policy == RowEditPolicy.SELECTED) {
			// The row source keys rows by identity, so the selected keys are the row objects.
			return _selectedKeys.contains(row);
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

		if (_policy == RowEditPolicy.SELECTED && isEditing() && _tableControl != null
			&& !flipped.isEmpty()) {
			_tableControl.invalidateRowCells(flipped);
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
		if (_selectionChannel == null || _tableControl == null) {
			return;
		}
		Object value = _selectionChannel.get();
		_applyingFromChannel = true;
		try {
			_tableControl.selectRow(value instanceof Collection ? null : value);
		} finally {
			_applyingFromChannel = false;
		}
	}

	private void attachObserver() {
		if (_observer != null) {
			_observer.attach(_context.getModelScope());
		}
	}

	private void detachObserver() {
		if (_observer != null) {
			_observer.detach();
			_observer = null;
		}
	}

	private void updateToolbar(boolean showAddButton) {
		if (_toolbar != null) {
			_toolbar.cleanupTree();
			_toolbar = null;
			_addButton = null;
		}

		if (showAddButton) {
			String addLabel = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_ADD);
			_addButton = new ReactButtonControl(_context, addLabel, ctx -> {
				addRow();
				return HandlerResult.DEFAULT_RESULT;
			});
			_addButton.setImage(Icons.COMPOSITION_TABLE_ADD);

			// TLPanel renders a single `toolbar` child control (a TLToolbar), so wrap the Add
			// button in a toolbar rather than pushing a bare button list.
			_toolbar = new ReactToolbarControl(_context);
			_toolbar.addGroup("add", ToolbarGroupDisplay.INLINE, null, null, List.of(_addButton));
			putState("toolbar", _toolbar);
		} else {
			putState("toolbar", null);
		}
	}

	private ReactButtonControl createDetailButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DETAIL);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			openDetailDialog(rowObject);
			return HandlerResult.DEFAULT_RESULT;
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
	 *        The row object (overlay or transient) to display in the dialog.
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
		String viewPath = ViewLoader.VIEW_BASE_PATH + _detailDialogConfig.getLayout();
		ViewElement dialogView;
		try {
			dialogView = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load detail dialog view: " + viewPath, ex);
		}

		// Create a fresh ViewContext for the dialog scope.
		ViewContext dialogContext = new DefaultViewContext(_context);

		// Inject the row object into the configured input channel.
		String inputChannelName = _detailDialogConfig.getInputChannel();
		if (inputChannelName != null && rowObject != null) {
			DefaultViewChannel inputChannel = new DefaultViewChannel(inputChannelName);
			inputChannel.set(rowObject);
			dialogContext.registerChannel(inputChannelName, inputChannel);
		}

		// Inject the edit mode state into the configured edit-mode channel.
		String editModeChannelName = _detailDialogConfig.getEditModeChannel();
		if (editModeChannelName != null && !editModeChannelName.isEmpty()) {
			DefaultViewChannel editModeChannel = new DefaultViewChannel(editModeChannelName);
			editModeChannel.set(Boolean.valueOf(formControl().isEditMode()));
			dialogContext.registerChannel(editModeChannelName, editModeChannel);
		}

		// Create the dialog control tree from the view definition.
		ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);

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

	private ReactButtonControl createDeleteButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DELETE);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			if (rowObject instanceof TLObject row) {
				removeRow(row);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		button.setImage(Icons.COMPOSITION_TABLE_DELETE);
		button.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		return button;
	}

	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		detachObserver();
		if (_selectionChannel != null && _selectionChannelListener != null) {
			_selectionChannel.removeListener(_selectionChannelListener);
			_selectionChannelListener = null;
		}
		if (_toolbar != null) {
			_toolbar.cleanupTree();
			_toolbar = null;
			_addButton = null;
		}
		if (_tableControl != null) {
			_tableControl.cleanupTree();
			_tableControl = null;
		}
	}

	/**
	 * Adapts a type-derived {@link Column} (rows typed {@code Object}) to the {@link TLObject} row
	 * type of this table, rendering cells editable when the enclosing form edits and the
	 * {@link RowEditPolicy} covers the row, and read-only cells through the attribute's view-mode
	 * field display (so value types keep their interactive display, e.g. a download link).
	 */
	private final class EditAwareColumn<V> implements Column<TLObject, V> {

		private final Column<Object, V> _inner;

		private final TLStructuredTypePart _part;

		private final boolean _editable;

		EditAwareColumn(Column<Object, V> inner, TLStructuredTypePart part, boolean editable) {
			_inner = inner;
			_part = part;
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
					ReactControl editControl = buildEditCellControl(context, row, name());
					return editControl != null
						? editControl
						: readOnlyControl(context, row);
				});
			}
			if (_part != null && row != null) {
				return new CellContent.Raw((CellControlFactory) context -> readOnlyControl(context, row));
			}
			return _inner.renderCell(row);
		}

		/**
		 * The read-only cell control: the attribute's view-mode field display, or a text fallback.
		 */
		private ReactControl readOnlyControl(ReactContext context, TLObject row) {
			if (_part != null) {
				return FieldControlService.getInstance()
					.createDisplayControl(context, _part, row.tValueByName(name()));
			}
			return new ReactTextControl(context, MetaLabelProvider.INSTANCE.getLabel(row.tValueByName(name())));
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
