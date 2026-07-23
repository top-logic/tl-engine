/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
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
import com.top_logic.model.TLStructuredType;
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
 * Table presentation of an inline-edited composition reference within a form.
 *
 * <p>
 * Renders as a {@code TLPanel} React component with the composition attribute's label as title, a
 * {@link TableViewControl} as content, and an Add button in the toolbar (visible only in edit
 * mode). Action columns for detail-open (only when a detail dialog is configured) and delete (edit
 * mode only) are appended to the table columns. The editing lifecycle is handled by
 * {@link AbstractCompositionControl}.
 * </p>
 */
public class CompositionTableControl extends AbstractCompositionControl {

	/** Column name for the detail-open action column. */
	static final String COLUMN_DETAIL = "_detail";

	/** Column name for the delete action column. */
	static final String COLUMN_DELETE = "_delete";

	/** Command name for {@link #handleAddRow()}. */
	private static final String CMD_ADD_ROW = "addRow";

	/**
	 * Configuration for a single column in the composition table.
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

	private final List<ColumnConfig> _columnConfigs;

	private final CompositionTableElement.DetailDialogConfig _detailDialogConfig;

	private TableViewControl<TLObject> _tableControl;

	private ListRowSource<TLObject> _rowSource;

	/** The Add button in the toolbar, or {@code null} if not in edit mode. */
	private ReactButtonControl _addButton;

	/** The panel toolbar holding the Add button, or {@code null} if not in edit mode. */
	private ReactToolbarControl _toolbar;

