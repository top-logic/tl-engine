/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * Base class for form controls that display and edit a set of row objects inline.
 *
 * <p>
 * Owns the complete editing lifecycle independent of the concrete presentation: implements
 * {@link FormModelListener} to react to form state changes (enter/exit edit mode, object switch)
 * and {@link FormParticipant} for validation, apply, cancel, and dirty tracking. On entering edit
 * mode, creates {@link TLObjectOverlay}s for the existing row objects and buffers the overlay list;
 * on save, persists new transient objects, applies overlay changes, and applies the binding's
 * remove semantics to orphaned objects.
 * </p>
 *
 * <p>
 * The membership semantics of the row set (which objects are the rows, what adding and removing
 * means, what is written back on commit) are delegated to a {@link RowSetBinding}. An
 * {@link AttributeRowSetBinding} derives them from a composition or plain reference of the form
 * object; a {@link QueryRowSetBinding} computes the rows from a query and takes them from explicit
 * configuration.
 * </p>
 *
 * <p>
 * Subclasses render the row objects: {@link #buildContent(List, boolean)} builds the presentation
 * for a row list, {@link #refreshRows()} updates it after {@link #addRow()} or
 * {@link #deleteRow(TLObject, int)} changed the list.
 * </p>
 */
public abstract class AbstractCompositionControl extends ReactControl
		implements FormModelListener, FormParticipant {

	private final FormControl _formControl;

	private final RowSetBinding _binding;

	private CompositionFieldModel _fieldModel;

	private final List<CompositionRowModel> _rowModels = new ArrayList<>();

	/** The persistent objects at edit-mode entry, for orphan detection on save. */
	private List<TLObject> _originalPersistentObjects;

	/** Validation listeners registered per cell, for cleanup on exit edit mode. */
	private final List<ConstraintValidationListener> _cellValidationListeners = new ArrayList<>();

	/** The validation model on which cell listeners were registered, for correct cleanup. */
	private FormValidationModel _registeredValidationModel;

	/**
	 * Creates a new {@link AbstractCompositionControl} over the given row-set binding.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param binding
	 *        The row-set semantics (row objects, create types, remove mode, commit).
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, FormControl formControl,
			RowSetBinding binding, String reactModule) {
		super(context, null, reactModule);
		_formControl = formControl;
		_binding = binding;

		formControl.addFormModelListener(this);
	}

	/**
	 * Creates a new {@link AbstractCompositionControl} editing a composition or plain reference
	 * attribute of the form object.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param compositionAttributeName
	 *        The name of the reference attribute on the parent object holding the rows.
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, String reactModule) {
		this(context, formControl, new AttributeRowSetBinding(compositionAttributeName), reactModule);
	}

	/**
	 * Initializes the presentation from the form's current object.
	 *
	 * <p>
	 * Must be called after construction. Resolves the row-set binding and builds the initial
	 * content; when the binding is not available (e.g. the current object's type does not declare
	 * the bound attribute), builds empty view-mode content.
	 * </p>
	 */
	public void init() {
		TLObject currentObject = _formControl.getCurrentObject();
		if (!_binding.resolve(currentObject)) {
			buildContent(List.of(), false);
			return;
		}
		buildContent(_binding.readRows(currentObject), _formControl.isEditMode());
	}

	/**
	 * Builds the presentation for the given row objects.
	 *
	 * @param rows
	 *        The row objects to display: overlays and transient objects in edit mode, persistent
	 *        objects in view mode.
	 * @param editMode
	 *        Whether the form is in edit mode.
	 */
	protected abstract void buildContent(List<? extends TLObject> rows, boolean editMode);

	/**
	 * Updates the presentation after the row list changed through {@link #addRow()} or
	 * {@link #deleteRow(TLObject, int)}. The current list is available from the
	 * {@link #fieldModel()}.
	 */
	protected abstract void refreshRows();

	/**
	 * The parent form control.
	 */
	protected final FormControl formControl() {
		return _formControl;
	}

	/**
	 * The row-set semantics of this control.
	 */
	protected final RowSetBinding binding() {
		return _binding;
	}

	/**
	 * The name of the bound reference attribute, or {@code null} for bindings without a bound
	 * attribute (e.g. a query binding).
	 */
	protected final String compositionAttributeName() {
		return _binding instanceof AttributeRowSetBinding attributeBinding
			? attributeBinding.getAttributeName()
			: null;
	}

	/**
	 * The bound reference of the row-set binding, or {@code null} before {@link #init()}, when the
	 * current object's type does not declare the attribute, or for a binding without a bound
	 * attribute.
	 */
	protected final TLStructuredTypePart compositionPart() {
		return _binding.getBoundPart();
	}

	/**
	 * The field model holding the edited row list, or {@code null} outside edit mode.
	 */
	protected final CompositionFieldModel fieldModel() {
		return _fieldModel;
	}

	/**
	 * Whether an edit session is running.
	 */
	protected final boolean isEditing() {
		return _fieldModel != null;
	}

	/**
	 * The per-row models tracking overlays and cell field models during edit mode.
	 */
	protected final List<CompositionRowModel> rowModels() {
		return _rowModels;
	}

	/**
	 * The validation model in effect for the current edit session, or {@code null}.
	 */
	protected final FormValidationModel registeredValidationModel() {
		return _registeredValidationModel;
	}

	// -- FormModelListener --

	@Override
	public void onFormStateChanged(FormModel source) {
		TLObject currentObject = source.getCurrentObject();

		if (!_binding.resolve(currentObject)) {
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
		// Clean up previous edit state (e.g. after executeApply resets the session).
		cleanupEditState();

		// currentObject is the overlay in edit mode.
		TLObject baseObject = currentObject;
		if (currentObject instanceof TLObjectOverlay) {
			baseObject = ((TLObjectOverlay) currentObject).getBase();
		}

		// Read the persistent row objects.
		List<TLObject> persistentObjects = _binding.readRows(baseObject);
		_originalPersistentObjects = new ArrayList<>(persistentObjects);

		// Create overlays for each existing row object.
		List<TLObject> overlayList = new ArrayList<>();
		_rowModels.clear();

		for (TLObject row : persistentObjects) {
			TLObjectOverlay rowOverlay = new TLObjectOverlay(row);
			CompositionRowModel rowModel = CompositionRowModel.forExisting(rowOverlay);
			_rowModels.add(rowModel);
			overlayList.add(rowOverlay);
		}

		// Publish the overlay list to the form overlay through the binding.
		_binding.updateMembership(_formControl, overlayList);

		// Create the row-list field model.
		_fieldModel = new CompositionFieldModel(overlayList);
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay rowOverlay = row.getRowOverlay();
			if (rowOverlay != null) {
				_fieldModel.addRowOverlay(rowOverlay);
			}
		}

		// Register row overlays with the validation model so field-level
		// constraints (mandatory, range) are evaluated for row objects.
		_registeredValidationModel = _formControl.getValidationModel();
		if (_registeredValidationModel != null) {
			for (CompositionRowModel rowModel : _rowModels) {
				TLObjectOverlay rowOverlay = rowModel.getRowOverlay();
				if (rowOverlay != null) {
					_registeredValidationModel.addOverlay(rowOverlay, rowOverlay.getBase());
				}
			}
		}

		// Register as participant.
		_formControl.registerParticipant(this);

		// Rebuild the presentation in edit mode.
		buildContent(overlayList, true);
	}

	private void exitEditMode(TLObject currentObject) {
		cleanupEditState();

		// Rebuild the presentation in view mode.
		List<TLObject> rows = _binding.readRows(currentObject);
		buildContent(rows, false);
	}

	/**
	 * Discards all edit-session state: cell validation listeners, row overlays, the field model,
	 * and the participant registration.
	 */
	protected final void cleanupEditState() {
		// Remove cell validation listeners and row overlays from the model they were
		// registered on (which may differ from the current model after setupEditSession).
		if (_registeredValidationModel != null) {
			for (ConstraintValidationListener listener : _cellValidationListeners) {
				_registeredValidationModel.removeConstraintValidationListener(listener);
			}
			for (CompositionRowModel row : _rowModels) {
				TLObjectOverlay overlay = row.getRowOverlay();
				if (overlay != null) {
					_registeredValidationModel.removeOverlay(overlay);
				}
			}
			_registeredValidationModel = null;
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
	public void applyState() {
		if (_fieldModel == null) {
			return;
		}
		for (CompositionRowModel row : _rowModels) {
			TLObjectOverlay overlay = row.getRowOverlay();
			if (overlay != null && overlay.isDirty()) {
				overlay.apply();
			}
		}

		if (isTransientOwner()) {
			// A transient owner keeps transient row objects: write the current row list into the
			// main overlay so applying it transfers the rows to the owner. The whole transient tree
			// becomes persistent in one piece when the owner is copied persistent (e.g. by a create
			// dialog's or a new-entry form's submit chain).
			List<TLObject> bases = new ArrayList<>();
			for (TLObject obj : _fieldModel.getCurrentList()) {
				bases.add(obj instanceof TLObjectOverlay overlay ? overlay.getBase() : obj);
			}
			_binding.updateMembership(_formControl, bases);
		}
	}

	/**
	 * Whether the form's base object is transient, so row objects stay transient as well and are
	 * persisted together with the owner.
	 */
	private boolean isTransientOwner() {
		TLObjectOverlay overlay = _formControl.getOverlay();
		return overlay != null && overlay.getBase() != null && overlay.getBase().tTransient();
	}

	@Override
	public void persist(Transaction tx) {
		if (_fieldModel == null) {
			return;
		}
		if (isTransientOwner()) {
			// Rows of a transient owner are not persisted individually - they are transferred to
			// the owner by applyState() and become persistent together with it.
			return;
		}
		List<TLObject> currentList = _fieldModel.getCurrentList();

		// Persist new transient objects and build the persisted row list.
		List<TLObject> persistedList = new ArrayList<>();
		for (TLObject obj : currentList) {
			if (obj instanceof TLObjectOverlay) {
				persistedList.add(((TLObjectOverlay) obj).getBase());
			} else if (obj.tTransient()) {
				TLObject persisted = persistTransientObject(obj);
				persistedList.add(persisted);
			} else {
				persistedList.add(obj);
			}
		}

		// Write the row set back and apply the binding's remove semantics to orphaned objects.
		_binding.commit(tx, _formControl, persistedList, _originalPersistentObjects);
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

	// -- Row Manipulation --

	/**
	 * Adds a new, empty row to the row set.
	 *
	 * <p>
	 * Creates a transient object of the binding's create type and appends it to the current list.
	 * Publishes the membership change and refreshes the presentation.
	 * </p>
	 *
	 * @return The created transient row object, or {@code null} if no edit session is running or the
	 *         binding offers no row creation.
	 */
	public TLObject addRow() {
		return addRow(null);
	}

	/**
	 * Adds a new row to the row set, optionally initialized by the given function.
	 *
	 * @param initializer
	 *        Initializes attribute values of the created transient object before it is added to the
	 *        list, or {@code null} for an empty row.
	 * @return The created transient row object, or {@code null} if no edit session is running or the
	 *         binding offers no row creation.
	 */
	public TLObject addRow(Consumer<TLObject> initializer) {
		if (_fieldModel == null) {
			return null;
		}

		// Determine the create type from the binding.
		List<TLClass> createTypes = _binding.getCreateTypes();
		if (createTypes.isEmpty()) {
			return null;
		}
		TLClass targetType = createTypes.get(0);

		// Create transient object.
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(targetType);
		if (initializer != null) {
			initializer.accept(transientObject);
		}

		// Register with validation model so constraints are evaluated.
		FormValidationModel validationModel = _registeredValidationModel;
		if (validationModel != null) {
			validationModel.addOverlay(transientObject, null);
		}

		// Create row model.
		CompositionRowModel rowModel = CompositionRowModel.forNew(transientObject);
		_rowModels.add(rowModel);

		// Replace the list instead of mutating it in place, so dirty tracking (comparison against
		// the initial snapshot) and value-change events observe the membership change.
		List<TLObject> currentList = new ArrayList<>(_fieldModel.getCurrentList());
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		// Publish the membership change.
		_binding.updateMembership(_formControl, currentList);

		// Notify validation model that the bound attribute changed.
		notifyValidationModelChanged();

		_formControl.updateDirtyState();

		refreshRows();

		return transientObject;
	}

	/**
	 * Removes the given row from the row set. The binding's remove semantics are applied on commit,
	 * not here.
	 *
	 * @param rowObject
	 *        The row object (overlay or transient) to remove.
	 */
	public void removeRow(TLObject rowObject) {
		if (_fieldModel == null) {
			return;
		}
		int index = _fieldModel.getCurrentList().indexOf(rowObject);
		if (index >= 0) {
			deleteRow(rowObject, index);
		}
	}

	/**
	 * Deletes a row from the row set.
	 *
	 * @param rowObject
	 *        The row to remove from the current list.
	 * @param rowIndex
	 *        The row's index in {@link #rowModels()}.
	 */
	public void deleteRow(TLObject rowObject, int rowIndex) {
		if (_fieldModel == null) {
			return;
		}

		// Replace the list instead of mutating it in place, so dirty tracking (comparison against
		// the initial snapshot) and value-change events observe the membership change.
		List<TLObject> currentList = new ArrayList<>(_fieldModel.getCurrentList());
		currentList.remove(rowObject);
		_fieldModel.setValue(currentList);

		// Remove row model and unregister from validation model.
		if (rowIndex >= 0 && rowIndex < _rowModels.size()) {
			CompositionRowModel removedRow = _rowModels.remove(rowIndex);
			TLObjectOverlay removedOverlay = removedRow.getRowOverlay();
			if (removedOverlay != null) {
				_fieldModel.removeRowOverlay(removedOverlay);
			}
			FormValidationModel validationModel = _registeredValidationModel;
			if (validationModel != null) {
				if (removedOverlay != null) {
					validationModel.removeOverlay(removedOverlay);
				} else if (rowObject.tTransient()) {
					validationModel.removeOverlay(rowObject);
				}
			}
		}

		// Publish the membership change.
		_binding.updateMembership(_formControl, currentList);

		// Notify validation model that the bound attribute changed.
		notifyValidationModelChanged();

		_formControl.updateDirtyState();

		refreshRows();
	}

	/**
	 * Notifies the {@link FormValidationModel} that the bound attribute value has changed, so that
	 * constraints on the attribute (e.g. min count) are re-evaluated.
	 */
	private void notifyValidationModelChanged() {
		FormValidationModel validationModel = _registeredValidationModel;
		TLObjectOverlay overlay = _formControl.getOverlay();
		TLStructuredTypePart boundPart = _binding.getBoundPart();
		if (validationModel != null && overlay != null && boundPart != null) {
			validationModel.onValueChanged(overlay, boundPart);
		}
	}

	// -- Cell Model Support --

	/**
	 * Builds the editable control for a cell: the attribute's field control bound to the cell's
	 * {@link AttributeFieldModel}, which is created once per row and column (resolved through
	 * {@link FieldControlService}, so enums and references edit as selects) and wired for validation
	 * and dirty tracking.
	 *
	 * @param context
	 *        The React context for control creation.
	 * @param row
	 *        The row object (overlay or transient).
	 * @param columnName
	 *        The model attribute name of the column.
	 * @return The editable cell control, or {@code null} if the cell cannot be edited (the attribute
	 *         does not resolve or the row is not part of the session).
	 */
	protected final ReactControl buildEditCellControl(ReactContext context, TLObject row, String columnName) {
		TLStructuredType type = row.tType();
		TLStructuredTypePart part = type.getPart(columnName);
		if (part == null) {
			return null;
		}
		CompositionRowModel rowModel = findRowModel(row);
		if (rowModel == null) {
			return null;
		}
		AttributeFieldModel cellFieldModel = rowModel.getColumnModel(columnName);
		if (cellFieldModel == null) {
			cellFieldModel = FieldControlService.getInstance().createModel(row, part, _formControl);
			cellFieldModel.setEditable(true);
			rowModel.putColumnModel(columnName, cellFieldModel);

			// Wire validation from FormValidationModel to this cell.
			wireCellValidation(cellFieldModel, row, part);

			// Add dirty propagation and live validation trigger.
			addCellListener(cellFieldModel, row);
		}
		return FieldControlService.getInstance().createFieldControl(context, part, cellFieldModel);
	}

	/**
	 * Wires a {@link ConstraintValidationListener} that propagates validation results from the
	 * {@link FormValidationModel} to the cell's {@link AttributeFieldModel}.
	 */
	protected final void wireCellValidation(AttributeFieldModel model, TLObject rowObject,
			TLStructuredTypePart part) {
		FormValidationModel validationModel = _registeredValidationModel;
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
	protected final void addCellListener(AttributeFieldModel cellFieldModel, TLObject rowObject) {
		cellFieldModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_formControl.updateDirtyState();

				// Reveal validation errors after user interaction.
				cellFieldModel.setRevealed(true);

				// Trigger live constraint re-evaluation.
				FormValidationModel validationModel = _registeredValidationModel;
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

	/**
	 * The row model tracking the given row object, or {@code null} if unknown.
	 */
	protected final CompositionRowModel findRowModel(TLObject rowObject) {
		for (CompositionRowModel row : _rowModels) {
			if (row.getRowObject() == rowObject) {
				return row;
			}
		}
		return null;
	}

	// -- Utility --

	/**
	 * Persists a transient object by creating a new persistent KB object of the same type and
	 * copying all attribute values.
	 */
	private TLObject persistTransientObject(TLObject transientObj) {
		TLClass type = (TLClass) transientObj.tType();
		TLObject persisted = DynamicModelService.getFactoryFor(type.getModule().getName())
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
	}
}
