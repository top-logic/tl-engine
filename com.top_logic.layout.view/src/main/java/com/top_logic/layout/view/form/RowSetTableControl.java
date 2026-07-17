/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Orchestrator for inline table editing of a bound row set within a form.
 *
 * <p>
 * Manages the lifecycle of a {@link TableViewControl} that displays and edits the row objects of a
 * {@link RowSetBinding}. Implements {@link FormModelListener} to react to form state changes
 * (enter/exit edit mode, object switch); the editing mechanics (row overlays, per-cell field
 * models, validation, participation in the form's save) are handled by a {@link RowSetEditor}.
 * </p>
 *
 * <p>
 * Renders as a {@code TLPanel} React component with the bound attribute's label as title, the
 * table as content, and an Add button in the toolbar (visible only in edit mode, when the binding
 * offers row creation). Action columns for detail-open (always visible) and row removal (edit mode
 * only, when the binding supports removal) are appended to the table columns.
 * </p>
 */
public class RowSetTableControl extends ReactControl implements FormModelListener {

	/** Column name for the detail-open action column. */
	static final String COLUMN_DETAIL = "_detail";

	/** Column name for the row-removal action column. */
	static final String COLUMN_DELETE = "_delete";

	/**
	 * Configuration for a single column in the table.
	 */
	public static class ColumnConfig {

		private final String _attributeName;

		private final boolean _readonly;

		/**
		 * Creates a new column configuration.
		 *
		 * @param attributeName
		 *        The model attribute name for this column.
		 * @param readonly
		 *        Whether the column is always read-only.
		 */
		public ColumnConfig(String attributeName, boolean readonly) {
			_attributeName = attributeName;
			_readonly = readonly;
		}

		/**
		 * The model attribute name.
		 */
		public String getAttributeName() {
			return _attributeName;
		}

		/**
		 * Whether this column is forced read-only regardless of form edit mode.
		 */
		public boolean isReadonly() {
			return _readonly;
		}
	}

	private final ReactContext _context;

	private final FormControl _formControl;

	private final RowSetEditor _editor;

	private final String _fallbackTitle;

	private final List<ColumnConfig> _columnConfigs;

	private final CompositionTableElement.DetailDialogConfig _detailDialogConfig;

	private TableViewControl<TLObject> _tableControl;

	private ListRowSource<TLObject> _rowSource;

	/** The Add button in the toolbar, or {@code null} if not in edit mode. */
	private ReactButtonControl _addButton;

	/** The panel toolbar holding the Add button, or {@code null} if not in edit mode. */
	private ReactToolbarControl _toolbar;

	/**
	 * Creates a new {@link RowSetTableControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics (row objects, add, remove, commit).
	 * @param fallbackTitle
	 *        The panel title used when the binding has no bound attribute to derive a label from.
	 * @param columnConfigs
	 *        The column configurations defining which attributes to display and edit.
	 * @param detailDialogConfig
	 *        Optional configuration for the detail dialog, or {@code null} if no detail dialog is
	 *        configured.
	 */
	public RowSetTableControl(ReactContext context, FormControl formControl, RowSetBinding binding,
			String fallbackTitle, List<ColumnConfig> columnConfigs,
			CompositionTableElement.DetailDialogConfig detailDialogConfig) {
		super(context, null, "TLPanel");
		_context = context;
		_formControl = formControl;
		_editor = new RowSetEditor(formControl, binding);
		_fallbackTitle = fallbackTitle;
		_columnConfigs = columnConfigs;
		_detailDialogConfig = detailDialogConfig;

		// Row-set tables should span the full form row.
		putState("fullLine", Boolean.TRUE);

		formControl.addFormModelListener(this);
	}

	/**
	 * Initializes the table control and sets it as state.
	 *
	 * <p>
	 * Must be called after construction, once column and row information is available from the
	 * form's current object. Called by the element that creates this control.
	 * </p>
	 */
	public void initTable() {
		TLObject currentObject = _formControl.getCurrentObject();
		boolean available = binding().resolve(currentObject);

		putState("title", title());

		if (!available) {
			// No rows can be computed yet — build an empty table so TLPanel always has a valid
			// child.
			buildTable(Collections.emptyList(), false);
			return;
		}

		List<TLObject> rows = binding().readRows(currentObject);
		buildTable(rows, _formControl.isEditMode());
	}

	/**
	 * The inner {@link TableViewControl}, or {@code null} if not yet initialized.
	 */
	public TableViewControl<TLObject> getTableControl() {
		return _tableControl;
	}

	/**
	 * The editor holding the editing state of this table.
	 */
	public RowSetEditor getEditor() {
		return _editor;
	}

	private RowSetBinding binding() {
		return _editor.getBinding();
	}

	/**
	 * The panel title: the bound attribute's display label if available, else the fallback title.
	 */
	private String title() {
		TLStructuredTypePart boundPart = binding().getBoundPart();
		if (boundPart != null) {
			String label = MetaLabelProvider.INSTANCE.getLabel(boundPart);
			if (label != null) {
				return label;
			}
		}
		return _fallbackTitle;
	}

	// -- FormModelListener --

	@Override
	public void onFormStateChanged(FormModel source) {
		TLObject currentObject = source.getCurrentObject();

		if (!binding().resolve(currentObject)) {
			_editor.endEditSession();
			return;
		}

		if (source.isEditMode()) {
			List<TLObject> rows = _editor.beginEditSession();
			buildTable(rows, true);
		} else {
			_editor.endEditSession();
			buildTable(binding().readRows(currentObject), false);
		}
	}

	// -- Add/Remove Row Commands --

	/**
	 * Adds a new row to the table.
	 */
	@ReactCommandHandler("addRow")
	void handleAddRow() {
		addRow();
	}

	/**
	 * Adds a new transient row of the binding's create type to the table.
	 */
	public void addRow() {
		List<TLClass> createTypes = binding().getCreateTypes();
		if (createTypes.isEmpty()) {
			return;
		}
		TLObject newRow = _editor.addRow(createTypes.get(0));
		if (newRow != null) {
			rebuildTableRows();
			onRowAdded(newRow);
		}
	}

	/**
	 * Hook invoked after a new row has been added and the table refreshed. Subclasses e.g. select
	 * the new row.
	 */
	protected void onRowAdded(TLObject newRow) {
		// No follow-up by default.
	}

	/**
	 * Removes a row from the table. The binding's remove semantics are applied when the form is
	 * saved.
	 */
	public void removeRow(TLObject rowObject) {
		if (_editor.removeRow(rowObject)) {
			rebuildTableRows();
		}
	}

	// -- Table Building --

	private void buildTable(List<? extends TLObject> rowObjects, boolean editMode) {
		List<Column<TLObject, ?>> columns = new ArrayList<>();

		// Detail action column (first, when a detail dialog is configured).
		if (_detailDialogConfig != null) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DETAIL, row -> row)
				.label(I18NConstants.COMPOSITION_TABLE_DETAIL)
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDetailButton(ctx, row))))
				.width(48)
				.frozenEligible(false)
				.build());
		}

		columns.addAll(createDataColumns(editMode));

		// Removal action column (edit mode only, when the binding supports removal, last).
		if (editMode && binding().getRemoveMode() != RowSetBinding.RemoveMode.NONE) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DELETE, row -> row)
				.label(I18NConstants.COMPOSITION_TABLE_DELETE)
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDeleteButton(ctx, row))))
				.width(48)
				.frozenEligible(false)
				.build());
		}

		// Create or replace the row source and table control (column set may change between
		// edit/view mode).
		_rowSource = new ListRowSource<>(new ArrayList<>(rowObjects), columns);
		DefaultTableView<TLObject> view = createTableView(columns, _rowSource);

		if (_tableControl != null) {
			_tableControl.cleanupTree();
		}
		_tableControl = new TableViewControl<>(_context, view, false);
		registerChildControl(_tableControl);

		// Set panel child to the table.
		putState("child", _tableControl);

		afterTableBuilt(_tableControl, editMode);

		// Update toolbar: Add button only in edit mode, when the binding offers row creation.
		updateToolbar(editMode && !binding().getCreateTypes().isEmpty());
	}

	/**
	 * Builds the data columns for the current mode.
	 *
	 * <p>
	 * The default implementation creates one column per configured {@link ColumnConfig}, rendering
	 * each cell through {@link #buildDataControl}. Subclasses override to derive columns
	 * differently (e.g. with sort and filter capabilities).
	 * </p>
	 */
	protected List<Column<TLObject, ?>> createDataColumns(boolean editMode) {
		List<Column<TLObject, ?>> columns = new ArrayList<>();
		TLClass rowType = binding().getRowType();
		for (ColumnConfig col : _columnConfigs) {
			String attribute = col.getAttributeName();
			boolean readonly = col.isReadonly();
			columns.add(DefaultColumn.<TLObject, TLObject> builder(attribute, row -> row)
				.label(columnLabel(rowType, attribute))
				.renderer(row -> new CellContent.Raw(
					(CellControlFactory) (ctx -> buildDataControl(ctx, row, attribute, editMode, readonly))))
				.build());
		}
		return columns;
	}

	/**
	 * Creates the {@link DefaultTableView} composing columns and row source.
	 *
	 * <p>
	 * The default creates a view without personalization; subclasses override to attach a
	 * {@link com.top_logic.table.ViewStateStore}.
	 * </p>
	 */
	protected DefaultTableView<TLObject> createTableView(List<Column<TLObject, ?>> columns,
			ListRowSource<TLObject> source) {
		return DefaultTableView.create(columns, source);
	}

	/**
	 * Hook invoked after the inner {@link TableViewControl} has been (re-)created, e.g. on an edit
	 * mode transition. Subclasses attach per-table wiring (selection listeners, observers) here.
	 *
	 * @param table
	 *        The freshly created table control.
	 * @param editMode
	 *        Whether the table was built for edit mode.
	 */
	protected void afterTableBuilt(TableViewControl<TLObject> table, boolean editMode) {
		// No additional wiring by default.
	}

	/**
	 * Whether the given row is currently editable. The default edits all rows; subclasses restrict
	 * this (e.g. to the selected row).
	 */
	protected boolean isRowEditable(TLObject row) {
		return true;
	}

	/**
	 * The current row source, or {@code null} before the first {@link #initTable()}.
	 */
	protected ListRowSource<TLObject> getRowSource() {
		return _rowSource;
	}

	/**
	 * The form control this table participates in.
	 */
	protected FormControl getFormControl() {
		return _formControl;
	}

	/**
	 * Replaces the displayed rows and refreshes the table.
	 */
	protected void updateRows(List<? extends TLObject> rows) {
		if (_rowSource == null) {
			return;
		}
		_rowSource.setElements(new ArrayList<>(rows));
		if (_tableControl != null) {
			_tableControl.refreshData();
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

	private void rebuildTableRows() {
		if (!_editor.isEditing()) {
			return;
		}
		updateRows(_editor.getCurrentRows());
	}

	/**
	 * The header label for a data column: the model attribute's label if the row type and part
	 * resolve, else the attribute name.
	 */
	private static ResKey columnLabel(TLClass rowType, String attribute) {
		if (rowType != null) {
			TLStructuredTypePart part = rowType.getPart(attribute);
			if (part != null) {
				return TLModelNamingConvention.resourceKey(part);
			}
		}
		return ResKey.text(attribute);
	}

	/**
	 * Builds the control for a data cell: a read-only label in view mode, for read-only columns
	 * and rows that are not {@link #isRowEditable(TLObject) editable}, otherwise the attribute's
	 * editable field control provided by the {@link RowSetEditor}.
	 */
	protected ReactControl buildDataControl(ReactContext ctx, TLObject row, String columnName, boolean editMode,
			boolean readonly) {
		if (editMode && !readonly && isRowEditable(row)) {
			ReactControl editControl = _editor.createEditCellControl(ctx, row, columnName);
			if (editControl != null) {
				return editControl;
			}
		}
		return new ReactTextControl(ctx, MetaLabelProvider.INSTANCE.getLabel(row.tValueByName(columnName)));
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
			editModeChannel.set(Boolean.valueOf(_formControl.isEditMode()));
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
				RowEditModel rowModel = _editor.findRowModel(row);
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
		_formControl.removeFormModelListener(this);
		_editor.endEditSession();
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
}