	/**
	 * Creates a new {@link CompositionTableControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param compositionAttributeName
	 *        The name of the composition reference attribute on the parent object.
	 * @param columnConfigs
	 *        The column configurations defining which attributes to display and edit.
	 * @param detailDialogConfig
	 *        Optional configuration for the detail dialog, or {@code null} if no detail dialog is
	 *        configured.
	 */
	public CompositionTableControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, List<ColumnConfig> columnConfigs,
			CompositionTableElement.DetailDialogConfig detailDialogConfig) {
		super(context, formControl, compositionAttributeName, "TLPanel");
		_context = context;
		_columnConfigs = columnConfigs;
		_detailDialogConfig = detailDialogConfig;

		// Composition tables should span the full form row.
		putState("fullLine", Boolean.TRUE);
	}

	/**
	 * The inner {@link TableViewControl}, or {@code null} if not yet initialized.
	 */
	public TableViewControl<TLObject> getTableControl() {
		return _tableControl;
	}

	/**
	 * Adds a new row to the composition table.
	 */
	@ReactCommandHandler(CMD_ADD_ROW)
	void handleAddRow() {
		addRow();
	}

	// -- Table Building --

	@Override
	protected void buildContent(List<? extends TLObject> rowObjects, boolean editMode) {
		// Set panel title from the composition attribute's display label.
		TLStructuredTypePart part = compositionPart();
		String title = part != null ? MetaLabelProvider.INSTANCE.getLabel(part) : null;
		putState("title", title != null ? title : compositionAttributeName());

		List<Column<TLObject, ?>> columns = new ArrayList<>();

		// Detail action column (first), only when a detail dialog is configured to open.
		// Action columns carry no header label - it would not fit their narrow width; the
		// buttons in the cells label the action themselves.
		if (_detailDialogConfig != null) {
			columns.add(DefaultColumn.<TLObject, TLObject> builder(COLUMN_DETAIL, row -> row)
				.label(ResKey.text(""))
				.renderer(row -> new CellContent.Raw((CellControlFactory) (ctx -> createDetailButton(ctx, row))))
				.width(48)
				.frozenEligible(false)
				.build());
		}

		// Data columns from configuration.
		TLClass targetType = resolveTargetType();
		for (ColumnConfig col : _columnConfigs) {
			String attribute = col.getAttributeName();
			boolean readonly = col.isReadonly();
			columns.add(DefaultColumn.<TLObject, TLObject> builder(attribute, row -> row)
				.label(columnLabel(targetType, attribute))
				.renderer(row -> new CellContent.Raw(
					(CellControlFactory) (ctx -> buildDataControl(ctx, row, attribute, editMode, readonly))))
				.build());
		}

		// Delete action column (edit mode only, last, no header label - see detail column).
		if (editMode) {
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
		DefaultTableView<TLObject> view = DefaultTableView.create(columns, _rowSource);

		if (_tableControl != null) {
			_tableControl.cleanupTree();
		}
		_tableControl = new TableViewControl<>(_context, view, false);
		registerChildControl(_tableControl);

		// Set panel child to the table.
		putState("child", _tableControl);

		// Update toolbar: Add button only in edit mode.
		updateToolbar(editMode);
	}

	private void updateToolbar(boolean editMode) {
		if (_toolbar != null) {
			_toolbar.cleanupTree();
			_toolbar = null;
			_addButton = null;
		}

		if (editMode) {
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

	@Override
	protected void refreshRows() {
		if (_rowSource == null || fieldModel() == null) {
			return;
		}
		List<TLObject> currentList = fieldModel().getCurrentList();
		_rowSource.setElements(new ArrayList<>(currentList));
		if (_tableControl != null) {
			_tableControl.refreshData();
		}
	}

	/**
	 * The header label for a data column: the model attribute's label if the target type and part
	 * resolve, else the attribute name.
	 */
	private static ResKey columnLabel(TLClass targetType, String attribute) {
		if (targetType != null) {
			TLStructuredTypePart part = targetType.getPart(attribute);
			if (part != null) {
				return TLModelNamingConvention.resourceKey(part);
			}
		}
		return ResKey.text(attribute);
	}

	/**
	 * Builds the control for a data cell: a read-only label in view mode or for read-only columns,
	 * otherwise the attribute's editable field control. The cell's {@link AttributeFieldModel} is
	 * created once per row/column and wired for validation and dirty tracking.
	 */
	private ReactControl buildDataControl(ReactContext ctx, TLObject row, String columnName, boolean editMode,
			boolean readonly) {
		TLStructuredType type = row.tType();
		TLStructuredTypePart part = type.getPart(columnName);

		if (!editMode || readonly) {
			if (part != null) {
				// Same read-only representation as a view-mode form field, so value types keep
				// their interactive display (e.g. a download link for a binary value).
				return FieldControlService.getInstance()
					.createDisplayControl(ctx, part, row.tValueByName(columnName));
			}
			return new ReactTextControl(ctx, MetaLabelProvider.INSTANCE.getLabel(row.tValueByName(columnName)));
		}

		if (part != null) {
			CompositionRowModel rowModel = findRowModel(row);
			if (rowModel != null) {
				AttributeFieldModel cellFieldModel = rowModel.getColumnModel(columnName);
				if (cellFieldModel == null) {
					cellFieldModel = new AttributeFieldModel(row, part);
					cellFieldModel.setEditable(true);
					rowModel.putColumnModel(columnName, cellFieldModel);

					// Wire validation from FormValidationModel to this cell.
					wireCellValidation(cellFieldModel, row, part);

					// Add dirty propagation and live validation trigger.
					addCellListener(cellFieldModel, row);
				}
				return FieldControlService.getInstance().createFieldControl(ctx, part, cellFieldModel);
			}
		}

		// Fallback: text display.
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
			Logger.info("No detail dialog configured for composition table.", CompositionTableControl.class);
			return;
		}

		DialogManager mgr = _context.getDialogManager();
		if (mgr == null) {
			Logger.warn("No DialogManager available, cannot open detail dialog.", CompositionTableControl.class);
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
			if (rowObject instanceof TLObject) {
				CompositionRowModel rowModel = findRowModel((TLObject) rowObject);
				if (rowModel != null) {
					rowModel.refreshColumnModels();
				}
			}
		});
	}

	private ReactButtonControl createDeleteButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DELETE);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			if (fieldModel() == null) {
				return HandlerResult.DEFAULT_RESULT;
			}
			if (rowObject instanceof TLObject) {
				TLObject tlObj = (TLObject) rowObject;
				List<TLObject> currentList = fieldModel().getCurrentList();
				int idx = currentList.indexOf(tlObj);
				if (idx >= 0) {
					deleteRow(tlObj, idx);
				}
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
