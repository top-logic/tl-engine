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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.util.Resources;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Orchestrator for inline composition table editing within a form.
 *
 * <p>
 * Manages the lifecycle of a {@link ReactTableControl} that displays and edits composed child
 * objects. Implements {@link FormModelListener} to react to form state changes (enter/exit edit
 * mode, object switch) and {@link FormParticipant} for validation, apply, cancel, and dirty
 * tracking.
 * </p>
 *
 * <p>
 * Renders as a {@code TLPanel} React component with the composition attribute's label as title, the
 * table as content, and an Add button in the toolbar (visible only in edit mode). Action columns for
 * detail-open (always visible) and delete (edit mode only) are appended to the table columns.
 * </p>
 *
 * <p>
 * On entering edit mode, the control creates {@link TLObjectOverlay}s for existing composed objects
 * and stores the overlay list in the main form overlay. On save, it persists new transient objects,
 * applies overlay changes, and removes orphaned objects.
 * </p>
 */
public class CompositionTableControl extends ReactControl implements FormModelListener, FormParticipant {

	/** Column name for the detail-open action column. */
	static final String COLUMN_DETAIL = "_detail";

	/** Column name for the delete action column. */
	static final String COLUMN_DELETE = "_delete";

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

	private final FormControl _formControl;

	private final String _compositionAttributeName;

	private final List<ColumnConfig> _columnConfigs;

	private final CompositionTableElement.DetailDialogConfig _detailDialogConfig;

	private TLStructuredTypePart _compositionPart;

	private CompositionFieldModel _fieldModel;

	private final List<CompositionRowModel> _rowModels = new ArrayList<>();

	/** The persistent objects at edit-mode entry, for orphan detection on save. */
	private List<TLObject> _originalPersistentObjects;

	private ReactTableControl _tableControl;

	private ObjectTableModel _tableModel;

	/** Validation listeners registered per cell, for cleanup on exit edit mode. */
	private final List<ConstraintValidationListener> _cellValidationListeners = new ArrayList<>();

	/** The Add button in the toolbar, or {@code null} if not in edit mode. */
	private ReactButtonControl _addButton;

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
		super(context, null, "TLPanel");
		_context = context;
		_formControl = formControl;
		_compositionAttributeName = compositionAttributeName;
		_columnConfigs = columnConfigs;
		_detailDialogConfig = detailDialogConfig;

		// Composition tables should span the full form row.
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
		if (currentObject == null) {
			// No object yet — build an empty table so TLPanel always has a valid child.
			putState("title", _compositionAttributeName);
			buildTable(Collections.emptyList(), false);
			return;
		}

		_compositionPart = resolveCompositionPart(currentObject);
		if (_compositionPart == null) {
			putState("title", _compositionAttributeName);
			buildTable(Collections.emptyList(), false);
			return;
		}

		// Set panel title from the composition attribute's display label.
		String label = MetaLabelProvider.INSTANCE.getLabel(_compositionPart);
		putState("title", label != null ? label : _compositionAttributeName);

		List<TLObject> composedObjects = readCompositionValue(currentObject);
		boolean editMode = _formControl.isEditMode();

