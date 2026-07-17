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
import java.util.List;
import java.util.Set;
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
 * Base class for form controls that display and edit the value of a composition reference inline.
 *
 * <p>
 * Owns the complete editing lifecycle independent of the concrete presentation: implements
 * {@link FormModelListener} to react to form state changes (enter/exit edit mode, object switch)
 * and {@link FormParticipant} for validation, apply, cancel, and dirty tracking. On entering edit
 * mode, creates {@link TLObjectOverlay}s for existing composed objects and stores the overlay list
 * in the main form overlay. On save, persists new transient objects, applies overlay changes, and
 * removes orphaned objects.
 * </p>
 *
 * <p>
 * Subclasses render the composed objects: {@link #buildContent(List, boolean)} builds the
 * presentation for a row list, {@link #refreshRows()} updates it after {@link #addRow()} or
 * {@link #deleteRow(TLObject, int)} changed the list.
 * </p>
 */
public abstract class AbstractCompositionControl extends ReactControl
		implements FormModelListener, FormParticipant {

	private final FormControl _formControl;

	private final String _compositionAttributeName;

	private TLStructuredTypePart _compositionPart;

	private CompositionFieldModel _fieldModel;

	private final List<CompositionRowModel> _rowModels = new ArrayList<>();

	/** The persistent objects at edit-mode entry, for orphan detection on save. */
	private List<TLObject> _originalPersistentObjects;

	/** Validation listeners registered per cell, for cleanup on exit edit mode. */
	private final List<ConstraintValidationListener> _cellValidationListeners = new ArrayList<>();

	/** The validation model on which cell listeners were registered, for correct cleanup. */
	private FormValidationModel _registeredValidationModel;

	/**
	 * Creates a new {@link AbstractCompositionControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param compositionAttributeName
	 *        The name of the composition reference attribute on the parent object.
	 * @param reactModule
	 *        The React module rendering this control.
	 */
	public AbstractCompositionControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, String reactModule) {
		super(context, null, reactModule);
		_formControl = formControl;
		_compositionAttributeName = compositionAttributeName;

		formControl.addFormModelListener(this);
	}

	/**
	 * Initializes the presentation from the form's current object.
	 *
	 * <p>
	 * Must be called after construction. Resolves the composition reference and builds the initial
	 * content; without a current object or resolvable reference, builds empty view-mode content.
	 * </p>
	 */
	public void init() {
		TLObject currentObject = _formControl.getCurrentObject();
		if (currentObject != null) {
			_compositionPart = resolveCompositionPart(currentObject);
		}
		if (currentObject == null || _compositionPart == null) {
			buildContent(Collections.emptyList(), false);
			return;
		}
		buildContent(readCompositionValue(currentObject), _formControl.isEditMode());
	}

	/**
	 * Builds the presentation for the given composed objects.
	 *
	 * @param rows
	 *        The composed objects to display: overlays and transient objects in edit mode,
	 *        persistent objects in view mode.
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
	 * The name of the composition reference attribute on the parent object.
	 */
	protected final String compositionAttributeName() {
		return _compositionAttributeName;
	}

	/**
	 * The resolved composition reference, or {@code null} before {@link #init()} or when the
	 * current object's type does not declare the attribute.
	 */
	protected final TLStructuredTypePart compositionPart() {
		return _compositionPart;
	}

	/**
	 * The field model holding the edited row list, or {@code null} outside edit mode.
	 */
	protected final CompositionFieldModel fieldModel() {
		return _fieldModel;
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
		// Clean up previous edit state (e.g. after executeApply resets the session).
		cleanupEditState();

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
		List<TLObject> composedObjects = readCompositionValue(currentObject);
		buildContent(composedObjects, false);
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
			// A transient owner keeps transient composition rows: write the current row list into
			// the main overlay so applying it transfers the rows to the owner. The whole transient
			// tree becomes persistent in one piece when the owner is copied persistent (e.g. by a
			// create dialog's or a new-entry form's submit chain).
			List<TLObject> bases = new ArrayList<>();
			for (TLObject obj : _fieldModel.getCurrentList()) {
				bases.add(obj instanceof TLObjectOverlay overlay ? overlay.getBase() : obj);
			}
			_formControl.getOverlay().tUpdate(_compositionPart, bases);
		}
	}

	/**
	 * Whether the form's base object is transient, so composition rows stay transient as well and
	 * are persisted together with the owner.
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

		// Persist new transient objects and build the persisted reference list.
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

		// Update composition reference in the main overlay so the main overlay.apply()
		// writes the correct persisted list to the base object.
		_formControl.getOverlay().tUpdate(_compositionPart, persistedList);

		// Delete orphaned objects (present in original but absent from current).
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

	// -- Row Manipulation --

	/**
	 * Adds a new, empty row to the composition.
	 *
	 * <p>
	 * Creates a transient object of the composition reference's target type and appends it to the
	 * current list. Updates the main overlay and refreshes the presentation.
	 * </p>
	 */
	public void addRow() {
		addRow(null);
	}

	/**
	 * Adds a new row to the composition, optionally initialized by the given function.
	 *
	 * @param initializer
	 *        Initializes attribute values of the created transient object before it is added to
	 *        the list, or {@code null} for an empty row.
	 */
	public void addRow(Consumer<TLObject> initializer) {
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

		// Add to current list.
		List<TLObject> currentList = _fieldModel.getCurrentList();
		currentList.add(transientObject);
		_fieldModel.setValue(currentList);

		// Update main overlay.
		_formControl.getOverlay().tUpdate(_compositionPart, currentList);

		// Notify validation model that the composition reference changed.
		notifyValidationModelChanged();

		_formControl.updateDirtyState();

		refreshRows();
	}

	/**
	 * Deletes a row from the composition.
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
			FormValidationModel validationModel = _registeredValidationModel;
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

		refreshRows();
	}

	/**
	 * Notifies the {@link FormValidationModel} that the composition reference value has changed,
	 * so that constraints on the reference (e.g. min count) are re-evaluated.
	 */
	private void notifyValidationModelChanged() {
		FormValidationModel validationModel = _registeredValidationModel;
		TLObjectOverlay overlay = _formControl.getOverlay();
		if (validationModel != null && overlay != null && _compositionPart != null) {
			validationModel.onValueChanged(overlay, _compositionPart);
		}
	}

	// -- Cell Model Support --

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

	private TLStructuredTypePart resolveCompositionPart(TLObject object) {
		TLObject base = object;
		if (object instanceof TLObjectOverlay) {
			base = ((TLObjectOverlay) object).getBase();
		}
		TLStructuredType type = base.tType();
		return type.getPart(_compositionAttributeName);
	}

	/**
	 * Reads the composition reference value of the given object as a list.
	 */
	@SuppressWarnings("unchecked")
	protected final List<TLObject> readCompositionValue(TLObject object) {
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

	/**
	 * The target type of the composition reference, or {@code null} if the reference is not
	 * resolved.
	 */
	protected final TLClass resolveTargetType() {
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
