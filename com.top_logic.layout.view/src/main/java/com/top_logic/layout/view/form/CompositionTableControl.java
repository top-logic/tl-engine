/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;

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
 * On entering edit mode, the control creates {@link TLObjectOverlay}s for existing composed objects
 * and stores the overlay list in the main form overlay. On save, it persists new transient objects,
 * applies overlay changes, and removes orphaned objects.
 * </p>
 */
public class CompositionTableControl extends ReactControl implements FormModelListener, FormParticipant {

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

	private TLStructuredTypePart _compositionPart;

	private CompositionFieldModel _fieldModel;

	private final List<CompositionRowModel> _rowModels = new ArrayList<>();

	/** The persistent objects at edit-mode entry, for orphan detection on save. */
	private List<TLObject> _originalPersistentObjects;

	private ReactTableControl _tableControl;

	private ObjectTableModel _tableModel;

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
	 */
	public CompositionTableControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, List<ColumnConfig> columnConfigs) {
		super(context, null, "TLFieldList");
		_context = context;
		_formControl = formControl;
		_compositionAttributeName = compositionAttributeName;
		_columnConfigs = columnConfigs;

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
			return;
		}

		_compositionPart = resolveCompositionPart(currentObject);
		if (_compositionPart == null) {
			return;
		}

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
		_fieldModel.validate();
		if (_fieldModel.hasError()) {
			return false;
		}
		// Validate all row field models.
		for (CompositionRowModel row : _rowModels) {
			for (AttributeFieldModel colModel : row.getColumnModels().values()) {
				colModel.validate();
				if (colModel.hasError()) {
					return false;
				}
			}
		}
		return true;
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
				TLObject base = overlay.getBase();
				overlay.applyTo(base);
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

		// Create row model.
		CompositionRowModel rowModel = CompositionRowModel.forNew(transientObject);
		_rowModels.add(rowModel);

		// Add to current list.
		List<TLObject> currentList = _fieldModel.getCurrentList();
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		// Update main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, currentList);

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

		// Remove row model.
		if (rowIndex >= 0 && rowIndex < _rowModels.size()) {
			CompositionRowModel removedRow = _rowModels.remove(rowIndex);
			TLObjectOverlay removedOverlay = removedRow.getRowOverlay();
			if (removedOverlay != null) {
				_fieldModel.removeRowOverlay(removedOverlay);
			}
		}

		// Update main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, currentList);

		_formControl.updateDirtyState();

		// Rebuild table.
		rebuildTableRows();
	}

	// -- Table Building --

	private void buildTable(List<? extends TLObject> rowObjects, boolean editMode) {
		// Build column names from column configs.
		List<String> columnNames = new ArrayList<>();
		for (ColumnConfig col : _columnConfigs) {
			columnNames.add(col.getAttributeName());
		}

		// Build table configuration from the target type.
		TableConfiguration tableConfig = buildTableConfiguration();

		// Create or replace the table model.
		List<Object> rows = new ArrayList<>(rowObjects);
		if (_tableModel == null) {
			_tableModel = new ObjectTableModel(columnNames, tableConfig, rows);
		} else {
			_tableModel.setRowObjects(rows);
		}

		// Create cell provider.
		ReactCellControlProvider cellProvider = createCellProvider(editMode);

		// Create or replace the table control.
		if (_tableControl != null) {
			unregisterChildControl(_tableControl);
		}
		_tableControl = new ReactTableControl(_context, _tableModel, cellProvider);
		registerChildControl(_tableControl);
		putState("fields", java.util.Collections.singletonList(_tableControl));
	}

	private void rebuildTableRows() {
		if (_tableModel == null || _fieldModel == null) {
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();
		_tableModel.setRowObjects(new ArrayList<>(currentList));
	}

	private TableConfiguration buildTableConfiguration() {
		if (_compositionPart instanceof TLReference) {
			TLClass targetType = resolveTargetType();
			if (targetType != null) {
				Set<TLClass> types = new HashSet<>();
				types.add(targetType);
				return TableConfigurationFactory.build(new GenericTableConfigurationProvider(types));
			}
		}
		return TableConfigurationFactory.table();
	}

	private ReactCellControlProvider createCellProvider(boolean editMode) {
		// Build a lookup map from column configs.
		Map<String, ColumnConfig> configByName = new LinkedHashMap<>();
		for (ColumnConfig col : _columnConfigs) {
			configByName.put(col.getAttributeName(), col);
		}

		return (ctx, rowObject, columnName, cellValue) -> {
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

							// Add dirty propagation listener.
							addCellDirtyListener(cellFieldModel);
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

	private void addCellDirtyListener(AttributeFieldModel cellFieldModel) {
		cellFieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_formControl.updateDirtyState();
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
		if (_tableControl != null) {
			_tableControl.cleanupTree();
			_tableControl = null;
		}
	}
}