		buildTable(composedObjects, editMode);
	}

	/**
	 * The inner {@link ReactTableControl}, or {@code null} if not yet initialized.
	 */
	public ReactTableControl getTableControl() {
		return _tableControl;
	}

	// -- FormModelListener --

	@Override
	public void onFormStateChanged(FormModel source) {
		TLObject currentObject = source.getCurrentObject();

		if (currentObject == null) {
			cleanupEditState();
			return;
		}

		_compositionPart = resolveCompositionPart(currentObject);
		if (_compositionPart == null) {
			cleanupEditState();
			return;
		}

		if (source.isEditMode()) {
			enterEditMode(currentObject);
		} else {
			exitEditMode(currentObject);
		}
	}

	private void enterEditMode(TLObject currentObject) {
		// currentObject is the overlay in edit mode.
		TLObject baseObject = currentObject;
		if (currentObject instanceof TLObjectOverlay) {
			baseObject = ((TLObjectOverlay) currentObject).getBase();
		}

		// Read the persistent composition value.
		List<TLObject> persistentObjects = readCompositionValue(baseObject);
		_originalPersistentObjects = new ArrayList<>(persistentObjects);

		// Create overlays for each existing composed object.
		List<TLObject> overlayList = new ArrayList<>();
		_rowModels.clear();

		for (TLObject composed : persistentObjects) {
			TLObjectOverlay rowOverlay = new TLObjectOverlay(composed);
			CompositionRowModel rowModel = CompositionRowModel.forExisting(rowOverlay);
			_rowModels.add(rowModel);
			overlayList.add(rowOverlay);
		}

		// Store the overlay list in the main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, overlayList);

		// Create composition field model.
		_fieldModel = new CompositionFieldModel(overlayList);
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay rowOverlay = row.getRowOverlay();
			if (rowOverlay != null) {
				_fieldModel.addRowOverlay(rowOverlay);
			}
		}

		// Register row overlays with the validation model so field-level
		// constraints (mandatory, range) are evaluated for composition items.
		FormValidationModel validationModel = _formControl.getValidationModel();
		if (validationModel != null) {
			for (CompositionRowModel rowModel : _rowModels) {
				TLObjectOverlay rowOverlay = rowModel.getRowOverlay();
				if (rowOverlay != null) {
					validationModel.addOverlay(rowOverlay, rowOverlay.getBase());
				}
			}
		}

		// Register as participant.
		_formControl.registerParticipant(this);

		// Rebuild the table in edit mode.
		buildTable(overlayList, true);
	}

	private void exitEditMode(TLObject currentObject) {
		cleanupEditState();

		// Rebuild table in view mode.
		List<TLObject> composedObjects = readCompositionValue(currentObject);
		buildTable(composedObjects, false);
	}

	private void cleanupEditState() {
		// Remove cell validation listeners and row overlays from validation model.
		FormValidationModel validationModel = _formControl.getValidationModel();
		if (validationModel != null) {
			for (ConstraintValidationListener listener : _cellValidationListeners) {
				validationModel.removeConstraintValidationListener(listener);
			}
			for (CompositionRowModel row : _rowModels) {
				TLObjectOverlay overlay = row.getRowOverlay();
				if (overlay != null) {
					validationModel.removeOverlay(overlay);
				}
			}
		}
		_cellValidationListeners.clear();

		if (_fieldModel != null) {
			_formControl.unregisterParticipant(this);
			_fieldModel = null;
		}
		_rowModels.clear();
		_originalPersistentObjects = null;
	}

	// -- FormParticipant --

	@Override
	public boolean validate() {
		if (_fieldModel == null) {
			return true;
		}

		// Reveal all fields so that model-level errors (mandatory, range constraints
		// from FormValidationModel) become visible via hasError().
		revealAll();

		boolean valid = !_fieldModel.hasError();
		for (CompositionRowModel row : _rowModels) {
			for (AttributeFieldModel colModel : row.getColumnModels().values()) {
				if (colModel.hasError()) {
					valid = false;
				}
			}
		}
		return valid;
	}

	@Override
	public void apply(Transaction tx) {
		if (_fieldModel == null) {
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();

		// 1. Apply row overlays (attribute changes on existing objects).
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null && overlay.isDirty()) {
				overlay.apply();
			}
		}

		// 2. Persist new transient objects and build the persisted reference list.
		List<TLObject> persistedList = new ArrayList<>();
		for (TLObject obj : currentList) {
			if (obj instanceof TLObjectOverlay) {
				persistedList.add(((TLObjectOverlay) obj).getBase());
			} else if (obj.tTransient()) {
				// New transient object -- persist it by creating a KB object and copying values.
				TLObject persisted = persistTransientObject(obj);
				persistedList.add(persisted);
			} else {
				persistedList.add(obj);
			}
		}

		// 3. Update composition reference in the main overlay.
		// FormControl.executeSave() calls participant.apply() BEFORE overlay.applyTo(),
		// so we prepare the overlay with the correct persisted list.
		_formControl.getOverlay().tUpdate(_compositionPart, persistedList);

		// 4. Delete orphaned objects (present in original but absent from current).
		Set<TLObject> currentBases = new HashSet<>(persistedList);
		for (TLObject original : _originalPersistentObjects) {
			if (!currentBases.contains(original)) {
				original.tDelete();
			}
		}
	}

	@Override
	public void cancel() {
		_rowModels.clear();
		_fieldModel = null;
	}

	@Override
	public void revealAll() {
		if (_fieldModel != null) {
			_fieldModel.setRevealed(true);
		}
		for (CompositionRowModel row : _rowModels) {
			for (AttributeFieldModel colModel : row.getColumnModels().values()) {
				colModel.setRevealed(true);
			}
		}
	}

	@Override
	public boolean isDirty() {
		return _fieldModel != null && _fieldModel.isDirty();
	}

	// -- Add/Delete Row Commands --

	/**
	 * Adds a new row to the composition table.
	 *
	 * <p>
	 * Creates a transient object of the composition reference's target type and appends it to the
	 * current list. Updates the main overlay and refreshes the table.
	 * </p>
	 */
	@ReactCommand("addRow")
	void handleAddRow() {
		addRow();
	}

	/**
	 * Removes a row from the composition table.
	 */
	@ReactCommand("deleteRow")
	void handleDeleteRow(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		if (_fieldModel == null) {
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();
		if (rowIndex < 0 || rowIndex >= currentList.size()) {
			return;
		}
		TLObject rowObject = currentList.get(rowIndex);
		deleteRow(rowObject, rowIndex);
	}

	/**
	 * Opens the detail dialog for a row.
	 *
	 * <p>
	 * Loads the configured dialog view, creates a fresh {@link ViewContext} with channels for the
	 * row object and edit mode, and opens the dialog via {@link DialogManager}.
	 * </p>
	 */
	@ReactCommand("openDetail")
	void handleOpenDetail(Map<String, Object> arguments) {
		int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
		if (_tableModel == null) {
			return;
		}
		Collection<?> allRows = _tableModel.getAllRows();
		List<?> rows = new ArrayList<>(allRows);
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			return;
		}
		Object rowObject = rows.get(rowIndex);
		openDetailDialog(rowObject);
	}

	/**
	 * Adds a new transient row to the table.
	 */
	public void addRow() {
		if (_fieldModel == null || _compositionPart == null) {
			return;
		}

		// Determine target type from composition reference.
		TLClass targetType = resolveTargetType();
		if (targetType == null) {
			return;
		}

		// Create transient object.
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(targetType);

		// Register with validation model so constraints are evaluated.
		FormValidationModel validationModel = _formControl.getValidationModel();
		if (validationModel != null) {
			validationModel.addOverlay(transientObject, null);
		}

		// Create row model.
		CompositionRowModel rowModel = CompositionRowModel.forNew(transientObject);
		_rowModels.add(rowModel);

		// Add to current list.
		List<TLObject> currentList = _fieldModel.getCurrentList();
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		// Update main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, currentList);

		// Notify validation model that the composition reference changed.
		notifyValidationModelChanged();

		_formControl.updateDirtyState();

		// Rebuild table.
		rebuildTableRows();
	}

	/**
	 * Deletes a row from the composition table.
	 */
	public void deleteRow(TLObject rowObject, int rowIndex) {
		if (_fieldModel == null) {
			return;
		}

		List<TLObject> currentList = _fieldModel.getCurrentList();
		currentList.remove(rowObject);
		_fieldModel.setValue(currentList);

		// Remove row model and unregister from validation model.
		if (rowIndex >= 0 && rowIndex < _rowModels.size()) {
			CompositionRowModel removedRow = _rowModels.remove(rowIndex);
			TLObjectOverlay removedOverlay = removedRow.getRowOverlay();
			if (removedOverlay != null) {
				_fieldModel.removeRowOverlay(removedOverlay);
			}
			FormValidationModel validationModel = _formControl.getValidationModel();
			if (validationModel != null) {
				if (removedOverlay != null) {
					validationModel.removeOverlay(removedOverlay);
				} else if (rowObject.tTransient()) {
					validationModel.removeOverlay(rowObject);
				}
			}
		}

		// Update main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, currentList);

		// Notify validation model that the composition reference changed.
		notifyValidationModelChanged();

		_formControl.updateDirtyState();

		// Rebuild table.
		rebuildTableRows();
	}

	/**
	 * Notifies the {@link FormValidationModel} that the composition reference value has changed,
	 * so that constraints on the reference (e.g. min count) are re-evaluated.
	 */
	private void notifyValidationModelChanged() {
		FormValidationModel validationModel = _formControl.getValidationModel();
		TLObjectOverlay overlay = _formControl.getOverlay();
		if (validationModel != null && overlay != null && _compositionPart != null) {
			validationModel.onValueChanged(overlay, _compositionPart);
		}
	}

	// -- Table Building --

	private void buildTable(List<? extends TLObject> rowObjects, boolean editMode) {
		// Build column names: detail first, then data columns, then delete last.
		List<String> columnNames = new ArrayList<>();

		// Detail column is always first.
		columnNames.add(COLUMN_DETAIL);

		// Data columns from configuration.
		for (ColumnConfig col : _columnConfigs) {
			columnNames.add(col.getAttributeName());
		}

		// Delete column is last (only in edit mode).
		if (editMode) {
			columnNames.add(COLUMN_DELETE);
		}

		// Build table configuration from the target type.
		TableConfiguration tableConfig = buildTableConfiguration(editMode);

		// Create or replace the table model (column set may change between edit/view mode).
		List<Object> rows = new ArrayList<>(rowObjects);
		_tableModel = new ObjectTableModel(columnNames, tableConfig, rows);

		// Create cell provider.
		ReactCellControlProvider cellProvider = createCellProvider(editMode);

		// Create or replace the table control.
		if (_tableControl != null) {
			unregisterChildControl(_tableControl);
			_tableControl.cleanupTree();
		}
		_tableControl = new ReactTableControl(_context, _tableModel, cellProvider);
		registerChildControl(_tableControl);

		// Set panel child to the table.
		putState("child", _tableControl);

		// Update toolbar: Add button only in edit mode.
		updateToolbar(editMode);
	}

	private void updateToolbar(boolean editMode) {
		if (_addButton != null) {
			unregisterChildControl(_addButton);
			_addButton.cleanupTree();
			_addButton = null;
		}

		if (editMode) {
			String addLabel = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_ADD);
			_addButton = new ReactButtonControl(_context, addLabel, ctx -> {
				addRow();
				return HandlerResult.DEFAULT_RESULT;
			});
			registerChildControl(_addButton);
			putState("toolbarButtons", Collections.singletonList(_addButton));
		} else {
			putState("toolbarButtons", Collections.emptyList());
		}
	}

	private void rebuildTableRows() {
		if (_tableModel == null || _fieldModel == null) {
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();
		_tableModel.setRowObjects(new ArrayList<>(currentList));
	}

	private TableConfiguration buildTableConfiguration(boolean editMode) {
		TableConfiguration tableConfig;
		if (_compositionPart instanceof TLReference) {
			TLClass targetType = resolveTargetType();
			if (targetType != null) {
				Set<TLClass> types = new HashSet<>();
				types.add(targetType);
				tableConfig = TableConfigurationFactory.build(new GenericTableConfigurationProvider(types));
			} else {
				tableConfig = TableConfigurationFactory.table();
			}
		} else {
			tableConfig = TableConfigurationFactory.table();
		}

		// Configure action columns.
		Resources res = Resources.getInstance();
		ColumnConfiguration detailCol = tableConfig.declareColumn(COLUMN_DETAIL);
		detailCol.setColumnLabel(res.getString(I18NConstants.COMPOSITION_TABLE_DETAIL));
		detailCol.setSortable(false);

		if (editMode) {
			ColumnConfiguration deleteCol = tableConfig.declareColumn(COLUMN_DELETE);
			deleteCol.setColumnLabel(res.getString(I18NConstants.COMPOSITION_TABLE_DELETE));
			deleteCol.setSortable(false);
		}

		return tableConfig;
	}

	private ReactCellControlProvider createCellProvider(boolean editMode) {
		// Build a lookup map from column configs.
		Map<String, ColumnConfig> configByName = new LinkedHashMap<>();
		for (ColumnConfig col : _columnConfigs) {
			configByName.put(col.getAttributeName(), col);
		}

		return (ctx, rowObject, columnName, cellValue) -> {
			// Handle action columns.
			if (COLUMN_DETAIL.equals(columnName)) {
				return createDetailButton(ctx, rowObject);
			}
			if (COLUMN_DELETE.equals(columnName)) {
				return createDeleteButton(ctx, rowObject);
			}

			ColumnConfig colConfig = configByName.get(columnName);

			if (!editMode || (colConfig != null && colConfig.isReadonly())) {
				// View mode or read-only column: just show formatted text.
				String text = MetaLabelProvider.INSTANCE.getLabel(cellValue);
				return new ReactTextCellControl(ctx, text);
			}

			// Edit mode: create an editable field control for this cell.
			if (rowObject instanceof TLObject) {
				TLObject tlObject = (TLObject) rowObject;
				TLStructuredType type = tlObject.tType();
				TLStructuredTypePart part = type.getPart(columnName);

				if (part != null) {
					// Find the row model for this row object.
					CompositionRowModel rowModel = findRowModel(tlObject);
					if (rowModel != null) {
						// Create or retrieve an AttributeFieldModel for this cell.
						AttributeFieldModel cellFieldModel = rowModel.getColumnModel(columnName);
						if (cellFieldModel == null) {
							cellFieldModel = new AttributeFieldModel(tlObject, part);
							cellFieldModel.setEditable(true);
							rowModel.putColumnModel(columnName, cellFieldModel);

							// Wire validation from FormValidationModel to this cell.
							wireCellValidation(cellFieldModel, tlObject, part);

							// Add dirty propagation and live validation trigger.
							addCellListener(cellFieldModel, tlObject);
						}

						// Create the field input control.
						return FieldControlService.getInstance().createFieldControl(ctx, part, cellFieldModel);
					}
				}
			}

			// Fallback: text display.
			String text = MetaLabelProvider.INSTANCE.getLabel(cellValue);
			return new ReactTextCellControl(ctx, text);
		};
	}

	private ReactButtonControl createDetailButton(ReactContext ctx, Object rowObject) {
		String label = Resources.getInstance().getString(I18NConstants.COMPOSITION_TABLE_DETAIL);
		ReactButtonControl button = new ReactButtonControl(ctx, label, context -> {
			openDetailDialog(rowObject);
			return HandlerResult.DEFAULT_RESULT;
		});
		button.setIcon("detail");
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
			if (_fieldModel == null) {
				return HandlerResult.DEFAULT_RESULT;
			}
			if (rowObject instanceof TLObject) {
				TLObject tlObj = (TLObject) rowObject;
				List<TLObject> currentList = _fieldModel.getCurrentList();
				int idx = currentList.indexOf(tlObj);
				if (idx >= 0) {
					deleteRow(tlObj, idx);
				}
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		button.setIcon("delete");
		return button;
	}

	/**
	 * Wires a {@link ConstraintValidationListener} that propagates validation results from the
	 * {@link FormValidationModel} to the cell's {@link AttributeFieldModel}.
	 */
	private void wireCellValidation(AttributeFieldModel model, TLObject rowObject, TLStructuredTypePart part) {
		FormValidationModel validationModel = _formControl.getValidationModel();
		if (validationModel == null) {
			return;
		}

		// Apply initial validation state.
		model.applyValidationResult(validationModel.getValidation(rowObject, part));

		// Listen for future changes on this specific (object, attribute).
		ConstraintValidationListener listener = (overlay, attr, result) -> {
			if (overlay == rowObject && attr.equals(part)) {
				model.applyValidationResult(result);
			}
		};
		validationModel.addConstraintValidationListener(listener);
		_cellValidationListeners.add(listener);
	}

	/**
	 * Adds a listener to a cell field model that propagates dirty state, reveals the field after
	 * user interaction, and triggers live constraint re-evaluation via the
	 * {@link FormValidationModel}.
	 */
	private void addCellListener(AttributeFieldModel cellFieldModel, TLObject rowObject) {
		cellFieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_formControl.updateDirtyState();

				// Reveal validation errors after user interaction.
				cellFieldModel.setRevealed(true);

				// Trigger live constraint re-evaluation.
				FormValidationModel validationModel = _formControl.getValidationModel();
				if (validationModel != null) {
					validationModel.onValueChanged(rowObject, cellFieldModel.getPart());
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// No-op.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// No-op.
			}
		});
	}

	private CompositionRowModel findRowModel(TLObject rowObject) {
		for (CompositionRowModel row : _rowModels) {
			if (row.getRowObject() == rowObject) {
				return row;
			}
		}
		return null;
	}

	// -- Utility --

	private TLStructuredTypePart resolveCompositionPart(TLObject object) {
		TLObject base = object;
		if (object instanceof TLObjectOverlay) {
			base = ((TLObjectOverlay) object).getBase();
		}
		TLStructuredType type = base.tType();
		return type.getPart(_compositionAttributeName);
	}

	@SuppressWarnings("unchecked")
	private List<TLObject> readCompositionValue(TLObject object) {
		Object value = object.tValue(_compositionPart);
		if (value instanceof Collection<?>) {
			return new ArrayList<>((Collection<TLObject>) value);
		}
		if (value instanceof TLObject) {
			List<TLObject> result = new ArrayList<>();
			result.add((TLObject) value);
			return result;
		}
		return new ArrayList<>();
	}

	private TLClass resolveTargetType() {
		if (_compositionPart instanceof TLReference) {
			TLReference ref = (TLReference) _compositionPart;
			return (TLClass) ref.getType();
		}
		return null;
	}

	/**
	 * Persists a transient object by creating a new persistent KB object of the same type and
	 * copying all attribute values.
	 */
	private TLObject persistTransientObject(TLObject transientObj) {
		TLClass type = (TLClass) transientObj.tType();
		TLObject persisted = com.top_logic.element.model.DynamicModelService.getFactoryFor(type.getModule().getName())
			.createObject(type, null);

		// Copy attribute values.
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part instanceof TLReference) {
				// Skip reverse references and non-composition references for now.
				TLReference ref = (TLReference) part;
				if (ref.isBackwards()) {
					continue;
				}
			}
			try {
				Object value = transientObj.tValue(part);
				if (value != null) {
					persisted.tUpdate(part, value);
				}
			} catch (RuntimeException ex) {
				// Skip attributes that cannot be copied (e.g. derived attributes).
			}
		}
		return persisted;
	}

	@Override
	protected void cleanupChildren() {
		_formControl.removeFormModelListener(this);
		cleanupEditState();
		if (_addButton != null) {
			_addButton.cleanupTree();
			_addButton = null;
		}
		if (_tableControl != null) {
			_tableControl.cleanupTree();
			_tableControl = null;
		}
	}
}
